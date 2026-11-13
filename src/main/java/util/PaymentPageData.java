package util;

import entity.Booking;
import entity.Paymentmethod;
import java.util.List;
import java.util.Map;
import service.PaymentRequest;

/** Booking details and payment options shown on the customer payment page. */
public final class PaymentPageData {

    private final Booking booking;
    private final PaymentRequest paymentRequest;
    private final Map<String, Object> summary;
    private final List<Paymentmethod> methods;

    public PaymentPageData(Booking booking, PaymentRequest paymentRequest,
            Map<String, Object> summary, List<Paymentmethod> methods) {
        this.booking = booking;
        this.paymentRequest = paymentRequest;
        this.summary = summary;
        this.methods = methods;
    }

    public Booking getBooking() {
        return booking;
    }

    public PaymentRequest getPaymentRequest() {
        return paymentRequest;
    }

    public Map<String, Object> getSummary() {
        return summary;
    }

    public List<Paymentmethod> getMethods() {
        return methods;
    }
}
