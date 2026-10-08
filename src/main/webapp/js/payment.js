document.addEventListener("DOMContentLoaded", function ()
{
    const form = document.getElementById("paymentForm");
    const paymentButton = document.getElementById("paymentButton");
    const paymentMethod = document.getElementById("methodID");
    const countdown = document.getElementById("paymentCountdown");
    const expiredMessage = document.getElementById("expiredMessage");
    const walletNotice = document.getElementById("walletPaymentNotice");
    const originalButtonContent = paymentButton ? paymentButton.innerHTML : "";
    const rawDeadline = countdown ? countdown.dataset.deadline || "" : "";
    const deadline = rawDeadline.trim() === "" ? NaN : Number(rawDeadline);
    const hasDeadline = countdown !== null;
    const validDeadline = Number.isFinite(deadline) && deadline > 0;
    let submitted = false;
    let countdownInterval = null;
    function updateWalletNotice()
    {
        if (walletNotice)
        {
            walletNotice.classList.toggle("d-none", !paymentMethod || paymentMethod.value !== "PT09");
        }
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
        if (expiredMessage)
        {
            expiredMessage.textContent = invalidDeadline ? "The payment deadline is unavailable. Reload the page." : "The payment time limit has expired.";
            expiredMessage.classList.remove("d-none");
        }
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
            if (paymentMethod && paymentMethod.value === "PT09" && form.dataset.walletCanPay !== "true")
            {
                event.preventDefault();
                paymentMethod.setCustomValidity("Your wallet cannot cover this payment. Select another method or reload the page.");
                paymentMethod.reportValidity();
                paymentMethod.setCustomValidity("");
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
        if (paymentButton)
        {
            paymentButton.disabled = false;
            paymentButton.innerHTML = originalButtonContent;
        }
        if (paymentMethod)
        {
            paymentMethod.disabled = false;
        }
        startCountdown();
        updateWalletNotice();
    });
});
