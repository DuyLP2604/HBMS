/*
 * Client-side validation for the admin "Add employee" / "Update employee" forms.
 *
 * Rules and messages mirror util/EmployeeValidator.java. The server always
 * validates again, so this script only gives faster feedback; keep both in sync.
 */
document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("employeeForm");
    if (!form) {
        return;
    }

    const SHIFTS = ["Morning", "Afternoon", "Night"];
    const SALARY_MAX = 1000000000;

    const NAME_PATTERN = /^[\p{L}\p{M}][\p{L}\p{M} .'\u2019-]*$/u;
    const PHONE_PATTERN = /^0\d{9}$/;
    const EMAIL_PATTERN = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}$/;
    const USERNAME_PATTERN = /^[A-Za-z0-9._-]+$/;
    const SALARY_PATTERN = /^\d{1,10}(\.\d{1,2})?$/;

    function normalizePhone(value) {
        let digits = value.trim().replace(/[\s.\-()]/g, "");
        if (digits.startsWith("+84")) {
            digits = "0" + digits.substring(3);
        } else if (digits.startsWith("84") && digits.length === 11) {
            digits = "0" + digits.substring(2);
        }
        return digits;
    }

    // Each rule returns an error message, or "" when the value is valid.
    const rules = {
        name: function (field) {
            const value = field.value.trim().replace(/\s+/g, " ");
            if (!value) {
                return "Full name is required.";
            }
            if (value.length < 2) {
                return "Full name must be at least 2 characters.";
            }
            if (value.length > 100) {
                return "Full name must not exceed 100 characters.";
            }
            if (!NAME_PATTERN.test(value)) {
                return "Full name may only contain letters, spaces, dots, apostrophes and hyphens.";
            }
            return "";
        },
        phone: function (field) {
            const value = normalizePhone(field.value);
            if (!value) {
                return "Phone number is required.";
            }
            if (!PHONE_PATTERN.test(value)) {
                return "Phone number must have 10 digits and start with 0 (e.g. 0901234567).";
            }
            return "";
        },
        email: function (field) {
            const value = field.value.trim();
            if (!value) {
                return "";
            }
            if (value.length > 100) {
                return "Email must not exceed 100 characters.";
            }
            if (!EMAIL_PATTERN.test(value)) {
                return "Invalid email address (e.g. name@gmail.com).";
            }
            return "";
        },
        address: function (field) {
            return field.value.trim().length > 200 ? "Address must not exceed 200 characters." : "";
        },
        position: function (field) {
            return field.value ? "" : "Position is required.";
        },
        shift: function (field) {
            const value = field.value;
            return !value || SHIFTS.includes(value) ? "" : "Please select a valid work shift.";
        },
        salary: function (field) {
            if (field.validity && field.validity.badInput) {
                return "Salary must be a valid number with at most 2 decimal places.";
            }
            const value = field.value.trim();
            if (!value) {
                return "";
            }
            if (value.startsWith("-")) {
                return "Salary must not be negative.";
            }
            if (!SALARY_PATTERN.test(value)) {
                return "Salary must be a valid number with at most 2 decimal places.";
            }
            if (Number(value) > SALARY_MAX) {
                return "Salary must not exceed 1,000,000,000 VND.";
            }
            return "";
        },
        username: function (field) {
            const value = field.value.trim();
            if (!value) {
                return "Username is required.";
            }
            if (value.length < 4 || value.length > 50) {
                return "Username must be 4-50 characters.";
            }
            if (!USERNAME_PATTERN.test(value)) {
                return "Username may only contain letters, digits, dots, underscores and hyphens.";
            }
            return "";
        },
        password: function (field) {
            const value = field.value;
            if (!value) {
                return "Password is required.";
            }
            if (value.length < 6 || value.length > 50) {
                return "Password must be 6-50 characters.";
            }
            if (/\s/.test(value)) {
                return "Password must not contain spaces.";
            }
            return "";
        }
    };

    // Only fields that exist in this form (the update form has no username/password).
    const fields = Object.keys(rules)
            .map(function (name) {
                return form.elements[name];
            })
            .filter(function (field) {
                // elements[...] also matches by id, so skip read-only display inputs that have no name.
                return field && field.name;
            });

    function showError(field, message) {
        const feedback = form.querySelector('[data-feedback-for="' + field.name + '"]');
        if (feedback) {
            feedback.textContent = message;
        }
        field.classList.toggle("is-invalid", message !== "");
        field.setAttribute("aria-invalid", message !== "" ? "true" : "false");
    }

    function validateField(field) {
        const message = rules[field.name](field);
        showError(field, message);
        return message === "";
    }

    fields.forEach(function (field) {
        // Validate when leaving the field, then live while the user fixes it.
        field.addEventListener("blur", function () {
            validateField(field);
        });
        const liveEvent = field.tagName === "SELECT" ? "change" : "input";
        field.addEventListener(liveEvent, function () {
            if (field.classList.contains("is-invalid")) {
                validateField(field);
            }
        });
    });

    form.addEventListener("submit", function (event) {
        // Trim text fields (never the password) before they are sent.
        fields.forEach(function (field) {
            if (field.name !== "password" && field.type !== "number" && field.tagName === "INPUT") {
                field.value = field.value.trim();
            }
        });

        let firstInvalid = null;
        fields.forEach(function (field) {
            if (!validateField(field) && !firstInvalid) {
                firstInvalid = field;
            }
        });

        if (firstInvalid) {
            event.preventDefault();
            firstInvalid.focus();
        }
    });
});
