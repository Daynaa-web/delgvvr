package com.example;

/** Нэвтэрсэн хэрэглэгчийн мэдээллийг хадгална. */
public class Session {
    private static int userId;
    private static String username;
    private static String role;

    public static void login(int id, String name, String userRole) {
        userId = id;
        username = name;
        role = userRole;
    }

    public static void logout() {
        userId = 0;
        username = null;
        role = null;
    }

    public static int getUserId() { return userId; }
    public static String getUsername() { return username; }
    public static String getRole() { return role; }
}
