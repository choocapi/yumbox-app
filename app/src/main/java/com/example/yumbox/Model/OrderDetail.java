package com.example.yumbox.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class OrderDetail implements Serializable {
    private String userUid;
    private String userName;
    private ArrayList<CartItem> orderItems;
    private String address;
    private String phoneNumber;
    private String totalPrice;
    private Boolean orderAccepted = false;
    private Boolean paymentReceived = false;
    private Boolean orderReceived = false;
    private Integer paymentMethod;
    private String itemPushKey;
    private Long currentTime = 0L;
    private String ownerUid;

    public OrderDetail() {
    }

    public OrderDetail(String userUid, String userName, ArrayList<CartItem> orderItems, String address, String phoneNumber, String totalPrice, Boolean orderAccepted, Boolean paymentReceived, Boolean orderReceived, Integer paymentMethod, String itemPushKey, Long currentTime, String ownerUid) {
        this.userUid = userUid;
        this.userName = userName;
        this.orderItems = orderItems;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.totalPrice = totalPrice;
        this.orderAccepted = orderAccepted;
        this.paymentReceived = paymentReceived;
        this.orderReceived = orderReceived;
        this.paymentMethod = paymentMethod;
        this.itemPushKey = itemPushKey;
        this.currentTime = currentTime;
        this.ownerUid = ownerUid;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ArrayList<CartItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(ArrayList<CartItem> orderItems) {
        this.orderItems = orderItems;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Boolean getOrderAccepted() {
        return orderAccepted;
    }

    public void setOrderAccepted(Boolean orderAccepted) {
        this.orderAccepted = orderAccepted;
    }

    public Boolean getPaymentReceived() {
        return paymentReceived;
    }

    public void setPaymentReceived(Boolean paymentReceived) {
        this.paymentReceived = paymentReceived;
    }

    public Boolean getOrderReceived() {
        return orderReceived;
    }

    public void setOrderReceived(Boolean orderReceived) {
        this.orderReceived = orderReceived;
    }

    public Integer getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Integer paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getItemPushKey() {
        return itemPushKey;
    }

    public void setItemPushKey(String itemPushKey) {
        this.itemPushKey = itemPushKey;
    }

    public Long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Long currentTime) {
        this.currentTime = currentTime;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }
}
