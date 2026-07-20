/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Lenovo
 */
public class Room {
    private String roomId;
    private String roomNumber;
    private String roomImage;
    private double price;
    private String status;
    
    private Hotel hotel;

    public Room() {
    }

    public Room(String roomId, String roomNumber, String roomImage, double price, String status, Hotel hotel) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomImage = roomImage;
        this.price = price;
        this.status = status;
        this.hotel = hotel;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomImage() {
        return roomImage;
    }

    public void setRoomImage(String roomImage) {
        this.roomImage = roomImage;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    @Override
    public String toString() {
        return "Room{" + "roomId=" + roomId + ", roomNumber=" + roomNumber + ", roomImage=" + roomImage + ", price=" + price + ", status=" + status + ", hotel=" + hotel + '}';
    }
    
    
}
