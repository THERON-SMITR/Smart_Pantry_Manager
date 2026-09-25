package com.example.smartpantrymanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantrymanager.models.PantryItem;
import com.example.smartpantrymanager.models.Recipe;
import com.example.smartpantrymanager.models.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper{
    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE settings(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "setting_key TEXT NOT NULL UNIQUE, " +
                "setting_value TEXT, " +
                "description TEXT, " +
                "is_system INTEGER NOT NULL DEFAULT 0, " +
                "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE pantry_items(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "quantity REAL NOT NULL, " +
                "unit TEXT NOT NULL, " +
                "expiry_date TEXT)");

        db.execSQL("CREATE TABLE recipes(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "method TEXT NOT NULL)");

        db.execSQL("CREATE TABLE recipe_ingredients(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER NOT NULL, " +
                "ingredient_name TEXT NOT NULL, " +
                "quantity REAL NOT NULL, " +
                "unit TEXT NOT NULL, " +
                "FOREIGN KEY(recipe_id) REFERENCES recipes(id))");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS pantry_items");
        onCreate(db);
    }

    private long insertRecipe(SQLiteDatabase db, String name, String method) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("method", method);
        return db.insert("recipes", null, values);
    }

    private void insertIngredient(SQLiteDatabase db, long recipeId, String name, double quantity, String unit) {
        ContentValues values = new ContentValues();
        values.put("recipe_id", recipeId);
        values.put("ingredient_name", name);
        values.put("quantity", quantity);
        values.put("unit", unit);
        db.insert("recipe_ingredients", null, values);
    }

    private void seedRecipes(SQLiteDatabase db) {
        long id;

        id = insertRecipe(db, "Buttered Toast", "Toast the bread. Spread butter on while warm.");
        insertIngredient(db, id, "bread", 2, "slice");
        insertIngredient(db, id, "butter", 10, "g");

        id = insertRecipe(db, "Grilled Cheese Sandwich", "Butter one side of each bread slice. Place cheese between the unbuttered sides and grill in a pan until golden on both sides.");
        insertIngredient(db, id, "bread", 2, "slice");
        insertIngredient(db, id, "cheese", 30, "g");
        insertIngredient(db, id, "butter", 10, "g");

        id = insertRecipe(db, "Scrambled Eggs on Toast", "Beat the eggs with a pinch of salt. Cook gently in a buttered pan, stirring, until just set. Serve on toasted bread.");
        insertIngredient(db, id, "egg", 2, "unit");
        insertIngredient(db, id, "bread", 2, "slice");
        insertIngredient(db, id, "butter", 5, "g");
        insertIngredient(db, id, "salt", 1, "pinch");

        id = insertRecipe(db, "Cheese Omelette", "Beat the eggs with milk and salt. Pour into a hot buttered pan, sprinkle cheese on top, fold once set.");
        insertIngredient(db, id, "egg", 3, "unit");
        insertIngredient(db, id, "cheese", 20, "g");
        insertIngredient(db, id, "milk", 20, "ml");
        insertIngredient(db, id, "salt", 1, "pinch");

        id = insertRecipe(db, "Spinach and Cheese Omelette", "Wilt the spinach in a pan. Beat eggs with milk, pour over the spinach, sprinkle cheese, cook until set.");
        insertIngredient(db, id, "egg", 3, "unit");
        insertIngredient(db, id, "spinach", 50, "g");
        insertIngredient(db, id, "cheese", 20, "g");
        insertIngredient(db, id, "milk", 20, "ml");

        id = insertRecipe(db, "Fried Rice", "Scramble the egg in a buttered pan and set aside. Fry onion until soft, add cooked rice and soy sauce, stir through the egg.");
        insertIngredient(db, id, "rice", 200, "g");
        insertIngredient(db, id, "egg", 1, "unit");
        insertIngredient(db, id, "onion", 1, "unit");
        insertIngredient(db, id, "soy sauce", 15, "ml");
        insertIngredient(db, id, "butter", 5, "g");

        id = insertRecipe(db, "Garlic Rice", "Fry the chopped garlic in butter until fragrant. Stir in cooked rice and a pinch of salt.");
        insertIngredient(db, id, "rice", 200, "g");
        insertIngredient(db, id, "garlic", 2, "clove");
        insertIngredient(db, id, "butter", 10, "g");
        insertIngredient(db, id, "salt", 1, "pinch");

        id = insertRecipe(db, "Chicken and Rice", "Brown the chicken with onion and garlic. Add rice and enough water, cover and simmer until the rice is cooked.");
        insertIngredient(db, id, "chicken", 200, "g");
        insertIngredient(db, id, "rice", 200, "g");
        insertIngredient(db, id, "onion", 1, "unit");
        insertIngredient(db, id, "garlic", 2, "clove");
        insertIngredient(db, id, "salt", 1, "pinch");

        id = insertRecipe(db, "Chicken Stir Fry", "Fry the chicken until browned. Add onion and garlic, then soy sauce. Serve over rice.");
        insertIngredient(db, id, "chicken", 200, "g");
        insertIngredient(db, id, "onion", 1, "unit");
        insertIngredient(db, id, "garlic", 2, "clove");
        insertIngredient(db, id, "soy sauce", 15, "ml");
        insertIngredient(db, id, "rice", 200, "g");

        id = insertRecipe(db, "Chicken Sandwich", "Spread mayonnaise on the bread. Layer with cooked chicken and lettuce.");
        insertIngredient(db, id, "bread", 2, "slice");
        insertIngredient(db, id, "chicken", 100, "g");
        insertIngredient(db, id, "lettuce", 20, "g");
        insertIngredient(db, id, "mayonnaise", 10, "g");

        id = insertRecipe(db, "Vegetable Sandwich", "Butter the bread. Layer with lettuce, tomato and cucumber.");
        insertIngredient(db, id, "bread", 2, "slice");
        insertIngredient(db, id, "lettuce", 20, "g");
        insertIngredient(db, id, "tomato", 1, "unit");
        insertIngredient(db, id, "cucumber", 0.5, "unit");
        insertIngredient(db, id, "butter", 5, "g");

        id = insertRecipe(db, "Cheese and Tomato Toast", "Toast the bread, top with sliced tomato and cheese, grill until the cheese melts.");
        insertIngredient(db, id, "bread", 2, "slice");
        insertIngredient(db, id, "cheese", 20, "g");
        insertIngredient(db, id, "tomato", 1, "unit");

        id = insertRecipe(db, "Tomato Pasta", "Cook the pasta. Fry garlic in olive oil, add chopped tomato and salt, simmer briefly, then toss with the pasta.");
        insertIngredient(db, id, "pasta", 150, "g");
        insertIngredient(db, id, "tomato", 2, "unit");
        insertIngredient(db, id, "garlic", 2, "clove");
        insertIngredient(db, id, "olive oil", 15, "ml");
        insertIngredient(db, id, "salt", 1, "pinch");

        id = insertRecipe(db, "Pasta with Garlic and Oil", "Cook the pasta. Gently fry sliced garlic in olive oil until golden, toss through the pasta with salt.");
        insertIngredient(db, id, "pasta", 150, "g");
        insertIngredient(db, id, "garlic", 3, "clove");
        insertIngredient(db, id, "olive oil", 20, "ml");
        insertIngredient(db, id, "salt", 1, "pinch");

        id = insertRecipe(db, "Tomato Soup", "Fry onion and garlic until soft, add chopped tomato and a little water, simmer, then season and blend.");
        insertIngredient(db, id, "tomato", 4, "unit");
        insertIngredient(db, id, "onion", 1, "unit");
        insertIngredient(db, id, "garlic", 1, "clove");
        insertIngredient(db, id, "salt", 1, "pinch");
        insertIngredient(db, id, "pepper", 1, "pinch");

        id = insertRecipe(db, "Onion Soup", "Slowly fry the sliced onion and garlic in butter until soft and golden, add water, season and simmer.");
        insertIngredient(db, id, "onion", 2, "unit");
        insertIngredient(db, id, "garlic", 1, "clove");
        insertIngredient(db, id, "butter", 15, "g");
        insertIngredient(db, id, "salt", 1, "pinch");
        insertIngredient(db, id, "pepper", 1, "pinch");

        id = insertRecipe(db, "Mashed Potatoes", "Boil the potatoes until soft. Drain, then mash with butter, milk and salt.");
        insertIngredient(db, id, "potato", 300, "g");
        insertIngredient(db, id, "butter", 15, "g");
        insertIngredient(db, id, "milk", 30, "ml");
        insertIngredient(db, id, "salt", 1, "pinch");

        id = insertRecipe(db, "Potato Salad", "Boil the potatoes until tender, cool, then mix with chopped onion, mayonnaise, salt and pepper.");
        insertIngredient(db, id, "potato", 300, "g");
        insertIngredient(db, id, "onion", 1, "unit");
        insertIngredient(db, id, "mayonnaise", 20, "g");
        insertIngredient(db, id, "salt", 1, "pinch");
        insertIngredient(db, id, "pepper", 1, "pinch");

        id = insertRecipe(db, "Cucumber Salad", "Slice the cucumber and onion thinly, toss with olive oil and salt.");
        insertIngredient(db, id, "cucumber", 1, "unit");
        insertIngredient(db, id, "onion", 0.5, "unit");
        insertIngredient(db, id, "olive oil", 10, "ml");
        insertIngredient(db, id, "salt", 1, "pinch");
    }


    // ---------- Create ----------
    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", item.getName());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        values.put("expiry_date", item.getExpiryDate());
        return db.insert("pantry_items", null, values);
    }

    // ---------- Read ----------
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("pantry_items", null, null, null, null, null, "name ASC");

        while (cursor.moveToNext()) {
            items.add(mapCursorToPantryItem(cursor));
        }
        cursor.close();
        return items;
    }

    public PantryItem getPantryItemById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("pantry_items", null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        PantryItem item = null;
        if (cursor.moveToFirst()) {
            item = mapCursorToPantryItem(cursor);
        }
        cursor.close();
        return item;
    }

    // ---------- Update ----------
    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", item.getName());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        values.put("expiry_date", item.getExpiryDate());

        return db.update("pantry_items", values, "id = ?",
                new String[]{String.valueOf(item.getId())});
    }

    // ---------- Delete ----------
    public int deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("pantry_items", "id = ?", new String[]{String.valueOf(id)});
    }

    // ---------- Helper ----------
    private PantryItem mapCursorToPantryItem(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity"));
        String unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"));
        String expiryDate = cursor.getString(cursor.getColumnIndexOrThrow("expiry_date"));
        return new PantryItem(id, name, quantity, unit, expiryDate);
    }

    // ---------- Recipes (read-only: seeded once, never edited by the user) ----------

    /** All recipes, each with its full ingredient list already loaded. */
    public List<Recipe> getAllRecipesWithIngredients() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("recipes", null, null, null, null, null, "name ASC");

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String method = cursor.getString(cursor.getColumnIndexOrThrow("method"));
            recipes.add(new Recipe(id, name, method, getIngredientsForRecipe(db, id)));
        }
        cursor.close();
        return recipes;
    }

    /** One recipe (with its ingredients) by id, or null if it no longer exists. */
    public Recipe getRecipeById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("recipes", null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        Recipe recipe = null;
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String method = cursor.getString(cursor.getColumnIndexOrThrow("method"));
            recipe = new Recipe(id, name, method, getIngredientsForRecipe(db, id));
        }
        cursor.close();
        return recipe;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        Cursor cursor = db.query("recipe_ingredients", null, "recipe_id = ?",
                new String[]{String.valueOf(recipeId)}, null, null, null);

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("ingredient_name"));
            double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity"));
            String unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"));
            ingredients.add(new RecipeIngredient(id, recipeId, name, quantity, unit));
        }
        cursor.close();
        return ingredients;
    }

    // ---------- Settings (key/value) ----------

    /** Reads one setting's value, or defaultValue if the key has never been saved. */
    public String getSetting(String key, String defaultValue) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("settings", new String[]{"setting_value"},
                "setting_key = ?", new String[]{key}, null, null, null);

        String value = defaultValue;
        if (cursor.moveToFirst()) {
            value = cursor.getString(cursor.getColumnIndexOrThrow("setting_value"));
        }
        cursor.close();
        return value;
    }

    /** Saves one setting, updating the existing row if the key is already present. */
    public void setSetting(String key, String value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("setting_value", value);

        int rowsUpdated = db.update("settings", values, "setting_key = ?", new String[]{key});
        if (rowsUpdated == 0) {
            values.put("setting_key", key);
            db.insert("settings", null, values);
        }
    }

}
