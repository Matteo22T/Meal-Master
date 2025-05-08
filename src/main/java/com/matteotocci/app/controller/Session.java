package com.matteotocci.app.controller;

public class Session {
    private static Integer currentUserId;

    public static void setUserId(Integer id) {
        currentUserId = id;
    }

    public static Integer getUserId() {
        return currentUserId;
    }
    public static void clear() {
        currentUserId = null;
    }
}
