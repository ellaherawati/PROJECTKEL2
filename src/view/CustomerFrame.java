package view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.swing.*;

public class CustomerFrame extends JFrame {
    // Database components
    private CustomerOrderDAO customerOrderDAO;
    private OrderDetailDAO orderDetailDAO;
    private PembayaranDAO pembayaranDAO;
    private NotaDAO notaDAO;
    private MenuDAO menuDAO;
    private UserDAO userDAO;
    private PesananDibatalkanDAO pesananDibatalkanDAO; // Added for cancel order
    
    // Current user info
    private int currentUserId = 4; // Default customer ID, bisa diubah sesuai login
    private String currentCustomerName = "Customer";
    
    // Data untuk menu items
    private List<MenuItem> menuItems;
    private List<OrderItem> orderItems;
    private JLabel totalLabel;
    private JPanel cartPanel;
    private JScrollPane cartScrollPane;
    private NumberFormat currencyFormat;
    
    // Menu item class
    static class MenuItem {
        String name;
        int price;
        String description;
        String category;
        String imagePath;
        ImageIcon imageIcon;
        int menuId; // Tambahan untuk ID menu dari database
        
        MenuItem(String name, int price, String description, String category, String imagePath) {
            this.name = name;
            this.price = price;
            this.description = description;
            this.category = category;
            this.imagePath = imagePath;
            this.imageIcon = loadImage(imagePath);
        }
        
        private ImageIcon loadImage(String path) {
            try {
                if (path != null && !path.isEmpty()) {
                    File imageFile = new File(path);
                    if (imageFile.exists()) {
                        BufferedImage img = ImageIO.read(imageFile);
                        Image scaledImg = img.getScaledInstance(150, 100, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImg);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading image: " + path + " - " + e.getMessage());
            }
            // Return default placeholder image if loading fails
            return createPlaceholderImage();
        }
        
        private ImageIcon createPlaceholderImage() {
            BufferedImage placeholder = new BufferedImage(150, 100, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = placeholder.createGraphics();
            
            // Set anti-aliasing for smoother text
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Create gradient background
            GradientPaint gradient = new GradientPaint(0, 0, new Color(240, 240, 240), 150, 100, new Color(220, 220, 220));
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, 150, 100);
            
            // Draw border
            g2d.setColor(new Color(200, 200, 200));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(1, 1, 148, 98);
            
            // Draw food icon (simple plate with food)
            g2d.setColor(new Color(150, 150, 150));
            
            // Draw plate
            g2d.fillOval(35, 45, 80, 15);
            g2d.setColor(new Color(180, 180, 180));
            g2d.fillOval(40, 40, 70, 20);
            
            // Draw food items on plate
            g2d.setColor(new Color(200, 150, 100));
            g2d.fillOval(50, 35, 15, 15); // food item 1
            g2d.fillOval(70, 38, 12, 12); // food item 2
            g2d.fillOval(90, 36, 14, 14); // food item 3
            
            // Draw text
            g2d.setColor(new Color(120, 120, 120));
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            String text = "No Image";
            int textWidth = fm.stringWidth(text);
            int textX = (150 - textWidth) / 2;
            int textY = 80;
            g2d.drawString(text, textX, textY);
            
            g2d.dispose();
            return new ImageIcon(placeholder);
        }
    }
    
    // Order item class
    static class OrderItem {
        MenuItem menuItem;
        int quantity;
        
        OrderItem(MenuItem menuItem, int quantity) {
            this.menuItem = menuItem;
            this.quantity = quantity;
        }
    }

    public CustomerFrame() {
        initializeDAOs();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        initializeMenuData();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        // Set current customer info
        setCurrentCustomerInfo();
    }
    
    private void initializeDAOs() {
        customerOrderDAO = new CustomerOrderDAO();
        orderDetailDAO = new OrderDetailDAO();
        pembayaranDAO = new PembayaranDAO();
        notaDAO = new NotaDAO();
        menuDAO = new MenuDAO();
        userDAO = new UserDAO();
        pesananDibatalkanDAO = new PesananDibatalkanDAO(); // Initialize cancel order DAO
    }
    
    private void setCurrentCustomerInfo() {
        // Get customer info from database
        try {
            User user = userDAO.findById(currentUserId);
            if (user != null) {
                currentCustomerName = user.getNama();
            }
        } catch (Exception e) {
            System.err.println("Error getting customer info: " + e.getMessage());
            currentCustomerName = "Customer"; // fallback
        }
    }
    
    private void initializeMenuData() {
        menuItems = new ArrayList<>();
        orderItems = new ArrayList<>();
        
        // Load menu dari database
        loadMenuFromDatabase();
        
        // Jika tidak ada data di database, gunakan data hardcode sebagai fallback
        if (menuItems.isEmpty()) {
            loadHardcodedMenu();
        }
    }
    
    private void loadMenuFromDatabase() {
        try {
            List<Menu> dbMenus = menuDAO.findAll();
            
            for (Menu menu : dbMenus) {
                // Convert database menu to MenuItem
                String imagePath = null;
                String description = menu.getDeskripsi(); // Use description from database
                
                // Check if menu has image from database
                if (menu.getGambar() != null && !menu.getGambar().trim().isEmpty()) {
                    imagePath = menu.getGambar();
                    System.out.println("Loading image for " + menu.getNamaMenu() + ": " + imagePath);
                } else {
                    // Try to find image in images folder with common naming patterns
                    imagePath = findImageForMenu(menu.getNamaMenu());
                }
                
                // Use database description or generate fallback
                if (description == null || description.trim().isEmpty()) {
                    description = generateFallbackDescription(menu.getNamaMenu(), menu.getJenisMenu());
                }
                
                MenuItem menuItem = new MenuItem(
                    menu.getNamaMenu(),
                    (int) menu.getHarga(),
                    description,
                    menu.getJenisMenu().toUpperCase(),
                    imagePath
                );
                menuItem.menuId = menu.getIdMenu(); // Set ID dari database
                menuItems.add(menuItem);
            }
            
            System.out.println("Loaded " + menuItems.size() + " menu items from database");
        } catch (Exception e) {
            System.err.println("Error loading menu from database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Try to find image file for menu item using common naming patterns
     */
    private String findImageForMenu(String menuName) {
        String[] possiblePaths = {
            "images/menu/" + menuName.toLowerCase().replace(" ", "_") + ".jpg",
            "images/menu/" + menuName.toLowerCase().replace(" ", "_") + ".jpeg",
            "images/menu/" + menuName.toLowerCase().replace(" ", "_") + ".png",
            "images/menu/" + menuName.toLowerCase().replace(" ", "-") + ".jpg",
            "images/menu/" + menuName.toLowerCase().replace(" ", "-") + ".jpeg",
            "images/menu/" + menuName.toLowerCase().replace(" ", "-") + ".png",
            "images/" + menuName.toLowerCase().replace(" ", "_") + ".jpg",
            "images/" + menuName.toLowerCase().replace(" ", "_") + ".jpeg",
            "images/" + menuName.toLowerCase().replace(" ", "_") + ".png"
        };
        
        for (String path : possiblePaths) {
            File imageFile = new File(path);
            if (imageFile.exists()) {
                System.out.println("Found image for " + menuName + " at: " + path);
                return path;
            }
        }
        
        System.out.println("No image found for: " + menuName);
        return null;
    }
    
    /**
     * Generate fallback description if no description in database
     */
    private String generateFallbackDescription(String menuName, String jenisMenu) {
        if (jenisMenu.equalsIgnoreCase("makanan")) {
            return "Hidangan lezat " + menuName + " dengan cita rasa yang menggugah selera.";
        } else if (jenisMenu.equalsIgnoreCase("minuman")) {
            return "Minuman segar " + menuName + " yang menyegarkan dahaga Anda.";
        } else {
            return "Menu spesial " + menuName + " dari dapur kami.";
        }
    }
    
    private void loadHardcodedMenu() {
        // Data menu hardcode sebagai fallback
        menuItems.add(new MenuItem("Nasi Gudeg", 15000, 
            "Nasi gudeg khas Yogyakarta dengan cita rasa manis dan gurih.", 
            "MAKANAN", "images/nasi_gudeg.jpg"));
        
        menuItems.add(new MenuItem("Ayam Geprek", 18000, 
            "Ayam crispy dengan sambal geprek yang pedas menggugah selera.", 
            "MAKANAN", "images/ayam_geprek.jpg"));
        
        menuItems.add(new MenuItem("Soto Ayam", 12000, 
            "Sup ayam tradisional dengan kuah bening yang segar.", 
            "MAKANAN", "images/soto_ayam.jpg"));
        
        menuItems.add(new MenuItem("Es Teh Manis", 3000, 
            "Teh dingin yang segar dengan rasa manis yang pas.", 
            "MINUMAN", "images/es_teh_manis.jpg"));
        
        menuItems.add(new MenuItem("Jus Alpukat", 8000, 
            "Jus alpukat creamy dengan susu, kaya nutrisi.", 
            "MINUMAN", "images/jus_alpukat.jpg"));
        
        System.out.println("Using hardcoded menu data");
    }
    
    private void initializeComponents() {
        setTitle("Dapur Arunika - Taking Order System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 1000);
        setLocationRelativeTo(null);
        setResizable(true);
        
        getContentPane().setBackground(new Color(217, 217, 217));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Main Content Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(217, 217, 217));
        
        // Left Panel - Menu
        JPanel menuPanel = createMenuPanel();
        JScrollPane menuScrollPane = new JScrollPane(menuPanel);
        menuScrollPane.setPreferredSize(new Dimension(700, 600));
        menuScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        menuScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        menuScrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Right Panel - Order Summary
        JPanel orderPanel = createOrderPanel();
        
        mainPanel.add(menuScrollPane, BorderLayout.CENTER);
        mainPanel.add(orderPanel, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(217, 217, 217));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Welcome text with customer name
        JLabel welcomeLabel = new JLabel("Welcome, " + currentCustomerName);
        welcomeLabel.setForeground(Color.BLACK);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Restaurant name
        JLabel restaurantLabel = new JLabel("Dapur Arunika");
        restaurantLabel.setForeground(Color.BLACK);
        restaurantLabel.setFont(new Font("Serif", Font.BOLD, 45));
        restaurantLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Header content
        JPanel headerContent = new JPanel(new BorderLayout());
        headerContent.setBackground(new Color(217, 217, 217));
        headerContent.add(welcomeLabel, BorderLayout.NORTH);
        headerContent.add(restaurantLabel, BorderLayout.CENTER);
        
        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.setBackground(new Color(217, 217, 217));
        logoutButton.setForeground(Color.BLACK);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setPreferredSize(new Dimension(100, 35));
        logoutButton.addActionListener(e -> logout());
        
        headerPanel.add(headerContent, BorderLayout.CENTER);
        headerPanel.add(logoutButton, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(Color.WHITE);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Kategorisasi menu
        createMenuSection(menuPanel, "MAKANAN");
        menuPanel.add(Box.createVerticalStrut(30));
        createMenuSection(menuPanel, "MINUMAN");
        
        return menuPanel;
    }
    
    private void createMenuSection(JPanel parent, String category) {
        // Section label
        JLabel categoryLabel = new JLabel(category);
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 20));
        categoryLabel.setForeground(new Color(60, 60, 60));
        categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(categoryLabel);
        parent.add(Box.createVerticalStrut(15));
        
        // Count items in category
        int itemCount = 0;
        for (MenuItem item : menuItems) {
            if (item.category.equals(category)) {
                itemCount++;
            }
        }
        
        if (itemCount == 0) {
            JLabel noItemsLabel = new JLabel("Tidak ada menu dalam kategori ini");
            noItemsLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            noItemsLabel.setForeground(new Color(150, 150, 150));
            noItemsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            parent.add(noItemsLabel);
            return;
        }
        
        // Create grid for items
        int columns = category.equals("MAKANAN") ? 4 : 3;
        int rows = (int) Math.ceil(itemCount / (double) columns);
        JPanel itemGrid = new JPanel(new GridLayout(rows, columns, 15, 15));
        itemGrid.setBackground(Color.WHITE);
        
        for (MenuItem item : menuItems) {
            if (item.category.equals(category)) {
                itemGrid.add(createMenuItemPanel(item));
            }
        }
        
        itemGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(itemGrid);
    }
    
    private JPanel createMenuItemPanel(MenuItem item) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        itemPanel.setPreferredSize(new Dimension(200, 280));
        
        // Image label
        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(150, 100));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        if (item.imageIcon != null) {
            imageLabel.setIcon(item.imageIcon);
        } else {
            // Create placeholder if no image
            imageLabel.setBackground(new Color(240, 240, 240));
            imageLabel.setOpaque(true);
            imageLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            imageLabel.setText("📸");
            imageLabel.setFont(new Font("Arial", Font.PLAIN, 40));
        }
        
        // Item info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(item.name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(new Color(60, 60, 60));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel priceLabel = new JLabel("Rp. " + String.format("%,d", item.price));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        priceLabel.setForeground(new Color(40, 167, 69));
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JTextArea descArea = new JTextArea(item.description);
        descArea.setFont(new Font("Arial", Font.PLAIN, 11));
        descArea.setForeground(new Color(100, 100, 100));
        descArea.setBackground(Color.WHITE);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add button
        JButton addButton = new JButton("+");
        addButton.setFont(new Font("Arial", Font.BOLD, 10));
        addButton.setBackground(new Color(82, 82, 82));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setPreferredSize(new Dimension(40, 40));
        addButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> addToOrder(item));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(addButton);
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(descArea);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(buttonPanel);
        
        itemPanel.add(imageLabel, BorderLayout.NORTH);
        itemPanel.add(infoPanel, BorderLayout.CENTER);
        
        return itemPanel;
    }
    
    private JPanel createOrderPanel() {
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.setBackground(new Color(248, 249, 250));
        orderPanel.setPreferredSize(new Dimension(350, 600));
        orderPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Order title
        JLabel orderTitle = new JLabel("Pesanan");
        orderTitle.setFont(new Font("Arial", Font.BOLD, 24));
        orderTitle.setForeground(new Color(60, 60, 60));
        orderTitle.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Cart panel
        cartPanel = new JPanel();
        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS));
        cartPanel.setBackground(new Color(248, 249, 250));
        
        cartScrollPane = new JScrollPane(cartPanel);
        cartScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        cartScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        cartScrollPane.setBorder(BorderFactory.createEmptyBorder());
        cartScrollPane.setBackground(new Color(248, 249, 250));
        
        // Total panel
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBackground(new Color(248, 249, 250));
        totalPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JLabel totalTextLabel = new JLabel("Total pesanan");
        totalTextLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalTextLabel.setForeground(new Color(60, 60, 60));
        
        totalLabel = new JLabel("Rp. 0");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setForeground(new Color(40, 167, 69));
        
        totalPanel.add(totalTextLabel, BorderLayout.WEST);
        totalPanel.add(totalLabel, BorderLayout.EAST);
        
        // Button panel with Checkout and Clear Cart buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        buttonPanel.setBackground(new Color(248, 249, 250));
        
        // Checkout button
        JButton checkoutButton = new JButton("Checkout");
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 16));
        checkoutButton.setBackground(new Color(40, 167, 69));
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setFocusPainted(false);
        checkoutButton.setBorderPainted(false);
        checkoutButton.setPreferredSize(new Dimension(300, 50));
        checkoutButton.addActionListener(e -> checkout());
        
        // Clear cart button
        JButton clearCartButton = new JButton("Bersihkan Keranjang");
        clearCartButton.setFont(new Font("Arial", Font.BOLD, 14));
        clearCartButton.setBackground(new Color(220, 53, 69));
        clearCartButton.setForeground(Color.WHITE);
        clearCartButton.setFocusPainted(false);
        clearCartButton.setBorderPainted(false);
        clearCartButton.setPreferredSize(new Dimension(300, 40));
        clearCartButton.addActionListener(e -> clearCart());
        
        buttonPanel.add(checkoutButton);
        buttonPanel.add(clearCartButton);
        
        orderPanel.add(orderTitle, BorderLayout.NORTH);
        orderPanel.add(cartScrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(248, 249, 250));
        bottomPanel.add(totalPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        
        orderPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        return orderPanel;
    }
    
    private void addToOrder(MenuItem item) {
        // Check if item already exists in order
        for (OrderItem orderItem : orderItems) {
            if (orderItem.menuItem.name.equals(item.name)) {
                orderItem.quantity++;
                updateOrderDisplay();
                return;
            }
        }
        
        // Add new item
        orderItems.add(new OrderItem(item, 1));
        updateOrderDisplay();
    }
    
    private void clearCart() {
        if (orderItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Keranjang sudah kosong!", 
                "Info", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int choice = JOptionPane.showConfirmDialog(this,
            "Apakah Anda yakin ingin mengosongkan keranjang?",
            "Konfirmasi Bersihkan Keranjang",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            orderItems.clear();
            updateOrderDisplay();
            JOptionPane.showMessageDialog(this, 
                "Keranjang berhasil dikosongkan!", 
                "Berhasil", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void updateOrderDisplay() {
        cartPanel.removeAll();
        
        if (orderItems.isEmpty()) {
            JLabel emptyLabel = new JLabel("Belum ada pesanan");
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            emptyLabel.setForeground(new Color(150, 150, 150));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            cartPanel.add(emptyLabel);
        } else {
            for (OrderItem orderItem : orderItems) {
                cartPanel.add(createOrderItemPanel(orderItem));
                cartPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        updateTotal();
        cartPanel.revalidate();
        cartPanel.repaint();
    }
    
    private JPanel createOrderItemPanel(OrderItem orderItem) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        itemPanel.setMaximumSize(new Dimension(300, 80));
        
        // Item info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(orderItem.menuItem.name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel priceLabel = new JLabel("Rp. " + String.format("%,d", orderItem.menuItem.price));
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        priceLabel.setForeground(new Color(100, 100, 100));
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(nameLabel);
        infoPanel.add(priceLabel);
        
        // Quantity controls
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        quantityPanel.setBackground(Color.WHITE);
        
        JButton minusButton = new JButton("-");
        minusButton.setFont(new Font("Arial", Font.PLAIN, 6));
        minusButton.setPreferredSize(new Dimension(30, 30));
        minusButton.setBackground(new Color(220, 53, 69));
        minusButton.setForeground(Color.WHITE);
        minusButton.setFocusPainted(false);
        minusButton.setBorderPainted(false);
        minusButton.addActionListener(e -> decreaseQuantity(orderItem));
        
        JLabel quantityLabel = new JLabel(String.valueOf(orderItem.quantity));
        quantityLabel.setFont(new Font("Arial", Font.BOLD, 14));
        quantityLabel.setHorizontalAlignment(SwingConstants.CENTER);
        quantityLabel.setPreferredSize(new Dimension(30, 30));
        
        JButton plusButton = new JButton("+");
        plusButton.setFont(new Font("Arial", Font.PLAIN, 6));
        plusButton.setPreferredSize(new Dimension(30, 30));
        plusButton.setBackground(new Color(40, 167, 69));
        plusButton.setForeground(Color.WHITE);
        plusButton.setFocusPainted(false);
        plusButton.setBorderPainted(false);
        plusButton.addActionListener(e -> increaseQuantity(orderItem));
        
        quantityPanel.add(minusButton);
        quantityPanel.add(quantityLabel);
        quantityPanel.add(plusButton);
        
        itemPanel.add(infoPanel, BorderLayout.WEST);
        itemPanel.add(quantityPanel, BorderLayout.EAST);
        
        return itemPanel;
    }
    
    private void increaseQuantity(OrderItem orderItem) {
        orderItem.quantity++;
        updateOrderDisplay();
    }
    
    private void decreaseQuantity(OrderItem orderItem) {
        if (orderItem.quantity > 1) {
            orderItem.quantity--;
        } else {
            orderItems.remove(orderItem);
        }
        updateOrderDisplay();
    }
    
    private void updateTotal() {
        int total = 0;
        for (OrderItem orderItem : orderItems) {
            total += orderItem.menuItem.price * orderItem.quantity;
        }
        totalLabel.setText("Rp. " + String.format("%,d", total));
    }

    // Fixed findMenuIdByName method
    private int findMenuIdByName(String menuName) {
        try {
            // First check if the menuId is already set in the MenuItem
            for (MenuItem item : menuItems) {
                if (item.name.equals(menuName) && item.menuId > 0) {
                    return item.menuId;
                }
            }
            
            // If not found, search in database
            List<Menu> allMenus = menuDAO.findAll();
            for (Menu menu : allMenus) {
                if (menu.getNamaMenu().equals(menuName)) {
                    return menu.getIdMenu();
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding menu ID: " + e.getMessage());
        }
        return -1;
    }

    // Fixed proceedToPayment method - properly defined
    private void proceedToPayment(int orderId, int total) {
        // Simpan order items sebelum clear untuk digunakan di nota
        List<OrderItem> savedOrderItems = new ArrayList<>(orderItems);
        
        // Tampilkan dialog metode pembayaran
        PaymentMethodDialog paymentDialog = new PaymentMethodDialog(this, orderId, total, savedOrderItems);
        paymentDialog.setVisible(true);

        String metodeBayar = paymentDialog.getSelectedPaymentMethod();

        if (metodeBayar != null) {
            // Bersihkan keranjang setelah pembayaran sukses
            orderItems.clear();
            updateOrderDisplay();
        } else {
            JOptionPane.showMessageDialog(this, "Pembayaran dibatalkan.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Cancel order and save to pesanan_dibatalkan table
     */
    private void cancelOrder(int orderId, String cancelReason) {
        try {
            // 1. Update status pesanan menjadi 'dibatalkan'
            if (customerOrderDAO.updateStatus(orderId, "dibatalkan")) {
                // 2. Simpan ke tabel pesanan_dibatalkan
                PesananDibatalkan pesananBatal = new PesananDibatalkan(
                    orderId,
                    new Timestamp(System.currentTimeMillis()),
                    cancelReason
                );
                
                if (pesananDibatalkanDAO.create(pesananBatal)) {
                    JOptionPane.showMessageDialog(this,
                        "Pesanan berhasil dibatalkan!\n" +
                        "ID Pesanan: " + orderId + "\n" +
                        "Alasan: " + cancelReason,
                        "Pesanan Dibatalkan",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    System.out.println("Order " + orderId + " cancelled successfully. Reason: " + cancelReason);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Pesanan dibatalkan, tetapi gagal menyimpan ke log pembatalan.",
                        "Peringatan",
                        JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Gagal membatalkan pesanan!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error saat membatalkan pesanan: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void checkout() {
        if (orderItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keranjang masih kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Hitung total
        int total = 0;
        for (OrderItem orderItem : orderItems) {
            total += orderItem.menuItem.price * orderItem.quantity;
        }

        // Buat ringkasan pesanan sebagai HTML
        StringBuilder orderSummaryHtml = new StringBuilder("<html><body style='width:300px'>");
        orderSummaryHtml.append("<h3>Ringkasan Pesanan:</h3><ul>");
        for (OrderItem orderItem : orderItems) {
            int itemTotal = orderItem.menuItem.price * orderItem.quantity;
            orderSummaryHtml.append(String.format("<li>%s x%d - Rp %,d</li>",
                    orderItem.menuItem.name, orderItem.quantity, itemTotal));
        }
        orderSummaryHtml.append("</ul>");
        orderSummaryHtml.append(String.format("<b>Total: Rp %,d</b>", total));
        orderSummaryHtml.append("</body></html>");

        JLabel summaryLabel = new JLabel(orderSummaryHtml.toString());

        // Panel input pesan opsional
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.FIRST_LINE_END;
        inputPanel.add(new JLabel("Pesan (Opsional):"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea messageArea = new JTextArea(5, 20);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        inputPanel.add(scrollPane, gbc);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.add(summaryLabel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        
        // Create custom button panel with Cancel option
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton continueButton = new JButton("Lanjutkan ke Pembayaran");
        continueButton.setBackground(new Color(40, 167, 69));
        continueButton.setForeground(Color.WHITE);
        continueButton.setFocusPainted(false);
        
        JButton cancelButton = new JButton("Batalkan Pesanan");
        cancelButton.setBackground(new Color(220, 53, 69));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        
        JButton backButton = new JButton("Kembali");
        backButton.setBackground(new Color(108, 117, 125));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        
        buttonPanel.add(continueButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(backButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Create custom dialog
        JDialog orderDialog = new JDialog(this, "Konfirmasi Pesanan", true);
        orderDialog.setSize(450, 400);
        orderDialog.setLocationRelativeTo(this);
        orderDialog.setContentPane(mainPanel);
        
        final boolean[] dialogResult = {false}; // false = back, true = continue
        final boolean[] shouldCancel = {false};
        
        continueButton.addActionListener(e -> {
            dialogResult[0] = true;
            orderDialog.dispose();
        });
        
        cancelButton.addActionListener(e -> {
            // Show cancel reason dialog
            String[] cancelOptions = {
                "Berubah pikiran",
                "Terlalu mahal", 
                "Salah pesan",
                "Waktu tunggu terlalu lama",
                "Lainnya..."
            };
            
            String selectedReason = (String) JOptionPane.showInputDialog(
                orderDialog,
                "Pilih alasan pembatalan atau ketik alasan Anda:",
                "Alasan Pembatalan",
                JOptionPane.QUESTION_MESSAGE,
                null,
                cancelOptions,
                cancelOptions[0]
            );
            
            if (selectedReason != null) {
                if ("Lainnya...".equals(selectedReason)) {
                    selectedReason = JOptionPane.showInputDialog(
                        orderDialog,
                        "Masukkan alasan pembatalan:",
                        "Alasan Pembatalan",
                        JOptionPane.PLAIN_MESSAGE
                    );
                    
                    if (selectedReason == null || selectedReason.trim().isEmpty()) {
                        selectedReason = "Tidak ada alasan";
                    }
                }
                
                shouldCancel[0] = true;
                
                // Clear the cart immediately when cancelled
                orderItems.clear();
                updateOrderDisplay();
                
                JOptionPane.showMessageDialog(orderDialog,
                    "Pesanan dibatalkan.\nAlasan: " + selectedReason,
                    "Pesanan Dibatalkan",
                    JOptionPane.INFORMATION_MESSAGE);
                
                orderDialog.dispose();
            }
        });
        
        backButton.addActionListener(e -> {
            dialogResult[0] = false;
            orderDialog.dispose();
        });
        
        orderDialog.setVisible(true);
        
        // Handle dialog result
        if (shouldCancel[0]) {
            return; // Order was cancelled, nothing more to do
        }
        
        if (!dialogResult[0]) {
            return; // User clicked back or closed dialog
        }

        String messageOptional = messageArea.getText().trim();

        // Simpan pesanan ke database
        try {
            // 1. Buat Customer Order
            CustomerOrder customerOrder = new CustomerOrder(
                new Timestamp(System.currentTimeMillis()),
                (double) total,
                messageOptional.isEmpty() ? null : messageOptional,
                currentUserId,
                "pending"
            );
            
            int orderId = customerOrderDAO.create(customerOrder);
            if (orderId <= 0) {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan pesanan!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 2. Simpan Order Details  
            boolean allDetailsSaved = true;
            for (OrderItem orderItem : orderItems) {
                // Cari menu ID dari database jika belum ada
                int menuId = findMenuIdByName(orderItem.menuItem.name);
                if (menuId <= 0) {
                    System.err.println("Menu ID not found for: " + orderItem.menuItem.name);
                    continue;
                }
                
                OrderDetail orderDetail = new OrderDetail(
                    orderId,
                    menuId,
                    orderItem.quantity,
                    (double) orderItem.menuItem.price
                );
                
                if (!orderDetailDAO.create(orderDetail)) {
                    allDetailsSaved = false;
                    System.err.println("Failed to save order detail for: " + orderItem.menuItem.name);
                }
            }
            
            if (!allDetailsSaved) {
                // Give option to cancel the order if details failed to save
                int choice = JOptionPane.showConfirmDialog(this,
                    "Beberapa detail pesanan gagal disimpan.\n" +
                    "Apakah Anda ingin membatalkan pesanan ini?",
                    "Error Menyimpan Detail",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (choice == JOptionPane.YES_OPTION) {
                    cancelOrder(orderId, "Gagal menyimpan detail pesanan");
                    return;
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Pesanan tersimpan dengan ID: " + orderId + 
                        "\nNamun beberapa detail gagal disimpan.", 
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Pesanan berhasil disimpan!\nID Pesanan: " + orderId + 
                    "\nSilakan lanjutkan ke pembayaran.", 
                    "Pesanan Berhasil", JOptionPane.INFORMATION_MESSAGE);
            }
            
            // 3. Lanjutkan ke pembayaran
            proceedToPayment(orderId, total);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error saat menyimpan pesanan: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // PaymentMethodDialog class with cancel order integration
    class PaymentMethodDialog extends JDialog {
        private int orderId;
        private int totalAmount;
        private String selectedPaymentMethod;
        private CustomerFrame parentFrame;
        private List<OrderItem> savedOrderItems;

        public PaymentMethodDialog(Frame parent, int orderId, int totalAmount, List<OrderItem> savedOrderItems) {
            super(parent, "Metode Pembayaran", true);
            this.orderId = orderId;
            this.totalAmount = totalAmount;
            this.parentFrame = (CustomerFrame) parent;
            this.savedOrderItems = new ArrayList<>(savedOrderItems);

            setSize(400, 300); // Increased height for cancel button
            setLocationRelativeTo(parent);

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());

            JLabel titleLabel = new JLabel(
                "<html><center><h2>Metode Pembayaran</h2>ID Pesanan: " + orderId + 
                "<br>Total: Rp " + String.format("%,d", totalAmount) + "</center></html>",
                SwingConstants.CENTER);
            panel.add(titleLabel, BorderLayout.NORTH);

            // Panel tombol metode pembayaran
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));

            // Tombol Cash
            JButton cashButton = new JButton("Cash");
            cashButton.setPreferredSize(new Dimension(120, 80));
            cashButton.setBackground(new Color(40, 167, 69));
            cashButton.setForeground(Color.WHITE);
            cashButton.setFocusPainted(false);
            cashButton.addActionListener(e -> {
                selectedPaymentMethod = "cash";
                processPayment();
                dispose();
            });

            // Tombol QRIS
            JButton qrisButton = new JButton("QRIS");
            qrisButton.setPreferredSize(new Dimension(120, 80));
            qrisButton.setBackground(new Color(0, 123, 255));
            qrisButton.setForeground(Color.WHITE);
            qrisButton.setFocusPainted(false);
            qrisButton.addActionListener(e -> {
                selectedPaymentMethod = "qris";
                showQRISPopup();
            });

            btnPanel.add(cashButton);
            btnPanel.add(qrisButton);

            panel.add(btnPanel, BorderLayout.CENTER);

            // Panel tombol batal dengan opsi cancel order
            JPanel bottomPanel = new JPanel(new BorderLayout());
            
            JButton cancelOrderButton = new JButton("Batalkan Pesanan");
            cancelOrderButton.setBackground(new Color(220, 53, 69));
            cancelOrderButton.setForeground(Color.WHITE);
            cancelOrderButton.setFocusPainted(false);
            cancelOrderButton.addActionListener(e -> showCancelOrderDialog());
            
            JButton backButton = new JButton("Kembali");
            backButton.setBackground(new Color(108, 117, 125));
            backButton.setForeground(Color.WHITE);
            backButton.setFocusPainted(false);
            backButton.addActionListener(e -> {
                selectedPaymentMethod = null;
                dispose();
            });
            
            JPanel cancelPanel = new JPanel(new FlowLayout());
            cancelPanel.add(backButton);
            cancelPanel.add(cancelOrderButton);
            bottomPanel.add(cancelPanel, BorderLayout.CENTER);
            
            panel.add(bottomPanel, BorderLayout.SOUTH);

            setContentPane(panel);
        }
        
        private void showCancelOrderDialog() {
            String[] cancelOptions = {
                "Berubah pikiran tentang pembayaran",
                "Metode pembayaran tidak tersedia", 
                "Terlalu lama menunggu",
                "Ingin mengubah pesanan",
                "Lainnya..."
            };
            
            String selectedReason = (String) JOptionPane.showInputDialog(
                this,
                "Pilih alasan pembatalan atau ketik alasan Anda:",
                "Alasan Pembatalan Pesanan",
                JOptionPane.QUESTION_MESSAGE,
                null,
                cancelOptions,
                cancelOptions[0]
            );
            
            if (selectedReason != null) {
                if ("Lainnya...".equals(selectedReason)) {
                    selectedReason = JOptionPane.showInputDialog(
                        this,
                        "Masukkan alasan pembatalan:",
                        "Alasan Pembatalan",
                        JOptionPane.PLAIN_MESSAGE
                    );
                    
                    if (selectedReason == null || selectedReason.trim().isEmpty()) {
                        selectedReason = "Dibatalkan saat pembayaran";
                    }
                }
                
                // Confirm cancellation
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Apakah Anda yakin ingin membatalkan pesanan?\n" +
                    "ID Pesanan: " + orderId + "\n" +
                    "Total: Rp " + String.format("%,d", totalAmount) + "\n" +
                    "Alasan: " + selectedReason,
                    "Konfirmasi Pembatalan",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    parentFrame.cancelOrder(orderId, selectedReason);
                    dispose(); // Close payment dialog
                }
            }
        }

        public String getSelectedPaymentMethod() {
            return selectedPaymentMethod;
        }

        private void processPayment() {
            try {
                // 1. Update status pesanan
                customerOrderDAO.updateStatus(orderId, "selesai");
                
                // 2. Buat record pembayaran
                Pembayaran pembayaran = new Pembayaran(
                    orderId,
                    2, // Default kasir ID (bisa disesuaikan)
                    new Timestamp(System.currentTimeMillis()),
                    selectedPaymentMethod,
                    (double) totalAmount,
                    "berhasil"
                );
                
                if (pembayaranDAO.create(pembayaran)) {
                    // 3. Buat nota
                    Nota nota = new Nota(
                        orderId,
                        new Timestamp(System.currentTimeMillis()),
                        (double) totalAmount,
                        selectedPaymentMethod,
                        "berhasil"
                    );
                    
                    if (notaDAO.create(nota)) {
                        showNotaDetail();
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Pembayaran berhasil, tetapi nota gagal dibuat.", 
                            "Peringatan", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Gagal menyimpan data pembayaran!", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Error saat memproses pembayaran: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void showQRISPopup() {
            JDialog qrisDialog = new JDialog(this, "Pembayaran QRIS", true);
            qrisDialog.setSize(350, 500); // Increased height for cancel button
            qrisDialog.setLocationRelativeTo(this);

            JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            int qrSize = 200;

            // QR Code placeholder
            JLabel barcodeLabel = new JLabel();
            barcodeLabel.setPreferredSize(new Dimension(qrSize, qrSize));
            barcodeLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            barcodeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            barcodeLabel.setVerticalAlignment(SwingConstants.CENTER);
            barcodeLabel.setText("<html><center>QR CODE<br>PLACEHOLDER</center></html>");
            barcodeLabel.setBackground(new Color(240, 240, 240));
            barcodeLabel.setOpaque(true);

            JLabel detailLabel = new JLabel(
                "<html><center>ID Pesanan: " + orderId + "<br>Total pembayaran:<br>Rp " + 
                String.format("%,d", totalAmount) + "<br><br>" +
                "Silakan scan QR code di atas menggunakan aplikasi QRIS Anda.</center></html>",
                SwingConstants.CENTER);

            JButton okButton = new JButton("Pembayaran Selesai");
            okButton.setPreferredSize(new Dimension(200, 40));
            okButton.setBackground(new Color(40, 167, 69));
            okButton.setForeground(Color.WHITE);
            okButton.setFocusPainted(false);
            okButton.addActionListener(e -> {
                qrisDialog.dispose();
                processPayment();
                dispose();
            });
            
            JButton cancelQrisButton = new JButton("Batalkan Pesanan");
            cancelQrisButton.setPreferredSize(new Dimension(200, 40));
            cancelQrisButton.setBackground(new Color(220, 53, 69));
            cancelQrisButton.setForeground(Color.WHITE);
            cancelQrisButton.setFocusPainted(false);
            cancelQrisButton.addActionListener(e -> {
                qrisDialog.dispose();
                showCancelOrderDialog();
            });

            JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
            buttonPanel.add(okButton);
            buttonPanel.add(cancelQrisButton);

            contentPanel.add(barcodeLabel, BorderLayout.NORTH);
            contentPanel.add(detailLabel, BorderLayout.CENTER);
            contentPanel.add(buttonPanel, BorderLayout.SOUTH);

            qrisDialog.setContentPane(contentPanel);
            qrisDialog.setVisible(true);
        }

        private void showNotaDetail() {
            String idNota = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            showNotaPopup(idNota);
        }

        private void showNotaPopup(String idNota) {
            JDialog notaDialog = new JDialog(this, "Nota Pembayaran", true);
            notaDialog.setSize(400, 550);
            notaDialog.setLocationRelativeTo(this);
            
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // Header nota
            JPanel headerPanel = new JPanel(new BorderLayout());
            JLabel titleLabel = new JLabel("NOTA PEMBAYARAN", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            
            JLabel idLabel = new JLabel("ID Pesanan: " + orderId, SwingConstants.CENTER);
            idLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            idLabel.setForeground(new Color(100, 100, 100));
            
            headerPanel.add(titleLabel, BorderLayout.NORTH);
            headerPanel.add(idLabel, BorderLayout.CENTER);
            
            // Detail pesanan
            JPanel detailPanel = new JPanel();
            detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
            detailPanel.setBorder(BorderFactory.createTitledBorder("Detail Pesanan"));
            
            for (OrderItem orderItem : savedOrderItems) {
                JPanel itemPanel = new JPanel(new BorderLayout());
                itemPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                
                JLabel itemLabel = new JLabel(orderItem.menuItem.name + " x" + orderItem.quantity);
                itemLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                
                JLabel priceLabel = new JLabel("Rp " + String.format("%,d", orderItem.menuItem.price * orderItem.quantity));
                priceLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                
                itemPanel.add(itemLabel, BorderLayout.WEST);
                itemPanel.add(priceLabel, BorderLayout.EAST);
                detailPanel.add(itemPanel);
            }

            JPanel totalPanel = new JPanel(new BorderLayout());
            totalPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            JLabel totalTextLabel = new JLabel("TOTAL BAYAR", SwingConstants.LEFT);
            totalTextLabel.setFont(new Font("Arial", Font.BOLD, 16));
            JLabel totalValueLabel = new JLabel("Rp " + String.format("%,d", totalAmount), SwingConstants.RIGHT);
            totalValueLabel.setFont(new Font("Arial", Font.BOLD, 16));
            totalValueLabel.setForeground(new Color(40, 167, 69));
            totalPanel.add(totalTextLabel, BorderLayout.WEST);
            totalPanel.add(totalValueLabel, BorderLayout.EAST);
            
            // Info pembayaran
            JLabel infoLabel = new JLabel("<html><center>Pembayaran dengan " + selectedPaymentMethod.toUpperCase() + 
                "<br>Customer: " + currentCustomerName + "<br>Terima kasih telah berbelanja!</center></html>", 
                SwingConstants.CENTER);
            infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            infoLabel.setForeground(new Color(100, 100, 100));
            
            // Tombol Print dan Tutup
            JButton printButton = new JButton("Print Nota");
            printButton.setPreferredSize(new Dimension(120, 35));
            printButton.setBackground(new Color(40, 167, 69));
            printButton.setForeground(Color.WHITE);
            printButton.setFocusPainted(false);
            printButton.addActionListener(e -> {
                notaDialog.dispose();
                printNota(idNota);
            });
            
            JButton closeButton = new JButton("Tutup");
            closeButton.setPreferredSize(new Dimension(120, 35));
            closeButton.setBackground(new Color(108, 117, 125));
            closeButton.setForeground(Color.WHITE);
            closeButton.setFocusPainted(false);
            closeButton.addActionListener(e -> notaDialog.dispose());
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(printButton);
            buttonPanel.add(closeButton);

            mainPanel.add(headerPanel, BorderLayout.NORTH);
            mainPanel.add(detailPanel, BorderLayout.CENTER);
            
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.add(totalPanel, BorderLayout.NORTH);
            bottomPanel.add(infoLabel, BorderLayout.CENTER);
            bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            mainPanel.add(bottomPanel, BorderLayout.SOUTH);
            
            notaDialog.setContentPane(mainPanel);
            notaDialog.setVisible(true);
        }

        private void printNota(String idNota) {
            JDialog printDialog = new JDialog(this, "Mencetak Nota", true);
            printDialog.setSize(300, 150);
            printDialog.setLocationRelativeTo(this);

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel printingLabel = new JLabel("Sedang mencetak nota...", SwingConstants.CENTER);
            printingLabel.setFont(new Font("Arial", Font.PLAIN, 16));

            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);

            panel.add(printingLabel, BorderLayout.CENTER);
            panel.add(progressBar, BorderLayout.SOUTH);

            printDialog.setContentPane(panel);

            SwingWorker<Void, Void> printWorker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Thread.sleep(2000); // Simulasi proses printing 2 detik
                    return null;
                }

                @Override
                protected void done() {
                    printDialog.dispose();
                    showPrintSuccessDialog();
                }
            };

            printWorker.execute();
            printDialog.setVisible(true);
        }

        private void showPrintSuccessDialog() {
            JDialog successDialog = new JDialog(this, "Nota Berhasil Dicetak", true);
            successDialog.setSize(400, 200);
            successDialog.setLocationRelativeTo(this);
            
            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            JLabel titleLabel = new JLabel("✓ NOTA BERHASIL DICETAK", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            titleLabel.setForeground(new Color(40, 167, 69));
            
            JLabel infoLabel = new JLabel("<html><center>Pesanan ID: " + orderId + 
                "<br>Customer: " + currentCustomerName + 
                "<br>Total: Rp " + String.format("%,d", totalAmount) + 
                "<br><br>Terima kasih telah berbelanja di Dapur Arunika!</center></html>", 
                SwingConstants.CENTER);
            infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            
            JButton closeButton = new JButton("Tutup");
            closeButton.setPreferredSize(new Dimension(120, 35));
            closeButton.setBackground(new Color(70, 130, 180));
            closeButton.setForeground(Color.WHITE);
            closeButton.setFocusPainted(false);
            closeButton.addActionListener(e -> successDialog.dispose());
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(closeButton);
            
            mainPanel.add(titleLabel, BorderLayout.NORTH);
            mainPanel.add(infoLabel, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            successDialog.setContentPane(mainPanel);
            successDialog.setVisible(true);
        }
    }

    private void setupEventHandlers() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                logout();
            }
        });
    }
    
    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this, 
            "Apakah Anda yakin ingin keluar?", 
            "Konfirmasi Logout", 
            JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new CustomerFrame().setVisible(true);
        });
    }
}