package util;

import entity.Hotel;
import java.util.function.BiConsumer;
import java.util.function.Function;

/** One editable image of the home page. */
public final class HotelImageSlot {

    private final String key;          // form field name and file name prefix
    private final String label;
    private final String defaultFile;
    private final Function<Hotel, String> getter;
    private final BiConsumer<Hotel, String> setter;

    public HotelImageSlot(String key, String label, String defaultFile,
            Function<Hotel, String> getter,
            BiConsumer<Hotel, String> setter) {
        this.key = key;
        this.label = label;
        this.defaultFile = defaultFile;
        this.getter = getter;
        this.setter = setter;
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public String getDefaultFile() {
        return defaultFile;
    }

    public String getImageFile(Hotel hotel) {
        return getter.apply(hotel);
    }

    public void setImageFile(Hotel hotel, String fileName) {
        setter.accept(hotel, fileName);
    }
}
