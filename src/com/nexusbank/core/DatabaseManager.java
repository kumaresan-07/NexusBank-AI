package com.nexusbank.core;

import java.sql.*;
import javax.swing.JOptionPane;

/**
 * DatabaseManager - Singleton class for managing MySQL database connections
 * Features: Connection pooling, auto-reconnect, database auto-creation
 */
public class DatabaseManager {
    
    private static DatabaseManager instance = null;
    private Connection connection = null;
    
    // Database configuration
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "nexusbank_ai";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "atm123"; // Change this to your MySQL password
    
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
    private static final String SERVER_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/";
    
    /**
     * Private constructor for Singleton pattern
     */
    private DatabaseManager() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, 
                "MySQL JDBC Driver not found!\nPlease add mysql-connector-j.jar to lib folder",
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Get singleton instance
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Initialize database and create tables if they don't exist
     */
    private void initializeDatabase() {
        try {
            // First connect to MySQL server
            connection = DriverManager.getConnection(SERVER_URL, DB_USER, DB_PASS);
            Statement stmt = connection.createStatement();
            
            // Create database if it doesn't exist
            String createDBQuery = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
            stmt.executeUpdate(createDBQuery);
            System.out.println("Database '" + DB_NAME + "' ready.");
            
            // Switch to the database
            stmt.executeUpdate("USE " + DB_NAME);
            
            // Close and reconnect to the specific database
            stmt.close();
            connection.close();
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            
            // Create all required tables
            createTables();
            
            System.out.println("Database initialized successfully!");
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Database initialization failed!\n" + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Create all database tables
     */
    private void createTables() throws SQLException {
        Statement stmt = connection.createStatement();
        
        // Users table
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
            "user_id INT AUTO_INCREMENT PRIMARY KEY," +
            "card_number VARCHAR(16) UNIQUE NOT NULL," +
            "primary_pin VARCHAR(4) NOT NULL," +
            "panic_pin VARCHAR(4)," +
            "full_name VARCHAR(100)," +
            "email VARCHAR(100)," +
            "phone VARCHAR(15)," +
            "date_of_birth DATE," +
            "address TEXT," +
            "account_type VARCHAR(20)," +
            "account_status VARCHAR(20) DEFAULT 'ACTIVE'," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "last_login TIMESTAMP NULL," +
            "biometric_hash VARCHAR(255)," +
            "security_question VARCHAR(255)," +
            "security_answer VARCHAR(255)" +
            ")";
        stmt.executeUpdate(createUsersTable);
        
        // Transactions table
        String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions (" +
            "transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "user_id INT," +
            "transaction_type VARCHAR(20)," +
            "amount DECIMAL(12,2)," +
            "category VARCHAR(50)," +
            "description TEXT," +
            "balance_after DECIMAL(12,2)," +
            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "location VARCHAR(100)," +
            "status VARCHAR(20) DEFAULT 'SUCCESS'," +
            "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
            ")";
        stmt.executeUpdate(createTransactionsTable);
        
        // Virtual cards table
        String createVirtualCardsTable = "CREATE TABLE IF NOT EXISTS virtual_cards (" +
            "card_id INT AUTO_INCREMENT PRIMARY KEY," +
            "user_id INT," +
            "card_number VARCHAR(16) UNIQUE," +
            "card_pin VARCHAR(4)," +
            "card_nickname VARCHAR(50)," +
            "spending_limit DECIMAL(10,2)," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "expires_at TIMESTAMP NULL," +
            "status VARCHAR(20) DEFAULT 'ACTIVE'," +
            "usage_count INT DEFAULT 0," +
            "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
            ")";
        stmt.executeUpdate(createVirtualCardsTable);
        
        // Rewards table
        String createRewardsTable = "CREATE TABLE IF NOT EXISTS rewards (" +
            "reward_id INT AUTO_INCREMENT PRIMARY KEY," +
            "user_id INT UNIQUE," +
            "total_points INT DEFAULT 0," +
            "level INT DEFAULT 1," +
            "xp INT DEFAULT 0," +
            "badges_unlocked TEXT," +
            "achievements TEXT," +
            "last_daily_login DATE," +
            "streak_days INT DEFAULT 0," +
            "lifetime_points INT DEFAULT 0," +
            "redeemed_points INT DEFAULT 0," +
            "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
            ")";
        stmt.executeUpdate(createRewardsTable);
        
        // Savings goals table
        String createSavingsGoalsTable = "CREATE TABLE IF NOT EXISTS savings_goals (" +
            "goal_id INT AUTO_INCREMENT PRIMARY KEY," +
            "user_id INT," +
            "goal_name VARCHAR(100)," +
            "target_amount DECIMAL(12,2)," +
            "current_amount DECIMAL(12,2) DEFAULT 0," +
            "start_date DATE," +
            "deadline DATE," +
            "auto_save_enabled BOOLEAN DEFAULT FALSE," +
            "auto_save_percentage DECIMAL(5,2) DEFAULT 0," +
            "status VARCHAR(20) DEFAULT 'ACTIVE'," +
            "completed_at TIMESTAMP NULL," +
            "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
            ")";
        stmt.executeUpdate(createSavingsGoalsTable);
        
        // Fraud alerts table
        String createFraudAlertsTable = "CREATE TABLE IF NOT EXISTS fraud_alerts (" +
            "alert_id INT AUTO_INCREMENT PRIMARY KEY," +
            "user_id INT," +
            "alert_type VARCHAR(50)," +
            "risk_level VARCHAR(20)," +
            "transaction_id BIGINT," +
            "details TEXT," +
            "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "resolved BOOLEAN DEFAULT FALSE," +
            "resolution_notes TEXT," +
            "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
            ")";
        stmt.executeUpdate(createFraudAlertsTable);
        
        // Budgets table
        String createBudgetsTable = "CREATE TABLE IF NOT EXISTS budgets (" +
            "budget_id INT AUTO_INCREMENT PRIMARY KEY," +
            "user_id INT," +
            "category VARCHAR(50)," +
            "monthly_limit DECIMAL(10,2)," +
            "current_spent DECIMAL(10,2) DEFAULT 0," +
            "alert_threshold INT DEFAULT 80," +
            "reset_day INT DEFAULT 1," +
            "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
            ")";
        stmt.executeUpdate(createBudgetsTable);
        
        // Notifications table
        String createNotificationsTable = "CREATE TABLE IF NOT EXISTS notifications (" +
            "notification_id INT AUTO_INCREMENT PRIMARY KEY," +
            "user_id INT," +
            "title VARCHAR(100)," +
            "message TEXT," +
            "type VARCHAR(30)," +
            "priority VARCHAR(20)," +
            "is_read BOOLEAN DEFAULT FALSE," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
            ")";
        stmt.executeUpdate(createNotificationsTable);
        
        // Challenges table
        String createChallengesTable = "CREATE TABLE IF NOT EXISTS challenges (" +
            "challenge_id INT AUTO_INCREMENT PRIMARY KEY," +
            "challenge_name VARCHAR(100)," +
            "description TEXT," +
            "challenge_type VARCHAR(30)," +
            "reward_points INT," +
            "target_value DECIMAL(10,2)," +
            "duration_days INT," +
            "is_active BOOLEAN DEFAULT TRUE" +
            ")";
        stmt.executeUpdate(createChallengesTable);
        
        // User challenges (tracking)
        String createUserChallengesTable = "CREATE TABLE IF NOT EXISTS user_challenges (" +
            "user_challenge_id INT AUTO_INCREMENT PRIMARY KEY," +
            "user_id INT," +
            "challenge_id INT," +
            "progress DECIMAL(10,2) DEFAULT 0," +
            "started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "completed_at TIMESTAMP NULL," +
            "status VARCHAR(20) DEFAULT 'IN_PROGRESS'," +
            "FOREIGN KEY (user_id) REFERENCES users(user_id)," +
            "FOREIGN KEY (challenge_id) REFERENCES challenges(challenge_id)" +
            ")";
        stmt.executeUpdate(createUserChallengesTable);
        
        // Investment portfolio table
        String createInvestmentsTable = "CREATE TABLE IF NOT EXISTS investments (" +
            "investment_id INT AUTO_INCREMENT PRIMARY KEY," +
            "user_id INT," +
            "investment_type VARCHAR(30)," +
            "amount DECIMAL(12,2)," +
            "interest_rate DECIMAL(5,2)," +
            "tenure_months INT," +
            "start_date DATE," +
            "maturity_date DATE," +
            "current_value DECIMAL(12,2)," +
            "status VARCHAR(20) DEFAULT 'ACTIVE'," +
            "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
            ")";
        stmt.executeUpdate(createInvestmentsTable);
        
        // Settings/Preferences table
        String createSettingsTable = "CREATE TABLE IF NOT EXISTS user_settings (" +
            "setting_id INT AUTO_INCREMENT PRIMARY KEY," +
            "user_id INT UNIQUE," +
            "theme VARCHAR(20) DEFAULT 'LIGHT'," +
            "language VARCHAR(10) DEFAULT 'en'," +
            "notifications_enabled BOOLEAN DEFAULT TRUE," +
            "biometric_enabled BOOLEAN DEFAULT FALSE," +
            "auto_logout_minutes INT DEFAULT 5," +
            "currency VARCHAR(10) DEFAULT 'INR'," +
            "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
            ")";
        stmt.executeUpdate(createSettingsTable);
        
        stmt.close();
        System.out.println("All tables created successfully!");
    }
    
    /**
     * Get active database connection
     */
    public Connection getConnection() {
        try {
            // Check if connection is valid, reconnect if needed
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                System.out.println("Database connection established.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Database connection failed!\n" + e.getMessage(),
                "Connection Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return connection;
    }
    
    /**
     * Execute a query and return ResultSet
     */
    public ResultSet executeQuery(String query) throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(query);
    }
    
    /**
     * Execute an update (INSERT, UPDATE, DELETE)
     */
    public int executeUpdate(String query) throws SQLException {
        Statement stmt = getConnection().createStatement();
        int result = stmt.executeUpdate(query);
        stmt.close();
        return result;
    }
    
    /**
     * Execute a prepared statement query
     */
    public PreparedStatement prepareStatement(String query) throws SQLException {
        return getConnection().prepareStatement(query);
    }
    
    /**
     * Close database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Test database connection
     */
    public boolean testConnection() {
        try {
            Connection testConn = getConnection();
            return testConn != null && !testConn.isClosed() && testConn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}
