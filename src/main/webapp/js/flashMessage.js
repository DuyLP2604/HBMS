document.addEventListener("DOMContentLoaded", () => {

    const flash = document.querySelector("#flash-message");

    if (!flash) {
        return;
    }

    const closeButton =
        flash.querySelector(".flash-close");

    const closeFlash = () => {

        flash.classList.add("flash-hide");

        flash.addEventListener(
            "animationend",
            () => {
                flash.remove();
            },
            { once: true }
        );
    };

    closeButton?.addEventListener(
        "click",
        closeFlash
    );

    setTimeout(
        closeFlash,
        4000
    );

});


