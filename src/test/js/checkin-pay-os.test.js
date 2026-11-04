const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const source = fs.readFileSync(path.join(__dirname, '../../main/webapp/js/checkin-payment.js'), 'utf8');
const flush = () => new Promise(resolve => setImmediate(resolve));

function fixture(activeOrder = false, initial = false) {
    function node(value = '') {
        const listeners = {};
        return {
            value, innerHTML: '', disabled: false, checked: false,
            addEventListener: (name, callback) => { listeners[name] = callback; },
            trigger: (name, event = {}) => listeners[name](event),
            getAttribute: () => null,
            querySelector: () => null,
            querySelectorAll: () => []
        };
    }

    const ids = {};
    for (const name of ['checkInForm', 'checkInMethod', 'checkInWalletAuthorization', 'confirmCheckIn',
        'collectionConfirmed', 'checkInCollectionGroup', 'checkInPayOs', 'checkInPayOsCreate',
        'checkInPayOsOrderCode', 'checkInPayOsImage', 'checkInPayOsCheckout', 'checkInPayOsStatus']) {
        ids[name] = node();
    }
    const form = ids.checkInForm;
    const method = ids.checkInMethod;
    method.options = [{value: 'PT01'}, {value: 'PT02'}, {value: 'PT03'}];
    if (initial) {
        method.options = [{value: 'PT03'}, {value: 'PT01'}];
        method.value = 'PT03';
        delete ids.checkInWalletAuthorization;
    }
    form.dataset = initial ? {initialCounterPayment: 'true'} : {requiredRooms: '1'};
    const actionPath = initial ? '/HBMS/staff/counter-booking' : '/HBMS/process-checkin';
    const baseURI = 'https://hbms.example.test' + actionPath + '?bookingID=BK0041';
    // The hidden input named action shadows the form property in the browser.
    form.action = {...node('confirmCheckIn'), toString: () => '[object HTMLInputElement]'};
    form.getAttribute = name => name === 'action' ? actionPath : null;
    form.elements = {action: form.action, bookingID: {value: 'BK0041'}, csrfToken: {value: 'csrf'}};
    if (initial) {
        form.elements.paymentToken = {value: 'counter-payment'};
    } else {
        form.elements.checkInToken = {value: 'checkin'};
    }
    const walletFields = [node(), node()];
    walletFields.forEach(input => { input.checkValidity = () => !input.required || !!input.value; });
    if (!initial) ids.checkInWalletAuthorization.querySelectorAll = () => walletFields;
    form.reportValidity = () => true;
    form.querySelectorAll = selector => selector === '.checkin-room' ? [] : [{checkValidity: () => true, removeAttribute() {}}];

    const order = {
        orderCode: '1234567890123', status: 'PENDING',
        imageUrl: actionPath + '?action=payOsImage', checkoutUrl: 'https://pay.payos.vn/web/test'
    };
    let current = activeOrder ? order : {status: 'NONE'};
    let scheduled;
    let created = 0;
    let reloads = 0;

    vm.runInNewContext(source, {
        document: {baseURI, getElementById: id => ids[id] || null}, window: {...node(), location: {reload: () => { reloads++; }}},
        URL, URLSearchParams, clearTimeout() {},
        setTimeout(callback) { scheduled = callback; },
        fetch: async (url, options = {}) => {
            const requestUrl = new URL(url, baseURI);
            assert.equal(requestUrl.origin, new URL(baseURI).origin);
            assert.equal(requestUrl.pathname, actionPath);
            if (options.method === 'POST') {
                created++;
                assert.equal(requestUrl.search, '');
                const expectedBody = {action: 'createPayOs', bookingID: 'BK0041', csrfToken: 'csrf'};
                Object.assign(expectedBody, initial ? {paymentToken: 'counter-payment'} : {checkInToken: 'checkin'});
                assert.deepEqual(Object.fromEntries(options.body), expectedBody);
                current = order;
            } else {
                assert.equal(requestUrl.searchParams.get('action'), 'payOsStatus');
                assert.equal(requestUrl.searchParams.get('bookingID'), 'BK0041');
            }
            return {ok: true, json: async () => current};
        }
    });

    return {ids, method, form, walletFields, reloads: () => reloads,
        setStatus: status => { current = {...order, status}; },
        tick: () => scheduled(), created: () => created};
}

async function main() {
    const desk = fixture();
    await flush();
    desk.method.value = 'PT01';
    desk.method.trigger('change');
    assert.equal(desk.ids.checkInPayOs.hidden, false);
    assert.equal(desk.ids.collectionConfirmed.disabled, true);
    assert.equal(desk.ids.confirmCheckIn.disabled, true);

    await desk.ids.checkInPayOsCreate.trigger('click');
    assert.equal(desk.created(), 1);
    assert.equal(desk.ids.checkInPayOsImage.hidden, false);
    assert.equal(desk.method.options[2].disabled, true);
    assert.equal(desk.ids.confirmCheckIn.disabled, true);
    desk.setStatus('PAID');
    await desk.tick();
    assert.equal(desk.ids.checkInPayOsOrderCode.value, '1234567890123');
    assert.equal(desk.ids.confirmCheckIn.disabled, false);
    assert.equal(desk.ids.checkInPayOsImage.hidden, true);

    const reload = fixture(true);
    await flush();
    assert.equal(reload.method.value, 'PT01');
    assert.equal(reload.created(), 0);
    reload.setStatus('EXPIRED');
    await reload.tick();
    assert.equal(reload.ids.checkInPayOsCreate.hidden, false);
    assert.equal(reload.method.options[2].disabled, false);
    reload.method.value = 'PT03';
    reload.ids.collectionConfirmed.checked = true;
    reload.method.trigger('change');
    assert.equal(reload.ids.confirmCheckIn.disabled, false);
    const initial = fixture(false, true);
    await flush();
    assert.equal(initial.ids.checkInPayOs.hidden, true);
    assert.equal(initial.ids.confirmCheckIn.disabled, true);
    initial.ids.collectionConfirmed.checked = true;
    initial.form.trigger('change');
    assert.equal(initial.ids.confirmCheckIn.disabled, false);

    initial.method.value = 'PT01';
    initial.method.trigger('change');
    assert.equal(initial.ids.confirmCheckIn.hidden, true);
    assert.equal(initial.ids.collectionConfirmed.disabled, true);
    await initial.ids.checkInPayOsCreate.trigger('click');
    assert.equal(initial.created(), 1);
    assert.equal(initial.ids.checkInPayOsImage.src, '/HBMS/staff/counter-booking?action=payOsImage');
    assert.equal(initial.method.options[0].disabled, true);
    initial.setStatus('PAID');
    await initial.tick();
    assert.equal(initial.reloads(), 1);

    const restoredInitial = fixture(true, true);
    await flush();
    assert.equal(restoredInitial.created(), 0);
    assert.equal(restoredInitial.method.value, 'PT01');
    assert.equal(restoredInitial.ids.checkInPayOsImage.hidden, false);
    restoredInitial.setStatus('EXPIRED');
    await restoredInitial.tick();
    assert.equal(restoredInitial.method.options[0].disabled, false);
    assert.equal(restoredInitial.ids.checkInPayOsImage.hidden, true);

    console.log('PASS: Counter booking and check-in payment states');
}

main().catch(error => { console.error(error); process.exitCode = 1; });
