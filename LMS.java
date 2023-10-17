package p1;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.JPasswordField;
import javax.swing.table.DefaultTableModel;

public class LibraryManagementSystem extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/lib";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345";

    private JTextField titleField, authorField, genreField;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField usernameField, passwordField;
    private boolean loggedIn = false; // Track login state

    public LibraryManagementSystem() {
        setTitle("Library Management System");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        titleField = new JTextField(20);
        authorField = new JTextField(20);
        genreField = new JTextField(20);
        JButton addButton = new JButton("Add Book");
        JButton deleteButton = new JButton("Delete Book");
        JButton searchButton = new JButton("Search Book");
        JButton findSimilarButton = new JButton("Find Similar Books");
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20); // Use JPasswordField for password input
        JButton loginButton = new JButton("Login");

        loginPanel.add(new JLabel("Username: "));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password: "));
        loginPanel.add(passwordField);
        loginPanel.add(loginButton);

        inputPanel.add(loginPanel);
        inputPanel.add(new JLabel("Title: "));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Author: "));
        inputPanel.add(authorField);
        inputPanel.add(new JLabel("Genre: "));
        inputPanel.add(genreField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(findSimilarButton);

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        tableModel.addColumn("ID");
        tableModel.addColumn("Title");
        tableModel.addColumn("Author");
        tableModel.addColumn("Genre");

        JScrollPane tableScrollPane = new JScrollPane(table);

        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (loggedIn) {
                    addBook();
                } else {
                    JOptionPane.showMessageDialog(LibraryManagementSystem.this, "Please log in first.", "Login Required", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (loggedIn) {
                    deleteBook();
                } else {
                    JOptionPane.showMessageDialog(LibraryManagementSystem.this, "Please log in first.", "Login Required", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!loggedIn) {
                    handleLogin();
                } else {
                    JOptionPane.showMessageDialog(LibraryManagementSystem.this, "You are already logged in.", "Logged In", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (loggedIn) {
                    searchBook();
                } else {
                    JOptionPane.showMessageDialog(LibraryManagementSystem.this, "Please log in first.", "Login Required", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        findSimilarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (loggedIn) {
                    findSimilarBooks();
                } else {
                    JOptionPane.showMessageDialog(LibraryManagementSystem.this, "Please log in first.", "Login Required", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM kitab");

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String genre = resultSet.getString("genre");
                tableModel.addRow(new Object[]{id, title, author, genre});
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryManagementSystem());
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(((JPasswordField) passwordField).getPassword());

        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Check if the user credentials are valid
            PreparedStatement loginStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");
            loginStatement.setString(1, username);
            loginStatement.setString(2, password);
            ResultSet loginResult = loginStatement.executeQuery();

            if (loginResult.next()) {
                loginResult.close();
                loginStatement.close();
                connection.close();

                loggedIn = true;

                // Remove the login form and display the main application
                
                revalidate();
                repaint();
                JOptionPane.showMessageDialog(LibraryManagementSystem.this, "You are already logged in.", "Logged In", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password. Please try again.", "Login Error", JOptionPane.ERROR_MESSAGE);
                loginResult.close();
                loginStatement.close();
                connection.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addBook() {
        String title = titleField.getText();
        String author = authorField.getText();
        String genre = genreField.getText();

        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement preparedStatement = connection
                    .prepareStatement("INSERT INTO kitab (title, author, genre) VALUES (?, ?, ?)");
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, author);
            preparedStatement.setString(3, genre);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                tableModel.addRow(new Object[]{getLastInsertedID(connection), title, author, genre});
                titleField.setText("");
                authorField.setText("");
                genreField.setText("");
            }
            preparedStatement.close();
            connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteBook() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int bookID = (int) tableModel.getValueAt(selectedRow, 0);
            try {
                Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM kitab WHERE id = ?");
                preparedStatement.setInt(1, bookID);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    tableModel.removeRow(selectedRow);
                }
                preparedStatement.close();
                connection.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void searchBook() {
        String searchTitle = titleField.getText();
        String searchAuthor = authorField.getText();

        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }

        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement preparedStatement = connection
                    .prepareStatement("SELECT * FROM kitab WHERE title LIKE ? AND author LIKE ?");
            preparedStatement.setString(1, "%" + searchTitle + "%");
            preparedStatement.setString(2, "%" + searchAuthor + "%");

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String genre = resultSet.getString("genre");
                tableModel.addRow(new Object[]{id, title, author, genre});
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void findSimilarBooks() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            String selectedGenre = (String) tableModel.getValueAt(selectedRow, 3);

            try {
                Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement preparedStatement = connection
                        .prepareStatement("SELECT * FROM kitab WHERE genre = ?");
                preparedStatement.setString(1, selectedGenre);

                // Clear the table before populating with similar books
                while (tableModel.getRowCount() > 0) {
                    tableModel.removeRow(0);
                }

                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String title = resultSet.getString("title");
                    String author = resultSet.getString("author");
                    String genre = resultSet.getString("genre");
                    tableModel.addRow(new Object[]{id, title, author, genre});
                }

                resultSet.close();
                preparedStatement.close();
                connection.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private int getLastInsertedID(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT LAST_INSERT_ID()");
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        return resultSet.getInt(1);
    }
}
