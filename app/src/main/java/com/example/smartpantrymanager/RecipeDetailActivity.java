package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.models.Recipe;
import com.example.smartpantrymanager.models.RecipeIngredient;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Shows the full ingredient list and method for one recipe, passed in as
 * EXTRA_RECIPE_ID from the Suggested Recipes screen.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = new DatabaseHelper(this).getRecipeById(recipeId);

        TextView textIngredients = findViewById(R.id.textIngredients);
        TextView textMethod = findViewById(R.id.textMethod);

        if (recipe == null) {
            // The recipe no longer exists (should not normally happen - seeded data is not edited).
            finish();
            return;
        }

        toolbar.setTitle(recipe.getName());
        textIngredients.setText(buildIngredientList(recipe));
        textMethod.setText(recipe.getMethod());
    }

    /** Builds a "- 2 slice bread" style bullet list from the recipe's ingredients. */
    private String buildIngredientList(Recipe recipe) {
        StringBuilder builder = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            builder.append("• ")
                    .append(formatQuantity(ingredient.getQuantity()))
                    .append(" ")
                    .append(ingredient.getUnit())
                    .append(" ")
                    .append(ingredient.getIngredientName())
                    .append("\n");
        }
        return builder.toString().trim();
    }

    /** 2.0 is shown as "2", 0.5 stays "0.5". */
    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }
}
