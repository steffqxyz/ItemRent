package xyz.steffq.itemrent.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ItemRentDatabase {

    private static Connection connection;

    public ItemRentDatabase(String path) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS rented_items (\n" +
                        "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        "    slot INTEGER,\n" +
                        "    price REAL,\n" +
                        "    owner TEXT,\n" +
                        "    item_name TEXT\n" +
                        ");");

            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize the database", e);
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public static Connection getConnection() {
        return connection;
    }

}
