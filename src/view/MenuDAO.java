package view;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {
    private Connection connection;

    public MenuDAO() {
        this.connection = DatabaseConnection.getConnection();
        if (this.connection == null) {
            System.err.println("Failed to establish database connection!");
        }
    }

    /**
     * Create new menu with description support
     */
    public boolean create(Menu menu) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return false;
        }

        String sql = "INSERT INTO Menu (nama_menu, jenis_menu, harga, deskripsi, ketersediaan, gambar) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, menu.getNamaMenu());
            stmt.setString(2, menu.getJenisMenu());
            stmt.setDouble(3, menu.getHarga());
            stmt.setString(4, menu.getDeskripsi());
            stmt.setString(5, menu.getKetersediaan());
            stmt.setString(6, menu.getGambar());
            
            int rowsAffected = stmt.executeUpdate();
            
            // Get generated ID and set it to the menu object
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        menu.setIdMenu(generatedKeys.getInt(1));
                    }
                }
            }
            
            System.out.println("Menu created successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in create(): " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Find menu by ID with description support
     */
    public Menu findById(int idMenu) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return null;
        }
        
        String sql = "SELECT id_menu, nama_menu, jenis_menu, harga, deskripsi, ketersediaan, gambar FROM Menu WHERE id_menu = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idMenu);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Menu menu = new Menu();
                menu.setIdMenu(rs.getInt("id_menu"));
                menu.setNamaMenu(rs.getString("nama_menu"));
                menu.setJenisMenu(rs.getString("jenis_menu"));
                menu.setHarga(rs.getDouble("harga"));
                menu.setDeskripsi(rs.getString("deskripsi"));
                menu.setKetersediaan(rs.getString("ketersediaan"));
                menu.setGambar(rs.getString("gambar"));
                return menu;
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in findById(): " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find all menus with description support
     */
    public List<Menu> findAll() {
        List<Menu> menus = new ArrayList<>();
        if (connection == null) {
            System.err.println("Database connection is null!");
            return menus;
        }
        
        String sql = "SELECT id_menu, nama_menu, jenis_menu, harga, deskripsi, ketersediaan, gambar FROM Menu ORDER BY jenis_menu, nama_menu";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Menu menu = new Menu();
                menu.setIdMenu(rs.getInt("id_menu"));
                menu.setNamaMenu(rs.getString("nama_menu"));
                menu.setJenisMenu(rs.getString("jenis_menu"));
                menu.setHarga(rs.getDouble("harga"));
                menu.setDeskripsi(rs.getString("deskripsi"));
                menu.setKetersediaan(rs.getString("ketersediaan"));
                menu.setGambar(rs.getString("gambar"));
                menus.add(menu);
            }
            System.out.println("Found " + menus.size() + " menus");
        } catch (SQLException e) {
            System.err.println("SQL Error in findAll(): " + e.getMessage());
            e.printStackTrace();
        }
        return menus;
    }

    /**
     * Find menus by type with description support
     */
    public List<Menu> findByJenis(String jenisMenu) {
        List<Menu> menus = new ArrayList<>();
        if (connection == null) {
            System.err.println("Database connection is null!");
            return menus;
        }
        
        String sql = "SELECT id_menu, nama_menu, jenis_menu, harga, deskripsi, ketersediaan, gambar FROM Menu WHERE jenis_menu = ? ORDER BY nama_menu";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, jenisMenu);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Menu menu = new Menu();
                menu.setIdMenu(rs.getInt("id_menu"));
                menu.setNamaMenu(rs.getString("nama_menu"));
                menu.setJenisMenu(rs.getString("jenis_menu"));
                menu.setHarga(rs.getDouble("harga"));
                menu.setDeskripsi(rs.getString("deskripsi"));
                menu.setKetersediaan(rs.getString("ketersediaan"));
                menu.setGambar(rs.getString("gambar"));
                menus.add(menu);
            }
            System.out.println("Found " + menus.size() + " menus of type: " + jenisMenu);
        } catch (SQLException e) {
            System.err.println("SQL Error in findByJenis(): " + e.getMessage());
            e.printStackTrace();
        }
        return menus;
    }

    /**
     * Find available menus only
     */
    public List<Menu> findAvailableMenus() {
        List<Menu> menus = new ArrayList<>();
        if (connection == null) {
            System.err.println("Database connection is null!");
            return menus;
        }
        
        String sql = "SELECT id_menu, nama_menu, jenis_menu, harga, deskripsi, ketersediaan, gambar FROM Menu WHERE ketersediaan = '1' ORDER BY jenis_menu, nama_menu";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Menu menu = new Menu();
                menu.setIdMenu(rs.getInt("id_menu"));
                menu.setNamaMenu(rs.getString("nama_menu"));
                menu.setJenisMenu(rs.getString("jenis_menu"));
                menu.setHarga(rs.getDouble("harga"));
                menu.setDeskripsi(rs.getString("deskripsi"));
                menu.setKetersediaan(rs.getString("ketersediaan"));
                menu.setGambar(rs.getString("gambar"));
                menus.add(menu);
            }
            System.out.println("Found " + menus.size() + " available menus");
        } catch (SQLException e) {
            System.err.println("SQL Error in findAvailableMenus(): " + e.getMessage());
            e.printStackTrace();
        }
        return menus;
    }

    /**
     * Update menu with description support
     */
    public boolean update(Menu menu) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return false;
        }
        
        String sql = "UPDATE Menu SET nama_menu = ?, jenis_menu = ?, harga = ?, deskripsi = ?, ketersediaan = ?, gambar = ? WHERE id_menu = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, menu.getNamaMenu());
            stmt.setString(2, menu.getJenisMenu());
            stmt.setDouble(3, menu.getHarga());
            stmt.setString(4, menu.getDeskripsi());
            stmt.setString(5, menu.getKetersediaan());
            stmt.setString(6, menu.getGambar());
            stmt.setInt(7, menu.getIdMenu());
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Menu updated successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in update(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update only the description of a menu
     */
    public boolean updateDescription(int idMenu, String deskripsi) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return false;
        }
        
        String sql = "UPDATE Menu SET deskripsi = ? WHERE id_menu = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, deskripsi);
            stmt.setInt(2, idMenu);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Menu description updated successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in updateDescription(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update availability status
     */
    public boolean updateAvailability(int idMenu, boolean available) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return false;
        }
        
        String sql = "UPDATE Menu SET ketersediaan = ? WHERE id_menu = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, available ? "1" : "0");
            stmt.setInt(2, idMenu);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Menu availability updated successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in updateAvailability(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete menu by ID
     */
    public boolean delete(int idMenu) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return false;
        }
        
        String sql = "DELETE FROM Menu WHERE id_menu = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idMenu);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Menu deleted successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in delete(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Search menus by name
     */
    public List<Menu> searchByName(String searchTerm) {
        List<Menu> menus = new ArrayList<>();
        if (connection == null) {
            System.err.println("Database connection is null!");
            return menus;
        }
        
        String sql = "SELECT id_menu, nama_menu, jenis_menu, harga, deskripsi, ketersediaan, gambar FROM Menu WHERE nama_menu LIKE ? ORDER BY nama_menu";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Menu menu = new Menu();
                menu.setIdMenu(rs.getInt("id_menu"));
                menu.setNamaMenu(rs.getString("nama_menu"));
                menu.setJenisMenu(rs.getString("jenis_menu"));
                menu.setHarga(rs.getDouble("harga"));
                menu.setDeskripsi(rs.getString("deskripsi"));
                menu.setKetersediaan(rs.getString("ketersediaan"));
                menu.setGambar(rs.getString("gambar"));
                menus.add(menu);
            }
            System.out.println("Found " + menus.size() + " menus matching search term: " + searchTerm);
        } catch (SQLException e) {
            System.err.println("SQL Error in searchByName(): " + e.getMessage());
            e.printStackTrace();
        }
        return menus;
    }

    /**
     * Get menu count by type
     */
    public int getMenuCountByType(String jenisMenu) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return 0;
        }
        
        String sql = "SELECT COUNT(*) as count FROM Menu WHERE jenis_menu = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, jenisMenu);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getMenuCountByType(): " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Check if menu name already exists (for validation)
     */
    public boolean isMenuNameExists(String namaMenu, int excludeId) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return false;
        }
        
        String sql = "SELECT COUNT(*) as count FROM Menu WHERE nama_menu = ? AND id_menu != ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, namaMenu);
            stmt.setInt(2, excludeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in isMenuNameExists(): " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Close database connection
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed successfully");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
}