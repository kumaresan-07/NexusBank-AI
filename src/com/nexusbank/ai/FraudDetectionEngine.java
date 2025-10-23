package com.nexusbank.ai;

import com.nexusbank.core.DatabaseManager;
import java.sql.*;
import java.util.Date;

/**
 * FraudDetectionEngine - Simple rule-based fraud detection with hooks for ML model integration
 */
public class FraudDetectionEngine {

    private DatabaseManager db;

    public FraudDetectionEngine() {
        this.db = DatabaseManager.getInstance();
    }

    /**
     * Analyze a transaction and record an alert in fraud_alerts if suspicious
     * Rules implemented:
     * 1. Amount greater than 3x average deposit for that user -> MEDIUM risk
     * 2. More than 3 transactions within 60 seconds -> HIGH risk
     * 3. Withdrawal that causes negative balance -> CRITICAL
     */
    public void analyzeTransaction(int userId, long transactionId, double amount, String type) {
        try {
            double avgDeposit = getAverageDeposit(userId);
            int recentCount = getRecentTransactionCount(userId, 60); // last 60 seconds
            double balance = getCurrentBalance(userId);

            String risk = "LOW";
            String alertType = "NONE";
            String details = "";

            if (type.equalsIgnoreCase("WITHDRAW") || type.equalsIgnoreCase("FASTCASH")) {
                if (amount > avgDeposit * 3 && avgDeposit > 0) {
                    risk = "MEDIUM";
                    alertType = "UNUSUAL_AMOUNT";
                    details = String.format("Withdrawal of %.2f is >3x avg deposit(%.2f)", amount, avgDeposit);
                }
                if (recentCount > 3) {
                    risk = "HIGH";
                    alertType = "RAPID_TRANSACTIONS";
                    details = String.format("%d transactions in last 60s", recentCount);
                }
                if (balance - amount < 0) {
                    risk = "CRITICAL";
                    alertType = "OVERDRAFT";
                    details = String.format("Transaction would cause negative balance (current: %.2f, amount: %.2f)", balance, amount);
                }
            } else if (type.equalsIgnoreCase("DEPOSIT") || type.equalsIgnoreCase("CASHBACK")) {
                // Large deposit might be suspicious if exceeds threshold
                if (amount > 100000) { // arbitrary large
                    risk = "MEDIUM";
                    alertType = "LARGE_DEPOSIT";
                    details = String.format("Large deposit: %.2f", amount);
                }
            }

            if (!risk.equals("LOW")) {
                insertFraudAlert(userId, transactionId, alertType, risk, details);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private double getAverageDeposit(int userId) throws SQLException {
        String q = "SELECT AVG(amount) as avgd FROM transactions WHERE user_id=" + userId + " AND transaction_type='DEPOSIT'";
        ResultSet rs = db.executeQuery(q);
        double avg = 0;
        if (rs.next()) {
            avg = rs.getDouble("avgd");
        }
        rs.close();
        return avg;
    }

    private int getRecentTransactionCount(int userId, int seconds) throws SQLException {
        String q = "SELECT COUNT(*) as cnt FROM transactions WHERE user_id=" + userId + " AND timestamp >= (NOW() - INTERVAL " + seconds + " SECOND)";
        ResultSet rs = db.executeQuery(q);
        int cnt = 0;
        if (rs.next()) cnt = rs.getInt("cnt");
        rs.close();
        return cnt;
    }

    private double getCurrentBalance(int userId) throws SQLException {
        String q = "SELECT SUM(CASE WHEN transaction_type IN ('DEPOSIT','CASHBACK') THEN amount ELSE -amount END) as bal FROM transactions WHERE user_id=" + userId;
        ResultSet rs = db.executeQuery(q);
        double bal = 0;
        if (rs.next()) bal = rs.getDouble("bal");
        rs.close();
        return bal;
    }

    private void insertFraudAlert(int userId, long transactionId, String alertType, String risk, String details) throws SQLException {
        String q = "INSERT INTO fraud_alerts (user_id, alert_type, risk_level, transaction_id, details) VALUES (?,?,?,?,?)";
        PreparedStatement ps = db.prepareStatement(q);
        ps.setInt(1, userId);
        ps.setString(2, alertType);
        ps.setString(3, risk);
        if (transactionId > 0) ps.setLong(4, transactionId); else ps.setNull(4, Types.BIGINT);
        ps.setString(5, details);
        ps.executeUpdate();
        ps.close();

        System.out.println("[FraudDetection] Alert inserted for user " + userId + " risk=" + risk + " type=" + alertType + " details=" + details);
    }
}
