(function () {
    'use strict';
    const checkInDate = document.getElementById('checkInDate');
    const paymentOption = document.getElementById('paymentOption');
    const deposit = document.getElementById('counterDepositOption');
    if (!checkInDate || !paymentOption || !deposit) return;

    function updatePaymentOption() {
        const depositAllowed = checkInDate.value > checkInDate.min;
        if (!depositAllowed && paymentOption.value === 'DEPOSIT') {
            paymentOption.value = 'FULL';
        }
        deposit.disabled = !depositAllowed;
        deposit.hidden = !depositAllowed;
    }

    checkInDate.addEventListener('change', updatePaymentOption);
    window.addEventListener('pageshow', updatePaymentOption);
    updatePaymentOption();
}());
