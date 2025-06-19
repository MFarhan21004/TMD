import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import java.awt.Rectangle;

/**
 * GameViewModel adalah kelas yang mengelola logika game inti,
 * bertindak sebagai "ViewModel" dalam arsitektur MVVM.
 * Ini bertanggung jawab untuk memperbarui state game, menangani interaksi antar objek,
 * mengelola skor, dan berkomunikasi dengan DatabaseModel dan GamePanel.
 */
public class GameViewModel {
    private Player player; // Objek pemain dalam game.
    private List<Balls> Balls; // List dari semua objek 'Balls' (bintang, bom, bonus) di layar.
    private String currentUsername; // Username pemain saat ini.
    private DatabaseModel databaseModel; // Model untuk interaksi dengan database (menyimpan skor).
    private GamePanel gamePanel; // Referensi ke GamePanel (View) untuk interaksi UI dan sound.

    private final int MAX_BALLS = 10; // Jumlah maksimum bola yang bisa ada di layar pada satu waktu.
    private final int BALL_SPAWN_INTERVAL = 50; // Interval (dalam tick game) untuk memunculkan bola baru.
    private int spawnCounter = 0; // Counter untuk melacak kapan bola baru harus muncul.
    private Random random; // Objek Random untuk menghasilkan nilai acak (posisi, jenis bola).

    private final double BOMB_CHANCE = 0.3; // Probabilitas munculnya bom (30%).
    private final double BONUS_Balls_CHANCE = 0.2; // Probabilitas munculnya bola bonus (20%).

    private final int BONUS_DURATION_TICKS = 600; // Durasi dasar efek bonus dalam tick game (sekitar 10 detik jika 60 tick/detik).
    private final int MAX_BONUS_DURATION_TICKS = 1800; // Durasi maksimum efek bonus yang bisa diakumulasi (sekitar 30 detik).

    /**
     * Konstruktor untuk GameViewModel.
     * Menginisialisasi username, referensi GamePanel, DatabaseModel, dan Random.
     * @param username Username pemain saat ini.
     * @param gamePanel Referensi ke GamePanel yang terkait.
     */
    public GameViewModel(String username, GamePanel gamePanel) {
        this.currentUsername = username;
        this.gamePanel = gamePanel;
        this.databaseModel = new DatabaseModel(); // Inisialisasi DatabaseModel.
        this.random = new Random(); // Inisialisasi Random.
        this.Balls = new ArrayList<>(); // Inisialisasi list Balls.
    }

    /**
     * Menginisialisasi ulang state game untuk memulai permainan baru.
     * Menempatkan pemain di tengah dan membersihkan semua bola yang ada.
     */
    public void initializeGame() {
        // Menempatkan pemain di tengah layar, menggunakan dimensi visual pemain dari kelas Player.
        player = new Player(gamePanel.getWidth() / 2 - Player.VISUAL_WIDTH / 2, gamePanel.getHeight() / 2 - Player.VISUAL_HEIGHT / 2);
        Balls.clear(); // Mengosongkan list bola.
        spawnCounter = 0; // Mereset counter spawn.
    }

    /**
     * Mengembalikan objek Player.
     * @return Objek Player saat ini.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Mengembalikan list objek Balls.
     * @return List dari semua objek Balls di layar.
     */
    public List<Balls> getBalls() {
        return Balls;
    }

    /**
     * Menggerakkan pemain berdasarkan perubahan dx dan dy yang diberikan.
     * Kecepatan pergerakan disesuaikan dengan `player.getCurrentSpeed()`.
     * Pergerakan dibatasi agar pemain tetap berada di dalam batas panel.
     * @param dx Perubahan posisi horizontal yang diinginkan (arah: -1 kiri, 1 kanan, 0 tidak bergerak).
     * @param dy Perubahan posisi vertikal yang diinginkan (arah: -1 atas, 1 bawah, 0 tidak bergerak).
     */
    public void movePlayer(int dx, int dy) {
        int speed = player.getCurrentSpeed(); // Mendapatkan kecepatan pemain saat ini (bisa berubah karena bonus).
        // Menghitung posisi X baru. Jika dx > 0 (kanan), tambahkan speed; jika dx < 0 (kiri), kurangi speed.
        int newX = player.getX() + (dx > 0 ? speed : (dx < 0 ? -speed : 0));
        // Menghitung posisi Y baru. Jika dy > 0 (bawah), tambahkan speed; jika dy < 0 (atas), kurangi speed.
        int newY = player.getY() + (dy > 0 ? speed : (dy < 0 ? -speed : 0));

        // Batasan pergerakan pemain di sumbu X. Pemain harus tetap di dalam lebar panel.
        // Menggunakan VISUAL_WIDTH dari kelas Player untuk perhitungan batas kanan.
        if (newX >= 0 && newX <= gamePanel.getWidth() - Player.VISUAL_WIDTH) {
            player.setX(newX);
        }
        // Batasan pergerakan pemain di sumbu Y. Pemain harus tetap di dalam tinggi panel.
        // Menggunakan VISUAL_HEIGHT dari kelas Player untuk perhitungan batas bawah.
        if (newY >= 0 && newY <= gamePanel.getHeight() - Player.VISUAL_HEIGHT) {
            player.setY(newY);
        }
    }

    /**
     * Metode utama untuk memperbarui state game di setiap tick.
     * Menggerakkan bola, memeriksa tabrakan, memperbarui durasi bonus,
     * dan memicu spawn bola baru.
     */
    public void updateGame() {
    
        // Hitbox pemain diletakkan di tengah visual pemain.
        int hitboxX = player.getX() + (Player.VISUAL_WIDTH - Player.HITBOX_WIDTH) / 2;
        int hitboxY = player.getY() + (Player.VISUAL_HEIGHT - Player.HITBOX_HEIGHT) / 2;
        Rectangle playerHitbox = new Rectangle(hitboxX, hitboxY, Player.HITBOX_WIDTH, Player.HITBOX_HEIGHT); // Membuat objek Rectangle untuk hitbox pemain.

        // Menggunakan Iterator untuk mengiterasi dan menghapus bola dengan aman.
        Iterator<Balls> iterator = Balls.iterator();
        while (iterator.hasNext()) {
            Balls ball = iterator.next();
            // Memproses bola hanya jika aktif, tidak sedang ditarik, dan tidak dipegang pemain.
            if (ball.isActive() && !ball.isBeingPulled() && !ball.isHeldByPlayer()) {
                ball.move(); // Menggerakkan bola.

                // Cek tabrakan pemain-bom untuk bom yang bergerak bebas.
                if (ball.isBomb()) {
                    Rectangle bombBounds = new Rectangle(ball.getX(), ball.getY(), ball.getSize(), ball.getSize()); // Membuat batas bom.
                    if (playerHitbox.intersects(bombBounds)) { // <<< GUNAKAN playerHitbox untuk deteksi tabrakan.
                        if (player.isInvincible()) {
                            // Jika pemain kebal, bom dihapus tanpa efek negatif.
                            System.out.println("Bomb hit, but player is invincible!");
                            iterator.remove(); // Hapus bom.
                        } else {
                            // Jika pemain tidak kebal dan terkena bom, game over.
                            System.out.println("GAME OVER! Player hit a bomb!");
                            gamePanel.playBombEffectSound(); // Memutar efek suara bom.
                            gamePanel.stopGame(); // Menghentikan game loop dan menyimpan hasil.
                            // Menampilkan pesan GAME OVER dengan skor akhir dan jumlah bintang yang dikumpulkan.
                            gamePanel.showMessage("GAME OVER!!!", "\nYour Final Score: " + player.getScore() + " points.\n           Count: " + player.getCollectedBalls() +  " Star.", GamePanel.MESSAGE_TYPE_ERROR);
                            gamePanel.getMainFrame().switchToMainPanel(); // Kembali ke menu utama.
                            return; // Keluar dari metode updateGame karena game sudah berakhir.
                        }
                    }
                }

                // Menghapus bola jika sudah keluar dari layar.
                // Logika ini untuk bola yang bergerak dari kiri ke kanan.
                if (ball.isMovingRight() && ball.getX() > gamePanel.getWidth() + ball.getSize()) {
                    iterator.remove();
                } // Logika ini untuk bola yang bergerak dari kanan ke kiri.
                else if (!ball.isMovingRight() && ball.getX() < -ball.getSize()) {
                    iterator.remove();
                }
            }
        }

        // Memperbarui durasi efek bonus pemain.
        if (player.isSpeedBoostActive()) {
            player.decreaseSpeedBoostDuration(); // Mengurangi durasi speed boost.
            if (player.getSpeedBoostDuration() <= 0) {
                player.setSpeedBoostActive(false); // Nonaktifkan speed boost jika durasi habis.
                System.out.println("Speed boost ended.");
            }
        }
        if (player.isInvincible()) {
            player.decreaseInvincibilityDuration(); // Mengurangi durasi invincibility.
            if (player.getInvincibilityDuration() <= 0) {
                player.setInvincible(false); // Nonaktifkan invincibility jika durasi habis.
                System.out.println("Invincibility ended.");
            }
        }
        // Jika tidak ada bonus yang aktif, hentikan musik bonus.
        if (!player.isSpeedBoostActive() && !player.isInvincible()) {
            gamePanel.stopBonusMusic();
        }

        // Logika spawning bola.
        spawnCounter++; // Meningkatkan counter spawn.
        // Jika sudah waktunya spawn dan jumlah bola belum mencapai maksimum, spawn bola baru.
        if (spawnCounter >= BALL_SPAWN_INTERVAL && Balls.size() < MAX_BALLS) {
            spawnSkillBall(); // Memanggil metode untuk memunculkan bola.
            spawnCounter = 0; // Mereset counter spawn.
        }
    }

    /**
     * Memunculkan bola baru (skill ball, bom, atau bonus ball) secara acak.
     * Bola muncul dari sisi kiri atau kanan layar dan bergerak menuju sisi berlawanan.
     */
    private void spawnSkillBall() {
        int BallstX; // Posisi X awal bola.
        int BallstY; // Posisi Y awal bola.
        boolean movingRight = random.nextBoolean(); // Tentukan arah gerak bola secara acak.

        boolean isBomb = false; // Flag untuk menentukan apakah bola adalah bom.
        boolean isBonusBalls = false; // Flag untuk menentukan apakah bola adalah bonus.

        double randType = random.nextDouble(); // Menghasilkan angka acak untuk menentukan jenis bola.
        if (randType < BOMB_CHANCE) {
            isBomb = true; // Jika dalam probabilitas bom, set sebagai bom.
        } else if (randType < BOMB_CHANCE + BONUS_Balls_CHANCE) {
            isBonusBalls = true; // Jika dalam probabilitas bonus (setelah probabilitas bom), set sebagai bonus.
        }

        // Menentukan posisi spawn dan arah awal.
        if (movingRight) {
            BallstX = -50; // Muncul dari kiri luar layar.
            // Posisi Y acak di paruh atas layar (asumsi paruh atas untuk bola bergerak ke kanan).
            BallstY = random.nextInt(gamePanel.getHeight() / 2 - 50); 
        } else {
            BallstX = gamePanel.getWidth() + 50; // Muncul dari kanan luar layar.
            // Posisi Y acak di paruh bawah layar (asumsi paruh bawah untuk bola bergerak ke kiri).
            BallstY = random.nextInt(gamePanel.getHeight() / 2 - 50) + gamePanel.getHeight() / 2;
        }

        // Menambahkan objek Balls baru ke list.
        // Parameter: x, y, speed, value, minSize, maxSize, isBomb, isBonusStar.
        Balls.add(new Balls(BallstX, BallstY, 2, 5, 40, 60, isBomb, isBonusBalls));
    }

    /**
     * Memeriksa apakah ujung lasso (kail) bertabrakan dengan bola yang aktif dan bebas.
     * Jika ada tabrakan, bola ditandai sebagai 'being pulled' dan animasi penarikan dimulai.
     * @param lassoTipBounds Batasan (Rectangle) dari ujung lasso.
     * @param lassoTipX Posisi X ujung lasso.
     * @param lassoTipY Posisi Y ujung lasso.
     * @return true jika bola tertangkap, false jika tidak.
     */
    public boolean checkLassoTipCollision(Rectangle lassoTipBounds, int lassoTipX, int lassoTipY) {
        Balls caughtBall = null;
        // Iterasi melalui semua bola untuk mencari tabrakan.
        for (Balls ball : Balls) {
            // Cek hanya bola yang aktif, tidak sedang ditarik, dan tidak dipegang.
            if (ball.isActive() && !ball.isBeingPulled() && !ball.isHeldByPlayer()) {
                Rectangle ballBounds = new Rectangle(ball.getX(), ball.getY(), ball.getSize(), ball.getSize()); // Membuat batas bola.
                if (lassoTipBounds.intersects(ballBounds)) { // Cek tabrakan antara ujung lasso dan bola.
                    caughtBall = ball; // Jika bertabrakan, simpan referensi bola.
                    break; // Keluar dari loop karena hanya ingin menangkap satu bola per tembakan.
                }
            }
        }

        if (caughtBall != null) { // Jika ada bola yang tertangkap.
            if (caughtBall.isBomb()) {
                // Jika yang tertangkap adalah bom, tandai tidak aktif, sedang ditarik, dan mulai animasi penarikan.
                caughtBall.setActive(false); // Bom tidak lagi berinteraksi sebagai objek bergerak.
                caughtBall.setBeingPulled(true); // Tandai sedang ditarik.
                System.out.println("Bomb caught by lasso tip! Pulling to player for explosion.");
                gamePanel.startPullAnimation(caughtBall, lassoTipX, lassoTipY); // Memicu animasi penarikan di GamePanel.
                return true; // Bola tertangkap.
            } else { // Ini adalah bola skill biasa atau bola bonus.
                // Tandai tidak aktif, sedang ditarik, dan mulai animasi penarikan.
                caughtBall.setActive(false);
                caughtBall.setBeingPulled(true);
                System.out.println("Ball caught by lasso tip! Starting pull animation to player.");
                gamePanel.startPullAnimation(caughtBall, lassoTipX, lassoTipY);
                return true; // Bola tertangkap.
            }
        }
        return false; // Tidak ada bola yang tertangkap.
    }

    /**
     * Memproses bola setelah ditarik kembali ke pemain.
     * Menangani efek bom, bonus, atau menempelkan bola skill ke pemain.
     * @param ball Bola yang telah ditarik kembali ke pemain.
     * @return true jika bola berhasil ditempelkan ke pemain, false jika tidak (misalnya bom/bonus).
     */
    public boolean attachBallToPlayer(Balls ball) {
        if (ball.isBomb()) { // Jika bola adalah bom.
            if (player.isInvincible()) {
                // Jika pemain kebal, bom dihapus tanpa efek.
                System.out.println("Bomb lassoed, but player is invincible! Bomb removed.");
                Balls.remove(ball); // Hapus bom dari list.
            } else {
                // Jika pemain tidak kebal, game over karena bom meledak.
                System.out.println("GAME OVER! Bomb exploded at player!");
                gamePanel.playBombEffectSound(); // Putar suara efek bom.
                gamePanel.stopGame(); // Hentikan logika game dan simpan hasil.

                // --- Peningkatan Tampilan GAME OVER (TANPA IKON KUSTOM) ---
                // Menampilkan dialog pesan GAME OVER.
                JOptionPane.showMessageDialog(
                    gamePanel, // Parent component untuk dialog.
                    "A bomb exploded on you!\nYour Final Score: " + player.getScore() + " points.\n           Count: " + player.getCollectedBalls() +  " Star.", // Pesan yang ditampilkan.
                    "GAME OVER!", // Judul dialog.
                    JOptionPane.ERROR_MESSAGE // Menggunakan tipe ERROR_MESSAGE untuk menampilkan ikon silang merah standar.
                );
                // --- Akhir Peningkatan Tampilan GAME OVER ---
                gamePanel.getMainFrame().switchToMainPanel(); // Kembali ke menu utama setelah dialog.
                Balls.remove(ball); // Hapus bola bom dari list.
            }
            return false; // Bom tidak ditempelkan.
        } else if (ball.isBonusStar()) { // Jika bola adalah bintang bonus.
            System.out.println("BONUS Balls COLLECTED! Speed boost & Invincibility!");
            gamePanel.playBonusMusic(); // Memutar musik bonus.

            player.addScore(ball.getValue()); // Menambahkan skor bonus.
            // Mendapatkan durasi bonus yang sudah aktif.
            int currentSpeedBoostDuration = player.getSpeedBoostDuration();
            int currentInvincibilityDuration = player.getInvincibilityDuration();

            player.setSpeedBoostActive(true); // Mengaktifkan speed boost.
            player.setInvincible(true); // Mengaktifkan invincibility.
            
            // Menambahkan durasi bonus dan memastikan tidak melebihi batas maksimum.
            player.setSpeedBoostDuration(Math.min(currentSpeedBoostDuration + BONUS_DURATION_TICKS, MAX_BONUS_DURATION_TICKS));
            player.setInvincibilityDuration(Math.min(currentInvincibilityDuration + BONUS_DURATION_TICKS, MAX_BONUS_DURATION_TICKS));

            Balls.remove(ball); // Hapus bola bonus dari list.
            return false; // Bola bonus tidak ditempelkan.
        }

        // Jika bukan bom atau bonus, itu adalah bola skill biasa yang akan dipegang pemain.
        ball.setBeingPulled(false); // Berhenti menandai sebagai sedang ditarik.
        ball.setHeldByPlayer(true); // Tandai bola dipegang oleh pemain.
        System.out.println("Ball attached to player.");
        return true; // Bola berhasil ditempelkan.
    }

    /**
     * Memproses bola yang dipegang pemain saat pemain mencapai keranjang.
     * Menambahkan skor, jumlah bola terkumpul, dan menghapus bola.
     * Menangani kasus khusus jika bom dibawa ke keranjang.
     * @param ball Bola yang akan dikumpulkan.
     */
    public void collectHeldBall(Balls ball) {
        if (ball.isBomb()) { // Jika bola yang dibawa ke keranjang adalah bom.
            System.out.println("GAME OVER! Brought bomb to basket!");
            gamePanel.playBombEffectSound(); // Putar efek suara bom.
            gamePanel.stopGame(); // Hentikan game.
            gamePanel.showMessage("GAME OVER", "You brought a bomb to the basket! Score: " + player.getScore(), GamePanel.MESSAGE_TYPE_ERROR); // Tampilkan pesan game over.
            Balls.remove(ball); // Hapus bom.
            return; // Keluar dari metode.
        }
        if (ball.isBonusStar()) { // Kasus ini seharusnya tidak terjadi jika bonus ball langsung diproses di attachBallToPlayer.
                                  // Ini adalah safety check.
            System.out.println("Bonus Balls (shouldn't be here) deposited. Removing.");
            Balls.remove(ball);
            return;
        }

        // Jika bola adalah skill ball biasa.
        player.addScore(ball.getValue());  // Tambahkan skor pemain berdasarkan nilai bola.
        player.addCollectedBall();         // Tambahkan jumlah bola yang dikumpulkan pemain.
        ball.setHeldByPlayer(false); // Tandai bola tidak lagi dipegang pemain.
        Balls.remove(ball); // Hapus bola dari list.
        System.out.println("Ball deposited! Score: " + player.getScore() + ", Balls: " + player.getCollectedBalls()); // Log informasi.
    }

    /**
     * Menyimpan hasil akhir game (username, skor, jumlah bola terkumpul) ke database.
     */
    public void saveGameResult() {
        databaseModel.saveThasil(currentUsername, player.getScore(), player.getCollectedBalls()); // Panggil metode save di DatabaseModel.
    }
}