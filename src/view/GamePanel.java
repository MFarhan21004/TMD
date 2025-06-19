import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl; // NEW: Penting untuk kontrol volume


/**
 * Kelas GamePanel bertanggung jawab untuk menampilkan semua elemen game
 * dan menangani interaksi pengguna (keyboard dan mouse).
 * Kelas ini bertindak sebagai "View" dalam arsitektur Model-View-ViewModel (MVVM)
 * yang berinteraksi dengan GameViewModel untuk logika game dan MainFrame untuk navigasi.
 */
public class GamePanel extends JPanel implements ActionListener {
    private GameViewModel gameViewModel; // Referensi ke ViewModel yang mengelola state dan logika game.
    private MainFrame mainFrame; // Referensi ke frame utama aplikasi untuk navigasi antar panel.
    private Timer gameLoop; // Timer untuk mengontrol kecepatan update game (game loop).
    private Random random; // Objek Random, meskipun tidak secara eksplisit digunakan di GamePanel ini, mungkin di ViewModel.

    // Variabel terkait animasi lasso (tali penangkap)
    private boolean isLassoActive = false; // Menunjukkan apakah lasso sedang dalam proses penembakan/penarikan.
    private int currentLassoDrawLength = 0; // Panjang lasso yang sedang digambar saat ini untuk animasi.
    private int targetLassoLength = 0;      // Panjang maksimal yang harus dicapai lasso (jarak dari pemain ke klik mouse).
    private int lassoDirection = 1;         // Arah pergerakan lasso: 1 untuk memanjang, -1 untuk menarik kembali.
    private final int LASSO_ANIMATION_SPEED = 10; // Kecepatan pergerakan lasso per frame.

    // Koordinat target klik mouse untuk arah tembakan lasso.
    private int mouseTargetX;
    private int mouseTargetY;

    // Variabel untuk animasi bola yang sedang ditarik oleh lasso.
    private Balls animatingPulledBall = null; // Bola yang saat ini sedang ditarik kembali oleh lasso.

    // Variabel untuk bola yang sedang dipegang oleh pemain.
    private Balls heldBall = null; // Bola yang saat ini dipegang oleh pemain setelah ditarik.
    private double heldBallOffsetAngle = 0; // Sudut offset bola yang dipegang relatif terhadap pemain untuk animasi melingkar.

    // Konstanta untuk tipe pesan JOptionPane, digunakan untuk dialog pop-up.
    public static final int MESSAGE_TYPE_PLAIN = JOptionPane.PLAIN_MESSAGE;
    public static final int MESSAGE_TYPE_INFORMATION = JOptionPane.INFORMATION_MESSAGE;
    public static final int MESSAGE_TYPE_WARNING = JOptionPane.WARNING_MESSAGE;
    public static final int MESSAGE_TYPE_ERROR = JOptionPane.ERROR_MESSAGE;
    public static final int MESSAGE_TYPE_QUESTION = JOptionPane.QUESTION_MESSAGE;

    private final int HOOK_DISPLAY_SIZE = 50; // Ukuran tampilan kail lasso.
    private final int CHAIN_SEGMENT_DISPLAY_SIZE = 25; // Ukuran segmen rantai lasso yang diinginkan.

    // Ukuran tampilan keranjang tempat mengumpulkan bola.
    private final int BASKET_DISPLAY_WIDTH = 80;
    private final int BASKET_DISPLAY_HEIGHT = 120;

    // --- VARIABEL ANIMASI PEMAIN ---
    private HashMap<String, ArrayList<Rectangle>> playerAnimations; // Map untuk menyimpan frame animasi per status (misal "idle_down").
    private String currentPlayerAnimation = "idle_down"; // Status animasi pemain yang sedang aktif (misal: idle_down, walk_left).
    private int currentAnimationFrame = 0; // Indeks frame saat ini dalam animasi yang sedang aktif.
    private int animationTick = 0; // Counter untuk mengontrol kecepatan perubahan frame animasi.
    private final int ANIMATION_SPEED_FACTOR = 10; // Faktor yang menentukan seberapa sering frame animasi berubah (lebih rendah = lebih cepat).
    private boolean isPlayerMoving = false; // Status apakah pemain sedang bergerak.
    private String lastPlayerDirection = "down"; // Arah terakhir pemain bergerak (untuk menentukan animasi idle).

    // Variabel animasi efek bonus di sekitar pemain.
    private int bonusEffectFrame = 0; // Frame animasi efek bonus saat ini.
    private final int BONUS_EFFECT_FRAME_COUNT = 4; // Jumlah total frame horizontal dalam sprite sheet efek bonus.
    private final int BONUS_EFFECT_FRAME_WIDTH = 204; // Lebar setiap frame efek bonus dalam sprite sheet.
    private final int BONUS_EFFECT_FRAME_HEIGHT = 228; // Tinggi setiap frame efek bonus dalam sprite sheet.
    private final int BONUS_EFFECT_ANIMATION_SPEED = 5; // Kecepatan perubahan frame animasi efek bonus.

    private final int BONUS_EFFECT_DISPLAY_SIZE = 180; // Ukuran tampilan efek bonus di layar.
    private final Font BONUS_TIMER_FONT = new Font("Arial", Font.BOLD, 24); // Font untuk menampilkan sisa waktu bonus.


    /**
     * Konstruktor untuk GamePanel.
     * Menginisialisasi komponen UI, ViewModel, dan listener input.
     *
     * @param username Nama pengguna pemain.
     * @param mainFrame Referensi ke MainFrame induk untuk interaksi UI level atas.
     */
    public GamePanel(String username, MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        gameViewModel = new GameViewModel(username, this); // Inisialisasi ViewModel dengan username dan referensi panel ini.
        setPreferredSize(new Dimension(1200, 700)); // Mengatur ukuran preferred dari panel.
        setFocusable(true); // Memungkinkan panel untuk menerima fokus keyboard.
        addKeyListener(new GameKeyListener()); // Menambahkan KeyListener untuk input keyboard.
        addMouseListener(new GameMouseListener()); // Menambahkan MouseListener untuk input mouse.

        random = new Random(); // Inisialisasi objek Random.
        gameLoop = new Timer(15, this); // Menginisialisasi Timer untuk game loop, memicu actionPerformed setiap 15ms.

        loadPlayerAnimations(); // Memuat semua frame animasi pemain dari sprite sheet.
        // playBackgroundMusic(); // Tidak dipanggil di sini, akan dipanggil di startGameLogic untuk memastikan musik mulai saat game siap.
    }

    /**
     * Memulai logika inti game. Dipanggil saat game dimulai atau diulang.
     * Menginisialisasi ulang ViewModel, mereset state tampilan, dan mengatur musik.
     */
    public void startGameLogic() {
        gameViewModel.initializeGame(); // Menginisialisasi ulang semua aspek game di ViewModel.
        resetGamePanelState(); // Mereset variabel-variabel state GamePanel ke kondisi awal.
        playBackgroundMusic(); // Memulai musik latar belakang game.
        // NEW: Mengatur volume untuk berbagai klip audio saat game dimulai.
        // Pengecekan null dilakukan untuk menghindari NullPointerException jika kontrol volume belum dimuat.
        if (AssetLoader.backgroundMusicGainControl != null) {
            AssetLoader.setClipVolume(AssetLoader.backgroundMusicGainControl, 0.5f); // Volume musik latar 50%.
        } else {
            System.err.println("Background music gain control not available.");
        }
        if (AssetLoader.bonusMusicGainControl != null) {
            AssetLoader.setClipVolume(AssetLoader.bonusMusicGainControl, 0.6f); // Volume musik bonus sedikit lebih keras.
        } else {
            System.err.println("Bonus music gain control not available.");
        }
        if (AssetLoader.bombEffectGainControl != null) {
            AssetLoader.setClipVolume(AssetLoader.bombEffectGainControl, 0.8f); // Volume efek bom paling keras.
        } else {
            System.err.println("Bomb effect sound gain control not available.");
        }

        gameLoop.start(); // Memulai game loop.
    }

    /**
     * Mereset semua variabel state GamePanel ke nilai defaultnya.
     * Digunakan untuk menyiapkan panel untuk game baru atau setelah game berakhir.
     */
    private void resetGamePanelState() {
        isLassoActive = false;
        currentLassoDrawLength = 0;
        targetLassoLength = 0;
        lassoDirection = 1;
        mouseTargetX = 0;
        mouseTargetY = 0;
        animatingPulledBall = null;
        heldBall = null;
        heldBallOffsetAngle = 0;

        // Mereset state animasi pemain ke idle menghadap bawah.
        currentPlayerAnimation = "idle_down";
        currentAnimationFrame = 0;
        animationTick = 0;
        isPlayerMoving = false;
        lastPlayerDirection = "down";

        stopAllMusic(); // Menghentikan semua musik.
    }

    /**
     * Metode untuk memuat definisi frame animasi pemain dari sprite sheet.
     * Frame-frame ini disimpan dalam HashMap dengan kunci string yang merepresentasikan status animasi.
     * Saat ini, hanya mendukung 4 arah utama (atas, bawah, kiri, kanan) dan idle untuk setiap arah tersebut.
     * Animasi `walk_down` menggunakan frame yang sama dengan `idle_down`.
     */
    private void loadPlayerAnimations() {
        playerAnimations = new HashMap<>();

        int frameWidth = 80;  // Lebar satu frame dalam sprite sheet.
        int frameHeight = 118; // Tinggi satu frame dalam sprite sheet.

        // Memuat frame untuk animasi 'idle_down' dan 'walk_down' (baris 0 dari sprite sheet).
        ArrayList<Rectangle> idleDownFrames = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            idleDownFrames.add(new Rectangle(i * frameWidth, 0 * frameHeight, frameWidth, frameHeight));
        }
        playerAnimations.put("idle_down", idleDownFrames);
        playerAnimations.put("walk_down", idleDownFrames);

        // Memuat frame untuk animasi 'walk_left' (baris 1 dari sprite sheet).
        ArrayList<Rectangle> walkLeftFrames = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            walkLeftFrames.add(new Rectangle(i * frameWidth, 1 * frameHeight, frameWidth, frameHeight));
        }
        playerAnimations.put("walk_left", walkLeftFrames);

        // Memuat frame untuk animasi 'walk_right' (baris 2 dari sprite sheet).
        ArrayList<Rectangle> walkRightFrames = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            walkRightFrames.add(new Rectangle(i * frameWidth, 2 * frameHeight, frameWidth, frameHeight));
        }
        playerAnimations.put("walk_right", walkRightFrames);

        // Memuat frame untuk animasi 'walk_up' (baris 3 dari sprite sheet).
        ArrayList<Rectangle> walkUpFrames = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            walkUpFrames.add(new Rectangle(i * frameWidth, 3 * frameHeight, frameWidth, frameHeight));
        }
        playerAnimations.put("walk_up", walkUpFrames);

        // Menambahkan frame idle statis untuk arah kiri, kanan, dan atas (frame pertama dari baris walk yang sesuai).
        playerAnimations.put("idle_left", new ArrayList<Rectangle>() {{ add(new Rectangle(0 * frameWidth, 1 * frameHeight, frameWidth, frameHeight)); }} );
        playerAnimations.put("idle_right", new ArrayList<Rectangle>() {{ add(new Rectangle(0 * frameWidth, 2 * frameHeight, frameWidth, frameHeight)); }} );
        playerAnimations.put("idle_up", new ArrayList<Rectangle>() {{ add(new Rectangle(0 * frameWidth, 3 * frameHeight, frameWidth, frameHeight)); }} );
        
        System.out.println("Player astronaut animations loaded."); // Konfirmasi pemuatan animasi.
    }

    // Metode-metode untuk kontrol suara

    /**
     * Memutar musik latar belakang secara berulang.
     * Jika musik sudah berjalan, akan dihentikan dan dimulai ulang dari awal.
     */
    private void playBackgroundMusic() {
        if (AssetLoader.backgroundMusicClip != null) {
            if (AssetLoader.backgroundMusicClip.isRunning()) {
                AssetLoader.backgroundMusicClip.stop(); // Hentikan jika sedang berjalan.
            }
            AssetLoader.backgroundMusicClip.setFramePosition(0); // Set posisi ke awal.
            AssetLoader.backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY); // Putar secara berulang.
        }
    }

    /**
     * Memutar musik bonus secara berulang.
     * Menghentikan musik latar belakang terlebih dahulu untuk transisi yang mulus.
     */
    public void playBonusMusic() {
        if (AssetLoader.bonusMusicClip != null) {
            stopBackgroundMusic(); // Hentikan musik latar.
            if (!AssetLoader.bonusMusicClip.isRunning()) { // Pastikan tidak memutar dua kali jika sudah berjalan.
                AssetLoader.bonusMusicClip.setFramePosition(0);
                AssetLoader.bonusMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }
    }

    /**
     * Menghentikan musik bonus dan memulai kembali musik latar belakang.
     */
    public void stopBonusMusic() {
        if (AssetLoader.bonusMusicClip != null && AssetLoader.bonusMusicClip.isRunning()) {
            AssetLoader.bonusMusicClip.stop(); // Hentikan musik bonus.
            playBackgroundMusic(); // Mulai kembali musik latar.
        }
    }

    /**
     * Memutar efek suara bom satu kali.
     * Jika efek sedang berjalan, akan dihentikan dan dimulai ulang dari awal.
     */
    public void playBombEffectSound() {
        if (AssetLoader.bombEffectClip != null) {
            if (AssetLoader.bombEffectClip.isRunning()) {
                AssetLoader.bombEffectClip.stop(); // Hentikan jika sedang berjalan.
            }
            AssetLoader.bombEffectClip.setFramePosition(0); // Set posisi ke awal.
            AssetLoader.bombEffectClip.start(); // Mulai putar satu kali.
        }
    }

    /**
     * Menghentikan musik latar belakang jika sedang berjalan.
     */
    private void stopBackgroundMusic() {
        if (AssetLoader.backgroundMusicClip != null && AssetLoader.backgroundMusicClip.isRunning()) {
            AssetLoader.backgroundMusicClip.stop();
        }
    }

    /**
     * Menghentikan semua klip musik yang mungkin sedang diputar (latar, bonus, menu utama).
     */
    private void stopAllMusic() {
        stopBackgroundMusic(); // Menghentikan musik latar.
        if (AssetLoader.bonusMusicClip != null && AssetLoader.bonusMusicClip.isRunning()) {
             AssetLoader.bonusMusicClip.stop(); // Menghentikan musik bonus.
        }
        if (AssetLoader.mainMenuMusicClip != null && AssetLoader.mainMenuMusicClip.isRunning()) {
             AssetLoader.mainMenuMusicClip.stop(); // Menghentikan musik menu utama.
        }
    }

    /**
     * Metode ini dipanggil setiap kali panel perlu digambar ulang (misalnya, oleh `repaint()`).
     * Ini bertanggung jawab untuk rendering semua elemen game ke layar.
     * @param g Objek Graphics yang digunakan untuk menggambar.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Memanggil implementasi `paintComponent` dari superclass (JPanel) untuk menggambar background default.
        Graphics2D g2d = (Graphics2D) g; // Mengkonversi ke Graphics2D untuk fitur menggambar yang lebih canggih (misalnya rotasi, transform).

        FontMetrics fm = g2d.getFontMetrics(); // Digunakan untuk menghitung dimensi teks (misalnya lebar untuk senter teks).

        // Menggambar Gambar Latar Belakang
        if (AssetLoader.backgroundImage != null) {
            // Menggambar gambar latar belakang, skala agar sesuai dengan ukuran panel.
            g2d.drawImage(AssetLoader.backgroundImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            // Fallback: Jika gambar latar belakang tidak dimuat, gambar persegi panjang abu-abu gelap.
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        Player player = gameViewModel.getPlayer(); // Mendapatkan objek pemain dari ViewModel.
        int playerCenterX = player.getX() + 25; // Menghitung pusat X pemain (pemain 50x50, jadi pusatnya di +25).
        int playerCenterY = player.getY() + 25; // Menghitung pusat Y pemain.

        // --- GAMBAR PEMAIN DENGAN ANIMASI ASTRONOT ---
        if (AssetLoader.playerAstronautSprite != null) {
            ArrayList<Rectangle> currentFrames = playerAnimations.get(currentPlayerAnimation); // Mendapatkan list frame untuk animasi pemain saat ini.
            if (currentFrames != null && !currentFrames.isEmpty()) {
                if (currentAnimationFrame >= currentFrames.size()) {
                    currentAnimationFrame = 0; // Reset frame ke awal jika sudah mencapai akhir animasi.
                }

                Rectangle frameRect = currentFrames.get(currentAnimationFrame); // Mendapatkan Rectangle yang mendefinisikan area frame saat ini di sprite sheet.
                BufferedImage currentSprite = AssetLoader.playerAstronautSprite.getSubimage(
                    frameRect.x, frameRect.y, frameRect.width, frameRect.height // Mengambil sub-gambar (frame) dari sprite sheet.
                );
                g2d.drawImage(currentSprite, player.getX(), player.getY(), 50, 50, null); // Menggambar frame pemain, diskalakan ke ukuran 50x50.
            } else {
                // Fallback: Jika animasi tidak ditemukan atau kosong, gambar kotak biru.
                g2d.setColor(Color.BLUE);
                g2d.fillRect(player.getX(), player.getY(), 50, 50);
            }
        } else {
            // Fallback: Jika sprite sheet pemain tidak dimuat, gambar kotak biru.
            g2d.setColor(Color.BLUE);
            g2d.fillRect(player.getX(), player.getY(), 50, 50);
        }

        // Menggambar Efek Bonus (efekbonus.png) di sekitar pemain jika aktif (speed boost atau invincible).
        if (player.isSpeedBoostActive() || player.isInvincible()) {
            if (AssetLoader.bonusEffectSprite != null) {
                int effectFrameWidth = BONUS_EFFECT_FRAME_WIDTH;
                int effectFrameHeight = BONUS_EFFECT_FRAME_HEIGHT;
                
                // Mendapatkan frame efek bonus saat ini dari sprite sheet.
                BufferedImage currentEffectSprite = AssetLoader.bonusEffectSprite.getSubimage(
                    (bonusEffectFrame / BONUS_EFFECT_ANIMATION_SPEED) % BONUS_EFFECT_FRAME_COUNT * effectFrameWidth, // Menghitung posisi X frame.
                    0, // Asumsi semua frame efek bonus berada di baris pertama (Y=0) sprite sheet.
                    effectFrameWidth,
                    effectFrameHeight
                );
                
                // Menggambar efek bonus di sekitar pemain, dengan penyesuaian posisi agar terlihat mengelilingi pemain.
                g2d.drawImage(currentEffectSprite, 
                              player.getX() + (50 - BONUS_EFFECT_DISPLAY_SIZE) / 2 - 15, // Penyesuaian X.
                              player.getY() + (50 - BONUS_EFFECT_DISPLAY_SIZE) / 2 - 40, // Penyesuaian Y.
                              BONUS_EFFECT_DISPLAY_SIZE, BONUS_EFFECT_DISPLAY_SIZE, null);
            } else {
                // Fallback: Jika sprite efek bonus tidak dimuat, gambar lingkaran hijau transparan di sekitar pemain.
                g2d.setColor(new Color(0, 255, 0, 100)); // Warna hijau dengan transparansi.
                g2d.fillOval(player.getX() - 10, player.getY() - 10, 70, 70);
            }

            // Menggambar Timer Efek Bonus (waktu yang tersisa).
            g2d.setColor(Color.YELLOW);
            g2d.setFont(BONUS_TIMER_FONT);
            
            // Mengambil waktu tersisa maksimal dari kedua efek (speed boost atau invincibility).
            int remainingTime = Math.max(player.getSpeedBoostDuration(), player.getInvincibilityDuration()); 
            String timerText = String.format("%d s", remainingTime / 60); // Format waktu ke detik.
            
            int textWidth = fm.stringWidth(timerText); // Menghitung lebar teks timer.
            g2d.drawString(timerText, player.getX() + (40 - textWidth) / 2, player.getY() - 10); // Menggambar teks timer di atas pemain.
        }


        // Menggambar Bola-bola Skill (item yang bisa ditangkap)
        for (Balls ball : gameViewModel.getBalls()) {
            // Hanya menggambar bola yang aktif, tidak sedang dipegang oleh pemain, dan tidak sedang ditarik lasso.
            if (ball.isActive() && !ball.isHeldByPlayer() && !ball.isBeingPulled()) {
                if (ball.isBomb()) { // Jika bola adalah bom.
                    if (AssetLoader.bombAlienImage != null) {
                        g2d.drawImage(AssetLoader.bombAlienImage, ball.getX(), ball.getY(), ball.getSize(), ball.getSize(), null);
                    } else {
                        // Fallback: Jika gambar bom tidak ada, gambar oval dengan warna bola.
                        g2d.setColor(ball.getColor());
                        g2d.fillOval(ball.getX(), ball.getY(), ball.getSize(), ball.getSize());
                    }
                    g2d.setColor(Color.WHITE); // Warna teks untuk nilai bom (jika ditampilkan).
                    g2d.setFont(new Font("Arial", Font.BOLD, 10));
                    // Catatan: Kode ini menggambar teks putih untuk bom, meskipun bom biasanya tidak memiliki nilai yang ditampilkan di UI.
                } else if (ball.isBonusStar()) { // Jika bola adalah bintang bonus.
                    if (AssetLoader.bonusStarImage != null) {
                        g2d.drawImage(AssetLoader.bonusStarImage, ball.getX(), ball.getY(), ball.getSize(), ball.getSize(), null);
                    } else {
                        // Fallback: Jika gambar bintang tidak ada, gambar oval dengan warna bola.
                        g2d.setColor(ball.getColor());
                        g2d.fillOval(ball.getX(), ball.getY(), ball.getSize(), ball.getSize());
                    }
                }
                else { // Jika bola adalah bola skill biasa.
                    if (AssetLoader.skillBallImage != null) {
                        g2d.drawImage(AssetLoader.skillBallImage, ball.getX(), ball.getY(), ball.getSize(), ball.getSize(), null);
                    } else {
                        // Fallback: Jika gambar bola skill tidak ada, gambar oval dengan warna bola.
                        g2d.setColor(ball.getColor());
                        g2d.fillOval(ball.getX(), ball.getY(), ball.getSize(), ball.getSize());
                    }
                    g2d.setColor(Color.BLACK); // Warna teks untuk nilai bola skill.
                    g2d.setFont(new Font("Arial", Font.BOLD, 10));
                    String valueStr = String.valueOf(ball.getValue()); // Mengambil nilai bola.
                    int textWidth = fm.stringWidth(valueStr); // Menghitung lebar teks nilai.
                    int textHeight = fm.getHeight(); // Menghitung tinggi teks.
                    g2d.drawString(valueStr, ball.getX() + ball.getSize() / 2 - textWidth / 2, ball.getY() + ball.getSize() / 2 + textHeight / 4); // Menggambar nilai di tengah bola.
                }
            }
        }

        // Menggambar Lasso (tali penangkap)
        int lassoEndX = 0; // Koordinat X ujung lasso saat ini.
        int lassoEndY = 0; // Koordinat Y ujung lasso saat ini.
        double angle = 0;   // Sudut tembakan lasso.

        if (isLassoActive) { // Jika lasso sedang aktif (sedang ditembakkan atau ditarik).
            // Menghitung sudut dari pemain ke target mouse (arah lasso).
            angle = Math.atan2(mouseTargetY - playerCenterY, mouseTargetX - playerCenterX);
            // Menghitung koordinat ujung lasso berdasarkan panjang gambar saat ini dan sudut.
            lassoEndX = (int) (playerCenterX + currentLassoDrawLength * Math.cos(angle));
            lassoEndY = (int) (playerCenterY + currentLassoDrawLength * Math.sin(angle));

            // Menggambar Rantai Lasso
            if (AssetLoader.lassoChainSegmentImage != null) { // Jika ada gambar segmen rantai.
                int segmentWidth = AssetLoader.lassoChainSegmentImage.getWidth();
                int segmentHeight = AssetLoader.lassoChainSegmentImage.getHeight();
                
                if (segmentWidth <= 0 || segmentHeight <= 0) {
                     // Fallback: Jika gambar segmen rantai tidak valid (misalnya lebar/tinggi nol), gambar garis sederhana.
                     g2d.setColor(Color.ORANGE);
                     g2d.setStroke(new BasicStroke(2)); // Mengatur ketebalan garis.
                     g2d.drawLine(playerCenterX, playerCenterY, lassoEndX, lassoEndY);
                     g2d.setStroke(new BasicStroke(1)); // Mengembalikan ketebalan garis default.
                } else {
                    int segmentStep = CHAIN_SEGMENT_DISPLAY_SIZE; // Jarak antar segmen rantai yang akan digambar.

                    int segmentsToDraw = 0;
                    if (segmentStep > 0) {
                        segmentsToDraw = currentLassoDrawLength / segmentStep; // Menghitung berapa banyak segmen yang perlu digambar sepanjang lasso.
                    }
                    
                    AffineTransform oldTransform = g2d.getTransform(); // Menyimpan AffineTransform saat ini untuk dikembalikan nanti.

                    g2d.translate(playerCenterX, playerCenterY); // Menggeser origin gambar ke pusat pemain.
                    g2d.rotate(angle); // Memutar koordinat sesuai sudut lasso.

                    // Menggambar setiap segmen rantai sepanjang lasso.
                    for (int i = 0; i <= segmentsToDraw; i++) {
                        // Menggambar segmen rantai; -CHAIN_SEGMENT_DISPLAY_SIZE / 2 untuk menengahkan segmen secara vertikal.
                        g2d.drawImage(AssetLoader.lassoChainSegmentImage, i * segmentStep, -CHAIN_SEGMENT_DISPLAY_SIZE / 2, CHAIN_SEGMENT_DISPLAY_SIZE, CHAIN_SEGMENT_DISPLAY_SIZE, null);
                    }
                    g2d.setTransform(oldTransform); // Mengembalikan AffineTransform ke kondisi semula.
                }

            } else {
                // Fallback: Jika gambar segmen rantai tidak ada, gambar garis oranye tebal sebagai pengganti rantai.
                g2d.setColor(Color.ORANGE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(playerCenterX, playerCenterY, lassoEndX, lassoEndY);
                g2d.setStroke(new BasicStroke(1));
            }


            // Menggambar Kail Lasso di Ujung
            if (AssetLoader.lassoHookImage != null) { // Jika ada gambar kail lasso.
                AffineTransform oldTransform = g2d.getTransform(); // Menyimpan AffineTransform saat ini.

                g2d.translate(lassoEndX, lassoEndY); // Menggeser origin gambar ke ujung lasso.
                g2d.rotate(angle - Math.PI / 2); // Memutar kail lasso agar orientasinya sesuai dengan tali. (Asumsi gambar kail default menghadap ke atas)

                g2d.drawImage(AssetLoader.lassoHookImage, -HOOK_DISPLAY_SIZE / 2, -HOOK_DISPLAY_SIZE / 2, HOOK_DISPLAY_SIZE, HOOK_DISPLAY_SIZE, null); // Menggambar kail, menengahkan posisinya.
                
                g2d.setTransform(oldTransform); // Mengembalikan AffineTransform ke kondisi semula.
            } else {
                // Fallback: Jika gambar kail tidak ada, gambar kotak cyan kecil di ujung lasso.
                int squareSize = 8;
                g2d.setColor(Color.CYAN);
                g2d.fillRect(lassoEndX - squareSize / 2, lassoEndY - squareSize / 2, squareSize, squareSize);
            }
        }

        // Menggambar Keranjang/Titik Pengumpulan Bola
        if (AssetLoader.basketImage != null) { // Jika ada gambar keranjang.
            // Hitung posisi X agar keranjang berada di tengah horizontal.
            int basketX = (getWidth() / 2) - ((BASKET_DISPLAY_WIDTH + 50) / 2);

            // Hitung posisi Y agar keranjang berada dekat bagian bawah panel.
            int basketY = getHeight() - (BASKET_DISPLAY_HEIGHT + 50) - 50; // Sekitar 50px dari bawah, disesuaikan dengan ukuran gambar.

            g2d.drawImage(AssetLoader.basketImage, basketX, basketY, BASKET_DISPLAY_WIDTH + 50, BASKET_DISPLAY_HEIGHT + 50, null); // Menggambar keranjang.
        } else {
            // Fallback: Jika gambar keranjang tidak ada, gambar kotak coklat sebagai pengganti.
            g2d.setColor(new Color(139, 69, 19)); // Warna coklat.

            // Hitung posisi X dan Y untuk fallback juga, agar posisinya konsisten.
            int basketX = (getWidth() / 2) - (BASKET_DISPLAY_WIDTH / 2);
            int basketY = getHeight() - BASKET_DISPLAY_HEIGHT - 20; // 20px dari bawah.

            g2d.fillRect(basketX, basketY, BASKET_DISPLAY_WIDTH, BASKET_DISPLAY_HEIGHT);
        }


        // Menggambar bola yang saat ini sedang ditarik oleh lasso.
        if (animatingPulledBall != null) {
            // Jika lasso tidak aktif tetapi bola masih ditandai sedang ditarik (untuk fase retraksi).
            if (!isLassoActive && animatingPulledBall.isBeingPulled()) {
                angle = Math.atan2(mouseTargetY - playerCenterY, mouseTargetX - playerCenterX); // Hitung ulang sudut.
                lassoEndX = (int) (playerCenterX + currentLassoDrawLength * Math.cos(angle)); // Hitung posisi X ujung lasso.
                lassoEndY = (int) (playerCenterY + currentLassoDrawLength * Math.sin(angle)); // Hitung posisi Y ujung lasso.
            }

            int currentBallX = lassoEndX; // Posisi X bola diatur sama dengan ujung lasso.
            int currentBallY = lassoEndY; // Posisi Y bola diatur sama dengan ujung lasso.

            // Offset agar bola terlihat menempel pada kail lasso, bukan di tengah kail.
            int ballOffsetFromLassoTipX = -animatingPulledBall.getSize()/2;
            int ballOffsetFromLassoTipY = -animatingPulledBall.getSize()/2;

            // Menggambar bola yang ditarik berdasarkan tipenya (bom, bintang bonus, atau bintang point).
            if (animatingPulledBall.isBomb()) {
                if (AssetLoader.bombAlienImage != null) {
                    g2d.drawImage(AssetLoader.bombAlienImage, currentBallX + ballOffsetFromLassoTipX, currentBallY + ballOffsetFromLassoTipY, animatingPulledBall.getSize(), animatingPulledBall.getSize(), null);
                } else {
                    g2d.setColor(animatingPulledBall.getColor());
                    g2d.fillOval(currentBallX + ballOffsetFromLassoTipX, currentBallY + ballOffsetFromLassoTipY, animatingPulledBall.getSize(), animatingPulledBall.getSize());
                }
            } else if (animatingPulledBall.isBonusStar()) {
                if (AssetLoader.bonusStarImage != null) {
                    g2d.drawImage(AssetLoader.bonusStarImage, currentBallX + ballOffsetFromLassoTipX, currentBallY + ballOffsetFromLassoTipY, animatingPulledBall.getSize(), animatingPulledBall.getSize(), null);
                } else {
                    g2d.setColor(animatingPulledBall.getColor());
                    g2d.fillOval(currentBallX + ballOffsetFromLassoTipX, currentBallY + ballOffsetFromLassoTipY, animatingPulledBall.getSize(), animatingPulledBall.getSize());
                }
            }
            else { // Ini adalah bola skill biasa.
                if (AssetLoader.skillBallImage != null) {
                    g2d.drawImage(AssetLoader.skillBallImage, currentBallX + ballOffsetFromLassoTipX, currentBallY + ballOffsetFromLassoTipY, animatingPulledBall.getSize(), animatingPulledBall.getSize(), null);
                } else {
                    g2d.setColor(animatingPulledBall.getColor());
                    g2d.fillOval(currentBallX + ballOffsetFromLassoTipX, currentBallY + ballOffsetFromLassoTipY, animatingPulledBall.getSize(), animatingPulledBall.getSize());
                }
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                String valueStr = String.valueOf(animatingPulledBall.getValue());
                int textWidth = fm.stringWidth(valueStr);
                int textHeight = fm.getHeight();
                g2d.drawString(valueStr, currentBallX + ballOffsetFromLassoTipX + animatingPulledBall.getSize()/2 - textWidth / 2, currentBallY + ballOffsetFromLassoTipY + animatingPulledBall.getSize()/2 + textHeight / 4);
            }
        }

        // Menggambar bola yang saat ini dipegang oleh pemain.
        if (heldBall != null) {
            int offsetDistance = 10 + heldBall.getSize() / 2; // Jarak bola dari pusat pemain, ditambah setengah ukuran bola.

            // Menghitung posisi X dan Y bola yang dipegang, menciptakan efek melingkar di sekitar pemain.
            int ballX = (int) (playerCenterX + offsetDistance * Math.cos(heldBallOffsetAngle)) - heldBall.getSize() / 2;
            int ballY = (int) (playerCenterY + offsetDistance * Math.sin(heldBallOffsetAngle)) - heldBall.getSize() / 2;

            // Menggambar bola yang dipegang berdasarkan tipenya.
            if (heldBall.isBomb()) {
                if (AssetLoader.bombAlienImage != null) {
                    g2d.drawImage(AssetLoader.bombAlienImage, ballX, ballY, heldBall.getSize(), heldBall.getSize(), null);
                } else {
                    g2d.setColor(heldBall.getColor());
                    g2d.fillOval(ballX, ballY, heldBall.getSize(), heldBall.getSize());
                }
            } else if (heldBall.isBonusStar()) {
                if (AssetLoader.bonusStarImage != null) {
                    g2d.drawImage(AssetLoader.bonusStarImage, ballX, ballY, heldBall.getSize(), heldBall.getSize(), null);
                } else {
                    g2d.setColor(heldBall.getColor());
                    g2d.fillOval(ballX, ballY, heldBall.getSize(), heldBall.getSize());
                }
            }
            else { // Ini adalah bola skill biasa.
                if (AssetLoader.skillBallImage != null) {
                    g2d.drawImage(AssetLoader.skillBallImage, ballX, ballY, heldBall.getSize(), heldBall.getSize(), null);
                } else {
                    g2d.setColor(heldBall.getColor());
                    g2d.fillOval(ballX, ballY, heldBall.getSize(), heldBall.getSize());
                }
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                String valueStr = String.valueOf(heldBall.getValue());
                int textWidth = fm.stringWidth(valueStr);
                int textHeight = fm.getHeight();
                g2d.drawString(valueStr, ballX + heldBall.getSize() / 2 - textWidth / 2, ballY + heldBall.getSize() / 2 + textHeight / 4);
            }
        }

        // Menggambar skor pemain dan jumlah bintang yang dikumpulkan di pojok kiri atas.
        g2d.setColor(Color.WHITE); // Warna teks putih.
        g2d.setFont(new Font("Arial", Font.BOLD, 20)); // Font tebal ukuran 20.
        g2d.drawString("Score: " + player.getScore(), 10, 30); // Tampilkan skor.
        g2d.drawString("Count Star: " + player.getCollectedBalls(), 10, 60); // Tampilkan jumlah bintang.
    }

    /**
     * Mengembalikan referensi ke MainFrame induk.
     * @return Objek MainFrame.
     */
    public MainFrame getMainFrame() {
        return this.mainFrame;
    }

    /**
     * Metode ini dipanggil setiap kali Timer `gameLoop` "berdetak".
     * Ini adalah inti dari loop game, di mana semua logika update (game state, animasi)
     * terjadi sebelum panel digambar ulang.
     * @param e Event Action yang memicu panggilan ini (dari Timer).
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        gameViewModel.updateGame(); // Memperbarui logika game inti melalui ViewModel.
        handleLassoAnimation(); // Menangani logika dan update animasi lasso.
        
        // --- UPDATE FRAME ANIMASI PEMAIN ---
        animationTick++; // Meningkatkan counter tick animasi.
        if (animationTick >= ANIMATION_SPEED_FACTOR) { // Cek apakah sudah waktunya mengganti frame.
            ArrayList<Rectangle> frames = playerAnimations.get(currentPlayerAnimation); // Mendapatkan list frame untuk animasi saat ini.

            if (frames != null && !frames.isEmpty()) { // Pastikan ada frame untuk animasi ini.
                if (currentPlayerAnimation.startsWith("idle_")) {
                    currentAnimationFrame = 0; // Untuk animasi idle, selalu kembali ke frame pertama (asumsi idle statis atau loop kecil).
                } else {
                    currentAnimationFrame++; // Pindah ke frame berikutnya untuk animasi bergerak.
                    if (currentAnimationFrame >= frames.size()) {
                        currentAnimationFrame = 0; // Kembali ke frame pertama jika sudah mencapai akhir animasi.
                    }
                }
            } else {
                currentAnimationFrame = 0; // Reset frame jika tidak ada frame yang ditemukan (fallback).
            }
            animationTick = 0; // Reset tick animasi.
        }

        // Memperbarui frame animasi efek bonus.
        bonusEffectFrame++;
        if (bonusEffectFrame >= BONUS_EFFECT_FRAME_COUNT * BONUS_EFFECT_ANIMATION_SPEED) {
            bonusEffectFrame = 0; // Kembali ke frame pertama jika sudah mencapai akhir.
        }

        // Jika pemain tidak sedang bergerak, atur animasi ke status idle berdasarkan arah terakhir.
        if (!isPlayerMoving) {
            currentPlayerAnimation = "idle_" + lastPlayerDirection;
        }
        isPlayerMoving = false; // Reset status bergerak untuk siklus update berikutnya (akan diatur true lagi jika ada input).

        repaint(); // Meminta sistem untuk menggambar ulang panel (memanggil paintComponent).

        // Logika untuk mendeteksi dan mengumpulkan bola yang dipegang saat pemain menyentuh keranjang.
        if (heldBall != null) {
            Player player = gameViewModel.getPlayer();
            Rectangle playerBounds = new Rectangle(player.getX(), player.getY(), 50, 50); // Membuat batas pemain.

            // --- Perbarui posisi deteksi keranjang ---
            // Hitung ulang batas keranjang karena ukurannya bisa disesuaikan di paintComponent.
            int basketWidthActual = BASKET_DISPLAY_WIDTH + 50;
            int basketHeightActual = BASKET_DISPLAY_HEIGHT + 50;
            int basketX = (getWidth() / 2) - (basketWidthActual / 2);
            int basketY = getHeight() - basketHeightActual - 50; // Mengacu pada perhitungan di paintComponent.

            Rectangle basketBounds = new Rectangle(basketX, basketY, basketWidthActual, basketHeightActual); // Membuat batas keranjang.
            // --- Akhir Perbarui posisi deteksi keranjang ---

            if (playerBounds.intersects(basketBounds)) { // Cek jika pemain berpotongan dengan keranjang.
                gameViewModel.collectHeldBall(heldBall); // Meminta ViewModel untuk memproses pengumpulan bola.
                heldBall = null; // Melepaskan bola dari pemain di sisi tampilan.
                heldBallOffsetAngle = 0; // Reset sudut offset bola yang dipegang.
            }
        }
    }

    /**
     * Menangani logika animasi ekstensi dan retraksi lasso.
     * Mengupdate `currentLassoDrawLength` dan `lassoDirection`.
     */
    private void handleLassoAnimation() {
        if (isLassoActive) { // Hanya berjalan jika lasso sedang aktif.
            Player player = gameViewModel.getPlayer();
            int playerCenterX = player.getX() + 25;
            int playerCenterY = player.getY() + 25;
            // Hitung sudut tembakan lasso berdasarkan posisi pemain dan target mouse.
            double angle = Math.atan2(mouseTargetY - playerCenterY, mouseTargetX - playerCenterX);

            if (lassoDirection == 1) { // Fase ekstensi (lasso memanjang).
                currentLassoDrawLength += LASSO_ANIMATION_SPEED; // Menambah panjang lasso.

                if (currentLassoDrawLength > targetLassoLength) {
                    currentLassoDrawLength = targetLassoLength; // Pastikan tidak melewati panjang target.
                }

                // Hitung posisi ujung lasso saat ini.
                int lassoTipX = (int) (playerCenterX + currentLassoDrawLength * Math.cos(angle));
                int lassoTipY = (int) (playerCenterY + currentLassoDrawLength * Math.sin(angle));

                int lassoTipSize = 8;
                Rectangle lassoTipBounds = new Rectangle(lassoTipX - lassoTipSize / 2, lassoTipY - lassoTipSize / 2, lassoTipSize, lassoTipSize); // Membuat batas untuk ujung lasso.

                // Memeriksa kolisi ujung lasso dengan bola di ViewModel.
                boolean ballCaught = gameViewModel.checkLassoTipCollision(lassoTipBounds, lassoTipX, lassoTipY);

                if (ballCaught) {
                    lassoDirection = -1; // Jika bola tertangkap, ubah arah menjadi retraksi.
                } else if (currentLassoDrawLength >= targetLassoLength) {
                    lassoDirection = -1; // Jika mencapai panjang target tanpa menangkap, juga ubah arah menjadi retraksi.
                }
            } else { // Fase retraksi (lasso menarik kembali).
                currentLassoDrawLength -= LASSO_ANIMATION_SPEED; // Mengurangi panjang lasso.
                
                // Hitung ulang posisi ujung lasso untuk menggambar bola yang ditarik.
                int lassoTipX = (int) (playerCenterX + currentLassoDrawLength * Math.cos(angle));
                int lassoTipY = (int) (playerCenterY + currentLassoDrawLength * Math.sin(angle));
                int lassoTipSize = 8;
                Rectangle lassoTipBounds = new Rectangle(lassoTipX - lassoTipSize / 2, lassoTipY - lassoTipSize / 2, lassoTipSize, lassoTipSize);
                
                // Panggil checkLassoTipCollision lagi jika animatingPulledBall null.
                // Ini mungkin untuk memastikan status 'isBeingPulled' tetap sinkron atau menangani kasus edge.
                if (animatingPulledBall == null) {
                     gameViewModel.checkLassoTipCollision(lassoTipBounds, lassoTipX, lassoTipY);
                }

                if (currentLassoDrawLength <= 0) { // Jika lasso sudah sepenuhnya kembali ke pemain.
                    currentLassoDrawLength = 0; // Pastikan panjangnya nol.
                    isLassoActive = false; // Lasso tidak lagi aktif.
                    lassoDirection = 1; // Reset arah untuk tembakan lasso berikutnya.
                    if (animatingPulledBall != null) { // Jika ada bola yang ditarik.
                        // Meminta ViewModel untuk memutuskan apakah bola harus dipegang oleh pemain.
                        boolean shouldHold = gameViewModel.attachBallToPlayer(animatingPulledBall);
                        if (shouldHold) {
                            heldBall = animatingPulledBall; // Atur bola yang dipegang jika ViewModel menyetujui.
                        } else {
                            heldBall = null; // Lepaskan bola (misalnya, jika itu bom).
                        }
                        animatingPulledBall = null; // Hapus referensi bola yang sedang dianimasikan.
                    }
                }
            }
        }
    }

    /**
     * Memulai animasi di mana bola yang tertangkap ditarik kembali ke pemain.
     * @param ball Bola yang tertangkap.
     * @param caughtX Koordinat X di mana bola tertangkap oleh lasso.
     * @param caughtY Koordinat Y di mana bola tertangkap oleh lasso.
     */
    public void startPullAnimation(Balls ball, int caughtX, int caughtY) {
        this.animatingPulledBall = ball; // Menentukan bola yang akan ditarik.
        Player player = gameViewModel.getPlayer();
        int playerCenterX = player.getX() + 25;
        int playerCenterY = player.getY() + 25;
        // Menghitung sudut offset awal bola yang ditarik relatif terhadap pemain.
        heldBallOffsetAngle = Math.atan2(caughtY - playerCenterY, caughtX - playerCenterX);
    }

    /**
     * Menghentikan game loop, menyimpan hasil game, mereset state,
     * dan mengalihkan tampilan kembali ke Main Menu.
     */
    public void stopGame() {
        gameLoop.stop(); // Menghentikan timer game loop.
        gameViewModel.saveGameResult(); // Menyimpan skor dan hasil game ke database atau file.

        resetGamePanelState(); // Mereset semua variabel state panel.

        mainFrame.switchToMainPanel(); // Beralih kembali ke panel menu utama.
    }

    /**
     * Menampilkan pesan dialog pop-up kepada pengguna.
     * @param title Judul dialog.
     * @param message Pesan yang akan ditampilkan.
     * @param messageType Tipe pesan (misalnya JOptionPane.INFORMATION_MESSAGE, JOptionPane.ERROR_MESSAGE).
     */
    public void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    /**
     * Inner class GameKeyListener menangani input dari keyboard untuk menggerakkan pemain.
     * Saat ini hanya mendukung gerakan 4 arah (horizontal dan vertikal) dan tidak ada gerakan diagonal.
     */
    private class GameKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            // Mengecek tombol panah atau WASD untuk pergerakan pemain.
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
                gameViewModel.movePlayer(-5, 0); // Panggil ViewModel untuk menggerakkan pemain 5px ke kiri.
                isPlayerMoving = true; // Set status pemain bergerak.
                currentPlayerAnimation = "walk_left"; // Set animasi berjalan ke kiri.
                lastPlayerDirection = "left"; // Simpan arah terakhir.
            } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
                gameViewModel.movePlayer(5, 0); // Panggil ViewModel untuk menggerakkan pemain 5px ke kanan.
                isPlayerMoving = true;
                currentPlayerAnimation = "walk_right";
                lastPlayerDirection = "right";
            } else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
                gameViewModel.movePlayer(0, -5); // Panggil ViewModel untuk menggerakkan pemain 5px ke atas.
                isPlayerMoving = true;
                currentPlayerAnimation = "walk_up";
                lastPlayerDirection = "up";
            } else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
                gameViewModel.movePlayer(0, 5); // Panggil ViewModel untuk menggerakkan pemain 5px ke bawah.
                isPlayerMoving = true;
                currentPlayerAnimation = "walk_down";
                lastPlayerDirection = "down";
            } else if (key == KeyEvent.VK_SPACE) {
                stopGame(); // Menghentikan game jika tombol spasi ditekan.
            }
        }
        @Override
        public void keyReleased(KeyEvent e) {
            // Ketika tombol keyboard dilepaskan, set isPlayerMoving menjadi false.
            // Ini akan memicu perubahan animasi ke idle di `actionPerformed`.
            isPlayerMoving = false;
        }
    }

    /**
     * Inner class GameMouseListener menangani input dari mouse, khususnya klik kiri untuk menembakkan lasso.
     */
    private class GameMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) { // Mengecek apakah itu klik kiri mouse.
                // Tembakkan lasso hanya jika tidak ada bola yang dipegang dan lasso tidak sedang aktif.
                if (heldBall == null && !isLassoActive) {
                    isLassoActive = true; // Aktifkan lasso.
                    currentLassoDrawLength = 0; // Reset panjang lasso ke nol.
                    lassoDirection = 1; // Atur arah lasso ke memanjang.

                    mouseTargetX = e.getX(); // Simpan koordinat X klik mouse sebagai target lasso.
                    mouseTargetY = e.getY(); // Simpan koordinat Y klik mouse sebagai target lasso.

                    Player player = gameViewModel.getPlayer();
                    int playerCenterX = player.getX() + 25;
                    int playerCenterY = player.getY() + 25;
                    // Hitung panjang target lasso berdasarkan jarak dari pemain ke titik klik mouse.
                    targetLassoLength = (int) Point2D.distance(playerCenterX, playerCenterY, mouseTargetX, mouseTargetY);
                }
            }
        }
    }
}