package com.pos.ui;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.*;
import com.pos.util.DatabaseConnection;

public class BillingFrame extends JFrame {
    private int userId;
    private JComboBox<String> productCombo;
    private JTextField quantityField, discountField;
    private JTable billTable;
    private DefaultTableModel tableModel;
    private JLabel subtotalLabel, gstLabel, discountLabelAmount, totalLabel;
    private double subtotal = 0;
    
    public BillingFrame(int userId) {
        this.userId = userId;
        
        setTitle("Billing - Create Invoice");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Create New Bill", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 123, 255));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add Product to Bill"));
        
        inputPanel.add(new JLabel("Select Product:"));
        productCombo = new JComboBox<>();
        loadProducts();
        inputPanel.add(productCombo);
        
        inputPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        inputPanel.add(quantityField);
        
        JButton addButton = new JButton("Add to Bill");
        addButton.setBackground(new Color(40, 167, 69));
        addButton.setForeground(Color.WHITE);
        inputPanel.add(new JLabel(""));
        inputPanel.add(addButton);
        
        leftPanel.add(inputPanel, BorderLayout.NORTH);
        
        String[] columns = {"Product", "Price", "Quantity", "Total"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        billTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(billTable);
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton removeButton = new JButton("Remove Item");
        removeButton.setBackground(new Color(220, 53, 69));
        removeButton.setForeground(Color.WHITE);
        JButton clearButton = new JButton("Clear All");
        buttonPanel.add(removeButton);
        buttonPanel.add(clearButton);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(leftPanel, BorderLayout.CENTER);
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(300, 0));
        
        JPanel summaryPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Bill Summary"));
        
        summaryPanel.add(new JLabel("Subtotal:"));
        subtotalLabel = new JLabel("₹0.00");
        subtotalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        summaryPanel.add(subtotalLabel);
        
        summaryPanel.add(new JLabel("Discount (%):"));
        discountField = new JTextField("0");
        summaryPanel.add(discountField);
        
        summaryPanel.add(new JLabel("Discount Amount:"));
        discountLabelAmount = new JLabel("₹0.00");
        summaryPanel.add(discountLabelAmount);
        
        summaryPanel.add(new JLabel("GST (18%):"));
        gstLabel = new JLabel("₹0.00");
        summaryPanel.add(gstLabel);
        
        summaryPanel.add(new JLabel(""));
        summaryPanel.add(new JLabel(""));
        
        summaryPanel.add(new JLabel("Total Amount:"));
        totalLabel = new JLabel("₹0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setForeground(new Color(40, 167, 69));
        summaryPanel.add(totalLabel);
        
        JButton calculateButton = new JButton("Calculate Total");
        calculateButton.setBackground(new Color(255, 193, 7));
        summaryPanel.add(new JLabel(""));
        summaryPanel.add(calculateButton);
        
        rightPanel.add(summaryPanel, BorderLayout.NORTH);
        
        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        
        JButton generateBillButton = new JButton("Generate Bill");
        generateBillButton.setFont(new Font("Arial", Font.BOLD, 16));
        generateBillButton.setBackground(new Color(0, 123, 255));
        generateBillButton.setForeground(Color.WHITE);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        
        actionPanel.add(generateBillButton);
        actionPanel.add(cancelButton);
        
        rightPanel.add(actionPanel, BorderLayout.SOUTH);
        
        mainPanel.add(rightPanel, BorderLayout.EAST);
        
        addButton.addActionListener(e -> addProductToBill());
        removeButton.addActionListener(e -> removeItem());
        clearButton.addActionListener(e -> clearBill());
        calculateButton.addActionListener(e -> calculateTotal());
        generateBillButton.addActionListener(e -> generateBill());
        cancelButton.addActionListener(e -> dispose());
        
        add(mainPanel);
    }
    
    private void loadProducts() {
        productCombo.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT product_id, product_name, price FROM products WHERE quantity > 0";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                String item = rs.getInt("product_id") + " - " + rs.getString("product_name") + " (₹" + rs.getDouble("price") + ")";
                productCombo.addItem(item);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addProductToBill() {
        if (productCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a product", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String quantityStr = quantityField.getText();
        if (quantityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter quantity", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String selected = productCombo.getSelectedItem().toString();
            int productId = Integer.parseInt(selected.split(" - ")[0]);
            
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT product_name, price, quantity FROM products WHERE product_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String productName = rs.getString("product_name");
                double price = rs.getDouble("price");
                int availableQty = rs.getInt("quantity");
                
                if (quantity > availableQty) {
                    JOptionPane.showMessageDialog(this, "Only " + availableQty + " items available in stock", "Error", JOptionPane.ERROR_MESSAGE);
                    conn.close();
                    return;
                }
                
                double total = price * quantity;
                Object[] row = {productName, price, quantity, total};
                tableModel.addRow(row);
                
                quantityField.setText("");
                updateSubtotal();
            }
            
            conn.close();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding product: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void removeItem() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tableModel.removeRow(row);
        updateSubtotal();
    }
    
    private void clearBill() {
        tableModel.setRowCount(0);
        subtotal = 0;
        updateSubtotal();
        discountField.setText("0");
    }
    
    private void updateSubtotal() {
        subtotal = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            subtotal += (double) tableModel.getValueAt(i, 3);
        }
        subtotalLabel.setText("₹" + String.format("%.2f", subtotal));
        calculateTotal();
    }
    
    private void calculateTotal() {
        double discountPercent = 0;
        try {
            discountPercent = Double.parseDouble(discountField.getText());
        } catch (NumberFormatException ex) {
            discountPercent = 0;
        }
        
        double discountAmount = subtotal * (discountPercent / 100);
        double afterDiscount = subtotal - discountAmount;
        double gst = afterDiscount * 0.18;
        double total = afterDiscount + gst;
        
        discountLabelAmount.setText("₹" + String.format("%.2f", discountAmount));
        gstLabel.setText("₹" + String.format("%.2f", gst));
        totalLabel.setText("₹" + String.format("%.2f", total));
    }
    
    private void generateBill() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Please add products to the bill", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        calculateTotal();
        
        double total = Double.parseDouble(totalLabel.getText().replace("₹", ""));
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            String salesQuery = "INSERT INTO sales (date, total_amount, user_id) VALUES (?, ?, ?)";
            PreparedStatement salesStmt = conn.prepareStatement(salesQuery, Statement.RETURN_GENERATED_KEYS);
            salesStmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            salesStmt.setDouble(2, total);
            salesStmt.setInt(3, userId);
            salesStmt.executeUpdate();
            
            ResultSet rs = salesStmt.getGeneratedKeys();
            int saleId = 0;
            if (rs.next()) {
                saleId = rs.getInt(1);
            }
            
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String productName = (String) tableModel.getValueAt(i, 0);
                double price = (double) tableModel.getValueAt(i, 1);
                int quantity = (int) tableModel.getValueAt(i, 2);
                
                String productQuery = "SELECT product_id, quantity FROM products WHERE product_name = ?";
                PreparedStatement productStmt = conn.prepareStatement(productQuery);
                productStmt.setString(1, productName);
                ResultSet productRs = productStmt.executeQuery();
                
                if (productRs.next()) {
                    int productId = productRs.getInt("product_id");
                    int currentQty = productRs.getInt("quantity");
                    
                    String saleItemQuery = "INSERT INTO sale_items (sale_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
                    PreparedStatement saleItemStmt = conn.prepareStatement(saleItemQuery);
                    saleItemStmt.setInt(1, saleId);
                    saleItemStmt.setInt(2, productId);
                    saleItemStmt.setInt(3, quantity);
                    saleItemStmt.setDouble(4, price);
                    saleItemStmt.executeUpdate();
                    
                    String updateQuery = "UPDATE products SET quantity = ? WHERE product_id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setInt(1, currentQty - quantity);
                    updateStmt.setInt(2, productId);
                    updateStmt.executeUpdate();
                }
            }
            
            conn.commit();
            
            JOptionPane.showMessageDialog(this, "Bill generated successfully!\nBill ID: " + saleId + "\nTotal: ₹" + String.format("%.2f", total), "Success", JOptionPane.INFORMATION_MESSAGE);
            
            clearBill();
            loadProducts();
            
            conn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error generating bill: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
