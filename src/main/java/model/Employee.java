/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author default
 */
public class Employee {
    private String id;
    private String fullname;
    private String position;
    private double salary;
    private String shift;
    private String address;
    private String phone;
    private Hotel hotel;
    private User user;
    
    public Employee() {
    }

    public Employee(String id, String fullname, String position, double salary, String shift, String address, String phone, Hotel hotel, User user) {
        this.id = id;
        this.fullname = fullname;
        this.position = position;
        this.salary = salary;
        this.shift = shift;
        this.address = address;
        this.phone = phone;
        this.hotel = hotel;
        this.user = user;
    }

    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
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

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Employee{" + "id=" + id + ", fullname=" + fullname + ", position=" + position + ", salary=" + salary + ", shift=" + shift + ", address=" + address + ", phone=" + phone + ", hotel=" + hotel + ", user=" + user + '}';
    }
    
    
    
}
