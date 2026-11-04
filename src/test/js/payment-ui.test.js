'use strict';
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const code = fs.readFileSync(path.join(__dirname, '../../main/webapp/js/payment.js'), 'utf8');
let checks = 0;
function check(value, message) {
    assert.ok(value, message);
    checks++;
}

function fixture({ wallet = 300, deadline = 180000 } = {}) {
    const timers = new Map();
    let timer = 0;
    let now = 1000000;
    const classes = new Set();
    const formEvents = {};
    const pageEvents = {};
    const qr = { value: 'PT01', checked: false, disabled: false };
    const walletMethod = { value: 'PT02', checked: false, disabled: wallet < 1000 };
    const methods = [qr, walletMethod];
    const ids = {
        paymentForm: {
            dataset: { amount: '1000', walletContribution: String(wallet) },
            querySelectorAll: selector => {
                assert.equal(selector, 'input[name="methodID"]');
                return methods;
            },
            reportValidity: () => methods.some(method => method.checked && !method.disabled),
            addEventListener: (name, callback) => { formEvents[name] = callback; }
        },
        paymentButton: {},
        paymentButtonLabel: {},
        paymentDeadlineBanner: {
            classList: {
                toggle: (name, force) => force ? classes.add(name) : classes.delete(name),
                contains: name => classes.has(name)
            }
        },
        paymentCountdown: deadline === null ? null : { dataset: { deadline: typeof deadline === 'number' ? String(now + deadline) : deadline } },
        expiredMessage: { hidden: true },
        paymentBreakdown: { hidden: true },
        walletContributionDisplay: {},
        externalContributionDisplay: {},
        walletSplitOption: wallet > 0 && wallet < 1000 ? { hidden: true } : null,
        useWallet: wallet > 0 && wallet < 1000 ? { value: 'true', checked: false, disabled: true } : null
    };
    vm.runInNewContext(code, {
        document: { getElementById: id => ids[id] ?? null, addEventListener: (name, callback) => callback() },
        window: { addEventListener: (name, callback) => { pageEvents[name] = callback; } },
        Intl, Date: { now: () => now },
        setInterval: callback => { timers.set(++timer, callback); return timer; },
        clearInterval: id => timers.delete(id)
    });
    return {
        ids, timers, pageEvents,
        choose: value => {
            for (const method of methods) method.checked = method.value === value;
            formEvents.change();
        },
        useWallet: checked => {
            ids.useWallet.checked = checked;
            formEvents.change();
        },
        advance: ms => {
            now += ms;
            for (const callback of [...timers.values()]) callback();
        },
        submit: () => {
            let prevented = false;
            formEvents.submit({ preventDefault: () => { prevented = true; } });
            return prevented;
        }
    };
}

const qr = fixture();
check(qr.ids.paymentButton.disabled && qr.ids.walletSplitOption.hidden, 'Select a method before paying');
check(qr.ids.paymentCountdown.textContent === '03:00', 'The initial payment shows time remaining');
qr.advance(60000);
check(qr.ids.paymentDeadlineBanner.classList.contains('bp-deadline-urgent'), 'Two minutes remaining highlights the deadline');
qr.choose('PT01');
check(!qr.ids.paymentButton.disabled && !qr.ids.walletSplitOption.hidden, 'VietQR enables checkout and offers split payment');
check(qr.ids.paymentButtonLabel.textContent === 'Continue to PayOS · 1,000 VND', 'VietQR initially charges the full amount');
qr.useWallet(true);
check(!qr.ids.paymentBreakdown.hidden && qr.ids.walletContributionDisplay.textContent === '300 VND'
    && qr.ids.externalContributionDisplay.textContent === '700 VND', 'Split payment shows both amounts');
check(qr.ids.paymentButtonLabel.textContent === 'Continue to PayOS · 700 VND', 'VietQR displays the actual external amount');
qr.useWallet(false);
check(qr.ids.paymentBreakdown.hidden && qr.ids.paymentButtonLabel.textContent === 'Continue to PayOS · 1,000 VND',
    'Turning off split payment restores the full amount');
qr.useWallet(true);
check(!qr.submit() && qr.submit() && qr.ids.paymentButtonLabel.textContent === 'Opening PayOS…', 'PayOS accepts one submit');
check(qr.ids.useWallet.checked && !qr.ids.useWallet.disabled, 'Split payment remains in the submitted form');
qr.pageEvents.pageshow({ persisted: true });
check(!qr.ids.paymentButton.disabled && qr.timers.size === 1, 'Back navigation restores the button and one timer');
qr.advance(120000);
check(qr.ids.paymentCountdown.textContent === 'Expired' && !qr.ids.expiredMessage.hidden
    && qr.ids.paymentButton.disabled && qr.ids.useWallet.disabled && qr.timers.size === 0 && qr.submit(),
    'Expiry stops the timer and blocks checkout');

const fullWallet = fixture({ wallet: 1000, deadline: null });
fullWallet.choose('PT02');
check(fullWallet.ids.walletSplitOption === null && fullWallet.timers.size === 0
    && fullWallet.ids.paymentButtonLabel.textContent === 'Pay with wallet · 1,000 VND', 'Balance payment can be paid directly by wallet');
check(!fullWallet.submit() && fullWallet.ids.paymentButtonLabel.textContent === 'Processing wallet payment…', 'Wallet submit uses the wallet message');

const insufficient = fixture();
insufficient.choose('PT02');
check(insufficient.ids.paymentButton.disabled && insufficient.submit(), 'An insufficient wallet cannot be submitted alone');

const invalid = fixture({ deadline: '' });
invalid.choose('PT01');
check(invalid.ids.paymentCountdown.textContent === 'Unavailable' && !invalid.ids.expiredMessage.hidden
    && invalid.ids.paymentButton.disabled && invalid.submit(), 'An unavailable deadline blocks initial payment');

async function checkPayOsReturn() {
    const waiting = 'Waiting for PayOS to confirm your payment.';
    const pending = {
        textContent: waiting,
        dataset: { statusUrl: '/payment-status?orderCode=123', returnUrl: '/payment?orderCode=123' }
    };
    const timers = [];
    let status = 'PENDING';
    let unavailable = false;
    let navigation;
    vm.runInNewContext(code, {
        document: { getElementById: id => id === 'payos-pending' ? pending : null, addEventListener: (name, callback) => callback() },
        window: { addEventListener() {}, location: { replace: url => { navigation = url; } } },
        fetch: async url => {
            assert.equal(url, pending.dataset.statusUrl);
            return { ok: !unavailable, status: unavailable ? 503 : 200, json: async () => ({ status }) };
        },
        setTimeout: callback => timers.push(callback),
        clearTimeout() {}
    });

    await new Promise(resolve => setImmediate(resolve));
    check(!navigation && timers.length === 1 && pending.textContent === waiting, 'A pending PayOS return keeps checking');
    unavailable = true;
    await timers.shift()();
    check(!navigation && pending.textContent.includes('Trying again'), 'A temporary failure can retry');
    unavailable = false;
    await timers.shift()();
    check(pending.textContent === waiting, 'Successful status checks restore the waiting message');
    status = 'PAID';
    await timers.shift()();
    check(navigation === pending.dataset.returnUrl, 'A paid PayOS order opens the receipt');
    console.log('PASS: ' + checks + ' payment UI state checks');
}

checkPayOsReturn().catch(error => {
    console.error(error);
    process.exitCode = 1;
});
