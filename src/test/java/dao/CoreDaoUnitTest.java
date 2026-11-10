package dao;

import entity.Booking;
import entity.Customer;

public class CoreDaoUnitTest {

    private static final CustomerDAO customerDAO = new CustomerDAO();
    private static final BookingDAO bookingDAO = new BookingDAO();
    private static final InvoiceDAO invoiceDAO = new InvoiceDAO();

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================================================");
        System.out.println("                   HBMS CORE DAO UNIT TEST EXECUTION (19 TEST CASES)                     ");
        System.out.println("==========================================================================================");

        testFunction1_CustomerDAO();
        testFunction2_BookingDAO();
        testFunction3_InvoiceDAO();

        System.out.println("\n==========================================================================================");
        System.out.printf("TOTAL TEST CASES: 19 | PASSED: %d | FAILED: %d | UNTESTED: 0\n", totalPassed, totalFailed);
        if (totalFailed == 0) {
            System.out.println("RESULT: ALL 19 TEST CASES PASSED SUCCESSFULLY (100% PASS RATE)!");
        } else {
            System.out.println("RESULT: SOME TESTS FAILED. PLEASE VERIFY DATABASE SEED DATA.");
        }
        System.out.println("==========================================================================================");
    }

    private static void testFunction1_CustomerDAO() {
        System.out.println("\n[FUNCTION 1] dao.CustomerDAO.getCustomerById(String id)");
        System.out.println("------------------------------------------------------------------------------------------");

        runTest("UTCID01", "Abnormal: id is null -> expect null", () -> {
            Customer result = customerDAO.getCustomerById(null);
            return result == null;
        });

        runTest("UTCID02", "Boundary: id is empty \"\" -> expect null", () -> {
            Customer result = customerDAO.getCustomerById("");
            return result == null;
        });

        runTest("UTCID03", "Boundary: id is whitespace \"   \" -> expect null", () -> {
            Customer result = customerDAO.getCustomerById("   ");
            return result == null;
        });

        runTest("UTCID04", "Normal: id is \"KH01\" -> expect Customer object", () -> {
            Customer result = customerDAO.getCustomerById("KH01");
            return result != null && "KH01".equalsIgnoreCase(result.getCustomerID());
        });

        runTest("UTCID05", "Normal: lowercase id \"kh01\" -> expect Customer object", () -> {
            Customer result = customerDAO.getCustomerById("kh01");
            return result != null && "KH01".equalsIgnoreCase(result.getCustomerID());
        });

        runTest("UTCID06", "Abnormal: non-existent id \"KH9999\" -> expect null", () -> {
            Customer result = customerDAO.getCustomerById("KH9999");
            return result == null;
        });
    }

    private static void testFunction2_BookingDAO() {
        System.out.println("\n[FUNCTION 2] dao.BookingDAO.cancelBooking(String bookingID, int actorUserID, String reason)");
        System.out.println("------------------------------------------------------------------------------------------");

        runTest("UTCID01", "Normal: \"BK0001\", actor 1, \"CUSTOMER_REQUEST\" -> expect Booking object or security check (SQL 51003)", () -> {
            try {
                Booking result = bookingDAO.cancelBooking("BK0001", 1, "CUSTOMER_REQUEST");
                return result != null && "BK0001".equals(result.getBookingID());
            } catch (Exception e) {
                return e.getMessage() != null && e.getMessage().contains("51003");
            }
        });

        runTest("UTCID02", "Abnormal: bookingID is null -> expect IllegalArgumentException", () -> {
            try {
                bookingDAO.cancelBooking(null, 1, "CUSTOMER_REQUEST");
                return false;
            } catch (IllegalArgumentException e) {
                return e.getMessage() != null && e.getMessage().contains("Booking ID");
            }
        });

        runTest("UTCID03", "Boundary: bookingID length > 6 \"BK000001\" -> expect IllegalArgumentException", () -> {
            try {
                bookingDAO.cancelBooking("BK000001", 1, "CUSTOMER_REQUEST");
                return false;
            } catch (IllegalArgumentException e) {
                return e.getMessage() != null && e.getMessage().contains("must not exceed 6 characters");
            }
        });

        runTest("UTCID04", "Abnormal: actorUserID is 0 -> expect IllegalArgumentException", () -> {
            try {
                bookingDAO.cancelBooking("BK0001", 0, "CUSTOMER_REQUEST");
                return false;
            } catch (IllegalArgumentException e) {
                return e.getMessage() != null && e.getMessage().contains("UserID is required");
            }
        });

        runTest("UTCID05", "Abnormal: invalid reason \"OTHER_REASON\" -> expect IllegalArgumentException", () -> {
            try {
                bookingDAO.cancelBooking("BK0001", 1, "OTHER_REASON");
                return false;
            } catch (IllegalArgumentException e) {
                return e.getMessage() != null && e.getMessage().contains("expirePendingBookings");
            }
        });

        runTest("UTCID06", "Normal: \"BK0001\", actor 1, reason \"NO_SHOW\" -> expect Booking object or policy check (SQL 51008)", () -> {
            try {
                Booking result = bookingDAO.cancelBooking("BK0001", 1, "NO_SHOW");
                return result != null && "BK0001".equals(result.getBookingID());
            } catch (Exception e) {
                return e.getMessage() != null && e.getMessage().contains("51008");
            }
        });

        runTest("UTCID07", "Boundary: \"BK0041\" (len 6), actor 1, \"STAFF_REQUEST\" -> expect Booking object", () -> {
            Booking result = bookingDAO.cancelBooking("BK0041", 1, "STAFF_REQUEST");
            return result != null && "BK0041".equals(result.getBookingID());
        });
    }

    private static void testFunction3_InvoiceDAO() {
        System.out.println("\n[FUNCTION 3] dao.InvoiceDAO.isBookingPaid(String bookingID)");
        System.out.println("------------------------------------------------------------------------------------------");

        runTest("UTCID01", "Normal: fully paid booking \"BK0001\" -> expect true", () -> {
            return invoiceDAO.isBookingPaid("BK0001");
        });

        runTest("UTCID02", "Normal: unpaid booking \"BK0005\" -> expect false", () -> {
            return !invoiceDAO.isBookingPaid("BK0005");
        });

        runTest("UTCID03", "Abnormal: cancelled booking \"BK0003\" -> expect false", () -> {
            return !invoiceDAO.isBookingPaid("BK0003");
        });

        runTest("UTCID04", "Abnormal: bookingID is null -> expect IllegalArgumentException", () -> {
            try {
                invoiceDAO.isBookingPaid(null);
                return false;
            } catch (IllegalArgumentException e) {
                return e.getMessage() != null && e.getMessage().contains("booking ID is required");
            }
        });

        runTest("UTCID05", "Boundary: empty bookingID \"\" -> expect IllegalArgumentException", () -> {
            try {
                invoiceDAO.isBookingPaid("");
                return false;
            } catch (IllegalArgumentException e) {
                return e.getMessage() != null && e.getMessage().contains("booking ID is required");
            }
        });

        runTest("UTCID06", "Boundary: bookingID length > 6 \"BK1234567\" -> expect IllegalArgumentException", () -> {
            try {
                invoiceDAO.isBookingPaid("BK1234567");
                return false;
            } catch (IllegalArgumentException e) {
                return e.getMessage() != null && e.getMessage().contains("booking ID is required");
            }
        });
    }

    @FunctionalInterface
    interface TestAction {
        boolean execute() throws Exception;
    }

    private static void runTest(String utcid, String description, TestAction action) {
        try {
            boolean success = action.execute();
            if (success) {
                totalPassed++;
                System.out.printf("  [%s] [PASS] - %s\n", utcid, description);
            } else {
                totalFailed++;
                System.out.printf("  [%s] [FAIL] - %s\n", utcid, description);
            }
        } catch (Throwable t) {
            totalFailed++;
            System.out.printf("  [%s] [FAIL] - %s (Exception: %s)\n", utcid, description, t.getMessage());
        }
    }
}
