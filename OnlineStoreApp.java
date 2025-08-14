import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class OnlineStoreApp extends JFrame {
    JComboBox<String> tableSelector, collectionSelector;
    JTextArea outputArea;
    JButton loadButton, insertButton, updateButton, deleteButton;

    public OnlineStoreApp() {
        setTitle("🛒 Online Store Dashboard");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 255, 250));

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        topPanel.setBackground(new Color(60, 179, 113));

        JLabel tableLabel = new JLabel("Table:");
        tableLabel.setForeground(Color.WHITE);
        tableLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel collectionLabel = new JLabel("Collection:");
        collectionLabel.setForeground(Color.WHITE);
        collectionLabel.setFont(new Font("Arial", Font.BOLD, 14));

        tableSelector = new JComboBox<>(new String[]{"Inventory", "Orders"});
        collectionSelector = new JComboBox<>(new String[]{"ArrayList", "TreeSet"});

        loadButton = new JButton("Load Data");
        loadButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loadButton.setBackground(new Color(255, 215, 0));

        insertButton = new JButton("Insert");
        insertButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        insertButton.setBackground(new Color(70, 130, 180));
        insertButton.setForeground(Color.WHITE);

        updateButton = new JButton("Update");
        updateButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        updateButton.setBackground(new Color(255, 140, 0));
        updateButton.setForeground(Color.WHITE);

        deleteButton = new JButton("Delete");
        deleteButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        deleteButton.setBackground(new Color(220, 20, 60));
        deleteButton.setForeground(Color.WHITE);

        topPanel.add(tableLabel);
        topPanel.add(tableSelector);
        topPanel.add(collectionLabel);
        topPanel.add(collectionSelector);
        topPanel.add(loadButton);
        topPanel.add(insertButton);
        topPanel.add(updateButton);
        topPanel.add(deleteButton);

        // Output Area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        outputArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadButton.addActionListener(e -> loadData());
        insertButton.addActionListener(e -> insertRecord());
        updateButton.addActionListener(e -> updateRecord());
        deleteButton.addActionListener(e -> deleteRecord());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/OnlineStore", "root", "");
    }

    private void loadData() {
        String table = (String) tableSelector.getSelectedItem();
        String collection = (String) collectionSelector.getSelectedItem();
        outputArea.setText("");

        try (Connection conn = getConnection()) {
            if ("Inventory".equals(table)) {
                List<Inventory> inventoryList = new ArrayList<>();
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM Inventory")) {
                    while (rs.next()) {
                        inventoryList.add(new Inventory(
                                rs.getInt("item_id"),
                                rs.getString("item_name"),
                                rs.getFloat("price"),
                                rs.getString("category").charAt(0),
                                rs.getBoolean("in_stock")
                        ));
                    }
                }
                outputArea.append(String.format("%-4s | %-20s | %-7s | %-3s | %-12s\n", "ID", "Name", "Price", "Cat", "Status"));
                outputArea.append("-------------------------------------------------------------------\n");

                if ("TreeSet".equals(collection)) {
                    TreeSet<Inventory> sortedSet = new TreeSet<>(Comparator.comparing(Inventory::getPrice));
                    sortedSet.addAll(inventoryList);
                    for (Inventory item : sortedSet) {
                        outputArea.append(item + "\n");
                    }
                } else {
                    for (Inventory item : inventoryList) {
                        outputArea.append(item + "\n");
                    }
                }

            } else if ("Orders".equals(table)) {
                List<Order> orderList = new ArrayList<>();
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM Orders")) {
                    while (rs.next()) {
                        orderList.add(new Order(
                                rs.getInt("order_id"),
                                rs.getString("customer_name"),
                                rs.getFloat("amount"),
                                rs.getString("status").charAt(0),
                                rs.getBoolean("is_paid")
                        ));
                    }
                }

                Map<Boolean, List<Order>> grouped = orderList.stream()
                        .collect(Collectors.groupingBy(Order::isPaid));

                outputArea.append("=== PAID Orders ===\n");
                outputArea.append(String.format("%-4s | %-15s | %-7s | %-6s\n", "ID", "Customer", "Amount", "Status"));
                outputArea.append("------------------------------------------------\n");
                for (Order o : grouped.getOrDefault(true, new ArrayList<>())) outputArea.append(o + "\n");

                outputArea.append("\n=== UNPAID Orders ===\n");
                outputArea.append(String.format("%-4s | %-15s | %-7s | %-6s\n", "ID", "Customer", "Amount", "Status"));
                outputArea.append("------------------------------------------------\n");
                for (Order o : grouped.getOrDefault(false, new ArrayList<>())) outputArea.append(o + "\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            outputArea.setText("❌ Error loading data: " + ex.getMessage());
        }
    }

    private void insertRecord() {
        String table = (String) tableSelector.getSelectedItem();

        if ("Inventory".equals(table)) {
            try {
                String idStr = JOptionPane.showInputDialog(this, "Enter Item ID (int):");
                if (idStr == null) return;
                int id = Integer.parseInt(idStr.trim());

                String name = JOptionPane.showInputDialog(this, "Enter Item Name:");
                if (name == null) return;

                String priceStr = JOptionPane.showInputDialog(this, "Enter Price (float):");
                if (priceStr == null) return;
                float price = Float.parseFloat(priceStr.trim());

                String categoryStr = JOptionPane.showInputDialog(this, "Enter Category (single char):");
                if (categoryStr == null) return;
                if (categoryStr.trim().length() != 1) {
                    JOptionPane.showMessageDialog(this, "Category must be a single character!");
                    return;
                }
                char category = categoryStr.trim().charAt(0);

                int stockOption = JOptionPane.showConfirmDialog(this, "Is item in stock?", "Stock Status", JOptionPane.YES_NO_OPTION);
                boolean inStock = (stockOption == JOptionPane.YES_OPTION);

                try (Connection conn = getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "INSERT INTO Inventory(item_id, item_name, price, category, in_stock) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setInt(1, id);
                    ps.setString(2, name);
                    ps.setFloat(3, price);
                    ps.setString(4, String.valueOf(category));
                    ps.setBoolean(5, inStock);
                    ps.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Inventory item inserted successfully.");
                loadData();

            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Invalid number format. Try again.");
            } catch (SQLException sqle) {
                JOptionPane.showMessageDialog(this, "SQL Error: " + sqle.getMessage());
            }
        } else if ("Orders".equals(table)) {
            try {
                String idStr = JOptionPane.showInputDialog(this, "Enter Order ID (int):");
                if (idStr == null) return;
                int id = Integer.parseInt(idStr.trim());

                String customerName = JOptionPane.showInputDialog(this, "Enter Customer Name:");
                if (customerName == null) return;

                String amountStr = JOptionPane.showInputDialog(this, "Enter Amount (float):");
                if (amountStr == null) return;
                float amount = Float.parseFloat(amountStr.trim());

                String statusStr = JOptionPane.showInputDialog(this, "Enter Status (single char):");
                if (statusStr == null) return;
                if (statusStr.trim().length() != 1) {
                    JOptionPane.showMessageDialog(this, "Status must be a single character!");
                    return;
                }
                char status = statusStr.trim().charAt(0);

                int paidOption = JOptionPane.showConfirmDialog(this, "Is the order paid?", "Payment Status", JOptionPane.YES_NO_OPTION);
                boolean isPaid = (paidOption == JOptionPane.YES_OPTION);

                try (Connection conn = getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "INSERT INTO Orders(order_id, customer_name, amount, status, is_paid) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setInt(1, id);
                    ps.setString(2, customerName);
                    ps.setFloat(3, amount);
                    ps.setString(4, String.valueOf(status));
                    ps.setBoolean(5, isPaid);
                    ps.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Order inserted successfully.");
                loadData();

            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Invalid number format. Try again.");
            } catch (SQLException sqle) {
                JOptionPane.showMessageDialog(this, "SQL Error: " + sqle.getMessage());
            }
        }
    }

    private void updateRecord() {
        String table = (String) tableSelector.getSelectedItem();

        try (Connection conn = getConnection()) {
            if ("Inventory".equals(table)) {
                String idStr = JOptionPane.showInputDialog(this, "Enter the Item ID to update:");
                if (idStr == null) return;
                int id = Integer.parseInt(idStr.trim());

                // Fetch current record
                Inventory current = null;
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM Inventory WHERE item_id = ?")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            current = new Inventory(
                                    rs.getInt("item_id"),
                                    rs.getString("item_name"),
                                    rs.getFloat("price"),
                                    rs.getString("category").charAt(0),
                                    rs.getBoolean("in_stock")
                            );
                        } else {
                            JOptionPane.showMessageDialog(this, "No Inventory item found with that ID.");
                            return;
                        }
                    }
                }

                // Show current values and ask for new values (can leave blank to keep current)
                String name = JOptionPane.showInputDialog(this,
                        "Enter new Item Name (leave blank to keep '" + current.getItem_name() + "'):");
                if (name == null) return;
                if (name.trim().isEmpty()) name = current.getItem_name();

                String priceStr = JOptionPane.showInputDialog(this,
                        "Enter new Price (leave blank to keep '" + current.getPrice() + "'):");
                if (priceStr == null) return;
                float price = priceStr.trim().isEmpty() ? current.getPrice() : Float.parseFloat(priceStr.trim());

                String categoryStr = JOptionPane.showInputDialog(this,
                        "Enter new Category (single char) (leave blank to keep '" + current.getCategory() + "'):");
                if (categoryStr == null) return;
                char category = categoryStr.trim().isEmpty() ? current.getCategory() : categoryStr.trim().charAt(0);

                int stockOption = JOptionPane.showConfirmDialog(this, "Is item in stock?", "Stock Status",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null);
                boolean inStock = (stockOption == JOptionPane.YES_OPTION);

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Inventory SET item_name=?, price=?, category=?, in_stock=? WHERE item_id=?")) {
                    ps.setString(1, name);
                    ps.setFloat(2, price);
                    ps.setString(3, String.valueOf(category));
                    ps.setBoolean(4, inStock);
                    ps.setInt(5, id);
                    ps.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Inventory item updated successfully.");
                loadData();

            } else if ("Orders".equals(table)) {
                String idStr = JOptionPane.showInputDialog(this, "Enter the Order ID to update:");
                if (idStr == null) return;
                int id = Integer.parseInt(idStr.trim());

                // Fetch current record
                Order current = null;
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM Orders WHERE order_id = ?")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            current = new Order(
                                    rs.getInt("order_id"),
                                    rs.getString("customer_name"),
                                    rs.getFloat("amount"),
                                    rs.getString("status").charAt(0),
                                    rs.getBoolean("is_paid")
                            );
                        } else {
                            JOptionPane.showMessageDialog(this, "No Order found with that ID.");
                            return;
                        }
                    }
                }

                String customerName = JOptionPane.showInputDialog(this,
                        "Enter new Customer Name (leave blank to keep '" + current.getCustomer_name() + "'):");
                if (customerName == null) return;
                if (customerName.trim().isEmpty()) customerName = current.getCustomer_name();

                String amountStr = JOptionPane.showInputDialog(this,
                        "Enter new Amount (leave blank to keep '" + current.getAmount() + "'):");
                if (amountStr == null) return;
                float amount = amountStr.trim().isEmpty() ? current.getAmount() : Float.parseFloat(amountStr.trim());

                String statusStr = JOptionPane.showInputDialog(this,
                        "Enter new Status (single char) (leave blank to keep '" + current.getStatus() + "'):");
                if (statusStr == null) return;
                char status = statusStr.trim().isEmpty() ? current.getStatus() : statusStr.trim().charAt(0);

                int paidOption = JOptionPane.showConfirmDialog(this, "Is the order paid?", "Payment Status",
                        JOptionPane.YES_NO_OPTION);
                boolean isPaid = (paidOption == JOptionPane.YES_OPTION);

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Orders SET customer_name=?, amount=?, status=?, is_paid=? WHERE order_id=?")) {
                    ps.setString(1, customerName);
                    ps.setFloat(2, amount);
                    ps.setString(3, String.valueOf(status));
                    ps.setBoolean(4, isPaid);
                    ps.setInt(5, id);
                    ps.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Order updated successfully.");
                loadData();
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid number format. Try again.");
        } catch (SQLException sqle) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + sqle.getMessage());
        }
    }

    private void deleteRecord() {
        String table = (String) tableSelector.getSelectedItem();

        try (Connection conn = getConnection()) {
            if ("Inventory".equals(table)) {
                String idStr = JOptionPane.showInputDialog(this, "Enter the Item ID to delete:");
                if (idStr == null) return;
                int id = Integer.parseInt(idStr.trim());

                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete Inventory item with ID " + id + "?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Inventory WHERE item_id = ?")) {
                        ps.setInt(1, id);
                        int affected = ps.executeUpdate();
                        if (affected == 0) {
                            JOptionPane.showMessageDialog(this, "No Inventory item found with that ID.");
                        } else {
                            JOptionPane.showMessageDialog(this, "Inventory item deleted.");
                        }
                    }
                    loadData();
                }
            } else if ("Orders".equals(table)) {
                String idStr = JOptionPane.showInputDialog(this, "Enter the Order ID to delete:");
                if (idStr == null) return;
                int id = Integer.parseInt(idStr.trim());

                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete Order with ID " + id + "?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Orders WHERE order_id = ?")) {
                        ps.setInt(1, id);
                        int affected = ps.executeUpdate();
                        if (affected == 0) {
                            JOptionPane.showMessageDialog(this, "No Order found with that ID.");
                        } else {
                            JOptionPane.showMessageDialog(this, "Order deleted.");
                        }
                    }
                    loadData();
                }
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid number format. Try again.");
        } catch (SQLException sqle) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + sqle.getMessage());
        }
    }

    // Inventory class
    static class Inventory {
        private int item_id;
        private String item_name;
        private float price;
        private char category;
        private boolean in_stock;

        public Inventory(int item_id, String item_name, float price, char category, boolean in_stock) {
            this.item_id = item_id;
            this.item_name = item_name;
            this.price = price;
            this.category = category;
            this.in_stock = in_stock;
        }

        public int getItem_id() { return item_id; }
        public String getItem_name() { return item_name; }
        public float getPrice() { return price; }
        public char getCategory() { return category; }
        public boolean isIn_stock() { return in_stock; }

        @Override
        public String toString() {
            return String.format("%-4d | %-20s | $%-6.2f | %-3c | %-12s",
                    item_id, item_name, price, category, in_stock ? "In Stock" : "Out of Stock");
        }
    }

    // Order class
    static class Order {
        private int order_id;
        private String customer_name;
        private float amount;
        private char status;
        private boolean is_paid;

        public Order(int order_id, String customer_name, float amount, char status, boolean is_paid) {
            this.order_id = order_id;
            this.customer_name = customer_name;
            this.amount = amount;
            this.status = status;
            this.is_paid = is_paid;
        }

        public int getOrder_id() { return order_id; }
        public String getCustomer_name() { return customer_name; }
        public float getAmount() { return amount; }
        public char getStatus() { return status; }
        public boolean isPaid() { return is_paid; }

        @Override
        public String toString() {
            return String.format("%-4d | %-15s | $%-6.2f | %-6c",
                    order_id, customer_name, amount, status);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(OnlineStoreApp::new);
    }
}