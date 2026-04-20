package com.pos.ui;

import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.table.*;
import com.pos.util.DatabaseConnection;

public class SalesReportFrame extends JFrame {
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private JLabel totalSalesLabel, todaySalesLabel, monthlySalesLabel;
    private JComboBox<String> reportTypeCombo;
    
    public SalesReportFrame() {
        setTitle("Sales Report");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Sales Report", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 123, 255));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JPanel todayPanel = createStatPanel("Today's Sales", "todaySales");
        todaySalesLabel = (JLabel) ((JPanel) todayPanel.getComponent(1)).getComponent(0);
        
        JPanel monthlyPanel = createStatPanel("Monthly Sales", "monthlySales");
        monthlySalesLabel = (JLabel) ((JPanel) monthlyPanel.getComponent(1)).getComponent(0);
        
        JPanel totalPanel = createStatPanel("Total Sales", "totalSales");
        totalSalesLabel = (JLabel) ((JPanel) totalPanel.getComponent(1)).getComponent(0);
        
        statsPanel.add(todayPanel);
        statsPanel.add(monthlyPanel);
        statsPanel.add(totalPanel);
        
        topPanel.add(statsPanel, BorderLayout.NORTH);
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Report Type:"));
        reportTypeCombo = new JComboBox<>(new String[]{"All Sales", "Today's Sales", "This Month", "Custom Date Range"});
        filterPanel.add(reportTypeCombo);
        
        JButton generateButton = new JButton("Generate Report");
        generateButton.setBackground(new Color(0, 123, 255));
        generateButton.setForeground(Color.WHITE);
        filterPanel.add(generateButton);
        
        JButton refreshButton = new JButton("Refresh");
        filterPanel.add(refreshButton);
        
        topPanel.add(filterPanel, BorderLayout.SOUTH);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        String[] columns = {"Sale ID", "Date", "Time", "Total Amount", "Items Count"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        salesTable = new JTable(tableModel);
        salesTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(salesTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton viewDetailsButton = new JButton("View Sale Details");
        viewDetailsButton.setBackground(new Color(40, 167, 69));
        viewDetailsButton.setForeground(Color.WHITE);
        bottomPanel.add(viewDetailsButton);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        generateButton.addActionListener(e -> generateReport());
        refreshButton.addActionListener(e -> loadAllSales());
        viewDetailsButton.addActionListener(e -> viewSaleDetails());
        
        add(mainPanel);
        loadAllSales();
        updateStatistics();
    }
    
    private JPanel createStatPanel(String title, String type) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.setBackground(new Color(240, 240, 240));
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        valuePanel.setBackground(new Color(240, 240, 240));
        JLabel valueLabel = new JLabel("₹0.00", JLabel.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 22));
        
        if (type.equals("todaySales")) {
            valueLabel.setForeground(new Color(40, 167, 69));
        } else if (type.equals("monthlySales")) {
            valueLabel.setForeground(new Color(0, 123, 255));
        } else {
            valueLabel.setForeground(new Color(220, 53, 69));
        }
        
        valuePanel.add(valueLabel);
        panel.add(valuePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadAllSales() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT s.sale_id, s.date, s.total_amount, COUNT(si.item_id) as items_count " +
                          "FROM sales s LEFT JOIN sale_items si ON s.sale_id = si.sale_id " +
                          "GROUP BY s.sale_id ORDER BY s.date DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            
            while (rs.next()) {
                int saleId = rs.getInt("sale_id");
                Timestamp date = rs.getTimestamp("date");
                double totalAmount = rs.getDouble("total_amount");
                int itemsCount = rs.getInt("items_count");
                
                Object[] row = {
                    saleId,
                    dateFormat.format(date),
                    timeFormat.format(date),
                    "₹" + String.format("%.2f", totalAmount),
                    itemsCount
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading sales: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void generateReport() {
        String reportType = (String) reportTypeCombo.getSelectedItem();
        tableModel.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "";
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            
            if (reportType.equals("Today's Sales")) {
                query = "SELECT s.sale_id, s.date, s.total_amount, COUNT(si.item_id) as items_count " +
                       "FROM sales s LEFT JOIN sale_items si ON s.sale_id = si.sale_id " +
                       "WHERE DATE(s.date) = CURDATE() " +
                       "GROUP BY s.sale_id ORDER BY s.date DESC";
            } else if (reportType.equals("This Month")) {
                query = "SELECT s.sale_id, s.date, s.total_amount, COUNT(si.item_id) as items_count " +
                       "FROM sales s LEFT JOIN sale_items si ON s.sale_id = si.sale_id " +
                       "WHERE MONTH(s.date) = MONTH(CURDATE()) AND YEAR(s.date) = YEAR(CURDATE()) " +
                       "GROUP BY s.sale_id ORDER BY s.date DESC";
            } else {
                loadAllSales();
                return;
            }
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                int saleId = rs.getInt("sale_id");
                Timestamp date = rs.getTimestamp("date");
                double totalAmount = rs.getDouble("total_amount");
                int itemsCount = rs.getInt("items_count");
                
                Object[] row = {
                    saleId,
                    dateFormat.format(date),
                    timeFormat.format(date),
                    "₹" + String.format("%.2f", totalAmount),
                    itemsCount
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateStatistics() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String todayQuery = "SELECT SUM(total_amount) as today_total FROM sales WHERE DATE(date) = CURDATE()";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(todayQuery);
            if (rs.next()) {
                double todayTotal = rs.getDouble("today_total");
                todaySalesLabel.setText("₹" + String.format("%.2f", todayTotal));
            }
            
            String monthQuery = "SELECT SUM(total_amount) as month_total FROM sales WHERE MONTH(date) = MONTH(CURDATE()) AND YEAR(date) = YEAR(CURDATE())";
            ResultSet rs2 = stmt.executeQuery(monthQuery);
            if (rs2.next()) {
                double monthTotal = rs2.getDouble("month_total");
                monthlySalesLabel.setText("₹" + String.format("%.2f", monthTotal));
            }
            
            String totalQuery = "SELECT SUM(total_amount) as total FROM sales";
            ResultSet rs3 = stmt.executeQuery(totalQuery);
            if (rs3.next()) {
                double total = rs3.getDouble("total");
                totalSalesLabel.setText("₹" + String.format("%.2f", total));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating statistics: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void viewSaleDetails() {
        int row = salesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a sale to view details", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int saleId = (int) tableModel.getValueAt(row, 0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT si.item_id, p.product_name, si.quantity, si.price, (si.quantity * si.price) as total " +
                          "FROM sale_items si JOIN products p ON si.product_id = p.product_id " +
                          "WHERE si.sale_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, saleId);
            ResultSet rs = pstmt.executeQuery();
            
            StringBuilder details = new StringBuilder();
            details.append("Sale ID: ").append(saleId).append("\n");
            details.append("Date: ").append(tableModel.getValueAt(row, 1)).append("\n");
            details.append("Time: ").append(tableModel.getValueAt(row, 2)).append("\n\n");
            details.append("Items:\n");
            details.append("--------------------------------------------------\n");
            
            while (rs.next()) {
                String productName = rs.getString("product_name");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");
                double total = rs.getDouble("total");
                
                details.append(String.format("%-20s x%d @ ₹%.2f = ₹%.2f\n", productName, quantity, price, total));
            }
            
            details.append("--------------------------------------------------\n");
            details.append("Total Amount: ").append(tableModel.getValueAt(row, 3)).append("\n");
            
            JTextArea textArea = new JTextArea(details.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(450, 300));
            
            JOptionPane.showMessageDialog(this, scrollPane, "Sale Details", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading sale details: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
