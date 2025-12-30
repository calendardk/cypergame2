package com.cybergame.repository.sql;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL =
        "jdbc:mysql://localhost:3306/cybergame?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
