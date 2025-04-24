package com.matteotocci.app.model;

import java.sql.*;
public class SQLiteConnessione {
    public static Connection connector() {
        try {
            Class.forName("org.sqlite.JDBC");
            String dbPath = "jdbc:sqlite:C:/Users/Dario De Paola/IdeaProjects/Meal-Master/database.db";
            Connection conn = DriverManager.getConnection(dbPath);
            return conn;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    }

