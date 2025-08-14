import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class RoomManagerApp extends JFrame {
    private JComboBox<String> tableSelector;
    private JComboBox<String> columnSelector;
    private JComboBox<String> collectionSelector;
    private JTextArea resultArea;
    private JButton viewFullTableBtn;
    private JButton loadButton;
    private JButton insertButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton sortButton;

    private JPanel cardPanel;
    private CardLayout cardLayout;

    private Connection conn;

    public RoomManagerApp() {
        setTitle("🏨 Advanced Room Management System");
        setSize(900, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initDBConnection();
        setupUI();
    }

    private void initDBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/room_manager", "root", "");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Database connection failed: " + e.getMessage(), 
                "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupUI() {
        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(230, 242, 255);
                Color color2 = new Color(255, 255, 255);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Top Panel for controls
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(6, 2, 10, 10));
        topPanel.setBorder(new CompoundBorder(
            new TitledBorder(
                new LineBorder(new Color(0, 120, 215), 2), 
                "🔧 Database Operations",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(0, 90, 158)
            ),
            new EmptyBorder(5, 5, 5, 5)
        ));
        topPanel.setOpaque(false);

        tableSelector = new JComboBox<>(new String[]{"room_booking", "maintenance_log"});
        tableSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        columnSelector = new JComboBox<>();
        columnSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        collectionSelector = new JComboBox<>(new String[]{"ArrayList", "TreeSet"});
        collectionSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Bright colored buttons
       loadButton = createStyledButton("📥 Load Collection", new Color(33, 150, 243));   // #2196F3
viewFullTableBtn = createStyledButton("📊 View Full Table", new Color(76, 175, 80)); // #4CAF50
insertButton = createStyledButton("➕ Insert Record", new Color(0, 150, 136));      // #009688
updateButton = createStyledButton("🔄 Update Record", new Color(255, 152, 0));     // #FF9800
deleteButton = createStyledButton("❌ Delete Record", new Color(244, 67, 54));     // #F44336
sortButton = createStyledButton("🔽 Sort Data", new Color(156, 39, 176));         // #9C27B0

        topPanel.add(createStyledLabel("📁 Select Table:"));
        topPanel.add(tableSelector);
        topPanel.add(createStyledLabel("🔎 Select Column:"));
        topPanel.add(columnSelector);
        topPanel.add(createStyledLabel("🧺 Select Collection:"));
        topPanel.add(collectionSelector);
        topPanel.add(loadButton);
        topPanel.add(viewFullTableBtn);
        topPanel.add(insertButton);
        topPanel.add(updateButton);
        topPanel.add(deleteButton);
        topPanel.add(sortButton);

        // Result Area
        resultArea = new JTextArea();
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        resultArea.setEditable(false);
        resultArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200)));

        // Table Panel
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        cardPanel.add(scrollPane, "collection");

        JPanel tablePanel = new JPanel(new BorderLayout());
        cardPanel.add(tablePanel, "table");

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);

        // Status bar
        JLabel statusBar = new JLabel(" Ready");
        statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBar.setForeground(new Color(70, 70, 70));
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        add(mainPanel);

        // Events
        tableSelector.addActionListener(e -> updateColumns());
        loadButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "collection");
            loadData();
        });
        viewFullTableBtn.addActionListener(e -> {
            cardLayout.show(cardPanel, "table");
            showFullTable(tableSelector.getSelectedItem().toString(), tablePanel);
        });
        insertButton.addActionListener(e -> showInsertDialog());
        updateButton.addActionListener(e -> showUpdateDialog());
        deleteButton.addActionListener(e -> showDeleteDialog());
        sortButton.addActionListener(e -> sortData());

        updateColumns();
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(
            new LineBorder(bgColor.darker(), 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(70, 70, 70));
        return label;
    }

    private void updateColumns() {
        columnSelector.removeAllItems();
        String table = (String) tableSelector.getSelectedItem();
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, table, null);
            while (rs.next()) {
                columnSelector.addItem(rs.getString("COLUMN_NAME"));
            }
        } catch (SQLException e) {
            showError("Failed to load columns: " + e.getMessage());
        }
    }

    private void loadData() {
    String table = (String) tableSelector.getSelectedItem();
    String column = (String) columnSelector.getSelectedItem();
    String collectionType = (String) collectionSelector.getSelectedItem();

    Collection<String> collection;
    StringBuilder explanation = new StringBuilder("\n📌 Collection Type: " + collectionType + "\n\n");

    switch (collectionType) {
        case "ArrayList":
            collection = new ArrayList<>();
            explanation.append("✔ Maintains insertion order\n");
            explanation.append("✔ Allows duplicates\n");
            explanation.append("✔ Fast random access\n");
            break;
        case "TreeSet":
            // Enhanced comparator for better name sorting
            collection = new TreeSet<>((s1, s2) -> {
                // Handle null values
                if (s1 == null && s2 == null) return 0;
                if (s1 == null) return -1;
                if (s2 == null) return 1;
                
                // Trim whitespace and compare case-insensitively
                return s1.trim().compareToIgnoreCase(s2.trim());
            });
            explanation.append("✔ Automatically sorted alphabetically\n");
            explanation.append("✔ No duplicates\n");
            explanation.append("✔ Case-insensitive sorting with whitespace trimming\n");
            break;
        default:
            collection = new ArrayList<>();
    }

    try {
        Statement stmt = conn.createStatement();
        String query = "SELECT " + column + " FROM " + table;
        
        // If using ArrayList and column contains "name", sort in SQL
        if (collection instanceof ArrayList && column.toLowerCase().contains("name")) {
            query += " ORDER BY " + column;
        }
        
        ResultSet rs = stmt.executeQuery(query);
        
        while (rs.next()) {
            String value = rs.getString(1);
            collection.add(value);
        }

        List<String> displayList = new ArrayList<>(collection);

        StringBuilder sb = new StringBuilder(explanation.toString());
        sb.append("\n📄 Retrieved Values (").append(displayList.size()).append(" items):\n\n");

        int count = 1;
        for (String item : displayList) {
            sb.append(String.format("%3d. %s%n", count++, item));
        }

        resultArea.setText(sb.toString());
    } catch (SQLException e) {
        showError("Query failed: " + e.getMessage());
    }
}

    private void showFullTable(String tableName, JPanel tablePanel) {
        tablePanel.removeAll();
        try {
            // Check if table has a 'name' column for sorting
            boolean hasNameColumn = false;
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet columns = meta.getColumns(null, null, tableName, null);
            while (columns.next()) {
                if (columns.getString("COLUMN_NAME").equalsIgnoreCase("name")) {
                    hasNameColumn = true;
                    break;
                }
            }
            
            String query = "SELECT * FROM " + tableName;
            if (hasNameColumn) {
                query += " ORDER BY name";
            }
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            Vector<String> columnNames = new Vector<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i));
            }

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                data.add(row);
            }

            JTable table = new JTable(new DefaultTableModel(data, columnNames));
            table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            table.setRowHeight(25);
            table.setSelectionBackground(new Color(0, 120, 215));
            table.setSelectionForeground(Color.WHITE);
            table.setGridColor(new Color(220, 220, 220));
            
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(new LineBorder(new Color(200, 200, 200)));
            tablePanel.add(scrollPane, BorderLayout.CENTER);
            tablePanel.revalidate();
            tablePanel.repaint();

        } catch (SQLException e) {
            showError("Failed to load full table: " + e.getMessage());
        }
    }

    private void showInsertDialog() {
        String table = (String) tableSelector.getSelectedItem();
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, table, null);
            
            JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
            List<JTextField> fields = new ArrayList<>();
            
            while (rs.next()) {
                String colName = rs.getString("COLUMN_NAME");
                String colType = rs.getString("TYPE_NAME");
                
                panel.add(new JLabel(colName + " (" + colType + "):"));
                JTextField field = new JTextField();
                fields.add(field);
                panel.add(field);
            }
            
            int result = JOptionPane.showConfirmDialog(
                this, panel, "Insert New Record", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                StringBuilder columns = new StringBuilder();
                StringBuilder values = new StringBuilder();
                
                rs.beforeFirst();
                int i = 0;
                while (rs.next()) {
                    if (i > 0) {
                        columns.append(", ");
                        values.append(", ");
                    }
                    columns.append(rs.getString("COLUMN_NAME"));
                    
                    String value = fields.get(i).getText();
                    if (rs.getString("TYPE_NAME").toLowerCase().contains("char") || 
                        rs.getString("TYPE_NAME").toLowerCase().contains("text")) {
                        values.append("'").append(value).append("'");
                    } else {
                        values.append(value);
                    }
                    i++;
                }
                
                String sql = "INSERT INTO " + table + " (" + columns + ") VALUES (" + values + ")";
                Statement stmt = conn.createStatement();
                stmt.executeUpdate(sql);
                
                JOptionPane.showMessageDialog(this, "Record inserted successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            showError("Failed to insert record: " + e.getMessage());
        }
    }

    private void showUpdateDialog() {
        String table = (String) tableSelector.getSelectedItem();
        try {
            // First get primary key column
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet pkRs = meta.getPrimaryKeys(null, null, table);
            String pkColumn = null;
            if (pkRs.next()) {
                pkColumn = pkRs.getString("COLUMN_NAME");
            }
            pkRs.close();
            
            if (pkColumn == null) {
                showError("Cannot update - no primary key found for table " + table);
                return;
            }
            
            // Get all columns
            ResultSet rs = meta.getColumns(null, null, table, null);
            
            JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
            List<JTextField> fields = new ArrayList<>();
            JComboBox<String> idCombo = new JComboBox<>();
            
            // Load existing IDs
            Statement stmt = conn.createStatement();
            ResultSet idRs = stmt.executeQuery("SELECT " + pkColumn + " FROM " + table);
            while (idRs.next()) {
                idCombo.addItem(idRs.getString(1));
            }
            idRs.close();
            
            panel.add(new JLabel("Select " + pkColumn + " to update:"));
            panel.add(idCombo);
            
            while (rs.next()) {
                String colName = rs.getString("COLUMN_NAME");
                if (colName.equals(pkColumn)) continue;
                
                String colType = rs.getString("TYPE_NAME");
                panel.add(new JLabel(colName + " (" + colType + "):"));
                JTextField field = new JTextField();
                fields.add(field);
                panel.add(field);
            }
            
            int result = JOptionPane.showConfirmDialog(
                this, panel, "Update Record", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                StringBuilder setClause = new StringBuilder();
                rs.beforeFirst();
                int i = 0;
                
                while (rs.next()) {
                    String colName = rs.getString("COLUMN_NAME");
                    if (colName.equals(pkColumn)) continue;
                    
                    if (i > 0) {
                        setClause.append(", ");
                    }
                    
                    String value = fields.get(i).getText();
                    setClause.append(colName).append("=");
                    
                    if (rs.getString("TYPE_NAME").toLowerCase().contains("char") || 
                        rs.getString("TYPE_NAME").toLowerCase().contains("text")) {
                        setClause.append("'").append(value).append("'");
                    } else {
                        setClause.append(value);
                    }
                    i++;
                }
                
                String sql = "UPDATE " + table + " SET " + setClause + 
                    " WHERE " + pkColumn + "=" + idCombo.getSelectedItem();
                stmt.executeUpdate(sql);
                
                JOptionPane.showMessageDialog(this, "Record updated successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            showError("Failed to update record: " + e.getMessage());
        }
    }

    private void showDeleteDialog() {
        String table = (String) tableSelector.getSelectedItem();
        try {
            // Get primary key column
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet pkRs = meta.getPrimaryKeys(null, null, table);
            String pkColumn = null;
            if (pkRs.next()) {
                pkColumn = pkRs.getString("COLUMN_NAME");
            }
            pkRs.close();
            
            if (pkColumn == null) {
                showError("Cannot delete - no primary key found for table " + table);
                return;
            }
            
            JComboBox<String> idCombo = new JComboBox<>();
            
            // Load existing IDs
            Statement stmt = conn.createStatement();
            ResultSet idRs = stmt.executeQuery("SELECT " + pkColumn + " FROM " + table);
            while (idRs.next()) {
                idCombo.addItem(idRs.getString(1));
            }
            idRs.close();
            
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.add(new JLabel("Select " + pkColumn + " to delete:"), BorderLayout.NORTH);
            panel.add(idCombo, BorderLayout.CENTER);
            
            int result = JOptionPane.showConfirmDialog(
                this, panel, "Delete Record", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                String sql = "DELETE FROM " + table + 
                    " WHERE " + pkColumn + "=" + idCombo.getSelectedItem();
                int rows = stmt.executeUpdate(sql);
                
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Record deleted successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No record was deleted!", 
                        "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (SQLException e) {
            showError("Failed to delete record: " + e.getMessage());
        }
    }

    private void sortData() {
    String table = (String) tableSelector.getSelectedItem();
    String column = (String) columnSelector.getSelectedItem();
    
    try {
        Statement stmt = conn.createStatement();
        // Enhanced sorting - natural order for names, numeric for IDs
        String orderBy = column.toLowerCase().contains("name") ? 
            " ORDER BY " + column + " COLLATE NOCASE" : 
            " ORDER BY " + column;
            
        ResultSet rs = stmt.executeQuery("SELECT " + column + " FROM " + table + orderBy);
        
        StringBuilder sb = new StringBuilder("\n📄 Sorted Values");
        sb.append(column.toLowerCase().contains("name") ? " (Alphabetical Order):" : " (Natural Order):");
        sb.append("\n\n");
        
        int count = 1;
        while (rs.next()) {
            sb.append(String.format("%3d. %s%n", count++, rs.getString(1)));
        }
        
        resultArea.setText(sb.toString());
        cardLayout.show(cardPanel, "collection");
    } catch (SQLException e) {
        showError("Failed to sort data: " + e.getMessage());
    }
}
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, "❌ " + message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new RoomManagerApp().setVisible(true);
        });
    }
}