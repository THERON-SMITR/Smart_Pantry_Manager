package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.models.PantryItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Pantry screen: lists every ingredient stored in the database, with a + button to add
 * a new one, tap-to-edit and a bin button to delete.
 */
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PantryAdapter adapter;
    private RecyclerView recyclerPantry;
    private TextView textEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        recyclerPantry = findViewById(R.id.recyclerPantry);
        textEmpty = findViewById(R.id.textEmpty);

        setUpRecyclerView();
        setUpAddButton();
        setUpBottomNavigation();
    }

    /** Reload the list every time we come back (e.g. after saving on the Add/Edit screen). */
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void setUpRecyclerView() {
        adapter = new PantryAdapter(new PantryAdapter.OnItemActionListener() {
            @Override
            public void onEdit(PantryItem item) {
                // Pass the id so the form knows it is editing rather than adding.
                Intent intent = new Intent(MainActivity.this, AddEditIngredientActivity.class);
                intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(PantryItem item) {
                confirmDelete(item);
            }
        });
        recyclerPantry.setLayoutManager(new LinearLayoutManager(this));
        recyclerPantry.setAdapter(adapter);
    }

    private void setUpAddButton() {
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));
    }

    /** Reads all items from the database and shows either the list or the empty message. */
    private void loadPantry() {
        List<PantryItem> items = dbHelper.getAllPantryItems();
        adapter.setItems(items);

        boolean empty = items.isEmpty();
        textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerPantry.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void confirmDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, item.getName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    dbHelper.deletePantryItem(item.getId());
                    loadPantry();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setUpBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        // This screen IS the Pantry tab, so mark it selected without triggering the listener.
        bottomNav.setSelectedItemId(R.id.nav_pantry);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_suggestions) {
                openScreen(SuggestedRecipesActivity.class);
                return true;
            } else if (id == R.id.nav_settings) {
                openScreen(SettingsActivity.class);
                return true;
            }
            return true; // Already on Pantry.
        });
    }

    private void openScreen(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}
