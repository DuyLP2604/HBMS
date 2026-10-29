package service;

import jakarta.persistence.*;
import java.util.*;

/** Checks the intentional JPA model against an isolated in-memory database. */
public class JpaMappingSmokeTest {
    public static void main(String[] args) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        properties.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:hbms_mapping;MODE=MSSQLServer;DB_CLOSE_DELAY=-1");
        properties.put("jakarta.persistence.jdbc.user", "sa");
        properties.put("jakarta.persistence.jdbc.password", "");
        properties.put("eclipselink.target-database", "org.eclipse.persistence.platform.database.H2Platform");
        properties.put("eclipselink.logging.level", "SEVERE");
        properties.put("eclipselink.weaving", "false");
        try (EntityManagerFactory factory = Persistence.createEntityManagerFactory("my_persistence_unit", properties)) {
            Set<String> entities = new HashSet<>();
            factory.getMetamodel().getEntities().forEach(e -> entities.add(e.getName()));
            for (String removed : List.of("BookingRefund", "CustomerWallet", "WalletTransaction", "InvoiceTransaction",
                    "BookingLockHistory", "UserBookingControl")) {
                if (entities.contains(removed)) throw new AssertionError("Unexpected generated entity: " + removed);
            }
            if (!entities.containsAll(List.of("Booking", "Payment", "Complaint", "Transaction", "Invoice", "BookingService"))) {
                throw new AssertionError("Missing core mappings: " + entities);
            }
            System.out.println("PASS: EclipseLink deployed " + entities.size() + " intentional entities; no orphan generated relationships.");
        }
    }
}
