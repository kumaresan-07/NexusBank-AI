package com.nexusbank.ai;

import com.nexusbank.core.DatabaseManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * TestFraud - inserts a sample suspicious transaction and runs the analysis
 */
public class TestFraud {
    public static void main(String[] args) {
        // Prepare: create a user entry if not exist, add deposits
        DatabaseManager db = DatabaseManager.getInstance();
        try {
            // Create demo user
            String card = "9999888877776666";
            String pin = "1234";
            String check = "SELECT user_id FROM users WHERE card_number='" + card + "'";
            ResultSet rs = db.executeQuery(check);
            int userId = -1;
            if (rs.next()) {
                userId = rs.getInt("user_id");
            } else {
                rs.close();
                String ins = "INSERT INTO users (card_number, primary_pin, full_name) VALUES (?,?,?)";
                PreparedStatement ps = db.prepareStatement(ins);
                ps.setString(1, card);
                ps.setString(2, pin);
                ps.setString(3, "Demo Fraud User");
                ps.executeUpdate();
                ps.close();
                rs = db.executeQuery(check);
                if (rs.next()) userId = rs.getInt("user_id");
            }
            if (rs != null && !rs.isClosed()) rs.close();

            // Add normal deposit history
            String dep = "INSERT INTO transactions (user_id, transaction_type, amount, category, description) VALUES (?,?,?,?,?)";
            PreparedStatement p2 = db.prepareStatement(dep);
            p2.setInt(1, userId);
            p2.setString(2, "DEPOSIT");
            p2.setDouble(3, 500.0);
            p2.setString(4, "SALARY");
            p2.setString(5, "Monthly salary demo");
            p2.executeUpdate();
            p2.close();

            // Add suspicious withdrawal (capture generated key via connection)
            String w = "INSERT INTO transactions (user_id, transaction_type, amount, category, description) VALUES (?,?,?,?,?)";
            PreparedStatement p3 = db.getConnection().prepareStatement(w, Statement.RETURN_GENERATED_KEYS);
            p3.setInt(1, userId);
            p3.setString(2, "WITHDRAW");
            p3.setDouble(3, 5000.0); // big relative to avg deposit
            p3.setString(4, "ATM");
            p3.setString(5, "Large withdrawal test");
            p3.executeUpdate();
            ResultSet keys = p3.getGeneratedKeys();
            long txnId = -1;
            if (keys != null && keys.next()) txnId = keys.getLong(1);
            if (keys != null && !keys.isClosed()) keys.close();
            p3.close();

            // Run analysis
            FraudDetectionEngine engine = new FraudDetectionEngine();
            engine.analyzeTransaction(userId, txnId, 5000.0, "WITHDRAW");

            System.out.println("Test complete. Check fraud_alerts table.");
            db.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
