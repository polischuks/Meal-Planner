package mealplanner;

import java.io.IOException;
import java.sql.SQLException;

public class Main {

    public static void main(String[] argv) throws SQLException, IOException {

        new MealPlanner(new DataBase()).init();
    }
}
