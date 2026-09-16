/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.Booking;
import entity.Customer;
import entity.Employee;
import entity.Hotel;
import entity.Invoice;
import entity.Room;
import entity.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Admin
 */
public class InvoiceDAO {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("my_persistence_unit");

    public List<Map<String, String>> getOccupiedRooms() {
        List<Map<String, String>> list = new ArrayList<>();
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT b FROM Booking b WHERE b.roomID.status = 'Occupied' AND b.bookingStatus != 'Completed'";
            TypedQuery<Booking> query = em.createQuery(jpql, Booking.class);
            List<Booking> bookings = query.getResultList();

            for (Booking b : bookings) {
                Map<String, String> room = new HashMap<>();
                room.put("bookingID", b.getBookingID());
                room.put("customerName", b.getCustomerID().getFullName());
                room.put("hotelName", b.getRoomID().getHotelID().getHotelName());
                room.put("roomID", b.getRoomID().getRoomID());
                room.put("roomNumber", b.getRoomID().getRoomNumber());
                room.put("nationality", b.getCustomerID().getNationalityID().getNationalityName());
                room.put("price", b.getRoomID().getPrice().toString());
                room.put("checkInDate", b.getCheckInDate().toString());
                room.put("checkOutDate", b.getCheckOutDate().toString());
                list.add(room);
            }
        }
        return list;
    }

    public List<Map<String, String>> getPaidInvoices() {
        List<Map<String, String>> list = new ArrayList<>();
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT i FROM Invoice i ORDER BY i.invoiceDate DESC";
            TypedQuery<Invoice> query = em.createQuery(jpql, Invoice.class);
            query.setMaxResults(10);
            List<Invoice> invoices = query.getResultList();

            for (Invoice i : invoices) {
                Map<String, String> invoice = new HashMap<>();
                invoice.put("invoiceID", i.getInvoiceID());
                invoice.put("bookingID", i.getBookingID().getBookingID());
                invoice.put("customerName", i.getCustomerID().getFullName());
                invoice.put("hotelName", i.getHotelID().getHotelName());
                invoice.put("roomNumber", i.getBookingID().getRoomID().getRoomNumber());
                invoice.put("totalAmount", i.getTotalAmount().toString());
                invoice.put("invoiceDate", i.getInvoiceDate().toString());
                list.add(invoice);
            }
        }
        return list;
    }

    public boolean isBookingPaid(String bookingID) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT COUNT(i) FROM Invoice i WHERE i.bookingID.bookingID = :bookingId";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("bookingID", bookingID);
            return query.getSingleResult() > 0;
        }
    }

    public List<Map<String, String>> getServicesByBooking(String bookingID) {
        List<Map<String, String>> list = new ArrayList<>();
        try (EntityManager em = emf.createEntityManager()) {
            Booking b = em.find(Booking.class, bookingID);
            if (b != null && b.getServiceCollection() != null) {
                for (Service s : b.getServiceCollection()) {
                    Map<String, String> serviceMap = new HashMap<>();
                    serviceMap.put("serviceID", s.getServiceID());
                    serviceMap.put("serviceName", s.getServiceName());
                    serviceMap.put("unitPrice", s.getUnitPrice().toString());
                    list.add(serviceMap);
                }
            }
        }
        return list;
    }

    public Map<String, String> getRoomDetailByBooking(String bookingID) {
        try (EntityManager em = emf.createEntityManager()) {
            Booking b = em.find(Booking.class, bookingID);
            if (b != null) {
                Map<String, String> room = new HashMap<>();
                room.put("bookingID", b.getBookingID());
                room.put("customerName", b.getCustomerID().getFullName());
                room.put("roomNumber", b.getRoomID().getRoomNumber());

                double price = b.getRoomID().getPrice().doubleValue();

                // Tính số ngày sử dụng ChronoUnit thay cho DATEDIFF của SQL
                long totalDays = ChronoUnit.DAYS.between(
                        b.getCheckInDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        b.getCheckOutDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                );
                if (totalDays == 0) {
                    totalDays = 1;
                }

                double serviceTotal = 0;
                if (b.getServiceCollection() != null) {
                    for (Service s : b.getServiceCollection()) {
                        serviceTotal += s.getUnitPrice().doubleValue();
                    }
                }

                double roomTotal = price * totalDays;
                double baseTotal = roomTotal + serviceTotal;

                room.put("price", String.valueOf(price));
                room.put("checkInDate", b.getCheckInDate().toString());
                room.put("checkOutDate", b.getCheckOutDate().toString());
                room.put("totalDays", String.valueOf(totalDays));
                room.put("roomTotal", String.valueOf(roomTotal));
                room.put("serviceTotal", String.valueOf(serviceTotal));
                room.put("baseTotal", String.valueOf(baseTotal));

                return room;
            }
        }
        return null;
    }

    public boolean processCheckout(String bookingId, double totalAmount, String employeeId) {
        EntityManager em = emf.createEntityManager();
        try {
            //open transaction
            em.getTransaction().begin();
            //find booking being to process
            Booking booking = em.find(Booking.class, bookingId);
            if (booking == null) {
                return false;
            }
            //get employee for process
            Employee employee = em.find(Employee.class, employeeId);

            Hotel hotel = booking.getRoomID().getHotelID();
            Customer customer = booking.getCustomerID();
            Room room = booking.getRoomID();

            //Create new invoice id
            String jpql = "SELECT MAX(CAST(SUBSTRING(i.invoiceID, 3, LENGTH(i.invoiceID)) AS Integer)) FROM Invoice i";
            TypedQuery<Integer> query = em.createQuery(jpql, Integer.class);
            Integer maxId = query.getSingleResult();

            String newInvoiceId = "HD01";
            if (maxId != null) {
                newInvoiceId = String.format("HD%02d", maxId + 1);
            }

            Invoice invoice = new Invoice();

            invoice.setInvoiceID(newInvoiceId);
            invoice.setInvoiceType("Checkout");
            invoice.setInvoiceDate(new java.util.Date());
            invoice.setTotalAmount(BigDecimal.valueOf(totalAmount));
            invoice.setHotelID(hotel);
            invoice.setCustomerID(customer);
            invoice.setEmployeeID(employee);
            invoice.setBookingID(booking);

            em.persist(invoice);

            // Update booking status completed
            booking.setBookingStatus("Completed");
            booking.setCheckOutDate(new java.util.Date());
            //update in db
            em.merge(booking);

            // 5. Update room status
            room.setStatus("Available");
            //update in db
            em.merge(room);

            //comfirm all updated
            em.getTransaction().commit();
            return true;

        } catch (Exception e) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
        return false;
    }
}
