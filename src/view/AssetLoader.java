import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import javax.sound.sampled.*; // NEW: Import for sound assets

public class AssetLoader {
    public static BufferedImage playerAstronautSprite;
    public static BufferedImage backgroundImage;
    public static BufferedImage skillBallImage;
    public static BufferedImage bombAlienImage;
    public static BufferedImage bonusStarImage;
    public static BufferedImage lassoChainSegmentImage;
    public static BufferedImage lassoHookImage;
    public static BufferedImage bonusEffectSprite; // NEW: for efekbonus.png

    // Sound assets
    public static Clip backgroundMusicClip; // For background_music.wav
    public static Clip bonusMusicClip;      // NEW: For musicbonus.wav
    public static Clip bombEffectClip;      // NEW: For musicefekbom.wav

    public static void loadAssets() {
        System.out.println("Loading assets...");

        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

        try {
            // Load Player Astronaut
            URL playerAstronautUrl = AssetLoader.class.getResource("/assets/astronot.png");
            if (playerAstronautUrl != null) {
                BufferedImage tempPlayer = ImageIO.read(playerAstronautUrl);
                if (tempPlayer != null) {
                    playerAstronautSprite = gc.createCompatibleImage(tempPlayer.getWidth(), tempPlayer.getHeight(), Transparency.TRANSLUCENT);
                    playerAstronautSprite.getGraphics().drawImage(tempPlayer, 0, 0, null);
                    playerAstronautSprite.getGraphics().dispose();
                    System.out.println("Player astronaut sprite loaded.");
                } else {
                    System.err.println("Player astronaut sprite (tempPlayer) is null. File might be corrupted or empty.");
                }
            } else {
                System.err.println("Player astronaut sprite not found: /assets/astronot.png");
            }

            // Load Background Image
            URL backgroundUrl = AssetLoader.class.getResource("/assets/background.jpg");
            if (backgroundUrl != null) {
                BufferedImage tempBg = ImageIO.read(backgroundUrl);
                if (tempBg != null) {
                    backgroundImage = gc.createCompatibleImage(tempBg.getWidth(), tempBg.getHeight(), Transparency.OPAQUE);
                    backgroundImage.getGraphics().drawImage(tempBg, 0, 0, null);
                    backgroundImage.getGraphics().dispose();
                    System.out.println("Background image loaded.");
                } else {
                    System.err.println("Background image (tempBg) is null. File might be corrupted or empty.");
                }
            } else {
                System.err.println("Background image not found: /assets/background.jpg");
            }

            // Load Skill Ball (Star) Image (bintang1.png)
            URL skillBallImageUrl = AssetLoader.class.getResource("/assets/bintang1.png");
            if (skillBallImageUrl != null) {
                BufferedImage tempStar = ImageIO.read(skillBallImageUrl);
                if (tempStar != null) {
                    skillBallImage = gc.createCompatibleImage(tempStar.getWidth(), tempStar.getHeight(), Transparency.TRANSLUCENT);
                    skillBallImage.getGraphics().drawImage(tempStar, 0, 0, null);
                    skillBallImage.getGraphics().dispose();
                    System.out.println("Skill ball (star) image loaded.");
                } else {
                    System.err.println("Skill ball (star) image (tempStar) is null. File might be corrupted or empty.");
                }
            } else {
                System.err.println("Skill ball (star) image not found: /assets/bintang1.png");
            }

            // Load Bomb Alien Image (bom_alien.png)
            URL bombAlienImageUrl = AssetLoader.class.getResource("/assets/bom_alien.png");
            if (bombAlienImageUrl != null) {
                BufferedImage tempBomb = ImageIO.read(bombAlienImageUrl);
                if (tempBomb != null) {
                    bombAlienImage = gc.createCompatibleImage(tempBomb.getWidth(), tempBomb.getHeight(), Transparency.TRANSLUCENT);
                    bombAlienImage.getGraphics().drawImage(tempBomb, 0, 0, null);
                    bombAlienImage.getGraphics().dispose();
                    System.out.println("Bomb alien image loaded.");
                } else {
                    System.err.println("Bomb alien image (tempBomb) is null. File might be corrupted or empty.");
                }
            } else {
                System.err.println("Bomb alien image not found: /assets/bom_alien.png");
            }

            // Load Bonus Star Image (bintangbonus.png)
            URL bonusStarImageUrl = AssetLoader.class.getResource("/assets/bintangbonus.png");
            if (bonusStarImageUrl != null) {
                BufferedImage tempBonusStar = ImageIO.read(bonusStarImageUrl);
                if (tempBonusStar != null) {
                    bonusStarImage = gc.createCompatibleImage(tempBonusStar.getWidth(), tempBonusStar.getHeight(), Transparency.TRANSLUCENT);
                    bonusStarImage.getGraphics().drawImage(tempBonusStar, 0, 0, null);
                    bonusStarImage.getGraphics().dispose();
                    System.out.println("Bonus star image loaded.");
                } else {
                    System.err.println("Bonus star image (tempBonusStar) is null. File might be corrupted or empty.");
                }
            } else {
                System.err.println("Bonus star image not found: /assets/bintangbonus.png");
            }

            // Load Lasso Chain Segment Image (tali.png)
            URL lassoChainUrl = AssetLoader.class.getResource("/assets/tali.png");
            if (lassoChainUrl != null) {
                BufferedImage tempChain = ImageIO.read(lassoChainUrl);
                if (tempChain != null) {
                    lassoChainSegmentImage = gc.createCompatibleImage(tempChain.getWidth(), tempChain.getHeight(), Transparency.TRANSLUCENT);
                    lassoChainSegmentImage.getGraphics().drawImage(tempChain, 0, 0, null);
                    lassoChainSegmentImage.getGraphics().dispose();
                    System.out.println("Lasso chain segment image loaded.");
                } else {
                    System.err.println("Lasso chain segment image (tempChain) is null. File might be corrupted or empty.");
                }
            } else {
                System.err.println("Lasso chain segment image not found: /assets/tali.png");
            }

            // Load Lasso Hook Image (kail.png)
            URL lassoHookUrl = AssetLoader.class.getResource("/assets/kail.png");
            if (lassoHookUrl != null) {
                BufferedImage tempHook = ImageIO.read(lassoHookUrl);
                if (tempHook != null) {
                    lassoHookImage = gc.createCompatibleImage(tempHook.getWidth(), tempHook.getHeight(), Transparency.TRANSLUCENT);
                    lassoHookImage.getGraphics().drawImage(tempHook, 0, 0, null);
                    lassoHookImage.getGraphics().dispose();
                    System.out.println("Lasso hook image loaded.");
                } else {
                    System.err.println("Lasso hook image (tempHook) is null. File might be corrupted or empty.");
                }
            } else {
                System.err.println("Lasso hook image not found: /assets/kail.png");
            }

            // NEW: Load Bonus Effect Sprite (efekbonus.png)
            URL bonusEffectUrl = AssetLoader.class.getResource("/assets/efekbonus.png");
            if (bonusEffectUrl != null) {
                BufferedImage tempEffect = ImageIO.read(bonusEffectUrl);
                if (tempEffect != null) {
                    bonusEffectSprite = gc.createCompatibleImage(tempEffect.getWidth(), tempEffect.getHeight(), Transparency.TRANSLUCENT);
                    bonusEffectSprite.getGraphics().drawImage(tempEffect, 0, 0, null);
                    bonusEffectSprite.getGraphics().dispose();
                    System.out.println("Bonus effect sprite loaded.");
                } else {
                    System.err.println("Bonus effect sprite (tempEffect) is null. File might be corrupted or empty.");
                }
            } else {
                System.err.println("Bonus effect sprite not found: /assets/efekbonus.png");
            }


            // NEW: Load Sound Assets
            // Background Music (background_music.wav)
            URL bgMusicUrl = AssetLoader.class.getResource("/assets/background_music.wav");
            if (bgMusicUrl != null) {
                try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(bgMusicUrl)) {
                    backgroundMusicClip = AudioSystem.getClip();
                    backgroundMusicClip.open(audioStream);
                    System.out.println("Background music loaded.");
                } catch (UnsupportedAudioFileException | LineUnavailableException e) {
                    System.err.println("Error loading background music: " + e.getMessage());
                }
            } else {
                System.err.println("Background music not found: /assets/background_music.wav");
            }

            // Bonus Music (musicbonus.wav)
            URL bonusMusicUrl = AssetLoader.class.getResource("/assets/musicbonus.wav");
            if (bonusMusicUrl != null) {
                try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(bonusMusicUrl)) {
                    bonusMusicClip = AudioSystem.getClip();
                    bonusMusicClip.open(audioStream);
                    System.out.println("Bonus music loaded.");
                } catch (UnsupportedAudioFileException | LineUnavailableException e) {
                    System.err.println("Error loading bonus music: " + e.getMessage());
                }
            } else {
                System.err.println("Bonus music not found: /assets/musicbonus.wav");
            }

            // Bomb Effect Sound (musicefekbom.wav)
            URL bombEffectUrl = AssetLoader.class.getResource("/assets/musicefekbom.wav");
            if (bombEffectUrl != null) {
                try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(bombEffectUrl)) {
                    bombEffectClip = AudioSystem.getClip();
                    bombEffectClip.open(audioStream);
                    System.out.println("Bomb effect sound loaded.");
                } catch (UnsupportedAudioFileException | LineUnavailableException e) {
                    System.err.println("Error loading bomb effect sound: " + e.getMessage());
                }
            } else {
                System.err.println("Bomb effect sound not found: /assets/musicefekbom.wav");
            }


        } catch (IOException e) {
            System.err.println("Error loading assets: " + e.getMessage());
            e.printStackTrace();
        }
    }
}