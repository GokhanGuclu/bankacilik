package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class databaseconnection {

    private static final String URL = "jdbc:mysql://localhost:3306/bankacilik";
    private static final String USER = "root";
    private static final String PASSWORD = "Gokhan626353";

    public static Connection connect() throws SQLException {
        try {

            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC Driver bulunamadı.", e);
        }
    }
}