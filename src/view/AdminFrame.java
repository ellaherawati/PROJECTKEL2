package view;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminFrame extends JFrame {
    private UserDAO userDAO;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField namaField;
    private JComboBox<String> roleComboBox;
    private JTextField searchField;
    private JButton addButton, updateButton, deleteButton, clearButton, searchButton, refreshButton;
    private int selectedUserId = -1;

    public AdminFrame() {
        userDAO = new UserDAO();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadUserData();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Admin - User Management System");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeComponents() {
        // Table setup
        String[] columnNames = {"ID", "Username", "Password", "Nama", "Role"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Form fields
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        namaField = new JTextField(20);
        roleComboBox = new JComboBox<>(new String[]{"Admin", "User", "Manager"});
        
        // Search field
        searchField = new JTextField(20);
        
        // Buttons
        addButton = new JButton("Add User");
        updateButton = new JButton("Update User");
        deleteButton = new JButton("Delete User");
        clearButton = new JButton("Clear Form");
        searchButton = new JButton("Search");
        refreshButton = new JButton("Refresh");
        
        // Style buttons
        addButton.setBackground(new Color(46, 125, 50));
        addButton.setForeground(Color.WHITE);
        updateButton.setBackground(new Color(25, 118, 210));
        updateButton.setForeground(Color.WHITE);
        deleteButton.setBackground(new Color(211, 47, 47));
        deleteButton.setForeground(Color.WHITE);
        clearButton.setBackground(new Color(117, 117, 117));
        clearButton.setForeground(Color.WHITE);
        searchButton.setBackground(new Color(255, 193, 7));
        refreshButton.setBackground(new Color(156, 39, 176));
        refreshButton.setForeground(Color.WHITE);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(63, 81, 181));
        JLabel titleLabel = new JLabel("User Management System - Admin Panel");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left panel - Form
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "User Details"));
        leftPanel.setPreferredSize(new Dimension(350, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Form fields
        gbc.gridx = 0; gbc.gridy = 0;
        leftPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        leftPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        leftPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        leftPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        leftPanel.add(new JLabel("Nama:"), gbc);
        gbc.gridx = 1;
        leftPanel.add(namaField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        leftPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        leftPanel.add(roleComboBox, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        leftPanel.add(buttonPanel, gbc);

        mainPanel.add(leftPanel, BorderLayout.WEST);

        // Right panel - Table and search
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Users"));
        searchPanel.add(new JLabel("Search by username:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        rightPanel.add(searchPanel, BorderLayout.NORTH);

        // Table panel
        JScrollPane tableScrollPane = new JScrollPane(userTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("User List"));
        rightPanel.add(tableScrollPane, BorderLayout.CENTER);

        mainPanel.add(rightPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(240, 240, 240));
        statusPanel.add(new JLabel("Ready"));
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void setupEventListeners() {
        // Table selection listener
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow != -1) {
                    selectedUserId = (Integer) tableModel.getValueAt(selectedRow, 0);
                    usernameField.setText((String) tableModel.getValueAt(selectedRow, 1));
                    passwordField.setText((String) tableModel.getValueAt(selectedRow, 2));
                    namaField.setText((String) tableModel.getValueAt(selectedRow, 3));
                    roleComboBox.setSelectedItem((String) tableModel.getValueAt(selectedRow, 4));
                }
            }
        });

        // Button listeners
        addButton.addActionListener(e -> addUser());
        updateButton.addActionListener(e -> updateUser());
        deleteButton.addActionListener(e -> deleteUser());
        clearButton.addActionListener(e -> clearForm());
        searchButton.addActionListener(e -> searchUser());
        refreshButton.addActionListener(e -> loadUserData());

        // Enter key listeners for search
        searchField.addActionListener(e -> searchUser());
    }

    private void loadUserData() {
        tableModel.setRowCount(0);
        List<User> users = userDAO.findAll();
        for (User user : users) {
            Object[] rowData = {
                user.getUserId(),
                user.getUsername(),
                user.getPassword(),
                user.getNama(),
                user.getRole()
            };
            tableModel.addRow(rowData);
        }
    }

    private void addUser() {
        if (validateInput()) {
            User user = new User(
                usernameField.getText().trim(),
                new String(passwordField.getPassword()),
                namaField.getText().trim(),
                (String) roleComboBox.getSelectedItem()
            );

            if (userDAO.create(user)) {
                JOptionPane.showMessageDialog(this, "User added successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadUserData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add user!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateUser() {
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to update!", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (validateInput()) {
            User user = new User(
                usernameField.getText().trim(),
                new String(passwordField.getPassword()),
                namaField.getText().trim(),
                (String) roleComboBox.getSelectedItem()
            );
            user.setUserId(selectedUserId);

            if (userDAO.update(user)) {
                JOptionPane.showMessageDialog(this, "User updated successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadUserData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update user!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteUser() {
        if (selectedUserId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete!", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this user?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (userDAO.delete(selectedUserId)) {
                JOptionPane.showMessageDialog(this, "User deleted successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadUserData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void searchUser() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadUserData();
            return;
        }

        User user = userDAO.findByUsername(searchTerm);
        tableModel.setRowCount(0);
        
        if (user != null) {
            Object[] rowData = {
                user.getUserId(),
                user.getUsername(),
                user.getPassword(),
                user.getNama(),
                user.getRole()
            };
            tableModel.addRow(rowData);
        } else {
            JOptionPane.showMessageDialog(this, "User not found!", 
                "Search Result", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearForm() {
        usernameField.setText("");
        passwordField.setText("");
        namaField.setText("");
        roleComboBox.setSelectedIndex(0);
        selectedUserId = -1;
        userTable.clearSelection();
    }

    private boolean validateInput() {
        if (usernameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return false;
        }

        if (passwordField.getPassword().length == 0) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            passwordField.requestFocus();
            return false;
        }

        if (namaField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama cannot be empty!", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            namaField.requestFocus();
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new AdminFrame();
        });
    }
}