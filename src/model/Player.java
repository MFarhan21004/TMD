public class Player {
    // Posisi koordinat pemain di layar
    private int x;
    private int y;

    // Skor total yang dikumpulkan pemain
    private int score;

    // Jumlah bola yang berhasil dikoleksi
    private int collectedBalls;

    // === Properti terkait efek bonus ===

    // True jika pemain sedang dalam mode kebal (invincible)
    private boolean isInvincible;

    // True jika pemain sedang mendapatkan peningkatan kecepatan (boost)
    private boolean isSpeedBoostActive;

    // Durasi efek speed boost dalam tick (misalnya frame update)
    private int speedBoostDuration;

    // Durasi efek invincibility dalam tick
    private int invincibilityDuration;

    // === Konstanta terkait kecepatan dan ukuran visual ===

    // Kecepatan normal pemain
    private final int BASE_SPEED = 7;

    // Kecepatan saat efek boost aktif
    private final int BOOST_SPEED = 15;

    // Ukuran visual pemain (misalnya untuk gambar sprite)
    public static final int VISUAL_WIDTH = 65;
    public static final int VISUAL_HEIGHT = 65;

    // Ukuran hitbox pemain (digunakan untuk deteksi tabrakan)
    public static final int HITBOX_WIDTH = 25;  // Disarankan dikurangi agar deteksi tidak terlalu ketat
    public static final int HITBOX_HEIGHT = 25;

    // Konstruktor untuk inisialisasi posisi dan status pemain
    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.score = 0;
        this.collectedBalls = 0;

        // Inisialisasi efek bonus
        this.isInvincible = false;
        this.isSpeedBoostActive = false;
        this.speedBoostDuration = 0;
        this.invincibilityDuration = 0;
    }

    // === Getter dan Setter standar untuk posisi dan skor ===

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getScore() { return score; }
    public void addScore(int points) { this.score += points; }

    public int getCollectedBalls() { return collectedBalls; }
    public void addCollectedBall() { this.collectedBalls++; }

    // === Getter dan Setter untuk efek bonus ===

    public boolean isInvincible() { return isInvincible; }
    public void setInvincible(boolean invincible) { isInvincible = invincible; }

    public boolean isSpeedBoostActive() { return isSpeedBoostActive; }
    public void setSpeedBoostActive(boolean speedBoostActive) { isSpeedBoostActive = speedBoostActive; }

    public int getSpeedBoostDuration() { return speedBoostDuration; }
    public void setSpeedBoostDuration(int speedBoostDuration) { this.speedBoostDuration = speedBoostDuration; }

    // Mengurangi durasi efek boost per tick
    public void decreaseSpeedBoostDuration() {
        if (this.speedBoostDuration > 0) this.speedBoostDuration--;
    }

    public int getInvincibilityDuration() { return invincibilityDuration; }
    public void setInvincibilityDuration(int invincibilityDuration) { this.invincibilityDuration = invincibilityDuration; }

    // Mengurangi durasi efek invincibility per tick
    public void decreaseInvincibilityDuration() {
        if (this.invincibilityDuration > 0) this.invincibilityDuration--;
    }

    // Mendapatkan kecepatan saat ini berdasarkan status boost
    public int getCurrentSpeed() {
        return isSpeedBoostActive ? BOOST_SPEED : BASE_SPEED;
    }
}
