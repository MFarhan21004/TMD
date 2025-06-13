public class Player {
    private int x;
    private int y;
    private int score;
    private int collectedBalls;

    // Properti untuk efek bonus
    private boolean isInvincible;
    private boolean isSpeedBoostActive;
    private int speedBoostDuration; // Durasi dalam game ticks
    private int invincibilityDuration; // Durasi dalam game ticks

    // NEW: Kecepatan pemain
    private final int BASE_SPEED = 7; // Kecepatan dasar pemain
    private final int BOOST_SPEED = 15; // Kecepatan saat bonus aktif

    public static final int VISUAL_WIDTH = 65; // Ukuran lebar visual pemain
    public static final int VISUAL_HEIGHT = 65; // Ukuran tinggi visual pemain

    // NEW: Ukuran hitbox pemain
    public static final int HITBOX_WIDTH = 25; // <<< Kurangi nilai ini untuk memperkecil lebar hitbox
    public static final int HITBOX_HEIGHT = 25; // <<< Kurangi nilai ini untuk memperkecil tinggi hitbox

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.score = 0;
        this.collectedBalls = 0;
        // Inisialisasi properti bonus
        this.isInvincible = false;
        this.isSpeedBoostActive = false;
        this.speedBoostDuration = 0;
        this.invincibilityDuration = 0;
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getScore() { return score; }
    public void addScore(int points) { this.score += points; }
    public int getCollectedBalls() { return collectedBalls; }
    public void addCollectedBall() { this.collectedBalls++; }

    // Getters dan Setters untuk properti bonus

    public boolean isInvincible() { return isInvincible; }
    public void setInvincible(boolean invincible) { isInvincible = invincible; }

    public boolean isSpeedBoostActive() { return isSpeedBoostActive; }
    public void setSpeedBoostActive(boolean speedBoostActive) { isSpeedBoostActive = speedBoostActive; }

    public int getSpeedBoostDuration() { return speedBoostDuration; }
    public void setSpeedBoostDuration(int speedBoostDuration) { this.speedBoostDuration = speedBoostDuration; }
    public void decreaseSpeedBoostDuration() { if (this.speedBoostDuration > 0) this.speedBoostDuration--; }

    public int getInvincibilityDuration() { return invincibilityDuration; }
    public void setInvincibilityDuration(int invincibilityDuration) { this.invincibilityDuration = invincibilityDuration; }
    public void decreaseInvincibilityDuration() { if (this.invincibilityDuration > 0) this.invincibilityDuration--; }

    // NEW: Metode untuk mendapatkan kecepatan pemain saat ini
    public int getCurrentSpeed() {
        return isSpeedBoostActive ? BOOST_SPEED : BASE_SPEED;
    }
}