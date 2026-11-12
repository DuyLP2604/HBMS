package util;

import com.google.gson.JsonObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import dto.PayOsOrder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import service.CounterPaymentService;

/** Writes the order status and QR image used by front desk payment forms. */
public final class CounterPaymentSupport {
    private CounterPaymentSupport() {
    }

    /** Return the order code, status, and QR image URL as JSON. */
    public static void writeOrder(HttpServletRequest request, HttpServletResponse response, PayOsOrder order, String path)
            throws IOException {
        JsonObject body = new JsonObject();
        String status = CounterPaymentService.status(order);
        body.addProperty("status", status);
        if (order != null) {
            body.addProperty("orderCode", Long.toString(order.orderCode));
            if ("PENDING".equals(status)) {
                body.addProperty("imageUrl", request.getContextPath() + path + "?action=payOsImage"
                        + "&bookingID=" + order.bookingID + "&orderCode=" + order.orderCode);
                body.addProperty("checkoutUrl", order.checkInCheckoutUrl);
            }
        }
        writeJson(response, body);
    }

    /** Render the PayOS QR payload as a PNG image. */
    public static void writeQrImage(HttpServletResponse response, PayOsOrder order) throws IOException, WriterException {
        if (order == null || !"PENDING".equals(CounterPaymentService.status(order))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        BitMatrix qr = new QRCodeWriter().encode(order.checkInQrCode, BarcodeFormat.QR_CODE, 240, 240);
        BufferedImage png = new BufferedImage(qr.getWidth(), qr.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < qr.getHeight(); y++) {
            for (int x = 0; x < qr.getWidth(); x++) {
                png.setRGB(x, y, qr.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        response.setContentType("image/png");
        ImageIO.write(png, "png", response.getOutputStream());
    }

    public static void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        JsonObject body = new JsonObject();
        body.addProperty("message", message);
        writeJson(response, body);
    }

    private static void writeJson(HttpServletResponse response, JsonObject body) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(body.toString());
    }

    public static long parseOrderCode(String value) {
        long code = Long.parseLong(value);
        if (code <= 0) {
            throw new IllegalArgumentException("Invalid PayOS order.");
        }
        return code;
    }
}
