package service;

import dao.DAOFramework;
import entity.*;
import jakarta.persistence.EntityManager;
import java.io.OutputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import util.PersistenceManager;

/**
 * Service xử lý xuất toàn bộ Database ra file Excel (.xlsx). Tối ưu connection
 * bằng cách truyền 1 EntityManager dùng chung cho toàn bộ DAOFramework.
 */
public class ExportDataService {

    // ==========================================
    // 1. CHỨC NĂNG EXPORT (SAO LƯU DB RA EXCEL)
    // ==========================================
    public void exportDB(OutputStream out) throws Exception {
        EntityManager em = PersistenceManager.createEntityManager();
        try (Workbook workbook = new XSSFWorkbook()) {

            // Master Data
            exportHotelSheet(workbook, em);
            exportNationalitySheet(workbook, em);
            exportRoomTypeSheet(workbook, em);
            exportServiceSheet(workbook, em);
            exportPaymentMethodSheet(workbook, em);

            // Users & Actors
            exportUsersSheet(workbook, em);
            exportEmployeeSheet(workbook, em);
            exportCustomerSheet(workbook, em);
            exportUserBookingControlSheet(workbook, em);
            exportCustomerWalletSheet(workbook, em);

            // Entities
            exportRoomSheet(workbook, em);
            exportComplaintSheet(workbook, em);

            // Giao dịch Booking
            exportBookingSheet(workbook, em);
            exportBookingDetailSheet(workbook, em);
            exportBookingServiceSheet(workbook, em);
            exportRoomAssignmentSheet(workbook, em);

            // Thanh toán, Ví & Hoàn tiền
            exportTransactionSheet(workbook, em);
            exportPaymentSheet(workbook, em);
            exportBookingRefundSheet(workbook, em);
            exportRefundPaymentSheet(workbook, em);
            exportWalletTransactionSheet(workbook, em);
            exportBookingLockHistorySheet(workbook, em);

            // Hóa đơn
            exportInvoiceSheet(workbook, em);
            exportInvoiceTransactionSheet(workbook, em);

            workbook.write(out);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    private void exportHotelSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("HOTEL");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("HotelName");
        header.createCell(1).setCellValue("HotelImage");
        header.createCell(2).setCellValue("RoomImage");
        header.createCell(3).setCellValue("PoolImage");
        header.createCell(4).setCellValue("RestaurantImage");
        header.createCell(5).setCellValue("Address");

        List<Hotel> hotels = new DAOFramework<>(Hotel.class).getAllBypassCache(em);
        Hotel h = hotels.isEmpty() ? new Hotel() : hotels.get(0);

        int rowIdx = 1;
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(h.getHotelName() != null ? h.getHotelName() : "");
        row.createCell(1).setCellValue(h.getHotelImage() != null ? h.getHotelImage() : "");
        row.createCell(2).setCellValue(h.getRoomImage() != null ? h.getRoomImage() : "");
        row.createCell(3).setCellValue(h.getPoolImage() != null ? h.getPoolImage() : "");
        row.createCell(4).setCellValue(h.getRestaurantImage() != null ? h.getRestaurantImage() : "");
        row.createCell(5).setCellValue(h.getAddress() != null ? h.getAddress() : "");
    }

    private void exportNationalitySheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("NATIONALITY");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("NationalityID");
        header.createCell(1).setCellValue("NationalityName");

        List<Nationality> list = new DAOFramework<>(Nationality.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (Nationality n : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(n.getNationalityID() != null ? n.getNationalityID() : "");
            row.createCell(1).setCellValue(n.getNationalityName() != null ? n.getNationalityName() : "");
        }
    }

    private void exportRoomTypeSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("ROOM_TYPE");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("RoomTypeID");
        header.createCell(1).setCellValue("TypeName");
        header.createCell(2).setCellValue("Capacity");
        header.createCell(3).setCellValue("Price");
        header.createCell(4).setCellValue("RoomTypeImage");
        header.createCell(5).setCellValue("Description");

        List<RoomType> list = new DAOFramework<>(RoomType.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (RoomType rt : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(rt.getRoomTypeID() != null ? rt.getRoomTypeID() : "");
            row.createCell(1).setCellValue(rt.getTypeName() != null ? rt.getTypeName() : "");
            row.createCell(2).setCellValue(rt.getCapacity());
            if (rt.getPrice() != null) {
                row.createCell(3).setCellValue(rt.getPrice().doubleValue());
            }
            row.createCell(4).setCellValue(rt.getRoomTypeImage() != null ? rt.getRoomTypeImage() : "");
            row.createCell(5).setCellValue(rt.getDescription() != null ? rt.getDescription() : "");
        }
    }

    private void exportServiceSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("SERVICE");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ServiceID");
        header.createCell(1).setCellValue("ServiceName");
        header.createCell(2).setCellValue("UnitPrice");

        List<Service> list = new DAOFramework<>(Service.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (Service s : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(s.getServiceID() != null ? s.getServiceID() : "");
            row.createCell(1).setCellValue(s.getServiceName() != null ? s.getServiceName() : "");
            if (s.getUnitPrice() != null) {
                row.createCell(2).setCellValue(s.getUnitPrice().doubleValue());
            }
        }
    }

    private void exportPaymentMethodSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("PAYMENTMETHOD");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("MethodID");
        header.createCell(1).setCellValue("MethodName");

        List<Paymentmethod> list = new DAOFramework<>(Paymentmethod.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (Paymentmethod pm : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(pm.getMethodID() != null ? pm.getMethodID() : "");
            row.createCell(1).setCellValue(pm.getMethodName() != null ? pm.getMethodName() : "");
        }
    }

    private void exportUsersSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("USERS");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("UserID");
        header.createCell(1).setCellValue("Username");
        header.createCell(2).setCellValue("Password");
        header.createCell(3).setCellValue("Role");
        header.createCell(4).setCellValue("MustChangePassword");

        List<Users> list = new DAOFramework<>(Users.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (Users u : list) {
            Row row = sheet.createRow(rowIdx++);
            if (u.getUserID() != null) {
                row.createCell(0).setCellValue(u.getUserID());
            }
            row.createCell(1).setCellValue(u.getUsername() != null ? u.getUsername() : "");
            row.createCell(2).setCellValue(u.getPassword() != null ? u.getPassword() : "");
            row.createCell(3).setCellValue(u.getRole() != null ? u.getRole() : "");

            Boolean mcp = u.getMustChangePassword();
            row.createCell(4).setCellValue(mcp != null ? mcp : false);
        }
    }

    private void exportEmployeeSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("EMPLOYEE");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("EmployeeID");
        header.createCell(1).setCellValue("FullName");
        header.createCell(2).setCellValue("Position");
        header.createCell(3).setCellValue("Salary");
        header.createCell(4).setCellValue("Shift");
        header.createCell(5).setCellValue("Address");
        header.createCell(6).setCellValue("Phone");
        header.createCell(7).setCellValue("Email");
        header.createCell(8).setCellValue("UserID");

        List<Employee> list = new DAOFramework<>(Employee.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (Employee e : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(e.getEmployeeID() != null ? e.getEmployeeID() : "");
            row.createCell(1).setCellValue(e.getFullName() != null ? e.getFullName() : "");
            row.createCell(2).setCellValue(e.getPosition() != null ? e.getPosition() : "");
            if (e.getSalary() != null) {
                row.createCell(3).setCellValue(e.getSalary().doubleValue());
            }
            row.createCell(4).setCellValue(e.getShift() != null ? e.getShift() : "");
            row.createCell(5).setCellValue(e.getAddress() != null ? e.getAddress() : "");
            row.createCell(6).setCellValue(e.getPhone() != null ? e.getPhone() : "");
            row.createCell(7).setCellValue(e.getEmail() != null ? e.getEmail() : "");
            if (e.getUserID() != null) {
                row.createCell(8).setCellValue(e.getUserID().getUserID());
            }
        }
    }

    private void exportCustomerSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("CUSTOMER");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("CustomerID");
        header.createCell(1).setCellValue("FullName");
        header.createCell(2).setCellValue("Phone");
        header.createCell(3).setCellValue("Email");
        header.createCell(4).setCellValue("Address");
        header.createCell(5).setCellValue("NationalityID");
        header.createCell(6).setCellValue("UserID");

        List<Customer> list = new DAOFramework<>(Customer.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (Customer c : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(c.getCustomerID() != null ? c.getCustomerID() : "");
            row.createCell(1).setCellValue(c.getFullName() != null ? c.getFullName() : "");
            row.createCell(2).setCellValue(c.getPhone() != null ? c.getPhone() : "");
            row.createCell(3).setCellValue(c.getEmail() != null ? c.getEmail() : "");
            row.createCell(4).setCellValue(c.getAddress() != null ? c.getAddress() : "");
            if (c.getNationalityID() != null) {
                row.createCell(5).setCellValue(c.getNationalityID().getNationalityID());
            }
            if (c.getUserID() != null) {
                row.createCell(6).setCellValue(c.getUserID().getUserID());
            }
        }
    }

    private void exportRoomSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("ROOM");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("RoomID");
        header.createCell(1).setCellValue("RoomNumber");
        header.createCell(2).setCellValue("RoomImage");
        header.createCell(3).setCellValue("Status");
        header.createCell(4).setCellValue("RoomTypeID");

        List<Room> list = new DAOFramework<>(Room.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (Room r : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(r.getRoomID() != null ? r.getRoomID() : "");
            row.createCell(1).setCellValue(r.getRoomNumber() != null ? r.getRoomNumber() : "");
            row.createCell(2).setCellValue(r.getRoomImage() != null ? r.getRoomImage() : "");
            row.createCell(3).setCellValue(r.getStatus() != null ? r.getStatus() : "");
            if (r.getRoomTypeID() != null) {
                row.createCell(4).setCellValue(r.getRoomTypeID().getRoomTypeID());
            }
        }
    }

    private void exportComplaintSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("COMPLAINT");
        Row header = sheet.createRow(0);

        CellStyle dateStyle = wb.createCellStyle();
        CreationHelper createHelper = wb.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

        header.createCell(0).setCellValue("ComplaintID");
        header.createCell(1).setCellValue("Title");
        header.createCell(2).setCellValue("Content");
        header.createCell(3).setCellValue("CreatedAt");
        header.createCell(4).setCellValue("Status");
        header.createCell(5).setCellValue("CustomerID");
        header.createCell(6).setCellValue("ReplyMessage");
        header.createCell(7).setCellValue("EmployeeID");

        List<Complaint> list = new DAOFramework<>(Complaint.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (Complaint c : list) {
            Row row = sheet.createRow(rowIdx++);
            if (c.getComplaintID() != null) {
                row.createCell(0).setCellValue(c.getComplaintID());
            }
            row.createCell(1).setCellValue(c.getTitle() != null ? c.getTitle() : "");
            row.createCell(2).setCellValue(c.getContent() != null ? c.getContent() : "");

            if (c.getCreatedAt() != null) {
                Cell cell = row.createCell(3);
                cell.setCellValue(c.getCreatedAt());
                cell.setCellStyle(dateStyle);
            }
            row.createCell(4).setCellValue(c.getStatus() != null ? c.getStatus() : "");
            if (c.getCustomerID() != null) {
                row.createCell(5).setCellValue(c.getCustomerID().getCustomerID());
            }
            row.createCell(6).setCellValue(c.getReplyMessage() != null ? c.getReplyMessage() : "");
            if (c.getEmployeeID() != null) {
                row.createCell(7).setCellValue(c.getEmployeeID());
            }
        }
    }

    private void exportBookingSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("BOOKING");
        Row header = sheet.createRow(0);

        CellStyle dateStyle = wb.createCellStyle();
        CreationHelper createHelper = wb.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

        header.createCell(0).setCellValue("BookingID");
        header.createCell(1).setCellValue("PaymentOption");
        header.createCell(2).setCellValue("FirstPaidAt");
        header.createCell(3).setCellValue("BookingAmountAtFirstPayment");
        header.createCell(4).setCellValue("CancelledAt");
        header.createCell(5).setCellValue("CancellationReason");
        header.createCell(6).setCellValue("CancelledByUserID");
        header.createCell(7).setCellValue("BookingDate");
        header.createCell(8).setCellValue("PaymentDeadline");
        header.createCell(9).setCellValue("CheckInDate");
        header.createCell(10).setCellValue("CheckOutDate");
        header.createCell(11).setCellValue("BookingStatus");
        header.createCell(12).setCellValue("CustomerID");
        header.createCell(13).setCellValue("TotalAmount");
        header.createCell(14).setCellValue("RefundDeadline");

        List<Booking> list = new DAOFramework<>(Booking.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (Booking b : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(b.getBookingID() != null ? b.getBookingID() : "");
            row.createCell(1).setCellValue(b.getPaymentOption() != null ? b.getPaymentOption() : "");

            if (b.getFirstPaidAt() != null) {
                Cell cell = row.createCell(2);
                cell.setCellValue(b.getFirstPaidAt());
                cell.setCellStyle(dateStyle);
            }
            if (b.getBookingAmountAtFirstPayment() != null) {
                row.createCell(3).setCellValue(b.getBookingAmountAtFirstPayment().doubleValue());
            }
            if (b.getCancelledAt() != null) {
                Cell cell = row.createCell(4);
                cell.setCellValue(b.getCancelledAt());
                cell.setCellStyle(dateStyle);
            }
            row.createCell(5).setCellValue(b.getCancellationReason() != null ? b.getCancellationReason() : "");
            if (b.getCancelledByUserID() != null) {
                row.createCell(6).setCellValue(b.getCancelledByUserID().getUserID());
            }
            if (b.getBookingDate() != null) {
                Cell cell = row.createCell(7);
                cell.setCellValue(b.getBookingDate());
                cell.setCellStyle(dateStyle);
            }
            if (b.getPaymentDeadline() != null) {
                Cell cell = row.createCell(8);
                cell.setCellValue(b.getPaymentDeadline());
                cell.setCellStyle(dateStyle);
            }
            if (b.getCheckInDate() != null) {
                Cell cell = row.createCell(9);
                cell.setCellValue(b.getCheckInDate());
                cell.setCellStyle(dateStyle);
            }
            if (b.getCheckOutDate() != null) {
                Cell cell = row.createCell(10);
                cell.setCellValue(b.getCheckOutDate());
                cell.setCellStyle(dateStyle);
            }

            row.createCell(11).setCellValue(b.getBookingStatus() != null ? b.getBookingStatus() : "");
            if (b.getCustomerID() != null) {
                row.createCell(12).setCellValue(b.getCustomerID().getCustomerID());
            }
            if (b.getTotalAmount() != null) {
                row.createCell(13).setCellValue(b.getTotalAmount().doubleValue());
            }
        }
    }

    private void exportBookingDetailSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("BOOKING_DETAIL");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("BookingDetailID");
        header.createCell(1).setCellValue("BookingID");
        header.createCell(2).setCellValue("RoomTypeID");
        header.createCell(3).setCellValue("Quantity");
        header.createCell(4).setCellValue("GuestCount");
        header.createCell(5).setCellValue("UnitPrice");
        header.createCell(6).setCellValue("Subtotal");

        List<BookingDetail> list = new DAOFramework<>(BookingDetail.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (BookingDetail bd : list) {
            Row row = sheet.createRow(rowIdx++);
            if (bd.getBookingDetailID() != null) {
                row.createCell(0).setCellValue(bd.getBookingDetailID());
            }
            if (bd.getBookingID() != null) {
                row.createCell(1).setCellValue(bd.getBookingID().getBookingID());
            }
            if (bd.getRoomTypeID() != null) {
                row.createCell(2).setCellValue(bd.getRoomTypeID().getRoomTypeID());
            }
            row.createCell(3).setCellValue(bd.getQuantity());
            row.createCell(4).setCellValue(bd.getGuestCount());
            if (bd.getUnitPrice() != null) {
                row.createCell(5).setCellValue(bd.getUnitPrice().doubleValue());
            }
            if (bd.getSubtotal() != null) {
                row.createCell(6).setCellValue(bd.getSubtotal().doubleValue());
            }
        }
    }

    private void exportBookingServiceSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("BOOKING_SERVICE");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("BookingID");
        header.createCell(1).setCellValue("ServiceID");
        header.createCell(2).setCellValue("Quantity");
        header.createCell(3).setCellValue("UnitPrice");
        header.createCell(4).setCellValue("Subtotal");

        List<BookingService> list = new DAOFramework<>(BookingService.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (BookingService bs : list) {
            Row row = sheet.createRow(rowIdx++);
            if (bs.getBookingServicePK() != null) {
                row.createCell(0).setCellValue(bs.getBookingServicePK().getBookingID());
                row.createCell(1).setCellValue(bs.getBookingServicePK().getServiceID());
            }
            row.createCell(2).setCellValue(bs.getQuantity());
            if (bs.getUnitPrice() != null) {
                row.createCell(3).setCellValue(bs.getUnitPrice().doubleValue());
            }
            if (bs.getSubtotal() != null) {
                row.createCell(4).setCellValue(bs.getSubtotal().doubleValue());
            }
        }
    }

    private void exportRoomAssignmentSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("ROOM_ASSIGNMENT");
        Row header = sheet.createRow(0);

        CellStyle dateStyle = wb.createCellStyle();
        CreationHelper createHelper = wb.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

        header.createCell(0).setCellValue("AssignmentID");
        header.createCell(1).setCellValue("BookingDetailID");
        header.createCell(2).setCellValue("RoomID");
        header.createCell(3).setCellValue("EmployeeID");
        header.createCell(4).setCellValue("AssignedAt");

        List<RoomAssignment> list = new DAOFramework<>(RoomAssignment.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (RoomAssignment ra : list) {
            Row row = sheet.createRow(rowIdx++);
            if (ra.getAssignmentID() != null) {
                row.createCell(0).setCellValue(ra.getAssignmentID());
            }
            if (ra.getBookingDetailID() != null) {
                row.createCell(1).setCellValue(ra.getBookingDetailID().getBookingDetailID());
            }
            if (ra.getRoomID() != null) {
                row.createCell(2).setCellValue(ra.getRoomID().getRoomID());
            }
            if (ra.getEmployeeID() != null) {
                row.createCell(3).setCellValue(ra.getEmployeeID().getEmployeeID());
            }
            if (ra.getAssignedAt() != null) {
                Cell cell = row.createCell(4);
                cell.setCellValue(ra.getAssignedAt());
                cell.setCellStyle(dateStyle);
            }
        }
    }

    private void exportPaymentSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("PAYMENT");
        Row header = sheet.createRow(0);

        CellStyle dateStyle = wb.createCellStyle();
        CreationHelper createHelper = wb.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

        header.createCell(0).setCellValue("PaymentID");
        header.createCell(1).setCellValue("BookingID");
        header.createCell(2).setCellValue("PaymentType");
        header.createCell(3).setCellValue("PaymentTime");
        header.createCell(4).setCellValue("Amount");
        header.createCell(5).setCellValue("WalletAmount");
        header.createCell(6).setCellValue("Status");
        header.createCell(7).setCellValue("MethodID");
        header.createCell(8).setCellValue("TransactionCode");
        header.createCell(9).setCellValue("TransactionID");

        List<Payment> list = new DAOFramework<>(Payment.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (Payment p : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(p.getPaymentID() != null ? p.getPaymentID() : "");
            if (p.getBookingID() != null) {
                row.createCell(1).setCellValue(p.getBookingID().getBookingID());
            }
            row.createCell(2).setCellValue(p.getPaymentType() != null ? p.getPaymentType() : "");

            if (p.getPaymentTime() != null) {
                Cell cell = row.createCell(3);
                cell.setCellValue(p.getPaymentTime());
                cell.setCellStyle(dateStyle);
            }
            if (p.getAmount() != null) {
                row.createCell(4).setCellValue(p.getAmount().doubleValue());
            }
            if (p.getWalletAmount() != null) {
                row.createCell(5).setCellValue(p.getWalletAmount().doubleValue());
            }
            row.createCell(6).setCellValue(p.getStatus() != null ? p.getStatus() : "");
            if (p.getMethodID() != null) {
                row.createCell(7).setCellValue(p.getMethodID().getMethodID());
            }
            row.createCell(8).setCellValue(p.getTransactionCode() != null ? p.getTransactionCode() : "");
            row.createCell(9).setCellValue(p.getTransactionID() != null ? (p.getTransactionID().getTransactionID() != null ? p.getTransactionID().getTransactionID() : "") : "");
        }
    }

    private void exportInvoiceSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("INVOICE");
        Row header = sheet.createRow(0);

        CellStyle dateStyle = wb.createCellStyle();
        CreationHelper createHelper = wb.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

        header.createCell(0).setCellValue("InvoiceID");
        header.createCell(1).setCellValue("InvoiceDate");
        header.createCell(2).setCellValue("TotalAmount");
        header.createCell(3).setCellValue("Status");
        header.createCell(4).setCellValue("ReplacedInvoiceID");
        header.createCell(5).setCellValue("EmployeeID");
        header.createCell(6).setCellValue("BookingID");

        List<Invoice> list = new DAOFramework<>(Invoice.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (Invoice i : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(i.getInvoiceID() != null ? i.getInvoiceID() : "");

            if (i.getInvoiceDate() != null) {
                Cell cell = row.createCell(1);
                cell.setCellValue(i.getInvoiceDate());
                cell.setCellStyle(dateStyle);
            }

            if (i.getTotalAmount() != null) {
                row.createCell(2).setCellValue(i.getTotalAmount().doubleValue());
            }
            row.createCell(3).setCellValue(i.getStatus() != null ? i.getStatus() : "");
            if (i.getReplacedInvoiceID() != null) {
                row.createCell(4).setCellValue(i.getReplacedInvoiceID().getInvoiceID());
            }
            if (i.getEmployeeID() != null) {
                row.createCell(5).setCellValue(i.getEmployeeID().getEmployeeID());
            }
            if (i.getBookingID() != null) {
                row.createCell(6).setCellValue(i.getBookingID().getBookingID());
            }
        }
    }

    private void exportTransactionSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("TRANSACTION");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("id");
        header.createCell(1).setCellValue("TransactionID");
        header.createCell(2).setCellValue("TransactionType");
        header.createCell(3).setCellValue("TransactionValue");

        List<Transaction> list = new DAOFramework<>(Transaction.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (entity.Transaction t : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(t.getId() != null ? t.getId() : 0L);
            row.createCell(1).setCellValue(t.getTransactionID() != null ? t.getTransactionID() : "");
            row.createCell(2).setCellValue(t.getTransactionType() != null ? t.getTransactionType() : "");
            if (t.getTransactionValue() != null) {
                row.createCell(3).setCellValue(t.getTransactionValue().doubleValue());
            }
        }
    }

    private void exportCustomerWalletSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("CUSTOMER_WALLET");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("WalletID");
        header.createCell(1).setCellValue("CustomerID");
        header.createCell(2).setCellValue("CreatedAt");

        CellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

        List<CustomerWallet> list = new DAOFramework<>(CustomerWallet.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (CustomerWallet cw : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(cw.getWalletID() != null ? cw.getWalletID() : 0L);
            if (cw.getCustomerID() != null) {
                row.createCell(1).setCellValue(cw.getCustomerID().getCustomerID());
            }
            if (cw.getCreatedAt() != null) {
                Cell cell = row.createCell(2);
                cell.setCellValue(cw.getCreatedAt());
                cell.setCellStyle(dateStyle);
            }
        }
    }

    private void exportBookingRefundSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("BOOKING_REFUND");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("RefundID");
        header.createCell(1).setCellValue("BookingID");
        header.createCell(2).setCellValue("CustomerID");
        header.createCell(3).setCellValue("WalletID");
        header.createCell(4).setCellValue("Amount");
        header.createCell(5).setCellValue("RefundedAt");
        header.createCell(6).setCellValue("TransactionID");

        CellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

        List<BookingRefund> list = new DAOFramework<>(BookingRefund.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (BookingRefund r : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(r.getRefundID() != null ? r.getRefundID() : 0L);
            if (r.getBooking() != null) {
                row.createCell(1).setCellValue(r.getBooking().getBookingID());
            }
            if (r.getCustomerWallet() != null && r.getCustomerWallet().getCustomerID() != null) {
                row.createCell(2).setCellValue(r.getCustomerWallet().getCustomerID().getCustomerID());
            }
            if (r.getCustomerWallet() != null) {
                row.createCell(3).setCellValue(r.getCustomerWallet().getWalletID());
            }
            if (r.getAmount() != null) {
                row.createCell(4).setCellValue(r.getAmount().doubleValue());
            }
            if (r.getRefundedAt() != null) {
                Cell cell = row.createCell(5);
                cell.setCellValue(r.getRefundedAt());
                cell.setCellStyle(dateStyle);
            }
            if (r.getTransactionID() != null) {
                row.createCell(6).setCellValue(r.getTransactionID().getTransactionID());
            }
        }
    }

    /**
     * Dữ liệu REFUND_PAYMENT là bảng nối trung gian (JoinTable), không phải
     * Entity độc lập.
     */
    private void exportRefundPaymentSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("REFUND_PAYMENT");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("RefundID");
        header.createCell(1).setCellValue("BookingID");
        header.createCell(2).setCellValue("PaymentID");
        header.createCell(3).setCellValue("Amount");

        List<Payment> list = new DAOFramework<>(Payment.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (Payment p : list) {
            if (p.getBookingRefundCollection() != null) {
                for (BookingRefund r : p.getBookingRefundCollection()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(r.getRefundID() != null ? r.getRefundID() : 0L);
                    row.createCell(1).setCellValue(p.getBookingID() != null ? p.getBookingID().getBookingID() : "");
                    row.createCell(2).setCellValue(p.getPaymentID());
                    row.createCell(3).setCellValue(r.getAmount() != null ? r.getAmount().doubleValue() : 0.0);
                }
            }
        }
    }

    private void exportWalletTransactionSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("WALLET_TRANSACTION");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("WalletTransactionID");
        header.createCell(1).setCellValue("WalletID");
        header.createCell(2).setCellValue("TransactionType");
        header.createCell(3).setCellValue("Amount");
        header.createCell(4).setCellValue("TransactionTime");
        header.createCell(5).setCellValue("RefundID");
        header.createCell(6).setCellValue("PaymentID");
        header.createCell(7).setCellValue("TransactionID");

        CellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

        List<WalletTransaction> list = new DAOFramework<>(WalletTransaction.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (WalletTransaction wt : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(wt.getWalletTransactionID() != null ? wt.getWalletTransactionID() : 0L);
            if (wt.getWalletID() != null) {
                row.createCell(1).setCellValue(wt.getWalletID().getWalletID());
            }
            row.createCell(2).setCellValue(wt.getTransactionType() != null ? wt.getTransactionType() : "");
            if (wt.getAmount() != null) {
                row.createCell(3).setCellValue(wt.getAmount().doubleValue());
            }
            if (wt.getTransactionTime() != null) {
                Cell cell = row.createCell(4);
                cell.setCellValue(wt.getTransactionTime());
                cell.setCellStyle(dateStyle);
            }
            if (wt.getBookingRefund() != null) {
                row.createCell(5).setCellValue(wt.getBookingRefund().getRefundID());
            }
            if (wt.getPayment() != null && wt.getPayment().getPaymentID() != null) {
                row.createCell(6).setCellValue(wt.getPayment().getPaymentID());
            } else if (wt.getPaymentID() != null && wt.getPaymentID().getPaymentID() != null) {
                row.createCell(6).setCellValue(wt.getPaymentID().getPaymentID());
            }
            if (wt.getTransactionID() != null) {
                row.createCell(7).setCellValue(wt.getTransactionID().getTransactionID());
            }
        }
    }

    private void exportUserBookingControlSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("USER_BOOKING_CONTROL");
        Row header = sheet.createRow(0);

        CellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

        header.createCell(0).setCellValue("UserID");
        header.createCell(1).setCellValue("WindowStartedAt");
        header.createCell(2).setCellValue("ConsecutiveCancellationCount");
        header.createCell(3).setCellValue("CancellationCountInWindow");
        header.createCell(4).setCellValue("PenaltyLevel");
        header.createCell(5).setCellValue("BookingLockedUntil");
        header.createCell(6).setCellValue("UpdatedAt");

        List<UserBookingControl> list = new DAOFramework<>(UserBookingControl.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (UserBookingControl u : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(u.getUserID() != null ? u.getUserID() : 0);

            if (u.getWindowStartedAt() != null) {
                Cell cell = row.createCell(1);
                cell.setCellValue(u.getWindowStartedAt());
                cell.setCellStyle(dateStyle);
            }

            row.createCell(2).setCellValue(u.getConsecutiveCancellationCount());
            row.createCell(3).setCellValue(u.getCancellationCountInWindow());
            row.createCell(4).setCellValue(u.getPenaltyLevel());

            if (u.getBookingLockedUntil() != null) {
                Cell cell = row.createCell(5);
                cell.setCellValue(u.getBookingLockedUntil());
                cell.setCellStyle(dateStyle);
            }

            if (u.getUpdatedAt() != null) {
                Cell cell = row.createCell(6);
                cell.setCellValue(u.getUpdatedAt());
                cell.setCellStyle(dateStyle);
            }
        }
    }

    private void exportBookingLockHistorySheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("BOOKING_LOCK_HISTORY");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("LockHistoryID");
        header.createCell(1).setCellValue("UserID");
        header.createCell(2).setCellValue("TriggerBookingID");
        header.createCell(3).setCellValue("PenaltyLevel");
        header.createCell(4).setCellValue("LockedAt");
        header.createCell(5).setCellValue("LockedUntil");
        header.createCell(6).setCellValue("DurationMinutes");

        CellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

        List<BookingLockHistory> list = new DAOFramework<>(BookingLockHistory.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (BookingLockHistory h : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(h.getLockHistoryID() != null ? h.getLockHistoryID() : 0L);
            if (h.getUserID() != null) {
                row.createCell(1).setCellValue(h.getUserID().getUserID());
            }
            if (h.getTriggerBookingID() != null) {
                row.createCell(2).setCellValue(h.getTriggerBookingID().getBookingID());
            }
            row.createCell(3).setCellValue(h.getPenaltyLevel());
            if (h.getLockedAt() != null) {
                Cell cell = row.createCell(4);
                cell.setCellValue(h.getLockedAt());
                cell.setCellStyle(dateStyle);
            }
            if (h.getLockedUntil() != null) {
                Cell cell = row.createCell(5);
                cell.setCellValue(h.getLockedUntil());
                cell.setCellStyle(dateStyle);
            }
            row.createCell(6).setCellValue(h.getDurationMinutes());
        }
    }

    private void exportInvoiceTransactionSheet(Workbook wb, EntityManager em) {
        Sheet sheet = wb.createSheet("INVOICE_TRANSACTION");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("InvoiceID");
        header.createCell(1).setCellValue("BookingID");
        header.createCell(2).setCellValue("PaymentID");
        header.createCell(3).setCellValue("TransactionID");

        List<InvoiceTransaction> list = new DAOFramework<>(InvoiceTransaction.class).getAllBypassCache(em);
        int rowIdx = 1;
        for (InvoiceTransaction it : list) {
            Row row = sheet.createRow(rowIdx++);
            if (it.getInvoiceTransactionPK() != null) {
                row.createCell(0).setCellValue(it.getInvoiceTransactionPK().getInvoiceID());
                row.createCell(3).setCellValue(it.getInvoiceTransactionPK().getTransactionID());
            }
            if (it.getInvoice() != null && it.getInvoice().getBookingID() != null) {
                row.createCell(1).setCellValue(it.getInvoice().getBookingID().getBookingID());
            }
            if (it.getPayment() != null) {
                row.createCell(2).setCellValue(it.getPayment().getPaymentID());
            }
        }
    }

}
