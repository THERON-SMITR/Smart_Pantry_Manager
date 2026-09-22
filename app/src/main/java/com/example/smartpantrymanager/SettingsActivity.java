package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * Settings screen: expiring-soon alerts (on/off + how many days ahead) and the
 * preferred units system. Every change is written straight to SharedPreferences
 * (via {@link AppPreferences}) so there is no separate "Save" button.
 */
public class SettingsActivity extends AppCompatActivity {

    private AppPreferences preferences;

    private MaterialSwitch switchExpiryAlerts;
    private Spinner spinnerAlertDays;
    private RadioGroup radioGroupUnits;

    // The day counts that line up with the spinner entries (res/values/arrays.xml).
    private int[] alertDayValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        preferences = new AppPreferences(this);
        alertDayValues = getResources().getIntArray(R.array.alert_days_values);

        switchExpiryAlerts = findViewById(R.id.switchExpiryAlerts);
        spinnerAlertDays = findViewById(R.id.spinnerAlertDays);
        radioGroupUnits = findViewById(R.id.radioGroupUnits);

        setUpAlertDaysSpinner();
        loadSavedSettings();
        setUpListeners();
        setUpBottomNavigation();
    }

    /** Fills the spinner with the "N days before" options. */
    private void setUpAlertDaysSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.alert_days_entries, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAlertDays.setAdapter(adapter);
    }

    /** Shows the currently saved values in the controls (before listeners are attached). */
    private void loadSavedSettings() {
        switchExpiryAlerts.setChecked(preferences.isExpiryAlertsEnabled());
        spinnerAlertDays.setSelection(indexOfDays(preferences.getExpiryAlertDays()));
        spinnerAlertDays.setEnabled(preferences.isExpiryAlertsEnabled());

        if (AppPreferences.UNITS_IMPERIAL.equals(preferences.getUnits())) {
            radioGroupUnits.check(R.id.radioImperial);
        } else {
            radioGroupUnits.check(R.id.radioMetric);
        }
    }

    private void setUpListeners() {
        switchExpiryAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setExpiryAlertsEnabled(isChecked);
            // The "days before" choice is meaningless while alerts are switched off.
            spinnerAlertDays.setEnabled(isChecked);
        });

        spinnerAlertDays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.setExpiryAlertDays(alertDayValues[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do: the previous value stays saved.
            }
        });

        radioGroupUnits.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioImperial) {
                preferences.setUnits(AppPreferences.UNITS_IMPERIAL);
            } else {
                preferences.setUnits(AppPreferences.UNITS_METRIC);
            }
        });
    }

    /**
     * Bottom navigation: Settings is the selected tab; the other two tabs use Intents
     * to move to the Pantry and Suggested Recipes screens.
     */
    private void setUpBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        // This tab is the settings page
        bottomNav.setSelectedItemId(R.id.nav_settings);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                openScreen(MainActivity.class);
                return true;
            } else if (id == R.id.nav_suggestions) {
                openScreen(SuggestedRecipesActivity.class);
                return true;
            }
            return true; // Already on Settings.
        });
    }

    /** Brings an existing instance of the target screen to the front instead of stacking copies. */
    private void openScreen(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    /** Position of a given day count in the spinner; falls back to the default (3 days). */
    private int indexOfDays(int days) {
        for (int i = 0; i < alertDayValues.length; i++) {
            if (alertDayValues[i] == days) {
                return i;
            }
        }
        return indexOfDefaultDays();
    }

    private int indexOfDefaultDays() {
        for (int i = 0; i < alertDayValues.length; i++) {
            if (alertDayValues[i] == AppPreferences.DEFAULT_ALERT_DAYS) {
                return i;
            }
        }
        return 0;
    }
}
