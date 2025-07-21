package view;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotaDAO {
    private Connection connection;

    public NotaDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public boolean create(Nota nota) {
        String sql = "INSERT INTO Nota (id_pesanan, waktu_cetak, total_pembayaran, metode_pembayaran, status_pembayaran) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, nota.getIdPesanan());
            stmt.setTimestamp(2, nota.getWaktuCetak());
            stmt.setDouble(3, nota.getTotalPembayaran());
            stmt.setString(4, nota.getMetodePembayaran());
            stmt.setString(5, nota.getStatusPembayaran());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Nota findByOrderId(int idPesanan) {
        String sql = "SELECT * FROM Nota WHERE id_pesanan = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPesanan);
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
            e.printStackTrace();
        }
        return null;
    }

    public List<Nota> findAll() {
        List<Nota> notas = new ArrayList<>();
        String sql = "SELECT * FROM Nota ORDER BY waktu_cetak DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Nota nota = new Nota();
                nota.setIdNota(rs.getInt("id_nota"));
                nota.setIdPesanan(rs.getInt("id_pesanan"));
                nota.setWaktuCetak(rs.getTimestamp("waktu_cetak"));
                nota.setTotalPembayaran(rs.getDouble("total_pembayaran"));
                nota.setMetodePembayaran(rs.getString("metode_pembayaran"));
                nota.setStatusPembayaran(rs.getString("status_pembayaran"));
                notas.add(nota);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notas;
    }

    public boolean delete(int idNota) {
        String sql = "DELETE FROM Nota WHERE id_nota = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idNota);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
