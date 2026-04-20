# Point of Sale (POS) Management System

A comprehensive Point of Sale system built with Java Swing and MySQL for managing products, billing, stock, and sales reports.

## Features

### 1. Login Module
- Secure authentication for Admin and Staff users
- Username and password verification from database
- Role-based access control

### 2. Product Management
- Add new products with name, price, and quantity
- Update existing product details
- Delete products from inventory
- Search products by name
- View complete product list

### 3. Billing Module
- Select products from dropdown
- Add multiple items to bill
- Calculate subtotal, discount, and GST (18%)
- Generate bills and save to database
- Automatic stock update after sale
- Remove items from bill

### 4. Stock Management
- View all products with stock levels
- Color-coded stock status:
  - Red: Out of Stock (quantity = 0)
  - Yellow: Low Stock (quantity < 20)
  - White: In Stock (quantity >= 20)
- Search products by name
- Filter low stock items
- Real-time statistics (Total Products, Low Stock Items)

### 5. Sales Report
- View all sales transactions
- Daily sales report
- Monthly sales report
- Total revenue statistics
- View detailed bill information
- Sales filtering by date range

## Technologies Used

| Layer | Technology |
|-------|-----------|
| Frontend | Java Swing |
| Backend | Java |
| Database | MySQL |
| Connectivity | JDBC |
| IDE | Eclipse / IntelliJ / NetBeans |

## Database Schema

### Tables

**users**
- user_id (Primary Key)
- username
- password
- role

**products**
- product_id (Primary Key)
- product_name
- price
- quantity

**sales**
- sale_id (Primary Key)
- date
- total_amount
- user_id (Foreign Key)

**sale_items**
- item_id (Primary Key)
- sale_id (Foreign Key)
- product_id (Foreign Key)
- quantity
- price

## Setup Instructions

### Prerequisites
1. Java Development Kit (JDK) 8 or higher
2. MySQL Server 5.7 or higher
3. MySQL Connector/J (JDBC Driver)

### Database Setup

1. Start MySQL Server

2. Create the database and tables:
```bash
mysql -u root -p < database/schema.sql
```

Or manually execute the SQL commands:
```sql
CREATE DATABASE pos_system;
USE pos_system;
-- Execute all table creation statements from schema.sql
```

3. Update database credentials in `src/com/pos/util/DatabaseConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/pos_system";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

### Running the Application

#### Using Command Line

1. Download MySQL Connector/J from [MySQL Official Website](https://dev.mysql.com/downloads/connector/j/)

2. Compile the project:
```bash
javac -cp ".:mysql-connector-java-8.0.xx.jar" -d bin src/com/pos/*.java src/com/pos/ui/*.java src/com/pos/util/*.java src/com/pos/model/*.java
```

For Windows:
```cmd
javac -cp ".;mysql-connector-java-8.0.xx.jar" -d bin src\com\pos\*.java src\com\pos\ui\*.java src\com\pos\util\*.java src\com\pos\model\*.java
```

3. Run the application:
```bash
java -cp ".:mysql-connector-java-8.0.xx.jar:bin" com.pos.POSSystem
```

For Windows:
```cmd
java -cp ".;mysql-connector-java-8.0.xx.jar;bin" com.pos.POSSystem
```

#### Using IDE (Eclipse/IntelliJ/NetBeans)

1. Import the project into your IDE
2. Add MySQL Connector/J JAR to project libraries
3. Build the project
4. Run `POSSystem.java`

## Default Login Credentials

**Admin Account:**
- Username: `admin`
- Password: `admin123`

**Staff Account:**
- Username: `staff`
- Password: `staff123`

## Project Structure

```
pos-system/
├── database/
│   └── schema.sql
├── src/
│   └── com/
│       └── pos/
│           ├── POSSystem.java (Main Entry Point)
│           ├── model/
│           │   ├── User.java
│           │   └── Product.java
│           ├── ui/
│           │   ├── LoginFrame.java
│           │   ├── MainDashboard.java
│           │   ├── ProductManagementFrame.java
│           │   ├── BillingFrame.java
│           │   ├── StockManagementFrame.java
│           │   └── SalesReportFrame.java
│           └── util/
│               └── DatabaseConnection.java
└── README.md
```

## Usage Guide

1. **Login**: Start the application and login with credentials
2. **Manage Products**: Add, update, or delete products from inventory
3. **Create Bills**: Select products, add quantities, apply discounts, and generate bills
4. **Check Stock**: Monitor inventory levels and get low stock alerts
5. **View Reports**: Check daily, monthly, and total sales statistics

## Features Highlight

- **Automatic Stock Updates**: Stock quantity decreases automatically after each sale
- **GST Calculation**: Automatic 18% GST calculation on bills
- **Discount Support**: Apply percentage-based discounts on bills
- **Low Stock Alerts**: Visual indicators for products running low
- **Sales Analytics**: Comprehensive sales reports with filtering options
- **User-Friendly Interface**: Clean and intuitive Swing-based GUI

## Troubleshooting

### Common Issues

1. **Database Connection Error**
   - Verify MySQL server is running
   - Check database credentials in DatabaseConnection.java
   - Ensure pos_system database exists

2. **ClassNotFoundException: com.mysql.cj.jdbc.Driver**
   - Add MySQL Connector/J JAR to classpath
   - Verify JAR file path is correct

3. **Product not adding to bill**
   - Ensure product quantity is available in stock
   - Check if product exists in database

## Future Enhancements

- Barcode scanner integration
- Print bill functionality
- Customer management module
- Advanced reporting with charts
- Multi-user session management
- Product categories and suppliers
- Backup and restore functionality

## License

This project is developed for educational purposes.

## Author

Developed as part of Java & MySQL learning project.
