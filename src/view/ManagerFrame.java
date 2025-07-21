package view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class ManagerFrame extends JFrame {
    private MenuDAO menuDAO;
    private JTable menuTable;
    private DefaultTableModel tableModel;
    private JTextField namaField, jenisField, hargaField;
    private JTextArea descField; // Added description field
    private JScrollPane descScrollPane; // Added scroll pane for description
    private JComboBox<String> ketersediaanCombo, jenisFilterCombo;
    private JButton addButton, updateButton, deleteButton, refreshButton, clearButton;
    private JButton uploadImageButton, removeImageButton;
    private JLabel imageLabel;
    private int selectedMenuId = -1;
    private String currentImagePath = null;
    
    // Constants for image handling
    private static final String IMAGES_FOLDER = "images/menu/";
    private static final int IMAGE_WIDTH = 150;
    private static final int IMAGE_HEIGHT = 100;

    public ManagerFrame() {
        menuDAO = new MenuDAO();
        initializeImagesFolder();
        initializeComponents();
        setupLayout();
        loadMenuData();
        setupEventHandlers();
        
        setTitle("Menu Manager with Description & Image - Restaurant Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 900); // Increased size to accommodate description field
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void initializeImagesFolder() {
        try {
            Path imagesPath = Paths.get(IMAGES_FOLDER);
            if (!Files.exists(imagesPath)) {
                Files.createDirectories(imagesPath);
                System.out.println("Created images folder: " + IMAGES_FOLDER);
            }
        } catch (IOException e) {
            System.err.println("Error creating images folder: " + e.getMessage());
        }
    }

    private void initializeComponents() {
        // Table setup - Added Description column
        String[] columnNames = {"ID", "Nama Menu", "Jenis Menu", "Harga", "Ketersediaan", "Deskripsi", "Gambar"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        menuTable = new JTable(tableModel);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuTable.setRowHeight(35); // Increased row height for better visibility
        
        // Set column widths
        menuTable.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
        menuTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Nama Menu
        menuTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Jenis Menu
        menuTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Harga
        menuTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Ketersediaan
        menuTable.getColumnModel().getColumn(5).setPreferredWidth(200); // Deskripsi
        menuTable.getColumnModel().getColumn(6).setPreferredWidth(60);  // Gambar
        
        menuTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedMenuToForm();
            }
        });

        // Form fields
        namaField = new JTextField(20);
        jenisField = new JTextField(20);
        hargaField = new JTextField(20);
        
        // Description field with scroll pane
        descField = new JTextArea(4, 25);
        descField.setLineWrap(true);
        descField.setWrapStyleWord(true);
        descField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        descScrollPane = new JScrollPane(descField);
        descScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        descScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        descScrollPane.setBorder(BorderFactory.createTitledBorder("Deskripsi Menu"));
        
        String[] ketersediaanOptions = {"1", "0"}; // 1 = Tersedia, 0 = Tidak Tersedia
        ketersediaanCombo = new JComboBox<>(ketersediaanOptions);
        ketersediaanCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if ("1".equals(value)) {
                    setText("Tersedia");
                } else if ("0".equals(value)) {
                    setText("Tidak Tersedia");
                }
                return this;
            }
        });
        
        String[] jenisFilterOptions = {"Semua", "makanan", "minuman", "lainnya"};
        jenisFilterCombo = new JComboBox<>(jenisFilterOptions);

        // Image components
        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setText("No Image");
        imageLabel.setOpaque(true);
        imageLabel.setBackground(Color.LIGHT_GRAY);

        // Buttons
        addButton = new JButton("Tambah Menu");
        updateButton = new JButton("Update Menu");
        deleteButton = new JButton("Hapus Menu");
        refreshButton = new JButton("Refresh");
        clearButton = new JButton("Clear Form");
        uploadImageButton = new JButton("Upload Gambar");
        removeImageButton = new JButton("Hapus Gambar");

        // Initial button states
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        removeImageButton.setEnabled(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Top Panel - Filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter"));
        filterPanel.add(new JLabel("Filter Jenis:"));
        filterPanel.add(jenisFilterCombo);
        filterPanel.add(refreshButton);

        // Center Panel - Table
        JScrollPane tableScrollPane = new JScrollPane(menuTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Daftar Menu"));

        // Bottom Panel - Form with Image and Description
        JPanel formPanel = createFormPanel();

        // Button Panel
        JPanel buttonPanel = createButtonPanel();

        // Main layout
        add(filterPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Menu"));

        // Main form content
        JPanel mainFormPanel = new JPanel(new BorderLayout());

        // Top part - Basic fields
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Row 1
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        fieldsPanel.add(new JLabel("Nama Menu:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        fieldsPanel.add(namaField, gbc);
        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        fieldsPanel.add(new JLabel("Jenis Menu:"), gbc);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        fieldsPanel.add(jenisField, gbc);

        // Row 2
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        fieldsPanel.add(new JLabel("Harga:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        fieldsPanel.add(hargaField, gbc);
        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        fieldsPanel.add(new JLabel("Ketersediaan:"), gbc);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        fieldsPanel.add(ketersediaanCombo, gbc);

        mainFormPanel.add(fieldsPanel, BorderLayout.NORTH);

        // Bottom part - Description and Image
        JPanel descAndImagePanel = new JPanel(new BorderLayout());
        
        // Description panel (left side)
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.add(descScrollPane, BorderLayout.CENTER);
        
        // Image panel (right side)
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createTitledBorder("Gambar Menu"));
        imagePanel.setPreferredSize(new Dimension(200, 150));
        
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        
        JPanel imageButtonPanel = new JPanel(new FlowLayout());
        imageButtonPanel.add(uploadImageButton);
        imageButtonPanel.add(removeImageButton);
        imagePanel.add(imageButtonPanel, BorderLayout.SOUTH);

        descAndImagePanel.add(descPanel, BorderLayout.CENTER);
        descAndImagePanel.add(imagePanel, BorderLayout.EAST);
        
        mainFormPanel.add(descAndImagePanel, BorderLayout.CENTER);

        formPanel.add(mainFormPanel, BorderLayout.CENTER);

        return formPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        return buttonPanel;
    }

    private void setupEventHandlers() {
        addButton.addActionListener(e -> addMenu());
        updateButton.addActionListener(e -> updateMenu());
        deleteButton.addActionListener(e -> deleteMenu());
        refreshButton.addActionListener(e -> loadMenuData());
        clearButton.addActionListener(e -> clearForm());
        uploadImageButton.addActionListener(e -> uploadImage());
        removeImageButton.addActionListener(e -> removeImage());
        
        jenisFilterCombo.addActionListener(e -> filterMenuByJenis());
    }

    private void uploadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Pilih Gambar Menu");
        fileChooser.setAcceptAllFileFilterUsed(false);
        
        // Add file filter for images
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image files", "jpg", "jpeg", "png", "gif", "bmp");
        fileChooser.addChoosableFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                // Validate image file
                BufferedImage image = ImageIO.read(selectedFile);
                if (image == null) {
                    JOptionPane.showMessageDialog(this, 
                        "File yang dipilih bukan gambar yang valid!", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Generate new filename
                String extension = getFileExtension(selectedFile.getName());
                String newFileName = generateFileName() + "." + extension;
                String newFilePath = IMAGES_FOLDER + newFileName;

                // Copy file to images folder
                Path sourcePath = selectedFile.toPath();
                Path targetPath = Paths.get(newFilePath);
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Update current image path and display
                currentImagePath = newFilePath;
                displayImage(currentImagePath);
                removeImageButton.setEnabled(true);

                JOptionPane.showMessageDialog(this, 
                    "Gambar berhasil diupload!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error uploading image: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeImage() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Apakah Anda yakin ingin menghapus gambar?",
            "Konfirmasi Hapus Gambar",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            currentImagePath = null;
            clearImageDisplay();
            removeImageButton.setEnabled(false);
        }
    }

    private void displayImage(String imagePath) {
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    BufferedImage originalImage = ImageIO.read(imageFile);
                    if (originalImage != null) {
                        Image scaledImage = originalImage.getScaledInstance(
                            IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
                        imageLabel.setIcon(new ImageIcon(scaledImage));
                        imageLabel.setText("");
                        return;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }
        clearImageDisplay();
    }

    private void clearImageDisplay() {
        imageLabel.setIcon(null);
        imageLabel.setText("No Image");
    }

    private String generateFileName() {
        return "menu_" + System.currentTimeMillis();
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "jpg"; // default extension
    }

    private void loadMenuData() {
        tableModel.setRowCount(0);
        List<Menu> menus = menuDAO.findAll();
        for (Menu menu : menus) {
            String ketersediaan = menu.getKetersediaan().equals("1") ? "Tersedia" : "Tidak Tersedia";
            String hasImage = (menu.getGambar() != null && !menu.getGambar().trim().isEmpty()) ? "✓" : "✗";
            
            // Truncate description for table display
            String description = menu.getDeskripsi();
            if (description != null && description.length() > 50) {
                description = description.substring(0, 47) + "...";
            } else if (description == null || description.trim().isEmpty()) {
                description = "-";
            }
            
            Object[] rowData = {
                menu.getIdMenu(),
                menu.getNamaMenu(),
                menu.getJenisMenu(),
                String.format("Rp %.2f", menu.getHarga()),
                ketersediaan,
                description,
                hasImage
            };
            tableModel.addRow(rowData);
        }
    }

    private void filterMenuByJenis() {
        String selectedJenis = (String) jenisFilterCombo.getSelectedItem();
        tableModel.setRowCount(0);
        
        List<Menu> menus;
        if ("Semua".equals(selectedJenis)) {
            menus = menuDAO.findAll();
        } else {
            menus = menuDAO.findByJenis(selectedJenis);
        }
        
        for (Menu menu : menus) {
            String ketersediaan = menu.getKetersediaan().equals("1") ? "Tersedia" : "Tidak Tersedia";
            String hasImage = (menu.getGambar() != null && !menu.getGambar().trim().isEmpty()) ? "✓" : "✗";
            
            // Truncate description for table display
            String description = menu.getDeskripsi();
            if (description != null && description.length() > 50) {
                description = description.substring(0, 47) + "...";
            } else if (description == null || description.trim().isEmpty()) {
                description = "-";
            }
            
            Object[] rowData = {
                menu.getIdMenu(),
                menu.getNamaMenu(),
                menu.getJenisMenu(),
                String.format("Rp %.2f", menu.getHarga()),
                ketersediaan,
                description,
                hasImage
            };
            tableModel.addRow(rowData);
        }
    }

    private void loadSelectedMenuToForm() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedMenuId = (Integer) tableModel.getValueAt(selectedRow, 0);
            
            // Load menu from database to get complete info including image path and description
            Menu menu = menuDAO.findById(selectedMenuId);
            if (menu != null) {
                namaField.setText(menu.getNamaMenu());
                jenisField.setText(menu.getJenisMenu());
                hargaField.setText(String.valueOf(menu.getHarga()));
                ketersediaanCombo.setSelectedItem(menu.getKetersediaan());
                
                // Load description
                descField.setText(menu.getDeskripsi() != null ? menu.getDeskripsi() : "");
                
                // Load image
                currentImagePath = menu.getGambar();
                displayImage(currentImagePath);
                removeImageButton.setEnabled(currentImagePath != null && !currentImagePath.trim().isEmpty());
            }
            
            updateButton.setEnabled(true);
            deleteButton.setEnabled(true);
            addButton.setText("Tambah Menu Baru");
        } else {
            clearForm();
        }
    }

    private void addMenu() {
        if (!validateForm()) return;

        Menu menu = new Menu(
            namaField.getText().trim(),
            jenisField.getText().trim(),
            Double.parseDouble(hargaField.getText().trim()),
            (String) ketersediaanCombo.getSelectedItem()
        );
        
        // Set description
        String description = descField.getText().trim();
        if (!description.isEmpty()) {
            menu.setDeskripsi(description);
        }
        
        // Set image path if available
        if (currentImagePath != null && !currentImagePath.trim().isEmpty()) {
            menu.setGambar(currentImagePath);
        }

        if (menuDAO.create(menu)) {
            JOptionPane.showMessageDialog(this, "Menu berhasil ditambahkan!");
            loadMenuData();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menambahkan menu!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMenu() {
        if (!validateForm() || selectedMenuId == -1) return;

        Menu menu = new Menu(
            namaField.getText().trim(),
            jenisField.getText().trim(),
            Double.parseDouble(hargaField.getText().trim()),
            (String) ketersediaanCombo.getSelectedItem()
        );
        menu.setIdMenu(selectedMenuId);
        
        // Set description
        String description = descField.getText().trim();
        if (!description.isEmpty()) {
            menu.setDeskripsi(description);
        }
        
        // Set image path if available
        if (currentImagePath != null && !currentImagePath.trim().isEmpty()) {
            menu.setGambar(currentImagePath);
        }

        if (menuDAO.update(menu)) {
            JOptionPane.showMessageDialog(this, "Menu berhasil diupdate!");
            loadMenuData();
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal mengupdate menu!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteMenu() {
        if (selectedMenuId == -1) return;

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Apakah Anda yakin ingin menghapus menu ini?\n(Gambar akan tetap tersimpan)",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (menuDAO.delete(selectedMenuId)) {
                JOptionPane.showMessageDialog(this, "Menu berhasil dihapus!");
                loadMenuData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus menu!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        namaField.setText("");
        jenisField.setText("");
        hargaField.setText("");
        descField.setText(""); // Clear description field
        ketersediaanCombo.setSelectedIndex(0);
        selectedMenuId = -1;
        currentImagePath = null;
        
        clearImageDisplay();
        
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        removeImageButton.setEnabled(false);
        addButton.setText("Tambah Menu");
        
        menuTable.clearSelection();
    }

    private boolean validateForm() {
        if (namaField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama menu tidak boleh kosong!", "Validasi Error", JOptionPane.WARNING_MESSAGE);
            namaField.requestFocus();
            return false;
        }
        
        if (jenisField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Jenis menu tidak boleh kosong!", "Validasi Error", JOptionPane.WARNING_MESSAGE);
            jenisField.requestFocus();
            return false;
        }
        
        try {
            double harga = Double.parseDouble(hargaField.getText().trim());
            if (harga < 0) {
                JOptionPane.showMessageDialog(this, "Harga tidak boleh negatif!", "Validasi Error", JOptionPane.WARNING_MESSAGE);
                hargaField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga harus berupa angka yang valid!", "Validasi Error", JOptionPane.WARNING_MESSAGE);
            hargaField.requestFocus();
            return false;
        }
        
        // Optional: Validate description length
        String description = descField.getText().trim();
        if (description.length() > 500) {
            JOptionPane.showMessageDialog(this, "Deskripsi tidak boleh lebih dari 500 karakter!", "Validasi Error", JOptionPane.WARNING_MESSAGE);
            descField.requestFocus();
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
            new ManagerFrame().setVisible(true);
        });
    }
}