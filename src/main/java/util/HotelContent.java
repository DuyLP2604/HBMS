package util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Helpers shared by the home page and the hotel admin page.
 */
public final class HotelContent {

    /** Images used when the database has no (or an unsafe) image name. */
    public static final String DEFAULT_HERO_IMAGE = "hotel2.jpg";
    public static final String DEFAULT_ROOM_IMAGE = "hotel3.jpg";
    public static final String DEFAULT_POOL_IMAGE = "hotel.jpg";
    public static final String DEFAULT_RESTAURANT_IMAGE = "hotel4.jpg";

    /** Letters, digits, dot, dash and underscore only: no path characters. */
    private static final Pattern SAFE_FILE_NAME
            = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]{0,98}$");

    private HotelContent() {
    }

    /**
     * Returns the stored image file name when it is a plain, safe file name,
     * otherwise the default one.
     */
    public static String safeImage(String stored, String defaultName) {
        if (stored != null) {
            String name = stored.trim();
            if (SAFE_FILE_NAME.matcher(name).matches() && !name.contains("..")) {
                return name;
            }
        }
        return defaultName;
    }

    /**
     * Splits the stored address into lines: one address per line, spaces
     * trimmed, empty lines removed. Handles both \n and \r\n.
     */
    public static List<String> addressLines(String address) {
        List<String> lines = new ArrayList<>();

        if (address == null) {
            return lines;
        }

        for (String line : address.split("\\R")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                lines.add(trimmed);
            }
        }

        return lines;
    }
}
