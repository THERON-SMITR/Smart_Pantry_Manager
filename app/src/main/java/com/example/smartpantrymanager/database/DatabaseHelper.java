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
    // ---------- Recipes ----------
    /** Creating the recipes in the database **/
    private long insertRecipe(SQLiteDatabase db, String name, String method) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("method", method);
        return db.insert("recipes", null, values);
    }

    /** Linking ingredients to the recipes using the recipe id **/
    private void insertRecipeIngredient(SQLiteDatabase db, long recipeId, String name, double quantity, String unit) {
        ContentValues values = new ContentValues();
        values.put("recipe_id", recipeId);
        values.put("ingredient_name", name);
        values.put("quantity", quantity);
        values.put("unit", unit);
        db.insert("recipe_ingredients", null, values);
    }

    /** Reading all recipes from the database **/
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

    /** Reading a recipes from the database using id **/
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

    /** Reading a recipe ingredients from the database using the recipe id **/
    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        Cursor cursor = db.query("recipe_ingredients", null, "recipe_id = ?",
                new String[]{String.valueOf(recipeId)}, null, null, null);

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("ingredient_name"));
            double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity"));
            String unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"));
            ingredients.add(new RecipeIngredient(id, name, quantity, unit));
        }
        cursor.close();
        return ingredients;
    }

    // ---------- Pantry Items ----------
    /** Creating a new pantry item **/
    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", item.getName());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        values.put("expiry_date", item.getExpiryDate());
        return db.insert("pantry_items", null, values);
    }

    /** Reading all pantry item **/
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

    /** Reading a pantry item using the item id **/
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

    /** Updating a pantry item using the item id **/
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

    /**
     * Delete a pantry item using the item
     **/
    public void deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("pantry_items", "id = ?", new String[]{String.valueOf(id)});
    }

    /** Raw DB data helper - Converts to usable Java object **/
    private PantryItem mapCursorToPantryItem(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity"));
        String unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"));
        String expiryDate = cursor.getString(cursor.getColumnIndexOrThrow("expiry_date"));
        return new PantryItem(id, name, quantity, unit, expiryDate);
    }


    // ---------- Settings ----------
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

    // ---------- Recipe Seed ----------
    /** Seed  to add 20 recipes to the database **/
    private void seedRecipes(SQLiteDatabase db) {
        long id;

        id = insertRecipe(db, "Buttered Toast", "Toast the bread. Spread butter on while warm.");
        insertRecipeIngredient(db, id, "bread", 2, "slice");
        insertRecipeIngredient(db, id, "butter", 10, "g");

        id = insertRecipe(db, "Grilled Cheese Sandwich", "Butter one side of each bread slice. Place cheese between the unbuttered sides and grill in a pan until golden on both sides.");
        insertRecipeIngredient(db, id, "bread", 2, "slice");
        insertRecipeIngredient(db, id, "cheese", 30, "g");
        insertRecipeIngredient(db, id, "butter", 10, "g");

        id = insertRecipe(db, "Scrambled Eggs on Toast", "Beat the eggs with a pinch of salt. Cook gently in a buttered pan, stirring, until just set. Serve on toasted bread.");
        insertRecipeIngredient(db, id, "egg", 2, "unit");
        insertRecipeIngredient(db, id, "bread", 2, "slice");
        insertRecipeIngredient(db, id, "butter", 5, "g");
        insertRecipeIngredient(db, id, "salt", 1, "pinch");

        id = insertRecipe(db, "Cheese Omelette", "Beat the eggs with milk and salt. Pour into a hot buttered pan, sprinkle cheese on top, fold once set.");
        insertRecipeIngredient(db, id, "egg", 3, "unit");
        insertRecipeIngredient(db, id, "cheese", 20, "g");
        insertRecipeIngredient(db, id, "milk", 20, "ml");
        insertRecipeIngredient(db, id, "salt", 1, "pinch");

        id = insertRecipe(db, "Spinach and Cheese Omelette", "Wilt the spinach in a pan. Beat eggs with milk, pour over the spinach, sprinkle cheese, cook until set.");
        insertRecipeIngredient(db, id, "egg", 3, "unit");
        insertRecipeIngredient(db, id, "spinach", 50, "g");
        insertRecipeIngredient(db, id, "cheese", 20, "g");
        insertRecipeIngredient(db, id, "milk", 20, "ml");

        id = insertRecipe(db, "Fried Rice", "Scramble the egg in a buttered pan and set aside. Fry onion until soft, add cooked rice and soy sauce, stir through the egg.");
        insertRecipeIngredient(db, id, "rice", 200, "g");
        insertRecipeIngredient(db, id, "egg", 1, "unit");
        insertRecipeIngredient(db, id, "onion", 1, "unit");
        insertRecipeIngredient(db, id, "soy sauce", 15, "ml");
        insertRecipeIngredient(db, id, "butter", 5, "g");

        id = insertRecipe(db, "Garlic Rice", "Fry the chopped garlic in butter until fragrant. Stir in cooked rice and a pinch of salt.");
        insertRecipeIngredient(db, id, "rice", 200, "g");
        insertRecipeIngredient(db, id, "garlic", 2, "clove");
        insertRecipeIngredient(db, id, "butter", 10, "g");
        insertRecipeIngredient(db, id, "salt", 1, "pinch");

        id = insertRecipe(db, "Chicken and Rice", "Brown the chicken with onion and garlic. Add rice and enough water, cover and simmer until the rice is cooked.");
        insertRecipeIngredient(db, id, "chicken", 200, "g");
        insertRecipeIngredient(db, id, "rice", 200, "g");
        insertRecipeIngredient(db, id, "onion", 1, "unit");
        insertRecipeIngredient(db, id, "garlic", 2, "clove");
        insertRecipeIngredient(db, id, "salt", 1, "pinch");

        id = insertRecipe(db, "Chicken Stir Fry", "Fry the chicken until browned. Add onion and garlic, then soy sauce. Serve over rice.");
        insertRecipeIngredient(db, id, "chicken", 200, "g");
        insertRecipeIngredient(db, id, "onion", 1, "unit");
        insertRecipeIngredient(db, id, "garlic", 2, "clove");
        insertRecipeIngredient(db, id, "soy sauce", 15, "ml");
        insertRecipeIngredient(db, id, "rice", 200, "g");

        id = insertRecipe(db, "Chicken Sandwich", "Spread mayonnaise on the bread. Layer with cooked chicken and lettuce.");
        insertRecipeIngredient(db, id, "bread", 2, "slice");
        insertRecipeIngredient(db, id, "chicken", 100, "g");
        insertRecipeIngredient(db, id, "lettuce", 20, "g");
        insertRecipeIngredient(db, id, "mayonnaise", 10, "g");

        id = insertRecipe(db, "Vegetable Sandwich", "Butter the bread. Layer with lettuce, tomato and cucumber.");
        insertRecipeIngredient(db, id, "bread", 2, "slice");
        insertRecipeIngredient(db, id, "lettuce", 20, "g");
        insertRecipeIngredient(db, id, "tomato", 1, "unit");
        insertRecipeIngredient(db, id, "cucumber", 0.5, "unit");
        insertRecipeIngredient(db, id, "butter", 5, "g");

        id = insertRecipe(db, "Cheese and Tomato Toast", "Toast the bread, top with sliced tomato and cheese, grill until the cheese melts.");
        insertRecipeIngredient(db, id, "bread", 2, "slice");
        insertRecipeIngredient(db, id, "cheese", 20, "g");
        insertRecipeIngredient(db, id, "tomato", 1, "unit");

        id = insertRecipe(db, "Tomato Pasta", "Cook the pasta. Fry garlic in olive oil, add chopped tomato and salt, simmer briefly, then toss with the pasta.");
        insertRecipeIngredient(db, id, "pasta", 150, "g");
        insertRecipeIngredient(db, id, "tomato", 2, "unit");
        insertRecipeIngredient(db, id, "garlic", 2, "clove");
        insertRecipeIngredient(db, id, "olive oil", 15, "ml");
        insertRecipeIngredient(db, id, "salt", 1, "pinch");

        id = insertRecipe(db, "Pasta with Garlic and Oil", "Cook the pasta. Gently fry sliced garlic in olive oil until golden, toss through the pasta with salt.");
        insertRecipeIngredient(db, id, "pasta", 150, "g");
        insertRecipeIngredient(db, id, "garlic", 3, "clove");
        insertRecipeIngredient(db, id, "olive oil", 20, "ml");
        insertRecipeIngredient(db, id, "salt", 1, "pinch");

        id = insertRecipe(db, "Tomato Soup", "Fry onion and garlic until soft, add chopped tomato and a little water, simmer, then season and blend.");
        insertRecipeIngredient(db, id, "tomato", 4, "unit");
        insertRecipeIngredient(db, id, "onion", 1, "unit");
        insertRecipeIngredient(db, id, "garlic", 1, "clove");
        insertRecipeIngredient(db, id, "salt", 1, "pinch");
        insertRecipeIngredient(db, id, "pepper", 1, "pinch");

        id = insertRecipe(db, "Onion Soup", "Slowly fry the sliced onion and garlic in butter until soft and golden, add water, season and simmer.");
        insertRecipeIngredient(db, id, "onion", 2, "unit");
        insertRecipeIngredient(db, id, "garlic", 1, "clove");
        insertRecipeIngredient(db, id, "butter", 15, "g");
        insertRecipeIngredient(db, id, "salt", 1, "pinch");
        insertRecipeIngredient(db, id, "pepper", 1, "pinch");

        id = insertRecipe(db, "Mashed Potatoes", "Boil the potatoes until soft. Drain, then mash with butter, milk and salt.");
        insertRecipeIngredient(db, id, "potato", 300, "g");
        insertRecipeIngredient(db, id, "butter", 15, "g");
        insertRecipeIngredient(db, id, "milk", 30, "ml");
        insertRecipeIngredient(db, id, "salt", 1, "pinch");

        id = insertRecipe(db, "Potato Salad", "Boil the potatoes until tender, cool, then mix with chopped onion, mayonnaise, salt and pepper.");
        insertRecipeIngredient(db, id, "potato", 300, "g");
        insertRecipeIngredient(db, id, "onion", 1, "unit");
        insertRecipeIngredient(db, id, "mayonnaise", 20, "g");
        insertRecipeIngredient(db, id, "salt", 1, "pinch");
        insertRecipeIngredient(db, id, "pepper", 1, "pinch");

        id = insertRecipe(db, "Cucumber Salad", "Slice the cucumber and onion thinly, toss with olive oil and salt.");
        insertRecipeIngredient(db, id, "cucumber", 1, "unit");
        insertRecipeIngredient(db, id, "onion", 0.5, "unit");
        insertRecipeIngredient(db, id, "olive oil", 10, "ml");
        insertRecipeIngredient(db, id, "salt", 1, "pinch");
    }
}
