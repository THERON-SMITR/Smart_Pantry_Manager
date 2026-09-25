package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.models.PantryItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

/**
 * Form for adding a new pantry ingredient, or editing an existing one when an
 * EXTRA_ITEM_ID is passed in through the Intent.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    /** Intent extra holding the id of the item being edited (absent when adding). */
    public static final String EXTRA_ITEM_ID = "item_id";

    private static final long NO_ITEM = -1;

    private DatabaseHelper dbHelper;
    private long itemId = NO_ITEM;

    private TextInputLayout layoutName;
    private TextInputLayout layoutQuantity;
    private TextInputLayout layoutExpiry;
    private TextInputEditText editName;
    private TextInputEditText editQuantity;
    private TextInputEditText editExpiry;
    private Spinner spinnerUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_ingredient);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        layoutName = findViewById(R.id.layoutName);
        layoutQuantity = findViewById(R.id.layoutQuantity);
        layoutExpiry = findViewById(R.id.layoutExpiry);
        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        editExpiry = findViewById(R.id.editExpiry);
        spinnerUnit = findViewById(R.id.spinnerUnit);

        setUpUnitSpinner();

        // If an id was passed in, we are editing: change the title and fill the form.
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, NO_ITEM);
        if (itemId != NO_ITEM) {
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setTitle(R.string.title_edit_ingredient);
            loadExistingItem();
        }

        editExpiry.setOnClickListener(v -> showDatePicker());
        layoutExpiry.setEndIconOnClickListener(v -> editExpiry.setText(""));
        findViewById(R.id.buttonSave).setOnClickListener(v -> saveIngredient());
        findViewById(R.id.buttonCancel).setOnClickListener(v -> finish());
    }

    private void setUpUnitSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.unit_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(adapter);
    }

    /** Fills the form with the values of the item being edited. */
    private void loadExistingItem() {
        PantryItem item = dbHelper.getPantryItemById(itemId);
        if (item == null) {
            finish(); // The item no longer exists.
            return;
        }
        editName.setText(item.getName());
        editQuantity.setText(String.valueOf(item.getQuantity()));
        editExpiry.setText(item.getExpiryDate());

        String[] units = getResources().getStringArray(R.array.unit_options);
        for (int i = 0; i < units.length; i++) {
            if (units[i].equals(item.getUnit())) {
                spinnerUnit.setSelection(i);
                break;
            }
        }
    }

    /** Date picker that writes the chosen date as yyyy-MM-dd (sorts correctly as text). */
    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                editExpiry.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    /** Validates the form, then inserts (new) or updates (existing) the item. */
    private void saveIngredient() {
        layoutName.setError(null);
        layoutQuantity.setError(null);

        // Names are stored lower-case and trimmed so "Tomato " and "tomato" match later.
        String name = editName.getText().toString().trim().toLowerCase(Locale.ROOT);
        String quantityText = editQuantity.getText().toString().trim();
        String unit = spinnerUnit.getSelectedItem().toString();
        String expiry = editExpiry.getText().toString().trim();

        boolean valid = true;

        if (name.isEmpty()) {
            layoutName.setError(getString(R.string.error_name_required));
            valid = false;
        }

        double quantity = 0;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            // Leave quantity at 0; it is rejected just below.
        }
        if (quantity <= 0) {
            layoutQuantity.setError(getString(R.string.error_quantity_invalid));
            valid = false;
        }

        if (!valid) {
            return;
        }

        // Expiry is optional: store null when the field is empty.
        String expiryValue = expiry.isEmpty() ? null : expiry;

        boolean success;
        if (itemId == NO_ITEM) {
            success = dbHelper.addPantryItem(new PantryItem(0, name, quantity, unit, expiryValue)) != -1;
        } else {
            success = dbHelper.updatePantryItem(new PantryItem(itemId, name, quantity, unit, expiryValue)) > 0;
        }

        if (success) {
            Toast.makeText(this, R.string.msg_saved, Toast.LENGTH_SHORT).show();
            finish(); // MainActivity reloads the list in onResume().
        } else {
            Toast.makeText(this, R.string.msg_save_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
