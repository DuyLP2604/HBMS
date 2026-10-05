package controller;

import dao.HotelDAO;
import entity.Hotel;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import util.HotelContent;
import util.flash.Flash;
import util.flash.FlashMessage;
import util.flash.FlashType;

/**
 * Lets the admin manager modify the hotel information:
 * - the address (one address per line),
 * - the background image and the three Overview images.
 *
 * The hotel name cannot be changed. The project has a single hotel.
 */
@WebServlet(name = "HotelServlet", urlPatterns = {"/hotel"})
@MultipartConfig(
        maxFileSize = 2 * 1024 * 1024, // 2 MB per image
        maxRequestSize = 10 * 1024 * 1024
)
public class HotelServlet extends HttpServlet {

    private static final String IMAGE_FOLDER = "/assets/images/hotel";
    private static final int ADDRESS_MAX = 500; // HOTEL.Address

    /** File names created by this servlet, so old uploads can be cleaned up. */
    private static final Pattern GENERATED_NAME = Pattern.compile(
            "^hotel-(hero|room|pool|restaurant)-\\d+\\.(jpg|png|webp)$");

    /** One editable image of the home page. */
    private static final class Slot {

        final String key;          // form field name and file name prefix
        final String label;
        final String defaultFile;
        final Function<Hotel, String> getter;
        final BiConsumer<Hotel, String> setter;

        Slot(String key, String label, String defaultFile,
                Function<Hotel, String> getter,
                BiConsumer<Hotel, String> setter) {
            this.key = key;
            this.label = label;
            this.defaultFile = defaultFile;
            this.getter = getter;
            this.setter = setter;
        }
    }

    private static final List<Slot> SLOTS = Arrays.asList(
            new Slot("hero", "Background image (top of the home page)",
                    HotelContent.DEFAULT_HERO_IMAGE,
                    Hotel::getHotelImage, Hotel::setHotelImage),
            new Slot("room", "Overview: Luxury Rooms",
                    HotelContent.DEFAULT_ROOM_IMAGE,
                    Hotel::getRoomImage, Hotel::setRoomImage),
            new Slot("pool", "Overview: Swimming Pool",
                    HotelContent.DEFAULT_POOL_IMAGE,
                    Hotel::getPoolImage, Hotel::setPoolImage),
            new Slot("restaurant", "Overview: Restaurant",
                    HotelContent.DEFAULT_RESTAURANT_IMAGE,
                    Hotel::getRestaurantImage, Hotel::setRestaurantImage)
    );

    private final HotelDAO hotelDAO = new HotelDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request, response)) {
            return;
        }

        Hotel hotel = hotelDAO.getHotel();
        if (hotel == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "The hotel information is missing.");
            return;
        }

        forwardForm(request, response, hotel, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!isAdmin(request, response)) {
            return;
        }

        Hotel hotel = hotelDAO.getHotel();
        if (hotel == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "The hotel information is missing.");
            return;
        }

        // Read the request body first: if a file is too large the container
        // rejects the whole request, and then no field can be read.
        try {
            request.getParts();
        } catch (IllegalStateException exception) {
            forwardForm(request, response, hotel,
                    "An image is too large. The maximum size is 2 MB per image.",
                    null);
            return;
        } catch (ServletException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "The form must be sent as multipart/form-data.");
            return;
        }

        // The hotel name is fixed: it is never read from the request.
        String submittedAddress = request.getParameter("address");
        String address = String.join("\n",
                HotelContent.addressLines(submittedAddress));

        if (address.isEmpty()) {
            forwardForm(request, response, hotel,
                    "Address is required.", submittedAddress);
            return;
        }
        if (address.length() > ADDRESS_MAX) {
            forwardForm(request, response, hotel,
                    "The addresses must be at most " + ADDRESS_MAX
                    + " characters in total.", submittedAddress);
            return;
        }

        // Check every chosen image before anything is saved.
        Map<Slot, Part> chosen = new LinkedHashMap<>();
        Map<Slot, String> extensions = new LinkedHashMap<>();

        for (Slot slot : SLOTS) {
            Part part = request.getPart(slot.key);

            if (part == null || part.getSize() <= 0) {
                continue; // not changed
            }

            String extension = validateImage(part);
            if (extension == null) {
                forwardForm(request, response, hotel,
                        slot.label + ": the file must be a valid JPG, PNG or WEBP image.",
                        submittedAddress);
                return;
            }

            chosen.put(slot, part);
            extensions.put(slot, extension);
        }

        // Save the images.
        List<Path> savedFiles = new ArrayList<>();
        Map<Slot, String> newNames = new LinkedHashMap<>();

        try {
            if (!chosen.isEmpty()) {
                Path folder = imageFolder();
                Files.createDirectories(folder);

                for (Map.Entry<Slot, Part> entry : chosen.entrySet()) {
                    Slot slot = entry.getKey();

                    // The server picks the name, so an upload cannot choose a path.
                    String fileName = "hotel-" + slot.key + "-"
                            + System.currentTimeMillis() + "."
                            + extensions.get(slot);

                    try (InputStream in = entry.getValue().getInputStream()) {
                        Files.copy(in, folder.resolve(fileName),
                                StandardCopyOption.REPLACE_EXISTING);
                    }

                    savedFiles.add(folder.resolve(fileName));
                    newNames.put(slot, fileName);
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            deleteQuietly(savedFiles);
            forwardForm(request, response, hotel,
                    "Unable to save the images on the server.", submittedAddress);
            return;
        }

        // Update the hotel.
        Map<Slot, String> oldNames = new LinkedHashMap<>();
        for (Map.Entry<Slot, String> entry : newNames.entrySet()) {
            oldNames.put(entry.getKey(), entry.getKey().getter.apply(hotel));
            entry.getKey().setter.accept(hotel, entry.getValue());
        }
        hotel.setAddress(address);

        if (!hotelDAO.updateHotel(hotel)) {
            deleteQuietly(savedFiles);
            // Reload so the form does not show the unsaved image names.
            Hotel stored = hotelDAO.getHotel();
            forwardForm(request, response, stored != null ? stored : hotel,
                    "Unable to update the hotel information. Please try again.",
                    submittedAddress);
            return;
        }

        // Remove the previous uploads that were replaced (never the default images).
        for (String oldName : oldNames.values()) {
            deleteIfGenerated(oldName);
        }

        Flash.success(request, "Hotel information updated successfully.");
        response.sendRedirect(request.getContextPath() + "/hotel");
    }

    /**
     * Only the admin manager may open or change this page.
     * Sends the response and returns false when access is denied.
     */
    private boolean isAdmin(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(false);
        Object sessionUser = session == null
                ? null
                : session.getAttribute("user");

        if (!(sessionUser instanceof Users)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        if (!"Admin".equalsIgnoreCase(((Users) sessionUser).getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Only the admin manager can modify the hotel information.");
            return false;
        }

        return true;
    }

    /**
     * Checks the extension and the real file content.
     *
     * @return jpg, png or webp, or null when the file is not an allowed image
     */
    private String validateImage(Part part) throws IOException {

        String extension = extensionOf(part.getSubmittedFileName());
        if (extension == null) {
            return null;
        }

        byte[] header = new byte[12];
        int read;
        try (InputStream in = part.getInputStream()) {
            read = in.readNBytes(header, 0, header.length);
        }

        return matchesImageSignature(header, read, extension) ? extension : null;
    }

    /** Returns jpg, png or webp, or null when the extension is not allowed. */
    private String extensionOf(String fileName) {
        if (fileName == null) {
            return null;
        }

        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return null;
        }

        String extension = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);

        switch (extension) {
            case "jpg":
            case "jpeg":
                return "jpg";
            case "png":
                return "png";
            case "webp":
                return "webp";
            default:
                return null;
        }
    }

    private boolean matchesImageSignature(byte[] h, int length, String extension) {
        if (length < 12) {
            return false;
        }

        switch (extension) {
            case "jpg":
                return (h[0] & 0xFF) == 0xFF
                        && (h[1] & 0xFF) == 0xD8
                        && (h[2] & 0xFF) == 0xFF;
            case "png":
                return (h[0] & 0xFF) == 0x89 && h[1] == 'P'
                        && h[2] == 'N' && h[3] == 'G';
            case "webp":
                return h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                        && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P';
            default:
                return false;
        }
    }

    private Path imageFolder() throws IOException {
        String folderPath = getServletContext().getRealPath(IMAGE_FOLDER);
        if (folderPath == null) {
            throw new IOException("The image folder is not on the file system.");
        }
        return Paths.get(folderPath);
    }

    private void deleteQuietly(List<Path> files) {
        for (Path file : files) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
                // Nothing more can be done.
            }
        }
    }

    /** Deletes an old upload, but only if this servlet created it. */
    private void deleteIfGenerated(String fileName) {
        if (fileName == null || !GENERATED_NAME.matcher(fileName).matches()) {
            return;
        }

        try {
            Files.deleteIfExists(imageFolder().resolve(fileName));
        } catch (IOException ignored) {
            // An old file left behind is harmless.
        }
    }

    private void forwardForm(HttpServletRequest request,
            HttpServletResponse response, Hotel hotel, String addressText)
            throws ServletException, IOException {

        List<Map<String, String>> imageFields = new ArrayList<>();

        for (Slot slot : SLOTS) {
            Map<String, String> field = new LinkedHashMap<>();
            field.put("key", slot.key);
            field.put("label", slot.label);
            field.put("file", HotelContent.safeImage(
                    slot.getter.apply(hotel), slot.defaultFile));
            imageFields.add(field);
        }

        String text = addressText != null
                ? addressText
                : (hotel.getAddress() == null ? "" : hotel.getAddress());

        request.setAttribute("hotel", hotel);
        request.setAttribute("addressText", text);
        request.setAttribute("imageFields", imageFields);

        request.getRequestDispatcher("/WEB-INF/views/update-hotel.jsp")
                .forward(request, response);
    }

    private void forwardForm(HttpServletRequest request,
            HttpServletResponse response, Hotel hotel, String message,
            String addressText) throws ServletException, IOException {

        // Set on the request (not the session) so it shows once, on this page.
        request.setAttribute("flashMessage",
                new FlashMessage(FlashType.ERROR, message));

        forwardForm(request, response, hotel, addressText);
    }

    @Override
    public String getServletInfo() {
        return "Modify the hotel address and images (admin only)";
    }
}
