package com.nexusbank.ui;

import com.nexusbank.gamification.RewardsEngine;

import javax.swing.*;

/**
 * TestRewards - Utility to test the RewardsPanel UI component
 */
public class TestRewards {

    public static void main(String[] args) {
        try {
            // Initialize sample data for testing
            System.out.println("Testing RewardsPanel UI...");
            
            // Create and display the rewards panel
            SwingUtilities.invokeLater(() -> {
                try {
                    // Set look and feel for better appearance (Java 8 compatible)
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception e) {
                    System.out.println("Could not set look and feel: " + e.getMessage());
                }
                
                System.out.println("Creating RewardsPanel for user ID 1...");
                RewardsPanel panel = new RewardsPanel(1);
                panel.setVisible(true);
                
                // Show test instructions
                JOptionPane.showMessageDialog(
                    panel,
                    "RewardsPanel Test Instructions:\n\n" +
                    "1. Check if points, level, and streak are displayed\n" +
                    "2. View unlocked badges in the left panel\n" +
                    "3. Check leaderboard in the right panel\n" +
                    "4. Try redeeming points (Enter: 500 for ₹50)\n" +
                    "5. Use Refresh button to update data\n" +
                    "6. Close when testing is complete",
                    "RewardsPanel Test",
                    JOptionPane.INFORMATION_MESSAGE
                );
            });
            
        } catch (Exception e) {
            System.err.println("Error during RewardsPanel test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}