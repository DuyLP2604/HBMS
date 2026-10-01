document.addEventListener("DOMContentLoaded", function () {
    const form =
            document.getElementById("paymentForm");

    const paymentButton =
            document.getElementById("paymentButton");

    const paymentMethod =
            document.getElementById("methodID");

    const countdown =
            document.getElementById("paymentCountdown");

    const expiredMessage =
            document.getElementById("expiredMessage");

    if (!countdown) {
        return;
    }

    const deadline =
            Number(countdown.dataset.deadline);

    let submitted = false;
    let countdownInterval = null;

    function expirePaymentPage() {
        countdown.textContent = "Expired";
        countdown.classList.remove("text-danger");
        countdown.classList.add("text-secondary");

        if (paymentButton) {
            paymentButton.disabled = true;
            paymentButton.textContent =
                    "Payment Expired";
        }

        if (paymentMethod) {
            paymentMethod.disabled = true;
        }

        if (expiredMessage) {
            expiredMessage.classList.remove("d-none");
        }

        if (countdownInterval !== null) {
            clearInterval(countdownInterval);
        }
    }

    function updateCountdown() {
        const remaining =
                deadline - Date.now();

        if (!Number.isFinite(deadline)
                || remaining <= 0) {

            expirePaymentPage();
            return;
        }

        const totalSeconds =
                Math.floor(remaining / 1000);

        const minutes =
                Math.floor(totalSeconds / 60);

        const seconds =
                totalSeconds % 60;

        countdown.textContent =
                String(minutes).padStart(2, "0")
                + ":"
                + String(seconds).padStart(2, "0");
    }

    updateCountdown();

    countdownInterval =
            setInterval(updateCountdown, 1000);

    if (form) {
        form.addEventListener(
                "submit",
                function (event) {

                    if (submitted) {
                        event.preventDefault();
                        return;
                    }

                    if (Date.now() >= deadline) {
                        event.preventDefault();
                        expirePaymentPage();
                        return;
                    }

                    submitted = true;

                    paymentButton.disabled = true;

                    paymentButton.innerHTML =
                            "<span class=\"spinner-border "
                            + "spinner-border-sm me-2\" "
                            + "aria-hidden=\"true\"></span>"
                            + "Processing Payment...";
                }
        );
    }
});
