package com.example.smartpantrymanager;

import android.content.Context;

import com.example.smartpantrymanager.database.DatabaseHelper;

/**
 * Small wrapper around the "settings" table (setting_key / setting_value) in the app's
 * SQLite database. Settings are stored as text and converted to their real type here,
 * so callers such as SettingsActivity never touch the database directly.
 */
public class AppPreferences {

    private static final String KEY_ALERTS_ENABLED = "expiry_alerts_enabled";
    private static final String KEY_ALERT_DAYS = "expiry_alert_days";

    public static final int DEFAULT_ALERT_DAYS = 3;

    private final DatabaseHelper dbHelper;

    public AppPreferences(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    /** Whether the user wants to be warned about ingredients that are expiring soon. */
    public boolean isExpiryAlertsEnabled() {
        return Boolean.parseBoolean(dbHelper.getSetting(KEY_ALERTS_ENABLED, "true"));
    }

    public void setExpiryAlertsEnabled(boolean enabled) {
        dbHelper.setSetting(KEY_ALERTS_ENABLED, String.valueOf(enabled));
    }

    /** How many days before the expiry date an item counts as "expiring soon". */
    public int getExpiryAlertDays() {
        String value = dbHelper.getSetting(KEY_ALERT_DAYS, String.valueOf(DEFAULT_ALERT_DAYS));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return DEFAULT_ALERT_DAYS;
        }
    }

    public void setExpiryAlertDays(int days) {
        dbHelper.setSetting(KEY_ALERT_DAYS, String.valueOf(days));
    }
}
