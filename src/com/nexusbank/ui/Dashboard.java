package com.nexusbank.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.nexusbank.gamification.RewardsEngine;
import com.nexusbank.core.DatabaseManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Dashboard extends JFrame {

    private int userId;
    private String userName;

    private JLabel balanceLabel, pointsLabel;
    private JButton depositBtn, withdrawBtn, rewardsBtn, logoutBtn;

    public Dashboard(int userId, String userName) {
        this.userId = userId;
        this.userName = userName;

        setTitle("NexusBank-AI - Dashboard");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(16,185,129));
        header.setPreferredSize(new Dimension(800, 80));
        JLabel title = new JLabel("Welcome, " + userName);
        title.setForeground(Color.WHITE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        header.add(title, BorderLayout.WEST);

        logoutBtn = new JButton("Logout");
        header.add(logoutBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1,2,10,10));

        // Left: Account summary
        JPanel left = new JPanel(new GridLayout(4,1,10,10));
        left.setBorder(BorderFactory.createTitledBorder("Account Summary"));

        balanceLabel = new JLabel("Balance: ₹0.00");
        balanceLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        left.add(balanceLabel);

        depositBtn = new JButton("Deposit");
        withdrawBtn = new JButton("Withdraw");
        left.add(depositBtn);
        left.add(withdrawBtn);

        center.add(left);

        // Right: Rewards preview
        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(BorderFactory.createTitledBorder("Rewards"));
        pointsLabel = new JLabel("Points: 0");
        pointsLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        right.add(pointsLabel, BorderLayout.NORTH);

        rewardsBtn = new JButton("Open Rewards Panel");
        right.add(rewardsBtn, BorderLayout.SOUTH);

        center.add(right);

        add(center, BorderLayout.CENTER);

        // Load initial data
        loadAccountSummary();
        loadRewardsInfo();

        addActions();
    }

    private void loadAccountSummary() {
        // Basic balance calculation from transactions
        DatabaseManager db = DatabaseManager.getInstance();
        try {
            String q = "SELECT SUM(CASE WHEN transaction_type IN ('DEPOSIT','CASHBACK') THEN amount ELSE -amount END) as bal FROM transactions t JOIN users u ON t.user_id=u.user_id WHERE u.user_id=" + userId;
            ResultSet rs = db.executeQuery(q);
            if (rs.next()) {
                double bal = rs.getDouble("bal");
                balanceLabel.setText(String.format("Balance: ₹%.2f", bal));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRewardsInfo() {
        RewardsEngine re = new RewardsEngine(userId);
        java.util.Map<String,Object> info = re.getRewardsInfo();
        Object pts = info.getOrDefault("totalPoints", 0);
        pointsLabel.setText("Points: " + pts.toString());
    }

    private void addActions() {
        logoutBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Dashboard.this.dispose();
                LoginScreen ls = new LoginScreen();
                ls.setVisible(true);
            }
        });

        rewardsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(Dashboard.this, "Rewards panel not yet implemented.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        depositBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(Dashboard.this, "Deposit flow will be added soon.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        withdrawBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(Dashboard.this, "Withdraw flow will be added soon.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    // For quick testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Dashboard d = new Dashboard(1, "Test User");
            d.setVisible(true);
        });
    }
}
