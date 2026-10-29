package dao;

import entity.Paymentmethod;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.stream.Collectors;
import util.PersistenceManager;

public class PaymentmethodDAO {

    public List<Paymentmethod> getBookingMethods() {
        return getAll().stream()
                .filter(method -> method.getMethodID() != null
                        && java.util.Set.of("PT01", "PT02").contains(method.getMethodID().trim()))
                .collect(Collectors.toList());
    }

    public List<Paymentmethod> getCollectionMethods() {
        return getAll().stream()
                .filter(method -> method.getMethodID() != null
                        && java.util.Set.of("PT01", "PT02", "PT03").contains(method.getMethodID().trim()))
                .collect(Collectors.toList());
    }

    public List<Paymentmethod> getAll() {
        EntityManager em = PersistenceManager.createEntityManager();

        try {
            String jpql
                    = "SELECT pm FROM Paymentmethod pm "
                    + "ORDER BY pm.methodName ASC";

            TypedQuery<Paymentmethod> query
                    = em.createQuery(
                            jpql,
                            Paymentmethod.class
                    );

            return query.getResultList();
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Unable to load payment methods.",
                    ex
            );
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }
}
