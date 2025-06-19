import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent; // Tambahkan import ini - Penting untuk ActionListener.
import java.awt.event.ActionListener; // Tambahkan import ini - Interface untuk menangani event aksi (tombol, timer, dll).
import java.awt.event.KeyEvent; // Tambahkan import ini - Kelas untuk event keyboard.
import java.util.List;
import javax.sound.sampled.Clip;

/**
 * MainFrame adalah kelas utama aplikasi GUI yang mengatur tampilan jendela utama,
 * termasuk layar menu, input username, tabel high score, dan navigasi ke panel game.
 * Kelas ini bertindak sebagai View dalam arsitektur MVVM, berinteraksi dengan MainViewModel.
 */
public class MainFrame extends JFrame {
    private MainViewModel mainViewModel; // ViewModel yang mengelola logika bisnis untuk MainFrame.
    private JTextField usernameField; // Bidang teks untuk input username pemain.
    private JButton playButton; // Tombol untuk memulai permainan.
    private JButton quitButton; // Tombol untuk keluar dari aplikasi.
    private JTable scoreTable; // Tabel untuk menampilkan high scores.
    private DefaultTableModel tableModel; // Model data untuk JTable high score.

    private BackgroundPanel backgroundPanel; // Panel kustom untuk menggambar latar belakang gambar.

    private GamePanel gamePanel; // Referensi ke panel game saat game sedang dimainkan.

    /**
     * Konstruktor MainFrame. Menginisialisasi jendela utama, komponen UI menu,
     * memuat aset, dan mengatur listener global.
     */
    public MainFrame() {
        setTitle("ASTRO-COLLECTORS"); // Mengatur judul jendela.
        setSize(1200, 700); // Mengatur ukuran jendela.
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Mencegah jendela langsung tertutup saat tombol X diklik, agar bisa ditangani sendiri.
        setLocationRelativeTo(null); // Menempatkan jendela di tengah layar.

        mainViewModel = new MainViewModel(this); // Inisialisasi ViewModel, meneruskan referensi MainFrame ini.

        backgroundPanel = new BackgroundPanel(); // Membuat instance BackgroundPanel untuk latar belakang kustom.
        backgroundPanel.setLayout(new BorderLayout()); // Mengatur layout manager untuk backgroundPanel.
        setContentPane(backgroundPanel); // Mengatur backgroundPanel sebagai content pane utama frame.

        setupMainScreenUI(); // Memanggil metode untuk membangun dan menambahkan komponen UI menu utama.

        AssetLoader.loadAssets(); // Memuat semua aset (gambar, suara) yang dibutuhkan aplikasi.
        playMainMenuMusic(); // Memutar musik latar belakang menu utama.

        // Listener untuk tombol X (Close Window) pada jendela.
        // Memanggil logika konfirmasi keluar dari ViewModel.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainViewModel.exitApplication(); // Memanggil logika konfirmasi keluar.
            }
        });

        // *** TAMBAHAN: Global Key Listener untuk tombol Esc (Exit) ***
        // Ini memungkinkan tombol Esc untuk memicu aksi keluar dari aplikasi dari mana saja dalam frame.
        setupGlobalKeyListeners();
        // --- Akhir Tambahan ---
    }

    /**
     * Mengatur komponen UI untuk layar menu utama.
     * Menggunakan berbagai JPanel dan JComponent untuk menyusun tata letak.
     */
    private void setupMainScreenUI() {
        // Definisi warna-warna yang digunakan di UI.
        Color primaryColor = new Color(70, 130, 180); // Biru baja (steel blue).
        Color accentColor = new Color(60, 179, 113); // Hijau laut (sea green).
        Color dangerColor = new Color(220, 20, 60); // Merah tua (crimson).
        Color textColorLight = Color.WHITE; // Warna teks terang.
        Color textColorDark = Color.BLACK; // Warna teks gelap.
        Color translucentWhite = new Color(255, 255, 255, 150); // Putih transparan.
        Color translucentBlack = new Color(0, 0, 0, 100); // Hitam transparan.

        // Panel untuk judul game.
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false); // Membuat panel transparan agar gambar latar belakang terlihat.
        JLabel titleLabel = new JLabel("ASTRO-COLLECTORS"); // Label judul.
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36)); // Mengatur font judul.
        titleLabel.setForeground(textColorLight); // Mengatur warna teks judul.
        titlePanel.setBorder(BorderFactory.createEmptyBorder(70, 0, 15, 0)); // Menambahkan padding atas.
        titlePanel.add(titleLabel); // Menambahkan label ke panel judul.
        backgroundPanel.add(titlePanel, BorderLayout.NORTH); // Menambahkan panel judul ke bagian atas backgroundPanel.

        // Container pusat untuk username input, tombol play, dan high score table.
        JPanel centralContainerPanel = new JPanel();
        centralContainerPanel.setOpaque(false); // Membuat panel transparan.
        centralContainerPanel.setLayout(new BoxLayout(centralContainerPanel, BoxLayout.Y_AXIS)); // Menggunakan BoxLayout vertikal.
        centralContainerPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0)); // Menambahkan padding.

        // Panel untuk input username dan tombol Play.
        JPanel usernamePlayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); // Menggunakan FlowLayout untuk menengahkan.
        usernamePlayPanel.setOpaque(false); // Membuat panel transparan.

        JLabel usernameLabel = new JLabel("Username:"); // Label "Username:".
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 18)); // Mengatur font label.
        usernameLabel.setForeground(textColorLight); // Mengatur warna teks label.
        usernamePlayPanel.add(usernameLabel); // Menambahkan label.

        usernameField = new JTextField(15); // Bidang teks untuk username dengan lebar 15 kolom.
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16)); // Mengatur font bidang teks.
        usernameField.setBackground(translucentWhite); // Mengatur warna latar belakang transparan.
        usernameField.setForeground(textColorDark); // Mengatur warna teks.
        usernamePlayPanel.add(usernameField); // Menambahkan bidang teks.

        playButton = new JButton("Play"); // Tombol Play.
        playButton.setFont(new Font("Arial", Font.BOLD, 18)); // Mengatur font tombol.
        playButton.setBackground(accentColor); // Mengatur warna latar belakang tombol.
        playButton.setForeground(textColorLight); // Mengatur warna teks tombol.
        playButton.setFocusPainted(false); // Menghilangkan efek fokus saat tombol diklik.
        playButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20)); // Menambahkan padding tombol.
        usernamePlayPanel.add(playButton); // Menambahkan tombol.

        centralContainerPanel.add(usernamePlayPanel); // Menambahkan panel username/play ke container utama.
        centralContainerPanel.add(Box.createVerticalStrut(20)); // Menambahkan spasi vertikal.

        // Label "High Scores:".
        JLabel highScoresLabel = new JLabel("High Scores:");
        highScoresLabel.setFont(new Font("Arial", Font.BOLD, 26)); // Mengatur font.
        highScoresLabel.setForeground(textColorLight); // Mengatur warna teks.
        highScoresLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Menengahkan label secara horizontal.
        centralContainerPanel.add(highScoresLabel); // Menambahkan label.
        centralContainerPanel.add(Box.createVerticalStrut(10)); // Menambahkan spasi vertikal.

        // Model tabel untuk JTable high score.
        tableModel = new DefaultTableModel(new Object[]{"Username", "Score", "Count"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Membuat semua sel tabel tidak dapat diedit.
            }
        };
        scoreTable = new JTable(tableModel); // Membuat JTable dengan model yang sudah didefinisikan.
        scoreTable.setFont(new Font("Arial", Font.PLAIN, 15)); // Mengatur font sel tabel.
        scoreTable.setRowHeight(25); // Mengatur tinggi baris tabel.
        scoreTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15)); // Mengatur font header tabel.
        scoreTable.getTableHeader().setBackground(new Color(100, 100, 100, 180)); // Mengatur warna latar belakang header transparan.
        scoreTable.getTableHeader().setForeground(textColorLight); // Mengatur warna teks header.
        scoreTable.setFillsViewportHeight(true); // Memastikan tabel mengisi tinggi viewport.

        scoreTable.setOpaque(false); // Membuat tabel transparan.
        scoreTable.setBackground(new Color(0,0,0,0)); // Mengatur warna latar belakang tabel menjadi sepenuhnya transparan.
        scoreTable.setForeground(textColorLight); // Mengatur warna teks sel tabel.

        // Renderer sel kustom untuk mengatur tampilan sel tabel (termasuk transparansi dan warna).
        scoreTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(translucentBlack); // Latar belakang sel hitam transparan.
                c.setForeground(textColorLight); // Warna teks sel terang.
                if (isSelected) {
                    c.setBackground(new Color(70, 130, 180, 150)); // Warna latar belakang saat sel terpilih.
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(scoreTable); // Membuat JScrollPane untuk tabel (agar bisa discroll).
        scrollPane.setOpaque(false); // Membuat scroll pane transparan.
        scrollPane.getViewport().setOpaque(false); // Membuat viewport scroll pane transparan.
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180, 200), 2)); // Menambahkan border biru transparan.
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT); // Menengahkan scroll pane secara horizontal.
        scrollPane.setMaximumSize(new Dimension(480, 180)); // Mengatur ukuran maksimum scroll pane.

        centralContainerPanel.add(scrollPane); // Menambahkan scroll pane ke container utama.

        backgroundPanel.add(centralContainerPanel, BorderLayout.CENTER); // Menambahkan container pusat ke bagian tengah backgroundPanel.

        // Panel untuk tombol Quit.
        JPanel quitButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Menggunakan FlowLayout untuk menengahkan.
        quitButtonPanel.setOpaque(false); // Membuat panel transparan.
        quitButtonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 20, 0)); // Menambahkan padding bawah.

        quitButton = new JButton("Quit"); // Tombol Quit.
        quitButton.setFont(new Font("Arial", Font.BOLD, 18)); // Mengatur font.
        quitButton.setBackground(dangerColor); // Mengatur warna latar belakang (merah).
        quitButton.setForeground(textColorLight); // Mengatur warna teks.
        quitButton.setFocusPainted(false); // Menghilangkan efek fokus.
        quitButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20)); // Menambahkan padding.
        quitButtonPanel.add(quitButton); // Menambahkan tombol.
        backgroundPanel.add(quitButtonPanel, BorderLayout.SOUTH); // Menambahkan panel tombol quit ke bagian bawah backgroundPanel.

        // --- Tambahkan Listener untuk Tombol ---
        // Listener untuk tombol Play: memanggil startGame di ViewModel dengan username dari field.
        playButton.addActionListener(e -> mainViewModel.startGame(usernameField.getText()));
        // Listener untuk tombol Quit: memanggil exitApplication di ViewModel.
        quitButton.addActionListener(e -> mainViewModel.exitApplication());

        // *** TAMBAHAN: Listener untuk tombol Enter di usernameField ***
        // Memungkinkan pemain untuk menekan Enter di usernameField untuk memulai game.
        usernameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Ketika Enter ditekan di usernameField, picu aksi playButton (seolah-olah tombol Play diklik).
                playButton.doClick();
            }
        });
        // --- Akhir Tambahan (Enter) ---

        mainViewModel.loadScores(); // Memuat high scores saat UI menu utama diatur.
    }

    /**
     * --- TAMBAHAN: Metode untuk Setup Global Key Listeners (Esc) ---
     * Mengatur key listener global menggunakan InputMap dan ActionMap dari JRootPane.
     * Ini memungkinkan tombol ESCAPE untuk dideteksi dari mana saja di dalam jendela
     * (selama jendela fokus) dan memicu aksi keluar aplikasi.
     */
    private void setupGlobalKeyListeners() {
        // Mengambil InputMap untuk jendela yang difokuskan.
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        // Mengambil ActionMap untuk jendela.
        ActionMap actionMap = getRootPane().getActionMap();

        // Mengikat (bind) tombol ESCAPE (tanpa modifier) ke string aksi "exitAction".
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exitAction");
        // Mendefinisikan aksi yang akan dijalankan ketika "exitAction" dipicu.
        actionMap.put("exitAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainViewModel.exitApplication(); // Panggil logika keluar aplikasi dari ViewModel.
            }
        });
    }
    // --- Akhir Tambahan (Esc) ---

    /**
     * Memperbarui data yang ditampilkan di tabel high score.
     * Membersihkan tabel dan mengisi ulang dengan data dari list hasil yang diberikan.
     * @param results List objek Thasil yang berisi data high score.
     */
    public void updateScoreTable(List<Thasil> results) {
        tableModel.setRowCount(0); // Menghapus semua baris yang ada di tabel.
        for (Thasil thasil : results) {
            // Menambahkan baris baru ke tabel dengan data username, skor, dan count.
            tableModel.addRow(new Object[]{thasil.getUsername(), thasil.getSkor(), thasil.getCount()});
        }
    }

    /**
     * Mengalihkan tampilan dari Main Menu ke panel Game.
     * Menghentikan musik menu, membuat instance GamePanel (jika belum ada),
     * mengganti content pane, dan memulai logika game.
     */
    public void switchToGamePanel() {
        stopMainMenuMusic(); // Menghentikan musik menu utama.
        if (gamePanel == null) {
            // Membuat instance GamePanel baru jika belum ada.
            gamePanel = new GamePanel(mainViewModel.getCurrentUsername(), this);
        }
        getContentPane().removeAll(); // Menghapus semua komponen dari content pane saat ini.
        setContentPane(gamePanel); // Mengatur GamePanel sebagai content pane yang baru.
        gamePanel.requestFocusInWindow(); // Memastikan GamePanel mendapatkan fokus keyboard.
        revalidate(); // Memvalidasi ulang layout komponen.
        repaint(); // Meminta komponen untuk digambar ulang.
        gamePanel.startGameLogic(); // Memulai logika game di GamePanel.
    }

    /**
     * Mengalihkan tampilan dari panel Game kembali ke Main Menu.
     * Memulai musik menu utama, membersihkan content pane, dan membangun kembali UI menu utama.
     */
    public void switchToMainPanel() {
        playMainMenuMusic(); // Memulai musik menu utama.
        getContentPane().removeAll(); // Menghapus semua komponen dari content pane saat ini.

        // Penting: Inisialisasi ulang backgroundPanel dan panggil setupMainScreenUI.
        // Ini diperlukan karena `removeAll()` menghapus backgroundPanel dari hierarki,
        // sehingga harus dibuat dan diatur ulang.
        backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        setupMainScreenUI(); // Memanggil ini untuk membangun kembali UI menu utama di backgroundPanel yang baru.

        revalidate(); // Memvalidasi ulang layout komponen.
        repaint(); // Meminta komponen untuk digambar ulang.
        mainViewModel.loadScores(); // Memuat ulang high scores untuk ditampilkan di tabel.
        usernameField.setText(""); // Mengosongkan bidang username.
        // Fokuskan kembali usernameField setelah kembali ke menu utama.
        usernameField.requestFocusInWindow();
    }

    /**
     * Menampilkan pesan dialog pop-up kepada pengguna.
     * @param title Judul dialog.
     * @param message Pesan yang akan ditampilkan.
     * @param messageType Tipe pesan (misalnya JOptionPane.INFORMATION_MESSAGE).
     */
    public void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    /**
     * Memutar musik latar belakang menu utama secara berulang.
     * Jika musik sudah berjalan, akan dihentikan dan dimulai ulang dari awal.
     * Juga mengatur volume musik.
     */
    private void playMainMenuMusic() {
        if (AssetLoader.mainMenuMusicClip != null) {
            if (AssetLoader.mainMenuMusicClip.isRunning()) {
                AssetLoader.mainMenuMusicClip.stop(); // Hentikan jika sedang berjalan.
            }
            AssetLoader.mainMenuMusicClip.setFramePosition(0); // Set posisi ke awal.
            AssetLoader.mainMenuMusicClip.loop(Clip.LOOP_CONTINUOUSLY); // Putar secara berulang.

            if (AssetLoader.mainMenuMusicGainControl != null) {
                // Mengatur volume musik menu utama ke 5% (0.05f).
                AssetLoader.setClipVolume(AssetLoader.mainMenuMusicGainControl, 0.05f);
            } else {
                System.err.println("Main menu music gain control not available. Cannot set fixed volume.");
            }
        }
    }

    /**
     * Menghentikan musik latar belakang menu utama jika sedang berjalan.
     */
    private void stopMainMenuMusic() {
        if (AssetLoader.mainMenuMusicClip != null && AssetLoader.mainMenuMusicClip.isRunning()) {
            AssetLoader.mainMenuMusicClip.stop();
        }
    }

    /**
     * Kelas inner statis BackgroundPanel adalah JPanel kustom yang bertanggung jawab
     * untuk menggambar gambar latar belakang menu utama.
     */
    private static class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Memanggil implementasi superclass untuk rendering dasar.
            if (AssetLoader.mainMenuBackgroundImage != null) {
                // Menggambar gambar latar belakang, diskalakan agar sesuai dengan ukuran panel.
                g.drawImage(AssetLoader.mainMenuBackgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}