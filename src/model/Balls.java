import java.awt.Color;
import java.util.Random;

public class Balls {
    // Posisi koordinat saat ini
    private int x;
    private int y;
    
    // Posisi awal (digunakan untuk animasi menarik bola ke pemain)
    private int originalX;
    private int originalY;

    // Nilai angka pada bola (1–10)
    private int value;

    // Ukuran diameter bola
    private int size;

    // Warna bola (digunakan jika asset gambar gagal dimuat)
    private Color color;

    // Status apakah bola masih aktif (belum ditangkap/dihilangkan)
    private boolean active;

    // True jika bola sedang dalam proses ditarik oleh lasso (bergerak ke pemain)
    private boolean isBeingPulled;

    // True jika bola sudah sampai ke pemain dan sedang dibawa
    private boolean isHeldByPlayer;

    // True jika bola ini adalah bom
    private boolean isBomb;

    // True jika bola ini adalah bintang bonus
    private boolean isBonusStar;

    // Kecepatan gerak bola
    private int speed;

    // Arah gerakan bola (true: ke kanan, false: ke kiri)
    private boolean movingRight;

    // Konstruktor dengan parameter lengkap termasuk jenis bom dan bonus
    public Balls(int startX, int startY, int minSpeed, int maxSpeed, int minSize, int maxSize, boolean isBomb, boolean isBonusStar) {
        this.x = startX;
        this.y = startY;
        this.originalX = startX;
        this.originalY = startY;

        Random rand = new Random();

        // Nilai acak antara 1 hingga 10
        this.value = rand.nextInt(10) + 1;

        // Ukuran acak antara minSize dan maxSize
        this.size = rand.nextInt(maxSize - minSize + 1) + minSize;

        this.isBomb = isBomb;
        this.isBonusStar = isBonusStar;

        // Tentukan warna berdasarkan tipe bola
        if (this.isBomb) {
            this.color = Color.RED; // Warna untuk bom
        } else if (this.isBonusStar) {
            this.color = Color.MAGENTA; // Warna untuk bintang bonus
        } else {
            this.color = generateRandomColor(); // Warna acak untuk bola biasa
        }

        this.active = true;
        this.isBeingPulled = false;
        this.isHeldByPlayer = false;

        // Kecepatan acak dalam rentang tertentu
        this.speed = rand.nextInt(maxSpeed - minSpeed + 1) + minSpeed;

        // Atur arah gerak awal berdasarkan posisi Y
        this.movingRight = (startY < 300);
    }

    // Fungsi untuk menghasilkan warna acak RGB
    private Color generateRandomColor() {
        Random rand = new Random();
        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();
        return new Color(r, g, b);
    }

    // Getter dan Setter untuk atribut-atribut bola
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getOriginalX() { return originalX; }
    public int getOriginalY() { return originalY; }
    public void setOriginalPosition(int x, int y) { this.originalX = x; this.originalY = y; }
    public int getValue() { return value; }
    public int getSize() { return size; }
    public Color getColor() { return color; } // Warna fallback untuk rendering
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isBeingPulled() { return isBeingPulled; }
    public void setBeingPulled(boolean beingPulled) { this.isBeingPulled = beingPulled; }
    public boolean isHeldByPlayer() { return isHeldByPlayer; }
    public void setHeldByPlayer(boolean isHeldByPlayer) { this.isHeldByPlayer = isHeldByPlayer; }
    public boolean isBomb() { return isBomb; }
    public boolean isBonusStar() { return isBonusStar; } // Getter untuk status bonus star

    // Fungsi untuk menggerakkan bola ke kanan atau kiri tergantung arah
    public void move() {
        if (movingRight) { x += speed; } else { x -= speed; }
    }

    public boolean isMovingRight() { return movingRight; }
    public int getSpeed() { return speed; }
}
