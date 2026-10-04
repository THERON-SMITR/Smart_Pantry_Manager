# Smart Pantry Manager
The Android application helps reduce food waste. 
The user keeps track of the ingredients they already have in their pantry, and the app suggests recipes they can cook using only those ingredients.

- **Student:** Ruan Theron-Smit
- **Repository:** https://github.com/THERON-SMITR/Smart_Pantry_Manager

## Features
- Add, edit and delete pantry items (name, quantity, unit and an optional expiry date).
- Pantry list that is read from the database and shown in a RecyclerView with a custom adapter.
- Input validation on the add/edit form (the name must be filled in and the quantity must be greater than zero).
- Expiry warnings on the pantry list:
  - a red border and the word "Expired" for items that are past their expiry date;
  - an orange border for items that expire within the number of days chosen in Settings.
- 20 pre-loaded recipes, added to the database the first time the app runs.
- Suggested Recipes screen with two tabs:
  - **Ready to cook:** recipes that strictly match the pantry.
  - **Almost there:** recipes that are missing exactly one ingredient. The missing ingredient is shown on the card. This list is kept separate from the strict suggestions.
- A friendly message when no recipes match, instead of an empty screen.
- Recipe Detail screen with the full ingredient list and the method.
- Settings screen:
  - switch expiry alerts on or off;
  - choose how many days before expiry an item is highlighted (1, 2, 3, 5 or 7 days);
  - choose between metric and imperial units.
- Bottom navigation between the Pantry, Suggestions and Settings screens.

## How the strict matching works
The logic is in `RecipeMatcher.java`. For each recipe, the app checks every ingredient against the pantry:

1. The ingredient names are compared in lower case, and a simple plural ending is removed, so "tomato" matches "tomatoes" and "onion" matches "onions".
2. If the units are the same, the pantry quantity must be equal to or more than the quantity the recipe needs.
3. If the units are different but of the same kind, they are converted before comparing. For example, 1 kg of flour in the pantry covers a recipe that needs 500 g. Weight units (g, kg, oz, lb) and volume units (ml, l, fl oz, cup) are converted. Count units (unit, slice, clove, pinch) must be the same.
4. If one ingredient is missing, or there is not enough of it, the whole recipe is left out of the main list.

## Database
The app uses **SQLite**, through the `SQLiteOpenHelper` class (`database/DatabaseHelper.java`).


## Technologies
- Java (no Kotlin)
- Android Studio
- SQLite (`SQLiteOpenHelper`)
- RecyclerView with custom adapters
- Material Components (toolbar, bottom navigation, tabs, cards and text fields)
- ConstraintLayout and LinearLayout
- Intents to move between screens and pass data
- Android Gradle Plugin 9.4.1 and Gradle 9.6.0
- Minimum Android version: API 24 (Android 7.0). Target and compile SDK: 37.

