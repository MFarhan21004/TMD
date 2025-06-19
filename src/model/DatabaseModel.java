import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseModel {
    // URL koneksi ke database MySQL, sesuaikan nama database dan parameter timezone
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/skillballs_db?useSSL=false&serverTimezone=UTC";
    
    // Username dan password untuk koneksi database
    private static final String USER = "root"; // Ganti jika Anda menggunakan username berbeda
    private static final String PASSWORD = ""; // Ganti jika Anda menggunakan password

    // Konstruktor untuk inisialisasi driver JDBC
    public DatabaseModel() {
        try {
            // Register JDBC driver (untuk versi JDBC baru, ini kadang tidak wajib)
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver registered!");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    // Mendapatkan koneksi ke database
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    // Mengambil semua data dari tabel 'thasil' dan mengurutkan berdasarkan skor (DESC) dan count (DESC)
    public List<Thasil> getAllThasil() {
        List<Thasil> results = new ArrayList<>();
        String sql = "SELECT username, skor, count FROM thasil ORDER BY skor DESC, count DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Iterasi hasil query dan masukkan ke dalam list
            while (rs.next()) {
                String username = rs.getString("username");
                int skor = rs.getInt("skor");
                int count = rs.getInt("count");
                results.add(new Thasil(username, skor, count));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching results: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    // Menyimpan atau mengupdate data skor ke tabel 'thasil'
    public void saveThasil(String username, int skor, int count) {
        // Cek apakah username sudah ada di database
        Thasil existingThasil = getThasilByUsername(username);

        String sql;
        if (existingThasil == null) {
            // Jika belum ada, lakukan INSERT
            sql = "INSERT INTO thasil (username, skor, count) VALUES (?, ?, ?)";
        } else {
            // Jika sudah ada, lakukan UPDATE hanya jika skor baru lebih tinggi,
            // atau jika skor sama tapi count lebih tinggi
            if (skor > existingThasil.getSkor() || (skor == existingThasil.getSkor() && count > existingThasil.getCount())) {
                sql = "UPDATE thasil SET skor = ?, count = ? WHERE username = ?";
            } else {
                System.out.println("Existing score/count for " + username + " is better or equal. No update needed.");
                return; // Tidak perlu update
            }
        }

        // Jalankan query INSERT atau UPDATE
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (existingThasil == null) {
                pstmt.setString(1, username);
                pstmt.setInt(2, skor);
                pstmt.setInt(3, count);
            } else {
                pstmt.setInt(1, skor);
                pstmt.setInt(2, count);
                pstmt.setString(3, username);
            }

            pstmt.executeUpdate();
            System.out.println("Thasil data saved/updated successfully for " + username);
        } catch (SQLException e) {
            System.err.println("Error saving/updating Thasil data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Mengambil data skor berdasarkan username
    public Thasil getThasilByUsername(String username) {
        String sql = "SELECT username, skor, count FROM thasil WHERE username = ?";
        Thasil result = null;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    result = new Thasil(rs.getString("username"), rs.getInt("skor"), rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching Thasil by username: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}
