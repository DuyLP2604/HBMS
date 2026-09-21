package dao;

import entity.Paymentmethod;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class PaymentmethodDAO {

    private static final EntityManagerFactory EMF
            = Persistence.createEntityManagerFactory(
                    "my_persistence_unit"
            );

    public List<Paymentmethod> getAll() {
        EntityManager em = EMF.createEntityManager();

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