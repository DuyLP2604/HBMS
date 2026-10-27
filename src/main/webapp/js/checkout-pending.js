(() => {
    'use strict';
    const page = document.querySelector('[data-checkout-page]');
    const deadline = page && page.querySelector('[data-checkout-deadline]');
    if (!deadline) return;
    const deadlineTime = Number(deadline.dataset.checkoutDeadline);
    const serverTime = Number(deadline.dataset.checkoutServerNow);
    if (!Number.isFinite(deadlineTime) || !Number.isFinite(serverTime)) return;
    const startedAt = performance.now();
    const remainingAtLoad = deadlineTime - serverTime;
    const output = deadline.querySelector('[data-checkout-countdown]');
    const pay = page.querySelector('[data-checkout-pay]');
    const expired = page.querySelector('[data-checkout-expired]');
    let interval;
    function tick() {
        // Use the server snapshot and elapsed time, independent of the customer's clock.
        const seconds = Math.max(0, Math.ceil((remainingAtLoad - (performance.now() - startedAt)) / 1000));
        deadline.classList.toggle('cp-deadline-urgent', seconds <= 300);
        if (seconds === 0) {
            output.textContent = 'Payment window ended';
            pay.removeAttribute('href');
            pay.setAttribute('aria-disabled', 'true');
            pay.classList.add('bc-button-disabled');
            expired.hidden = false;
            clearInterval(interval);
            return;
        }
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const rest = seconds % 60;
        output.textContent = `${hours ? hours + 'h ' : ''}${minutes}m ${String(rest).padStart(2, '0')}s remaining`;
    }
    interval = setInterval(tick, 1000);
    document.addEventListener('visibilitychange', tick);
    window.addEventListener('pageshow', tick);
    tick();
})();
