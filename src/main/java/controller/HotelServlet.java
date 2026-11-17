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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import util.HotelContent;
import util.HotelImageSlot;
import util.ProjectFileUtil;
import util.flash.Flash;
import util.flash.FlashMessage;
import util.flash.FlashType;

/**
 * Lets the admin manager change the hotel images: the background image and the
 * three Overview images.
 *
 * The hotel name and the address are shown on the page but cannot be changed.
 * The project has a single hotel.
 *
 * How an image is replaced: 1. the new file is written to assets/images/hotel
 * under a new, unique name (ProjectFileUtil saves it to both the Tomcat deploy
 * folder and src/main/webapp), 2. the new file name is saved in the database
 * (HOTEL table), 3. only after the database update succeeded, the old file is
 * deleted. If step 1 or 2 fails, the new files are removed and the old image
 * stays in use.
 */
@WebServlet(name = "HotelServlet", urlPatterns = {"/hotel"})
@MultipartConfig(
        maxFileSize = 2 * 1024 * 1024, // 2 MB per image
        maxRequestSize = 10 * 1024 * 1024
)
public class HotelServlet extends HttpServlet {

    /**
     * Sub folder under /assets/images, used by ProjectFileUtil.
     */
    private static final String IMAGE_SUBFOLDER = "hotel";

    /**
     * File names created by this servlet, so old uploads can be cleaned up.
     */
    private static final Pattern GENERATED_NAME = Pattern.compile(
            "^hotel-(hero|room|pool|restaurant)-\\d+\\.(jpg|png|webp)$");

    private static final List<HotelImageSlot> SLOTS = Arrays.asList(
            new HotelImageSlot("hero", "Background image (top of the home page)",
                    HotelContent.DEFAULT_HERO_IMAGE,
                    Hotel::getHotelImage, Hotel::setHotelImage),
            new HotelImageSlot("room", "Overview: Luxury Rooms",
                    HotelContent.DEFAULT_ROOM_IMAGE,
                    Hotel::getRoomImage, Hotel::setRoomImage),
            new HotelImageSlot("pool", "Overview: Swimming Pool",
                    HotelContent.DEFAULT_POOL_IMAGE,
                    Hotel::getPoolImage, Hotel::setPoolImage),
            new HotelImageSlot("restaurant", "Overview: Restaurant",
                    HotelContent.DEFAULT_RESTAURANT_IMAGE,
                    Hotel::getRestaurantImage, Hotel::setRestaurantImage)
    );

    private final HotelDAO hotelDAO = new HotelDAO();

    /**
     * Load the current hotel information for the admin edit form.
     */
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

        forwardForm(request, response, hotel);
    }

    /**
     * Validate the hotel fields and images before saving the changes.
     */
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
                    "An image is too large. The maximum size is 2 MB per image.");
            return;
        } catch (ServletException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "The form must be sent as multipart/form-data.");
            return;
        }

        // The hotel name and the address are fixed: they are never read from the request.
        // Check every chosen image before anything is saved.
        Map<HotelImageSlot, Part> chosen = new LinkedHashMap<>();
        Map<HotelImageSlot, String> extensions = new LinkedHashMap<>();

        for (HotelImageSlot slot : SLOTS) {
            Part part = request.getPart(slot.getKey());

            if (part == null || part.getSize() <= 0) {
                continue; // not changed
            }

            String extension = validateImage(part);
            if (extension == null) {
                forwardForm(request, response, hotel,
                        slot.getLabel() + ": the file must be a valid JPG, PNG or WEBP image.");
                return;
            }

            chosen.put(slot, part);
            extensions.put(slot, extension);
        }

        if (chosen.isEmpty()) {
            Flash.info(request, "No image was selected, so nothing was changed.");
            response.sendRedirect(request.getContextPath() + "/hotel");
            return;
        }

        // Step 1: write the new images (the old ones are still in place).
        List<String> savedNames = new ArrayList<>();
        Map<HotelImageSlot, String> newNames = new LinkedHashMap<>();

        for (Map.Entry<HotelImageSlot, Part> entry : chosen.entrySet()) {
            HotelImageSlot slot = entry.getKey();

            // The server picks the name, so an upload cannot choose a path.
            // The format must match GENERATED_NAME.
            String fileName = "hotel-" + slot.getKey() + "-"
                    + System.currentTimeMillis() + "."
                    + extensions.get(slot);

            String saved = ProjectFileUtil.saveImageWithExactName(
                    getServletContext(), entry.getValue(), IMAGE_SUBFOLDER, fileName);

            if (saved == null) {
                deleteSavedImages(savedNames);
                forwardForm(request, response, hotel,
                        "Unable to save the images on the server.");
                return;
            }

            savedNames.add(saved);
            newNames.put(slot, saved);
        }

        // Step 2: save the new file names in the database.
        Map<HotelImageSlot, String> oldNames = new LinkedHashMap<>();
        for (Map.Entry<HotelImageSlot, String> entry : newNames.entrySet()) {
            oldNames.put(entry.getKey(), entry.getKey().getImageFile(hotel));
            entry.getKey().setImageFile(hotel, entry.getValue());
        }

        if (!hotelDAO.updateHotel(hotel)) {
            deleteSavedImages(savedNames);
            // Reload so the form does not show the unsaved image names.
            Hotel stored = hotelDAO.getHotel();
            forwardForm(request, response, stored != null ? stored : hotel,
                    "Unable to update the hotel information. Please try again.");
            return;
        }

        // Step 3: the database now points to the new files, so delete the replaced
        // uploads (never the default images).
        for (String oldName : oldNames.values()) {
            deleteIfGenerated(oldName);
        }

        Flash.success(request, "Hotel information updated successfully.");
        response.sendRedirect(request.getContextPath() + "/hotel");
    }

    /**
     * Only the admin manager may open or change this page. Sends the response
     * and returns false when access is denied.
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

    /**
     * Returns jpg, png or webp, or null when the extension is not allowed.
     */
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

    /**
     * Removes files that were just saved (rollback).
     */
    private void deleteSavedImages(List<String> fileNames) {
        for (String fileName : fileNames) {
            ProjectFileUtil.deleteImageFromProject(
                    getServletContext(), IMAGE_SUBFOLDER, fileName);
        }
    }

    /**
     * Deletes an old upload, but only if this servlet created it.
     */
    private void deleteIfGenerated(String fileName) {
        if (fileName == null || !GENERATED_NAME.matcher(fileName).matches()) {
            return;
        }

        ProjectFileUtil.deleteImageFromProject(
                getServletContext(), IMAGE_SUBFOLDER, fileName);
    }

    private void forwardForm(HttpServletRequest request,
            HttpServletResponse response, Hotel hotel)
            throws ServletException, IOException {

        List<Map<String, String>> imageFields = new ArrayList<>();

        for (HotelImageSlot slot : SLOTS) {
            Map<String, String> field = new LinkedHashMap<>();
            field.put("key", slot.getKey());
            field.put("label", slot.getLabel());
            field.put("file", HotelContent.safeImage(
                    slot.getImageFile(hotel), slot.getDefaultFile()));
            imageFields.add(field);
        }

        request.setAttribute("hotel", hotel);
        request.setAttribute("imageFields", imageFields);

        request.getRequestDispatcher("/WEB-INF/views/update-hotel.jsp")
                .forward(request, response);
    }

    private void forwardForm(HttpServletRequest request,
            HttpServletResponse response, Hotel hotel, String message)
            throws ServletException, IOException {

        // Set on the request (not the session) so it shows once, on this page.
        request.setAttribute("flashMessage",
                new FlashMessage(FlashType.ERROR, message));

        forwardForm(request, response, hotel);
    }

    @Override
    public String getServletInfo() {
        return "Change the hotel images (admin only)";
    }
}
