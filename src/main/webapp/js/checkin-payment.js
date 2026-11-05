// Toggles the front desk payment form and polls for PayOS confirmation.
(function () {
    'use strict';
    const form = document.getElementById('checkInForm');
    if (!form) return;
    const paymentUrl = new URL(form.getAttribute('action'), document.baseURI).href;
    const initialCounterPayment = form.dataset.initialCounterPayment === 'true';
    const method = document.getElementById('checkInMethod');
    const bankGroup = document.getElementById('checkInBankReferenceGroup');
    const bankReference = document.getElementById('checkInBankReference');
    const walletGroup = document.getElementById('checkInWalletAuthorization');
    const confirmation = document.getElementById('collectionConfirmed');
    const collectionGroup = document.getElementById('checkInCollectionGroup');
    const payOsPanel = document.getElementById('checkInPayOs');
    const payOsCreate = document.getElementById('checkInPayOsCreate');
    const payOsCode = document.getElementById('checkInPayOsOrderCode');
    const payOsImage = document.getElementById('checkInPayOsImage');
    const payOsCheckout = document.getElementById('checkInPayOsCheckout');
    const payOsStatus = document.getElementById('checkInPayOsStatus');
    const submit = document.getElementById('confirmCheckIn');
    const initialLabel = submit.innerHTML;
    const progress = form.querySelector('[data-document-progress]');
    const hint = form.querySelector('[data-checkin-hint]');
    const requiredRooms = Number(form.dataset.requiredRooms || form.querySelectorAll('.checkin-room').length);
    let submitting = false;
    let payOsState = 'NONE';
    let payOsBusy = false;
    let pollTimer;

    function usesPayOs() {
        return payOsPanel && method.value === 'PT01';
    }

    function renderPayOs(order) {
        payOsState = order.status;
        payOsCode.value = order.orderCode || '';
        const pending = payOsState === 'PENDING';
        const locked = pending || payOsState === 'PAID' || payOsState === 'FAILED';
        if (locked) method.value = 'PT01';
        for (const option of method.options) {
            option.disabled = locked && option.value !== 'PT01';
        }
        payOsImage.hidden = !pending;
        payOsCheckout.hidden = !pending;
        if (pending) {
            if (payOsImage.getAttribute('src') !== order.imageUrl) payOsImage.src = order.imageUrl;
            payOsCheckout.href = order.checkoutUrl;
        }
        if (payOsState === 'PAID') {
            if (initialCounterPayment) {
                window.location.reload();
                return;
            }
            const roomTotal = form.querySelector('[data-checkin-room-total]');
            const roomPaid = form.querySelector('[data-checkin-room-paid]');
            const roomBalance = form.querySelector('[data-checkin-room-balance]');
            if (roomTotal && roomPaid) roomPaid.textContent = roomTotal.textContent;
            if (roomBalance) roomBalance.textContent = '0 VND';
        }
        payOsStatus.textContent = {
            PENDING: 'Waiting for PayOS to confirm the transfer.',
            PAID: 'Room payment received. You can complete check-in.',
            FAILED: 'The transfer needs review. Check the PayOS order before collecting again.',
            EXPIRED: 'This QR expired. Verify any transfer before generating a new code.'
        }[payOsState] || '';
        syncPaymentMethod();
    }

    async function readPayOsResponse(response) {
        if (!response.ok) throw new Error('Unable to check PayOS. Reload the booking and try again.');
        return response.json();
    }

    async function pollPayOs() {
        const url = new URL(paymentUrl);
        url.searchParams.set('bookingID', form.elements.bookingID.value);
        url.searchParams.set('action', 'payOsStatus');
        if (payOsCode.value) url.searchParams.set('orderCode', payOsCode.value);
        try {
            renderPayOs(await readPayOsResponse(await fetch(url, {cache: 'no-store'})));
        } catch (error) {
            payOsStatus.textContent = error.message;
        }
        if (payOsState === 'PENDING') pollTimer = setTimeout(pollPayOs, 3000);
    }

    async function createPayOs() {
        if (payOsBusy || payOsState === 'PENDING') return;
        payOsBusy = true;
        updateState();
        payOsStatus.textContent = 'Generating VietQR…';
        const body = new URLSearchParams({
            action: 'createPayOs',
            bookingID: form.elements.bookingID.value,
            csrfToken: form.elements.csrfToken.value
        });
        if (initialCounterPayment) {
            body.set('paymentToken', form.elements.paymentToken.value);
        } else {
            body.set('checkInToken', form.elements.checkInToken.value);
        }
        try {
            const response = await fetch(paymentUrl, {method: 'POST', body});
            if (response.status === 409) {
                const result = await response.json();
                throw new Error(result.message);
            }
            renderPayOs(await readPayOsResponse(response));
            if (payOsState === 'PENDING') {
                clearTimeout(pollTimer);
                pollTimer = setTimeout(pollPayOs, 3000);
            }
        } catch (error) {
            payOsStatus.textContent = error.message;
        } finally {
            payOsBusy = false;
            updateState();
        }
    }

    function updateState() {
        form.querySelectorAll('input[name="cccd"]').forEach(input => {
            if (input.checkValidity()) input.removeAttribute('aria-invalid');
        });
        const representatives = Array.from(form.querySelectorAll('[data-representative-document]'));
        const completed = representatives.filter(input => input.checkValidity()).length;
        if (progress) progress.textContent = completed + ' of ' + requiredRooms + ' representatives entered';
        form.querySelectorAll('.checkin-room').forEach(room => {
            const count = room.querySelectorAll('.checkin-guest-row').length;
            room.querySelector('[data-add-guest]').disabled = count >= 4;
            const label = room.querySelector('[data-room-guest-count]');
            if (label) label.textContent = count + ' of 4 guests';
        });
        const documentsReady = initialCounterPayment || (requiredRooms > 0 && representatives.length === requiredRooms
                && Array.from(form.querySelectorAll('input[name="cccd"]')).every(input => input.checkValidity()));
        const paymentReady = !method || (usesPayOs() ? !initialCounterPayment && payOsState === 'PAID' : (method.value !== ''
                && (confirmation.disabled || confirmation.checked)
                && (!walletGroup || Array.from(walletGroup.querySelectorAll('input')).every(input => input.disabled || input.checkValidity()))
                && (!bankReference || bankReference.disabled || bankReference.checkValidity())));
        if (payOsCreate) {
            payOsCreate.disabled = payOsBusy;
            payOsCreate.hidden = ['PENDING', 'PAID', 'FAILED'].includes(payOsState);
        }
        const submitLabel = submit.querySelector('span');
        if (submitLabel && payOsState === 'PAID') submitLabel.textContent = 'Complete check-in';
        if (initialCounterPayment) submit.hidden = usesPayOs();
        submit.disabled = submitting || payOsBusy || !documentsReady || !paymentReady;
        if (hint) hint.textContent = !documentsReady ? 'Enter valid guest documents for every assigned room.'
                : !paymentReady ? (usesPayOs() ? 'Wait for PayOS to confirm the room payment.'
                        : 'Choose a payment method and confirm the room payment.')
                : 'Guest documents are ready. You can confirm check-in.';
    }
    function syncPaymentMethod() {
        if (!method) return;
        const useWallet = method.value === 'PT02';
        const usePayOs = usesPayOs();
        if (bankGroup && bankReference) {
            const useManualQr = method.value === 'PT01' && !usePayOs;
            bankGroup.hidden = !useManualQr;
            bankReference.disabled = !useManualQr;
            bankReference.required = useManualQr;
        }
        if (payOsPanel) {
            payOsPanel.hidden = !usePayOs;
            const collectConfirmation = initialCounterPayment ? method.value === 'PT03' : !usePayOs;
            collectionGroup.hidden = !collectConfirmation;
            confirmation.disabled = !collectConfirmation;
            confirmation.required = collectConfirmation;
            if (initialCounterPayment && !collectConfirmation) confirmation.checked = false;
        }
        if (walletGroup) {
            walletGroup.hidden = !useWallet;
            walletGroup.querySelectorAll('input').forEach(input => {
                input.required = useWallet;
                input.disabled = !useWallet;
                if (!useWallet) input.value = '';
            });
        }
        updateState();
    }

    if (method) {
        method.addEventListener('change', syncPaymentMethod);
        syncPaymentMethod();
    }
    if (payOsPanel) {
        payOsCreate.addEventListener('click', createPayOs);
        window.addEventListener('pagehide', () => clearTimeout(pollTimer));
        payOsBusy = true;
        updateState();
        pollPayOs().finally(() => {
            payOsBusy = false;
            updateState();
        });
    }
    form.addEventListener('input', updateState);
    form.addEventListener('change', updateState);
    form.addEventListener('focusout', event => {
        if (event.target.matches('input[name="cccd"]')) {
            event.target.setAttribute('aria-invalid', String(event.target.value !== '' && !event.target.checkValidity()));
        }
    });
    form.addEventListener('click', event => {
        const add = event.target.closest('[data-add-guest]');
        const remove = event.target.closest('[data-remove-guest]');
        if (add) {
            const room = add.closest('.checkin-room');
            const guests = room.querySelector('.checkin-guests');
            if (guests.children.length >= 4) return;
            const row = guests.firstElementChild.cloneNode(true);
            const input = row.querySelector('input');
            input.value = '';
            input.removeAttribute('data-representative-document');
            input.removeAttribute('aria-invalid');
            row.querySelector('[data-document-label]').textContent = 'Roommate CCCD';
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'guest-remove';
            button.setAttribute('data-remove-guest', '');
            button.setAttribute('aria-label', 'Remove roommate from room ' + room.dataset.roomNumber);
            button.textContent = '×';
            row.appendChild(button);
            guests.appendChild(row);
            updateState();
            input.focus();
        } else if (remove) {
            const room = remove.closest('.checkin-room');
            remove.closest('.checkin-guest-row').remove();
            updateState();
            room.querySelector('[data-add-guest]').focus();
        }
    });
    form.addEventListener('submit', event => {
        if (submitting || !form.reportValidity() || submit.disabled) {
            event.preventDefault();
            return;
        }
        submitting = true;
        submit.disabled = true;
        submit.textContent = initialCounterPayment ? 'Recording payment...' : 'Confirming check-in...';
    });
    window.addEventListener('pageshow', event => {
        if (event.persisted && payOsPanel) {
            clearTimeout(pollTimer);
            pollPayOs();
        }
        submitting = false;
        submit.innerHTML = initialLabel;
        updateState();
    });
    updateState();
}());
