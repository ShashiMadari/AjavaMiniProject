import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class MiniProjectApp1 extends JFrame {

    JComboBox<String> tableSelector;
    JComboBox<String> columnSelector;
    JComboBox<String> collectionSelector;
    JButton fetchButton;
    JLabel statusBar;
    JTable resultTable;
    DefaultTableModel tableModel;

    Connection conn;

    public MiniProjectApp1() {
        setTitle("🌐 Java Mini Project - Database Viewer");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(34, 40, 49));

        UIManager.put("ComboBox.background", new Color(57, 62, 70));
        UIManager.put("ComboBox.foreground", Color.WHITE);
        UIManager.put("Button.background", new Color(0, 173, 181));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Label.foreground", Color.WHITE);

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

        tableModel = new DefaultTableModel();
        resultTable = new JTable(tableModel);
        resultTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resultTable.setRowHeight(25);
        resultTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(resultTable);

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

            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            if (fullView || column.equals("* (All Columns)")) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                for (int i = 1; i <= colCount; i++) {
                    tableModel.addColumn(meta.getColumnName(i));
                }

                while (rs.next()) {
                    Object[] row = new Object[colCount];
                    for (int i = 0; i < colCount; i++) {
                        row[i] = rs.getObject(i + 1);
                    }
                    collection.add(row);
                }

                List<Object[]> sortedRows = new ArrayList<>();
                for (Object obj : collection) {
                    sortedRows.add((Object[]) obj);
                }

                sortedRows.sort(Comparator.comparing(row -> Arrays.toString(row)));

                for (Object[] row : sortedRows) {
                    tableModel.addRow(row);
                }
            } else {
                tableModel.addColumn(column);

                while (rs.next()) {
                    collection.add(rs.getObject(1));
                }

                List<Object> sortedList = new ArrayList<>(collection);
                sortedList.sort(Comparator.comparing(Object::toString));

                for (Object val : sortedList) {
                    tableModel.addRow(new Object[]{val});
                }
            }

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
        SwingUtilities.invokeLater(MiniProjectApp1::new);
    }
}
