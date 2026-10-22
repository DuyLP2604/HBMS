package service;

import dao.*;
import entity.*;
import jakarta.persistence.EntityManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class WorkSheetService {

    // ==========================================
    // 1. CHỨC NĂNG EXPORT (SAO LƯU DB RA EXCEL)
    // ==========================================
    public void exportDatabase(OutputStream out) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {

            exportHotelSheet(workbook);
            exportNationalitySheet(workbook);
            exportRoomTypeSheet(workbook);
            exportServiceSheet(workbook);
            exportPaymentMethodSheet(workbook);
            exportUsersSheet(workbook);
            exportEmployeeSheet(workbook);
            exportCustomerSheet(workbook);
            exportRoomSheet(workbook);
            exportComplaintSheet(workbook);
            // Giao dịch
            exportBookingSheet(workbook);
            exportBookingDetailSheet(workbook);
            exportBookingServiceSheet(workbook);
            exportRoomAssignmentSheet(workbook);
            exportPaymentSheet(workbook);
            exportInvoiceSheet(workbook);

            workbook.write(out);
        }
    }

    private void exportHotelSheet(Workbook wb) {
        Sheet sheet = wb.createSheet("HOTEL");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("HotelName");
        header.createCell(1).setCellValue("HotelImage");
        header.createCell(2).setCellValue("RoomImage");
        header.createCell(3).setCellValue("PoolImage");
        header.createCell(4).setCellValue("RestaurantImage");
        header.createCell(5).setCellValue("Address");

        Hotel h = new HotelDAO().getHotel();
        int rowIdx = 1;
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(h.getHotelName() != null ? h.getHotelName() : "");
        row.createCell(1).setCellValue(h.getHotelImage() != null ? h.getHotelImage() : "");
        row.createCell(2).setCellValue(h.getRoomImage() != null ? h.getRoomImage() : "");
        row.createCell(3).setCellValue(h.getPoolImage() != null ? h.getPoolImage() : "");
        row.createCell(4).setCellValue(h.getRestaurantImage() != null ? h.getRestaurantImage() : "");
        row.createCell(5).setCellValue(h.getAddress() != null ? h.getAddress() : "");

    }

    private void exportNationalitySheet(Workbook wb) {
        Sheet sheet = wb.createSheet("NATIONALITY");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("NationalityID");
        header.createCell(1).setCellValue("NationalityName");

        List<Nationality> list = new NationalityDAO().getAll();
        int rowIdx = 1;
        for (Nationality n : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(n.getNationalityID() != null ? n.getNationalityID() : "");
            row.createCell(1).setCellValue(n.getNationalityName() != null ? n.getNationalityName() : "");
        }
    }

    private void exportRoomTypeSheet(Workbook wb) {
        Sheet sheet = wb.createSheet("ROOM_TYPE");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("RoomTypeID");
        header.createCell(1).setCellValue("TypeName");
        header.createCell(2).setCellValue("Capacity");
        header.createCell(3).setCellValue("Price");
        header.createCell(4).setCellValue("RoomTypeImage");
        header.createCell(5).setCellValue("Description");

        List<RoomType> list = new RoomTypeDAO().getAll();
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

    private void exportServiceSheet(Workbook wb) {
        Sheet sheet = wb.createSheet("SERVICE");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ServiceID");
        header.createCell(1).setCellValue("ServiceName");
        header.createCell(2).setCellValue("UnitPrice");

        List<Service> list = new ServiceDAO().getAllServices();
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

    private void exportPaymentMethodSheet(Workbook wb) {
        Sheet sheet = wb.createSheet("PAYMENTMETHOD");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("MethodID");
        header.createCell(1).setCellValue("MethodName");

        List<Paymentmethod> list = new PaymentmethodDAO().getAll();
        int rowIdx = 1;
        for (Paymentmethod pm : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(pm.getMethodID() != null ? pm.getMethodID() : "");
            row.createCell(1).setCellValue(pm.getMethodName() != null ? pm.getMethodName() : "");
        }
    }

    private void exportUsersSheet(Workbook wb) {
        Sheet sheet = wb.createSheet("USERS");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("UserID");
        header.createCell(1).setCellValue("Username");
        header.createCell(2).setCellValue("Password");
        header.createCell(3).setCellValue("Role");

        List<Users> list = new DAOFramework<>(Users.class).getAll();
        int rowIdx = 1;
        for (Users u : list) {
            Row row = sheet.createRow(rowIdx++);
            if (u.getUserID() != null) {
                row.createCell(0).setCellValue(u.getUserID());
            }
            row.createCell(1).setCellValue(u.getUsername() != null ? u.getUsername() : "");
            row.createCell(2).setCellValue(u.getPassword() != null ? u.getPassword() : "");
            row.createCell(3).setCellValue(u.getRole() != null ? u.getRole() : "");
        }
    }

    private void exportEmployeeSheet(Workbook wb) {
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

        List<Employee> list = new EmployeeDAO().getAllEmployees();
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

    private void exportCustomerSheet(Workbook wb) {
        Sheet sheet = wb.createSheet("CUSTOMER");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("CustomerID");
        header.createCell(1).setCellValue("FullName");
        header.createCell(2).setCellValue("Phone");
        header.createCell(3).setCellValue("Email");
        header.createCell(4).setCellValue("Address");
        header.createCell(5).setCellValue("NationalityID");
        header.createCell(6).setCellValue("UserID");

        List<Customer> list = new CustomerDAO().getAllCustomers();
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

    private void exportRoomSheet(Workbook wb) {
        Sheet sheet = wb.createSheet("ROOM");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("RoomID");
        header.createCell(1).setCellValue("RoomNumber");
        header.createCell(2).setCellValue("RoomImage");
        header.createCell(3).setCellValue("Status");
        header.createCell(4).setCellValue("RoomTypeID");

        List<Room> list = new RoomDAO().getAll();
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

    private void exportComplaintSheet(Workbook wb) {
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

        List<Complaint> list = new ComplaintDAO().getAll();
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
        }
    }

    private void exportBookingSheet(Workbook wb) {
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

        List<Booking> list = new BookingDAO().getAll();
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
            if (b.getRefundDeadline() != null) {
                Cell cell = row.createCell(14);
                cell.setCellValue(b.getRefundDeadline());
                cell.setCellStyle(dateStyle);
            }
        }
    }

    private void exportBookingDetailSheet(Workbook wb) {
        Sheet sheet = wb.createSheet("BOOKING_DETAIL");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("BookingDetailID");
        header.createCell(1).setCellValue("BookingID");
        header.createCell(2).setCellValue("RoomTypeID");
        header.createCell(3).setCellValue("Quantity");
        header.createCell(4).setCellValue("GuestCount");
        header.createCell(5).setCellValue("UnitPrice");
        header.createCell(6).setCellValue("Subtotal");

        List<BookingDetail> list = new DAOFramework<>(BookingDetail.class).getAll();
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

    private void exportBookingServiceSheet(Workbook wb) {
        Sheet sheet = wb.createSheet("BOOKING_SERVICE");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("BookingID");
        header.createCell(1).setCellValue("ServiceID");
        header.createCell(2).setCellValue("Quantity");
        header.createCell(3).setCellValue("UnitPrice");
        header.createCell(4).setCellValue("Subtotal");

        List<BookingService> list = new DAOFramework<>(BookingService.class).getAll();
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

    private void exportRoomAssignmentSheet(Workbook wb) {
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

        List<RoomAssignment> list = new DAOFramework<>(RoomAssignment.class).getAll();
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

    private void exportPaymentSheet(Workbook wb) {
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

        List<Payment> list = new DAOFramework<>(Payment.class).getAll();
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
        }
    }

    private void exportInvoiceSheet(Workbook wb) {
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

        List<Invoice> list = new DAOFramework<>(Invoice.class).getAll();
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

    // ==========================================
    // 2. CHỨC NĂNG IMPORT (PHỤC HỒI DB TỪ EXCEL)
    // ==========================================
    public void importDatabase(InputStream in, EntityManager em) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(in)) {
            // LƯU Ý QUAN TRỌNG: Phải import theo đúng thứ tự để không vi phạm khóa ngoại (Foreign Key).
            // Các bảng Master Data trước.
            importHotelSheet(workbook.getSheet("HOTEL"), em);
            importNationalitySheet(workbook.getSheet("NATIONALITY"), em);
            importRoomTypeSheet(workbook.getSheet("ROOM_TYPE"), em);
            importServiceSheet(workbook.getSheet("SERVICE"), em);
            importPaymentMethodSheet(workbook.getSheet("PAYMENTMETHOD"), em);

            // Tài khoản
            importUsersSheet(workbook.getSheet("USERS"), em);
            importEmployeeSheet(workbook.getSheet("EMPLOYEE"), em);
            importCustomerSheet(workbook.getSheet("CUSTOMER"), em);

            // Dữ liệu độc lập
            importRoomSheet(workbook.getSheet("ROOM"), em);

            // Giao dịch chính
            importBookingSheet(workbook.getSheet("BOOKING"), em);
            importBookingDetailSheet(workbook.getSheet("BOOKING_DETAIL"), em);
            importBookingServiceSheet(workbook.getSheet("BOOKING_SERVICE"), em);
            importRoomAssignmentSheet(workbook.getSheet("ROOM_ASSIGNMENT"), em);

            // Giao dịch phụ
            importPaymentSheet(workbook.getSheet("PAYMENT"), em);
            importInvoiceSheet(workbook.getSheet("INVOICE"), em);
            importComplaintSheet(workbook.getSheet("COMPLAINT"), em);
        }
    }

    private void importHotelSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            Hotel h = new Hotel();
            h.setHotelName(getCellValue(row.getCell(0)));
            h.setHotelImage(getCellValue(row.getCell(1)));
            h.setRoomImage(getCellValue(row.getCell(2)));
            h.setPoolImage(getCellValue(row.getCell(3)));
            h.setRestaurantImage(getCellValue(row.getCell(4)));
            h.setAddress(getCellValue(row.getCell(5)));

            em.merge(h);
        }
    }

    private void importNationalitySheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            Nationality n = new Nationality();
            n.setNationalityID(getCellValue(row.getCell(0)));
            n.setNationalityName(getCellValue(row.getCell(1)));

            em.merge(n);
        }
    }

    private void importRoomTypeSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            RoomType rt = new RoomType();
            rt.setRoomTypeID(getCellValue(row.getCell(0)));
            rt.setTypeName(getCellValue(row.getCell(1)));

            String cap = getCellValue(row.getCell(2));
            if (!cap.isEmpty()) {
                rt.setCapacity((int) Double.parseDouble(cap));
            }

            String price = getCellValue(row.getCell(3));
            if (!price.isEmpty()) {
                rt.setPrice(new BigDecimal(price));
            }

            rt.setRoomTypeImage(getCellValue(row.getCell(4)));
            rt.setDescription(getCellValue(row.getCell(5)));

            em.merge(rt);
        }
    }

    private void importServiceSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            Service s = new Service();
            s.setServiceID(getCellValue(row.getCell(0)));
            s.setServiceName(getCellValue(row.getCell(1)));

            String price = getCellValue(row.getCell(2));
            if (!price.isEmpty()) {
                s.setUnitPrice(new BigDecimal(price));
            }

            em.merge(s);
        }
    }

    private void importPaymentMethodSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            Paymentmethod p = new Paymentmethod();
            p.setMethodID(getCellValue(row.getCell(0)));
            p.setMethodName(getCellValue(row.getCell(1)));

            em.merge(p);
        }
    }

    private void importUsersSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            Users u = new Users();
            String idStr = getCellValue(row.getCell(0));
            if (!idStr.isEmpty()) {
                u.setUserID((int) Double.parseDouble(idStr));
            }
            u.setUsername(getCellValue(row.getCell(1)));
            u.setPassword(getCellValue(row.getCell(2)));
            u.setRole(getCellValue(row.getCell(3)));

            em.merge(u);
        }
    }

    private void importEmployeeSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            Employee e = new Employee();
            e.setEmployeeID(getCellValue(row.getCell(0)));
            e.setFullName(getCellValue(row.getCell(1)));
            e.setPosition(getCellValue(row.getCell(2)));

            String sal = getCellValue(row.getCell(3));
            if (!sal.isEmpty()) {
                e.setSalary(new BigDecimal(sal));
            }

            e.setShift(getCellValue(row.getCell(4)));
            e.setAddress(getCellValue(row.getCell(5)));
            e.setPhone(getCellValue(row.getCell(6)));
            e.setEmail(getCellValue(row.getCell(7)));

            String uId = getCellValue(row.getCell(8));
            if (!uId.isEmpty()) {
                Users u = em.find(Users.class, (int) Double.parseDouble(uId));
                e.setUserID(u);
            }

            em.merge(e);
        }
    }

    private void importCustomerSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            Customer c = new Customer();
            c.setCustomerID(getCellValue(row.getCell(0)));
            c.setFullName(getCellValue(row.getCell(1)));
            c.setPhone(getCellValue(row.getCell(2)));
            c.setEmail(getCellValue(row.getCell(3)));
            c.setAddress(getCellValue(row.getCell(4)));

            String natId = getCellValue(row.getCell(5));
            if (!natId.isEmpty()) {
                Nationality n = em.find(Nationality.class, natId);
                c.setNationalityID(n);
            }

            String uId = getCellValue(row.getCell(6));
            if (!uId.isEmpty()) {
                Users u = em.find(Users.class, (int) Double.parseDouble(uId));
                c.setUserID(u);
            }

            em.merge(c);
        }
    }

    private void importRoomSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            Room r = new Room();
            r.setRoomID(getCellValue(row.getCell(0)));
            r.setRoomNumber(getCellValue(row.getCell(1)));
            r.setRoomImage(getCellValue(row.getCell(2)));
            r.setStatus(getCellValue(row.getCell(3)));

            String rtId = getCellValue(row.getCell(4));
            if (!rtId.isEmpty()) {
                RoomType rt = em.find(RoomType.class, rtId);
                r.setRoomTypeID(rt);
            }

            em.merge(r);
        }
    }

    private void importBookingSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            Booking b = new Booking();
            b.setBookingID(getCellValue(row.getCell(0)));
            b.setPaymentOption(getCellValue(row.getCell(1)));

            if (row.getCell(7) != null && row.getCell(7).getCellType() != CellType.BLANK) {
                b.setBookingDate(row.getCell(7).getDateCellValue());
            }
            if (row.getCell(8) != null && row.getCell(8).getCellType() != CellType.BLANK) {
                b.setPaymentDeadline(row.getCell(8).getDateCellValue());
            }
            if (row.getCell(9) != null && row.getCell(9).getCellType() != CellType.BLANK) {
                b.setCheckInDate(row.getCell(9).getDateCellValue());
            }
            if (row.getCell(10) != null && row.getCell(10).getCellType() != CellType.BLANK) {
                b.setCheckOutDate(row.getCell(10).getDateCellValue());
            }

            b.setBookingStatus(getCellValue(row.getCell(11)));

            String cusId = getCellValue(row.getCell(12));
            if (!cusId.isEmpty()) {
                Customer c = em.find(Customer.class, cusId);
                b.setCustomerID(c);
            }

            String total = getCellValue(row.getCell(13));
            if (!total.isEmpty()) {
                b.setTotalAmount(new BigDecimal(total));
            }

            em.merge(b);
        }
    }

    private void importBookingDetailSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            // 1. Đọc BookingID và RoomTypeID để kiểm tra trùng lặp theo Unique Key
            String bId = getCellValue(row.getCell(1));
            String rtId = getCellValue(row.getCell(2));

            if (bId.isEmpty() || rtId.isEmpty()) {
                continue;
            }

            Booking b = em.find(Booking.class, bId);
            RoomType rt = em.find(RoomType.class, rtId);

            if (b == null || rt == null) {
                continue;
            }

            // Truy vấn xem BookingDetail đã tồn tại theo cặp (Booking, RoomType) này chưa
            List<BookingDetail> existings = em.createQuery(
                    "SELECT bd FROM BookingDetail bd WHERE bd.bookingID = :b AND bd.roomTypeID = :rt", BookingDetail.class)
                    .setParameter("b", b)
                    .setParameter("rt", rt)
                    .getResultList();

            BookingDetail bd;
            if (!existings.isEmpty()) {
                // Đã tồn tại -> Lấy ra để cập nhật (Update)
                bd = existings.get(0);
            } else {
                // Chưa tồn tại -> Tạo mới (Insert)
                bd = new BookingDetail();
                bd.setBookingID(b);
                bd.setRoomTypeID(rt);

                // Chỉ đọc BookingDetailID nếu là bản ghi mới
                String idStr = getCellValue(row.getCell(0));
                if (!idStr.isEmpty()) {
                    try {
                        bd.setBookingDetailID((int) Double.parseDouble(idStr));
                    } catch (Exception ignored) {
                    }
                }
            }

            // 2. Cập nhật các thông số còn lại
            String q = getCellValue(row.getCell(3));
            if (!q.isEmpty()) {
                try {
                    bd.setQuantity((int) Double.parseDouble(q));
                } catch (Exception ignored) {
                }
            }

            String g = getCellValue(row.getCell(4));
            if (!g.isEmpty()) {
                try {
                    bd.setGuestCount((int) Double.parseDouble(g));
                } catch (Exception ignored) {
                }
            }

            String up = getCellValue(row.getCell(5));
            if (!up.isEmpty()) {
                try {
                    bd.setUnitPrice(new BigDecimal(up));
                } catch (Exception ignored) {
                }
            }

            String sub = getCellValue(row.getCell(6));
            if (!sub.isEmpty()) {
                try {
                    bd.setSubtotal(new BigDecimal(sub));
                } catch (Exception ignored) {
                }
            }

            em.merge(bd);
        }
    }

    private void importBookingServiceSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            BookingService bs = new BookingService();
            BookingServicePK pk = new BookingServicePK();
            pk.setBookingID(getCellValue(row.getCell(0)));
            pk.setServiceID(getCellValue(row.getCell(1)));
            bs.setBookingServicePK(pk);

            String q = getCellValue(row.getCell(2));
            if (!q.isEmpty()) {
                bs.setQuantity((int) Double.parseDouble(q));
            }

            String up = getCellValue(row.getCell(3));
            if (!up.isEmpty()) {
                bs.setUnitPrice(new BigDecimal(up));
            }

            String sub = getCellValue(row.getCell(4));
            if (!sub.isEmpty()) {
                bs.setSubtotal(new BigDecimal(sub));
            }

            em.merge(bs);
        }
    }

    private void importRoomAssignmentSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            String bdIdStr = getCellValue(row.getCell(1)); // Cột 1: BookingDetailID
            String rIdStr = getCellValue(row.getCell(2));  // Cột 2: RoomID

            // Bỏ qua nếu thiếu RoomID hoặc BookingDetailID
            if (bdIdStr.isEmpty() || rIdStr.isEmpty()) {
                continue;
            }

            BookingDetail bd = null;
            try {
                int bdId = (int) Double.parseDouble(bdIdStr);
                bd = em.find(BookingDetail.class, bdId);
            } catch (Exception e) {
                bd = null;
            }

            // Nếu không tìm thấy BookingDetail tương ứng, bỏ qua dòng này để không bắn lỗi NULL vào DB
            if (bd == null) {
                continue;
            }

            Room r = em.find(Room.class, rIdStr);
            if (r == null) {
                continue;
            }

            // Kiểm tra xem RoomAssignment đã tồn tại theo cặp (BookingDetailID, RoomID) chưa
            List<RoomAssignment> existings = em.createQuery(
                    "SELECT ra FROM RoomAssignment ra WHERE ra.bookingDetailID = :bd AND ra.roomID = :room", RoomAssignment.class)
                    .setParameter("bd", bd)
                    .setParameter("room", r)
                    .getResultList();

            RoomAssignment ra;
            if (!existings.isEmpty()) {
                ra = existings.get(0); // Đã có -> Cập nhật (Update)
            } else {
                ra = new RoomAssignment(); // Chưa có -> Tạo mới (Insert)
                ra.setBookingDetailID(bd);
                ra.setRoomID(r);

                String idStr = getCellValue(row.getCell(0)); // Cột 0: AssignmentID
                if (!idStr.isEmpty()) {
                    try {
                        ra.setAssignmentID((int) Double.parseDouble(idStr));
                    } catch (Exception ignored) {
                    }
                }
            }

            // Cập nhật EmployeeID và AssignedAt
            String eId = getCellValue(row.getCell(3));
            if (!eId.isEmpty()) {
                Employee e = em.find(Employee.class, eId);
                if (e != null) {
                    ra.setEmployeeID(e);
                }
            }

            if (row.getCell(4) != null && row.getCell(4).getCellType() != CellType.BLANK) {
                try {
                    ra.setAssignedAt(row.getCell(4).getDateCellValue());
                } catch (Exception ignored) {
                }
            }

            em.merge(ra);
        }
    }

    private void importPaymentSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            String paymentId = getCellValue(row.getCell(0));
            if (paymentId.isEmpty()) {
                continue;
            }

            // Tìm xem Payment đã tồn tại trong DB chưa
            Payment p = em.find(Payment.class, paymentId);
            if (p == null) {
                p = new Payment();
                p.setPaymentID(paymentId);
            }

            // BookingID (Cell 1)
            String bId = getCellValue(row.getCell(1));
            if (!bId.isEmpty()) {
                Booking b = em.find(Booking.class, bId);
                p.setBookingID(b);
            } else {
                p.setBookingID(null);
            }

            // PaymentType (Cell 2)
            p.setPaymentType(getCellValue(row.getCell(2)));

            // PaymentTime (Cell 3)
            if (row.getCell(3) != null && row.getCell(3).getCellType() != CellType.BLANK) {
                try {
                    p.setPaymentTime(row.getCell(3).getDateCellValue());
                } catch (Exception ignored) {
                }
            }

            // Amount (Cell 4)
            String amt = getCellValue(row.getCell(4));
            if (!amt.isEmpty()) {
                try {
                    p.setAmount(new BigDecimal(amt));
                } catch (Exception ignored) {
                }
            }

            // WalletAmount (Cell 5)
            String walletAmt = getCellValue(row.getCell(5));
            if (!walletAmt.isEmpty()) {
                try {
                    p.setWalletAmount(new BigDecimal(walletAmt));
                } catch (Exception ignored) {
                }
            } else {
                p.setWalletAmount(BigDecimal.ZERO);
            }

            // Status (Cell 6)
            p.setStatus(getCellValue(row.getCell(6)));

            // MethodID (Cell 7)
            String mId = getCellValue(row.getCell(7));
            if (!mId.isEmpty()) {
                Paymentmethod pm = em.find(Paymentmethod.class, mId);
                p.setMethodID(pm);
            } else {
                p.setMethodID(null);
            }

            // TransactionCode (Cell 8) - Xử lý chống trùng lặp khi giá trị rỗng
            String transCode = getCellValue(row.getCell(8));
            if (!transCode.isEmpty()) {
                p.setTransactionCode(transCode);
            } else {
                p.setTransactionCode(null); // Gán null để tránh lỗi trùng lặp Unique Index với các dòng trống khác
            }

            em.merge(p);
        }
    }

    private void importInvoiceSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            Invoice inv = new Invoice();
            inv.setInvoiceID(getCellValue(row.getCell(0)));

            if (row.getCell(1) != null && row.getCell(1).getCellType() != CellType.BLANK) {
                inv.setInvoiceDate(row.getCell(1).getDateCellValue());
            }

            String t = getCellValue(row.getCell(2));
            if (!t.isEmpty()) {
                inv.setTotalAmount(new BigDecimal(t));
            }

            inv.setStatus(getCellValue(row.getCell(3)));

            String rId = getCellValue(row.getCell(4));
            if (!rId.isEmpty()) {
                Invoice rInv = em.find(Invoice.class, rId);
                inv.setReplacedInvoiceID(rInv);
            }

            String eId = getCellValue(row.getCell(5));
            if (!eId.isEmpty()) {
                Employee e = em.find(Employee.class, eId);
                inv.setEmployeeID(e);
            }

            String bId = getCellValue(row.getCell(6));
            if (!bId.isEmpty()) {
                Booking b = em.find(Booking.class, bId);
                inv.setBookingID(b);
            }

            em.merge(inv);
        }
    }

    private void importComplaintSheet(Sheet sheet, EntityManager em) {
        if (sheet == null) {
            return;
        }
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            Complaint c = new Complaint();
            String idStr = getCellValue(row.getCell(0));
            if (!idStr.isEmpty()) {
                c.setComplaintID((int) Double.parseDouble(idStr));
            }

            c.setTitle(getCellValue(row.getCell(1)));
            c.setContent(getCellValue(row.getCell(2)));

            if (row.getCell(3) != null && row.getCell(3).getCellType() != CellType.BLANK) {
                c.setCreatedAt(row.getCell(3).getDateCellValue());
            }

            c.setStatus(getCellValue(row.getCell(4)));

            String cusId = getCellValue(row.getCell(5));
            if (!cusId.isEmpty()) {
                Customer cus = em.find(Customer.class, cusId);
                c.setCustomerID(cus);
            }

            c.setReplyMessage(getCellValue(row.getCell(6)));

            em.merge(c);
        }
    }

    // Hàm tiện ích đọc Cell dưới dạng String an toàn
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // Tránh lỗi hiển thị số nguyên thành dạng .0
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
