package view;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailDAO {
    private Connection connection;

    public OrderDetailDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public boolean create(OrderDetail orderDetail) {
        String sql = "INSERT INTO Order_Detail (id_pesanan, id_menu, jumlah, harga_satuan) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderDetail.getIdPesanan());
            stmt.setInt(2, orderDetail.getIdMenu());
            stmt.setInt(3, orderDetail.getJumlah());
            stmt.setDouble(4, orderDetail.getHargaSatuan());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<OrderDetail> findByOrderId(int idPesanan) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        String sql = "SELECT * FROM Order_Detail WHERE id_pesanan = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPesanan);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrderDetailId(rs.getInt("order_detail_id"));
                orderDetail.setIdPesanan(rs.getInt("id_pesanan"));
                orderDetail.setIdMenu(rs.getInt("id_menu"));
                orderDetail.setJumlah(rs.getInt("jumlah"));
                orderDetail.setHargaSatuan(rs.getDouble("harga_satuan"));
                orderDetails.add(orderDetail);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderDetails;
    }

    public boolean deleteByOrderId(int idPesanan) {
        String sql = "DELETE FROM Order_Detail WHERE id_pesanan = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPesanan);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}