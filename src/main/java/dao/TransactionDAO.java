package dao; // Thay đổi theo tên package của bạn

import entity.Transaction;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.EntityManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.List;
import util.PersistenceManager;

public class TransactionDAO extends DAOFramework<Transaction> {

    public TransactionDAO() {
        super(Transaction.class);
    }

    public Transaction findByTransactionID(String transactionID) {
        if (transactionID == null || transactionID.isBlank()) return null;
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            return findByTransactionID(transactionID, em);
        }
    }

    public Transaction findByTransactionCode(String transactionCode) {
        if (transactionCode == null || transactionCode.isBlank()) return null;
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            return findByTransactionCode(transactionCode, em);
        }
    }

    public Transaction findByTransactionID(String transactionID, EntityManager em) {
        return findByReference(em, "Transaction.findByTransactionID", "transactionID", transactionID);
    }

    public Transaction findByTransactionCode(String transactionCode, EntityManager em) {
        return findByReference(em, "Transaction.findByTransactionCode", "transactionCode", transactionCode);
    }

    private Transaction findByReference(EntityManager em, String query, String parameter, String reference) {
        if (reference == null || reference.isBlank()) {
            return null;
        }
        List<Transaction> matches = em.createNamedQuery(query, Transaction.class)
                .setParameter(parameter, reference.trim())
                .setHint("jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS)
                .setHint("jakarta.persistence.cache.storeMode", CacheStoreMode.REFRESH)
                .getResultList();
        return matches.isEmpty() ? null : matches.get(0);
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
