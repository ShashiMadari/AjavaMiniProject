import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

// Book class
class Book {
    private int book_id;
    private String title;
    private String author;
    private int year_published;
    private boolean available;

    public Book(int book_id, String title, String author, int year_published, boolean available) {
        this.book_id = book_id;
        this.title = title;
        this.author = author;
        this.year_published = year_published;
        this.available = available;
    }

    public int getBook_id() { return book_id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYear_published() { return year_published; }
    public boolean isAvailable() { return available; }
}

// Member class
class Member {
    private int member_id;
    private String name;
    private String email;
    private char membership_type;
    private boolean active;

    public Member(int member_id, String name, String email, char membership_type, boolean active) {
        this.member_id = member_id;
        this.name = name;
        this.email = email;
        this.membership_type = membership_type;
        this.active = active;
    }

    public int getMember_id() { return member_id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public char getMembership_type() { return membership_type; }
    public boolean isActive() { return active; }
}

public class LibraryApp extends JFrame {

    // Database connection info - change if needed
    static final String DB_URL = "jdbc:mysql://localhost:3306/LibraryDB";

    static final String USER = "root";   // Change to your MySQL username
    static final String PASS = "";       // Change to your MySQL password

    private JComboBox<String> tableSelector, collectionSelector;
    private JButton loadBtn, insertBtn, updateBtn, deleteBtn;
    private JTable displayTable;
    private DefaultTableModel tableModel;

    private JPanel inputPanel;
    private java.util.List<JTextField> inputFields;
    private JCheckBox availableCheckBox, activeCheckBox;

    private Collection<?> currentCollection;

    public LibraryApp() {
        setTitle("Library Management System");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());

        tableSelector = new JComboBox<>(new String[]{"Books", "Members"});
        collectionSelector = new JComboBox<>(new String[]{"ArrayList", "TreeSet"});

        loadBtn = new JButton("Load");
        insertBtn = new JButton("Insert");
        updateBtn = new JButton("Update");
        deleteBtn = new JButton("Delete");

        topPanel.add(new JLabel("Select Table:"));
        topPanel.add(tableSelector);
        topPanel.add(new JLabel("Select Collection:"));
        topPanel.add(collectionSelector);
        topPanel.add(loadBtn);
        topPanel.add(insertBtn);
        topPanel.add(updateBtn);
        topPanel.add(deleteBtn);

        inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(0, 2, 5, 5));
        createInputFields("Books");

        tableSelector.addActionListener(e -> {
            String selected = (String) tableSelector.getSelectedItem();
            createInputFields(selected);
        });

        displayTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(displayTable);

        add(topPanel, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);

        loadBtn.addActionListener(e -> loadData());
        insertBtn.addActionListener(e -> insertData());
        updateBtn.addActionListener(e -> updateData());
        deleteBtn.addActionListener(e -> deleteData());

        setVisible(true);
    }

    private void createInputFields(String table) {
        inputPanel.removeAll();
        inputFields = new ArrayList<>();

        if (table.equals("Books")) {
            String[] labels = {"Book ID", "Title", "Author", "Year Published"};
            for (String label : labels) {
                inputPanel.add(new JLabel(label + ":"));
                JTextField field = new JTextField();
                inputFields.add(field);
                inputPanel.add(field);
            }
            availableCheckBox = new JCheckBox();
            inputPanel.add(new JLabel("Available:"));
            inputPanel.add(availableCheckBox);

        } else if (table.equals("Members")) {
            String[] labels = {"Member ID", "Name", "Email", "Membership Type (R/P)"};
            for (String label : labels) {
                inputPanel.add(new JLabel(label + ":"));
                JTextField field = new JTextField();
                inputFields.add(field);
                inputPanel.add(field);
            }
            activeCheckBox = new JCheckBox();
            inputPanel.add(new JLabel("Active:"));
            inputPanel.add(activeCheckBox);
        }

        inputPanel.revalidate();
        inputPanel.repaint();
    }

    private void loadData() {
        String selectedTable = (String) tableSelector.getSelectedItem();
        String collectionType = (String) collectionSelector.getSelectedItem();

        if (selectedTable.equals("Books")) {
            if (collectionType.equals("ArrayList")) {
                ArrayList<Book> books = new ArrayList<>();
                try (Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
                     Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT * FROM Books")) {

                    while (rs.next()) {
                        books.add(new Book(
                                rs.getInt("book_id"),
                                rs.getString("title"),
                                rs.getString("author"),
                                rs.getInt("year_published"),
                                rs.getBoolean("available")
                        ));
                    }
                    currentCollection = books;
                    displayBooks(books);

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error loading books: " + ex.getMessage());
                }
            } else {
                TreeSet<Book> books = new TreeSet<>(Comparator.comparingInt(Book::getYear_published));
                try (Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
                     Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT * FROM Books")) {

                    while (rs.next()) {
                        books.add(new Book(
                                rs.getInt("book_id"),
                                rs.getString("title"),
                                rs.getString("author"),
                                rs.getInt("year_published"),
                                rs.getBoolean("available")
                        ));
                    }
                    currentCollection = books;
                    displayBooks(books);

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error loading books: " + ex.getMessage());
                }
            }
        } else if (selectedTable.equals("Members")) {
            if (collectionType.equals("ArrayList")) {
                ArrayList<Member> members = new ArrayList<>();
                try (Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
                     Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT * FROM Members")) {

                    while (rs.next()) {
                        members.add(new Member(
                                rs.getInt("member_id"),
                                rs.getString("name"),
                                rs.getString("email"),
                                rs.getString("membership_type").charAt(0),
                                rs.getBoolean("active")
                        ));
                    }
                    currentCollection = members;
                    displayMembers(members);

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error loading members: " + ex.getMessage());
                }
            } else {
                TreeSet<Member> members = new TreeSet<>(Comparator.comparing(Member::getName));
                try (Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
                     Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT * FROM Members")) {

                    while (rs.next()) {
                        members.add(new Member(
                                rs.getInt("member_id"),
                                rs.getString("name"),
                                rs.getString("email"),
                                rs.getString("membership_type").charAt(0),
                                rs.getBoolean("active")
                        ));
                    }
                    currentCollection = members;
                    displayMembers(members);

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error loading members: " + ex.getMessage());
                }
            }
        }
    }

    private void displayBooks(Collection<Book> books) {
        String[] columns = {"Book ID", "Title", "Author", "Year Published", "Available"};
        tableModel = new DefaultTableModel(columns, 0);
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getBook_id(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getYear_published(),
                    b.isAvailable()
            });
        }
        displayTable.setModel(tableModel);
    }

    private void displayMembers(Collection<Member> members) {
        String[] columns = {"Member ID", "Name", "Email", "Membership Type", "Active"};
        tableModel = new DefaultTableModel(columns, 0);
        for (Member m : members) {
            tableModel.addRow(new Object[]{
                    m.getMember_id(),
                    m.getName(),
                    m.getEmail(),
                    m.getMembership_type(),
                    m.isActive()
            });
        }
        displayTable.setModel(tableModel);
    }

    private void insertData() {
        String selectedTable = (String) tableSelector.getSelectedItem();

        try (Connection con = DriverManager.getConnection(DB_URL, USER, PASS)) {
            if (selectedTable.equals("Books")) {
                int book_id = Integer.parseInt(inputFields.get(0).getText().trim());
                String title = inputFields.get(1).getText().trim();
                String author = inputFields.get(2).getText().trim();
                int year = Integer.parseInt(inputFields.get(3).getText().trim());
                boolean available = availableCheckBox.isSelected();

                String sql = "INSERT INTO Books (book_id, title, author, year_published, available) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, book_id);
                ps.setString(2, title);
                ps.setString(3, author);
                ps.setInt(4, year);
                ps.setBoolean(5, available);

                int rows = ps.executeUpdate();
                JOptionPane.showMessageDialog(this, rows + " Book inserted.");
            } else if (selectedTable.equals("Members")) {
                int member_id = Integer.parseInt(inputFields.get(0).getText().trim());
                String name = inputFields.get(1).getText().trim();
                String email = inputFields.get(2).getText().trim();
                char memType = inputFields.get(3).getText().trim().toUpperCase().charAt(0);
                boolean active = activeCheckBox.isSelected();

                String sql = "INSERT INTO Members (member_id, name, email, membership_type, active) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, member_id);
                ps.setString(2, name);
                ps.setString(3, email);
                ps.setString(4, String.valueOf(memType));
                ps.setBoolean(5, active);

                int rows = ps.executeUpdate();
                JOptionPane.showMessageDialog(this, rows + " Member inserted.");
            }
            loadData();  // Refresh table
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Insert Error: " + ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
        }
    }

    private void updateData() {
        int selectedRow = displayTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to update");
            return;
        }

        String selectedTable = (String) tableSelector.getSelectedItem();

        try (Connection con = DriverManager.getConnection(DB_URL, USER, PASS)) {
            if (selectedTable.equals("Books")) {
                int book_id = Integer.parseInt(inputFields.get(0).getText().trim());
                String title = inputFields.get(1).getText().trim();
                String author = inputFields.get(2).getText().trim();
                int year = Integer.parseInt(inputFields.get(3).getText().trim());
                boolean available = availableCheckBox.isSelected();

                String sql = "UPDATE Books SET title=?, author=?, year_published=?, available=? WHERE book_id=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, title);
                ps.setString(2, author);
                ps.setInt(3, year);
                ps.setBoolean(4, available);
                ps.setInt(5, book_id);

                int rows = ps.executeUpdate();
                JOptionPane.showMessageDialog(this, rows + " Book updated.");
            } else if (selectedTable.equals("Members")) {
                int member_id = Integer.parseInt(inputFields.get(0).getText().trim());
                String name = inputFields.get(1).getText().trim();
                String email = inputFields.get(2).getText().trim();
                char memType = inputFields.get(3).getText().trim().toUpperCase().charAt(0);
                boolean active = activeCheckBox.isSelected();

                String sql = "UPDATE Members SET name=?, email=?, membership_type=?, active=? WHERE member_id=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, String.valueOf(memType));
                ps.setBoolean(4, active);
                ps.setInt(5, member_id);

                int rows = ps.executeUpdate();
                JOptionPane.showMessageDialog(this, rows + " Member updated.");
            }
            loadData(); // Refresh
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Update Error: " + ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
        }
    }

    private void deleteData() {
        int selectedRow = displayTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to delete");
            return;
        }

        String selectedTable = (String) tableSelector.getSelectedItem();

        try (Connection con = DriverManager.getConnection(DB_URL, USER, PASS)) {
            if (selectedTable.equals("Books")) {
                int book_id = Integer.parseInt(displayTable.getValueAt(selectedRow, 0).toString());
                String sql = "DELETE FROM Books WHERE book_id=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, book_id);

                int rows = ps.executeUpdate();
                JOptionPane.showMessageDialog(this, rows + " Book deleted.");
            } else if (selectedTable.equals("Members")) {
                int member_id = Integer.parseInt(displayTable.getValueAt(selectedRow, 0).toString());
                String sql = "DELETE FROM Members WHERE member_id=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, member_id);

                int rows = ps.executeUpdate();
                JOptionPane.showMessageDialog(this, rows + " Member deleted.");
            }
            loadData(); // Refresh
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Delete Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "MySQL JDBC Driver not found. Add the connector jar to classpath.");
            System.exit(1);
        }
        SwingUtilities.invokeLater(() -> new LibraryApp());
    }
}
