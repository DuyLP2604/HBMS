document.addEventListener("DOMContentLoaded", function ()
{
    const form = document.getElementById("paymentForm");
    const paymentButton = document.getElementById("paymentButton");
    const paymentMethod = document.getElementById("methodID");
    const countdown = document.getElementById("paymentCountdown");
    const expiredMessage = document.getElementById("expiredMessage");
    const walletNotice = document.getElementById("walletPaymentNotice");
    const qrContainer = document.getElementById("vietqr-container");
    const qrImage = document.getElementById("vietqr-img");
    const useWallet = document.getElementById("useWallet");
    const walletDisplay = document.getElementById("walletContributionDisplay");
    const externalDisplay = document.getElementById("externalContributionDisplay");
    const amountCents = form ? Math.round(Number(form.dataset.amount) * 100) : 0;
    const walletCents = form ? Math.round(Number(form.dataset.walletContribution) * 100) : 0;
    const walletCoversPayment = form && form.dataset.walletCanPay === "true";
    const currency = new Intl.NumberFormat("en-US", { maximumFractionDigits: 2 });
    let previousExternalMethod = "";
    let walletSelectedAutomatically = false;
    let expired = false;
    const originalButtonContent = paymentButton ? paymentButton.innerHTML : "";
    const rawDeadline = countdown ? countdown.dataset.deadline || "" : "";
    const deadline = rawDeadline.trim() === "" ? NaN : Number(rawDeadline);
    const hasDeadline = countdown !== null;
    const validDeadline = Number.isFinite(deadline) && deadline > 0;
    let submitted = false;
    let countdownInterval = null;
    function updateWalletNotice()
    {
        const walletOnly = useWallet && useWallet.checked && walletCoversPayment;
        if (paymentMethod)
        {
            if (walletOnly)
            {
                if (!walletSelectedAutomatically)
                {
                    previousExternalMethod = paymentMethod.value === "PT02" ? "" : paymentMethod.value;
                }
                paymentMethod.value = "PT02";
                walletSelectedAutomatically = true;
            }
            else if (walletSelectedAutomatically)
            {
                paymentMethod.value = previousExternalMethod;
                walletSelectedAutomatically = false;
            }
            paymentMethod.required = !walletOnly;
            paymentMethod.disabled = expired || walletOnly;
        }
        const walletAmount = paymentMethod && paymentMethod.value === "PT02" ? amountCents : (useWallet && useWallet.checked ? walletCents : 0);
        if (walletDisplay)
        {
            walletDisplay.textContent = currency.format(walletAmount / 100) + " VND";
        }
        if (externalDisplay)
        {
            externalDisplay.textContent = currency.format((amountCents - walletAmount) / 100) + " VND";
        }
        if (walletNotice)
        {
            walletNotice.classList.toggle("d-none", walletAmount <= 0);
        }
        const qrSelected = paymentMethod && paymentMethod.value === "PT01" && amountCents > walletAmount && !expired;
        if (qrContainer) {
            qrContainer.classList.toggle("d-none", !qrSelected);
            if (qrSelected && qrImage) {
                const url = walletAmount > 0 ? qrContainer.dataset.walletUrl : qrContainer.dataset.fullUrl;
                if (qrImage.getAttribute("src") !== url) qrImage.setAttribute("src", url);
            }
        }
        if (paymentButton) {
            paymentButton.classList.toggle("d-none", !!qrSelected);
            paymentButton.disabled = expired || submitted || !!qrSelected;
        }
    }
    if (useWallet)
    {
        useWallet.addEventListener("change", updateWalletNotice);
    }
    if (paymentMethod)
    {
        paymentMethod.addEventListener("change", updateWalletNotice);
    }
    updateWalletNotice();
    function stopCountdown()
    {
        if (countdownInterval !== null)
        {
            clearInterval(countdownInterval);
            countdownInterval = null;
        }
    }
    function expirePaymentPage(invalidDeadline)
    {
        expired = true;
        if (countdown)
        {
            countdown.textContent = invalidDeadline ? "Unavailable" : "Expired";
            countdown.classList.remove("text-danger");
            countdown.classList.add("text-secondary");
        }
        if (paymentButton)
        {
            paymentButton.disabled = true;
            paymentButton.textContent = invalidDeadline ? "Reload Payment Page" : "Payment Expired";
        }
        if (paymentMethod)
        {
            paymentMethod.disabled = true;
        }
        if (useWallet)
        {
            useWallet.disabled = true;
        }
        if (expiredMessage)
        {
            expiredMessage.textContent = invalidDeadline ? "The payment deadline is unavailable. Reload the page." : "The payment time limit has expired.";
            expiredMessage.classList.remove("d-none");
        }
        if (qrContainer) qrContainer.classList.add("d-none");
        stopCountdown();
    }
    function updateCountdown()
    {
        if (!hasDeadline)
        {
            return true;
        }
        if (!validDeadline)
        {
            expirePaymentPage(true);
            return false;
        }
        const remaining = deadline - Date.now();
        if (remaining <= 0)
        {
            expirePaymentPage(false);
            return false;
        }
        const totalSeconds = Math.ceil(remaining / 1000);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        countdown.textContent = String(minutes).padStart(2, "0") + ":" + String(seconds).padStart(2, "0");
        return true;
    }
    function startCountdown()
    {
        stopCountdown();
        if (updateCountdown() && hasDeadline)
        {
            countdownInterval = setInterval(updateCountdown, 1000);
        }
    }
    startCountdown();
    if (form)
    {
        form.addEventListener("submit", function (event)
        {
            if (submitted)
            {
                event.preventDefault();
                return;
            }
            if (!updateCountdown())
            {
                event.preventDefault();
                return;
            }
            if (!form.checkValidity())
            {
                event.preventDefault();
                form.reportValidity();
                return;
            }
            if (paymentMethod && paymentMethod.value === "PT02" && form.dataset.walletCanPay !== "true")
            {
                event.preventDefault();
                paymentMethod.setCustomValidity("Your wallet cannot cover this payment. Select another method or reload the page.");
                paymentMethod.reportValidity();
                paymentMethod.setCustomValidity("");
                return;
            }
            if (paymentMethod && paymentMethod.value === "PT01") {
                event.preventDefault();
                return;
            }
            submitted = true;
            if (paymentButton)
            {
                paymentButton.disabled = true;
                paymentButton.innerHTML = '<span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>Processing Payment...';
            }
        });
    }
    window.addEventListener("pageshow", function (event)
    {
        if (!event.persisted)
        {
            return;
        }
        submitted = false;
        expired = false;
        if (paymentButton)
        {
            paymentButton.disabled = false;
            paymentButton.innerHTML = originalButtonContent;
        }
        if (paymentMethod)
        {
            paymentMethod.disabled = false;
        }
        if (useWallet)
        {
            useWallet.disabled = walletCents <= 0;
        }
        startCountdown();
        updateWalletNotice();
    });
});
