/**
 * Класс для подключения к БД.
 * Задача:
 *  1. Получить данные из БД.
 *  2. Сформировать объект типа Map, заполнить его полученными данными.
 *  3. Передать объект.
 */

package addition;

import javafx.util.Pair;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class BDServer {

    private Map<String, Pair<Integer, String>> map;

    public BDServer() {
        this.map = new HashMap<>();
        connect();
    }

    public BDServer(String nameCl, String pathNew){
        editBD(nameCl, pathNew);
    }

    private void editBD(String nameCl, String pathNew) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection connection = null;

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:ServerNIO/src/main/resources/CloudBase.db");
            PreparedStatement statement = connection.prepareStatement("UPDATE personCloud SET" +
                    " path = ? WHERE name = ?");
            statement.setString(1, pathNew);
            statement.setString(2, nameCl);
            statement.executeUpdate();

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

    private void connect() {

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection connection = null;

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:ServerNIO/src/main/resources/CloudBase.db");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM personCloud");

            while (resultSet.next()) {
                String nameBD = resultSet.getString("name");
                Integer passBD = resultSet.getInt("pass");
                String pathBD = resultSet.getString("path");
                this.map.putIfAbsent(nameBD, new Pair<>(passBD, pathBD));
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

    public Map<String, Pair<Integer, String>> getMap() {
        return map;
    }

}
