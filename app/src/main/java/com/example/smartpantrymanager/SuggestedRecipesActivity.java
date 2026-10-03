package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.models.PantryItem;
import com.example.smartpantrymanager.models.Recipe;
import com.example.smartpantrymanager.models.RecipeIngredient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/* Suggested Recipes screen: used to show suggestion recipes base on the ingredients **/
public class SuggestedRecipesActivity extends AppCompatActivity {
    private static final int TAB_ALMOST_THERE = 1;

    private DatabaseHelper dbHelper;
    private RecipeAdapter adapter;
    private RecyclerView recyclerSuggestions;
    private TextView textNoMatches;
    private TabLayout tabLayout;
    private List<Recipe> readyRecipes = new ArrayList<>();
    private Map<Recipe, RecipeIngredient> almostThereRecipes = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suggested_recipes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        recyclerSuggestions = findViewById(R.id.recyclerSuggestions);
        textNoMatches = findViewById(R.id.textNoMatches);
        tabLayout = findViewById(R.id.tabLayout);

        setUpRecyclerView();
        setUpTabs();
        setUpBottomNavigation();
    }

    /* Revalidate the  suggestions everytime this screen is reloaded - the pantry may have changed. **/
    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void setUpRecyclerView() {
        adapter = new RecipeAdapter(recipe -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(intent);
        });
        recyclerSuggestions.setLayoutManager(new LinearLayoutManager(this));
        recyclerSuggestions.setAdapter(adapter);
    }

    private void setUpTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showSelectedTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    /* Loads the pantry and every recipe, then keeps only the ones that strictly match. **/
    private void loadSuggestions() {
        List<PantryItem> pantryItems = dbHelper.getAllPantryItems();
        List<Recipe> allRecipes = dbHelper.getAllRecipesWithIngredients();

        RecipeMatcher matcher = new RecipeMatcher();
        readyRecipes = matcher.getSuggestedRecipes(allRecipes, pantryItems);
        almostThereRecipes = matcher.getAlmostThereRecipes(allRecipes, pantryItems);

        showSelectedTab();
    }

    private void showSelectedTab() {
        boolean almostThereTab = tabLayout.getSelectedTabPosition() == TAB_ALMOST_THERE;

        List<Recipe> recipes;
        Map<Long, String> missing = new HashMap<>();
        if (almostThereTab) {
            recipes = new ArrayList<>(almostThereRecipes.keySet());
            for (Map.Entry<Recipe, RecipeIngredient> entry : almostThereRecipes.entrySet()) {
                missing.put(entry.getKey().getId(), entry.getValue().getIngredientName());
            }
        } else {
            recipes = readyRecipes;
        }

        adapter.setMissingIngredients(missing);
        adapter.setRecipes(recipes);

        boolean empty = recipes.isEmpty();
        textNoMatches.setText(almostThereTab ? R.string.no_almost_there : R.string.no_recipes_match);
        textNoMatches.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerSuggestions.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void setUpBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        /* Highlights the suggestions tab (selected) without triggering the listener. **/
        bottomNav.setSelectedItemId(R.id.nav_suggestions);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                openScreen(MainActivity.class);
                return true;
            } else if (id == R.id.nav_settings) {
                openScreen(SettingsActivity.class);
                return true;
            }
            /** Already on Suggestions. **/
            return true;
        });
    }

    /* Helper used to navigate the tabs without stacking up duplicate screens. **/
    private void openScreen(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}
