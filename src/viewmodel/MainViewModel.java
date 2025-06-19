import javax.swing.JOptionPane; // Digunakan untuk menampilkan dialog pesan.
import java.util.List; // Digunakan untuk bekerja dengan koleksi List.

/**
 * MainViewModel adalah kelas ViewModel yang bertanggung jawab untuk mengelola
 * logika bisnis dan interaksi data untuk tampilan menu utama (MainFrame).
 * Kelas ini bertindak sebagai perantara antara MainFrame (View) dan DatabaseModel (Model).
 */
public class MainViewModel {
    private MainFrame mainFrame; // Referensi ke MainFrame (View) untuk memanipulasi UI.
    private DatabaseModel databaseModel; // Referensi ke DatabaseModel untuk interaksi dengan data (skor).
    private String currentUsername; // Menyimpan username pemain yang sedang aktif atau terakhir dimasukkan.

    /**
     * Konstruktor untuk MainViewModel.
     * Menginisialisasi referensi ke MainFrame dan DatabaseModel.
     * @param mainFrame Referensi ke MainFrame yang terkait dengan ViewModel ini.
     */
    public MainViewModel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.databaseModel = new DatabaseModel(); // Inisialisasi DatabaseModel.
    }

    /**
     * Memuat daftar high score dari database dan memperbarui tampilan tabel di MainFrame.
     */
    public void loadScores() {
        List<Thasil> results = databaseModel.getAllThasil(); // Mengambil semua hasil (skor) dari database.
        mainFrame.updateScoreTable(results); // Memanggil metode di MainFrame untuk memperbarui tabel skor dengan data yang didapat.
    }

    /**
     * Memulai permainan baru dengan username yang diberikan.
     * Melakukan validasi username dan jika valid, beralih ke panel game.
     * @param username Username yang dimasukkan oleh pemain.
     */
    public void startGame(String username) {
        // Validasi: Memeriksa apakah username null atau kosong setelah di-trim (menghapus spasi di awal/akhir).
        if (username == null || username.trim().isEmpty()) {
            // Jika username kosong, tampilkan pesan peringatan kepada pengguna.
            mainFrame.showMessage("Input Error", "Username cannot be empty!", JOptionPane.WARNING_MESSAGE);
            return; // Menghentikan eksekusi metode.
        }
        this.currentUsername = username.trim(); // Menyimpan username yang sudah di-trim sebagai username saat ini.
        
        mainFrame.switchToGamePanel(); // Memanggil metode di MainFrame untuk beralih ke panel game.
    }

    /**
     * Mengembalikan username pemain yang sedang aktif saat ini.
     * @return Username pemain saat ini.
     */
    public String getCurrentUsername() {
        return currentUsername;
    }

    /**
     * Menampilkan dialog konfirmasi untuk keluar dari aplikasi.
     * Jika pengguna memilih "Ya", aplikasi akan ditutup.
     */
    public void exitApplication() {
        // Menampilkan dialog konfirmasi dengan opsi "Ya" dan "Tidak".
        int confirm = JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to quit?", "Quit Game", JOptionPane.YES_NO_OPTION);
        // Memeriksa pilihan pengguna.
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0); // Menghentikan aplikasi Java.
        }
    }
}