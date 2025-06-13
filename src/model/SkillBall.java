import java.awt.Color;
import java.util.Random;

public class SkillBall {
    private int x;
    private int y;
    private int originalX; // Store original position for animation start
    private int originalY; // Store original position for animation start
    private int value;
    private int size;
    private Color color;
    private boolean active;
    private boolean isBeingPulled; // True if the ball is currently being pulled by lasso (animating to player)
    private boolean isHeldByPlayer; // True if the ball has reached the player and is being carried
    private boolean isBomb;       // True if this ball is a bomb
    private boolean isBonusStar;  // NEW: True if this ball is a bonus star
    private int speed;
    private boolean movingRight; // Direction of movement

    // Modified constructor to accept isBomb and isBonusStar parameters
    public SkillBall(int startX, int startY, int minSpeed, int maxSpeed, int minSize, int maxSize, boolean isBomb, boolean isBonusStar) {
        this.x = startX;
        this.y = startY;
        this.originalX = startX;
        this.originalY = startY;
        Random rand = new Random();
        this.value = rand.nextInt(10) + 1;
        this.size = rand.nextInt(maxSize - minSize + 1) + minSize;
        this.isBomb = isBomb;
        this.isBonusStar = isBonusStar; // Set bonus star status

        // Set color based on type (for fallback drawing if image assets fail)
        if (this.isBomb) {
            this.color = Color.RED;
        } else if (this.isBonusStar) {
            this.color = Color.MAGENTA; // Or any distinct color for bonus star fallback
        } else {
            this.color = generateRandomColor();
        }

        this.active = true;
        this.isBeingPulled = false;
        this.isHeldByPlayer = false;
        this.speed = rand.nextInt(maxSpeed - minSpeed + 1) + minSpeed;
        this.movingRight = (startY < 300);
    }

    private Color generateRandomColor() {
        Random rand = new Random();
        float r = rand.nextFloat();
        float g = rand.nextFloat();
        float b = rand.nextFloat();
        return new Color(r, g, b);
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getOriginalX() { return originalX; }
    public int getOriginalY() { return originalY; }
    public void setOriginalPosition(int x, int y) { this.originalX = x; this.originalY = y; }
    public int getValue() { return value; }
    public int getSize() { return size; }
    public Color getColor() { return color; } // This will return RED for bombs, MAGENTA for bonus, etc.
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isBeingPulled() { return isBeingPulled; }
    public void setBeingPulled(boolean beingPulled) { this.isBeingPulled = beingPulled; }
    public boolean isHeldByPlayer() { return isHeldByPlayer; }
    public void setHeldByPlayer(boolean isHeldByPlayer) { this.isHeldByPlayer = isHeldByPlayer; }
    public boolean isBomb() { return isBomb; }
    public boolean isBonusStar() { return isBonusStar; } // NEW: Getter for isBonusStar
    public void move() {
        if (movingRight) { x += speed; } else { x -= speed; }
    }
    public boolean isMovingRight() { return movingRight; }
    public int getSpeed() { return speed; }
}