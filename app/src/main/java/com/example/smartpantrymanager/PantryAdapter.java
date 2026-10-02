package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.models.PantryItem;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Adapter used to create rows from the list of PantryItem objects **/
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** Lets the Activity decide what happens when a row is tapped or its bin is pressed. **/
    public interface OnItemActionListener {
        void onEdit(PantryItem item);

        void onDelete(PantryItem item);
    }

    private static final long MILLIS_PER_DAY = 24L * 60 * 60 * 1000;
    private static final int BORDER_WIDTH_DP = 3;

    private final List<PantryItem> items = new ArrayList<>();
    private final OnItemActionListener listener;

    /** Expiry highlighting, taken from the Settings screen. **/
    private boolean alertsEnabled = false;
    private int alertDays = AppPreferences.DEFAULT_ALERT_DAYS;

    public PantryAdapter(OnItemActionListener listener) {
        this.listener = listener;
    }

    /** Sets whether expiring/expired rows get a coloured border, and how many days counts as "soon". **/
    public void setExpiryAlerts(boolean enabled, int days) {
        this.alertsEnabled = enabled;
        this.alertDays = days;
    }

    /** Replaces the data and refreshes the list. **/
    public void setItems(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);

        holder.textName.setText(capitalise(item.getName()));
        holder.textQuantity.setText(formatQuantity(item.getQuantity()) + " " + item.getUnit());

        String expiry = item.getExpiryDate();
        if (expiry == null || expiry.isEmpty()) {
            holder.textExpiry.setText(R.string.no_expiry);
        } else {
            holder.textExpiry.setText(holder.itemView.getContext()
                    .getString(R.string.expires_on, expiry));
        }

        applyExpiryBorder((MaterialCardView) holder.itemView, expiry);

        holder.itemView.setOnClickListener(v -> listener.onEdit(item));
        holder.buttonDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    /**
     * Red border = already expired, orange border = expires within the chosen number of
     * days, no border otherwise. Rows are reused by the RecyclerView, so the border must
     * always be set explicitly (including removed) or it would carry over to other rows.
     **/
    private void applyExpiryBorder(MaterialCardView card, String expiry) {
        int color = 0;
        boolean highlight = false;

        if (alertsEnabled && expiry != null && !expiry.isEmpty()) {
            Long daysLeft = daysUntil(expiry);
            if (daysLeft != null) {
                if (daysLeft < 0) {
                    color = ContextCompat.getColor(card.getContext(), R.color.expiry_expired);
                    highlight = true;
                } else if (daysLeft <= alertDays) {
                    color = ContextCompat.getColor(card.getContext(), R.color.expiry_warning);
                    highlight = true;
                }
            }
        }

        if (highlight) {
            float density = card.getResources().getDisplayMetrics().density;
            card.setStrokeColor(color);
            card.setStrokeWidth((int) (BORDER_WIDTH_DP * density + 0.5f));
        } else {
            card.setStrokeWidth(0);
        }
    }

    /** Whole days from today until the expiry date (yyyy-MM-dd); negative if it has passed. **/
    private Long daysUntil(String expiry) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setLenient(false);
            Date expiryDate = format.parse(expiry);

            /** Compare against the start of today so the time of day does not matter. **/
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            long difference = expiryDate.getTime() - today.getTimeInMillis();
            return Math.round(difference / (double) MILLIS_PER_DAY);
        } catch (ParseException e) {
            /** Unreadable date: leave the row un-highlighted. **/
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Normalize quantity values e.g 2.0 is shown as "2", 0.5 stays "0.5". **/
    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }

    /** Normalize stings to be capitalized **/
    private String capitalise(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    /** The views of one row so they are only looked up once. **/
    static class PantryViewHolder extends RecyclerView.ViewHolder {
        final TextView textName;
        final TextView textQuantity;
        final TextView textExpiry;
        final ImageButton buttonDelete;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textExpiry = itemView.findViewById(R.id.textExpiry);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
