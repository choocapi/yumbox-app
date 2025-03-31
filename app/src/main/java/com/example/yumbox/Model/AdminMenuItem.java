package com.example.yumbox.Model;

public class AdminMenuItem {
    private String foodName;
    private String foodPrice;
    private String foodImage;
    private String foodDescription;
    private String foodIngredients;
    private String foodKey;
    private String foodType;
    private String ownerUid;
    private String nameOfRestaurant;

    public AdminMenuItem() {
    }

    public AdminMenuItem(String foodName, String foodPrice, String foodImage, String foodDescription, String foodIngredients, String foodKey, String foodType, String ownerUid, String nameOfRestaurant) {
        this.foodName = foodName;
        this.foodPrice = foodPrice;
        this.foodImage = foodImage;
        this.foodDescription = foodDescription;
        this.foodIngredients = foodIngredients;
        this.foodKey = foodKey;
        this.foodType = foodType;
        this.ownerUid = ownerUid;
        this.nameOfRestaurant = nameOfRestaurant;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(String foodPrice) {
        this.foodPrice = foodPrice;
    }

    public String getFoodImage() {
        return foodImage;
    }

    public void setFoodImage(String foodImage) {
        this.foodImage = foodImage;
    }

    public String getFoodDescription() {
        return foodDescription;
    }

    public void setFoodDescription(String foodDescription) {
        this.foodDescription = foodDescription;
    }

    public String getFoodIngredients() {
        return foodIngredients;
    }

    public void setFoodIngredients(String foodIngredients) {
        this.foodIngredients = foodIngredients;
    }

    public String getFoodKey() {
        return foodKey;
    }

    public void setFoodKey(String foodKey) {
        this.foodKey = foodKey;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    public void setNameOfRestaurant(String nameOfRestaurant) {
        this.nameOfRestaurant = nameOfRestaurant;
    }

    public String getNameOfRestaurant() {
        return nameOfRestaurant;
    }
}
