package com.nexusbank.core;

public class TestDatabase {
    public static void main(String[] args) {
        DatabaseManager db = DatabaseManager.getInstance();
        boolean ok = db.testConnection();
        System.out.println(ok ? "✅ Database connected and initialized!" : "❌ Database connection failed");
        // Close explicitly to release resources
        db.closeConnection();
    }
}
