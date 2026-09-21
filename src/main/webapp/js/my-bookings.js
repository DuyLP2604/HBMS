document.addEventListener("DOMContentLoaded", function () {

    const countdownElements
            = document.querySelectorAll(
                    ".payment-countdown"
                    );

    let pageReloading = false;

    function updateCountdowns() {

        countdownElements.forEach(function (element) {

            const deadline
                    = Number(element.dataset.deadline);

            const remaining
                    = deadline - Date.now();

            if (remaining <= 0) {

                element.textContent = "(Expired)";

                if (!pageReloading) {
                    pageReloading = true;

                    setTimeout(function () {
                        window.location.reload();
                    }, 1000);
                }

                return;
            }

            const totalSeconds
                    = Math.floor(remaining / 1000);

            const minutes
                    = Math.floor(totalSeconds / 60);

            const seconds
                    = totalSeconds % 60;

            element.textContent
                    = "("
                    + minutes
                    + ":"
                    + String(seconds).padStart(2, "0")
                    + " remaining)";
        });
    }

    updateCountdowns();

    setInterval(
            updateCountdowns,
            1000
            );
});
