import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class MainFrame extends JFrame {
    private MainViewModel mainViewModel;
    private JTextField usernameField;
    private JButton playButton;
    private JButton quitButton;
    private JTable scoreTable;
    private DefaultTableModel tableModel;
    private JPanel mainPanel;
    private GamePanel gamePanel;

    public MainFrame() {
        setTitle("Collect The Skill Balls");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Handle closing manually
        setLocationRelativeTo(null); // Center the frame

        mainViewModel = new MainViewModel(this);
        setupMainPanel();
        add(mainPanel); // Add main panel initially

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainViewModel.exitApplication();
            }
        });

        // Initialize AssetLoader
        AssetLoader.loadAssets();
    }

     private void setupMainPanel() {
        // --- Warna Dasar ---
        Color primaryColor = new Color(70, 130, 180);   // Steel Blue
        Color secondaryColor = new Color(240, 240, 240); // Light Grey
        Color accentColor = new Color(60, 179, 113);    // Medium Sea Green
        Color dangerColor = new Color(220, 20, 60);     // Crimson
        Color textColorDark = Color.BLACK;
        Color textColorLight = Color.WHITE;

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(secondaryColor); // Latar belakang utama abu-abu terang

        // --- Bagian Atas (Title) ---
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(primaryColor); // Latar belakang biru tua
        JLabel titleLabel = new JLabel("COLLECT THE SKILL BALLS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36)); // Font lebih besar
        titleLabel.setForeground(textColorLight); // Teks putih
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0)); // Padding atas bawah
        titlePanel.add(titleLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // --- Bagian Tengah (Username, Play, High Scores, Table) ---
        JPanel centralContainerPanel = new JPanel();
        centralContainerPanel.setBackground(secondaryColor); // Latar belakang abu-abu terang
        centralContainerPanel.setLayout(new BoxLayout(centralContainerPanel, BoxLayout.Y_AXIS));
        centralContainerPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0)); // Padding atas bawah untuk container

        // Panel Username dan Play Button
        JPanel usernamePlayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); // Jarak antar komponen
        usernamePlayPanel.setBackground(new Color(220, 220, 220)); // Latar belakang abu-abu sedang
        usernamePlayPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)); // Border tipis
        
        JLabel usernameLabel = new JLabel("Username:"); // Tambah ":" agar konsisten
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        usernameLabel.setForeground(textColorDark); // Teks hitam
        usernamePlayPanel.add(usernameLabel);

        usernameField = new JTextField(15); 
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameField.setBackground(Color.WHITE); // Latar belakang field putih
        usernameField.setForeground(textColorDark); // Teks field hitam
        usernamePlayPanel.add(usernameField);

        playButton = new JButton("Play");
        playButton.setFont(new Font("Arial", Font.BOLD, 18)); 
        playButton.setBackground(accentColor); // Warna hijau aksen
        playButton.setForeground(textColorLight); // Teks putih
        playButton.setFocusPainted(false);
        playButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20)); // Padding tombol
        usernamePlayPanel.add(playButton);
        
        centralContainerPanel.add(usernamePlayPanel);
        centralContainerPanel.add(Box.createVerticalStrut(25)); // Spasi vertikal lebih besar

        // Label High Scores
        JLabel highScoresLabel = new JLabel("High Scores:");
        highScoresLabel.setFont(new Font("Arial", Font.BOLD, 26)); // Font lebih besar
        highScoresLabel.setForeground(primaryColor.darker()); // Warna biru tua gelap
        highScoresLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centralContainerPanel.add(highScoresLabel);
        centralContainerPanel.add(Box.createVerticalStrut(15)); // Spasi vertikal

        // Tabel Skor
        tableModel = new DefaultTableModel(new Object[]{"Username", "Score", "Count"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        scoreTable = new JTable(tableModel);
        scoreTable.setFont(new Font("Arial", Font.PLAIN, 15)); // Font sel tabel
        scoreTable.setRowHeight(25); // Tinggi baris
        scoreTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15)); // Font header tabel
        scoreTable.getTableHeader().setBackground(new Color(180, 180, 180)); // Warna header abu-abu sedang
        scoreTable.getTableHeader().setForeground(textColorDark); // Teks header hitam
        scoreTable.setFillsViewportHeight(true); 
        scoreTable.setBackground(Color.WHITE); // Latar belakang sel putih
        scoreTable.setForeground(textColorDark); // Teks sel hitam
        scoreTable.setGridColor(Color.LIGHT_GRAY); // Warna grid tabel
        scoreTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()); // Renderer default

        JScrollPane scrollPane = new JScrollPane(scoreTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(primaryColor, 2)); // Border lebih tebal dengan warna primer
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollPane.setMaximumSize(new Dimension(480, 180)); // Batasi ukuran tabel sedikit lebih besar

        centralContainerPanel.add(scrollPane);

        mainPanel.add(centralContainerPanel, BorderLayout.CENTER);

        // --- Bagian Bawah (Quit Button) ---
        JPanel quitButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        quitButtonPanel.setBackground(secondaryColor); // Latar belakang abu-abu terang
        quitButtonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0)); // Padding atas bawah
        
        quitButton = new JButton("Quit");
        quitButton.setFont(new Font("Arial", Font.BOLD, 18));
        quitButton.setBackground(dangerColor); // Warna merah bahaya
        quitButton.setForeground(textColorLight); // Teks putih
        quitButton.setFocusPainted(false);
        quitButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20)); // Padding tombol
        quitButtonPanel.add(quitButton);
        mainPanel.add(quitButtonPanel, BorderLayout.SOUTH);

        // --- Tambahkan Listener ---
        playButton.addActionListener(e -> mainViewModel.startGame(usernameField.getText()));
        quitButton.addActionListener(e -> mainViewModel.exitApplication());

        mainViewModel.loadScores(); // Muat skor awal
    }

    public void updateScoreTable(List<Thasil> results) {
        tableModel.setRowCount(0); // Clear existing data
        for (Thasil thasil : results) {
            tableModel.addRow(new Object[]{thasil.getUsername(), thasil.getSkor(), thasil.getCount()});
        }
    }

    public void switchToGamePanel() {
        if (gamePanel == null) {
            gamePanel = new GamePanel(mainViewModel.getCurrentUsername(), this); // Pass username and MainFrame reference
        }
        getContentPane().removeAll();
        add(gamePanel);
        gamePanel.requestFocusInWindow(); // Ensure GamePanel has focus for key events
        revalidate();
        repaint();
        gamePanel.startGameLogic(); // Start game logic
    }

    public void switchToMainPanel() {
        getContentPane().removeAll();
        add(mainPanel);
        revalidate();
        repaint();
        mainViewModel.loadScores(); // Reload scores when returning to main panel
        usernameField.setText(""); // Clear username field
    }

    public void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
}