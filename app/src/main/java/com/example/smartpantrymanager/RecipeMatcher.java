package com.example.smartpantrymanager;

import com.example.smartpantrymanager.models.PantryItem;
import com.example.smartpantrymanager.models.Recipe;
import com.example.smartpantrymanager.models.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Implements the app's core rule **/
public class RecipeMatcher {
    /* Units that can be converted to a common base for comparison. **/
    private static final Map<String, Double> GRAMS = new HashMap<>();
    private static final Map<String, Double> MILLILITRES = new HashMap<>();

    static {
        GRAMS.put("g", 1.0);
        GRAMS.put("kg", 1000.0);
        GRAMS.put("oz", 28.35);
        GRAMS.put("lb", 453.59);

        MILLILITRES.put("ml", 1.0);
        MILLILITRES.put("l", 1000.0);
        MILLILITRES.put("fl oz", 29.57);
        MILLILITRES.put("cup", 236.59);
    }

    /* Filters allRecipes down to only the ones the current pantry can make right now. **/
    public List<Recipe> getSuggestedRecipes(List<Recipe> allRecipes, List<PantryItem> pantryItems) {
        List<Recipe> suggestions = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (matches(recipe, pantryItems)) {
                suggestions.add(recipe);
            }
        }
        return suggestions;
    }

    /* True only if every ingredient the recipe needs is available in enough quantity. **/
    public boolean matches(Recipe recipe, List<PantryItem> pantryItems) {
        for (RecipeIngredient required : recipe.getIngredients()) {
            if (!pantryHasEnough(required, pantryItems)) {
                /* One missing/short ingredient disqualifies the whole recipe. **/
                return false;
            }
        }
        return true;
    }

    public List<RecipeIngredient> getMissingIngredients(Recipe recipe, List<PantryItem> pantryItems) {
        List<RecipeIngredient> missing = new ArrayList<>();
        for (RecipeIngredient required : recipe.getIngredients()) {
            if (!pantryHasEnough(required, pantryItems)) {
                missing.add(required);
            }
        }
        return missing;
    }

    public Map<Recipe, RecipeIngredient> getAlmostThereRecipes(List<Recipe> allRecipes, List<PantryItem> pantryItems) {
        Map<Recipe, RecipeIngredient> almostThere = new LinkedHashMap<>();
        for (Recipe recipe : allRecipes) {
            List<RecipeIngredient> missing = getMissingIngredients(recipe, pantryItems);
            if (missing.size() == 1) {
                almostThere.put(recipe, missing.get(0));
            }
        }
        return almostThere;
    }

    private boolean pantryHasEnough(RecipeIngredient required, List<PantryItem> pantryItems) {
        String requiredName = normalise(required.getIngredientName());

        for (PantryItem pantryItem : pantryItems) {
            if (normalise(pantryItem.getName()).equals(requiredName)) {
                return hasEnoughQuantity(
                        pantryItem.getQuantity(), pantryItem.getUnit(),
                        required.getQuantity(), required.getUnit());
            }
        }
        /* Ingredient is not in the pantry at all. **/
        return false;
    }

    /** Lower-cases and strips a simple plural ending so "tomato"/"tomatoes" match. **/
    private String normalise(String ingredientName) {
        String name = ingredientName.trim().toLowerCase(Locale.ROOT);

        if (name.endsWith("ies") && name.length() > 4) {
            /* berries -> berry **/
            return name.substring(0, name.length() - 3) + "y";
        }
        if (name.endsWith("oes") && name.length() > 4) {
            /* tomatoes -> tomato **/
            return name.substring(0, name.length() - 2);
        }
        if (name.endsWith("s") && !name.endsWith("ss") && name.length() > 3) {
            /* onions -> onion, eggs -> egg **/
            return name.substring(0, name.length() - 1);
        }
        return name;
    }

    /* True if the pantry quantity (in its unit) covers the required quantity (in its unit). **/
    private boolean hasEnoughQuantity(double pantryQty, String pantryUnit,
                                       double requiredQty, String requiredUnit) {
        String pUnit = pantryUnit.trim().toLowerCase(Locale.ROOT);
        String rUnit = requiredUnit.trim().toLowerCase(Locale.ROOT);

        if (pUnit.equals(rUnit)) {
            return pantryQty >= requiredQty;
        }

        Double pantryGrams = GRAMS.get(pUnit);
        Double requiredGrams = GRAMS.get(rUnit);
        if (pantryGrams != null && requiredGrams != null) {
            return pantryQty * pantryGrams >= requiredQty * requiredGrams;
        }

        Double pantryMl = MILLILITRES.get(pUnit);
        Double requiredMl = MILLILITRES.get(rUnit);
        if (pantryMl != null && requiredMl != null) {
            return pantryQty * pantryMl >= requiredQty * requiredMl;
        }

        /* Units are not directly comparable (e.g. "clove" vs "unit") - cannot confirm enough. **/
        return false;
    }
}
