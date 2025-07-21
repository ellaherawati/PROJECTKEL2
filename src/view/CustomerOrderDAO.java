package view;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerOrderDAO {
    private Connection connection;

    public CustomerOrderDAO() {
        this.connection = DatabaseConnection.getConnection();
        if (this.connection == null) {
            System.err.println("Failed to establish database connection!");
        }
    }

    public int create(CustomerOrder order) {
        if (connection == null) {
            System.err.println("Database connection is null!");
            return -1;
        }

        String sql = "INSERT INTO Customer_Order (tanggal_pesanan, total_pesanan, catatan, customer_id, status_pesanan) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, order.getTanggalPesanan());
            stmt.setDouble(2, order.getTotalPesanan());
            stmt.setString(3, order.getCatatan());
            stmt.setInt(4, order.getCustomerId());
            stmt.setString(5, order.getStatusPesanan());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in create(): " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public CustomerOrder findById(int idPesanan) {
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
            System.err.println("SQL Error in findById(): " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<CustomerOrder> findByCustomerId(int customerId) {
        List<CustomerOrder> orders = new ArrayList<>();
        if (connection == null) return orders;
        
        String sql = "SELECT * FROM Customer_Order WHERE customer_id = ? ORDER BY tanggal_pesanan DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CustomerOrder order = new CustomerOrder();
                order.setIdPesanan(rs.getInt("id_pesanan"));
                order.setTanggalPesanan(rs.getTimestamp("tanggal_pesanan"));
                order.setTotalPesanan(rs.getDouble("total_pesanan"));
                order.setCatatan(rs.getString("catatan"));
                order.setCustomerId(rs.getInt("customer_id"));
                order.setStatusPesanan(rs.getString("status_pesanan"));
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in findByCustomerId(): " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }

    public boolean updateStatus(int idPesanan, String status) {
        if (connection == null) return false;
        
        String sql = "UPDATE Customer_Order SET status_pesanan = ? WHERE id_pesanan = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, idPesanan);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in updateStatus(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<CustomerOrder> findAll() {
        List<CustomerOrder> orders = new ArrayList<>();
        if (connection == null) return orders;
        
        String sql = "SELECT * FROM Customer_Order ORDER BY tanggal_pesanan DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CustomerOrder order = new CustomerOrder();
                order.setIdPesanan(rs.getInt("id_pesanan"));
                order.setTanggalPesanan(rs.getTimestamp("tanggal_pesanan"));
                order.setTotalPesanan(rs.getDouble("total_pesanan"));
                order.setCatatan(rs.getString("catatan"));
                order.setCustomerId(rs.getInt("customer_id"));
                order.setStatusPesanan(rs.getString("status_pesanan"));
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in findAll(): " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }

    public boolean delete(int idPesanan) {
        if (connection == null) return false;
        
        String sql = "DELETE FROM Customer_Order WHERE id_pesanan = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPesanan);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error in delete(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}