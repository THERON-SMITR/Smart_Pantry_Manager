package com.example.smartpantrymanager.models;

public class RecipeIngredient {
    private long id;
    private final String ingredientName;
    private final double quantity;
    private final String unit;

    public RecipeIngredient(long id, String ingredientName, double quantity, String unit) {
        this.id = id;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getIngredientName() { return ingredientName; }

    public double getQuantity() { return quantity; }

    public String getUnit() { return unit; }
}
