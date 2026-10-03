package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.models.Recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Adapter used to create rows from the list of recipes objects **/
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }
    private final List<Recipe> recipes = new ArrayList<>();
    private final Map<Long, String> missingIngredients = new HashMap<>();
    private final OnRecipeClickListener listener;
    public RecipeAdapter(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    /* Replaces the data and refreshes the list. **/
    public void setRecipes(List<Recipe> newRecipes) {
        recipes.clear();
        recipes.addAll(newRecipes);
        notifyDataSetChanged();
    }

    public void setMissingIngredients(Map<Long, String> missing) {
        missingIngredients.clear();
        missingIngredients.putAll(missing);
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        int count = recipe.getIngredients().size();

        holder.textName.setText(recipe.getName());

        String missing = missingIngredients.get(recipe.getId());
        if (missing != null) {
            holder.textCount.setText(holder.itemView.getContext()
                    .getString(R.string.missing_ingredient, missing));
        } else {
            holder.textCount.setText(holder.itemView.getContext().getResources()
                    .getQuantityString(R.plurals.ingredient_count, count, count));
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        final TextView textName;
        final TextView textCount;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textRecipeName);
            textCount = itemView.findViewById(R.id.textIngredientCount);
        }
    }
}
