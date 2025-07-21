package view;




import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PembayaranDAO {
    private Connection connection;

    public PembayaranDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public boolean create(Pembayaran pembayaran) {
        String sql = "INSERT INTO Pembayaran (id_pesanan, id_kasir, tanggal_pembayaran, metode_pembayaran, jumlah_pembayaran, status_pembayaran) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, pembayaran.getIdPesanan());
            stmt.setInt(2, pembayaran.getIdKasir());
            stmt.setTimestamp(3, pembayaran.getTanggalPembayaran());
            stmt.setString(4, pembayaran.getMetodePembayaran());
            stmt.setDouble(5, pembayaran.getJumlahPembayaran());
            stmt.setString(6, pembayaran.getStatusPembayaran());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Pembayaran findByOrderId(int idPesanan) {
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
            e.printStackTrace();
        }
        return null;
    }

    public List<Pembayaran> findAll() {
        List<Pembayaran> pembayarans = new ArrayList<>();
        String sql = "SELECT * FROM Pembayaran ORDER BY tanggal_pembayaran DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Pembayaran pembayaran = new Pembayaran();
                pembayaran.setIdPembayaran(rs.getInt("id_pembayaran"));
                pembayaran.setIdPesanan(rs.getInt("id_pesanan"));
                pembayaran.setIdKasir(rs.getInt("id_kasir"));
                pembayaran.setTanggalPembayaran(rs.getTimestamp("tanggal_pembayaran"));
                pembayaran.setMetodePembayaran(rs.getString("metode_pembayaran"));
                pembayaran.setJumlahPembayaran(rs.getDouble("jumlah_pembayaran"));
                pembayaran.setStatusPembayaran(rs.getString("status_pembayaran"));
                pembayarans.add(pembayaran);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pembayarans;
    }

    public boolean updateStatus(int idPembayaran, String status) {
        String sql = "UPDATE Pembayaran SET status_pembayaran = ? WHERE id_pembayaran = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, idPembayaran);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int idPembayaran) {
        String sql = "DELETE FROM Pembayaran WHERE id_pembayaran = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPembayaran);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
