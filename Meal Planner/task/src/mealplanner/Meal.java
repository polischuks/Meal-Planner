package mealplanner;

import java.util.ArrayList;

public class Meal {
    private int mealId;
    private String name;
    private String category;
    private ArrayList<String> ingredients = new ArrayList<String>();

    public Meal(String category, String name, ArrayList<String> ingredients){

        this.name = name;
        this.category = category;
        this.ingredients = ingredients;
    }

    public Meal(String category, String name){
        this.name = name;
        this.category = category;
    }

    public int getMealId() {
        return mealId;
    }

    public void setMealId(int mealId) {
        this.mealId = mealId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<String> ingredients) {
        this.ingredients = ingredients;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb//.append("Category: ").append(category)
                .append("\nName: ").append(name)
                .append("\nIngredients: \n");
        ingredients.forEach(s -> sb.append(s).append("\n"));
        return sb.toString();
    }

    public void addIngredient(String ingredient) {
        ingredients.add(ingredient);
    }
}

