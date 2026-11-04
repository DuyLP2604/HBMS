const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const source = fs.readFileSync(path.join(__dirname, '../../main/webapp/js/checkin-payment.js'), 'utf8');
const flush = () => new Promise(resolve => setImmediate(resolve));

function fixture(activeOrder = false) {
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
    form.dataset = {requiredRooms: '1'};
    form.action = 'http://localhost:8080/HBMS/process-checkin';
    form.elements = {bookingID: {value: 'BK0041'}, csrfToken: {value: 'csrf'}, checkInToken: {value: 'checkin'}};
    form.reportValidity = () => true;
    form.querySelectorAll = selector => selector === '.checkin-room' ? [] : [{checkValidity: () => true, removeAttribute() {}}];

    const order = {
        orderCode: '1234567890123', status: 'PENDING',
        imageUrl: '/HBMS/process-checkin?action=payOsImage', checkoutUrl: 'https://pay.payos.vn/web/test'
    };
    let current = activeOrder ? order : {status: 'NONE'};
    let scheduled;
    let created = 0;

    vm.runInNewContext(source, {
        document: {getElementById: id => ids[id] || null}, window: node(),
        URL, URLSearchParams, clearTimeout() {},
        setTimeout(callback) { scheduled = callback; },
        fetch: async (url, options = {}) => {
            if (options.method === 'POST') {
                created++;
                assert.equal(url, form.action);
                assert.deepEqual(Object.fromEntries(options.body), {
                    action: 'createPayOs', bookingID: 'BK0041', checkInToken: 'checkin', csrfToken: 'csrf'
                });
                current = order;
            } else {
                assert.equal(url.searchParams.get('action'), 'payOsStatus');
            }
            return {ok: true, json: async () => current};
        }
    });

    return {ids, method, form, setStatus: status => { current = {...order, status}; },
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
    console.log('PASS: Check-in PayOS create, poll, resume, expiry and cash fallback');
}

main().catch(error => { console.error(error); process.exitCode = 1; });
