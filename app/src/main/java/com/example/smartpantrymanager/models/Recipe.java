package com.example.smartpantrymanager.models;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private long id;
    private String name;
    private String method; // preparation steps, as one block of text
    private List<RecipeIngredient> ingredients;

    public Recipe(long id, String name, String method, List<RecipeIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.method = method;
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
    }

    // Convenience constructor for a recipe being seeded, before it has an id or ingredients loaded.
    public Recipe(String name, String method) {
        this(0, name, method, new ArrayList<>());
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }

    public void addIngredient(RecipeIngredient ingredient) { this.ingredients.add(ingredient); }
}
