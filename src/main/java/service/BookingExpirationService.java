package service;

import entity.Booking;
import jakarta.persistence.EntityManager;
import util.PersistenceManager;

public class BookingExpirationService {

    public int expirePendingBookings() {
        EntityManager em = PersistenceManager.createEntityManager();

        try {
            em.getTransaction().begin();

            String jpql
                    = "UPDATE Booking b "
                    + "SET b.bookingStatus = :cancelledStatus "
                    + "WHERE b.bookingStatus = :pendingStatus "
                    + "AND b.paymentDeadline IS NOT NULL "
                    + "AND b.paymentDeadline <= CURRENT_TIMESTAMP";

            int updatedRows
                    = em.createQuery(jpql)
                            .setParameter(
                                    "cancelledStatus",
                                    "CANCELLED"
                            )
                            .setParameter(
                                    "pendingStatus",
                                    "PENDING_PAYMENT"
                            )
                            .executeUpdate();

            em.getTransaction().commit();

            /*
             * Remove stale Booking objects from this EMF cache.
             */
            PersistenceManager.getEMF().getCache().evict(Booking.class);

            return updatedRows;
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            throw new RuntimeException(
                    "Unable to expire pending bookings.",
                    ex
            );
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }
}
