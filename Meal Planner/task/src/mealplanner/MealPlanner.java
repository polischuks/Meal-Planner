package mealplanner;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

public class MealPlanner {
    private ArrayList<Meal> mealArrayList = new ArrayList<>();
    private ArrayList<String> category = new ArrayList<>();
    private ArrayList<String> operation = new ArrayList<>();
    private DataBase dataBase;
    private static Scanner sc = new Scanner(System.in);

    public MealPlanner(DataBase dataBase) throws SQLException {
        category.add("breakfast");
        category.add("lunch");
        category.add("dinner");

        operation.add("add");
        operation.add("show");
        operation.add("exit");
        this.dataBase = dataBase;
        dataBase.connect();
        mealArrayList = (ArrayList<Meal>) dataBase.readAllMeals();
    }

    public void init() throws SQLException, IOException {
        boolean isRun = true;
        while (isRun) {
            System.out.println("What would you like to do (add, show, plan, save, exit)?");
            String option = sc.nextLine();
            switch (option) {
                case "add": {
                    addMeal();
                    break;
                }
                case "show": {
                    showMeal();
                    break;
                }
                case "plan" : {
                    plan();
                    break;
                }
                case "save":
                    save();
                    break;
                case "exit": {
                    System.out.print("Bye!");
                    isRun = false;
                    System.exit(0);
                }
            }
        }
    }

    public void save() throws SQLException, IOException {
        Connection connection = dataBase.connection();
        connection.setAutoCommit(true);

        Statement statement = connection.createStatement();
        Statement statement2 = connection.createStatement();

        Map<String, Integer> map = new HashMap<>();

        ResultSet rs = statement.executeQuery("select * from plan");
        while (rs.next()) {
            int id = rs.getInt("meal_id");
            ResultSet rs2 = statement2.executeQuery("select * from ingredients where meal_id="+id);
            while(rs2.next()){
                String ingredient = rs2.getString("ingredient");
                if(map.containsKey(ingredient)){
                    map.put(ingredient, map.get(ingredient)+1);
                }else{
                    map.put(ingredient, 1);
                }
            }
        }
        if(map.keySet().isEmpty()){
            System.out.println("Unable to save. Plan your meals first.");
            return;
        }
        System.out.println("Input a filename:");
        Scanner scanner = new Scanner(System.in);
        String fileName = scanner.nextLine();
        FileWriter fileWriter = new FileWriter(fileName);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        for(String key : map.keySet()){
            if(map.get(key)==1){
                printWriter.print(key+"\n");
            }else{
                printWriter.print(key+" x"+map.get(key)+"\n");
            }
        }
        printWriter.close();
        System.out.println("Saved!");
    }

    public void plan() throws SQLException {
        mealArrayList = (ArrayList<Meal>) dataBase.readAllMeals();
        dataBase.getStatement().execute("TRUNCATE TABLE plan");
        Scanner scanner = new Scanner(System.in);

        Connection connection = dataBase.connection();
        connection.setAutoCommit(true);

        Statement statement = connection.createStatement();
        Statement statement2 = connection.createStatement();

        Map<String, Integer> breakfast = new HashMap<>();
        ResultSet rs = statement.executeQuery("select * from meals where category='breakfast'");
        while (rs.next()) {
            int id = rs.getInt("meal_id");
            String meal = rs.getString("meal");
            breakfast.put(meal,id);
        }

        Map<String, Integer> lunch = new HashMap<>();
        rs = statement.executeQuery("select * from meals where category='lunch'");
        while (rs.next()) {
            int id = rs.getInt("meal_id");
            String meal = rs.getString("meal");
            lunch.put(meal,id);
        }

        Map<String, Integer> dinner = new HashMap<>();
        rs = statement.executeQuery("select * from meals where category='dinner'");
        while (rs.next()) {
            int id = rs.getInt("meal_id");
            String meal = rs.getString("meal");
            dinner.put(meal,id);
        }

        Map<String, Map<String, Integer>> types = Map.of("breakfast", breakfast, "lunch", lunch, "dinner", dinner);

        for (String day : new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"}) {
            System.out.println(day);
            Iterator<String> iterator = types.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                for (Object meal : types.get(key).keySet().stream().sorted().toArray()) {
                    System.out.println(meal);
                }
                System.out.println(String.format("Choose the %s for %s from the list above:", key, day));
                String ch = "";
                while (true) {
                    ch = scanner.nextLine();
                    if (types.get(key).keySet().contains(ch)) {
                        break;
                    }
                    System.out.println("This meal doesn’t exist. Choose a meal from the list above.");
                }
                statement.executeUpdate(String.format("INSERT INTO plan (meal_id, day, type) VALUES (%d,'%s', '%s')",
                        types.get(key).get(ch), day, key));
            }
            System.out.println(String.format("Yeah! We planned the meals for %s.",day));
        }
        for (String day : new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"}) {
            System.out.println(day);
            rs = statement.executeQuery("select * from plan where (type='breakfast' and day='"+day+"')");
            while (rs.next()) {
                int id = rs.getInt("meal_id");
                ResultSet rs2 = statement2.executeQuery("select * from meals where meal_id="+id);
                while(rs2.next()){
                    String meal = rs2.getString("meal");
                    System.out.println("Breakfast: "+meal);
                }
            }
            rs = statement.executeQuery("select * from plan where (type='lunch' and day='"+day+"')");
            while (rs.next()) {
                int id = rs.getInt("meal_id");
                ResultSet rs2 = statement2.executeQuery("select * from meals where meal_id="+id);
                while(rs2.next()){
                    String meal = rs2.getString("meal");
                    System.out.println("Lunch: "+meal);
                }
            }
            rs = statement.executeQuery("select * from plan where (type='dinner' and day='"+day+"')");
            while (rs.next()) {
                int id = rs.getInt("meal_id");
                ResultSet rs2 = statement2.executeQuery("select * from meals where meal_id="+id);
                while(rs2.next()){
                    String meal = rs2.getString("meal");
                    System.out.println("Dinner: "+meal);
                }
            }
        }
    }

    private void showMeal() throws SQLException {
        System.out.println("Which category do you want to print (breakfast, lunch, dinner)?");
        String category = sc.nextLine();
        while (!(category.equals("lunch") || category.equals("breakfast") || category.equals("dinner"))) {
            System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
            category = sc.nextLine();
        }
        dataBase.show(category);
    }

    private void addMeal() throws SQLException {
        Meal meal = new Meal(inputCategory(), inputNameMeal(), inputIngredient());
        mealArrayList.add(meal);
        dataBase.insert(meal);
        System.out.println("The meal has been added!");
    }

    private String inputNameMeal() {
        Pattern pattern = Pattern.compile("[a-zA-Z]+\\s*[a-zA-Z]*");
        String name;
        System.out.println("Input the meal's name:");
        name = sc.nextLine();
        while (!pattern.matcher(name).matches()) {
            System.out.println("Wrong format. Use letters only!");
            name = sc.nextLine();
        }
        return name;
    }

    private ArrayList<String> inputIngredient() {
        Pattern pattern = Pattern.compile("([a-zA-Z][a-zA-Z ]*,\\s*)*[a-zA-Z][a-zA-Z ]+");
        ArrayList<String> ingredients = new ArrayList<>();
        System.out.println("Input the ingredients:");
        String ingredient = sc.nextLine();
        while (!pattern.matcher(ingredient).matches()) {
            System.out.println("Wrong format. Use letters only!");
            ingredient = sc.nextLine();
        }
        String[] input = ingredient.split(",\\s*");
        for (String s : input) {
            ingredients.add(s.trim());
        }
        return ingredients;
    }

    private String inputCategory() {
        String cat = "";
        System.out.println("Which meal do you want to add (breakfast, lunch, dinner)?");
        cat = sc.nextLine();
        while ((!cat.equals("lunch") && !cat.equals("breakfast") && !cat.equals("dinner"))) {
            System.out.println("Wrong meal category! Choose from: breakfast, lunch, dinner.");
            cat = sc.nextLine();
        }
        return cat;
    }
}
