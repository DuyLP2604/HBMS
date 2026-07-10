/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author default
 */
public class Customer {

    private String id;
    private String fullname;

    private String phone;
    private String email;
    private String address;
    private String cccd;
    private String passportNumber;
    private User user;
    private Nationality nationality;

    public Customer() {
    }

    public Customer(String id, String fullname, String phone, String email, String address,
            String cccd, String passportNumber, User user, Nationality nationality) {
        this.id = id;
        this.fullname = fullname;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.cccd = cccd;
        this.passportNumber = passportNumber;
        this.user = user;
        this.nationality = nationality;
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
    
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public Nationality getNationality() {
        return nationality;
    }

    public void setNationality(Nationality nationality) {
        this.nationality = nationality;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Customer{" + "id=" + id + ", fullname=" + fullname + ", phone=" + phone + ", email=" + email + ", address=" + address + ", cccd=" + cccd + ", passportNumber=" + passportNumber + ", user=" + user + ", nationality=" + nationality + '}';
    }
}