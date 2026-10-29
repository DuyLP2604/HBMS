(function () {
    'use strict';
    const form = document.getElementById('checkInForm');
    if (!form) return;
    const method = document.getElementById('checkInMethod');
    const bankGroup = document.getElementById('checkInBankReferenceGroup');
    const bankReference = document.getElementById('checkInBankReference');
    const walletGroup = document.getElementById('checkInWalletAuthorization');
    const confirmation = document.getElementById('collectionConfirmed');
    const submit = document.getElementById('confirmCheckIn');
    const initialLabel = submit.innerHTML;
    const progress = form.querySelector('[data-document-progress]');
    const hint = form.querySelector('[data-checkin-hint]');
    const requiredRooms = Number(form.dataset.requiredRooms || form.querySelectorAll('.checkin-room').length);
    let submitting = false;

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
        const documentsReady = requiredRooms > 0 && representatives.length === requiredRooms
                && Array.from(form.querySelectorAll('input[name="cccd"]')).every(input => input.checkValidity());
        const paymentReady = !method || (method.value !== '' && confirmation.checked
                && (!walletGroup || Array.from(walletGroup.querySelectorAll('input')).every(input => input.disabled || input.checkValidity()))
                && (!bankReference || bankReference.disabled || bankReference.checkValidity()));
        submit.disabled = submitting || !documentsReady || !paymentReady;
        if (hint) hint.textContent = !documentsReady ? 'Enter valid guest documents for every assigned room.'
                : !paymentReady ? 'Choose a payment method and confirm the room payment.'
                : 'Guest documents are ready. You can confirm check-in.';
    }
    if (method && walletGroup) {
        const updateMethod = function () {
            const useWallet = method.value === 'PT02';
            if (bankGroup && bankReference) {
                const useQr = method.value === 'PT01';
                bankGroup.hidden = !useQr;
                bankReference.disabled = !useQr;
                bankReference.required = useQr;
            }
            walletGroup.hidden = !useWallet;
            walletGroup.querySelectorAll('input').forEach(input => {
                input.required = useWallet;
                input.disabled = !useWallet;
                if (!useWallet) input.value = '';
            });
            updateState();
        };
        method.addEventListener('change', updateMethod);
        updateMethod();
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
        submit.textContent = 'Confirming check-in...';
    });
    window.addEventListener('pageshow', () => {
        submitting = false;
        submit.innerHTML = initialLabel;
        updateState();
    });
    updateState();
}());
