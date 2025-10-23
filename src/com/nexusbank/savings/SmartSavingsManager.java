package com.nexusbank.savings;

import com.nexusbank.core.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * SmartSavingsManager - Manages savings goals, automated transfers, and progress tracking
 */
public class SmartSavingsManager {
    
    private int userId;
    private DatabaseManager db;
    
    public SmartSavingsManager(int userId) {
        this.userId = userId;
        this.db = DatabaseManager.getInstance();
    }
    
    /**
     * Create a new savings goal
     */
    public Map<String, Object> createSavingsGoal(String goalName, String description, double targetAmount, 
                                                String targetDate, String category, boolean autoTransfer, double transferAmount) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String sql = "INSERT INTO savings_goals (user_id, goal_name, target_amount, " +
                        "current_amount, deadline, auto_save_enabled, auto_save_percentage, " +
                        "status, start_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, userId);
            pstmt.setString(2, goalName);
            pstmt.setDouble(3, targetAmount);
            pstmt.setDouble(4, 0.0); // Starting amount
            pstmt.setString(5, targetDate);
            pstmt.setBoolean(6, autoTransfer);
            pstmt.setDouble(7, transferAmount); // Using auto_save_percentage for transfer amount
            pstmt.setString(8, "ACTIVE");
            pstmt.setDate(9, java.sql.Date.valueOf(LocalDateTime.now().toLocalDate())); // start_date
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int goalId = rs.getInt(1);
                    
                    result.put("success", true);
                    result.put("goalId", goalId);
                    result.put("message", "Savings goal created successfully!");
                    
                    System.out.println("Savings goal created: " + goalName + " for user " + userId);
                }
                rs.close();
            }
            pstmt.close();
            
        } catch (SQLException e) {
            result.put("success", false);
            result.put("message", "Error creating savings goal: " + e.getMessage());
            System.err.println("Error creating savings goal: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get all savings goals for the user
     */
    public List<Map<String, Object>> getUserSavingsGoals() {
        List<Map<String, Object>> goals = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM savings_goals WHERE user_id = ? ORDER BY start_date DESC";
            PreparedStatement pstmt = db.getConnection().prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> goal = new HashMap<>();
                goal.put("goalId", rs.getInt("goal_id"));
                goal.put("goalName", rs.getString("goal_name"));
                goal.put("description", "Savings Goal"); // Default description since column doesn't exist
                goal.put("targetAmount", rs.getDouble("target_amount"));
                goal.put("currentAmount", rs.getDouble("current_amount"));
                goal.put("targetDate", rs.getString("deadline"));
                goal.put("category", "General"); // Default category since column doesn't exist
                goal.put("autoTransfer", rs.getBoolean("auto_save_enabled"));
                goal.put("transferAmount", rs.getDouble("auto_save_percentage"));
                goal.put("status", rs.getString("status"));
                goal.put("createdAt", rs.getDate("start_date"));
                
                // Calculate progress percentage
                double progress = (rs.getDouble("current_amount") / rs.getDouble("target_amount")) * 100;
                goal.put("progressPercentage", Math.min(100.0, progress));
                
                goals.add(goal);
            }
            
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            System.err.println("Error fetching savings goals: " + e.getMessage());
        }
        
        return goals;
    }
    
    /**
     * Add money to a savings goal
     */
    public Map<String, Object> addToSavingsGoal(int goalId, double amount, String description) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // First check if goal exists and is active
            String checkSql = "SELECT * FROM savings_goals WHERE goal_id = ? AND user_id = ? AND status = 'ACTIVE'";
            PreparedStatement checkStmt = db.getConnection().prepareStatement(checkSql);
            checkStmt.setInt(1, goalId);
            checkStmt.setInt(2, userId);
            
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                result.put("success", false);
                result.put("message", "Goal not found or inactive");
                rs.close();
                checkStmt.close();
                return result;
            }
            
            String goalName = rs.getString("goal_name");
            double currentAmount = rs.getDouble("current_amount");
            double targetAmount = rs.getDouble("target_amount");
            
            rs.close();
            checkStmt.close();
            
            // Update the goal amount
            double newAmount = currentAmount + amount;
            String updateSql = "UPDATE savings_goals SET current_amount = ? WHERE goal_id = ?";
            PreparedStatement updateStmt = db.getConnection().prepareStatement(updateSql);
            updateStmt.setDouble(1, newAmount);
            updateStmt.setInt(2, goalId);
            updateStmt.executeUpdate();
            updateStmt.close();
            
            // Record the transaction
            String transSql = "INSERT INTO transactions (user_id, transaction_type, amount, description, timestamp) " +
                             "VALUES (?, 'SAVINGS', ?, ?, ?)";
            PreparedStatement transStmt = db.getConnection().prepareStatement(transSql);
            transStmt.setInt(1, userId);
            transStmt.setDouble(2, -amount); // Negative because money goes to savings
            transStmt.setString(3, "Savings Goal: " + goalName + " - " + description);
            transStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            transStmt.executeUpdate();
            transStmt.close();
            
            // Check if goal is completed
            boolean goalCompleted = newAmount >= targetAmount;
            if (goalCompleted) {
                String completeSql = "UPDATE savings_goals SET status = 'COMPLETED' WHERE goal_id = ?";
                PreparedStatement completeStmt = db.getConnection().prepareStatement(completeSql);
                completeStmt.setInt(1, goalId);
                completeStmt.executeUpdate();
                completeStmt.close();
            }
            
            result.put("success", true);
            result.put("newAmount", newAmount);
            result.put("goalCompleted", goalCompleted);
            result.put("progressPercentage", Math.min(100.0, (newAmount / targetAmount) * 100));
            result.put("message", goalCompleted ? "Congratulations! Goal completed!" : "Amount added successfully!");
            
            System.out.println("Added ₹" + amount + " to savings goal: " + goalName);
            
        } catch (SQLException e) {
            result.put("success", false);
            result.put("message", "Error adding to savings goal: " + e.getMessage());
            System.err.println("Error adding to savings goal: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Process automatic transfers for goals with auto-transfer enabled
     */
    public Map<String, Object> processAutoTransfers() {
        Map<String, Object> result = new HashMap<>();
        int transfersProcessed = 0;
        double totalTransferred = 0;
        
        try {
            String sql = "SELECT * FROM savings_goals WHERE user_id = ? AND auto_save_enabled = true AND status = 'ACTIVE'";
            PreparedStatement pstmt = db.getConnection().prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int goalId = rs.getInt("goal_id");
                String goalName = rs.getString("goal_name");
                double transferAmount = rs.getDouble("auto_save_percentage");
                double currentAmount = rs.getDouble("current_amount");
                double targetAmount = rs.getDouble("target_amount");
                
                // Only transfer if goal is not yet completed
                if (currentAmount < targetAmount) {
                    // Adjust transfer amount if it would exceed the target
                    double remainingAmount = targetAmount - currentAmount;
                    double actualTransfer = Math.min(transferAmount, remainingAmount);
                    
                    Map<String, Object> transferResult = addToSavingsGoal(goalId, actualTransfer, "Auto Transfer");
                    
                    if ((Boolean) transferResult.get("success")) {
                        transfersProcessed++;
                        totalTransferred += actualTransfer;
                        System.out.println("Auto transfer processed: ₹" + actualTransfer + " to " + goalName);
                    }
                }
            }
            
            rs.close();
            pstmt.close();
            
            result.put("success", true);
            result.put("transfersProcessed", transfersProcessed);
            result.put("totalTransferred", totalTransferred);
            result.put("message", "Processed " + transfersProcessed + " auto transfers totaling ₹" + totalTransferred);
            
        } catch (SQLException e) {
            result.put("success", false);
            result.put("message", "Error processing auto transfers: " + e.getMessage());
            System.err.println("Error processing auto transfers: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get savings statistics for the user
     */
    public Map<String, Object> getSavingsStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            String sql = "SELECT " +
                        "COUNT(*) as total_goals, " +
                        "SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) as active_goals, " +
                        "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_goals, " +
                        "SUM(target_amount) as total_target, " +
                        "SUM(current_amount) as total_saved, " +
                        "AVG(current_amount / target_amount * 100) as avg_progress " +
                        "FROM savings_goals WHERE user_id = ?";
            
            PreparedStatement pstmt = db.getConnection().prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                stats.put("totalGoals", rs.getInt("total_goals"));
                stats.put("activeGoals", rs.getInt("active_goals"));
                stats.put("completedGoals", rs.getInt("completed_goals"));
                stats.put("totalTarget", rs.getDouble("total_target"));
                stats.put("totalSaved", rs.getDouble("total_saved"));
                stats.put("averageProgress", rs.getDouble("avg_progress"));
                
                // Calculate overall progress
                double overallProgress = 0;
                if (rs.getDouble("total_target") > 0) {
                    overallProgress = (rs.getDouble("total_saved") / rs.getDouble("total_target")) * 100;
                }
                stats.put("overallProgress", Math.min(100.0, overallProgress));
            }
            
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            System.err.println("Error getting savings statistics: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Update savings goal
     */
    public boolean updateSavingsGoal(int goalId, String goalName, String description, double targetAmount, String targetDate) {
        try {
            String sql = "UPDATE savings_goals SET goal_name = ?, target_amount = ?, deadline = ? " +
                        "WHERE goal_id = ? AND user_id = ?";
            PreparedStatement pstmt = db.getConnection().prepareStatement(sql);
            pstmt.setString(1, goalName);
            pstmt.setDouble(2, targetAmount);
            pstmt.setString(3, targetDate);
            pstmt.setInt(4, goalId);
            pstmt.setInt(5, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating savings goal: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete/Cancel a savings goal
     */
    public boolean deleteSavingsGoal(int goalId) {
        try {
            // First check if there are any savings in the goal
            String checkSql = "SELECT current_amount, goal_name FROM savings_goals WHERE goal_id = ? AND user_id = ?";
            PreparedStatement checkStmt = db.getConnection().prepareStatement(checkSql);
            checkStmt.setInt(1, goalId);
            checkStmt.setInt(2, userId);
            
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                double currentAmount = rs.getDouble("current_amount");
                String goalName = rs.getString("goal_name");
                
                if (currentAmount > 0) {
                    // Return money to main account
                    String returnSql = "INSERT INTO transactions (user_id, transaction_type, amount, description, timestamp) " +
                                      "VALUES (?, 'WITHDRAWAL', ?, ?, ?)";
                    PreparedStatement returnStmt = db.getConnection().prepareStatement(returnSql);
                    returnStmt.setInt(1, userId);
                    returnStmt.setDouble(2, currentAmount);
                    returnStmt.setString(3, "Savings Goal Cancelled: " + goalName + " - Amount returned");
                    returnStmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    returnStmt.executeUpdate();
                    returnStmt.close();
                }
            }
            rs.close();
            checkStmt.close();
            
            // Delete the goal
            String deleteSql = "DELETE FROM savings_goals WHERE goal_id = ? AND user_id = ?";
            PreparedStatement deleteStmt = db.getConnection().prepareStatement(deleteSql);
            deleteStmt.setInt(1, goalId);
            deleteStmt.setInt(2, userId);
            
            int rowsAffected = deleteStmt.executeUpdate();
            deleteStmt.close();
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting savings goal: " + e.getMessage());
            return false;
        }
    }
}