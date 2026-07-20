/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.DBContext;

/**
 *
 * @author Admin
 */
public class InvoiceDAO extends DBContext {

    public List<Map<String, String>> getOccupiedRooms() {
        List<Map<String, String>> list = new ArrayList<>();
        // Đã xóa INNER JOIN SERVICE và cột SERVICE.ServiceName để tránh lặp dữ liệu
        String sql = "SELECT BOOKING.BookingID, CUSTOMER.FullName, HOTEL.HotelName, ROOM.RoomID, ROOM.RoomNumber, "
                   + "Nationality.NationalityName, ROOM.Price, BOOKING.CheckInDate, BOOKING.CheckOutDate "
                   + "FROM BOOKING "
                   + "INNER JOIN CUSTOMER ON BOOKING.CustomerID = CUSTOMER.CustomerID "
                   + "INNER JOIN Nationality ON CUSTOMER.NationalityID = Nationality.NationalityID "
                   + "INNER JOIN ROOM ON BOOKING.RoomID = ROOM.RoomID "
                   + "INNER JOIN HOTEL ON ROOM.HotelID = HOTEL.HotelID "
                   + "WHERE ROOM.Status = N'Occupied' AND BOOKING.BookingStatus != N'Completed'";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, String> room = new HashMap<>();
                
                room.put("bookingId", rs.getString("BookingID"));
                room.put("customerName", rs.getString("FullName"));
                room.put("hotelName", rs.getString("HotelName"));
                room.put("roomId", rs.getString("RoomID"));
                room.put("roomNumber", rs.getString("RoomNumber"));
                room.put("nationality", rs.getString("NationalityName"));
                room.put("price", rs.getString("Price"));
                room.put("checkInDate", rs.getString("CheckInDate"));
                room.put("checkOutDate", rs.getString("CheckOutDate"));
                
                list.add(room);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Map<String, String>> getPaidInvoices() {
        List<Map<String, String>> list = new ArrayList<>();
        
        String sql = "SELECT TOP 10 i.InvoiceID, b.BookingID, c.FullName, h.HotelName, r.RoomNumber, i.TotalAmount, i.InvoiceDate "
                   + "FROM INVOICE i "
                   + "INNER JOIN BOOKING b ON i.BookingID = b.BookingID "
                   + "INNER JOIN CUSTOMER c ON i.CustomerID = c.CustomerID "
                   + "INNER JOIN ROOM r ON b.RoomID = r.RoomID "
                   + "INNER JOIN HOTEL h ON r.HotelID = h.HotelID "
                   + "ORDER BY i.InvoiceDate DESC";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, String> invoice = new HashMap<>();
                invoice.put("invoiceId", rs.getString("InvoiceID"));
                invoice.put("bookingId", rs.getString("BookingID"));
                invoice.put("customerName", rs.getString("FullName"));
                invoice.put("hotelName", rs.getString("HotelName"));
                invoice.put("roomNumber", rs.getString("RoomNumber"));
                invoice.put("totalAmount", rs.getString("TotalAmount"));
                invoice.put("invoiceDate", rs.getString("InvoiceDate"));
                list.add(invoice);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean isBookingPaid(String bookingId) {
        String sql = "SELECT 1 FROM INVOICE WHERE BookingID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return true; // Nếu có kết quả, nghĩa là đã có hóa đơn cho booking này
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // Mặc định trả về false nếu có lỗi hoặc không tìm thấy
    }


    public List<Map<String, String>> getServicesByBooking(String bookingId) {
        List<Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT s.ServiceID, s.ServiceName, s.UnitPrice "
                + "FROM BOOKING_SERVICE bs "
                + "JOIN SERVICE s ON bs.ServiceID = s.ServiceID "
                + "WHERE bs.BookingID = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, bookingId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, String> service = new HashMap<>();
                service.put("serviceId", rs.getString("ServiceID"));
                service.put("serviceName", rs.getString("ServiceName"));
                service.put("unitPrice", rs.getString("UnitPrice"));
                list.add(service);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Map<String, String> getRoomDetailByBooking(String bookingId) {
        Map<String, String> room = null;
        // SQL dùng DATEDIFF để tính số ngày ở (Nếu CheckIn và CheckOut cùng ngày thì tính là 1 ngày)
        // Dùng Subquery để tính tổng tiền dịch vụ (Service Unit Price) đã có sẵn của Booking
        String sql = "SELECT b.BookingID, c.FullName, r.RoomNumber, r.Price, b.CheckInDate, b.CheckOutDate, "
                   + "       CASE "
                   + "           WHEN DATEDIFF(day, b.CheckInDate, b.CheckOutDate) = 0 THEN 1 "
                   + "           ELSE DATEDIFF(day, b.CheckInDate, b.CheckOutDate) "
                   + "       END AS TotalDays, "
                   + "       ISNULL((SELECT SUM(s.UnitPrice) "
                   + "               FROM BOOKING_SERVICE bs "
                   + "               JOIN SERVICE s ON bs.ServiceID = s.ServiceID "
                   + "               WHERE bs.BookingID = b.BookingID), 0) AS ServiceTotal "
                   + "FROM BOOKING b "
                   + "JOIN CUSTOMER c ON b.CustomerID = c.CustomerID "
                   + "JOIN ROOM r ON b.RoomID = r.RoomID "
                   + "WHERE b.BookingID = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, bookingId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                room = new HashMap<>();
                room.put("bookingId", rs.getString("BookingID"));
                room.put("customerName", rs.getString("FullName"));
                room.put("roomNumber", rs.getString("RoomNumber"));
                
                // Lấy các giá trị ra để tính toán
                double price = rs.getDouble("Price");
                int totalDays = rs.getInt("TotalDays");
                double serviceTotal = rs.getDouble("ServiceTotal");
                
                double roomTotal = price * totalDays;
                
                room.put("price", String.valueOf(price));
                room.put("checkInDate", rs.getString("CheckInDate"));
                room.put("checkOutDate", rs.getString("CheckOutDate"));
                room.put("totalDays", String.valueOf(totalDays));
                room.put("roomTotal", String.valueOf(roomTotal));
                room.put("serviceTotal", String.valueOf(serviceTotal));
                
                // Tính Base Total: (Giá phòng * Số ngày) + Dịch vụ đã đặt
                double baseTotal = roomTotal + serviceTotal;
                room.put("baseTotal", String.valueOf(baseTotal)); 
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return room;
    }

    public String getEmployeeById(int userId) {
        String employeeId = null;
        String sql = "SELECT EmployeeID FROM EMPLOYEE WHERE UserID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                employeeId = rs.getString("EmployeeID");
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return employeeId;
    }

    public boolean processCheckout(String bookingId, double totalAmount, String employeeId) {
        try {
            conn.setAutoCommit(false); 

            String getInfoSql = "SELECT HotelID, CustomerID, RoomID FROM BOOKING b JOIN ROOM r ON b.RoomID = r.RoomID WHERE BookingID = ?";
            String hotelId, customerId, roomId;
            try (PreparedStatement psInfo = conn.prepareStatement(getInfoSql)) {
                psInfo.setString(1, bookingId);
                try (ResultSet rsInfo = psInfo.executeQuery()) {
                    if (!rsInfo.next()) {
                        return false;
                    }
                    hotelId = rsInfo.getString("HotelID");
                    customerId = rsInfo.getString("CustomerID");
                    roomId = rsInfo.getString("RoomID");
                }
            }

            String getNewInvoiceIdSql = "SELECT 'HD' + RIGHT('00' + CAST(ISNULL(MAX(CAST(SUBSTRING(InvoiceID, 3, LEN(InvoiceID)) AS INT)), 0) + 1 AS VARCHAR), 2) AS NewID FROM INVOICE";
            String newInvoiceId;
            try (PreparedStatement psNewId = conn.prepareStatement(getNewInvoiceIdSql);
                 ResultSet rsNewId = psNewId.executeQuery()) {
                rsNewId.next();
                newInvoiceId = rsNewId.getString("NewID");
            }

            
            String insertInvoiceSql = "INSERT INTO INVOICE (InvoiceID, InvoiceDate, TotalAmount, HotelID, CustomerID, EmployeeID, BookingID) "
                    + "VALUES (?, GETDATE(), ?, ?, ?, ?, ?)";
            try (PreparedStatement psInvoice = conn.prepareStatement(insertInvoiceSql)) {
                psInvoice.setString(1, newInvoiceId);
                psInvoice.setDouble(2, totalAmount);
                psInvoice.setString(3, hotelId);
                psInvoice.setString(4, customerId);
                psInvoice.setString(5, employeeId);
                psInvoice.setString(6, bookingId);
                psInvoice.executeUpdate();
            }

            // 4. Cập nhật Booking (Hoàn thành & Cập nhật ngày CheckOut thực tế)
            String updateBookingSql = "UPDATE BOOKING SET BookingStatus = N'Completed', CheckOutDate = GETDATE() WHERE BookingID = ?";
            try (PreparedStatement psBooking = conn.prepareStatement(updateBookingSql)) {
                psBooking.setString(1, bookingId);
                psBooking.executeUpdate();
            }

            // 5. Cập nhật trạng thái Phòng (Trống)
            String updateRoomSql = "UPDATE ROOM SET Status = N'Available' WHERE RoomID = ?";
            try (PreparedStatement psRoom = conn.prepareStatement(updateRoomSql)) {
                psRoom.setString(1, roomId);
                psRoom.executeUpdate();
            }

            // Xác nhận lưu toàn bộ thay đổi
            conn.commit(); 
            return true;

        } catch (Exception e) {
            try {
                conn.rollback(); // Hoàn tác nếu có bất kỳ lỗi nào
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                conn.setAutoCommit(true); // Trả lại trạng thái auto-commit mặc định
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
