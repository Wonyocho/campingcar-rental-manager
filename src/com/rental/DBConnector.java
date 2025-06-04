package com.rental;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    public static Connection getConnection(boolean isAdmin) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/DBTEST";
        String user, password;
        if (isAdmin) {
            user = "root";
            password = "1234";
        } else {
            user = "user1";
            password = "user1";
        }
        return DriverManager.getConnection(url, user, password);
    }
}
