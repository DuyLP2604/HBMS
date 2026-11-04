package dao;

import entity.Invoice;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import util.PersistenceManager;

public class DAOFramework<T> {

    private static final Set<String> PROCEDURE_TABLES = Set.of("BOOKING", "BOOKING_DETAIL", "BOOKING_SERVICE", "ROOM_ASSIGNMENT", "PAYMENT", "CUSTOMER_WALLET", "BOOKING_REFUND", "REFUND_PAYMENT", "WALLET_TRANSACTION", "USER_BOOKING_CONTROL", "BOOKING_LOCK_HISTORY", "TRANSACTION", "INVOICE_TRANSACTION", "INVOICE");
    private final Class<T> entityClass;

    public DAOFramework(Class<T> entityClassType) {
        this.entityClass = entityClassType;
    }

    public List<T> getAll(EntityManager em) {
        String entityName = em.getMetamodel().entity(entityClass).getName();
        return em.createQuery("SELECT x FROM " + entityName + " x", entityClass).getResultList();
    }

    public T getById(String id, EntityManager em) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return em.find(entityClass, id.trim());
    }

    public List<T> getAllBypassCache(EntityManager em) {
        String entityName = em.getMetamodel().entity(entityClass).getName();
        return em.createQuery("SELECT x FROM " + entityName + " x", entityClass)
                .setHint("jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS)
                .setHint("jakarta.persistence.cache.storeMode", CacheStoreMode.REFRESH)
                .getResultList();
    }

    public List<T> getAll() {
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            return getAll(em);
        }
    }

    public boolean save(T entity) {
        requireDirectWriteAllowed();
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            // Trích xuất ID
            Object id = PersistenceManager.getEMF().getPersistenceUnitUtil().getIdentifier(entity);

            boolean exists = false;
            if (id != null) {
                T found = em.find(entityClass, id,
                        java.util.Collections.singletonMap("jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS));
                exists = (found != null);
            }

            if (exists) {
                em.merge(entity);
            } else {
                em.persist(entity);
            }

            em.getTransaction().commit();
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return false;
        } finally {
            em.close();
        }
    }

    public T getById(String id) {
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            return getById(id, em);
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public boolean insert(T entity) {
        requireDirectWriteAllowed();
        return process(entity, true);
    }

    public boolean update(T entity) {
        requireDirectWriteAllowed();
        return process(entity, false);
    }

    public boolean deleteById(String id) {
        requireDirectWriteAllowed();
        if (entityClass == Invoice.class || id == null || id.isBlank()) {
            return false;
        }
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            T entity = em.find(entityClass, id.trim());
            if (entity == null) {
                em.getTransaction().rollback();
                return false;
            }
            em.remove(entity);
            em.getTransaction().commit();
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return false;
        } finally {
            em.close();
        }
    }

    private boolean process(T entity, boolean insert) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            if (insert) {
                em.persist(entity);
            } else {
                em.merge(entity);
            }
            em.getTransaction().commit();
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return false;
        } finally {
            em.close();
        }
    }

    public boolean saveAll(List<T> entities, boolean isImport, EntityManager em) {
        if (entities == null || entities.isEmpty()) {
            return true;
        }

        if (!isImport) {
            requireDirectWriteAllowed();
        }

        try {
            int batchSize = 50; // Tránh tràn bộ nhớ
            for (int i = 0; i < entities.size(); i++) {
                T entity = entities.get(i);
                // Trích xuất ID
                Object id = PersistenceManager.getEMF().getPersistenceUnitUtil().getIdentifier(entity);

                boolean exists = false;
                if (id != null) {
                    T found = em.find(entityClass, id,
                            java.util.Collections.singletonMap("jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS));
                    exists = (found != null);
                }

                if (exists) {
                    em.merge(entity);
                } else {
                    em.persist(entity);
                }

                if (i > 0 && i % batchSize == 0) {
                    em.flush();
                    em.clear();
                }
            }
            return true;
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            return false;
        }

    }

    private void requireDirectWriteAllowed() {
        Table table = entityClass.getAnnotation(Table.class);
        String tableName = table == null || table.name().isBlank() ? entityClass.getSimpleName() : table.name();
        if (PROCEDURE_TABLES.contains(tableName.replace("[", "").replace("]", "").toUpperCase(Locale.ROOT))) {
            throw new UnsupportedOperationException("Direct writes to " + tableName + " are disabled. Use the HBMS stored procedures.");
        }
    }
}
