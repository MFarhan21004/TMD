import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseModel {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/skillballs_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // Ganti dengan username MySQL Anda
    private static final String PASSWORD = ""; // Ganti dengan password MySQL Anda

    public DatabaseModel() {
        try {
            // Register JDBC driver (for newer JDBC versions, this is often not strictly necessary)
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver registered!");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    public List<Thasil> getAllThasil() {
        List<Thasil> results = new ArrayList<>();
        String sql = "SELECT username, skor, count FROM thasil ORDER BY skor DESC, count DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

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

    public void saveThasil(String username, int skor, int count) {
        // Check if username already exists
        Thasil existingThasil = getThasilByUsername(username);

        String sql;
        if (existingThasil == null) {
            // Insert new record if username doesn't exist
            sql = "INSERT INTO thasil (username, skor, count) VALUES (?, ?, ?)";
        } else {
            // Update existing record if username exists and new score is higher
            // Or if new score is equal, update if new count is higher
            if (skor > existingThasil.getSkor() || (skor == existingThasil.getSkor() && count > existingThasil.getCount())) {
                sql = "UPDATE thasil SET skor = ?, count = ? WHERE username = ?";
            } else {
                System.out.println("Existing score/count for " + username + " is better or equal. No update needed.");
                return; // No update needed
            }
        }

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

    // You might want to add other CRUD operations here if needed (e.g., delete)
}