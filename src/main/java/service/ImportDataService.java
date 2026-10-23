package service;

import jakarta.persistence.EntityManager;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import static org.apache.poi.ss.usermodel.CellType.BOOLEAN;
import static org.apache.poi.ss.usermodel.CellType.FORMULA;
import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import static org.apache.poi.ss.usermodel.CellType.STRING;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Service xử lý Import toàn bộ Database từ file Excel (.xlsx). BẢN ĐẶC BIỆT: Sử
 * dụng Native SQL để vượt qua các Trigger, Constraint và các cột
 * Identity/Computed khắt khe của Database.
 */
public class ImportDataService {

    private final SimpleDateFormat sdfDatetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

    // ==========================================
    // 2. CHỨC NĂNG IMPORT (PHỤC HỒI DB TỪ EXCEL)
    // ==========================================
    public void importDB(InputStream in, EntityManager em) throws Exception {
        boolean isMyTx = false;
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
            isMyTx = true;
        }

        try (Workbook workbook = WorkbookFactory.create(in)) {

            // 1. TẠM THỜI VÔ HIỆU HÓA CÁC RÀO CẢN CỦA DB
            disableConstraintsAndTriggers(em);

            // 2. XÓA DỮ LIỆU CŨ (Nếu cần) - Tùy bạn quyết định có bật lên không
            // clearAllData(em);
            // 3. THỰC HIỆN IMPORT TỪNG SHEET BẰNG NATIVE SQL
            // Master Data
            importHotelSheet(workbook.getSheet("HOTEL"), em);
            importNationalitySheet(workbook.getSheet("NATIONALITY"), em);
            importRoomTypeSheet(workbook.getSheet("ROOM_TYPE"), em);
            importServiceSheet(workbook.getSheet("SERVICE"), em);
            importPaymentMethodSheet(workbook.getSheet("PAYMENTMETHOD"), em);

            // Tài khoản & Người dùng (Cần IDENTITY_INSERT cho USERS)
            importUsersSheet(workbook.getSheet("USERS"), em);
            importEmployeeSheet(workbook.getSheet("EMPLOYEE"), em);
            importCustomerSheet(workbook.getSheet("CUSTOMER"), em);
            importUserBookingControlSheet(workbook.getSheet("USER_BOOKING_CONTROL"), em);
            // Cần IDENTITY_INSERT cho CUSTOMER_WALLET
            importCustomerWalletSheet(workbook.getSheet("CUSTOMER_WALLET"), em);

            // Dữ liệu độc lập
            importRoomSheet(workbook.getSheet("ROOM"), em);
            // Cần IDENTITY_INSERT cho COMPLAINT
            importComplaintSheet(workbook.getSheet("COMPLAINT"), em);

            // Giao dịch chính
            importBookingSheet(workbook.getSheet("BOOKING"), em);
            // Cần IDENTITY_INSERT cho BOOKING_DETAIL
            importBookingDetailSheet(workbook.getSheet("BOOKING_DETAIL"), em);
            importBookingServiceSheet(workbook.getSheet("BOOKING_SERVICE"), em);
            // Cần IDENTITY_INSERT cho ROOM_ASSIGNMENT
            importRoomAssignmentSheet(workbook.getSheet("ROOM_ASSIGNMENT"), em);

            // Giao dịch thanh toán & Hoàn tiền
            // Cần IDENTITY_INSERT cho TRANSACTION
            importTransactionSheet(workbook.getSheet("TRANSACTION"), em);
            importPaymentSheet(workbook.getSheet("PAYMENT"), em);
            // Cần IDENTITY_INSERT cho BOOKING_REFUND
            importBookingRefundSheet(workbook.getSheet("BOOKING_REFUND"), em);
            importRefundPaymentSheet(workbook.getSheet("REFUND_PAYMENT"), em);
            // Cần IDENTITY_INSERT cho WALLET_TRANSACTION
            importWalletTransactionSheet(workbook.getSheet("WALLET_TRANSACTION"), em);
            // Cần IDENTITY_INSERT cho BOOKING_LOCK_HISTORY
            importBookingLockHistorySheet(workbook.getSheet("BOOKING_LOCK_HISTORY"), em);

            // Hóa đơn
            importInvoiceSheet(workbook.getSheet("INVOICE"), em);
            importInvoiceTransactionSheet(workbook.getSheet("INVOICE_TRANSACTION"), em);

            // 4. BẬT LẠI CÁC RÀO CẢN CỦA DB
            enableConstraintsAndTriggers(em);

            if (isMyTx) {
                em.getTransaction().commit();
            }
        } catch (Exception e) {
            if (isMyTx) {
                em.getTransaction().rollback();
            }
            throw e;
        }
    }

    // ==========================================
    // CÁC HÀM IMPORT TỪNG SHEET (Gen Native SQL)
    // ==========================================
    private void importHotelSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        String sql = "INSERT INTO HOTEL (HotelName, HotelImage, RoomImage, PoolImage, RestaurantImage, Address) VALUES (?, ?, ?, ?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            String name = getCellValue(row.getCell(0));
            if (name.isEmpty()) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, name)
                    .setParameter(2, nullIfEmpty(getCellValue(row.getCell(1))))
                    .setParameter(3, nullIfEmpty(getCellValue(row.getCell(2))))
                    .setParameter(4, nullIfEmpty(getCellValue(row.getCell(3))))
                    .setParameter(5, nullIfEmpty(getCellValue(row.getCell(4))))
                    .setParameter(6, getCellValue(row.getCell(5)))
                    .executeUpdate();
        }
    }

    private void importNationalitySheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        String sql = "INSERT INTO NATIONALITY (NationalityID, NationalityName) VALUES (?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .setParameter(2, getCellValue(row.getCell(1)))
                    .executeUpdate();
        }
    }

    private void importRoomTypeSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        String sql = "INSERT INTO ROOM_TYPE (RoomTypeID, TypeName, Capacity, Price, RoomTypeImage, Description) VALUES (?, ?, ?, ?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .setParameter(2, getCellValue(row.getCell(1)))
                    .setParameter(3, parseInteger(getCellValue(row.getCell(2))))
                    .setParameter(4, parseBigDecimal(getCellValue(row.getCell(3))))
                    .setParameter(5, nullIfEmpty(getCellValue(row.getCell(4))))
                    .setParameter(6, nullIfEmpty(getCellValue(row.getCell(5))))
                    .executeUpdate();
        }
    }

    private void importServiceSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        String sql = "INSERT INTO SERVICE (ServiceID, ServiceName, UnitPrice) VALUES (?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .setParameter(2, getCellValue(row.getCell(1)))
                    .setParameter(3, parseBigDecimal(getCellValue(row.getCell(2))))
                    .executeUpdate();
        }
    }

    private void importPaymentMethodSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        String sql = "INSERT INTO PAYMENTMETHOD (MethodID, MethodName) VALUES (?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .setParameter(2, getCellValue(row.getCell(1)))
                    .executeUpdate();
        }
    }

    private void importUsersSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        em.createNativeQuery("SET IDENTITY_INSERT USERS ON").executeUpdate();
        String sql = "INSERT INTO USERS (UserID, Username, Password, Role, MustChangePassword) VALUES (?, ?, ?, ?, ?)";

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            Integer id = parseInteger(getCellValue(row.getCell(0)));
            if (id == null) {
                continue;
            }

            String mcpStr = getCellValue(row.getCell(4));
            boolean mcp = mcpStr.equalsIgnoreCase("true") || mcpStr.equals("1");

            em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .setParameter(2, getCellValue(row.getCell(1)))
                    .setParameter(3, getCellValue(row.getCell(2)))
                    .setParameter(4, getCellValue(row.getCell(3)))
                    .setParameter(5, mcp ? 1 : 0)
                    .executeUpdate();
        }
        em.createNativeQuery("SET IDENTITY_INSERT USERS OFF").executeUpdate();
    }

    private void importEmployeeSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        String sql = "INSERT INTO EMPLOYEE (EmployeeID, FullName, Position, Salary, Shift, Address, Phone, Email, UserID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .setParameter(2, getCellValue(row.getCell(1)))
                    .setParameter(3, getCellValue(row.getCell(2)))
                    .setParameter(4, parseBigDecimal(getCellValue(row.getCell(3))))
                    .setParameter(5, nullIfEmpty(getCellValue(row.getCell(4))))
                    .setParameter(6, nullIfEmpty(getCellValue(row.getCell(5))))
                    .setParameter(7, nullIfEmpty(getCellValue(row.getCell(6))))
                    .setParameter(8, nullIfEmpty(getCellValue(row.getCell(7))))
                    .setParameter(9, parseInteger(getCellValue(row.getCell(8))))
                    .executeUpdate();
        }
    }

    private void importCustomerSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        String sql = "INSERT INTO CUSTOMER (CustomerID, FullName, Phone, Email, Address, NationalityID, UserID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .setParameter(2, getCellValue(row.getCell(1)))
                    .setParameter(3, nullIfEmpty(getCellValue(row.getCell(2))))
                    .setParameter(4, nullIfEmpty(getCellValue(row.getCell(3))))
                    .setParameter(5, nullIfEmpty(getCellValue(row.getCell(4))))
                    .setParameter(6, nullIfEmpty(getCellValue(row.getCell(5))))
                    .setParameter(7, parseInteger(getCellValue(row.getCell(6))))
                    .executeUpdate();
        }
    }

    private void importUserBookingControlSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        String sql = "INSERT INTO USER_BOOKING_CONTROL (UserID, WindowStartedAt, ConsecutiveCancellationCount, CancellationCountInWindow, PenaltyLevel, BookingLockedUntil, UpdatedAt) VALUES (?, ?, ?, ?, ?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            Integer uId = parseInteger(getCellValue(row.getCell(0)));
            if (uId == null) {
                continue;
            }

            // Xóa bản ghi cũ do Trigger sinh ra (nếu có)
            em.createNativeQuery("DELETE FROM USER_BOOKING_CONTROL WHERE UserID = ?").setParameter(1, uId).executeUpdate();

            em.createNativeQuery(sql)
                    .setParameter(1, uId)
                    .setParameter(2, getDateCellValue(row.getCell(1)))
                    .setParameter(3, parseInteger(getCellValue(row.getCell(2)), 0))
                    .setParameter(4, parseInteger(getCellValue(row.getCell(3)), 0))
                    .setParameter(5, parseInteger(getCellValue(row.getCell(4)), 0))
                    .setParameter(6, getDateCellValue(row.getCell(5)))
                    .setParameter(7, getDateCellValue(row.getCell(6)))
                    .executeUpdate();
        }
    }

    private void importCustomerWalletSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        em.createNativeQuery("SET IDENTITY_INSERT CUSTOMER_WALLET ON").executeUpdate();
        String sql = "INSERT INTO CUSTOMER_WALLET (WalletID, CustomerID, CreatedAt) VALUES (?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            Long wId = parseLong(getCellValue(row.getCell(0)));
            String cusId = getCellValue(row.getCell(1));
            if (wId == null || cusId.isEmpty()) {
                continue;
            }

            // Xóa bản ghi cũ do Trigger sinh ra (nếu có)
            em.createNativeQuery("DELETE FROM CUSTOMER_WALLET WHERE CustomerID = ?").setParameter(1, cusId).executeUpdate();

            em.createNativeQuery(sql)
                    .setParameter(1, wId)
                    .setParameter(2, cusId)
                    .setParameter(3, getDateCellValue(row.getCell(2)))
                    .executeUpdate();
        }
        em.createNativeQuery("SET IDENTITY_INSERT CUSTOMER_WALLET OFF").executeUpdate();
    }

    private void importRoomSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        String sql = "INSERT INTO ROOM (RoomID, RoomNumber, RoomImage, Status, RoomTypeID) VALUES (?, ?, ?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .setParameter(2, getCellValue(row.getCell(1)))
                    .setParameter(3, nullIfEmpty(getCellValue(row.getCell(2))))
                    .setParameter(4, getCellValue(row.getCell(3)))
                    .setParameter(5, getCellValue(row.getCell(4)))
                    .executeUpdate();
        }
    }

    private void importComplaintSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        em.createNativeQuery("SET IDENTITY_INSERT COMPLAINT ON").executeUpdate();
        String sql = "INSERT INTO COMPLAINT (ComplaintID, Title, Content, CreatedAt, Status, CustomerID, ReplyMessage, EmployeeID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            Integer id = parseInteger(getCellValue(row.getCell(0)));
            if (id == null) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .setParameter(2, getCellValue(row.getCell(1)))
                    .setParameter(3, getCellValue(row.getCell(2)))
                    .setParameter(4, getDateCellValue(row.getCell(3)))
                    .setParameter(5, getCellValue(row.getCell(4)))
                    .setParameter(6, nullIfEmpty(getCellValue(row.getCell(5))))
                    .setParameter(7, nullIfEmpty(getCellValue(row.getCell(6))))
                    .setParameter(8, nullIfEmpty(getCellValue(row.getCell(7))))
                    .executeUpdate();
        }
        em.createNativeQuery("SET IDENTITY_INSERT COMPLAINT OFF").executeUpdate();
    }

    private void importBookingSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        // Bỏ qua RefundDeadline, CheckInAt, NoShowDeadline vì chúng là Computed Column
        String sql = "INSERT INTO BOOKING (BookingID, PaymentOption, FirstPaidAt, BookingAmountAtFirstPayment, CancelledAt, CancellationReason, CancelledByUserID, BookingDate, PaymentDeadline, CheckInDate, CheckOutDate, BookingStatus, CustomerID, TotalAmount) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .setParameter(2, getCellValue(row.getCell(1)))
                    .setParameter(3, getDateCellValue(row.getCell(2)))
                    .setParameter(4, parseBigDecimal(getCellValue(row.getCell(3))))
                    .setParameter(5, getDateCellValue(row.getCell(4)))
                    .setParameter(6, nullIfEmpty(getCellValue(row.getCell(5))))
                    .setParameter(7, parseInteger(getCellValue(row.getCell(6))))
                    .setParameter(8, getDateCellValue(row.getCell(7)))
                    .setParameter(9, getDateCellValue(row.getCell(8)))
                    .setParameter(10, getDateCellValue(row.getCell(9))) // CheckInDate
                    .setParameter(11, getDateCellValue(row.getCell(10))) // CheckOutDate
                    .setParameter(12, getCellValue(row.getCell(11)))
                    .setParameter(13, getCellValue(row.getCell(12)))
                    .setParameter(14, parseBigDecimal(getCellValue(row.getCell(13))))
                    .executeUpdate();
        }
    }

    private void importBookingDetailSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        em.createNativeQuery("SET IDENTITY_INSERT BOOKING_DETAIL ON").executeUpdate();
        String sql = "INSERT INTO BOOKING_DETAIL (BookingDetailID, BookingID, RoomTypeID, Quantity, GuestCount, UnitPrice, Subtotal) VALUES (?, ?, ?, ?, ?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            Integer id = parseInteger(getCellValue(row.getCell(0)));
            String bId = getCellValue(row.getCell(1));
            String rtId = getCellValue(row.getCell(2));
            if (id == null || bId.isEmpty() || rtId.isEmpty()) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .setParameter(2, bId)
                    .setParameter(3, rtId)
                    .setParameter(4, parseInteger(getCellValue(row.getCell(3))))
                    .setParameter(5, parseInteger(getCellValue(row.getCell(4))))
                    .setParameter(6, parseBigDecimal(getCellValue(row.getCell(5))))
                    .setParameter(7, parseBigDecimal(getCellValue(row.getCell(6))))
                    .executeUpdate();
        }
        em.createNativeQuery("SET IDENTITY_INSERT BOOKING_DETAIL OFF").executeUpdate();
    }

    private void importBookingServiceSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        String sql = "INSERT INTO BOOKING_SERVICE (BookingID, ServiceID, Quantity, UnitPrice, Subtotal) VALUES (?, ?, ?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            String bId = getCellValue(row.getCell(0));
            String sId = getCellValue(row.getCell(1));
            if (bId.isEmpty() || sId.isEmpty()) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, bId)
                    .setParameter(2, sId)
                    .setParameter(3, parseInteger(getCellValue(row.getCell(2))))
                    .setParameter(4, parseBigDecimal(getCellValue(row.getCell(3))))
                    .setParameter(5, parseBigDecimal(getCellValue(row.getCell(4))))
                    .executeUpdate();
        }
    }

    private void importRoomAssignmentSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        em.createNativeQuery("SET IDENTITY_INSERT ROOM_ASSIGNMENT ON").executeUpdate();
        String sql = "INSERT INTO ROOM_ASSIGNMENT (AssignmentID, BookingDetailID, RoomID, EmployeeID, AssignedAt) VALUES (?, ?, ?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            Integer id = parseInteger(getCellValue(row.getCell(0)));
            if (id == null) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .setParameter(2, parseInteger(getCellValue(row.getCell(1))))
                    .setParameter(3, getCellValue(row.getCell(2)))
                    .setParameter(4, nullIfEmpty(getCellValue(row.getCell(3))))
                    .setParameter(5, getDateCellValue(row.getCell(4)))
                    .executeUpdate();
        }
        em.createNativeQuery("SET IDENTITY_INSERT ROOM_ASSIGNMENT OFF").executeUpdate();
    }

    private void importTransactionSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        em.createNativeQuery("SET IDENTITY_INSERT [TRANSACTION] ON").executeUpdate();
        String sql = "INSERT INTO [TRANSACTION] (id, TransactionID, TransactionType, TransactionValue) VALUES (?, ?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            Long id = parseLong(getCellValue(row.getCell(0)));
            String txId = getCellValue(row.getCell(1));
            if (id == null || txId.isEmpty()) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .setParameter(2, txId)
                    .setParameter(3, getCellValue(row.getCell(2)))
                    .setParameter(4, parseBigDecimal(getCellValue(row.getCell(3))))
                    .executeUpdate();
        }
        em.createNativeQuery("SET IDENTITY_INSERT [TRANSACTION] OFF").executeUpdate();
    }

    private void importPaymentSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        // Bỏ qua ExternalAmount vì là Computed Column
        String sql = "INSERT INTO PAYMENT (PaymentID, BookingID, PaymentType, PaymentTime, Amount, Status, MethodID, TransactionCode, TransactionID, WalletAmount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .setParameter(2, getCellValue(row.getCell(1)))
                    .setParameter(3, getCellValue(row.getCell(2)))
                    .setParameter(4, getDateCellValue(row.getCell(3)))
                    .setParameter(5, parseBigDecimal(getCellValue(row.getCell(4))))
                    .setParameter(6, getCellValue(row.getCell(6))) // Status ở index 6
                    .setParameter(7, nullIfEmpty(getCellValue(row.getCell(7))))
                    .setParameter(8, nullIfEmpty(getCellValue(row.getCell(8))))
                    .setParameter(9, nullIfEmpty(getCellValue(row.getCell(9))))
                    .setParameter(10, parseBigDecimal(getCellValue(row.getCell(5)))) // WalletAmount ở index 5
                    .executeUpdate();
        }
    }

    private void importBookingRefundSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        em.createNativeQuery("SET IDENTITY_INSERT BOOKING_REFUND ON").executeUpdate();
        String sql = "INSERT INTO BOOKING_REFUND (RefundID, BookingID, CustomerID, WalletID, Amount, RefundedAt, TransactionID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            Long id = parseLong(getCellValue(row.getCell(0)));
            if (id == null) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .setParameter(2, getCellValue(row.getCell(1)))
                    .setParameter(3, getCellValue(row.getCell(2)))
                    .setParameter(4, parseLong(getCellValue(row.getCell(3))))
                    .setParameter(5, parseBigDecimal(getCellValue(row.getCell(4))))
                    .setParameter(6, getDateCellValue(row.getCell(5)))
                    .setParameter(7, getCellValue(row.getCell(6)))
                    .executeUpdate();
        }
        em.createNativeQuery("SET IDENTITY_INSERT BOOKING_REFUND OFF").executeUpdate();
    }

    private void importRefundPaymentSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        String sql = "INSERT INTO REFUND_PAYMENT (RefundID, BookingID, PaymentID, Amount) VALUES (?, ?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            Long rId = parseLong(getCellValue(row.getCell(0)));
            String pId = getCellValue(row.getCell(2));
            if (rId == null || pId.isEmpty()) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, rId)
                    .setParameter(2, getCellValue(row.getCell(1)))
                    .setParameter(3, pId)
                    .setParameter(4, parseBigDecimal(getCellValue(row.getCell(3))))
                    .executeUpdate();
        }
    }

    private void importWalletTransactionSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        em.createNativeQuery("SET IDENTITY_INSERT WALLET_TRANSACTION ON").executeUpdate();
        String sql = "INSERT INTO WALLET_TRANSACTION (WalletTransactionID, WalletID, TransactionType, Amount, TransactionTime, RefundID, PaymentID, TransactionID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            Long id = parseLong(getCellValue(row.getCell(0)));
            if (id == null) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .setParameter(2, parseLong(getCellValue(row.getCell(1))))
                    .setParameter(3, getCellValue(row.getCell(2)))
                    .setParameter(4, parseBigDecimal(getCellValue(row.getCell(3))))
                    .setParameter(5, getDateCellValue(row.getCell(4)))
                    .setParameter(6, parseLong(getCellValue(row.getCell(5))))
                    .setParameter(7, nullIfEmpty(getCellValue(row.getCell(6))))
                    .setParameter(8, getCellValue(row.getCell(7)))
                    .executeUpdate();
        }
        em.createNativeQuery("SET IDENTITY_INSERT WALLET_TRANSACTION OFF").executeUpdate();
    }

    private void importBookingLockHistorySheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        em.createNativeQuery("SET IDENTITY_INSERT BOOKING_LOCK_HISTORY ON").executeUpdate();
        String sql = "INSERT INTO BOOKING_LOCK_HISTORY (LockHistoryID, UserID, TriggerBookingID, PenaltyLevel, LockedAt, LockedUntil, DurationMinutes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            Long id = parseLong(getCellValue(row.getCell(0)));
            if (id == null) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .setParameter(2, parseInteger(getCellValue(row.getCell(1))))
                    .setParameter(3, getCellValue(row.getCell(2)))
                    .setParameter(4, parseInteger(getCellValue(row.getCell(3))))
                    .setParameter(5, getDateCellValue(row.getCell(4)))
                    .setParameter(6, getDateCellValue(row.getCell(5)))
                    .setParameter(7, parseInteger(getCellValue(row.getCell(6))))
                    .executeUpdate();
        }
        em.createNativeQuery("SET IDENTITY_INSERT BOOKING_LOCK_HISTORY OFF").executeUpdate();
    }

    private void importInvoiceSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        String sql = "INSERT INTO INVOICE (InvoiceID, InvoiceDate, TotalAmount, Status, ReplacedInvoiceID, EmployeeID, BookingID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, id)
                    .setParameter(2, getDateCellValue(row.getCell(1)))
                    .setParameter(3, parseBigDecimal(getCellValue(row.getCell(2))))
                    .setParameter(4, getCellValue(row.getCell(3)))
                    .setParameter(5, nullIfEmpty(getCellValue(row.getCell(4))))
                    .setParameter(6, nullIfEmpty(getCellValue(row.getCell(5))))
                    .setParameter(7, getCellValue(row.getCell(6)))
                    .executeUpdate();
        }
    }

    private void importInvoiceTransactionSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        String sql = "INSERT INTO INVOICE_TRANSACTION (InvoiceID, BookingID, PaymentID, TransactionID) VALUES (?, ?, ?, ?)";
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            String iId = getCellValue(row.getCell(0));
            String bId = getCellValue(row.getCell(1));
            if (iId.isEmpty() || bId.isEmpty()) {
                continue;
            }

            em.createNativeQuery(sql)
                    .setParameter(1, iId)
                    .setParameter(2, bId)
                    .setParameter(3, getCellValue(row.getCell(2)))
                    .setParameter(4, getCellValue(row.getCell(3)))
                    .executeUpdate();
        }
    }

    // ==========================================
    // CÁC HÀM TIỆN ÍCH DÙNG CHUNG
    // ==========================================
    private void disableConstraintsAndTriggers(EntityManager em) {
        // Tắt toàn bộ Check Constraints và Foreign Keys
        em.createNativeQuery("EXEC sp_MSforeachtable \"ALTER TABLE ? NOCHECK CONSTRAINT all\"").executeUpdate();
        // Tắt toàn bộ Triggers để tránh sinh dữ liệu phụ đè lên file Excel
        em.createNativeQuery("EXEC sp_MSforeachtable \"DISABLE TRIGGER all ON ?\"").executeUpdate();
    }

    private void enableConstraintsAndTriggers(EntityManager em) {
        em.createNativeQuery("EXEC sp_MSforeachtable \"ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all\"").executeUpdate();
        em.createNativeQuery("EXEC sp_MSforeachtable \"ENABLE TRIGGER all ON ?\"").executeUpdate();
    }

    private String nullIfEmpty(String str) {
        return (str == null || str.trim().isEmpty()) ? null : str.trim();
    }

    private Integer parseInteger(String str) {
        return parseInteger(str, null);
    }

    private Integer parseInteger(String str, Integer defaultVal) {
        if (str == null || str.trim().isEmpty()) {
            return defaultVal;
        }
        try {
            return (int) Double.parseDouble(str);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private Long parseLong(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        try {
            return (long) Double.parseDouble(str);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(str);
        } catch (Exception e) {
            return null;
        }
    }

    private Date getDateCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        try {
            return cell.getDateCellValue();
        } catch (Exception e) {
            return null;
        }
    }

    // Đọc giá trị Cell an toàn thành String
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    return String.format("%.0f", val);
                }
                return String.valueOf(val);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (IllegalStateException e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }

    // Kiểm tra dòng rỗng
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
