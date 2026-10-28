document.addEventListener("DOMContentLoaded", function ()
{
    const form = document.getElementById("addCustomerForm");
    const phoneInput = document.getElementById("phone");
    const usernameInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");

    if (!form || !phoneInput || !usernameInput || !passwordInput)
    {
        return;
    }

    function syncUsername()
    {
        usernameInput.value = phoneInput.value.trim();
        usernameInput.setCustomValidity("");
    }

    usernameInput.readOnly = true;
    phoneInput.addEventListener("input", syncUsername);
    syncUsername();

    form.addEventListener("input", function (event)
    {
        const field = event.target;
        if (typeof field.setCustomValidity === "function")
        {
            field.setCustomValidity("");
        }
    });

    form.addEventListener("submit", function (event)
    {
        const fields = form.querySelectorAll(
                "input[type='text'], input[type='tel'], input[type='email']"
        );

        for (const field of fields)
        {
            field.value = field.value.trim();
            field.setCustomValidity("");
        }

        syncUsername();

        passwordInput.setCustomValidity("");
        if (passwordInput.value.trim() === "")
        {
            passwordInput.setCustomValidity("Please enter a password");
        }

        if (!form.checkValidity())
        {
            event.preventDefault();
            form.reportValidity();
        }
    });
});