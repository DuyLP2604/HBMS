package dao; // Thay đổi theo tên package của bạn

import entity.Transaction;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class TransactionDAO extends DAOFramework<Transaction> {

    public TransactionDAO() {
        super(Transaction.class);
    }

    /**
     * Tạo Transaction ID tối ưu hiệu suất (Không cần query DB). Format:
     * [PREFIX][YYMMDDHHMMSS][4 Số ngẫu nhiên] Ví dụ: VQR2610241830451234
     *
     * @param paymentType Truyền vào "VIETQR", "CASH", hoặc "WALLET"
     * @return Chuỗi Transaction ID duy nhất
     */
    public String generateTransactionID(String paymentType) {
        String prefix;
        switch (paymentType.toUpperCase()) {
            case "VIETQR":
                prefix = "VQR";
                break;
            case "CASH":
                prefix = "CSH";
                break;
            case "WALLET":
                prefix = "WLT";
                break;
            default:
                prefix = "TXN";
                break;
        }

        String timestamp = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
        int randomNum = new Random().nextInt(9000) + 1000;

        return prefix + timestamp + randomNum;
    }

}
