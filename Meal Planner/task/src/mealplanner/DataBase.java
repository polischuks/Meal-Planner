package mealplanner;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBase {
    private static Statement statement;
    private Map<Integer, Meal> map = new HashMap<>();

    public void connect() throws SQLException {
        statement = dbInit();
    }

    public void insert(Meal meal) throws SQLException {
        ResultSet rs = statement.executeQuery("select count(*) from meals");
        rs.next();
        int id_meal = rs.getInt(1);
        ResultSet rs2 = statement.executeQuery("select count(*) from ingredients");
        rs2.next();
        int id_ingr = rs2.getInt(1);
        statement.executeUpdate(String.format("INSERT INTO meals (meal_id, category, meal) VALUES (%d,'%s', '%s')",
                id_meal, meal.getCategory(), meal.getName()));

        for (String i : meal.getIngredients()) {
            statement.executeUpdate(String.format("INSERT INTO ingredients (meal_id, ingredient_id, ingredient) VALUES (%d," +
                            "%d,'%s')",
                    id_meal, id_ingr++, i.strip()));
        }
    }

    public List<Meal> readAllMeals() throws SQLException {

        ResultSet resultSet = statement.executeQuery("SELECT * FROM meals");

        map = new HashMap<>();

        while (resultSet.next()) {
            map.put(resultSet.getInt("meal_id"), new Meal(resultSet.getString("category"), resultSet.getString("meal")));
        }
        resultSet = statement.executeQuery("SELECT * FROM ingredients");
        while (resultSet.next()) {
            map.get(resultSet.getInt("meal_id")).addIngredient(resultSet.getString("ingredient"));
        }
        map.forEach((k, v) -> v.setMealId(k));

        return new ArrayList<>(map.values());
    }

    public Connection connection() throws SQLException {
        String DB_URL = "jdbc:postgresql:meals_db";
        String USER = "postgres";
        String PASS = "1111";

        Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
        connection.setAutoCommit(true);
        //if (!connection.isClosed()) System.out.println("Database connected successfully");
        return connection;
    }

    private Statement dbInit() throws SQLException {

        Statement statement = connection().createStatement();
        statement.executeUpdate("create table if not exists meals(" +
                "meal_id integer," +
                "category varchar(1024) NOT NULL," +
                "meal varchar(1024) NOT NULL," +
                "PRIMARY KEY (meal_id)" +
                ");");
        statement.executeUpdate("create table if not exists ingredients(" +
                "meal_id integer," +
                "ingredient_id integer," +
                "ingredient varchar(1024) NOT NULL," +
                "PRIMARY KEY (ingredient_id)," +
                "FOREIGN KEY (meal_id) REFERENCES meals(meal_id)" +
                ");");
        statement.executeUpdate("create table if not exists plan(" +
                "meal_id integer," +
                "day varchar(1024) NOT NULL," +
                "type varchar(1024) NOT NULL," +
                "FOREIGN KEY (meal_id) REFERENCES meals(meal_id)" +
                ");");
        return statement;
    }

    public void show(String ch) throws SQLException {
        Connection connection = connection();
        connection.setAutoCommit(true);

        Statement statement = connection.createStatement();
        Statement statement2 = connection.createStatement();

        ResultSet rs = statement.executeQuery(String.format("select * from meals where category='%s'", ch));
        if (!rs.next()) {
            System.out.println("No meals found.");
            return;
        }

        System.out.println("Category: " + ch);
        do {
            System.out.println("Name: " + rs.getString("meal"));
            System.out.println("Ingredients:");
            ResultSet rs2 = statement2.executeQuery(String.format(
                    "select * from ingredients where meal_id=%d", rs.getInt("meal_id")));
            while (rs2.next()) {
                System.out.println(rs2.getString("ingredient"));
            }
            System.out.println();
        } while (rs.next());
    }

    public Statement getStatement() {
        return statement;
    }

}
