(() => {
    'use strict';
    const page = document.querySelector('[data-wishlist-page]');
    if (!page) return;

    const checkout = page.querySelector('#checkoutForm');
    const due = page.querySelector('[data-wl-due-output]');
    const remaining = page.querySelector('[data-wl-remaining-output]');
    function updatePaymentPreview() {
        if (!checkout || !due || !remaining) return;
        const selected = checkout.querySelector('input[name="paymentOption"]:checked');
        // Values are formatted by the server; this preview never sets the charge amount.
        due.textContent = selected ? `${selected.dataset.wlDue} VND` : 'Choose an option';
        due.classList.toggle('wl-unselected', !selected);
        remaining.textContent = selected ? `${selected.dataset.wlRemaining} VND` : '—';
    }
    if (checkout) checkout.addEventListener('change', updatePaymentPreview);
    window.addEventListener('pageshow', updatePaymentPreview);
    updatePaymentPreview();

    page.querySelectorAll('form[data-wl-confirm]').forEach(form => {
        form.addEventListener('submit', event => {
            if (!window.confirm(form.dataset.wlConfirm)) event.preventDefault();
        });
    });
})();
