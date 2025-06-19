import javax.swing.SwingUtilities; // Import kelas SwingUtilities untuk manajemen Event Dispatch Thread (EDT).

/**
 * Kelas Main adalah titik masuk utama (entry point) untuk aplikasi game.
 * Kelas ini bertanggung jawab untuk menginisialisasi dan menjalankan aplikasi
 * Swing dengan memastikan bahwa semua operasi UI dilakukan di Event Dispatch Thread (EDT).
 */
public class Main {
    /**
     * Metode main adalah metode yang pertama kali dieksekusi saat program dimulai.
     * @param args Argumen baris perintah (tidak digunakan dalam aplikasi ini).
     */
    public static void main(String[] args) {
        // Jalankan aplikasi di Event Dispatch Thread (EDT).
        // SwingUtilities.invokeLater() memastikan bahwa kode di dalamnya dieksekusi
        // pada thread khusus yang bertanggung jawab untuk menangani semua event UI Swing.
        // Ini adalah praktik terbaik untuk menghindari masalah threading dalam aplikasi Swing.
        SwingUtilities.invokeLater(() -> {
            // Membuat instance dari MainFrame, yang merupakan jendela utama aplikasi.
            MainFrame mainFrame = new MainFrame();
            // Menjadikan jendela utama terlihat oleh pengguna.
            mainFrame.setVisible(true);
        });
    }
}