package dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InvoiceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String bookingID;
    private String customerName;
    private String roomNumber;
    private String checkInDate;
    private String checkOutDate;
    private String paymentStatus;
    private int daysOfNight;
    private String usedServices;
    private BigDecimal roomCharges;
    private BigDecimal serviceCharges;
    private BigDecimal grandTotal;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;

    public InvoiceDTO() {
    }

    public InvoiceDTO(Map<String, String> roomDetail, List<Map<String, String>> services, Map<String, Object> summary) {
        setBookingID(roomDetail.get("bookingID"));
        setCustomerName(roomDetail.get("customerName"));
        setRoomNumber(roomDetail.get("roomNumber"));
        setCheckInDate(roomDetail.get("checkInDate"));
        setCheckOutDate(roomDetail.get("checkOutDate"));
        setPaymentStatus((String) summary.get("paymentStatus"));

        // Tính toán số đêm (Days of night)
        try {
            setDaysOfNight(Integer.parseInt(roomDetail.get("totalDays")));
        } catch (Exception e) {
            setDaysOfNight(0);
        }

        // Gộp danh sách dịch vụ đã dùng
        if (services != null && !services.isEmpty()) {
            setUsedServices(services.stream()
                    .map(s -> s.get("serviceName") + " (x" + s.get("quantity") + ")")
                    .collect(Collectors.joining(", ")));
        } else {
            setUsedServices("Không có dịch vụ");
        }

        // Ép kiểu và tính toán tiền bạc
        BigDecimal gTotal = getBigDecimal(summary.get("totalAmount"));
        BigDecimal pAmount = getBigDecimal(summary.get("totalPaidAmount"));
        BigDecimal rAmount = getBigDecimal(summary.get("remainingAmount"));

        BigDecimal sCharges = BigDecimal.ZERO;
        if (services != null) {
            for (Map<String, String> service : services) {
                BigDecimal subtotal = new BigDecimal(service.getOrDefault("subtotal", "0"));
                sCharges = sCharges.add(subtotal);
            }
        }
        
        setGrandTotal(gTotal);
        setPaidAmount(pAmount);
        setRemainingAmount(rAmount);
        setServiceCharges(sCharges);
        setRoomCharges(gTotal.subtract(sCharges));
    }

    private BigDecimal getBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value != null) {
            return new BigDecimal(value.toString());
        }
        return BigDecimal.ZERO;
    }

    public String getBookingID() {
        return bookingID;
    }

    public void setBookingID(String bookingID) {
        if (bookingID == null || bookingID.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã booking không được để trống.");
        }
        this.bookingID = bookingID.trim();
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khách hàng không được để trống.");
        }
        this.customerName = customerName.trim();
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = (roomNumber == null || roomNumber.trim().isEmpty()) ? "Chưa xếp phòng" : roomNumber.trim();
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(String checkInDate) {
        if (checkInDate == null || checkInDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Ngày nhận phòng không được để trống.");
        }
        this.checkInDate = checkInDate.trim();
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        if (checkOutDate == null || checkOutDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Ngày trả phòng không được để trống.");
        }
        this.checkOutDate = checkOutDate.trim();
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        if (paymentStatus == null || paymentStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái thanh toán không được để trống.");
        }
        this.paymentStatus = paymentStatus.trim();
    }

    public int getDaysOfNight() {
        return daysOfNight;
    }

    public void setDaysOfNight(int daysOfNight) {
        if (daysOfNight < 0) {
            throw new IllegalArgumentException("Số đêm lưu trú không được là số âm.");
        }
        this.daysOfNight = daysOfNight;
    }

    public String getUsedServices() {
        return usedServices;
    }

    public void setUsedServices(String usedServices) {
        this.usedServices = (usedServices == null || usedServices.trim().isEmpty()) ? "Không có dịch vụ" : usedServices.trim();
    }

    public BigDecimal getRoomCharges() {
        return roomCharges;
    }

    public void setRoomCharges(BigDecimal roomCharges) {
        if (roomCharges == null || roomCharges.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tiền phòng không được là số âm.");
        }
        this.roomCharges = roomCharges;
    }

    public BigDecimal getServiceCharges() {
        return serviceCharges;
    }

    public void setServiceCharges(BigDecimal serviceCharges) {
        if (serviceCharges == null || serviceCharges.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tiền dịch vụ không được là số âm.");
        }
        this.serviceCharges = serviceCharges;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        if (grandTotal == null || grandTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tổng tiền không được là số âm.");
        }
        this.grandTotal = grandTotal;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số tiền đã thanh toán không được là số âm.");
        }
        this.paidAmount = paidAmount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        if (remainingAmount == null || remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số tiền còn lại không được là số âm.");
        }
        this.remainingAmount = remainingAmount;
    }

    @Override
    public String toString() {
        return "InvoiceDTO{" +
                "bookingID='" + bookingID + '\'' +
                ", customerName='" + customerName + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                ", checkInDate='" + checkInDate + '\'' +
                ", checkOutDate='" + checkOutDate + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", daysOfNight=" + daysOfNight +
                ", usedServices='" + usedServices + '\'' +
                ", roomCharges=" + roomCharges +
                ", serviceCharges=" + serviceCharges +
                ", grandTotal=" + grandTotal +
                ", paidAmount=" + paidAmount +
                ", remainingAmount=" + remainingAmount +
                '}';
    }
}