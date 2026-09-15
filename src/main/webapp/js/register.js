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

