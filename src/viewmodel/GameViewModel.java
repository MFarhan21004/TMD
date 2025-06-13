import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import java.awt.Rectangle;

public class GameViewModel {
    private Player player;
    private List<Balls> Balls;
    private String currentUsername;
    private DatabaseModel databaseModel;
    private GamePanel gamePanel;

    private final int MAX_BALLS = 10;
    private final int BALL_SPAWN_INTERVAL = 80;
    private int spawnCounter = 0;
    private Random random;

    private final double BOMB_CHANCE = 0.3;
    private final double BONUS_Balls_CHANCE = 0.2;

    private final int BONUS_DURATION_TICKS = 600; 
    private final int MAX_BONUS_DURATION_TICKS = 1800; 

    public GameViewModel(String username, GamePanel gamePanel) {
        this.currentUsername = username;
        this.gamePanel = gamePanel;
        this.databaseModel = new DatabaseModel();
        this.random = new Random();
        this.Balls = new ArrayList<>();
    }

    public void initializeGame() {
        // Player Ballsts in the middle, using visual dimensions
        player = new Player(gamePanel.getWidth() / 2 - Player.VISUAL_WIDTH / 2, gamePanel.getHeight() / 2 - Player.VISUAL_HEIGHT / 2);
        Balls.clear();
        spawnCounter = 0;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Balls> getBalls() {
        return Balls;
    }

    public void movePlayer(int dx, int dy) {
        int speed = player.getCurrentSpeed(); 
        int newX = player.getX() + (dx > 0 ? speed : (dx < 0 ? -speed : 0));
        int newY = player.getY() + (dy > 0 ? speed : (dy < 0 ? -speed : 0));

        // Batasan pergerakan pemain (tetap gunakan VISUAL_WIDTH/HEIGHT)
        if (newX >= 0 && newX <= gamePanel.getWidth() - Player.VISUAL_WIDTH) {
            player.setX(newX);
        }
        if (newY >= 0 && newY <= gamePanel.getHeight() - Player.VISUAL_HEIGHT) {
            player.setY(newY);
        }
    }

    public void updateGame() {
        // NEW: Hitbox position calculation untuk pemain
        int hitboxX = player.getX() + (Player.VISUAL_WIDTH - Player.HITBOX_WIDTH) / 2;
        int hitboxY = player.getY() + (Player.VISUAL_HEIGHT - Player.HITBOX_HEIGHT) / 2;
        Rectangle playerHitbox = new Rectangle(hitboxX, hitboxY, Player.HITBOX_WIDTH, Player.HITBOX_HEIGHT); // <<< INI PERBAIKANNYA

        Iterator<Balls> iterator = Balls.iterator();
        while (iterator.hasNext()) {
            Balls ball = iterator.next();
            if (ball.isActive() && !ball.isBeingPulled() && !ball.isHeldByPlayer()) {
                ball.move();

                // Cek tabrakan pemain-bom untuk bom yang bergerak bebas
                if (ball.isBomb()) {
                    // Gunakan playerHitbox di sini
                    Rectangle bombBounds = new Rectangle(ball.getX(), ball.getY(), ball.getSize(), ball.getSize());
                    if (playerHitbox.intersects(bombBounds)) { // <<< GUNAKAN playerHitbox
                        if (player.isInvincible()) {
                            System.out.println("Bomb hit, but player is invincible!");
                            iterator.remove();
                        } else {
                            System.out.println("GAME OVER! Player hit a bomb!");
                            gamePanel.playBombEffectSound();
                            gamePanel.stopGame();
                            gamePanel.showMessage("GAME OVER!!!", "\nYour Final Score: " + player.getScore() + " points.\n                   Count: " + player.getCollectedBalls() +  " Star.", GamePanel.MESSAGE_TYPE_ERROR);
                            gamePanel.getMainFrame().switchToMainPanel();
                            return;
                        }
                    }
                }

                if (ball.isMovingRight() && ball.getX() > gamePanel.getWidth() + ball.getSize()) {
                    iterator.remove();
                } else if (!ball.isMovingRight() && ball.getX() < -ball.getSize()) {
                    iterator.remove();
                }
            }
        }

        // Update bonus durations
        if (player.isSpeedBoostActive()) {
            player.decreaseSpeedBoostDuration();
            if (player.getSpeedBoostDuration() <= 0) {
                player.setSpeedBoostActive(false);
                System.out.println("Speed boost ended.");
            }
        }
        if (player.isInvincible()) {
            player.decreaseInvincibilityDuration();
            if (player.getInvincibilityDuration() <= 0) {
                player.setInvincible(false);
                System.out.println("Invincibility ended.");
            }
        }
        if (!player.isSpeedBoostActive() && !player.isInvincible()) {
            gamePanel.stopBonusMusic();
        }


        spawnCounter++;
        if (spawnCounter >= BALL_SPAWN_INTERVAL && Balls.size() < MAX_BALLS) {
            spawnSkillBall();
            spawnCounter = 0;
        }
    }

    private void spawnSkillBall() {
        int BallstX;
        int BallstY;
        boolean movingRight = random.nextBoolean();

        boolean isBomb = false;
        boolean isBonusBalls = false;

        double randType = random.nextDouble();
        if (randType < BOMB_CHANCE) {
            isBomb = true;
        } else if (randType < BOMB_CHANCE + BONUS_Balls_CHANCE) {
            isBonusBalls = true;
        }

        if (movingRight) {
            BallstX = -50;
            BallstY = random.nextInt(gamePanel.getHeight() / 2 - 50);
        } else {
            BallstX = gamePanel.getWidth() + 50;
            BallstY = random.nextInt(gamePanel.getHeight() / 2 - 50) + gamePanel.getHeight() / 2;
        }

        Balls.add(new Balls(BallstX, BallstY, 2, 5, 40, 60, isBomb, isBonusBalls));
    }

    public boolean checkLassoTipCollision(Rectangle lassoTipBounds, int lassoTipX, int lassoTipY) {
        Balls caughtBall = null;
        for (Balls ball : Balls) {
            if (ball.isActive() && !ball.isBeingPulled() && !ball.isHeldByPlayer()) {
                Rectangle ballBounds = new Rectangle(ball.getX(), ball.getY(), ball.getSize(), ball.getSize());
                if (lassoTipBounds.intersects(ballBounds)) {
                    caughtBall = ball;
                    break;
                }
            }
        }

        if (caughtBall != null) {
            if (caughtBall.isBomb()) {
                caughtBall.setActive(false);
                caughtBall.setBeingPulled(true);
                System.out.println("Bomb caught by lasso tip! Pulling to player for explosion.");
                gamePanel.startPullAnimation(caughtBall, lassoTipX, lassoTipY);
                return true;
            } else { // It's a regular skill ball or bonus Balls
                caughtBall.setActive(false);
                caughtBall.setBeingPulled(true);
                System.out.println("Ball caught by lasso tip! Ballsting pull animation to player.");
                gamePanel.startPullAnimation(caughtBall, lassoTipX, lassoTipY);
                return true;
            }
        }
        return false;
    }

    public boolean attachBallToPlayer(Balls ball) {
        if (ball.isBomb()) {
            if (player.isInvincible()) {
                System.out.println("Bomb lassoed, but player is invincible! Bomb removed.");
                Balls.remove(ball);
            } else {
                System.out.println("GAME OVER! Bomb exploded at player!");
                gamePanel.playBombEffectSound(); // Putar suara efek bom
                gamePanel.stopGame(); // Hentikan logika game

                // --- Peningkatan Tampilan GAME OVER (TANPA IKON KUSTOM) ---
                JOptionPane.showMessageDialog(
                    gamePanel, // Parent component
                    "A bomb exploded on you!\nYour Final Score: " + player.getScore() + " points.\n                   Count: " + player.getCollectedBalls() +  " Star.", // Pesan
                    "GAME OVER!", // Judul dialog
                    JOptionPane.ERROR_MESSAGE // Menggunakan tipe ERROR_MESSAGE untuk ikon silang merah standar
                );
                // --- Akhir Peningkatan Tampilan GAME OVER ---
                gamePanel.getMainFrame().switchToMainPanel();
                Balls.remove(ball); // Hapus bola bom
        
            }
            return false;
        } else if (ball.isBonusStar()) {
            System.out.println("BONUS Balls COLLECTED! Speed boost & Invincibility!");
            gamePanel.playBonusMusic();

            player.addScore(ball.getValue() * 2);

            int currentSpeedBoostDuration = player.getSpeedBoostDuration();
            int currentInvincibilityDuration = player.getInvincibilityDuration();

            player.setSpeedBoostActive(true);
            player.setInvincible(true);
            
            player.setSpeedBoostDuration(Math.min(currentSpeedBoostDuration + BONUS_DURATION_TICKS, MAX_BONUS_DURATION_TICKS));
            player.setInvincibilityDuration(Math.min(currentInvincibilityDuration + BONUS_DURATION_TICKS, MAX_BONUS_DURATION_TICKS));

            Balls.remove(ball);
            return false;
        }

        ball.setBeingPulled(false);
        ball.setHeldByPlayer(true);
        System.out.println("Ball attached to player.");
        return true;
    }

    public void collectHeldBall(Balls ball) {
        if (ball.isBomb()) {
            System.out.println("GAME OVER! Brought bomb to basket!");
            gamePanel.playBombEffectSound();
            gamePanel.stopGame();
            gamePanel.showMessage("GAME OVER", "You brought a bomb to the basket! Score: " + player.getScore(), GamePanel.MESSAGE_TYPE_ERROR);
            Balls.remove(ball);
            return;
        }
        if (ball.isBonusStar()) {
             System.out.println("Bonus Balls (shouldn't be here) deposited. Removing.");
             Balls.remove(ball);
             return;
        }

        player.addScore(ball.getValue());
        player.addCollectedBall();
        ball.setHeldByPlayer(false);
        Balls.remove(ball);
        System.out.println("Ball deposited! Score: " + player.getScore() + ", Balls: " + player.getCollectedBalls());
    }

    public void saveGameResult() {
        databaseModel.saveThasil(currentUsername, player.getScore(), player.getCollectedBalls());
    }
}