package util;

import java.util.Collection;
import java.util.Locale;

/**
 * Builds the next employee ID (NV01, NV02, ... NV99, NV100, ...) and the
 * matching username (the ID in lower case: nv32).
 *
 * The number is taken from the highest existing ID compared numerically, not
 * as text: sorting as text would put "NV99" after "NV100" and hand out an ID
 * that already exists.
 */
public final class EmployeeIdGenerator {

    private static final String PREFIX = "NV";

    private EmployeeIdGenerator() {
    }

    /**
     * Highest number found in the given IDs plus one; 1 when there are none.
     * IDs may carry trailing spaces (the column is CHAR(6)) and are ignored
     * when they do not contain digits.
     */
    public static int nextNumber(Collection<String> existingIds) {
        int max = 0;
        if (existingIds != null) {
            for (String id : existingIds) {
                if (id == null) {
                    continue;
                }
                String digits = id.replaceAll("\\D+", "");
                if (digits.isEmpty() || digits.length() > 6) {
                    continue;
                }
                max = Math.max(max, Integer.parseInt(digits));
            }
        }
        return max + 1;
    }

    /**
     * 32 -> "NV32", 5 -> "NV05", 100 -> "NV100".
     */
    public static String formatId(int number) {
        return String.format(PREFIX + "%02d", number);
    }

    /**
     * "NV32" -> "nv32".
     */
    public static String usernameOf(String employeeId) {
        return employeeId.trim().toLowerCase(Locale.ROOT);
    }
}
