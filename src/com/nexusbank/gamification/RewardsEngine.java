package com.nexusbank.gamification;

import com.nexusbank.core.DatabaseManager;
import javax.swing.JOptionPane;
import java.sql.*;
import java.util.*;

/**
 * RewardsEngine - Manages gamification rewards, points, badges, and achievements
 */
public class RewardsEngine {
    
    private DatabaseManager db;
    private int userId;
    
    // Points configuration
    private static final int POINTS_PER_100_RUPEES = 1;
    private static final int DAILY_LOGIN_BONUS = 5;
    private static final int BADGE_UNLOCK_BONUS = 100;
    private static final int CHALLENGE_COMPLETION_BONUS = 50;
    
    // Badge definitions
    public static final String BADGE_FIRST_DEPOSIT = "First Deposit";
    public static final String BADGE_SAVINGS_STREAK_7 = "Savings Streak 7";
    public static final String BADGE_BUDGET_MASTER = "Budget Master";
    public static final String BADGE_TRANSACTION_PRO = "Transaction Pro (100+)";
    public static final String BADGE_MILLIONAIRE = "Millionaire Club";
    public static final String BADGE_EARLY_BIRD = "Early Bird";
    public static final String BADGE_SMART_SAVER = "Smart Saver";
    
    public RewardsEngine(int userId) {
        this.db = DatabaseManager.getInstance();
        this.userId = userId;
        initializeUserRewards();
    }
    
    /**
     * Initialize rewards entry for new user
     */
    private void initializeUserRewards() {
        try {
            String checkQuery = "SELECT user_id FROM rewards WHERE user_id = " + userId;
            ResultSet rs = db.executeQuery(checkQuery);
            
            if (!rs.next()) {
                // Create new rewards entry
                String insertQuery = "INSERT INTO rewards (user_id, total_points, level, xp) " +
                                   "VALUES (" + userId + ", 0, 1, 0)";
                db.executeUpdate(insertQuery);
                System.out.println("Rewards initialized for user " + userId);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Award points for a transaction
     */
    public void awardTransactionPoints(double amount) {
        int points = (int)(amount / 100) * POINTS_PER_100_RUPEES;
        if (points > 0) {
            addPoints(points, "Transaction reward");
            checkLevelUp();
        }
    }
    
    /**
     * Award daily login bonus
     */
    public boolean awardDailyLoginBonus() {
        try {
            String query = "SELECT last_daily_login, streak_days FROM rewards WHERE user_id = " + userId;
            ResultSet rs = db.executeQuery(query);
            
            if (rs.next()) {
                java.sql.Date lastLogin = rs.getDate("last_daily_login");
                int currentStreak = rs.getInt("streak_days");
                java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
                
                // Check if already claimed today
                if (lastLogin != null && lastLogin.equals(today)) {
                    rs.close();
                    return false; // Already claimed
                }
                
                // Calculate new streak
                int newStreak = 1;
                if (lastLogin != null) {
                    long diffMs = today.getTime() - lastLogin.getTime();
                    long diffDays = diffMs / (1000 * 60 * 60 * 24);
                    if (diffDays == 1) {
                        newStreak = currentStreak + 1;
                    }
                }
                
                // Update and award points
                String updateQuery = "UPDATE rewards SET last_daily_login = ?, streak_days = ? WHERE user_id = ?";
                PreparedStatement pstmt = db.prepareStatement(updateQuery);
                pstmt.setDate(1, today);
                pstmt.setInt(2, newStreak);
                pstmt.setInt(3, userId);
                pstmt.executeUpdate();
                pstmt.close();
                
                addPoints(DAILY_LOGIN_BONUS, "Daily login bonus");
                
                // Check for streak badges
                if (newStreak == 7) {
                    unlockBadge(BADGE_SAVINGS_STREAK_7);
                }
                
                rs.close();
                return true;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Add points to user account
     */
    public void addPoints(int points, String reason) {
        try {
            String query = "UPDATE rewards SET total_points = total_points + ?, " +
                         "lifetime_points = lifetime_points + ?, xp = xp + ? WHERE user_id = ?";
            PreparedStatement pstmt = db.prepareStatement(query);
            pstmt.setInt(1, points);
            pstmt.setInt(2, points);
            pstmt.setInt(3, points);
            pstmt.setInt(4, userId);
            pstmt.executeUpdate();
            pstmt.close();
            
            System.out.println("Awarded " + points + " points to user " + userId + " for: " + reason);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Redeem points for cashback
     */
    public boolean redeemPoints(int pointsToRedeem, double cashbackAmount) {
        try {
            String query = "SELECT total_points FROM rewards WHERE user_id = " + userId;
            ResultSet rs = db.executeQuery(query);
            
            if (rs.next()) {
                int currentPoints = rs.getInt("total_points");
                
                if (currentPoints >= pointsToRedeem) {
                    // Deduct points
                    String updateQuery = "UPDATE rewards SET total_points = total_points - ?, " +
                                       "redeemed_points = redeemed_points + ? WHERE user_id = ?";
                    PreparedStatement pstmt = db.prepareStatement(updateQuery);
                    pstmt.setInt(1, pointsToRedeem);
                    pstmt.setInt(2, pointsToRedeem);
                    pstmt.setInt(3, userId);
                    pstmt.executeUpdate();
                    pstmt.close();
                    
                    // Credit cashback to account (deposit transaction)
                    addCashbackTransaction(cashbackAmount);
                    
                    rs.close();
                    return true;
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "Insufficient points!\nYou have: " + currentPoints + " points\nRequired: " + pointsToRedeem,
                        "Redemption Failed", JOptionPane.WARNING_MESSAGE);
                }
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Add cashback transaction
     */
    private void addCashbackTransaction(double amount) {
        try {
            String query = "INSERT INTO transactions (user_id, transaction_type, amount, category, description, status) " +
                         "VALUES (?, 'CASHBACK', ?, 'REWARD', 'Points redemption cashback', 'SUCCESS')";
            PreparedStatement pstmt = db.prepareStatement(query);
            pstmt.setInt(1, userId);
            pstmt.setDouble(2, amount);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Unlock a badge
     */
    public void unlockBadge(String badgeName) {
        try {
            String query = "SELECT badges_unlocked FROM rewards WHERE user_id = " + userId;
            ResultSet rs = db.executeQuery(query);
            
            if (rs.next()) {
                String badges = rs.getString("badges_unlocked");
                if (badges == null) badges = "";
                
                // Check if already unlocked
                if (!badges.contains(badgeName)) {
                    badges += (badges.isEmpty() ? "" : ",") + badgeName;
                    
                    String updateQuery = "UPDATE rewards SET badges_unlocked = ? WHERE user_id = ?";
                    PreparedStatement pstmt = db.prepareStatement(updateQuery);
                    pstmt.setString(1, badges);
                    pstmt.setInt(2, userId);
                    pstmt.executeUpdate();
                    pstmt.close();
                    
                    addPoints(BADGE_UNLOCK_BONUS, "Badge unlocked: " + badgeName);
                    
                    JOptionPane.showMessageDialog(null,
                        "🏆 Badge Unlocked!\n\n" + badgeName + "\n\n+" + BADGE_UNLOCK_BONUS + " bonus points!",
                        "Achievement!", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Check for level up
     */
    private void checkLevelUp() {
        try {
            String query = "SELECT level, xp FROM rewards WHERE user_id = " + userId;
            ResultSet rs = db.executeQuery(query);
            
            if (rs.next()) {
                int currentLevel = rs.getInt("level");
                int currentXP = rs.getInt("xp");
                
                // XP required for next level: level * 100
                int xpForNextLevel = currentLevel * 100;
                
                if (currentXP >= xpForNextLevel) {
                    int newLevel = currentLevel + 1;
                    
                    String updateQuery = "UPDATE rewards SET level = ?, xp = ? WHERE user_id = ?";
                    PreparedStatement pstmt = db.prepareStatement(updateQuery);
                    pstmt.setInt(1, newLevel);
                    pstmt.setInt(2, currentXP - xpForNextLevel);
                    pstmt.setInt(3, userId);
                    pstmt.executeUpdate();
                    pstmt.close();
                    
                    JOptionPane.showMessageDialog(null,
                        "🎉 Level Up!\n\nYou are now Level " + newLevel + "!",
                        "Congratulations!", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get user rewards info
     */
    public Map<String, Object> getRewardsInfo() {
        Map<String, Object> info = new HashMap<>();
        try {
            String query = "SELECT * FROM rewards WHERE user_id = " + userId;
            ResultSet rs = db.executeQuery(query);
            
            if (rs.next()) {
                info.put("totalPoints", rs.getInt("total_points"));
                info.put("level", rs.getInt("level"));
                info.put("xp", rs.getInt("xp"));
                info.put("streakDays", rs.getInt("streak_days"));
                info.put("lifetimePoints", rs.getInt("lifetime_points"));
                info.put("redeemedPoints", rs.getInt("redeemed_points"));
                
                String badges = rs.getString("badges_unlocked");
                info.put("badges", badges != null ? badges.split(",") : new String[0]);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return info;
    }
    
    /**
     * Get leaderboard (top 10 users by level and XP)
     */
    public List<Map<String, Object>> getLeaderboard() {
        List<Map<String, Object>> leaderboard = new ArrayList<>();
        try {
            String query = "SELECT u.full_name, r.level, r.total_points, r.xp " +
                         "FROM rewards r JOIN users u ON r.user_id = u.user_id " +
                         "ORDER BY r.level DESC, r.xp DESC LIMIT 10";
            ResultSet rs = db.executeQuery(query);
            
            int rank = 1;
            while (rs.next()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("rank", rank++);
                entry.put("name", rs.getString("full_name"));
                entry.put("level", rs.getInt("level"));
                entry.put("points", rs.getInt("total_points"));
                entry.put("xp", rs.getInt("xp"));
                leaderboard.add(entry);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaderboard;
    }
    
    /**
     * Check for transaction milestone badges
     */
    public void checkTransactionMilestones() {
        try {
            String query = "SELECT COUNT(*) as count FROM transactions WHERE user_id = " + userId;
            ResultSet rs = db.executeQuery(query);
            
            if (rs.next()) {
                int count = rs.getInt("count");
                if (count == 1) {
                    unlockBadge(BADGE_FIRST_DEPOSIT);
                } else if (count == 100) {
                    unlockBadge(BADGE_TRANSACTION_PRO);
                }
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
