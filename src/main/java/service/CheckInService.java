package service;

import dao.PaymentmethodDAO;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.PersistenceManager;

/** Collects only outstanding room charges, together with check-in, atomically. */
public class CheckInService {
    private static final Logger LOGGER = Logger.getLogger(CheckInService.class.getName());

    public CheckInRequest createRequest(String bookingID, int actorUserID, Map<String, Object> summary) {
        if (bookingID == null || bookingID.isBlank() || bookingID.trim().length() > 6 || actorUserID <= 0) {
            throw new IllegalArgumentException("A valid booking and staff account are required.");
        }
        if (!"ASSIGNED".equals(summary.get("bookingStatus"))) {
            throw new IllegalStateException("Assign all rooms before opening check-in.");
        }
        Object amount = summary.get("roomRemainingAmount");
        if (!(amount instanceof BigDecimal) || ((BigDecimal) amount).signum() < 0) {
            throw new IllegalStateException("Room payment details are unavailable. Run the check-in payment database migration.");
        }
        Object owner = summary.get("customerUserID");
        if (!(owner instanceof Number) || ((Number) owner).intValue() <= 0) {
            throw new IllegalStateException("The booking customer account is unavailable. Reload check-in.");
        }
        return new CheckInRequest(bookingID.trim(), actorUserID, ((Number) owner).intValue(), (BigDecimal) amount);
    }

    public void confirm(CheckInRequest request, int actorUserID, String[] documents,
            String methodID, boolean collectionConfirmed, Integer walletAuthorizedBy, String bankReference) {
        if (request == null || actorUserID <= 0 || request.actorUserID != actorUserID) {
            throw new IllegalArgumentException("Reload the check-in form before trying again.");
        }
        String guestDocuments = validateDocuments(documents);
        String method = methodID == null ? "" : methodID.trim();
        String reference = "CHECKIN-" + request.token;
        if (request.roomAmount.signum() > 0) {
            if (!collectionConfirmed) {
                throw new IllegalArgumentException("Confirm that the outstanding room payment has been collected.");
            }
            if (method.isEmpty() || method.length() > 4 || !isAvailableMethod(method)) {
                throw new IllegalArgumentException("Select one of the available booking payment methods.");
            }
            if ("PT02".equals(method) && (walletAuthorizedBy == null || walletAuthorizedBy != request.customerUserID)) {
                throw new IllegalArgumentException("The booking customer must authorize their own wallet payment.");
            }
        } else {
            method = null;
        }
        if (request.roomAmount.signum() > 0 && "PT01".equals(method)) {
            reference = bankReference == null ? "" : bankReference.trim();
            if (reference.isEmpty() || reference.length() > 100) {
                throw new IllegalArgumentException("Enter the verified bank transaction reference (maximum 100 characters).");
            }
        }
        Integer authorizedWalletOwner = "PT02".equals(method) ? walletAuthorizedBy : null;
        synchronized (request) {
            try (Connection connection = openConnection()) {
                writeCheckIn(connection, request, guestDocuments, method, reference, authorizedWalletOwner);
            } catch (SQLException exception) {
                throw new IllegalStateException(checkInError(exception), exception);
            } finally {
                clearCache();
            }
        }
    }

    protected boolean isAvailableMethod(String methodID) {
        return new PaymentmethodDAO().getCollectionMethods().stream()
                .anyMatch(method -> methodID.equals(method.getMethodID().trim()));
    }

    protected Connection openConnection() throws SQLException {
        return PersistenceManager.openConnection();
    }

    protected void clearCache() {
        try {
            PersistenceManager.getEMF().getCache().evictAll();
        } catch (RuntimeException exception) {
            LOGGER.log(Level.WARNING, "Unable to refresh the check-in cache.", exception);
        }
    }

    void writeCheckIn(Connection connection, CheckInRequest request, String documents,
            String method, String reference, Integer walletAuthorizedBy) throws SQLException {
        connection.setAutoCommit(false);
        try (PreparedStatement statement = connection.prepareStatement(
                "EXEC dbo.SP_CHECKIN_WITH_ROOM_PAYMENT @BookingID = ?, @ActorUserID = ?, "
                + "@GuestDocuments = ?, @ExpectedRoomAmount = ?, @MethodID = ?, @TransactionCode = ?, @WalletAuthorizedBy = ?, @TransactionID = ?;")) {
            statement.setString(1, request.bookingID);
            statement.setInt(2, request.actorUserID);
            statement.setString(3, documents);
            statement.setBigDecimal(4, request.roomAmount);
            statement.setString(5, method);
            statement.setString(6, reference);
            if (walletAuthorizedBy == null) statement.setNull(7, java.sql.Types.INTEGER);
            else statement.setInt(7, walletAuthorizedBy);
            statement.setString(8, "PT01".equals(method) ? reference : null);
            boolean result = statement.execute();
            // Nested payment can return a row before a later check-in error. Drain all results.
            while (true) {
                if (result) {
                    try (ResultSet rows = statement.getResultSet()) {
                        while (rows.next()) { /* drain nested results */ }
                    }
                } else if (statement.getUpdateCount() == -1) {
                    break;
                }
                result = statement.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
            }
            connection.commit();
        } catch (SQLException | RuntimeException exception) {
            try {
                connection.rollback();
            } catch (SQLException rollbackError) {
                exception.addSuppressed(rollbackError);
            }
            throw exception;
        }
    }

    static String validateDocuments(String[] documents) {
        if (documents == null || documents.length == 0) {
            throw new IllegalArgumentException("Enter guest identity documents before check-in.");
        }
        Set<String> valid = new LinkedHashSet<>();
        for (String document : documents) {
            String value = document == null ? "" : document.trim();
            if (!value.matches("[0-9]{12}")) {
                throw new IllegalArgumentException("Each CCCD must contain exactly 12 digits.");
            }
            if (!valid.add(value)) {
                throw new IllegalArgumentException("Each guest must have a different CCCD.");
            }
        }
        String joined = String.join(", ", valid);
        if (joined.length() > 500) {
            throw new IllegalArgumentException("Guest documents exceed the booking's 500-character limit.");
        }
        return joined;
    }

    static String checkInError(SQLException exception) {
        switch (exception.getErrorCode()) {
            case 208: case 2812: return "Run db/migrations/DB_checkin_room_payment.sql before using check-in payment.";
            case 51000: return "The booking is busy. Check its status before trying again.";
            case 51031: case 51060: return "Only authorized staff or admin can check in guests.";
            case 51038: return "This payment reference has already been recorded. Review payment history before retrying.";
            case 51046: return "The customer's wallet cannot cover the room balance. Select another payment method.";
            case 51210: return "A verified bank transaction reference is required for VietQR.";
            case 51048: return "A receptionist must record a wallet payment authorized by the booking customer.";
            case 51044: case 51111: return "The room balance changed. Reload check-in and verify payments before collecting money.";
            case 51064: return "Assign all requested rooms before check-in.";
            case 51069: case 51114: return "Enter valid guest identity documents for every assigned room.";
            case 51070: return "An assigned room is unavailable or is still occupied. Review room assignments.";
            case 51110: return "Pay the outstanding room charges before check-in.";
            case 51112: return "Select an available booking payment method for the room balance.";
            case 51113: return "The payment ID limit has been reached. Contact the administrator.";
            case 51115: return "Assign all rooms first. Cancelled or completed bookings cannot check in.";
            case 51116: return "The booking has no successful initial payment. Verify its payment history.";
            default: return "Check-in could not be confirmed. Verify the booking and payment history before retrying."
                    + " (SQL " + exception.getErrorCode() + ")";
        }
    }

    public static final class CheckInRequest implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String token = UUID.randomUUID().toString();
        private final String bookingID;
        private final int actorUserID;
        private final int customerUserID;
        private final BigDecimal roomAmount;
        private CheckInRequest(String bookingID, int actorUserID, int customerUserID, BigDecimal roomAmount) {
            this.bookingID = bookingID;
            this.actorUserID = actorUserID;
            this.customerUserID = customerUserID;
            this.roomAmount = roomAmount;
        }
        public String getToken() { return token; }
        public String getBookingID() { return bookingID; }
        public int getActorUserID() { return actorUserID; }
        public int getCustomerUserID() { return customerUserID; }
        public BigDecimal getRoomAmount() { return roomAmount; }
    }
}
