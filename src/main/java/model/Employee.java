package model;

import java.math.BigDecimal;

public class Employee {

    private String id;
    private String name;
    private String position;
    private BigDecimal salary;
    private String shift;
    private String address;
    private String phone;
    private String hotelId;

    public Employee() {
    }

    public Employee(String id, String name, String position, BigDecimal salary, String shift, String address, String phone, String hotelId) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.salary = salary;
        this.shift = shift;
        this.address = address;
        this.phone = phone;
        this.hotelId = hotelId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }
}
