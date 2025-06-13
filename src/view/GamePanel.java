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


public class GamePanel extends JPanel implements ActionListener {
    private GameViewModel gameViewModel;
    private MainFrame mainFrame;
    private Timer gameLoop;
    private Random random;

    // Lasso animation variables
    private boolean isLassoActive = false;
    private int currentLassoDrawLength = 0; // The length currently being drawn for animation
    private int targetLassoLength = 0;      // The full length to the mouse click
    private int lassoDirection = 1;         // 1 for forward, -1 for backward
    private final int LASSO_ANIMATION_SPEED = 10; // Speed of lasso extension/retraction

    // Mouse click coordinates for lasso target
    private int mouseTargetX;
    private int mouseTargetY;

    // For caught ball animation
    private Balls animatingPulledBall = null; // The ball currently being pulled by lasso

    // For ball currently held by player
    private Balls heldBall = null;
    private double heldBallOffsetAngle = 0;

    // Constants for message types
    public static final int MESSAGE_TYPE_PLAIN = JOptionPane.PLAIN_MESSAGE;
    public static final int MESSAGE_TYPE_INFORMATION = JOptionPane.INFORMATION_MESSAGE;
    public static final int MESSAGE_TYPE_WARNING = JOptionPane.WARNING_MESSAGE;
    public static final int MESSAGE_TYPE_ERROR = JOptionPane.ERROR_MESSAGE;
    public static final int MESSAGE_TYPE_QUESTION = JOptionPane.QUESTION_MESSAGE;

    private final int HOOK_DISPLAY_SIZE = 50;
    private final int CHAIN_SEGMENT_DISPLAY_SIZE = 25; // Ukuran segmen rantai yang diinginkan

    // Ukuran tampilan keranjang
    private final int BASKET_DISPLAY_WIDTH = 80; 
    private final int BASKET_DISPLAY_HEIGHT = 120; 

    // --- PLAYER ANIMATION VARIABLES ---
    private HashMap<String, ArrayList<Rectangle>> playerAnimations;
    private String currentPlayerAnimation = "idle_down"; // Default animation state
    private int currentAnimationFrame = 0;
    private int animationTick = 0;
    private final int ANIMATION_SPEED_FACTOR = 10; // Adjust to control animation speed (lower is faster)
    private boolean isPlayerMoving = false;
    private String lastPlayerDirection = "down"; // "up", "down", "left", "right"

    // Bonus effect animation variables
    private int bonusEffectFrame = 0;
    private final int BONUS_EFFECT_FRAME_COUNT = 4; // Assuming efekbonus.png has 4 frames horizontally
    private final int BONUS_EFFECT_FRAME_WIDTH = 204; // Adjust based on efekbonus.png frame size
    private final int BONUS_EFFECT_FRAME_HEIGHT = 228; // Adjust based on efekbonus.png frame size
    private final int BONUS_EFFECT_ANIMATION_SPEED = 5; // Speed for bonus effect animation

    private final int BONUS_EFFECT_DISPLAY_SIZE = 180; // Ukuran tampilan efek bonus
    private final Font BONUS_TIMER_FONT = new Font("Arial", Font.BOLD, 24);


    public GamePanel(String username, MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        gameViewModel = new GameViewModel(username, this);
        setPreferredSize(new Dimension(1200, 700));
        setFocusable(true);
        addKeyListener(new GameKeyListener());
        addMouseListener(new GameMouseListener());

        random = new Random();
        gameLoop = new Timer(15, this);

        loadPlayerAnimations(); // Call this to set up animations
        // playBackgroundMusic(); // Tidak perlu panggil di sini, akan dipanggil di startGameLogic
    }

    public void startGameLogic() {
        gameViewModel.initializeGame();
        resetGamePanelState();
        playBackgroundMusic(); // Restart background music
        // NEW: Atur volume musik latar game saat dimulai (misal 0.5f untuk 50%)
        // Pastikan gain control tidak null sebelum digunakan
        if (AssetLoader.backgroundMusicGainControl != null) {
            AssetLoader.setClipVolume(AssetLoader.backgroundMusicGainControl, 0.5f);
        } else {
            System.err.println("Background music gain control not available.");
        }
        if (AssetLoader.bonusMusicGainControl != null) {
            AssetLoader.setClipVolume(AssetLoader.bonusMusicGainControl, 0.6f); // Bonus music sedikit lebih keras
        } else {
            System.err.println("Bonus music gain control not available.");
        }
        if (AssetLoader.bombEffectGainControl != null) {
            AssetLoader.setClipVolume(AssetLoader.bombEffectGainControl, 0.8f); // Efek bom paling keras
        } else {
            System.err.println("Bomb effect sound gain control not available.");
        }

        gameLoop.start();
    }

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

        // Reset player animation state
        currentPlayerAnimation = "idle_down";
        currentAnimationFrame = 0;
        animationTick = 0;
        isPlayerMoving = false;
        lastPlayerDirection = "down";

        stopAllMusic();
    }

    // --- Load Player Animations Method ---
    private void loadPlayerAnimations() {
        playerAnimations = new HashMap<>();

        int frameWidth = 80;  
        int frameHeight = 118; 

        ArrayList<Rectangle> idleDownFrames = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            idleDownFrames.add(new Rectangle(i * frameWidth, 0 * frameHeight, frameWidth, frameHeight));
        }
        playerAnimations.put("idle_down", idleDownFrames);
        playerAnimations.put("walk_down", idleDownFrames);

        ArrayList<Rectangle> walkLeftFrames = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            walkLeftFrames.add(new Rectangle(i * frameWidth, 1 * frameHeight, frameWidth, frameHeight));
        }
        playerAnimations.put("walk_left", walkLeftFrames);

        ArrayList<Rectangle> walkRightFrames = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            walkRightFrames.add(new Rectangle(i * frameWidth, 2 * frameHeight, frameWidth, frameHeight));
        }
        playerAnimations.put("walk_right", walkRightFrames);

        ArrayList<Rectangle> walkUpFrames = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            walkUpFrames.add(new Rectangle(i * frameWidth, 3 * frameHeight, frameWidth, frameHeight));
        }
        playerAnimations.put("walk_up", walkUpFrames);

        playerAnimations.put("idle_left", new ArrayList<Rectangle>() {{ add(new Rectangle(0 * frameWidth, 1 * frameHeight, frameWidth, frameHeight)); }} );
        playerAnimations.put("idle_right", new ArrayList<Rectangle>() {{ add(new Rectangle(0 * frameWidth, 2 * frameHeight, frameWidth, frameHeight)); }} );
        playerAnimations.put("idle_up", new ArrayList<Rectangle>() {{ add(new Rectangle(0 * frameWidth, 3 * frameHeight, frameWidth, frameHeight)); }} );
        
        System.out.println("Player astronaut animations loaded.");
    }

    // Sound control methods
    private void playBackgroundMusic() {
        if (AssetLoader.backgroundMusicClip != null) {
            if (AssetLoader.backgroundMusicClip.isRunning()) {
                AssetLoader.backgroundMusicClip.stop();
            }
            AssetLoader.backgroundMusicClip.setFramePosition(0);
            AssetLoader.backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void playBonusMusic() {
        if (AssetLoader.bonusMusicClip != null) {
            stopBackgroundMusic();
            if (!AssetLoader.bonusMusicClip.isRunning()) { 
                AssetLoader.bonusMusicClip.setFramePosition(0);
                AssetLoader.bonusMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }
    }

    public void stopBonusMusic() {
        if (AssetLoader.bonusMusicClip != null && AssetLoader.bonusMusicClip.isRunning()) {
            AssetLoader.bonusMusicClip.stop();
            playBackgroundMusic();
        }
    }

    public void playBombEffectSound() {
        if (AssetLoader.bombEffectClip != null) {
            if (AssetLoader.bombEffectClip.isRunning()) {
                AssetLoader.bombEffectClip.stop();
            }
            AssetLoader.bombEffectClip.setFramePosition(0);
            AssetLoader.bombEffectClip.start();
        }
    }

    private void stopBackgroundMusic() {
        if (AssetLoader.backgroundMusicClip != null && AssetLoader.backgroundMusicClip.isRunning()) {
            AssetLoader.backgroundMusicClip.stop();
        }
    }

    private void stopAllMusic() {
        stopBackgroundMusic();
        if (AssetLoader.bonusMusicClip != null && AssetLoader.bonusMusicClip.isRunning()) {
             AssetLoader.bonusMusicClip.stop();
        }
        if (AssetLoader.mainMenuMusicClip != null && AssetLoader.mainMenuMusicClip.isRunning()) {
             AssetLoader.mainMenuMusicClip.stop();
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        FontMetrics fm = g2d.getFontMetrics();

        // Draw Background Image
        if (AssetLoader.backgroundImage != null) {
            g2d.drawImage(AssetLoader.backgroundImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        Player player = gameViewModel.getPlayer();
        int playerCenterX = player.getX() + 25;
        int playerCenterY = player.getY() + 25;

        // --- GAMBAR PEMAIN DENGAN ANIMASI ASTRONOT ---
        if (AssetLoader.playerAstronautSprite != null) {
            ArrayList<Rectangle> currentFrames = playerAnimations.get(currentPlayerAnimation);
            if (currentFrames != null && !currentFrames.isEmpty()) {
                if (currentAnimationFrame >= currentFrames.size()) {
                    currentAnimationFrame = 0;
                }

                Rectangle frameRect = currentFrames.get(currentAnimationFrame);
                BufferedImage currentSprite = AssetLoader.playerAstronautSprite.getSubimage(
                    frameRect.x, frameRect.y, frameRect.width, frameRect.height
                );
                g2d.drawImage(currentSprite, player.getX(), player.getY(), 50, 50, null);
            } else {
                g2d.setColor(Color.BLUE);
                g2d.fillRect(player.getX(), player.getY(), 50, 50);
            }
        } else {
            g2d.setColor(Color.BLUE);
            g2d.fillRect(player.getX(), player.getY(), 50, 50);
        }

        // Draw Bonus Effect (efekbonus.png) around player if active
        if (player.isSpeedBoostActive() || player.isInvincible()) {
            if (AssetLoader.bonusEffectSprite != null) {
                int effectFrameWidth = BONUS_EFFECT_FRAME_WIDTH;
                int effectFrameHeight = BONUS_EFFECT_FRAME_HEIGHT;
                
                BufferedImage currentEffectSprite = AssetLoader.bonusEffectSprite.getSubimage(
                    (bonusEffectFrame / BONUS_EFFECT_ANIMATION_SPEED) % BONUS_EFFECT_FRAME_COUNT * effectFrameWidth,
                    0,
                    effectFrameWidth,
                    effectFrameHeight
                );
                
                g2d.drawImage(currentEffectSprite, 
                              player.getX() + (50 - BONUS_EFFECT_DISPLAY_SIZE) / 2-15, 
                              player.getY() + (50 - BONUS_EFFECT_DISPLAY_SIZE) / 2-40, 
                              BONUS_EFFECT_DISPLAY_SIZE, BONUS_EFFECT_DISPLAY_SIZE, null);
            } else {
                g2d.setColor(new Color(0, 255, 0, 100));
                g2d.fillOval(player.getX() - 10, player.getY() - 10, 70, 70);
            }

            // Draw Bonus Effect Timer
            g2d.setColor(Color.YELLOW);
            g2d.setFont(BONUS_TIMER_FONT);
            
            int remainingTime = Math.max(player.getSpeedBoostDuration(), player.getInvincibilityDuration()); 
            String timerText = String.format("%d s", remainingTime / 60); 
            
            int textWidth = fm.stringWidth(timerText);
            g2d.drawString(timerText, player.getX() + (40 - textWidth) / 2, player.getY() - 10);
        }


        // Draw Skill Balls
        for (Balls ball : gameViewModel.getBalls()) {
            if (ball.isActive() && !ball.isHeldByPlayer() && !ball.isBeingPulled()) {
                if (ball.isBomb()) {
                    if (AssetLoader.bombAlienImage != null) {
                        g2d.drawImage(AssetLoader.bombAlienImage, ball.getX(), ball.getY(), ball.getSize(), ball.getSize(), null);
                    } else {
                        g2d.setColor(ball.getColor());
                        g2d.fillOval(ball.getX(), ball.getY(), ball.getSize(), ball.getSize());
                    }
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.BOLD, 10));
                } else if (ball.isBonusStar()) {
                    if (AssetLoader.bonusStarImage != null) {
                        g2d.drawImage(AssetLoader.bonusStarImage, ball.getX(), ball.getY(), ball.getSize(), ball.getSize(), null);
                    } else {
                        g2d.setColor(ball.getColor());
                        g2d.fillOval(ball.getX(), ball.getY(), ball.getSize(), ball.getSize());
                    }
                }
                else {
                    if (AssetLoader.skillBallImage != null) {
                        g2d.drawImage(AssetLoader.skillBallImage, ball.getX(), ball.getY(), ball.getSize(), ball.getSize(), null);
                    } else {
                        g2d.setColor(ball.getColor());
                        g2d.fillOval(ball.getX(), ball.getY(), ball.getSize(), ball.getSize());
                    }
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Arial", Font.BOLD, 10));
                    String valueStr = String.valueOf(ball.getValue());
                    int textWidth = fm.stringWidth(valueStr);
                    int textHeight = fm.getHeight();
                    g2d.drawString(valueStr, ball.getX() + ball.getSize() / 2 - textWidth / 2, ball.getY() + ball.getSize() / 2 + textHeight / 4);
                }
            }
        }

        // Draw Lasso
        int lassoEndX = 0;
        int lassoEndY = 0;
        double angle = 0;

        if (isLassoActive) {
            angle = Math.atan2(mouseTargetY - playerCenterY, mouseTargetX - playerCenterX);
            lassoEndX = (int) (playerCenterX + currentLassoDrawLength * Math.cos(angle));
            lassoEndY = (int) (playerCenterY + currentLassoDrawLength * Math.sin(angle));

            // Gambar Rantai Lasso
            if (AssetLoader.lassoChainSegmentImage != null) {
                int segmentWidth = AssetLoader.lassoChainSegmentImage.getWidth();
                int segmentHeight = AssetLoader.lassoChainSegmentImage.getHeight();
                
                if (segmentWidth <= 0 || segmentHeight <= 0) {
                     g2d.setColor(Color.ORANGE);
                     g2d.setStroke(new BasicStroke(2));
                     g2d.drawLine(playerCenterX, playerCenterY, lassoEndX, lassoEndY);
                     g2d.setStroke(new BasicStroke(1));
                } else {
                    int segmentStep = CHAIN_SEGMENT_DISPLAY_SIZE; 

                    int segmentsToDraw = 0;
                    if (segmentStep > 0) {
                        segmentsToDraw = currentLassoDrawLength / segmentStep;
                    }
                    
                    AffineTransform oldTransform = g2d.getTransform();

                    g2d.translate(playerCenterX, playerCenterY);
                    g2d.rotate(angle);

                    for (int i = 0; i <= segmentsToDraw; i++) {
                        g2d.drawImage(AssetLoader.lassoChainSegmentImage, i * segmentStep, -CHAIN_SEGMENT_DISPLAY_SIZE / 2, CHAIN_SEGMENT_DISPLAY_SIZE, CHAIN_SEGMENT_DISPLAY_SIZE, null);
                    }
                    g2d.setTransform(oldTransform);
                }

            } else {
                g2d.setColor(Color.ORANGE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(playerCenterX, playerCenterY, lassoEndX, lassoEndY);
                g2d.setStroke(new BasicStroke(1));
            }


            // Gambar Kail Lasso di Ujung
            if (AssetLoader.lassoHookImage != null) {
                AffineTransform oldTransform = g2d.getTransform();

                g2d.translate(lassoEndX, lassoEndY);
                g2d.rotate(angle - Math.PI / 2); 

                g2d.drawImage(AssetLoader.lassoHookImage, -HOOK_DISPLAY_SIZE / 2, -HOOK_DISPLAY_SIZE / 2, HOOK_DISPLAY_SIZE, HOOK_DISPLAY_SIZE, null);
                
                g2d.setTransform(oldTransform);
            } else {
                int squareSize = 8;
                g2d.setColor(Color.CYAN);
                g2d.fillRect(lassoEndX - squareSize / 2, lassoEndY - squareSize / 2, squareSize, squareSize);
            }
        }

        // Draw Basket/Collection point
        if (AssetLoader.basketImage != null) {
            // Hitung posisi X agar di tengah horizontal
            // (Lebar frame / 2) - (Lebar basket / 2)
            int basketX = (getWidth() / 2) - ((BASKET_DISPLAY_WIDTH + 50) / 2);

            // Hitung posisi Y agar dekat bagian bawah (misalnya, 20 piksel dari bawah)
            int basketY = getHeight() - (BASKET_DISPLAY_HEIGHT + 50) - 50; // 20px dari bawah

            g2d.drawImage(AssetLoader.basketImage, basketX, basketY, BASKET_DISPLAY_WIDTH + 50, BASKET_DISPLAY_HEIGHT + 50, null);
        } else {
            // Fallback: Kotak coklat
            g2d.setColor(new Color(139, 69, 19)); // Brown

            // Hitung posisi X dan Y untuk fallback juga
            int basketX = (getWidth() / 2) - (BASKET_DISPLAY_WIDTH / 2);
            int basketY = getHeight() - BASKET_DISPLAY_HEIGHT - 20; // 20px dari bawah

            g2d.fillRect(basketX, basketY, BASKET_DISPLAY_WIDTH, BASKET_DISPLAY_HEIGHT);
        }


        // Draw the ball currently being pulled by the lasso
        if (animatingPulledBall != null) {
            if (!isLassoActive && animatingPulledBall.isBeingPulled()) {
                angle = Math.atan2(mouseTargetY - playerCenterY, mouseTargetX - playerCenterX);
                lassoEndX = (int) (playerCenterX + currentLassoDrawLength * Math.cos(angle));
                lassoEndY = (int) (playerCenterY + currentLassoDrawLength * Math.sin(angle));
            }

            int currentBallX = lassoEndX;
            int currentBallY = lassoEndY;

            int ballOffsetFromLassoTipX = -animatingPulledBall.getSize()/2;
            int ballOffsetFromLassoTipY = -animatingPulledBall.getSize()/2;

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
            else {
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

        // Draw the ball held by the player
        if (heldBall != null) {
            int offsetDistance = 10 + heldBall.getSize() / 2;

            int ballX = (int) (playerCenterX + offsetDistance * Math.cos(heldBallOffsetAngle)) - heldBall.getSize() / 2;
            int ballY = (int) (playerCenterY + offsetDistance * Math.sin(heldBallOffsetAngle)) - heldBall.getSize() / 2;

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
            else {
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

        // Draw score and ball count
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Score: " + player.getScore(), 10, 30);
        g2d.drawString("Count Star: " + player.getCollectedBalls(), 10, 60);
    }

    public MainFrame getMainFrame() {
        return this.mainFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        gameViewModel.updateGame();
        handleLassoAnimation();
        
        // --- UPDATE PLAYER ANIMATION FRAME ---
        animationTick++;
        if (animationTick >= ANIMATION_SPEED_FACTOR) {
            ArrayList<Rectangle> frames = playerAnimations.get(currentPlayerAnimation);

            if (frames != null && !frames.isEmpty()) {
                if (currentPlayerAnimation.startsWith("idle_")) {
                    currentAnimationFrame = 0;
                } else {
                    currentAnimationFrame++;
                    if (currentAnimationFrame >= frames.size()) {
                        currentAnimationFrame = 0;
                    }
                }
            } else {
                currentAnimationFrame = 0;
            }
            animationTick = 0;
        }

        // Update bonus effect animation frame
        bonusEffectFrame++;
        if (bonusEffectFrame >= BONUS_EFFECT_FRAME_COUNT * BONUS_EFFECT_ANIMATION_SPEED) {
            bonusEffectFrame = 0;
        }

        if (!isPlayerMoving) {
            currentPlayerAnimation = "idle_" + lastPlayerDirection;
        }
        isPlayerMoving = false;

        repaint();

        if (heldBall != null) {
        Player player = gameViewModel.getPlayer();
        Rectangle playerBounds = new Rectangle(player.getX(), player.getY(), 50, 50);

        // --- Perbarui posisi deteksi keranjang ---
        // Hitung batas keranjang dengan posisi yang sudah diperbarui
        int basketWidthActual = BASKET_DISPLAY_WIDTH + 50;
        int basketHeightActual = BASKET_DISPLAY_HEIGHT + 50;
        int basketX = (getWidth() / 2) - (basketWidthActual / 2);
        int basketY = getHeight() - basketHeightActual - 50; // Sesuaikan dengan nilai di paintComponent

        Rectangle basketBounds = new Rectangle(basketX, basketY, basketWidthActual, basketHeightActual);
        // --- Akhir Perbarui posisi deteksi keranjang ---

        if (playerBounds.intersects(basketBounds)) {
            gameViewModel.collectHeldBall(heldBall);
            heldBall = null;
            heldBallOffsetAngle = 0;
        }
    }
    }

    private void handleLassoAnimation() {
        if (isLassoActive) {
            Player player = gameViewModel.getPlayer();
            int playerCenterX = player.getX() + 25;
            int playerCenterY = player.getY() + 25;
            double angle = Math.atan2(mouseTargetY - playerCenterY, mouseTargetX - playerCenterX);

            if (lassoDirection == 1) {
                currentLassoDrawLength += LASSO_ANIMATION_SPEED;

                if (currentLassoDrawLength > targetLassoLength) {
                    currentLassoDrawLength = targetLassoLength;
                }

                int lassoTipX = (int) (playerCenterX + currentLassoDrawLength * Math.cos(angle));
                int lassoTipY = (int) (playerCenterY + currentLassoDrawLength * Math.sin(angle));

                int lassoTipSize = 8;
                Rectangle lassoTipBounds = new Rectangle(lassoTipX - lassoTipSize / 2, lassoTipY - lassoTipSize / 2, lassoTipSize, lassoTipSize);

                boolean ballCaught = gameViewModel.checkLassoTipCollision(lassoTipBounds, lassoTipX, lassoTipY);

                if (ballCaught) {
                    lassoDirection = -1;
                } else if (currentLassoDrawLength >= targetLassoLength) {
                    lassoDirection = -1;
                }
            } else {
                currentLassoDrawLength -= LASSO_ANIMATION_SPEED;
                
                int lassoTipX = (int) (playerCenterX + currentLassoDrawLength * Math.cos(angle));
                int lassoTipY = (int) (playerCenterY + currentLassoDrawLength * Math.sin(angle));
                int lassoTipSize = 8;
                Rectangle lassoTipBounds = new Rectangle(lassoTipX - lassoTipSize / 2, lassoTipY - lassoTipSize / 2, lassoTipSize, lassoTipSize);
                
                if (animatingPulledBall == null) {
                     gameViewModel.checkLassoTipCollision(lassoTipBounds, lassoTipX, lassoTipY);
                }

                if (currentLassoDrawLength <= 0) {
                    currentLassoDrawLength = 0;
                    isLassoActive = false;
                    lassoDirection = 1;
                    if (animatingPulledBall != null) {
                        boolean shouldHold = gameViewModel.attachBallToPlayer(animatingPulledBall);
                        if (shouldHold) {
                            heldBall = animatingPulledBall;
                        } else {
                            heldBall = null;
                        }
                        animatingPulledBall = null;
                    }
                }
            }
        }
    }

    public void startPullAnimation(Balls ball, int caughtX, int caughtY) {
        this.animatingPulledBall = ball;
        Player player = gameViewModel.getPlayer();
        int playerCenterX = player.getX() + 25;
        int playerCenterY = player.getY() + 25;
        heldBallOffsetAngle = Math.atan2(caughtY - playerCenterY, caughtX - playerCenterX);
    }

    public void stopGame() {
        gameLoop.stop();
        gameViewModel.saveGameResult();

        resetGamePanelState();

        mainFrame.switchToMainPanel();
    }

    public void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    private class GameKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
                gameViewModel.movePlayer(-5, 0);
                isPlayerMoving = true;
                currentPlayerAnimation = "walk_left";
                lastPlayerDirection = "left";
            } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
                gameViewModel.movePlayer(5, 0);
                isPlayerMoving = true;
                currentPlayerAnimation = "walk_right";
                lastPlayerDirection = "right";
            } else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
                gameViewModel.movePlayer(0, -5);
                isPlayerMoving = true;
                currentPlayerAnimation = "walk_up";
                lastPlayerDirection = "up";
            } else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
                gameViewModel.movePlayer(0, 5);
                isPlayerMoving = true;
                currentPlayerAnimation = "walk_down";
                lastPlayerDirection = "down";
            } else if (key == KeyEvent.VK_SPACE) {
                stopGame();
            }
        }
        @Override
        public void keyReleased(KeyEvent e) {
            isPlayerMoving = false;
        }
    }

    private class GameMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (heldBall == null && !isLassoActive) {
                    isLassoActive = true;
                    currentLassoDrawLength = 0;
                    lassoDirection = 1;

                    mouseTargetX = e.getX();
                    mouseTargetY = e.getY();

                    Player player = gameViewModel.getPlayer();
                    int playerCenterX = player.getX() + 25;
                    int playerCenterY = player.getY() + 25;
                    targetLassoLength = (int) Point2D.distance(playerCenterX, playerCenterY, mouseTargetX, mouseTargetY);
                }
            }
        }
    }
}