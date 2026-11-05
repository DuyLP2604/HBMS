package service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.*;
import service.CheckInService.CheckInRequest;
import static service.PaymentFlowRegressionTest.*;

public class CheckInPaymentRegressionTest {
    static class CheckIns extends CheckInService {
        final Database database = new Database();
        @Override protected boolean isAvailableMethod(String method) { return Set.of("PT01","PT02","PT03").contains(method); }
        @Override protected Connection openConnection() { return database.connection(); }
        @Override protected void clearCache() { }
        CheckInRequest request(String amount) {
            Map<String,Object> summary=new HashMap<>();
            summary.put("bookingStatus","ASSIGNED"); summary.put("roomRemainingAmount",new BigDecimal(amount));
            summary.put("customerUserID",100);
            return createRequest("BK0041",10,summary);
        }
    }
    static void run() throws Exception {
        CheckIns cash = new CheckIns();
        cash.confirm(cash.request("700"),10,new String[]{"012345678901"},"PT03",true,null,null);
        expect(cash.database.commits==1 && cash.database.payment.get(4).equals(new BigDecimal("700")),"Check-in collects the room amount only");
        expect(cash.database.paymentSql.contains("SP_CHECKIN_WITH_ROOM_PAYMENT") && cash.database.paymentSql.contains("@TransactionID = ?"),"Collection and arrival use the atomic v6 procedure");
        CheckIns qr = new CheckIns();
        rejects(() -> qr.confirm(qr.request("700"),10,new String[]{"012345678901"},"PT01",true,null,""),"QR requires a bank reference");
        expect(qr.database.opens==0,"Unverified QR cannot reach the check-in procedure");
        qr.confirm(qr.request("700"),10,new String[]{"012345678901"},"PT01",true,null,"BANK-567");
        expect("BANK-567".equals(qr.database.payment.get(8)),"Check-in passes the verified transfer ID");
        CheckIns wallet = new CheckIns();
        rejects(() -> wallet.confirm(wallet.request("700"),10,new String[]{"012345678901"},"PT02",true,99,null),"Wallet must be authorized by its owner");
        wallet.confirm(wallet.request("700"),10,new String[]{"012345678901"},"PT02",true,100,null);
        expect(Integer.valueOf(100).equals(wallet.database.payment.get(7)),"Owner authorization reaches SQL");
        CheckIns paid = new CheckIns();
        paid.confirm(paid.request("0"),10,new String[]{"012345678901"},"",false,null,null);
        expect(paid.database.payment.get(5)==null && paid.database.commits==1,"A prepaid booking checks in without an additional collection");
        CheckIns fail = new CheckIns(); fail.database.lateFailure=true;
        rejects(() -> fail.confirm(fail.request("700"),10,new String[]{"012345678901"},"PT03",true,null,null),"A late stay error is propagated");
        expect(fail.database.commits==0 && fail.database.rollbacks==1,"Failed check-in rolls back the nested collection");
    }
}
