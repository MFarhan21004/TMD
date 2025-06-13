import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer; // Import for custom table cell rendering
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.sound.sampled.Clip; // Pastikan Clip diimpor
import javax.sound.sampled.FloatControl; 
import javax.swing.plaf.basic.BasicSliderUI; // Untuk custom slider UI (meskipun slider dihapus, import mungkin ada di kode lama)


public class MainFrame extends JFrame {
    private MainViewModel mainViewModel;
    private JTextField usernameField;
    private JButton playButton;
    private JButton quitButton;
    private JTable scoreTable;
    private DefaultTableModel tableModel;
    
    // Background panel untuk menggambar gambar latar belakang menu utama
    private BackgroundPanel backgroundPanel; 

    private GamePanel gamePanel; 

    public MainFrame() {
        setTitle("Collect The Skill Balls");
        // Ukuran frame bisa disesuaikan di sini
        setSize(1200, 700); // Ukuran yang Anda set sebelumnya
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // <<< Ini yang membuat tombol X tidak langsung keluar
        setLocationRelativeTo(null); 

        mainViewModel = new MainViewModel(this);
        
        // Inisialisasi backgroundPanel dan set layout-nya
        backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout()); 
        setContentPane(backgroundPanel); // Mengatur backgroundPanel sebagai content pane utama JFrame

        setupMainScreenUI(); // Metode ini akan menambahkan komponen ke backgroundPanel
        
        // Inisialisasi AssetLoader (memuat aset termasuk musik menu utama)
        AssetLoader.loadAssets(); 
        playMainMenuMusic(); // Ini yang memutar musik menu utama saat aplikasi dimulai
        
        // Listener untuk tombol X (Close Window)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainViewModel.exitApplication(); // Memanggil logika konfirmasi keluar
            }
        });
    }

    private void setupMainScreenUI() { // Mengganti nama metode dari setupMainPanel()
        // --- Warna Dasar --- (disesuaikan untuk background gambar)
        Color primaryColor = new Color(70, 130, 180);   // Steel Blue
        Color accentColor = new Color(60, 179, 113);    // Medium Sea Green
        Color dangerColor = new Color(220, 20, 60);     // Crimson
        Color textColorLight = Color.WHITE; // Teks putih untuk kontras di background gelap
        Color textColorDark = Color.BLACK; // Teks hitam untuk field username
        Color translucentWhite = new Color(255, 255, 255, 150); // Putih semi-transparan
        Color translucentBlack = new Color(0, 0, 0, 100);     // Hitam semi-transparan

        // backgroundPanel sudah diset sebagai content pane, backgroundnya akan digambar olehnya sendiri
        
        // --- Bagian Atas (Title) ---
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false); // Transparan agar gambar background terlihat
        JLabel titleLabel = new JLabel("COLLECT THE SKILL BALLS"); 
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36)); // Ukuran font sesuai screenshot gelap
        titleLabel.setForeground(textColorLight); // Teks putih
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0)); 
        titlePanel.add(titleLabel);
        backgroundPanel.add(titlePanel, BorderLayout.NORTH);

        // --- Bagian Tengah (Username, Play, High Scores, Table) ---
        JPanel centralContainerPanel = new JPanel(); 
        centralContainerPanel.setOpaque(false); // Transparan agar gambar background terlihat
        centralContainerPanel.setLayout(new BoxLayout(centralContainerPanel, BoxLayout.Y_AXIS)); 
        centralContainerPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0)); 

        // Panel Username dan Play Button
        JPanel usernamePlayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); 
        usernamePlayPanel.setOpaque(false); // Transparan
        
        JLabel usernameLabel = new JLabel("Username:"); 
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        usernameLabel.setForeground(textColorLight); // Teks putih
        usernamePlayPanel.add(usernameLabel);

        usernameField = new JTextField(15); 
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameField.setBackground(translucentWhite); // Semi-transparan putih
        usernameField.setForeground(textColorDark); // Teks hitam
        usernamePlayPanel.add(usernameField);

        playButton = new JButton("Play");
        playButton.setFont(new Font("Arial", Font.BOLD, 18)); 
        playButton.setBackground(accentColor); 
        playButton.setForeground(textColorLight);
        playButton.setFocusPainted(false);
        playButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20)); 
        usernamePlayPanel.add(playButton);
        
        centralContainerPanel.add(usernamePlayPanel);
        centralContainerPanel.add(Box.createVerticalStrut(20)); 

        // Label High Scores
        JLabel highScoresLabel = new JLabel("High Scores:");
        highScoresLabel.setFont(new Font("Arial", Font.BOLD, 26)); 
        highScoresLabel.setForeground(textColorLight); // Teks putih
        highScoresLabel.setAlignmentX(Component.CENTER_ALIGNMENT); 
        centralContainerPanel.add(highScoresLabel);
        centralContainerPanel.add(Box.createVerticalStrut(10)); 

        // Tabel Skor
        tableModel = new DefaultTableModel(new Object[]{"Username", "Score", "Count"}, 0) { 
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        scoreTable = new JTable(tableModel);
        scoreTable.setFont(new Font("Arial", Font.PLAIN, 15)); 
        scoreTable.setRowHeight(25); 
        scoreTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15)); 
        scoreTable.getTableHeader().setBackground(new Color(100, 100, 100, 180)); // Semi-transparan gelap
        scoreTable.getTableHeader().setForeground(textColorLight); // Teks putih
        scoreTable.setFillsViewportHeight(true); 
        
        scoreTable.setOpaque(false); // Transparan penuh
        scoreTable.setBackground(new Color(0,0,0,0)); // Transparan penuh
        scoreTable.setForeground(textColorLight); // Teks sel putih

        // Custom renderer untuk sel tabel agar background semi-transparan
        scoreTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(translucentBlack); // Latar belakang sel semi-transparan hitam
                c.setForeground(textColorLight); // Teks sel putih
                if (isSelected) {
                    c.setBackground(new Color(70, 130, 180, 150)); // Steel Blue semi-transparan saat terpilih
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(scoreTable);
        scrollPane.setOpaque(false); // Scroll pane transparan
        scrollPane.getViewport().setOpaque(false); // Viewport scroll pane transparan
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180, 200), 2)); // Border warna primaryColor semi-transparan
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT); 
        scrollPane.setMaximumSize(new Dimension(480, 180)); 

        centralContainerPanel.add(scrollPane);

        // HAPUS: Volume Control Slider dan Label Volume (sesuai permintaan)
        // JLabel volumeLabel = new JLabel("Music Volume:"); ...
        // volumeSlider = new JSlider(...); ...
        // centralContainerPanel.add(volumeLabel); ...
        // centralContainerPanel.add(volumeSlider); ...


        backgroundPanel.add(centralContainerPanel, BorderLayout.CENTER);

        // --- Bagian Bawah (Quit Button) ---
        JPanel quitButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); 
        quitButtonPanel.setOpaque(false); // Transparan
        quitButtonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 20, 0)); 
        
        quitButton = new JButton("Quit");
        quitButton.setFont(new Font("Arial", Font.BOLD, 18));
        quitButton.setBackground(dangerColor); 
        quitButton.setForeground(textColorLight);
        quitButton.setFocusPainted(false);
        quitButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20)); 
        quitButtonPanel.add(quitButton);
        backgroundPanel.add(quitButtonPanel, BorderLayout.SOUTH);

        // --- Tambahkan Listener ---
        playButton.addActionListener(e -> mainViewModel.startGame(usernameField.getText()));
        quitButton.addActionListener(e -> mainViewModel.exitApplication());

        // HAPUS: Listener volumeSlider (sesuai permintaan)
        // volumeSlider.addChangeListener(...);

        mainViewModel.loadScores(); 
        
        // HAPUS: Panggilan setGlobalMusicVolume() di sini (volume fixed sekarang)
        // setGlobalMusicVolume(volumeSlider.getValue() / 100.0f);
    }

    public void updateScoreTable(List<Thasil> results) {
        tableModel.setRowCount(0);
        for (Thasil thasil : results) {
            tableModel.addRow(new Object[]{thasil.getUsername(), thasil.getSkor(), thasil.getCount()});
        }
    }

    public void switchToGamePanel() {
        stopMainMenuMusic(); 
        if (gamePanel == null) { 
            gamePanel = new GamePanel(mainViewModel.getCurrentUsername(), this); 
        }
        getContentPane().removeAll(); 
        setContentPane(gamePanel); 
        gamePanel.requestFocusInWindow();
        revalidate();
        repaint();
        gamePanel.startGameLogic();
    }

    public void switchToMainPanel() {
        playMainMenuMusic(); 
        getContentPane().removeAll(); 
        setContentPane(backgroundPanel); 
        revalidate();
        repaint();
        mainViewModel.loadScores();
        usernameField.setText("");
    }

    public void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    // Metode untuk mengontrol volume musik secara global (tetap ada karena GamePanel masih memanggilnya)
    private void setGlobalMusicVolume(float volume) {
        // Logika ini masih akan dipanggil oleh GamePanel.startGameLogic()
        // Pastikan volumenya juga diatur untuk musik game
        AssetLoader.setClipVolume(AssetLoader.mainMenuMusicGainControl, volume);
        AssetLoader.setClipVolume(AssetLoader.backgroundMusicGainControl, volume);
        AssetLoader.setClipVolume(AssetLoader.bonusMusicGainControl, volume);
        AssetLoader.setClipVolume(AssetLoader.bombEffectGainControl, volume);
    }

    // Metode untuk mengontrol musik menu utama
    private void playMainMenuMusic() {
        if (AssetLoader.mainMenuMusicClip != null) {
            if (AssetLoader.mainMenuMusicClip.isRunning()) {
                AssetLoader.mainMenuMusicClip.stop(); 
            }
            AssetLoader.mainMenuMusicClip.setFramePosition(0); 
            AssetLoader.mainMenuMusicClip.loop(Clip.LOOP_CONTINUOUSLY); 

            // Atur volume musik menu utama secara fixed di sini
            if (AssetLoader.mainMenuMusicGainControl != null) {
                AssetLoader.setClipVolume(AssetLoader.mainMenuMusicGainControl, 0.05f); // Volume fixed 0.05f (sangat pelan)
            } else {
                System.err.println("Main menu music gain control not available. Cannot set fixed volume.");
            }
        }
    }

    private void stopMainMenuMusic() {
        if (AssetLoader.mainMenuMusicClip != null && AssetLoader.mainMenuMusicClip.isRunning()) {
            AssetLoader.mainMenuMusicClip.stop(); 
        }
    }

    // Custom JPanel untuk menggambar gambar latar belakang menu utama
    private static class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); 
            if (AssetLoader.mainMenuBackgroundImage != null) { // Menggambar background khusus menu utama
                g.drawImage(AssetLoader.mainMenuBackgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}