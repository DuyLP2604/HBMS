document.addEventListener("DOMContentLoaded", function () {

    const resendButton =
            document.getElementById("resendOtpBtn");

    if (!resendButton) {
        return;
    }


    let remaining =
            parseInt(
                resendButton.dataset.remaining || "0"
            );


    if (remaining <= 0) {

        resendButton.disabled = false;
        resendButton.textContent = "Resend Code";

        return;
    }


    resendButton.disabled = true;


    function updateCountdown() {

        if (remaining <= 0) {

            resendButton.disabled = false;

            resendButton.textContent =
                    "Resend Code";

            return;
        }


        resendButton.textContent =
                "Resend Code (" + remaining + "s)";


        remaining--;


        setTimeout(
            updateCountdown,
            1000
        );
    }


    updateCountdown();

});