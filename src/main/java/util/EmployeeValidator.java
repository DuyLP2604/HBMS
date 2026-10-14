package util;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Server-side validation rules for the admin "Employee management" forms.
 *
 * The rules mirror the database (see DB_v3.sql: EMPLOYEE / USERS) and the
 * client-side rules in webapp/js/employeeForm.js. Keep the three in sync.
 *
 * Every validate* method returns an error message, or null when the value is OK.
 * Keys of the maps returned by validateEmployee/validateAccount are the form
 * field names, so the JSP can show each message next to its input.
 */
public final class EmployeeValidator {

    public static final List<String> SHIFTS = List.of("Morning", "Afternoon", "Night");

    public static final int NAME_MIN = 2;
    public static final int NAME_MAX = 100;
    public static final int EMAIL_MAX = 100;
    public static final int ADDRESS_MAX = 200;
    public static final int USERNAME_MIN = 4;
    public static final int USERNAME_MAX = 50;
    public static final int PASSWORD_MIN = 6;
    public static final int PASSWORD_MAX = 50;
    public static final BigDecimal SALARY_MAX = new BigDecimal("1000000000");

    // Letters (any language, including Vietnamese diacritics), spaces, . ' - ; no digits.
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}\\p{M}][\\p{L}\\p{M} .'\u2019-]*$");
    // Vietnamese phone number after normalisation: 10 digits starting with 0.
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$");
    private static final Pattern EMAIL_PATTERN
            = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");
    // Up to 10 integer digits and at most 2 decimals (column is DECIMAL(12,2)).
    private static final Pattern SALARY_PATTERN = Pattern.compile("^\\d{1,10}(\\.\\d{1,2})?$");

    private EmployeeValidator() {
    }

    /* ------------------------------------------------------------------ */
 /* Normalisation                                                       */
 /* ------------------------------------------------------------------ */
    /**
     * Trims and collapses repeated whitespace. Returns null for blank input.
     */
    public static String normalizeName(String value) {
        if (value == null) {
            return null;
        }
        String result = value.trim().replaceAll("\\s+", " ");
        return result.isEmpty() ? null : result;
    }

    /**
     * Removes spaces, dots, dashes and brackets, and turns +84 / 84 prefixes into 0.
     * Returns null for blank input. The result is what gets stored and compared
     * for uniqueness, so "090 123 4567" and "+84901234567" are the same number.
     */
    public static String normalizePhone(String value) {
        if (value == null) {
            return null;
        }
        String digits = value.trim().replaceAll("[\\s.\\-()]", "");
        if (digits.isEmpty()) {
            return null;
        }
        if (digits.startsWith("+84")) {
            digits = "0" + digits.substring(3);
        } else if (digits.startsWith("84") && digits.length() == 11) {
            digits = "0" + digits.substring(2);
        }
        return digits;
    }

    public static String normalizeEmail(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    /* ------------------------------------------------------------------ */
 /* Field rules                                                         */
 /* ------------------------------------------------------------------ */
    public static String validateName(String name) {
        if (name == null) {
            return "Full name is required.";
        }
        if (name.length() < NAME_MIN) {
            return "Full name must be at least " + NAME_MIN + " characters.";
        }
        if (name.length() > NAME_MAX) {
            return "Full name must not exceed " + NAME_MAX + " characters.";
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            return "Full name may only contain letters, spaces, dots, apostrophes and hyphens.";
        }
        return null;
    }

    /**
     * Phone is required: the database has UNIQUE(Phone), and SQL Server allows
     * only one NULL in such a column, so a second employee without a phone
     * number would be rejected by the database anyway.
     */
    public static String validatePhone(String phone) {
        if (phone == null) {
            return "Phone number is required.";
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return "Phone number must have 10 digits and start with 0 (e.g. 0901234567).";
        }
        return null;
    }

    /**
     * Email is optional; when present it must be well formed.
     */
    public static String validateEmail(String email) {
        if (email == null) {
            return null;
        }
        if (email.length() > EMAIL_MAX) {
            return "Email must not exceed " + EMAIL_MAX + " characters.";
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Invalid email address (e.g. name@gmail.com).";
        }
        return null;
    }

    public static String validateAddress(String address) {
        if (address != null && address.length() > ADDRESS_MAX) {
            return "Address must not exceed " + ADDRESS_MAX + " characters.";
        }
        return null;
    }

    /**
     * Position must be one of the values offered by the dropdown, so a crafted
     * request cannot create a position such as "receptionist " that breaks the
     * permission checks which compare positions by name.
     */
    public static String validatePosition(String position, Collection<String> allowedPositions) {
        if (position == null) {
            return "Position is required.";
        }
        if (allowedPositions == null || !allowedPositions.contains(position)) {
            return "Please select a valid position.";
        }
        return null;
    }

    /**
     * Shift is optional; when present it must be one of {@link #SHIFTS}.
     */
    public static String validateShift(String shift) {
        if (shift != null && !SHIFTS.contains(shift)) {
            return "Please select a valid work shift.";
        }
        return null;
    }

    /**
     * Salary is optional; when present it must be a non-negative number with
     * at most 2 decimals and not above {@link #SALARY_MAX}.
     */
    public static String validateSalary(String raw) {
        String value = normalizeOptional(raw);
        if (value == null) {
            return null;
        }
        if (value.startsWith("-")) {
            return "Salary must not be negative.";
        }
        if (!SALARY_PATTERN.matcher(value).matches()) {
            return "Salary must be a valid number with at most 2 decimal places.";
        }
        if (new BigDecimal(value).compareTo(SALARY_MAX) > 0) {
            return "Salary must not exceed 1,000,000,000 VND.";
        }
        return null;
    }

    /**
     * Parses a salary that already passed {@link #validateSalary}.
     */
    public static BigDecimal parseSalary(String raw) {
        String value = normalizeOptional(raw);
        return value == null ? null : new BigDecimal(value);
    }

    public static String validateUsername(String username) {
        if (username == null) {
            return "Username is required.";
        }
        if (username.length() < USERNAME_MIN || username.length() > USERNAME_MAX) {
            return "Username must be " + USERNAME_MIN + "-" + USERNAME_MAX + " characters.";
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return "Username may only contain letters, digits, dots, underscores and hyphens.";
        }
        return null;
    }

    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required.";
        }
        if (password.length() < PASSWORD_MIN || password.length() > PASSWORD_MAX) {
            return "Password must be " + PASSWORD_MIN + "-" + PASSWORD_MAX + " characters.";
        }
        if (password.chars().anyMatch(Character::isWhitespace)) {
            return "Password must not contain spaces.";
        }
        return null;
    }

    /* ------------------------------------------------------------------ */
 /* Whole-form validation                                               */
 /* ------------------------------------------------------------------ */
    /**
     * Validates the employee part of the form. Pass values that were already
     * normalised with the normalize* methods (salary is the raw parameter).
     *
     * @return field name -> message; empty when everything is valid
     */
    public static Map<String, String> validateEmployee(String name, String phone, String email,
            String address, String position, String shift, String salaryRaw,
            Collection<String> allowedPositions) {
        Map<String, String> errors = new LinkedHashMap<>();
        put(errors, "name", validateName(name));
        put(errors, "phone", validatePhone(phone));
        put(errors, "email", validateEmail(email));
        put(errors, "address", validateAddress(address));
        put(errors, "position", validatePosition(position, allowedPositions));
        put(errors, "shift", validateShift(shift));
        put(errors, "salary", validateSalary(salaryRaw));
        return errors;
    }

    /**
     * Validates the login credentials entered when adding an employee.
     */
    public static Map<String, String> validateAccount(String username, String password) {
        Map<String, String> errors = new LinkedHashMap<>();
        put(errors, "username", validateUsername(username));
        put(errors, "password", validatePassword(password));
        return errors;
    }

    private static void put(Map<String, String> errors, String field, String message) {
        if (message != null) {
            errors.put(field, message);
        }
    }
}
