package com.pos.ui;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import com.pos.util.DatabaseConnection;

public class StockManagementFrame extends JFrame {
    private JTable stockTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel totalProductsLabel, lowStockLabel;
    
    public StockManagementFrame() {
        setTitle("Stock Management");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Stock Management", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 123, 255));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        totalPanel.setBackground(new Color(240, 240, 240));
        JLabel totalLabel = new JLabel("Total Products", JLabel.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalProductsLabel = new JLabel("0", JLabel.CENTER);
        totalProductsLabel.setFont(new Font("Arial", Font.BOLD, 24));
        totalProductsLabel.setForeground(new Color(0, 123, 255));
        totalPanel.add(totalLabel, BorderLayout.NORTH);
        totalPanel.add(totalProductsLabel, BorderLayout.CENTER);
        
        JPanel lowStockPanel = new JPanel(new BorderLayout());
        lowStockPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        lowStockPanel.setBackground(new Color(240, 240, 240));
        JLabel lowLabel = new JLabel("Low Stock Items (< 20)", JLabel.CENTER);
        lowLabel.setFont(new Font("Arial", Font.BOLD, 14));
        lowStockLabel = new JLabel("0", JLabel.CENTER);
        lowStockLabel.setFont(new Font("Arial", Font.BOLD, 24));
        lowStockLabel.setForeground(new Color(220, 53, 69));
        lowStockPanel.add(lowLabel, BorderLayout.NORTH);
        lowStockPanel.add(lowStockLabel, BorderLayout.CENTER);
        
        statsPanel.add(totalPanel);
        statsPanel.add(lowStockPanel);
        
        topPanel.add(statsPanel, BorderLayout.NORTH);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search Product:"));
        searchField = new JTextField(25);
        searchPanel.add(searchField);
        JButton searchButton = new JButton("Search");
        JButton refreshButton = new JButton("Refresh");
        JButton showLowStockButton = new JButton("Show Low Stock");
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);
        searchPanel.add(showLowStockButton);
        
        topPanel.add(searchPanel, BorderLayout.SOUTH);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        String[] columns = {"Product ID", "Product Name", "Price", "Quantity", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        stockTable = new JTable(tableModel);
        stockTable.setRowHeight(25);
        
        stockTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    int quantity = (int) table.getValueAt(row, 3);
                    if (quantity == 0) {
                        c.setBackground(new Color(255, 200, 200));
                    } else if (quantity < 20) {
                        c.setBackground(new Color(255, 255, 200));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(stockTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        searchButton.addActionListener(e -> searchStock());
        refreshButton.addActionListener(e -> loadStock());
        showLowStockButton.addActionListener(e -> showLowStock());
        
        add(mainPanel);
        loadStock();
        updateStatistics();
    }
    
    private void loadStock() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM products ORDER BY quantity ASC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                String status;
                
                if (quantity == 0) {
                    status = "Out of Stock";
                } else if (quantity < 20) {
                    status = "Low Stock";
                } else {
                    status = "In Stock";
                }
                
                Object[] row = {productId, productName, price, quantity, status};
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading stock: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        updateStatistics();
    }
    
    private void searchStock() {
        String searchText = searchField.getText();
        if (searchText.isEmpty()) {
            loadStock();
            return;
        }
        
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM products WHERE product_name LIKE ? ORDER BY quantity ASC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "%" + searchText + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                String status;
                
                if (quantity == 0) {
                    status = "Out of Stock";
                } else if (quantity < 20) {
                    status = "Low Stock";
                } else {
                    status = "In Stock";
                }
                
                Object[] row = {productId, productName, price, quantity, status};
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error searching stock: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showLowStock() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM products WHERE quantity < 20 ORDER BY quantity ASC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                String status;
                
                if (quantity == 0) {
                    status = "Out of Stock";
                } else {
                    status = "Low Stock";
                }
                
                Object[] row = {productId, productName, price, quantity, status};
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading low stock items: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateStatistics() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String countQuery = "SELECT COUNT(*) as total FROM products";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(countQuery);
            if (rs.next()) {
                totalProductsLabel.setText(String.valueOf(rs.getInt("total")));
            }
            
            String lowStockQuery = "SELECT COUNT(*) as low_stock FROM products WHERE quantity < 20";
            ResultSet rs2 = stmt.executeQuery(lowStockQuery);
            if (rs2.next()) {
                lowStockLabel.setText(String.valueOf(rs2.getInt("low_stock")));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating statistics: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
