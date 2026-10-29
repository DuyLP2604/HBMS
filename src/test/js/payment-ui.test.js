'use strict';
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const code = fs.readFileSync(path.join(__dirname, '../../main/webapp/js/payment.js'), 'utf8');
let checks = 0;
function check(value, message) { assert.ok(value, message); checks++; }
function fixture(wallet = 300) {
    const timers = new Map();
    let timer = 0;
    let now = 1000000;
    function element(value = '') {
        const classes = new Set(['d-none']);
        const handlers = {};
        const attributes = {};
        return {
            value, checked: false, disabled: false, required: false,
            dataset: {}, innerHTML: 'Pay', textContent: '',
            classList: {
                add: (...names) => names.forEach(name => classes.add(name)),
                remove: (...names) => names.forEach(name => classes.delete(name)),
                contains: name => classes.has(name),
                toggle: (name, force) => { const add = force === undefined ? !classes.has(name) : force; if (add) classes.add(name); else classes.delete(name); }
            },
            addEventListener: (name, callback) => { handlers[name] = callback; },
            emit: (name, event = {}) => handlers[name]?.(event),
            getAttribute: name => attributes[name],
            setAttribute: (name, value) => { attributes[name] = value; },
            checkValidity: () => true,
            reportValidity: () => true,
            setCustomValidity: () => {}
        };
    }
    const ids = Object.fromEntries(['paymentForm', 'paymentButton', 'methodID', 'paymentCountdown', 'expiredMessage',
        'walletPaymentNotice', 'useWallet', 'walletContributionDisplay', 'externalContributionDisplay', 'vietqr-container', 'vietqr-img']
        .map(id => [id, element()]));
    ids.paymentForm.dataset = { amount: '1000', walletContribution: String(wallet), walletCanPay: String(wallet >= 1000) };
    ids.paymentCountdown.dataset.deadline = String(now + 60000);
    ids['vietqr-container'].dataset = { fullUrl: 'https://example.test/qr?amount=1000', walletUrl: 'https://example.test/qr?amount=' + (1000 - wallet) };
    const pageEvents = {};
    vm.runInNewContext(code, {
        document: { getElementById: id => ids[id], addEventListener: (name, callback) => callback() },
        window: { addEventListener: (name, callback) => { pageEvents[name] = callback; } },
        Intl, Date: { now: () => now },
        setInterval: callback => { timers.set(++timer, callback); return timer; },
        clearInterval: id => timers.delete(id)
    });
    return { ids, choose: value => { ids.methodID.value = value; ids.methodID.emit('change'); },
        wallet: checked => { ids.useWallet.checked = checked; ids.useWallet.emit('change'); },
        expire: () => { now += 61000; [...timers.values()].forEach(callback => callback()); },
        submit: () => { let prevented = false; ids.paymentForm.emit('submit', { preventDefault: () => { prevented = true; } }); return prevented; },
        pageEvents, timers };
}
const qr = fixture();
qr.choose('PT01');
check(!qr.ids['vietqr-container'].classList.contains('d-none'), 'VietQR becomes visible');
check(qr.ids['vietqr-img'].getAttribute('src').endsWith('amount=1000'), 'QR starts with the full amount');
check(qr.ids.paymentButton.disabled && qr.submit(), 'QR does not allow a direct payment POST');
qr.wallet(true);
check(qr.ids['vietqr-img'].getAttribute('src').endsWith('amount=700'), 'Wallet contribution updates the QR shortfall');
check(qr.ids.externalContributionDisplay.textContent === '700 VND', 'Displayed shortfall matches the QR');
qr.expire();
check(qr.ids['vietqr-container'].classList.contains('d-none') && qr.ids.paymentButton.disabled && qr.timers.size === 0, 'Expiry hides QR and disables payment');
const fullWallet = fixture(1000);
fullWallet.choose('PT01'); fullWallet.wallet(true);
check(fullWallet.ids.methodID.value === 'PT02' && fullWallet.ids.methodID.disabled, 'A full wallet selects v6 wallet payment');
check(fullWallet.ids['vietqr-container'].classList.contains('d-none') && !fullWallet.ids.paymentButton.disabled, 'A full wallet hides the QR and enables normal payment');
check(!fullWallet.submit(), 'A valid wallet submission remains available');
const insufficient = fixture(300); insufficient.choose('PT02');
check(insufficient.submit(), 'An insufficient wallet cannot be submitted');
console.log('PASS: ' + checks + ' payment UI state checks');
