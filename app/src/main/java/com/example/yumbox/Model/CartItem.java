package com.example.yumbox.Model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String foodName;
    private String foodPrice;
    private String foodImage;
    private String foodDescription;
    private String foodIngredients;
    private Integer foodQuantity;
    private String ownerUid;
    private String nameOfRestaurant;

    public CartItem() {
    }

    public CartItem(String foodName, String foodPrice, String foodImage, String foodDescription, String foodIngredients, Integer foodQuantity, String ownerUid, String nameOfRestaurant) {
        this.foodName = foodName;
        this.foodPrice = foodPrice;
        this.foodImage = foodImage;
        this.foodDescription = foodDescription;
        this.foodIngredients = foodIngredients;
        this.foodQuantity = foodQuantity;
        this.ownerUid = ownerUid;
        this.nameOfRestaurant = nameOfRestaurant;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public void setFoodPrice(String foodPrice) {
        this.foodPrice = foodPrice;
    }

    public void setFoodImage(String foodImage) {
        this.foodImage = foodImage;
    }

    public void setFoodDescription(String foodDescription) {
        this.foodDescription = foodDescription;
    }

    public void setFoodIngredients(String foodIngredients) {
        this.foodIngredients = foodIngredients;
    }

    public void setFoodQuantity(Integer foodQuantity) {
        this.foodQuantity = foodQuantity;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    public void setNameOfRestaurant(String nameOfRestaurant) {
        this.nameOfRestaurant = nameOfRestaurant;
    }

    public String getFoodName() {
        return foodName;
    }

    public String getFoodPrice() {
        return foodPrice;
    }

    public String getFoodImage() {
        return foodImage;
    }

    public String getFoodDescription() {
        return foodDescription;
    }

    public String getFoodIngredients() {
        return foodIngredients;
    }

    public Integer getFoodQuantity() {
        return foodQuantity;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public String getNameOfRestaurant() {
        return nameOfRestaurant;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "foodName='" + foodName + '\'' +
                ", foodPrice='" + foodPrice + '\'' +
                ", foodImage='" + foodImage + '\'' +
                ", foodDescription='" + foodDescription + '\'' +
                ", foodIngredients='" + foodIngredients + '\'' +
                ", foodQuantity=" + foodQuantity +
                ", ownerUid='" + ownerUid + '\'' +
                ", nameOfRestaurant='" + nameOfRestaurant + '\'' +
                '}';
    }
}
