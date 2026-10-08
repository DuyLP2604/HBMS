function showStep(step)
{
    document.getElementById("step1").classList.toggle("register-step-hidden", step !== 1);
    document.getElementById("step2").classList.toggle("register-step-hidden", step !== 2);
}

function validateAccount()
{
    const usernameInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");
    const confirmInput = document.getElementById("confirmPassword");
    const fields = [usernameInput, passwordInput, confirmInput];
    usernameInput.value = usernameInput.value.trim();
    for (const field of fields)
    {
        field.setCustomValidity("");
    }
    if (passwordInput.value.trim() === "" || passwordInput.value.length < 6)
    {
        passwordInput.setCustomValidity("Password must contain at least 6 characters and cannot be blank.");
    }
    if (confirmInput.value !== passwordInput.value)
    {
        confirmInput.setCustomValidity("Passwords do not match.");
    }
    for (const field of fields)
    {
        if (!field.checkValidity())
        {
            showStep(1);
            field.reportValidity();
            return false;
        }
    }
    return true;
}

function validatePersonalInformation()
{
    showStep(2);
    handleNationalityChange();
    const fields = document.getElementById("step2").querySelectorAll("input, select, textarea");
    for (const field of fields)
    {
        field.setCustomValidity("");
        field.value = field.value.trim();
    }
    for (const field of fields)
    {
        if (!field.checkValidity())
        {
            field.reportValidity();
            return false;
        }
    }
    return true;
}

function nextStep()
{
    if (!validateAccount())
    {
        return;
    }
    showStep(2);
    handleNationalityChange();
    const nationalitySelect = document.getElementById("nationalityID");
    if (nationalitySelect.value === "")
    {
        nationalitySelect.focus();
    }
    else
    {
        document.getElementById("fullname").focus();
    }
}

function backStep()
{
    showStep(1);
    document.getElementById("username").focus();
}

function handleNationalityChange()
{
    const nationalitySelect = document.getElementById("nationalityID");
    const phoneInput = document.getElementById("phone");
    const phoneRequiredMark = document.getElementById("phoneRequiredMark");
    const isVietnamese = nationalitySelect.value === "N01";
    phoneInput.required = isVietnamese;
    phoneInput.setCustomValidity("");
    phoneRequiredMark.classList.toggle("register-step-hidden", !isVietnamese);
}

document.addEventListener("DOMContentLoaded", function ()
{
    const form = document.querySelector(".register-form");
    if (!form)
    {
        return;
    }
    handleNationalityChange();
    form.addEventListener("input", function (event)
    {
        const field = event.target;
        if (typeof field.setCustomValidity === "function")
        {
            field.setCustomValidity("");
        }
        if (field.id === "password" || field.id === "confirmPassword")
        {
            document.getElementById("confirmPassword").setCustomValidity("");
        }
    });
    form.addEventListener("submit", function (event)
    {
        if (!document.getElementById("step1").classList.contains("register-step-hidden"))
        {
            event.preventDefault();
            nextStep();
            return;
        }
        if (!validateAccount())
        {
            event.preventDefault();
            return;
        }
        if (!validatePersonalInformation())
        {
            event.preventDefault();
            return;
        }
    });
    form.noValidate = true;
});