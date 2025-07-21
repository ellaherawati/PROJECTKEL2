package view;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantDAO {
    private Connection connection;

    public RestaurantDAO() {
        this.connection = DatabaseConnection.getConnection();
        if (this.connection == null) {
            System.err.println("Failed to establish database connection!");
        }
    }

    // Nota methods
    public Nota findNotaById(int idNota) {
        if (connection == null) return null;
        
        String sql = "SELECT * FROM Nota WHERE id_nota = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idNota);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Nota nota = new Nota();
                nota.setIdNota(rs.getInt("id_nota"));
                nota.setIdPesanan(rs.getInt("id_pesanan"));
                nota.setWaktuCetak(rs.getTimestamp("waktu_cetak"));
                nota.setTotalPembayaran(rs.getDouble("total_pembayaran"));
                nota.setMetodePembayaran(rs.getString("metode_pembayaran"));
                nota.setStatusPembayaran(rs.getString("status_pembayaran"));
                return nota;
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in findNotaById(): " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Customer Order methods
    public CustomerOrder findCustomerOrderById(int idPesanan) {
        if (connection == null) return null;
        
        String sql = "SELECT * FROM Customer_Order WHERE id_pesanan = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPesanan);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                CustomerOrder order = new CustomerOrder();
                order.setIdPesanan(rs.getInt("id_pesanan"));
                order.setTanggalPesanan(rs.getTimestamp("tanggal_pesanan"));
                order.setTotalPesanan(rs.getDouble("total_pesanan"));
                order.setCatatan(rs.getString("catatan"));
                order.setCustomerId(rs.getInt("customer_id"));
                order.setStatusPesanan(rs.getString("status_pesanan"));
                return order;
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in findCustomerOrderById(): " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Order Detail methods
    public List<OrderDetail> findOrderDetailsByPesananId(int idPesanan) {
        List<OrderDetail> details = new ArrayList<>();
        if (connection == null) return details;
        
        String sql = """
            SELECT od.*, m.nama_menu 
            FROM Order_Detail od 
            JOIN Menu m ON od.id_menu = m.id_menu 
            WHERE od.id_pesanan = ?
            ORDER BY od.order_detail_id
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPesanan);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                OrderDetail detail = new OrderDetail();
                detail.setOrderDetailId(rs.getInt("order_detail_id"));
                detail.setIdPesanan(rs.getInt("id_pesanan"));
                detail.setIdMenu(rs.getInt("id_menu"));
                detail.setJumlah(rs.getInt("jumlah"));
                detail.setHargaSatuan(rs.getDouble("harga_satuan"));
                detail.setNamaMenu(rs.getString("nama_menu"));
                detail.setSubtotal(detail.getJumlah() * detail.getHargaSatuan());
                details.add(detail);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in findOrderDetailsByPesananId(): " + e.getMessage());
            e.printStackTrace();
        }
        return details;
    }

    // Pembayaran methods
    public Pembayaran findPembayaranByPesananId(int idPesanan) {
        if (connection == null) return null;
        
        String sql = "SELECT * FROM Pembayaran WHERE id_pesanan = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPesanan);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Pembayaran pembayaran = new Pembayaran();
                pembayaran.setIdPembayaran(rs.getInt("id_pembayaran"));
                pembayaran.setIdPesanan(rs.getInt("id_pesanan"));
                pembayaran.setIdKasir(rs.getInt("id_kasir"));
                pembayaran.setTanggalPembayaran(rs.getTimestamp("tanggal_pembayaran"));
                pembayaran.setMetodePembayaran(rs.getString("metode_pembayaran"));
                pembayaran.setJumlahPembayaran(rs.getDouble("jumlah_pembayaran"));
                pembayaran.setStatusPembayaran(rs.getString("status_pembayaran"));
                return pembayaran;
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in findPembayaranByPesananId(): " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Get customer name (assuming there's a customer table or info in User table)
    public String getCustomerName(int customerId) {
        if (connection == null) return "Unknown";
        
        // Try User table first
        String sql = "SELECT nama FROM User WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nama");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getCustomerName(): " + e.getMessage());
            e.printStackTrace();
        }
        return "Customer #" + customerId;
    }

    // Get kasir name
    public String getKasirName(int kasirId) {
        if (connection == null) return "Unknown";
        
        String sql = "SELECT nama FROM User WHERE user_id = ? AND role = 'kasir'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, kasirId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nama");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getKasirName(): " + e.getMessage());
            e.printStackTrace();
        }
        return "Kasir #" + kasirId;
    }

    // Get menu info
    public Menu getMenuById(int idMenu) {
        if (connection == null) return null;
        
        String sql = "SELECT * FROM Menu WHERE id_menu = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idMenu);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Menu menu = new Menu();
                menu.setIdMenu(rs.getInt("id_menu"));
                menu.setNamaMenu(rs.getString("nama_menu"));
                menu.setJenisMenu(rs.getString("jenis_menu"));
                menu.setHarga(rs.getDouble("harga"));
                menu.setKetersediaan(rs.getString("ketersediaan"));
                return menu;
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in getMenuById(): " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
