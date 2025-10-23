package com.nexusbank.ui;

import com.nexusbank.gamification.RewardsEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

/**
 * RewardsPanel - Display gamification data: points, badges, level, leaderboard
 */
public class RewardsPanel extends JFrame {

    private int userId;
    private RewardsEngine rewardsEngine;
    
    private JLabel pointsLabel, levelLabel, streakLabel;
    private JTextArea badgesArea, leaderboardArea;
    private JButton redeemBtn, refreshBtn, closeBtn;

    public RewardsPanel(int userId) {
        this.userId = userId;
        this.rewardsEngine = new RewardsEngine(userId);
        
        setTitle("NexusBank-AI - Rewards & Achievements");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);
        
        // Main content - split between user stats and leaderboard
        JPanel main = new JPanel(new GridLayout(1, 2, 10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel leftPanel = createUserStatsPanel();
        JPanel rightPanel = createLeaderboardPanel();
        
        main.add(leftPanel);
        main.add(rightPanel);
        add(main, BorderLayout.CENTER);
        
        // Bottom buttons
        JPanel bottom = createBottomPanel();
        add(bottom, BorderLayout.SOUTH);
        
        loadData();
        addActions();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(new Color(245, 158, 11)); // Gold color for rewards
        header.setPreferredSize(new Dimension(700, 60));
        
        JLabel title = new JLabel("🏆 Rewards & Achievements");
        title.setForeground(Color.WHITE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        header.add(title);
        
        return header;
    }

    private JPanel createUserStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Your Stats"));
        
        // Points and level info
        JPanel statsGrid = new JPanel(new GridLayout(3, 1, 5, 5));
        
        pointsLabel = new JLabel("Points: Loading...");
        pointsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        statsGrid.add(pointsLabel);
        
        levelLabel = new JLabel("Level: Loading...");
        levelLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        statsGrid.add(levelLabel);
        
        streakLabel = new JLabel("Streak: Loading...");
        streakLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        statsGrid.add(streakLabel);
        
        panel.add(statsGrid, BorderLayout.NORTH);
        
        // Badges section
        JPanel badgesPanel = new JPanel(new BorderLayout());
        badgesPanel.setBorder(BorderFactory.createTitledBorder("Badges Unlocked"));
        
        badgesArea = new JTextArea(8, 20);
        badgesArea.setEditable(false);
        badgesArea.setBackground(new Color(248, 250, 252));
        badgesArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        JScrollPane badgesScroll = new JScrollPane(badgesArea);
        badgesPanel.add(badgesScroll, BorderLayout.CENTER);
        
        panel.add(badgesPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createLeaderboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Leaderboard (Top 10)"));
        
        leaderboardArea = new JTextArea(15, 25);
        leaderboardArea.setEditable(false);
        leaderboardArea.setBackground(new Color(248, 250, 252));
        leaderboardArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane leaderScroll = new JScrollPane(leaderboardArea);
        panel.add(leaderScroll, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        redeemBtn = new JButton("Redeem Points");
        refreshBtn = new JButton("Refresh");
        closeBtn = new JButton("Close");
        
        panel.add(redeemBtn);
        panel.add(refreshBtn);
        panel.add(closeBtn);
        
        return panel;
    }

    private void loadData() {
        // Load user rewards info
        Map<String, Object> info = rewardsEngine.getRewardsInfo();
        
        int points = (Integer) info.getOrDefault("totalPoints", 0);
        int level = (Integer) info.getOrDefault("level", 1);
        int streak = (Integer) info.getOrDefault("streakDays", 0);
        int lifetimePoints = (Integer) info.getOrDefault("lifetimePoints", 0);
        
        pointsLabel.setText(String.format("Points: %,d (Lifetime: %,d)", points, lifetimePoints));
        levelLabel.setText(String.format("Level: %d", level));
        streakLabel.setText(String.format("Login Streak: %d days", streak));
        
        // Load badges
        String[] badges = (String[]) info.getOrDefault("badges", new String[0]);
        if (badges.length == 0) {
            badgesArea.setText("No badges unlocked yet.\nComplete transactions to earn your first badge!");
        } else {
            StringBuilder badgeText = new StringBuilder("🏆 Unlocked Badges:\n\n");
            for (String badge : badges) {
                badgeText.append("• ").append(badge).append("\n");
            }
            badgeText.append("\nKeep banking to unlock more badges!");
            badgesArea.setText(badgeText.toString());
        }
        
        // Load leaderboard
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        List<Map<String, Object>> leaderboard = rewardsEngine.getLeaderboard();
        
        if (leaderboard.isEmpty()) {
            leaderboardArea.setText("No leaderboard data available.\nBe the first to earn points!");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Rank  Name              Level  Points\n");
        sb.append("----  ----              -----  ------\n");
        
        for (Map<String, Object> entry : leaderboard) {
            int rank = (Integer) entry.get("rank");
            String name = (String) entry.get("name");
            int level = (Integer) entry.get("level");
            int points = (Integer) entry.get("points");
            
            // Truncate long names
            if (name.length() > 15) {
                name = name.substring(0, 12) + "...";
            }
            
            sb.append(String.format("%2d    %-15s %5d  %6d\n", rank, name, level, points));
        }
        
        leaderboardArea.setText(sb.toString());
    }

    private void addActions() {
        redeemBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog(
                    RewardsPanel.this, 
                    "Enter points to redeem (1000 points = ₹100):", 
                    "Redeem Points", 
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (input != null && !input.trim().isEmpty()) {
                    try {
                        int pointsToRedeem = Integer.parseInt(input.trim());
                        double cashback = pointsToRedeem / 10.0; // 1000 points = 100 rupees
                        
                        boolean success = rewardsEngine.redeemPoints(pointsToRedeem, cashback);
                        if (success) {
                            JOptionPane.showMessageDialog(
                                RewardsPanel.this,
                                String.format("Successfully redeemed %d points for ₹%.2f!", pointsToRedeem, cashback),
                                "Redemption Successful",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            loadData(); // Refresh display
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(
                            RewardsPanel.this,
                            "Please enter a valid number",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        });

        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadData();
                JOptionPane.showMessageDialog(
                    RewardsPanel.this,
                    "Data refreshed!",
                    "Refresh Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    // Test main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RewardsPanel panel = new RewardsPanel(1); // Test with user ID 1
            panel.setVisible(true);
        });
    }
}