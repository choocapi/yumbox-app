package com.example.yumbox.Model;

import java.io.Serializable;

public class CustomerMenuItem implements Serializable {
    private String foodName;
    private String foodPrice;
    private String foodImage;
    private String foodDescription;
    private String foodIngredients;
    private String foodType;
    private String ownerUid;
    private String nameOfRestaurant;

    public CustomerMenuItem() {
    }

    public CustomerMenuItem(String foodName, String foodPrice, String foodImage, String foodDescription, String foodIngredients, String foodType, String ownerUid, String nameOfRestaurant) {
        this.foodName = foodName;
        this.foodPrice = foodPrice;
        this.foodImage = foodImage;
        this.foodDescription = foodDescription;
        this.foodIngredients = foodIngredients;
        this.foodType = foodType;
        this.ownerUid = ownerUid;
        this.nameOfRestaurant = nameOfRestaurant;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public void setFoodPrice(String foodPrice) {
        this.foodPrice = foodPrice;
    }

    public void setfoodImage(String foodImage) {
        this.foodImage = foodImage;
    }

    public void setFoodDescription(String foodDescription) {
        this.foodDescription = foodDescription;
    }

    public void setFoodIngredients(String foodIngredients) {
        this.foodIngredients = foodIngredients;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
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

    public String getFoodType() {
        return foodType;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public String getNameOfRestaurant() {
        return nameOfRestaurant;
    }
}
