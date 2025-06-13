import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.awt.Rectangle;

public class GameViewModel {
    private Player player;
    private List<SkillBall> skillBalls;
    private String currentUsername;
    private DatabaseModel databaseModel;
    private GamePanel gamePanel;

    private final int MAX_BALLS = 10;
    private final int BALL_SPAWN_INTERVAL = 100;
    private int spawnCounter = 0;
    private Random random;

    private final double BOMB_CHANCE = 0.2;
    private final double BONUS_STAR_CHANCE = 0.15;

    private final int BONUS_DURATION_TICKS = 600; 
    private final int MAX_BONUS_DURATION_TICKS = 1800; 

    public GameViewModel(String username, GamePanel gamePanel) {
        this.currentUsername = username;
        this.gamePanel = gamePanel;
        this.databaseModel = new DatabaseModel();
        this.random = new Random();
        this.skillBalls = new ArrayList<>();
    }

    public void initializeGame() {
        // Player starts in the middle, using visual dimensions
        player = new Player(gamePanel.getWidth() / 2 - Player.VISUAL_WIDTH / 2, gamePanel.getHeight() / 2 - Player.VISUAL_HEIGHT / 2);
        skillBalls.clear();
        spawnCounter = 0;
    }

    public Player getPlayer() {
        return player;
    }

    public List<SkillBall> getSkillBalls() {
        return skillBalls;
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

        Iterator<SkillBall> iterator = skillBalls.iterator();
        while (iterator.hasNext()) {
            SkillBall ball = iterator.next();
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
                            gamePanel.showMessage("GAME OVER", "You hit a bomb! Score: " + player.getScore(), GamePanel.MESSAGE_TYPE_ERROR);
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
        if (spawnCounter >= BALL_SPAWN_INTERVAL && skillBalls.size() < MAX_BALLS) {
            spawnSkillBall();
            spawnCounter = 0;
        }
    }

    private void spawnSkillBall() {
        int startX;
        int startY;
        boolean movingRight = random.nextBoolean();

        boolean isBomb = false;
        boolean isBonusStar = false;

        double randType = random.nextDouble();
        if (randType < BOMB_CHANCE) {
            isBomb = true;
        } else if (randType < BOMB_CHANCE + BONUS_STAR_CHANCE) {
            isBonusStar = true;
        }

        if (movingRight) {
            startX = -50;
            startY = random.nextInt(gamePanel.getHeight() / 2 - 50);
        } else {
            startX = gamePanel.getWidth() + 50;
            startY = random.nextInt(gamePanel.getHeight() / 2 - 50) + gamePanel.getHeight() / 2;
        }

        skillBalls.add(new SkillBall(startX, startY, 2, 5, 30, 50, isBomb, isBonusStar));
    }

    public boolean checkLassoTipCollision(Rectangle lassoTipBounds, int lassoTipX, int lassoTipY) {
        SkillBall caughtBall = null;
        for (SkillBall ball : skillBalls) {
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
            } else { // It's a regular skill ball or bonus star
                caughtBall.setActive(false);
                caughtBall.setBeingPulled(true);
                System.out.println("Ball caught by lasso tip! Starting pull animation to player.");
                gamePanel.startPullAnimation(caughtBall, lassoTipX, lassoTipY);
                return true;
            }
        }
        return false;
    }

    public boolean attachBallToPlayer(SkillBall ball) {
        if (ball.isBomb()) {
            if (player.isInvincible()) {
                System.out.println("Bomb lassoed, but player is invincible! Bomb removed.");
                skillBalls.remove(ball);
            } else {
                System.out.println("GAME OVER! Bomb exploded at player!");
                gamePanel.playBombEffectSound();
                gamePanel.stopGame();
                gamePanel.showMessage("GAME OVER", "A bomb exploded on you! Score: " + player.getScore(), GamePanel.MESSAGE_TYPE_ERROR);
                skillBalls.remove(ball);
            }
            return false;
        } else if (ball.isBonusStar()) {
            System.out.println("BONUS STAR COLLECTED! Speed boost & Invincibility!");
            gamePanel.playBonusMusic();

            player.addScore(ball.getValue() * 2);

            int currentSpeedBoostDuration = player.getSpeedBoostDuration();
            int currentInvincibilityDuration = player.getInvincibilityDuration();

            player.setSpeedBoostActive(true);
            player.setInvincible(true);
            
            player.setSpeedBoostDuration(Math.min(currentSpeedBoostDuration + BONUS_DURATION_TICKS, MAX_BONUS_DURATION_TICKS));
            player.setInvincibilityDuration(Math.min(currentInvincibilityDuration + BONUS_DURATION_TICKS, MAX_BONUS_DURATION_TICKS));

            skillBalls.remove(ball);
            return false;
        }

        ball.setBeingPulled(false);
        ball.setHeldByPlayer(true);
        System.out.println("Ball attached to player.");
        return true;
    }

    public void collectHeldBall(SkillBall ball) {
        if (ball.isBomb()) {
            System.out.println("GAME OVER! Brought bomb to basket!");
            gamePanel.playBombEffectSound();
            gamePanel.stopGame();
            gamePanel.showMessage("GAME OVER", "You brought a bomb to the basket! Score: " + player.getScore(), GamePanel.MESSAGE_TYPE_ERROR);
            skillBalls.remove(ball);
            return;
        }
        if (ball.isBonusStar()) {
             System.out.println("Bonus Star (shouldn't be here) deposited. Removing.");
             skillBalls.remove(ball);
             return;
        }

        player.addScore(ball.getValue());
        player.addCollectedBall();
        ball.setHeldByPlayer(false);
        skillBalls.remove(ball);
        System.out.println("Ball deposited! Score: " + player.getScore() + ", Balls: " + player.getCollectedBalls());
    }

    public void saveGameResult() {
        databaseModel.saveThasil(currentUsername, player.getScore(), player.getCollectedBalls());
    }
}