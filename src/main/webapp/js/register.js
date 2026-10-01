function nextStep() {

    const username =
            document.getElementById("username").value.trim();

    const password =
            document.getElementById("password").value;

    const confirm =
            document.getElementById("confirmPassword").value;


    if (username === "") {
        alert("Please enter a username.");
        return;
    }


    if (password.length < 6) {
        alert("Password must contain at least 6 characters.");
        return;
    }


    if (password !== confirm) {
        alert("Passwords do not match.");
        return;
    }


    document
            .getElementById("step1")
            .classList.add("register-step-hidden");

    document
            .getElementById("step2")
            .classList.remove("register-step-hidden");
}


function backStep() {

    document
            .getElementById("step2")
            .classList.add("register-step-hidden");

    document
            .getElementById("step1")
            .classList.remove("register-step-hidden");
}

function handleNationalityChange() {
    const nationalitySelect =
            document.getElementById("nationalityID");

    const phoneInput = document.getElementById("phone");
    const phoneRequiredMark =
            document.getElementById("phoneRequiredMark");

    const cccdGroup = document.getElementById("cccdGroup");
    const cccdInput = document.getElementById("cccd");

    const passportGroup =
            document.getElementById("passportGroup");

    const passportInput =
            document.getElementById("passportNumber");

    const nationalityID = nationalitySelect.value;
    const isVietnamese = nationalityID === "N01";

    // No nationality selected
    if (nationalityID === "") {
        phoneInput.required = false;
        phoneRequiredMark.style.display = "none";

        cccdInput.required = false;
        passportInput.required = false;

        cccdGroup.classList.add("register-step-hidden");
        passportGroup.classList.add("register-step-hidden");
        return;
    }

    if (isVietnamese) {
        // Vietnamese customer
        phoneInput.required = true;
        phoneRequiredMark.style.display = "inline";

        cccdGroup.classList.remove("register-step-hidden");
        cccdInput.required = true;

        passportGroup.classList.add("register-step-hidden");
        passportInput.required = false;
        passportInput.value = "";

    } else {
        // Foreign customer
        phoneInput.required = false;
        phoneRequiredMark.style.display = "none";

        cccdGroup.classList.add("register-step-hidden");
        cccdInput.required = false;
        cccdInput.value = "";

        passportGroup.classList.remove("register-step-hidden");
        passportInput.required = true;
    }
}

document.addEventListener("DOMContentLoaded", function () {
    handleNationalityChange();
});

