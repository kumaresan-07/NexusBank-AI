package com.nexusbank.savings;

import com.nexusbank.core.DatabaseManager;

import javax.swing.*;
import java.util.List;
import java.util.Map;

/**
 * TestSavingsGoals - Utility to test the Smart Savings Goals functionality
 */
public class TestSavingsGoals {

    public static void main(String[] args) {
        try {
            System.out.println("Testing Smart Savings Goals functionality...");
            
            // Test SmartSavingsManager
            SmartSavingsManager savingsManager = new SmartSavingsManager(1);
            
            System.out.println("\n=== Creating Test Savings Goals ===");
            
            // Create an emergency fund goal
            System.out.println("Creating emergency fund goal...");
            Map<String, Object> result1 = savingsManager.createSavingsGoal(
                "Emergency Fund", 
                "6 months of expenses for emergencies", 
                50000.0, 
                "2026-06-01", 
                "Emergency Fund", 
                true, 
                4.0  // 4% auto save
            );
            System.out.println("Emergency fund result: " + result1.get("message"));
            
            // Create a vacation goal
            System.out.println("Creating vacation goal...");
            Map<String, Object> result2 = savingsManager.createSavingsGoal(
                "Dream Vacation", 
                "Trip to Europe for 2 weeks", 
                25000.0, 
                "2025-12-01", 
                "Vacation", 
                true, 
                6.0  // 6% auto save
            );
            System.out.println("Vacation goal result: " + result2.get("message"));
            
            // Create a car goal
            System.out.println("Creating car goal...");
            Map<String, Object> result3 = savingsManager.createSavingsGoal(
                "New Car", 
                "Down payment for a new car", 
                15000.0, 
                "2025-09-01", 
                "Car", 
                false, 
                0.0
            );
            System.out.println("Car goal result: " + result3.get("message"));
            
            System.out.println("\n=== Retrieving Savings Goals ===");
            List<Map<String, Object>> goals = savingsManager.getUserSavingsGoals();
            System.out.println("Total goals found: " + goals.size());
            
            for (Map<String, Object> goal : goals) {
                System.out.println("Goal: " + goal.get("goalName") + 
                                 " | Target: ₹" + goal.get("targetAmount") + 
                                 " | Current: ₹" + goal.get("currentAmount") +
                                 " | Progress: " + String.format("%.1f%%", (Double) goal.get("progressPercentage")) +
                                 " | Auto Transfer: " + (Boolean) goal.get("autoTransfer"));
            }
            
            // Test adding money to goals
            if (!goals.isEmpty()) {
                System.out.println("\n=== Testing Add Money to Goals ===");
                Map<String, Object> firstGoal = goals.get(0);
                int goalId = (Integer) firstGoal.get("goalId");
                
                System.out.println("Adding ₹5000 to goal: " + firstGoal.get("goalName"));
                Map<String, Object> addResult = savingsManager.addToSavingsGoal(goalId, 5000.0, "Initial deposit");
                System.out.println("Add money result: " + addResult.get("message"));
                
                if ((Boolean) addResult.get("success")) {
                    System.out.println("New amount: ₹" + addResult.get("newAmount"));
                    System.out.println("Progress: " + String.format("%.1f%%", (Double) addResult.get("progressPercentage")));
                    System.out.println("Goal completed: " + addResult.get("goalCompleted"));
                }
                
                // Add more money to test goal completion
                if (goals.size() > 2) {
                    Map<String, Object> thirdGoal = goals.get(2);
                    int carGoalId = (Integer) thirdGoal.get("goalId");
                    double targetAmount = (Double) thirdGoal.get("targetAmount");
                    
                    System.out.println("\nCompleting car goal with full amount...");
                    Map<String, Object> completeResult = savingsManager.addToSavingsGoal(carGoalId, targetAmount, "Full payment");
                    System.out.println("Complete goal result: " + completeResult.get("message"));
                }
            }
            
            // Test auto transfers
            System.out.println("\n=== Testing Auto Transfers ===");
            Map<String, Object> autoResult = savingsManager.processAutoTransfers();
            System.out.println("Auto transfer result: " + autoResult.get("message"));
            
            // Get statistics
            System.out.println("\n=== Savings Statistics ===");
            Map<String, Object> stats = savingsManager.getSavingsStatistics();
            System.out.println("Total Goals: " + stats.get("totalGoals"));
            System.out.println("Active Goals: " + stats.get("activeGoals"));
            System.out.println("Completed Goals: " + stats.get("completedGoals"));
            System.out.println("Total Target: ₹" + stats.get("totalTarget"));
            System.out.println("Total Saved: ₹" + stats.get("totalSaved"));
            System.out.println("Overall Progress: " + String.format("%.1f%%", (Double) stats.get("overallProgress")));
            
            System.out.println("\n=== Launching Savings Goals UI ===");
            
            // Launch the UI
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    } catch (Exception e) {
                        System.out.println("Could not set look and feel: " + e.getMessage());
                    }
                    
                    // Import SavingsGoalsPanel from ui package
                    try {
                        Class<?> panelClass = Class.forName("com.nexusbank.ui.SavingsGoalsPanel");
                        Object panel = panelClass.getConstructor(int.class).newInstance(1);
                        panelClass.getMethod("setVisible", boolean.class).invoke(panel, true);
                        
                        // Show instructions
                        JOptionPane.showMessageDialog(
                            null,
                            "Smart Savings Goals Test Instructions:\n\n" +
                            "1. View existing savings goals in the table\n" +
                            "2. Check overall progress in the header\n" +
                            "3. Click 'Create New Goal' to add goals\n" +
                            "4. Select a goal and click 'Add Money'\n" +
                            "5. Try 'Process Auto Transfers' for automatic savings\n" +
                            "6. Monitor progress bars and completion status\n" +
                            "7. Delete goals if needed (money will be returned)",
                            "Smart Savings Goals Test",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        
                    } catch (Exception e) {
                        System.err.println("Could not launch UI: " + e.getMessage());
                        // Fallback - just show the test completed message
                        JOptionPane.showMessageDialog(
                            null,
                            "Smart Savings Goals backend test completed!\nCheck console for results.",
                            "Test Complete",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error during Smart Savings Goals test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}