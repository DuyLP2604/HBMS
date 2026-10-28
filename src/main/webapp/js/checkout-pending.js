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
    const outputs = page.querySelectorAll('[data-checkout-countdown]');
    const clock = deadline.querySelector('[data-checkout-clock]');
    const title = deadline.querySelector('[data-checkout-deadline-title]');
    const note = deadline.querySelector('[data-checkout-clock-note]');
    const status = deadline.querySelector('[data-checkout-time-status]');
    let lastState = '';
    const pay = page.querySelector('[data-checkout-pay]');
    const expired = page.querySelector('[data-checkout-expired]');
    let interval;
    function tick() {
        // Use the server snapshot and elapsed time, independent of the customer's clock.
        const seconds = Math.max(0, Math.ceil((remainingAtLoad - (performance.now() - startedAt)) / 1000));
        const state = seconds === 0 ? 'expired' : seconds <= 300 ? 'urgent' : 'open';
        deadline.classList.toggle('cp-deadline-urgent', seconds <= 300);
        page.classList.toggle('cp-payment-urgent', seconds <= 300);
        if (state !== lastState) {
            title.textContent = seconds === 0 ? 'The payment window has ended' : seconds <= 300 ? 'Your payment deadline is approaching' : 'Complete payment to confirm your booking';
            note.textContent = seconds === 0 ? 'Check the latest booking status below' : seconds <= 300 ? 'Complete payment now to avoid expiry' : 'Please complete your payment in time';
            if (state !== 'open') status.textContent = title.textContent;
            lastState = state;
        }
        if (seconds === 0) {
            outputs.forEach(output => output.textContent = 'Payment window ended');
            clock.textContent = '00:00';
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
        clock.textContent = `${hours ? String(hours).padStart(2, '0') + ':' : ''}${String(minutes).padStart(2, '0')}:${String(rest).padStart(2, '0')}`;
        outputs.forEach(output => output.textContent = `${hours ? hours + 'h ' : ''}${minutes}m ${String(rest).padStart(2, '0')}s remaining`);
    }
    interval = setInterval(tick, 1000);
    document.addEventListener('visibilitychange', tick);
    window.addEventListener('pageshow', tick);
    tick();
})();
