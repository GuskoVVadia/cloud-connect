/**
 * Класс для подключения к БД.
 * Задача:
 *  1. Получить данные из БД.
 *  2. Сформировать объект типа Map, заполнить его полученными данными.
 *  3. Передать объект.
 */

package ServerNIO.addition;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class BDServer {

    Map<String, String> map;

    public BDServer() {
        this.map = new HashMap<>();
        connect();
    }

    private void connect() {

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection connection = null;

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/CloudBase.db");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM personCloud");

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String pass = resultSet.getString("password");
                this.map.putIfAbsent(name, pass);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, String> getMap() {
        return map;
    }
}
