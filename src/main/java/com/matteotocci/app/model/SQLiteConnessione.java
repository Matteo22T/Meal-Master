package com.matteotocci.app.model;

import java.sql.*;
public class SQLiteConnessione {
    public static Connection connector() {
        try {
            Class.forName("org.sqlite.JDBC");
            String dbPath = "jdbc:sqlite:database.db";
            Connection conn = DriverManager.getConnection(dbPath);


            // Abilita i vincoli di chiave esterna (ON DELETE CASCADE)
            Statement stmt = conn.createStatement();
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.close();
            return conn;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    }

