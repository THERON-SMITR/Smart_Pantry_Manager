package com.example.smartpantrymanager;

/* Implimented a converter to update the pantry and recipes based on the user settings **/
public class UnitConverter {
    private static final double GRAMS_PER_OUNCE = 28.35;
    private static final double GRAMS_PER_POUND = 453.59;
    private static final double ML_PER_FL_OZ = 29.57;
    private static final double ML_PER_CUP = 236.59;

    public static String format(double quantity, String unit, String system) {
        boolean imperial = AppPreferences.UNITS_IMPERIAL.equals(system);

        double grams = toGrams(quantity, unit);
        if (grams >= 0) {
            return imperial ? imperialWeight(grams) : metricWeight(grams);
        }

        double millilitres = toMillilitres(quantity, unit);
        if (millilitres >= 0) {
            return imperial ? imperialVolume(millilitres) : metricVolume(millilitres);
        }

        return round(quantity) + " " + unit;
    }

    private static double toGrams(double quantity, String unit) {
        switch (unit) {
            case "g":
                return quantity;
            case "kg":
                return quantity * 1000;
            case "oz":
                return quantity * GRAMS_PER_OUNCE;
            case "lb":
                return quantity * GRAMS_PER_POUND;
        }
        return -1;
    }

    private static double toMillilitres(double quantity, String unit) {
        switch (unit) {
            case "ml":
                return quantity;
            case "l":
                return quantity * 1000;
            case "fl oz":
                return quantity * ML_PER_FL_OZ;
            case "cup":
                return quantity * ML_PER_CUP;
        }
        return -1;
    }

    private static String metricWeight(double grams) {
        if (grams >= 1000) {
            return round(grams / 1000) + " kg";
        }
        return round(grams) + " g";
    }

    private static String imperialWeight(double grams) {
        if (grams >= GRAMS_PER_POUND) {
            return round(grams / GRAMS_PER_POUND) + " lb";
        }
        return round(grams / GRAMS_PER_OUNCE) + " oz";
    }

    private static String metricVolume(double millilitres) {
        if (millilitres >= 1000) {
            return round(millilitres / 1000) + " l";
        }
        return round(millilitres) + " ml";
    }

    private static String imperialVolume(double millilitres) {
        if (millilitres >= ML_PER_CUP) {
            return round(millilitres / ML_PER_CUP) + " cup";
        }
        return round(millilitres / ML_PER_FL_OZ) + " fl oz";
    }

    private static String round(double value) {
        double rounded = Math.round(value * 10) / 10.0;
        if (rounded == Math.floor(rounded)) {
            return String.valueOf((long) rounded);
        }
        return String.valueOf(rounded);
    }
}
