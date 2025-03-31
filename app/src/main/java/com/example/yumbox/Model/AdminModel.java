package com.example.yumbox.Model;

public class AdminModel {
    private String name;
    private String ownerUid;
    private String nameOfRestaurant;
    private String email;
    private String password;
    private String address;
    private String phone;
    private String role;

    public AdminModel() {
    }

    public AdminModel(String name, String ownerUid, String nameOfRestaurant, String email, String password, String address, String phone) {
        this.name = name;
        this.ownerUid = ownerUid;
        this.nameOfRestaurant = nameOfRestaurant;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phone = phone;
        this.role = "ownerRestaurant";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    public String getNameOfRestaurant() {
        return nameOfRestaurant;
    }

    public void setNameOfRestaurant(String nameOfRestaurant) {
        this.nameOfRestaurant = nameOfRestaurant;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public void setRole(String role) {
        this.role = role;
    }
}
