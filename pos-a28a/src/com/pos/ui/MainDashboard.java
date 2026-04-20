package com.pos.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainDashboard extends JFrame {
    private int userId;
    private String username;
    private String role;
    
    public MainDashboard(int userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        
        setTitle("POS System - Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 123, 255));
        headerPanel.setPreferredSize(new Dimension(800, 60));
        JLabel headerLabel = new JLabel("Point of Sale Management System");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        
        JPanel userPanel = new JPanel();
        userPanel.setBackground(new Color(0, 123, 255));
        JLabel userLabel = new JLabel("User: " + username + " (" + role + ")");
        userLabel.setForeground(Color.WHITE);
        userPanel.add(userLabel);
        headerPanel.add(userLabel);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(3, 2, 20, 20));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
        menuPanel.setBackground(Color.WHITE);
        
        JButton productManagementBtn = createMenuButton("Product Management", "Manage Products");
        JButton billingBtn = createMenuButton("Billing", "Create Bills");
        JButton stockManagementBtn = createMenuButton("Stock Management", "View Stock");
        JButton salesReportBtn = createMenuButton("Sales Report", "View Reports");
        JButton logoutBtn = createMenuButton("Logout", "Exit System");
        
        productManagementBtn.addActionListener(e -> {
            new ProductManagementFrame(userId).setVisible(true);
        });
        
        billingBtn.addActionListener(e -> {
            new BillingFrame(userId).setVisible(true);
        });
        
        stockManagementBtn.addActionListener(e -> {
            new StockManagementFrame().setVisible(true);
        });
        
        salesReportBtn.addActionListener(e -> {
            new SalesReportFrame().setVisible(true);
        });
        
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        
        menuPanel.add(productManagementBtn);
        menuPanel.add(billingBtn);
        menuPanel.add(stockManagementBtn);
        menuPanel.add(salesReportBtn);
        menuPanel.add(logoutBtn);
        
        panel.add(menuPanel, BorderLayout.CENTER);
        
        add(panel);
    }
    
    private JButton createMenuButton(String title, String description) {
        JButton button = new JButton("<html><center><b>" + title + "</b><br>" + description + "</center></html>");
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setBackground(new Color(0, 123, 255));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}
