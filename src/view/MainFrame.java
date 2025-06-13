import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent; // Tambahkan import ini
import java.awt.event.ActionListener; // Tambahkan import ini
import java.awt.event.KeyEvent; // Tambahkan import ini
import java.util.List;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.plaf.basic.BasicSliderUI;


public class MainFrame extends JFrame {
    private MainViewModel mainViewModel;
    private JTextField usernameField;
    private JButton playButton;
    private JButton quitButton;
    private JTable scoreTable;
    private DefaultTableModel tableModel;

    private BackgroundPanel backgroundPanel;

    private GamePanel gamePanel;

    public MainFrame() {
        setTitle("ASTRO-COLLECTORS");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        mainViewModel = new MainViewModel(this);

        backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        setupMainScreenUI(); // Metode ini akan menambahkan komponen ke backgroundPanel

        AssetLoader.loadAssets();
        playMainMenuMusic();

        // Listener untuk tombol X (Close Window)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainViewModel.exitApplication(); // Memanggil logika konfirmasi keluar
            }
        });

        // *** TAMBAHAN: Global Key Listener untuk Esc (Exit) ***
        setupGlobalKeyListeners();
        // --- Akhir Tambahan ---
    }

    private void setupMainScreenUI() {
        Color primaryColor = new Color(70, 130, 180);
        Color accentColor = new Color(60, 179, 113);
        Color dangerColor = new Color(220, 20, 60);
        Color textColorLight = Color.WHITE;
        Color textColorDark = Color.BLACK;
        Color translucentWhite = new Color(255, 255, 255, 150);
        Color translucentBlack = new Color(0, 0, 0, 100);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("ASTRO-COLLECTORS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(textColorLight);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(70, 0, 15, 0));
        titlePanel.add(titleLabel);
        backgroundPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel centralContainerPanel = new JPanel();
        centralContainerPanel.setOpaque(false);
        centralContainerPanel.setLayout(new BoxLayout(centralContainerPanel, BoxLayout.Y_AXIS));
        centralContainerPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        JPanel usernamePlayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        usernamePlayPanel.setOpaque(false);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        usernameLabel.setForeground(textColorLight);
        usernamePlayPanel.add(usernameLabel);

        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameField.setBackground(translucentWhite);
        usernameField.setForeground(textColorDark);
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

        JLabel highScoresLabel = new JLabel("High Scores:");
        highScoresLabel.setFont(new Font("Arial", Font.BOLD, 26));
        highScoresLabel.setForeground(textColorLight);
        highScoresLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centralContainerPanel.add(highScoresLabel);
        centralContainerPanel.add(Box.createVerticalStrut(10));

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
        scoreTable.getTableHeader().setBackground(new Color(100, 100, 100, 180));
        scoreTable.getTableHeader().setForeground(textColorLight);
        scoreTable.setFillsViewportHeight(true);

        scoreTable.setOpaque(false);
        scoreTable.setBackground(new Color(0,0,0,0));
        scoreTable.setForeground(textColorLight);

        scoreTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(translucentBlack);
                c.setForeground(textColorLight);
                if (isSelected) {
                    c.setBackground(new Color(70, 130, 180, 150));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(scoreTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180, 200), 2));
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollPane.setMaximumSize(new Dimension(480, 180));

        centralContainerPanel.add(scrollPane);

        backgroundPanel.add(centralContainerPanel, BorderLayout.CENTER);

        JPanel quitButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        quitButtonPanel.setOpaque(false);
        quitButtonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 20, 0));

        quitButton = new JButton("Quit");
        quitButton.setFont(new Font("Arial", Font.BOLD, 18));
        quitButton.setBackground(dangerColor);
        quitButton.setForeground(textColorLight);
        quitButton.setFocusPainted(false);
        quitButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        quitButtonPanel.add(quitButton);
        backgroundPanel.add(quitButtonPanel, BorderLayout.SOUTH);

        // --- Tambahkan Listener untuk Tombol ---
        playButton.addActionListener(e -> mainViewModel.startGame(usernameField.getText()));
        quitButton.addActionListener(e -> mainViewModel.exitApplication());

        // *** TAMBAHAN: Listener untuk tombol Enter di usernameField ***
        usernameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Ketika Enter ditekan di usernameField, picu aksi playButton
                playButton.doClick();
            }
        });
        // --- Akhir Tambahan (Enter) ---

        mainViewModel.loadScores();
    }

    // --- TAMBAHAN: Metode untuk Setup Global Key Listeners (Esc) ---
    private void setupGlobalKeyListeners() {
        // Menggunakan JRootPane atau content pane untuk menangkap key event secara global
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        // Bind ESC key to "exitAction"
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exitAction");
        actionMap.put("exitAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainViewModel.exitApplication(); // Panggil logika keluar aplikasi
            }
        });
    }
    // --- Akhir Tambahan (Esc) ---

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

        // Penting: Inisialisasi ulang backgroundPanel dan panggil setupMainScreenUI
        backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        setupMainScreenUI(); // Panggil ini untuk membangun kembali UI menu utama di backgroundPanel

        revalidate();
        repaint();
        mainViewModel.loadScores();
        usernameField.setText("");
        // Fokuskan kembali usernameField setelah kembali ke menu utama
        usernameField.requestFocusInWindow();
    }

    public void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    
    private void playMainMenuMusic() {
        if (AssetLoader.mainMenuMusicClip != null) {
            if (AssetLoader.mainMenuMusicClip.isRunning()) {
                AssetLoader.mainMenuMusicClip.stop();
            }
            AssetLoader.mainMenuMusicClip.setFramePosition(0);
            AssetLoader.mainMenuMusicClip.loop(Clip.LOOP_CONTINUOUSLY);

            if (AssetLoader.mainMenuMusicGainControl != null) {
                AssetLoader.setClipVolume(AssetLoader.mainMenuMusicGainControl, 0.05f);
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

    private static class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (AssetLoader.mainMenuBackgroundImage != null) {
                g.drawImage(AssetLoader.mainMenuBackgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}