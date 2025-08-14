import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;


public class MiniProjectApp extends JFrame {

    JComboBox<String> tableSelector;
    JComboBox<String> columnSelector;
    JComboBox<String> collectionSelector;
    JTextArea resultArea;
    JButton fetchButton;
    JLabel statusBar;

    Connection conn;

    public MiniProjectApp() {
        setTitle("🌐 Java Mini Project - Database Viewer");
        setSize(700, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(34, 40, 49));

        UIManager.put("ComboBox.background", new Color(57, 62, 70));
        UIManager.put("ComboBox.foreground", Color.WHITE);
        UIManager.put("Button.background", new Color(0, 173, 181));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Label.foreground", Color.WHITE);

        // Font customization
        Font uiFont = new Font("Segoe UI", Font.PLAIN, 14);

        JPanel topPanel = new JPanel(new GridLayout(5, 2, 10, 5));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(new Color(34, 40, 49));

        tableSelector = new JComboBox<>(new String[]{"employees", "products"});
        columnSelector = new JComboBox<>();
        collectionSelector = new JComboBox<>(new String[]{"ArrayList", "LinkedList"});
        fetchButton = new JButton("🚀 Fetch Data");
        JCheckBox fullTableViewCheckBox = new JCheckBox("🧾 Full Table View");
        fullTableViewCheckBox.setBackground(new Color(34, 40, 49));
        fullTableViewCheckBox.setForeground(Color.WHITE);

        JLabel tableLabel = new JLabel("📋 Select Table:");
        JLabel columnLabel = new JLabel("📌 Select Column:");
        JLabel collectionLabel = new JLabel("📦 Select Collection:");

        tableLabel.setFont(uiFont);
        columnLabel.setFont(uiFont);
        collectionLabel.setFont(uiFont);

        topPanel.add(tableLabel);
        topPanel.add(tableSelector);
        topPanel.add(columnLabel);
        topPanel.add(columnSelector);
        topPanel.add(collectionLabel);
        topPanel.add(collectionSelector);
        topPanel.add(fullTableViewCheckBox);
        topPanel.add(fetchButton);

        resultArea = new JTextArea();
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        resultArea.setBackground(new Color(238, 238, 238));
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        statusBar = new JLabel("🔌 Not Connected");
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(0, 173, 181));
        statusBar.setForeground(Color.BLACK);
        statusBar.setBorder(new EmptyBorder(5, 10, 5, 10));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        connectToDatabase();
        populateColumns("employees");

        tableSelector.addActionListener(e -> populateColumns((String) tableSelector.getSelectedItem()));

        fetchButton.addActionListener(e -> fetchData(fullTableViewCheckBox.isSelected()));

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb", "root", "");
            statusBar.setText("✅ Connected to testdb");
        } catch (Exception e) {
            statusBar.setText("❌ Connection Failed");
            showError("Database Connection Error", e.getMessage());
        }
    }

    void populateColumns(String tableName) {
        try {
            columnSelector.removeAllItems();
            columnSelector.addItem("* (All Columns)");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName + " LIMIT 1");
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                columnSelector.addItem(meta.getColumnName(i));
            }
        } catch (Exception e) {
            showError("Column Load Error", e.getMessage());
        }
    }

    void fetchData(boolean fullView) {
        String table = (String) tableSelector.getSelectedItem();
        String column = (String) columnSelector.getSelectedItem();
        String collectionType = (String) collectionSelector.getSelectedItem();

        try {
            statusBar.setText("📡 Fetching data...");
            String query = fullView || column.equals("* (All Columns)") ? "SELECT * FROM " + table
                    : "SELECT " + column + " FROM " + table;

            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            Collection<Object> collection = "ArrayList".equals(collectionType) ? new ArrayList<>() : new LinkedList<>();

            if (fullView || column.equals("* (All Columns)")) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                while (rs.next()) {
                    StringBuilder row = new StringBuilder();
                    for (int i = 1; i <= colCount; i++) {
                        row.append(meta.getColumnName(i)).append(": ").append(rs.getObject(i)).append(" | ");
                    }
                    collection.add(row.toString());
                }
            } else {
                while (rs.next()) {
                    collection.add(rs.getObject(1));
                }
            }

            List<Object> sortedList = new ArrayList<>(collection);
            sortedList.sort(Comparator.comparing(Object::toString));

            resultArea.setText("📊 Data from " + (fullView ? "all columns" : "column `" + column + "`") + " in `" + table + "`:\n\n");

            resultArea.append("🔁 Using for-each loop:\n");
            for (Object val : sortedList) {
                resultArea.append(val.toString() + "\n");
            }

            resultArea.append("\n🔄 Using Iterator:\n");
            Iterator<Object> it = sortedList.iterator();
            while (it.hasNext()) {
                resultArea.append(it.next().toString() + "\n");
            }

            // Scroll to bottom
            resultArea.setCaretPosition(resultArea.getDocument().getLength());

            statusBar.setText("✅ Data fetched successfully. Rows: " + collection.size());
        } catch (Exception e) {
            showError("Data Fetch Error", e.getMessage());
            statusBar.setText("❌ Error fetching data");
        }
    }

    void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MiniProjectApp::new);
    }
}
