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

/** Looks up payment transactions by their internal or external identifiers. */
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
     * Build a transaction ID from its payment type, timestamp, and random suffix.
     *
     * @param paymentType the payment method type
     * @return the generated transaction ID
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
