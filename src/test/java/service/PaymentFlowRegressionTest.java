package service;

import entity.*;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.net.URI;
import java.sql.*;
import java.util.*;
import service.PaymentService.PaymentRequest;

/** Regression checks for amounts, actor permissions, v6 bindings and atomic failures. */
public class PaymentFlowRegressionTest {
    static int checks;
    static void expect(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        checks++;
    }
    interface Action { void run() throws Exception; }
    static void rejects(Action action, String label) throws Exception {
        try { action.run(); } catch (IllegalArgumentException | IllegalStateException | SecurityException expected) { checks++; return; }
        throw new AssertionError(label);
    }
    static Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        return null;
    }
    static final class Database {
        int commits, rollbacks, opens;
        boolean lateFailure;
        Map<Integer,Object> payment = new HashMap<>();
        String paymentSql = "";
        Connection connection() {
            opens++;
            return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class}, (o,m,a) -> {
                if (m.getName().equals("prepareStatement")) return statement((String)a[0]);
                if (m.getName().equals("commit")) commits++;
                if (m.getName().equals("rollback")) rollbacks++;
                return defaultValue(m.getReturnType());
            });
        }
        PreparedStatement statement(String sql) {
            Map<Integer,Object> parameters = new HashMap<>();
            boolean record = sql.contains("SP_RECORD_PAYMENT") || sql.contains("SP_CHECKIN_WITH_ROOM_PAYMENT");
            return (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{PreparedStatement.class}, (o,m,a) -> {
                String name = m.getName();
                if (name.startsWith("set") && a != null && a.length > 1 && a[0] instanceof Integer) {
                    parameters.put((Integer)a[0], name.equals("setNull") ? null : a[1]);
                    return null;
                }
                if (name.equals("execute")) {
                    if (record) { payment = new HashMap<>(parameters); paymentSql = sql; }
                    return record;
                }
                if (name.equals("getUpdateCount")) return -1;
                if (name.equals("getMoreResults")) {
                    if (record && lateFailure) throw new SQLException("Nested payment succeeded but stay update failed", "S0001", 51111);
                    return false;
                }
                if (name.equals("getResultSet")) return rows(Collections.emptyMap(), false);
                if (name.equals("executeQuery")) {
                    if (sql.contains("COALESCE(MAX")) return rows(Map.of(1, 0), true);
                    Map<Object,Object> row = new HashMap<>();
                    row.put("PaymentTime", new Timestamp(System.currentTimeMillis()));
                    row.put("Amount", payment.get(5)); row.put("WalletAmount", payment.get(8));
                    row.put("Status", "PAID"); row.put("TransactionCode", payment.get(7));
                    row.put("TransactionID", payment.get(10) == null ? "PAYMENT-PM0001" : payment.get(10));
                    return rows(row, true);
                }
                return defaultValue(m.getReturnType());
            });
        }
        ResultSet rows(Map<?,?> row, boolean exists) {
            boolean[] first = {true};
            return (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ResultSet.class}, (o,m,a) -> {
                if (m.getName().equals("next")) { boolean result = first[0] && exists; first[0] = false; return result; }
                if (m.getName().equals("getString")) { Object value=row.get(a[0]); return value == null ? null : value.toString(); }
                if (m.getName().equals("getInt")) return ((Number)row.get(a[0])).intValue();
                if (m.getName().startsWith("get") && a != null && a.length == 1) return row.get(a[0]);
                return defaultValue(m.getReturnType());
            });
        }
    }
    static final class Payments extends PaymentService {
        final Database database = new Database();
        Map<String,Object> summary = summary("ASSIGNED", new BigDecimal("700"), new BigDecimal("850"));
        BigDecimal wallet = new BigDecimal("1000");
        @Override public Map<String,Object> getPaymentSummary(String id, int user) { return summary; }
        @Override protected BigDecimal loadWalletBalance(int user) { return wallet; }
        @Override protected boolean isReceptionist(int user) { return user == 10; }
        @Override public Booking getBookingForPayment(String id, int user) { Booking b=new Booking(id); b.setBookingStatus("ASSIGNED"); return b; }
        @Override protected Paymentmethod findMethod(String id) { Paymentmethod method=new Paymentmethod(id); method.setMethodName(id); return method; }
        @Override protected Connection openConnection() { return database.connection(); }
        @Override protected void clearCache() { }
    }
    static Map<String,Object> summary(String status, BigDecimal room, BigDecimal remaining) {
        Map<String,Object> result = new HashMap<>();
        result.put("bookingStatus",status); result.put("firstPaidAt",new java.util.Date());
        result.put("paymentOption","DEPOSIT"); result.put("initialRequiredAmount",new BigDecimal("300"));
        result.put("roomRemainingAmount",room); result.put("remainingAmount",remaining);
        return result;
    }
    public static void main(String[] args) throws Exception {
        Payments service = new Payments();
        PaymentRequest balance = service.createPaymentRequest("BK0041",100);
        expect(balance.getAmount().compareTo(new BigDecimal("700")) == 0, "Before arrival only the room balance is due");
        service.summary = summary("CHECKED_IN",BigDecimal.ZERO,new BigDecimal("150"));
        expect(service.createPaymentRequest("BK0041",100).getAmount().compareTo(new BigDecimal("150")) == 0,"After check-in collect outstanding services");
        service.summary = summary("ASSIGNED",BigDecimal.ZERO,new BigDecimal("150"));
        rejects(() -> service.createPaymentRequest("BK0041",100),"Do not collect services as a room balance");
        service.summary = summary("PENDING_PAYMENT",new BigDecimal("1000"),new BigDecimal("1000"));
        service.summary.put("firstPaidAt",null);
        expect(service.createPaymentRequest("BK0041",100).getAmount().compareTo(new BigDecimal("300")) == 0,"Initial deposit remains 30 percent");
        rejects(() -> service.processPayment(balance,"PT01",100,false),"Customer cannot confirm a QR by POST");
        rejects(() -> service.processPayment(balance,"PT03",100,false),"Customer cannot record cash");
        expect(service.database.opens == 0,"Rejected methods do not open a write transaction");
        service.summary = summary("ASSIGNED",new BigDecimal("700"),new BigDecimal("850"));
        service.wallet = new BigDecimal("100");
        PaymentRequest insufficient = service.createPaymentRequest("BK0041",100);
        rejects(() -> service.processPayment(insufficient,"PT02",100,false),"Wallet-only needs sufficient funds");
        service.wallet = new BigDecimal("1000");
        Payment wallet = service.processPayment(service.createPaymentRequest("BK0041",100),"PT02",100,false);
        expect(service.database.commits == 1 && service.database.rollbacks == 0,"Wallet payment commits atomically");
        expect("PT02".equals(service.database.payment.get(6)) && new BigDecimal("700").equals(service.database.payment.get(8)),"Wallet uses the v6 method and contribution");
        expect(service.database.paymentSql.contains("@TransactionID = ?") && "PAYMENT-PM0001".equals(wallet.getTransactionID()),"Central transaction is passed and read back");
        Payments counter = new Payments();
        counter.collectAtCounter(counter.createPaymentRequest("BK0041",100),"PT03",10,false,false,"");
        expect("PT03".equals(counter.database.payment.get(6)) && counter.database.payment.get(10)==null,"Cash uses v6 and SQL-generated transaction ID");
        Payments qr = new Payments();
        qr.collectAtCounter(qr.createPaymentRequest("BK0041",100),"PT01",10,false,false,"BANK-123");
        expect("BANK-123".equals(qr.database.payment.get(10)),"Verified counter bank reference reaches SQL");
        rejects(() -> qr.collectAtCounter(qr.createPaymentRequest("BK0041",100),"PT01",11,false,false,"BANK-124"),"Only a receptionist can record counter transfers");
        Payments failed = new Payments(); failed.database.lateFailure = true;
        rejects(() -> failed.processPayment(failed.createPaymentRequest("BK0041",100),"PT02",100,false),"Errors after nested results are consumed");
        expect(failed.database.commits == 0 && failed.database.rollbacks == 1,"A late failure rolls the whole payment back");
        String url = new VietQrService().createUrl(new BigDecimal("500.00"),"HBMS test&ref");
        expect(URI.create(url).getRawQuery().contains("amount=500&") && url.contains("HBMS+test%26ref"),"QR preserves the server amount and encodes its reference");
        CheckInPaymentRegressionTest.run();
        System.out.println("PASS: " + checks + " payment/check-in regression checks");
    }
}
