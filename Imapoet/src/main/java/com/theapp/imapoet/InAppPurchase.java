package com.theapp.imapoet;

/**
 * In app purchase item
 * Created by whitney on 10/8/14.
 */
public class InAppPurchase {
    private boolean purchased = false;
    private String title;
    private String description;
    private String type;
    private String price;
    private String productId;
    //private String price_amount_micros;
    //private String price_currency_code;

    public String title() { return title; }
    public String description() { return description; }
    public String price() { return price; }
    public String type() { return type; }
    public String productId() { return productId; }
    public boolean hasBeenPurchased () { return purchased; }
    //public void setPurchased(boolean hasBeenPurchased) {
   //     purchased = hasBeenPurchased;
    //}

    public InAppPurchase(String title, String description, String type, String price, String productId, boolean purchased) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.price = price;
        this.productId = productId;
        this.purchased = purchased;
    }
}
