package util.exception;

import java.util.Map;

/** Carries one validation message per invalid employee form field. */
public final class EmployeeValidationException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    private final transient Map<String, String> errors;

    public EmployeeValidationException(Map<String, String> errors) {
        super("Invalid employee information.");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
