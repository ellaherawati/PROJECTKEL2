package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class ManagerFrame extends JFrame {
    // DAOs
    private MenuDAO menuDAO;
    private CustomerOrderDAO customerOrderDAO;
    private PembayaranDAO pembayaranDAO;
    private PesananDibatalkanDAO pesananDibatalkanDAO;
    private OrderDetailDAO orderDetailDAO;
    private UserDAO userDAO;
    
    // Main components
    private JTabbedPane tabbedPane;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;
    
    // Dashboard components
    private JLabel totalRevenueLabel;
    private JLabel totalOrdersLabel;
    private JLabel totalCancelledLabel;
    private JLabel totalMenusLabel;
    
    // Menu management components (from original ManagerFrame)
    private JTable menuTable;
    private DefaultTableModel menuTableModel;
    
    // Sales report components
    private JTable salesReportTable;
    private DefaultTableModel salesTableModel;
    private JComboBox<String> reportPeriodCombo;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;
    
    // Order tracking components
    private JTable orderTrackingTable;
    private DefaultTableModel orderTableModel;
    private JComboBox<String> orderStatusFilter;
    
    // Cancelled orders components
    private JTable cancelledOrdersTable;
    private DefaultTableModel cancelledTableModel;

    public ManagerFrame() {
        initializeDAOs();
        initializeFormatters();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadDashboardData();
        
        setTitle("Manager Dashboard - Dapur Arunika Restaurant Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setResizable(true);
    }
    
    private void initializeDAOs() {
        menuDAO = new MenuDAO();
        customerOrderDAO = new CustomerOrderDAO();
        pembayaranDAO = new PembayaranDAO();
        pesananDibatalkanDAO = new PesananDibatalkanDAO();
        orderDetailDAO = new OrderDetailDAO();
        userDAO = new UserDAO();
    }
    
    private void initializeFormatters() {
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    }
    
    private void initializeComponents() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Create all tabs
        createDashboardTab();
        createMenuManagementTab();
        createSalesReportTab();
        createOrderTrackingTab();
        createCancelledOrdersTab();
    }
    
    private void createDashboardTab() {
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Dashboard Manager - Dapur Arunika");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        JLabel dateLabel = new JLabel("Updated: " + dateFormat.format(new Date()));
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        dateLabel.setForeground(Color.GRAY);
        headerPanel.add(dateLabel, BorderLayout.SOUTH);
        
        // Stats Cards
        JPanel statsPanel = createStatsPanel();
        
        // Charts and Summary
        JPanel chartsPanel = createChartsPanel();
        
        dashboardPanel.add(headerPanel, BorderLayout.NORTH);
        dashboardPanel.add(statsPanel, BorderLayout.CENTER);
        dashboardPanel.add(chartsPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("📊 Dashboard", dashboardPanel);
    }
    
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Revenue Card
        JPanel revenueCard = createStatsCard("💰 Total Revenue", "Rp 0", Color.GREEN);
        totalRevenueLabel = (JLabel) ((JPanel) revenueCard.getComponent(1)).getComponent(0);
        
        // Orders Card
        JPanel ordersCard = createStatsCard("📦 Total Orders", "0", Color.BLUE);
        totalOrdersLabel = (JLabel) ((JPanel) ordersCard.getComponent(1)).getComponent(0);
        
        // Cancelled Orders Card
        JPanel cancelledCard = createStatsCard("❌ Cancelled Orders", "0", Color.RED);
        totalCancelledLabel = (JLabel) ((JPanel) cancelledCard.getComponent(1)).getComponent(0);
        
        // Menu Items Card
        JPanel menuCard = createStatsCard("🍽️ Menu Items", "0", Color.ORANGE);
        totalMenusLabel = (JLabel) ((JPanel) menuCard.getComponent(1)).getComponent(0);
        
        statsPanel.add(revenueCard);
        statsPanel.add(ordersCard);
        statsPanel.add(cancelledCard);
        statsPanel.add(menuCard);
        
        return statsPanel;
    }
    
    private JPanel createStatsCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(color);
        
        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        valuePanel.setBackground(Color.WHITE);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(Color.BLACK);
        valuePanel.add(valueLabel);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valuePanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createChartsPanel() {
        JPanel chartsPanel = new JPanel(new BorderLayout());
        chartsPanel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
        chartsPanel.setPreferredSize(new Dimension(0, 120));
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton refreshButton = new JButton("🔄 Refresh Data");
        refreshButton.addActionListener(e -> loadDashboardData());
        
        JButton menuManagementButton = new JButton("🍽️ Manage Menu");
        menuManagementButton.addActionListener(e -> tabbedPane.setSelectedIndex(1));
        
        JButton salesReportButton = new JButton("📈 Sales Report");
        salesReportButton.addActionListener(e -> tabbedPane.setSelectedIndex(2));
        
        JButton exportButton = new JButton("💾 Export Data");
        exportButton.addActionListener(e -> exportSalesData());
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(menuManagementButton);
        buttonPanel.add(salesReportButton);
        buttonPanel.add(exportButton);
        
        chartsPanel.add(buttonPanel, BorderLayout.CENTER);
        
        return chartsPanel;
    }
    
    private void createMenuManagementTab() {
        // This will be similar to original ManagerFrame but integrated
        JPanel menuPanel = new JPanel(new BorderLayout());
        
        // Create simplified menu management interface
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton addMenuButton = new JButton("➕ Add Menu");
        JButton editMenuButton = new JButton("✏️ Edit Menu");
        JButton deleteMenuButton = new JButton("🗑️ Delete Menu");
        JButton openFullEditorButton = new JButton("📝 Open Full Editor");
        
        addMenuButton.addActionListener(e -> openMenuDialog(null));
        editMenuButton.addActionListener(e -> editSelectedMenu());
        deleteMenuButton.addActionListener(e -> deleteSelectedMenu());
        openFullEditorButton.addActionListener(e -> openFullMenuEditor());
        
        controlPanel.add(addMenuButton);
        controlPanel.add(editMenuButton);
        controlPanel.add(deleteMenuButton);
        controlPanel.add(openFullEditorButton);
        
        // Menu table
        String[] menuColumns = {"ID", "Nama Menu", "Jenis", "Harga", "Status", "Deskripsi", "Image"};
        menuTableModel = new DefaultTableModel(menuColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        menuTable = new JTable(menuTableModel);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane menuScrollPane = new JScrollPane(menuTable);
        
        menuPanel.add(controlPanel, BorderLayout.NORTH);
        menuPanel.add(menuScrollPane, BorderLayout.CENTER);
        
        tabbedPane.addTab("🍽️ Menu Management", menuPanel);
    }
    
    private void createSalesReportTab() {
        JPanel salesPanel = new JPanel(new BorderLayout());
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Laporan"));
        
        reportPeriodCombo = new JComboBox<>(new String[]{
            "Hari Ini", "7 Hari Terakhir", "Bulan Ini", "Bulan Lalu", "Custom Range"
        });
        
        startDateChooser = new JDateChooser();
        endDateChooser = new JDateChooser();
        startDateChooser.setPreferredSize(new Dimension(120, 25));
        endDateChooser.setPreferredSize(new Dimension(120, 25));
        
        JButton generateReportButton = new JButton("📊 Generate Report");
        generateReportButton.addActionListener(e -> generateSalesReport());
        
        filterPanel.add(new JLabel("Period:"));
        filterPanel.add(reportPeriodCombo);
        filterPanel.add(new JLabel("From:"));
        filterPanel.add(startDateChooser);
        filterPanel.add(new JLabel("To:"));
        filterPanel.add(endDateChooser);
        filterPanel.add(generateReportButton);
        
        // Sales table
        String[] salesColumns = {"Tanggal", "Order ID", "Customer", "Items", "Total", "Payment", "Status"};
        salesTableModel = new DefaultTableModel(salesColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        salesReportTable = new JTable(salesTableModel);
        JScrollPane salesScrollPane = new JScrollPane(salesReportTable);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));
        
        salesPanel.add(filterPanel, BorderLayout.NORTH);
        salesPanel.add(salesScrollPane, BorderLayout.CENTER);
        salesPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        tabbedPane.addTab("📈 Sales Report", salesPanel);
    }
    
    private void createOrderTrackingTab() {
        JPanel orderPanel = new JPanel(new BorderLayout());
        
        // Filter panel
        JPanel orderFilterPanel = new JPanel(new FlowLayout());
        orderFilterPanel.setBorder(BorderFactory.createTitledBorder("Filter Orders"));
        
        orderStatusFilter = new JComboBox<>(new String[]{
            "All", "pending", "selesai", "dibatalkan"
        });
        
        JButton refreshOrdersButton = new JButton("🔄 Refresh Orders");
        refreshOrdersButton.addActionListener(e -> loadOrderTracking());
        
        JButton viewDetailsButton = new JButton("👁️ View Details");
        viewDetailsButton.addActionListener(e -> viewOrderDetails());
        
        orderFilterPanel.add(new JLabel("Status:"));
        orderFilterPanel.add(orderStatusFilter);
        orderFilterPanel.add(refreshOrdersButton);
        orderFilterPanel.add(viewDetailsButton);
        
        // Orders table
        String[] orderColumns = {"Order ID", "Customer", "Date", "Total", "Status", "Notes"};
        orderTableModel = new DefaultTableModel(orderColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderTrackingTable = new JTable(orderTableModel);
        JScrollPane orderScrollPane = new JScrollPane(orderTrackingTable);
        
        orderPanel.add(orderFilterPanel, BorderLayout.NORTH);
        orderPanel.add(orderScrollPane, BorderLayout.CENTER);
        
        tabbedPane.addTab("📦 Order Tracking", orderPanel);
    }
    
    private void createCancelledOrdersTab() {
        JPanel cancelledPanel = new JPanel(new BorderLayout());
        
        // Control panel
        JPanel cancelControlPanel = new JPanel(new FlowLayout());
        JButton refreshCancelledButton = new JButton("🔄 Refresh");
        refreshCancelledButton.addActionListener(e -> loadCancelledOrders());
        
        JButton analyzeButton = new JButton("📊 Analyze Reasons");
        analyzeButton.addActionListener(e -> analyzeCancellationReasons());
        
        cancelControlPanel.add(refreshCancelledButton);
        cancelControlPanel.add(analyzeButton);
        
        // Cancelled orders table
        String[] cancelledColumns = {"Order ID", "Customer", "Cancel Date", "Reason", "Amount Lost"};
        cancelledTableModel = new DefaultTableModel(cancelledColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        cancelledOrdersTable = new JTable(cancelledTableModel);
        JScrollPane cancelledScrollPane = new JScrollPane(cancelledOrdersTable);
        
        cancelledPanel.add(cancelControlPanel, BorderLayout.NORTH);
        cancelledPanel.add(cancelledScrollPane, BorderLayout.CENTER);
        
        tabbedPane.addTab("❌ Cancelled Orders", cancelledPanel);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        reportPeriodCombo.addActionListener(e -> {
            String selected = (String) reportPeriodCombo.getSelectedItem();
            boolean customRange = "Custom Range".equals(selected);
            startDateChooser.setEnabled(customRange);
            endDateChooser.setEnabled(customRange);
            
            if (!customRange) {
                setDateRangeFromPeriod(selected);
            }
        });
        
        orderStatusFilter.addActionListener(e -> loadOrderTracking());
    }
    
    private void loadDashboardData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Load stats
                loadStats();
                loadMenuData();
                loadOrderTracking();
                loadCancelledOrders();
                return null;
            }
            
            @Override
            protected void done() {
                JOptionPane.showMessageDialog(ManagerFrame.this, 
                    "Dashboard data refreshed successfully!", 
                    "Data Refreshed", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        };
        worker.execute();
    }
    
    private void loadStats() {
        try {
            // Total Revenue
            List<Pembayaran> payments = pembayaranDAO.findAll();
            double totalRevenue = payments.stream()
                .filter(p -> "berhasil".equals(p.getStatusPembayaran()))
                .mapToDouble(Pembayaran::getJumlahPembayaran)
                .sum();
            
            SwingUtilities.invokeLater(() -> 
                totalRevenueLabel.setText(currencyFormat.format(totalRevenue)));
            
            // Total Orders
            List<CustomerOrder> orders = customerOrderDAO.findAll();
            SwingUtilities.invokeLater(() -> 
                totalOrdersLabel.setText(String.valueOf(orders.size())));
            
            // Cancelled Orders
            List<PesananDibatalkan> cancelledOrders = pesananDibatalkanDAO.findAll();
            SwingUtilities.invokeLater(() -> 
                totalCancelledLabel.setText(String.valueOf(cancelledOrders.size())));
            
            // Menu Items
            List<Menu> menus = menuDAO.findAll();
            SwingUtilities.invokeLater(() -> 
                totalMenusLabel.setText(String.valueOf(menus.size())));
            
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(this, 
                    "Error loading dashboard stats: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE));
        }
    }
    
    private void loadMenuData() {
        SwingUtilities.invokeLater(() -> {
            menuTableModel.setRowCount(0);
            List<Menu> menus = menuDAO.findAll();
            
            for (Menu menu : menus) {
                String status = "1".equals(menu.getKetersediaan()) ? "Available" : "Unavailable";
                String description = menu.getDeskripsi();
                if (description != null && description.length() > 50) {
                    description = description.substring(0, 47) + "...";
                }
                
                String hasImage = (menu.getGambar() != null && !menu.getGambar().trim().isEmpty()) ? "✓" : "✗";
                
                Object[] row = {
                    menu.getIdMenu(),
                    menu.getNamaMenu(),
                    menu.getJenisMenu(),
                    currencyFormat.format(menu.getHarga()),
                    status,
                    description != null ? description : "-",
                    hasImage
                };
                menuTableModel.addRow(row);
            }
        });
    }
    
    private void loadOrderTracking() {
        SwingUtilities.invokeLater(() -> {
            orderTableModel.setRowCount(0);
            List<CustomerOrder> orders = customerOrderDAO.findAll();
            
            String statusFilter = (String) orderStatusFilter.getSelectedItem();
            
            for (CustomerOrder order : orders) {
                if (!"All".equals(statusFilter) && !statusFilter.equals(order.getStatusPesanan())) {
                    continue;
                }
                
                // Get customer name
                String customerName = "Unknown";
                try {
                    User customer = userDAO.findById(order.getCustomerId());
                    if (customer != null) {
                        customerName = customer.getNama();
                    }
                } catch (Exception e) {
                    // Handle error silently
                }
                
                Object[] row = {
                    order.getIdPesanan(),
                    customerName,
                    dateFormat.format(order.getTanggalPesanan()),
                    currencyFormat.format(order.getTotalPesanan()),
                    order.getStatusPesanan(),
                    order.getCatatan() != null ? order.getCatatan() : "-"
                };
                orderTableModel.addRow(row);
            }
        });
    }
    
    private void loadCancelledOrders() {
        SwingUtilities.invokeLater(() -> {
            cancelledTableModel.setRowCount(0);
            List<PesananDibatalkan> cancelledOrders = pesananDibatalkanDAO.findAll();
            
            for (PesananDibatalkan cancelled : cancelledOrders) {
                // Get original order info
                CustomerOrder originalOrder = customerOrderDAO.findById(cancelled.getIdPesanan());
                String customerName = "Unknown";
                double amountLost = 0;
                
                if (originalOrder != null) {
                    amountLost = originalOrder.getTotalPesanan();
                    try {
                        User customer = userDAO.findById(originalOrder.getCustomerId());
                        if (customer != null) {
                            customerName = customer.getNama();
                        }
                    } catch (Exception e) {
                        // Handle error silently
                    }
                }
                
                Object[] row = {
                    cancelled.getIdPesanan(),
                    customerName,
                    dateFormat.format(cancelled.getTanggalBatal()),
                    cancelled.getAlasanBatal(),
                    currencyFormat.format(amountLost)
                };
                cancelledTableModel.addRow(row);
            }
        });
    }
    
    private void generateSalesReport() {
        try {
            salesTableModel.setRowCount(0);
            
            Date startDate = startDateChooser.getDate();
            Date endDate = endDateChooser.getDate();
            
            if (startDate == null || endDate == null) {
                JOptionPane.showMessageDialog(this, "Please select date range", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            List<CustomerOrder> orders = customerOrderDAO.findAll();
            double totalSales = 0;
            int totalTransactions = 0;
            
            for (CustomerOrder order : orders) {
                Date orderDate = new Date(order.getTanggalPesanan().getTime());
                
                if (orderDate.compareTo(startDate) >= 0 && orderDate.compareTo(endDate) <= 0) {
                    if ("selesai".equals(order.getStatusPesanan())) {
                        // Get customer name
                        String customerName = "Unknown";
                        try {
                            User customer = userDAO.findById(order.getCustomerId());
                            if (customer != null) {
                                customerName = customer.getNama();
                            }
                        } catch (Exception e) {
                            // Handle error silently
                        }
                        
                        // Get payment method
                        String paymentMethod = "Cash";
                        try {
                            Pembayaran payment = pembayaranDAO.findByOrderId(order.getIdPesanan());
                            if (payment != null) {
                                paymentMethod = payment.getMetodePembayaran().toUpperCase();
                            }
                        } catch (Exception e) {
                            // Handle error silently
                        }
                        
                        // Get order items count
                        List<OrderDetail> details = orderDetailDAO.findByOrderId(order.getIdPesanan());
                        int itemCount = details.stream().mapToInt(OrderDetail::getJumlah).sum();
                        
                        Object[] row = {
                            dateFormat.format(order.getTanggalPesanan()),
                            order.getIdPesanan(),
                            customerName,
                            itemCount + " items",
                            currencyFormat.format(order.getTotalPesanan()),
                            paymentMethod,
                            order.getStatusPesanan()
                        };
                        
                        salesTableModel.addRow(row);
                        totalSales += order.getTotalPesanan();
                        totalTransactions++;
                    }
                }
            }
            
            // Show summary
            JOptionPane.showMessageDialog(this,
                String.format("Sales Report Generated!\n\nTotal Transactions: %d\nTotal Sales: %s\nAverage Order: %s",
                    totalTransactions,
                    currencyFormat.format(totalSales),
                    currencyFormat.format(totalTransactions > 0 ? totalSales / totalTransactions : 0)),
                "Sales Report Summary",
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating sales report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void setDateRangeFromPeriod(String period) {
        LocalDate now = LocalDate.now();
        LocalDate start;
        LocalDate end = now;
        
        switch (period) {
            case "Hari Ini":
                start = now;
                break;
            case "7 Hari Terakhir":
                start = now.minusDays(7);
                break;
            case "Bulan Ini":
                start = now.withDayOfMonth(1);
                break;
            case "Bulan Lalu":
                start = now.minusMonths(1).withDayOfMonth(1);
                end = now.withDayOfMonth(1).minusDays(1);
                break;
            default:
                return;
        }
        
        startDateChooser.setDate(java.sql.Date.valueOf(start));
        endDateChooser.setDate(java.sql.Date.valueOf(end));
    }
    
    private void openMenuDialog(Menu menu) {
        // Enhanced add/edit menu dialog with image upload
        JDialog dialog = new JDialog(this, menu == null ? "Add Menu" : "Edit Menu", true);
        dialog.setSize(900, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        
        JTextField nameField = new JTextField(25);
        JTextField typeField = new JTextField(25);
        JTextField priceField = new JTextField(25);
        JTextArea descArea = new JTextArea(4, 25);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descArea);
        JComboBox<String> availabilityCombo = new JComboBox<>(new String[]{"1", "0"});
        
        // Image components
        JLabel imagePreviewLabel = new JLabel("No Image");
        imagePreviewLabel.setPreferredSize(new Dimension(120, 80));
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setVerticalAlignment(SwingConstants.CENTER);
        imagePreviewLabel.setOpaque(true);
        imagePreviewLabel.setBackground(Color.LIGHT_GRAY);
        
        JButton uploadImageButton = new JButton("📷 Upload Image");
        JButton removeImageButton = new JButton("🗑️ Remove Image");
        removeImageButton.setEnabled(false);
        
        final String[] currentImagePath = {null}; // Array to make it effectively final
        
        if (menu != null) {
            nameField.setText(menu.getNamaMenu());
            typeField.setText(menu.getJenisMenu());
            priceField.setText(String.valueOf(menu.getHarga()));
            descArea.setText(menu.getDeskripsi());
            availabilityCombo.setSelectedItem(menu.getKetersediaan());
            
            // Load existing image
            if (menu.getGambar() != null && !menu.getGambar().trim().isEmpty()) {
                currentImagePath[0] = menu.getGambar();
                displayImagePreview(imagePreviewLabel, menu.getGambar());
                removeImageButton.setEnabled(true);
            }
        }
        
        // Upload image button action
        uploadImageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Menu Image");
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter(
                    "Image files", "jpg", "jpeg", "png", "gif", "bmp"));
            
            int result = fileChooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    java.io.File selectedFile = fileChooser.getSelectedFile();
                    
                    // Validate image file
                    java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(selectedFile);
                    if (image == null) {
                        JOptionPane.showMessageDialog(dialog, 
                            "Selected file is not a valid image!", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    // Create images/menu directory if it doesn't exist
                    java.io.File imagesDir = new java.io.File("images/menu/");
                    if (!imagesDir.exists()) {
                        imagesDir.mkdirs();
                    }
                    
                    // Generate new filename
                    String extension = getFileExtension(selectedFile.getName());
                    String newFileName = "menu_" + System.currentTimeMillis() + "." + extension;
                    String newFilePath = "images/menu/" + newFileName;
                    
                    // Copy file to images folder
                    java.nio.file.Path sourcePath = selectedFile.toPath();
                    java.nio.file.Path targetPath = java.nio.file.Paths.get(newFilePath);
                    java.nio.file.Files.copy(sourcePath, targetPath, 
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    
                    // Update preview and path
                    currentImagePath[0] = newFilePath;
                    displayImagePreview(imagePreviewLabel, newFilePath);
                    removeImageButton.setEnabled(true);
                    
                    JOptionPane.showMessageDialog(dialog, 
                        "Image uploaded successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                        
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Error uploading image: " + ex.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        // Remove image button action
        removeImageButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog,
                "Are you sure you want to remove the image?",
                "Confirm Remove Image",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                currentImagePath[0] = null;
                clearImagePreview(imagePreviewLabel);
                removeImageButton.setEnabled(false);
            }
        });
        
        // Layout form components
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);
        
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Type:"), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        formPanel.add(typeField, gbc);
        
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Price:"), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        formPanel.add(priceField, gbc);
        
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Available:"), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        formPanel.add(availabilityCombo, gbc);
        
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        formPanel.add(descScrollPane, gbc);
        
        // Image panel (right side)
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createTitledBorder("Menu Image"));
        imagePanel.add(imagePreviewLabel, BorderLayout.CENTER);
        
        JPanel imageButtonPanel = new JPanel(new FlowLayout());
        imageButtonPanel.add(uploadImageButton);
        imageButtonPanel.add(removeImageButton);
        imagePanel.add(imageButtonPanel, BorderLayout.SOUTH);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("💾 Save");
        JButton cancelButton = new JButton("❌ Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                // Validate form
                if (nameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Menu name cannot be empty!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    nameField.requestFocus();
                    return;
                }
                
                if (typeField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Menu type cannot be empty!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    typeField.requestFocus();
                    return;
                }
                
                double price;
                try {
                    price = Double.parseDouble(priceField.getText().trim());
                    if (price < 0) {
                        JOptionPane.showMessageDialog(dialog, "Price cannot be negative!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                        priceField.requestFocus();
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a valid price!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    priceField.requestFocus();
                    return;
                }
                
                Menu menuToSave = menu != null ? menu : new Menu();
                menuToSave.setNamaMenu(nameField.getText().trim());
                menuToSave.setJenisMenu(typeField.getText().trim());
                menuToSave.setHarga(price);
                menuToSave.setDeskripsi(descArea.getText().trim());
                menuToSave.setKetersediaan((String) availabilityCombo.getSelectedItem());
                menuToSave.setGambar(currentImagePath[0]); // Set image path
                
                boolean success;
                if (menu == null) {
                    success = menuDAO.create(menuToSave);
                } else {
                    success = menuDAO.update(menuToSave);
                }
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Menu saved successfully!");
                    loadMenuData();
                    loadStats(); // Refresh stats
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to save menu!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        // Main layout
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(formPanel, BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(leftPanel, BorderLayout.CENTER);
        mainPanel.add(imagePanel, BorderLayout.EAST);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    // Helper methods for image handling
    private void displayImagePreview(JLabel imageLabel, String imagePath) {
        try {
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                java.io.File imageFile = new java.io.File(imagePath);
                if (imageFile.exists()) {
                    java.awt.image.BufferedImage originalImage = javax.imageio.ImageIO.read(imageFile);
                    if (originalImage != null) {
                        java.awt.Image scaledImage = originalImage.getScaledInstance(
                            120, 80, java.awt.Image.SCALE_SMOOTH);
                        imageLabel.setIcon(new ImageIcon(scaledImage));
                        imageLabel.setText("");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading image preview: " + e.getMessage());
        }
        clearImagePreview(imageLabel);
    }
    
    private void clearImagePreview(JLabel imageLabel) {
        imageLabel.setIcon(null);
        imageLabel.setText("No Image");
    }
    
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "jpg"; // default extension
    }
    
    private void editSelectedMenu() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow >= 0) {
            int menuId = (Integer) menuTableModel.getValueAt(selectedRow, 0);
            Menu menu = menuDAO.findById(menuId);
            if (menu != null) {
                openMenuDialog(menu);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a menu to edit!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void deleteSelectedMenu() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow >= 0) {
            int menuId = (Integer) menuTableModel.getValueAt(selectedRow, 0);
            String menuName = (String) menuTableModel.getValueAt(selectedRow, 1);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete menu: " + menuName + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                if (menuDAO.delete(menuId)) {
                    JOptionPane.showMessageDialog(this, "Menu deleted successfully!");
                    loadMenuData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete menu!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a menu to delete!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void openFullMenuEditor() {
        // Open the original ManagerFrame for full menu editing
        SwingUtilities.invokeLater(() -> {
            ManagerFrame fullEditor = new ManagerFrame();
            fullEditor.setVisible(true);
        });
    }
    
    private void viewOrderDetails() {
        int selectedRow = orderTrackingTable.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (Integer) orderTableModel.getValueAt(selectedRow, 0);
            showOrderDetailsDialog(orderId);
        } else {
            JOptionPane.showMessageDialog(this, "Please select an order to view details!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void showOrderDetailsDialog(int orderId) {
        JDialog detailsDialog = new JDialog(this, "Order Details - ID: " + orderId, true);
        detailsDialog.setSize(600, 400);
        detailsDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Order info
        CustomerOrder order = customerOrderDAO.findById(orderId);
        if (order == null) {
            JOptionPane.showMessageDialog(this, "Order not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Header info
        JPanel headerPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        headerPanel.setBorder(BorderFactory.createTitledBorder("Order Information"));
        
        String customerName = "Unknown";
        try {
            User customer = userDAO.findById(order.getCustomerId());
            if (customer != null) {
                customerName = customer.getNama();
            }
        } catch (Exception e) {
            // Handle silently
        }
        
        headerPanel.add(new JLabel("Order ID:"));
        headerPanel.add(new JLabel(String.valueOf(order.getIdPesanan())));
        headerPanel.add(new JLabel("Customer:"));
        headerPanel.add(new JLabel(customerName));
        headerPanel.add(new JLabel("Date:"));
        headerPanel.add(new JLabel(dateFormat.format(order.getTanggalPesanan())));
        headerPanel.add(new JLabel("Status:"));
        headerPanel.add(new JLabel(order.getStatusPesanan()));
        
        // Order items
        String[] itemColumns = {"Menu Item", "Quantity", "Unit Price", "Subtotal"};
        DefaultTableModel itemsModel = new DefaultTableModel(itemColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable itemsTable = new JTable(itemsModel);
        JScrollPane itemsScrollPane = new JScrollPane(itemsTable);
        itemsScrollPane.setBorder(BorderFactory.createTitledBorder("Order Items"));
        
        List<OrderDetail> orderDetails = orderDetailDAO.findByOrderId(orderId);
        for (OrderDetail detail : orderDetails) {
            String menuName = "Unknown Menu";
            try {
                Menu menu = menuDAO.findById(detail.getIdMenu());
                if (menu != null) {
                    menuName = menu.getNamaMenu();
                }
            } catch (Exception e) {
                // Handle silently
            }
            
            Object[] row = {
                menuName,
                detail.getJumlah(),
                currencyFormat.format(detail.getHargaSatuan()),
                currencyFormat.format(detail.getJumlah() * detail.getHargaSatuan())
            };
            itemsModel.addRow(row);
        }
        
        // Footer with total
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.add(new JLabel("Total: " + currencyFormat.format(order.getTotalPesanan())));
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(itemsScrollPane, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        detailsDialog.add(mainPanel);
        detailsDialog.setVisible(true);
    }
    
    private void analyzeCancellationReasons() {
        try {
            List<PesananDibatalkan> cancelledOrders = pesananDibatalkanDAO.findAll();
            
            if (cancelledOrders.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No cancelled orders found!", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Count reasons
            Map<String, Integer> reasonCounts = new HashMap<>();
            Map<String, Double> reasonAmounts = new HashMap<>();
            
            for (PesananDibatalkan cancelled : cancelledOrders) {
                String reason = cancelled.getAlasanBatal();
                reasonCounts.put(reason, reasonCounts.getOrDefault(reason, 0) + 1);
                
                // Get order amount
                CustomerOrder originalOrder = customerOrderDAO.findById(cancelled.getIdPesanan());
                if (originalOrder != null) {
                    reasonAmounts.put(reason, 
                        reasonAmounts.getOrDefault(reason, 0.0) + originalOrder.getTotalPesanan());
                }
            }
            
            // Create analysis dialog
            JDialog analysisDialog = new JDialog(this, "Cancellation Analysis", true);
            analysisDialog.setSize(500, 400);
            analysisDialog.setLocationRelativeTo(this);
            
            String[] columns = {"Reason", "Count", "Percentage", "Lost Revenue"};
            DefaultTableModel analysisModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            
            JTable analysisTable = new JTable(analysisModel);
            
            int totalCancellations = cancelledOrders.size();
            double totalLostRevenue = reasonAmounts.values().stream().mapToDouble(Double::doubleValue).sum();
            
            for (Map.Entry<String, Integer> entry : reasonCounts.entrySet()) {
                String reason = entry.getKey();
                int count = entry.getValue();
                double percentage = (count * 100.0) / totalCancellations;
                double lostRevenue = reasonAmounts.getOrDefault(reason, 0.0);
                
                Object[] row = {
                    reason,
                    count,
                    String.format("%.1f%%", percentage),
                    currencyFormat.format(lostRevenue)
                };
                analysisModel.addRow(row);
            }
            
            JScrollPane scrollPane = new JScrollPane(analysisTable);
            
            JPanel summaryPanel = new JPanel(new GridLayout(3, 2));
            summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary"));
            summaryPanel.add(new JLabel("Total Cancellations:"));
            summaryPanel.add(new JLabel(String.valueOf(totalCancellations)));
            summaryPanel.add(new JLabel("Total Lost Revenue:"));
            summaryPanel.add(new JLabel(currencyFormat.format(totalLostRevenue)));
            summaryPanel.add(new JLabel("Average Lost per Cancel:"));
            summaryPanel.add(new JLabel(currencyFormat.format(totalLostRevenue / totalCancellations)));
            
            analysisDialog.setLayout(new BorderLayout());
            analysisDialog.add(scrollPane, BorderLayout.CENTER);
            analysisDialog.add(summaryPanel, BorderLayout.SOUTH);
            
            analysisDialog.setVisible(true);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error analyzing cancellation reasons: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportSalesData() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Sales Data");
            fileChooser.setSelectedFile(new java.io.File("sales_report_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv"));
            
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                
                // Create CSV content
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("Date,Order ID,Customer,Items,Total,Payment Method,Status\n");
                
                for (int i = 0; i < salesTableModel.getRowCount(); i++) {
                    for (int j = 0; j < salesTableModel.getColumnCount(); j++) {
                        if (j > 0) csvContent.append(",");
                        Object value = salesTableModel.getValueAt(i, j);
                        csvContent.append("\"").append(value != null ? value.toString() : "").append("\"");
                    }
                    csvContent.append("\n");
                }
                
                // Write to file
                try (java.io.FileWriter writer = new java.io.FileWriter(fileToSave)) {
                    writer.write(csvContent.toString());
                }
                
                JOptionPane.showMessageDialog(this, 
                    "Sales data exported successfully to:\n" + fileToSave.getAbsolutePath(), 
                    "Export Complete", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting data: " + e.getMessage(), 
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Custom JDateChooser replacement (simple implementation)
    private static class JDateChooser extends JPanel {
        private JTextField dateField;
        private Date selectedDate;
        
        public JDateChooser() {
            setLayout(new BorderLayout());
            dateField = new JTextField(10);
            dateField.setEditable(false);
            
            JButton calendarButton = new JButton("📅");
            calendarButton.addActionListener(e -> showDatePicker());
            
            add(dateField, BorderLayout.CENTER);
            add(calendarButton, BorderLayout.EAST);
            
            // Set default to today
            setDate(new Date());
        }
        
        private void showDatePicker() {
            // Simple date picker dialog
            String dateStr = JOptionPane.showInputDialog(this, 
                "Enter date (dd/MM/yyyy):", 
                dateField.getText());
            
            if (dateStr != null && !dateStr.trim().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date date = sdf.parse(dateStr);
                    setDate(date);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Please use dd/MM/yyyy", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        public void setDate(Date date) {
            this.selectedDate = date;
            if (date != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                dateField.setText(sdf.format(date));
            } else {
                dateField.setText("");
            }
        }
        
        public Date getDate() {
            return selectedDate;
        }
        
        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            dateField.setEnabled(enabled);
        }
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