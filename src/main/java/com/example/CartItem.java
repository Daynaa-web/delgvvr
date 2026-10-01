package com.example;

public class CartItem {
    private int productId;
    private String productName;
    private double price;
    private int quantity;
    private double subtotal;

    public CartItem(int productId, String productName, double price, int quantity, double subtotal) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public double getSubtotal() { return subtotal; }
}