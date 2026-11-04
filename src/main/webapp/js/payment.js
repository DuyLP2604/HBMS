document.addEventListener("DOMContentLoaded", function () {
    const pending = document.getElementById("payos-pending");
    if (pending) {
        const waitingMessage = pending.textContent.trim();
        const stopAt = Date.now() + 120000;
        let timer;
        let stopped = false;
        async function checkPayment() {
            try {
                const response = await fetch(pending.dataset.statusUrl, { cache: "no-store", credentials: "same-origin" });
                if (stopped) return;
                if ([401, 404].includes(response.status)) {
                    pending.textContent = "This payment status is unavailable. Sign in and check your booking.";
                    return;
                }
                if (!response.ok) throw new Error("Payment status unavailable");
                const result = await response.json();
                if (stopped) return;
                if (result.status === "PAID" || result.status === "FAILED") {
                    window.location.replace(pending.dataset.returnUrl);
                    return;
                }
                if (pending.textContent !== waitingMessage) pending.textContent = waitingMessage;
            } catch {
                if (!stopped) pending.textContent = "Unable to check payment status right now. Trying again...";
            }
            if (stopped) return;
            if (Date.now() < stopAt) {
                timer = setTimeout(checkPayment, 4000);
            } else {
                pending.textContent = "Confirmation is taking longer than usual. Use Check again or view your booking.";
            }
        }
        window.addEventListener("pagehide", () => {
            stopped = true;
            clearTimeout(timer);
        });
        checkPayment();
        return;
    }

    const form = document.getElementById("paymentForm");
    const countdown = document.getElementById("paymentCountdown");
    const deadlineBanner = document.getElementById("paymentDeadlineBanner");
    const expiredMessage = document.getElementById("expiredMessage");
    const deadline = Number(countdown?.dataset.deadline);
    let countdownInterval;
    let expired = false;
    let submitted = false;

    const methods = form ? Array.from(form.querySelectorAll('input[name="methodID"]')) : [];
    const paymentButton = document.getElementById("paymentButton");
    const buttonLabel = document.getElementById("paymentButtonLabel");
    const useWallet = document.getElementById("useWallet");
    const splitOption = document.getElementById("walletSplitOption");
    const breakdown = document.getElementById("paymentBreakdown");
    const walletDisplay = document.getElementById("walletContributionDisplay");
    const externalDisplay = document.getElementById("externalContributionDisplay");

    const amountCents = form ? Math.round(Number(form.dataset.amount) * 100) : 0;
    const walletCents = form ? Math.round(Number(form.dataset.walletContribution) * 100) : 0;
    const currency = new Intl.NumberFormat("en-US", { maximumFractionDigits: 2 });

    function formatAmount(cents) {
        return currency.format(cents / 100) + " VND";
    }

    function selectedMethod() {
        return methods.find(method => method.checked && !method.disabled)?.value;
    }

    function updatePayment() {
        if (!form || submitted) return;

        const method = selectedMethod();
        if (useWallet) {
            splitOption.hidden = method !== "PT01";
            useWallet.disabled = splitOption.hidden || expired;
            if (splitOption.hidden) useWallet.checked = false;
        }
        const split = Boolean(useWallet && !useWallet.disabled && useWallet.checked);
        breakdown.hidden = !split;
        const externalCents = amountCents - (split ? walletCents : 0);
        if (split) {
            walletDisplay.textContent = formatAmount(walletCents);
            externalDisplay.textContent = formatAmount(externalCents);
        }

        paymentButton.disabled = expired || !method;
        if (expired) {
            buttonLabel.textContent = deadline > 0 ? "Payment expired" : "Reload payment page";
        } else if (method === "PT01") {
            buttonLabel.textContent = "Continue to PayOS · " + formatAmount(externalCents);
        } else if (method === "PT02") {
            buttonLabel.textContent = "Pay with wallet · " + formatAmount(amountCents);
        } else {
            buttonLabel.textContent = "Select a payment method";
        }
    }

    function updateCountdown() {
        if (!countdown) return true;

        const validDeadline = Number.isFinite(deadline) && deadline > 0;
        const remaining = deadline - Date.now();
        deadlineBanner.classList.toggle("bp-deadline-urgent", !validDeadline || remaining <= 120000);
        if (!validDeadline || remaining <= 0) {
            expired = true;
            countdown.textContent = validDeadline ? "Expired" : "Unavailable";
            expiredMessage.textContent = validDeadline
                ? "The payment time limit has expired. View your booking for its current status."
                : "The payment deadline is unavailable. Reload this page to check again.";
            expiredMessage.hidden = false;
            clearInterval(countdownInterval);
            updatePayment();
            return false;
        }

        const totalSeconds = Math.ceil(remaining / 1000);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        countdown.textContent = String(minutes).padStart(2, "0") + ":" + String(seconds).padStart(2, "0");
        return true;
    }

    function startCountdown() {
        clearInterval(countdownInterval);
        if (countdown && updateCountdown()) {
            countdownInterval = setInterval(updateCountdown, 1000);
        }
    }

    if (form) {
        form.addEventListener("change", updatePayment);
        form.addEventListener("submit", function (event) {
            const method = selectedMethod();
            if (submitted || !updateCountdown() || !method) {
                event.preventDefault();
                return;
            }
            if (!form.reportValidity()) {
                event.preventDefault();
                return;
            }

            submitted = true;
            paymentButton.disabled = true;
            buttonLabel.textContent = method === "PT01" ? "Opening PayOS…" : "Processing wallet payment…";
        });
    }

    startCountdown();
    updatePayment();

    window.addEventListener("pageshow", function (event) {
        if (!event.persisted) return;
        submitted = false;
        startCountdown();
        updatePayment();
    });
});
