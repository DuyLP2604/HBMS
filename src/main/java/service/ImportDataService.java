package service;

import dao.DAOFramework;
import entity.*;
import jakarta.persistence.EntityManager;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ImportDataService {

    private final SimpleDateFormat sdfDatetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

    public void importDB(InputStream in, EntityManager em) throws Exception {
        boolean isMyTx = false;
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
            isMyTx = true;
        }
        try (Workbook workbook = WorkbookFactory.create(in)) {
            importHotelSheet(workbook.getSheet("HOTEL"), em);
            importNationalitySheet(workbook.getSheet("NATIONALITY"), em);
            importRoomTypeSheet(workbook.getSheet("ROOM_TYPE"), em);
            importServiceSheet(workbook.getSheet("SERVICE"), em);
            importPaymentMethodSheet(workbook.getSheet("PAYMENTMETHOD"), em);
            importUsersSheet(workbook.getSheet("USERS"), em);
            importEmployeeSheet(workbook.getSheet("EMPLOYEE"), em);
            importCustomerSheet(workbook.getSheet("CUSTOMER"), em);
            importUserBookingControlSheet(workbook.getSheet("USER_BOOKING_CONTROL"), em);
            importCustomerWalletSheet(workbook.getSheet("CUSTOMER_WALLET"), em);
            importRoomSheet(workbook.getSheet("ROOM"), em);
            importComplaintSheet(workbook.getSheet("COMPLAINT"), em);
            importBookingSheet(workbook.getSheet("BOOKING"), em);
            importBookingDetailSheet(workbook.getSheet("BOOKING_DETAIL"), em);
            importBookingServiceSheet(workbook.getSheet("BOOKING_SERVICE"), em);
            importRoomAssignmentSheet(workbook.getSheet("ROOM_ASSIGNMENT"), em);
            importTransactionSheet(workbook.getSheet("TRANSACTION"), em);
            em.flush(); // Xả lệnh xuống DB để findTransactionByCode có thể truy vấn ngay
            importPaymentSheet(workbook.getSheet("PAYMENT"), em);
            importBookingRefundSheet(workbook.getSheet("BOOKING_REFUND"), em);
            importRefundPaymentSheet(workbook.getSheet("REFUND_PAYMENT"), em);
            importWalletTransactionSheet(workbook.getSheet("WALLET_TRANSACTION"), em);
            importBookingLockHistorySheet(workbook.getSheet("BOOKING_LOCK_HISTORY"), em);
            importInvoiceSheet(workbook.getSheet("INVOICE"), em);
            importInvoiceTransactionSheet(workbook.getSheet("INVOICE_TRANSACTION"), em);

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
    // HÀM HELPER AN TOÀN THAY THẾ CHO getReference
    // ==========================================
    private <T> T getSafeEntity(EntityManager em, Class<T> entityClass, Object id) {
        if (id == null || id.toString().trim().isEmpty()) {
            return null;
        }
        T entity = em.find(entityClass, id);
        if (entity == null) {
            throw new RuntimeException("Lỗi dữ liệu Excel: Không tìm thấy " + entityClass.getSimpleName() + " có ID = '" + id + "' trong database. Vui lòng kiểm tra lại tính liên kết trong file Excel!");
        }
        return entity;
    }

    private void importHotelSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<Hotel> list = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            String name = getCellValue(row.getCell(0));
            if (name.isEmpty()) {
                continue;
            }
            Hotel h = new Hotel();
            h.setHotelName(name);
            h.setHotelImage(nullIfEmpty(getCellValue(row.getCell(1))));
            h.setRoomImage(nullIfEmpty(getCellValue(row.getCell(2))));
            h.setPoolImage(nullIfEmpty(getCellValue(row.getCell(3))));
            h.setRestaurantImage(nullIfEmpty(getCellValue(row.getCell(4))));
            h.setAddress(getCellValue(row.getCell(5)));
            list.add(h);
        }
        new DAOFramework<>(Hotel.class).saveAll(list, true, em);
    }

    private void importNationalitySheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<Nationality> list = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }
            Nationality n = new Nationality();
            n.setNationalityID(id);
            n.setNationalityName(getCellValue(row.getCell(1)));
            list.add(n);
        }
        new DAOFramework<>(Nationality.class).saveAll(list, true, em);
    }

    private void importRoomTypeSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<RoomType> list = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }
            RoomType rt = new RoomType();
            rt.setRoomTypeID(id);
            rt.setTypeName(getCellValue(row.getCell(1)));
            rt.setCapacity(parseInteger(getCellValue(row.getCell(2))));
            rt.setPrice(parseBigDecimal(getCellValue(row.getCell(3))));
            rt.setRoomTypeImage(nullIfEmpty(getCellValue(row.getCell(4))));
            rt.setDescription(nullIfEmpty(getCellValue(row.getCell(5))));
            list.add(rt);
        }
        new DAOFramework<>(RoomType.class).saveAll(list, true, em);
    }

    private void importServiceSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<Service> list = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }
            Service s = new Service();
            s.setServiceID(id);
            s.setServiceName(getCellValue(row.getCell(1)));
            s.setUnitPrice(parseBigDecimal(getCellValue(row.getCell(2))));
            list.add(s);
        }
        new DAOFramework<>(Service.class).saveAll(list, true, em);
    }

    private void importPaymentMethodSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<Paymentmethod> list = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }
            Paymentmethod pm = new Paymentmethod();
            pm.setMethodID(id);
            pm.setMethodName(getCellValue(row.getCell(1)));
            list.add(pm);
        }
        new DAOFramework<>(Paymentmethod.class).saveAll(list, true, em);
    }

    private void importUsersSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<Users> list = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            Integer id = parseInteger(getCellValue(row.getCell(0)));
            if (id == null) {
                continue;
            }
            Users u = new Users();
            u.setUserID(id);
            u.setUsername(getCellValue(row.getCell(1)));
            u.setPassword(getCellValue(row.getCell(2)));
            u.setRole(getCellValue(row.getCell(3)));
            String mcpStr = getCellValue(row.getCell(4));
            u.setMustChangePassword(mcpStr.equalsIgnoreCase("true") || mcpStr.equals("1"));
            list.add(u);
        }
        em.createNativeQuery("SET IDENTITY_INSERT USERS ON").executeUpdate();
        new DAOFramework<>(Users.class).saveAll(list, true, em);
        em.createNativeQuery("SET IDENTITY_INSERT USERS OFF").executeUpdate();
    }

    private void importEmployeeSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<Employee> list = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }
            Employee e = new Employee();
            e.setEmployeeID(id);
            e.setFullName(getCellValue(row.getCell(1)));
            e.setPosition(getCellValue(row.getCell(2)));
            e.setSalary(parseBigDecimal(getCellValue(row.getCell(3))));
            e.setShift(nullIfEmpty(getCellValue(row.getCell(4))));
            e.setAddress(nullIfEmpty(getCellValue(row.getCell(5))));
            e.setPhone(nullIfEmpty(getCellValue(row.getCell(6))));
            e.setEmail(nullIfEmpty(getCellValue(row.getCell(7))));
            Integer userId = parseInteger(getCellValue(row.getCell(8)));
            if (userId != null) {
                e.setUserID(getSafeEntity(em, Users.class, userId));
            }
            list.add(e);
        }
        new DAOFramework<>(Employee.class).saveAll(list, true, em);
    }

    private void importCustomerSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<Customer> list = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }
            Customer c = new Customer();
            c.setCustomerID(id);
            c.setFullName(getCellValue(row.getCell(1)));
            c.setPhone(nullIfEmpty(getCellValue(row.getCell(2))));
            c.setEmail(nullIfEmpty(getCellValue(row.getCell(3))));
            c.setAddress(nullIfEmpty(getCellValue(row.getCell(4))));
            String natId = nullIfEmpty(getCellValue(row.getCell(5)));
            if (natId != null) {
                c.setNationalityID(getSafeEntity(em, Nationality.class, natId));
            }
            Integer userId = parseInteger(getCellValue(row.getCell(6)));
            if (userId != null) {
                c.setUserID(getSafeEntity(em, Users.class, userId));
            }
            list.add(c);
        }
        new DAOFramework<>(Customer.class).saveAll(list, true, em);
    }

    private void importUserBookingControlSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<UserBookingControl> list = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            Integer uId = parseInteger(getCellValue(row.getCell(0)));
            if (uId == null) {
                continue;
            }
            UserBookingControl u = new UserBookingControl();
            u.setUserID(uId);
            u.setWindowStartedAt(getDateCellValue(row.getCell(1)));
            u.setConsecutiveCancellationCount(parseInteger(getCellValue(row.getCell(2)), 0));
            u.setCancellationCountInWindow(parseInteger(getCellValue(row.getCell(3)), 0));
            u.setPenaltyLevel(parseShort(getCellValue(row.getCell(4)), 0));
            u.setBookingLockedUntil(getDateCellValue(row.getCell(5)));
            u.setUpdatedAt(getDateCellValue(row.getCell(6)));
            list.add(u);
        }
        new DAOFramework<>(UserBookingControl.class).saveAll(list, true, em);
    }

    private void importCustomerWalletSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<CustomerWallet> list = new ArrayList<>();
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
            CustomerWallet cw = new CustomerWallet();
            cw.setWalletID(wId);
            cw.setCustomerID(getSafeEntity(em, Customer.class, cusId));
            cw.setCreatedAt(getDateCellValue(row.getCell(2)));
            list.add(cw);
        }
        em.createNativeQuery("SET IDENTITY_INSERT CUSTOMER_WALLET ON").executeUpdate();
        new DAOFramework<>(CustomerWallet.class).saveAll(list, true, em);
        em.createNativeQuery("SET IDENTITY_INSERT CUSTOMER_WALLET OFF").executeUpdate();
    }

    private void importRoomSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<Room> list = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }
            Room r = new Room();
            r.setRoomID(id);
            r.setRoomNumber(getCellValue(row.getCell(1)));
            r.setRoomImage(nullIfEmpty(getCellValue(row.getCell(2))));
            r.setStatus(getCellValue(row.getCell(3)));
            String rtId = getCellValue(row.getCell(4));
            if (!rtId.isEmpty()) {
                r.setRoomTypeID(getSafeEntity(em, RoomType.class, rtId));
            }
            list.add(r);
        }
        new DAOFramework<>(Room.class).saveAll(list, true, em);
    }

    private void importComplaintSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<Complaint> list = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            Integer id = parseInteger(getCellValue(row.getCell(0)));
            if (id == null) {
                continue;
            }
            Complaint c = new Complaint();
            c.setComplaintID(id);
            c.setTitle(getCellValue(row.getCell(1)));
            c.setContent(getCellValue(row.getCell(2)));
            c.setCreatedAt(getDateCellValue(row.getCell(3)));
            c.setStatus(getCellValue(row.getCell(4)));
            String cusId = nullIfEmpty(getCellValue(row.getCell(5)));
            if (cusId != null) {
                c.setCustomerID(getSafeEntity(em, Customer.class, cusId));
            }
            c.setReplyMessage(nullIfEmpty(getCellValue(row.getCell(6))));
            c.setEmployeeID(nullIfEmpty(getCellValue(row.getCell(7))));
            list.add(c);
        }
        em.createNativeQuery("SET IDENTITY_INSERT COMPLAINT ON").executeUpdate();
        new DAOFramework<>(Complaint.class).saveAll(list, true, em);
        em.createNativeQuery("SET IDENTITY_INSERT COMPLAINT OFF").executeUpdate();
    }

    private void importBookingSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<Booking> list = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }
            Booking b = new Booking();
            b.setBookingID(id);
            b.setPaymentOption(getCellValue(row.getCell(1)));
            b.setFirstPaidAt(getDateCellValue(row.getCell(2)));
            b.setBookingAmountAtFirstPayment(parseBigDecimal(getCellValue(row.getCell(3))));
            b.setCancelledAt(getDateCellValue(row.getCell(4)));
            b.setCancellationReason(nullIfEmpty(getCellValue(row.getCell(5))));
            Integer uId = parseInteger(getCellValue(row.getCell(6)));
            if (uId != null) {
                b.setCancelledByUserID(getSafeEntity(em, Users.class, uId));
            }
            b.setBookingDate(getDateCellValue(row.getCell(7)));
            b.setPaymentDeadline(getDateCellValue(row.getCell(8)));
            b.setCheckInDate(getDateCellValue(row.getCell(9)));
            b.setCheckOutDate(getDateCellValue(row.getCell(10)));
            b.setBookingStatus(getCellValue(row.getCell(11)));
            String cusId = getCellValue(row.getCell(12));
            if (!cusId.isEmpty()) {
                b.setCustomerID(getSafeEntity(em, Customer.class, cusId));
            }
            b.setTotalAmount(parseBigDecimal(getCellValue(row.getCell(13))));
            list.add(b);
        }
        new DAOFramework<>(Booking.class).saveAll(list, true, em);
    }

    private void importBookingDetailSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<BookingDetail> list = new ArrayList<>();
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
            BookingDetail bd = new BookingDetail();
            bd.setBookingDetailID(id);
            bd.setBookingID(getSafeEntity(em, Booking.class, bId));
            bd.setRoomTypeID(getSafeEntity(em, RoomType.class, rtId));
            bd.setQuantity(parseInteger(getCellValue(row.getCell(3))));
            bd.setGuestCount(parseInteger(getCellValue(row.getCell(4))));
            bd.setUnitPrice(parseBigDecimal(getCellValue(row.getCell(5))));
            bd.setSubtotal(parseBigDecimal(getCellValue(row.getCell(6))));
            list.add(bd);
        }
        em.createNativeQuery("SET IDENTITY_INSERT BOOKING_DETAIL ON").executeUpdate();
        new DAOFramework<>(BookingDetail.class).saveAll(list, true, em);
        em.createNativeQuery("SET IDENTITY_INSERT BOOKING_DETAIL OFF").executeUpdate();
    }

    private void importBookingServiceSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<BookingService> list = new ArrayList<>();
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
            BookingService bs = new BookingService();
            bs.setBookingServicePK(new BookingServicePK(bId, sId));
            bs.setQuantity(parseInteger(getCellValue(row.getCell(2))));
            bs.setUnitPrice(parseBigDecimal(getCellValue(row.getCell(3))));
            bs.setSubtotal(parseBigDecimal(getCellValue(row.getCell(4))));
            list.add(bs);
        }
        new DAOFramework<>(BookingService.class).saveAll(list, true, em);
    }

    private void importRoomAssignmentSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<RoomAssignment> list = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            Integer id = parseInteger(getCellValue(row.getCell(0)));
            if (id == null) {
                continue;
            }
            RoomAssignment ra = new RoomAssignment();
            ra.setAssignmentID(id);
            Integer bdId = parseInteger(getCellValue(row.getCell(1)));
            if (bdId != null) {
                ra.setBookingDetailID(getSafeEntity(em, BookingDetail.class, bdId));
            }
            String rId = getCellValue(row.getCell(2));
            if (!rId.isEmpty()) {
                ra.setRoomID(getSafeEntity(em, Room.class, rId)); // <-- Nếu R08 vắng mặt, nó sẽ chết tại đây và báo lỗi cụ thể!
            }
            String empId = nullIfEmpty(getCellValue(row.getCell(3)));
            if (empId != null) {
                ra.setEmployeeID(getSafeEntity(em, Employee.class, empId));
            }
            ra.setAssignedAt(getDateCellValue(row.getCell(4)));
            list.add(ra);
        }
        em.createNativeQuery("SET IDENTITY_INSERT ROOM_ASSIGNMENT ON").executeUpdate();
        new DAOFramework<>(RoomAssignment.class).saveAll(list, true, em);
        em.createNativeQuery("SET IDENTITY_INSERT ROOM_ASSIGNMENT OFF").executeUpdate();
    }

    private void importTransactionSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<entity.Transaction> list = new ArrayList<>();
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
            entity.Transaction t = new entity.Transaction();
            t.setId(id);
            t.setTransactionID(txId);
            t.setTransactionType(getCellValue(row.getCell(2)));
            t.setTransactionValue(parseBigDecimal(getCellValue(row.getCell(3))));
            list.add(t);
        }
        em.createNativeQuery("SET IDENTITY_INSERT [TRANSACTION] ON").executeUpdate();
        new DAOFramework<>(entity.Transaction.class).saveAll(list, true, em);
        em.createNativeQuery("SET IDENTITY_INSERT [TRANSACTION] OFF").executeUpdate();
    }

    private void importPaymentSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<Payment> list = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }
            Payment p = new Payment();
            p.setPaymentID(id);
            p.setBookingID(getSafeEntity(em, Booking.class, getCellValue(row.getCell(1))));
            p.setPaymentType(getCellValue(row.getCell(2)));
            p.setPaymentTime(getDateCellValue(row.getCell(3)));
            p.setAmount(parseBigDecimal(getCellValue(row.getCell(4))));
            p.setWalletAmount(parseBigDecimal(getCellValue(row.getCell(5))));
            p.setStatus(getCellValue(row.getCell(6)));
            String methId = nullIfEmpty(getCellValue(row.getCell(7)));
            if (methId != null) {
                p.setMethodID(getSafeEntity(em, Paymentmethod.class, methId));
            }
            p.setTransactionCode(nullIfEmpty(getCellValue(row.getCell(8))));
            String txId = nullIfEmpty(getCellValue(row.getCell(9)));
            if (txId != null) {
                p.setTransactionID(findTransactionByCode(em, txId));
            }
            list.add(p);
        }
        new DAOFramework<>(Payment.class).saveAll(list, true, em);
    }

//    private void importBookingRefundSheet(Sheet sheet, EntityManager em) {
//        if (sheet == null) {
//            return;
//        }
//        List<BookingRefund> list = new ArrayList<>();
//        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
//            Row row = sheet.getRow(i);
//            if (isRowEmpty(row)) {
//                continue;
//            }
//            Long id = parseLong(getCellValue(row.getCell(0)));
//            if (id == null) {
//                continue;
//            }
//            BookingRefund r = new BookingRefund();
//            r.setRefundID(id);
//            r.setBooking(getSafeEntity(em, Booking.class, getCellValue(row.getCell(1))));
//            Long wId = parseLong(getCellValue(row.getCell(3)));
//            if (wId != null) {
//                r.setCustomerWallet(getSafeEntity(em, CustomerWallet.class, wId));
//            }
//            r.setAmount(parseBigDecimal(getCellValue(row.getCell(4))));
//            r.setRefundedAt(getDateCellValue(row.getCell(5)));
//            String txId = nullIfEmpty(getCellValue(row.getCell(6)));
//            if (txId != null) {
//                r.setTransactionID(findTransactionByCode(em, txId));
//            }
//            list.add(r);
//        }
//        em.createNativeQuery("SET IDENTITY_INSERT BOOKING_REFUND ON").executeUpdate();
//        new DAOFramework<>(BookingRefund.class).saveAll(list, true, em);
//        em.createNativeQuery("SET IDENTITY_INSERT BOOKING_REFUND OFF").executeUpdate();
//    }
    private void importRefundPaymentSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        String sql = "MERGE INTO REFUND_PAYMENT AS Target "
                + "USING (VALUES (?, ?, ?, ?)) AS Source (RefundID, BookingID, PaymentID, Amount) "
                + "ON Target.RefundID = Source.RefundID AND Target.PaymentID = Source.PaymentID "
                + "WHEN MATCHED THEN "
                + "  UPDATE SET Target.BookingID = Source.BookingID, Target.Amount = Source.Amount "
                + "WHEN NOT MATCHED THEN "
                + "  INSERT (RefundID, BookingID, PaymentID, Amount) VALUES (Source.RefundID, Source.BookingID, Source.PaymentID, Source.Amount);";
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

//    private void importWalletTransactionSheet(Sheet sheet, EntityManager em) {
//        if (sheet == null) {
//            return;
//        }
//        List<WalletTransaction> list = new ArrayList<>();
//        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
//            Row row = sheet.getRow(i);
//            if (isRowEmpty(row)) {
//                continue;
//            }
//            Long id = parseLong(getCellValue(row.getCell(0)));
//            if (id == null) {
//                continue;
//            }
//            WalletTransaction wt = new WalletTransaction();
//            wt.setWalletTransactionID(id);
//            Long wId = parseLong(getCellValue(row.getCell(1)));
//            if (wId != null) {
//                wt.setWalletID(getSafeEntity(em, CustomerWallet.class, wId));
//            }
//            wt.setTransactionType(getCellValue(row.getCell(2)));
//            wt.setAmount(parseBigDecimal(getCellValue(row.getCell(3))));
//            wt.setTransactionTime(getDateCellValue(row.getCell(4)));
//            Long refId = parseLong(getCellValue(row.getCell(5)));
//            if (refId != null) {
//                wt.setBookingRefund(getSafeEntity(em, BookingRefund.class, refId));
//            }
//            String payId = nullIfEmpty(getCellValue(row.getCell(6)));
//            if (payId != null) {
//                wt.setPayment(getSafeEntity(em, Payment.class, payId));
//            }
//            String txId = nullIfEmpty(getCellValue(row.getCell(7)));
//            if (txId != null) {
//                wt.setTransactionID(findTransactionByCode(em, txId));
//            }
//            list.add(wt);
//        }
//        em.createNativeQuery("SET IDENTITY_INSERT WALLET_TRANSACTION ON").executeUpdate();
//        new DAOFramework<>(WalletTransaction.class).saveAll(list, true, em);
//        em.createNativeQuery("SET IDENTITY_INSERT WALLET_TRANSACTION OFF").executeUpdate();
//    }
//    private void importBookingLockHistorySheet(Sheet sheet, EntityManager em) {
//        if (sheet == null) {
//            return;
//        }
//        List<BookingLockHistory> list = new ArrayList<>();
//        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
//            Row row = sheet.getRow(i);
//            if (isRowEmpty(row)) {
//                continue;
//            }
//            Long id = parseLong(getCellValue(row.getCell(0)));
//            if (id == null) {
//                continue;
//            }
//            BookingLockHistory h = new BookingLockHistory();
//            h.setLockHistoryID(id);
//            Integer uId = parseInteger(getCellValue(row.getCell(1)));
//            if (uId != null) {
//                h.setUserID(getSafeEntity(em, Users.class, uId));
//            }
//            String bId = nullIfEmpty(getCellValue(row.getCell(2)));
//            if (bId != null) {
//                h.setTriggerBookingID(getSafeEntity(em, Booking.class, bId));
//            }
//            h.setPenaltyLevel(parseShort(getCellValue(row.getCell(3)), 0));
//            h.setLockedAt(getDateCellValue(row.getCell(4)));
//            h.setLockedUntil(getDateCellValue(row.getCell(5)));
//            h.setDurationMinutes(parseInteger(getCellValue(row.getCell(6))));
//            list.add(h);
//        }
//        em.createNativeQuery("SET IDENTITY_INSERT BOOKING_LOCK_HISTORY ON").executeUpdate();
//        new DAOFramework<>(BookingLockHistory.class).saveAll(list, true, em);
//        em.createNativeQuery("SET IDENTITY_INSERT BOOKING_LOCK_HISTORY OFF").executeUpdate();
//    }
    private void importInvoiceSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<Invoice> list = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            String id = getCellValue(row.getCell(0));
            if (id.isEmpty()) {
                continue;
            }
            Invoice invoice = new Invoice();
            invoice.setInvoiceID(id);
            invoice.setInvoiceDate(getDateCellValue(row.getCell(1)));
            invoice.setTotalAmount(parseBigDecimal(getCellValue(row.getCell(2))));
            invoice.setStatus(getCellValue(row.getCell(3)));
            String repId = nullIfEmpty(getCellValue(row.getCell(4)));
            if (repId != null) {
                invoice.setReplacedInvoiceID(getSafeEntity(em, Invoice.class, repId));
            }
            String empId = nullIfEmpty(getCellValue(row.getCell(5)));
            if (empId != null) {
                invoice.setEmployeeID(getSafeEntity(em, Employee.class, empId));
            }
            String bId = nullIfEmpty(getCellValue(row.getCell(6)));
            if (bId != null) {
                invoice.setBookingID(getSafeEntity(em, Booking.class, bId));
            }
            list.add(invoice);
        }
        new DAOFramework<>(Invoice.class).saveAll(list, true, em);
    }

    private void importInvoiceTransactionSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        List<InvoiceTransaction> list = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }
            String iId = getCellValue(row.getCell(0));
            String bId = getCellValue(row.getCell(1));
            String pId = getCellValue(row.getCell(2));
            String txId = getCellValue(row.getCell(3));
            if (iId.isEmpty() || bId.isEmpty()) {
                continue;
            }
            InvoiceTransaction it = new InvoiceTransaction();
            it.setInvoiceTransactionPK(new InvoiceTransactionPK(iId, txId));
            it.setInvoice(getSafeEntity(em, Invoice.class, iId));
            it.setPayment(getSafeEntity(em, Payment.class, pId));
            list.add(it);
        }
        new DAOFramework<>(InvoiceTransaction.class).saveAll(list, true, em);
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
                    .setParameter(7, nullIfEmpty(getCellValue(row.getCell(6))))
                    .executeUpdate();
        }
        em.createNativeQuery("SET IDENTITY_INSERT BOOKING_REFUND OFF").executeUpdate();
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
                    .setParameter(8, nullIfEmpty(getCellValue(row.getCell(7))))
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
                    .setParameter(3, nullIfEmpty(getCellValue(row.getCell(2))))
                    .setParameter(4, parseShort(getCellValue(row.getCell(3)), 0))
                    .setParameter(5, getDateCellValue(row.getCell(4)))
                    .setParameter(6, getDateCellValue(row.getCell(5)))
                    .setParameter(7, parseInteger(getCellValue(row.getCell(6))))
                    .executeUpdate();
        }
        em.createNativeQuery("SET IDENTITY_INSERT BOOKING_LOCK_HISTORY OFF").executeUpdate();
    }

    // ==========================================
    // CÁC HÀM TIỆN ÍCH DÙNG CHUNG
    // ==========================================
    private String nullIfEmpty(String str) {
        return (str == null || str.trim().isEmpty()) ? null : str.trim();
    }

    private Integer parseInteger(String str) {
        return parseInteger(str, null);
    }

    private Short parseShort(String str, Integer defaultVal) {
        Integer val = parseInteger(str, defaultVal);
        return val == null ? null : val.shortValue();
    }

    private Integer parseInteger(String str, Integer defaultVal) {
        if (str == null || str.trim().isEmpty()) {
            return defaultVal;
        }
        try {
            return (int) Double.parseDouble(str);
        } catch (NumberFormatException e) {
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

    private entity.Transaction findTransactionByCode(EntityManager em, String txCode) {
        if (txCode == null || txCode.trim().isEmpty()) {
            return null;
        }
        try {
            return em.createQuery("SELECT t FROM Transaction t WHERE t.transactionID = :txCode", entity.Transaction.class)
                    .setParameter("txCode", txCode.trim())
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}
