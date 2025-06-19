import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;

// NEW: Penting untuk kontrol volume
import javax.sound.sampled.*; // Pastikan ini diimpor

public class AssetLoader {
    public static BufferedImage playerAstronautSprite;
    public static BufferedImage backgroundImage;
    public static BufferedImage mainMenuBackgroundImage;
    public static BufferedImage skillBallImage;
    public static BufferedImage bombAlienImage;
    public static BufferedImage bonusStarImage;
    public static BufferedImage lassoChainSegmentImage;
    public static BufferedImage lassoHookImage;
    public static BufferedImage bonusEffectSprite; 

    // Sound assets
    public static Clip backgroundMusicClip;
    public static Clip bonusMusicClip;      
    public static Clip bombEffectClip;      
    public static Clip mainMenuMusicClip;   

    // NEW: FloatControl untuk mengatur volume masing-masing klip
    public static FloatControl backgroundMusicGainControl;
    public static FloatControl bonusMusicGainControl;
    public static FloatControl bombEffectGainControl;
    public static FloatControl mainMenuMusicGainControl;

    // NEW: Gambar untuk keranjang
    public static BufferedImage basketImage; 


    public static void loadAssets() {
        System.out.println("Loading assets...");

        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

        try {
            // Load Player Astronaut
            URL playerAstronautUrl = AssetLoader.class.getResource("/assets/images/astronot.png"); // Ganti nama file ini jika di aset Anda namanya "astronot.png"
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
                System.err.println("Player astronaut sprite not found: /assets/images/astronot.png");
            }

            // Load Background Image
            URL backgroundUrl = AssetLoader.class.getResource("/assets/images/background.jpg");
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
                System.err.println("Background image not found: /assets/images/background.jpg");
            }

            URL mainMenuBackgroundUrl = AssetLoader.class.getResource("/assets/images/main_background.jpg"); // Nama file baru
            if (mainMenuBackgroundUrl != null) {
                BufferedImage tempMainMenuBg = ImageIO.read(mainMenuBackgroundUrl);
                if (tempMainMenuBg != null) {
                    mainMenuBackgroundImage = gc.createCompatibleImage(tempMainMenuBg.getWidth(), tempMainMenuBg.getHeight(), Transparency.OPAQUE);
                    mainMenuBackgroundImage.getGraphics().drawImage(tempMainMenuBg, 0, 0, null);
                    mainMenuBackgroundImage.getGraphics().dispose();
                    System.out.println("Main menu background image loaded.");
                } else {
                    System.err.println("Main menu background image (tempMainMenuBg) is null. File might be corrupted or empty.");
                }
            } else {
                System.err.println("Main menu background image not found: /assets/images/main_background.jpg");
            }

            // Load Skill Ball (Star) Image (bintang1.png)
            URL skillBallImageUrl = AssetLoader.class.getResource("/assets/images/bintang_point.png");
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
                System.err.println("Skill ball (star) image not found: /assets/images/bintang_point.png");
            }

            // Load Bomb Alien Image (bom_alien.png)
            URL bombAlienImageUrl = AssetLoader.class.getResource("/assets/images/bom_alien.png");
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
                System.err.println("Bomb alien image not found: /assets/images/bom_alien.png");
            }

            // Load Bonus Star Image (bintangbonus.png)
            URL bonusStarImageUrl = AssetLoader.class.getResource("/assets/images/bintang_bonus.png");
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
                System.err.println("Bonus star image not found: /assets/images/bintang_bonus.png");
            }

            // Load Lasso Chain Segment Image (tali.png)
            URL lassoChainUrl = AssetLoader.class.getResource("/assets/images/tali_laso.png");
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
                System.err.println("Lasso chain segment image not found: /assets/images/tali_laso.png");
            }

            // Load Lasso Hook Image (kail.png)
            URL lassoHookUrl = AssetLoader.class.getResource("/assets/images/kail.png");
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
                System.err.println("Lasso hook image not found: /assets/images/kail.png");
            }

            // Load Bonus Effect Sprite (efekbonus.png)
            URL bonusEffectUrl = AssetLoader.class.getResource("/assets/images/efek_bonus.png");
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
                System.err.println("Bonus effect sprite not found: /assets/images/efek_bonus.png");
            }

            // Load Basket Image (kapal.png)
            URL basketImageUrl = AssetLoader.class.getResource("/assets/images/kapal.png");
            if (basketImageUrl != null) {
                BufferedImage tempBasket = ImageIO.read(basketImageUrl);
                if (tempBasket != null) {
                    basketImage = gc.createCompatibleImage(tempBasket.getWidth(), tempBasket.getHeight(), Transparency.TRANSLUCENT);
                    basketImage.getGraphics().drawImage(tempBasket, 0, 0, null);
                    basketImage.getGraphics().dispose();
                    System.out.println("Basket image loaded.");
                } else {
                    System.err.println("Basket image (tempBasket) is null. File might be corrupted or empty.");
                }
            } else {
                System.err.println("Basket image not found: /assets/images/kapal.png");
            }


            // Load Sound Assets dan mendapatkan FloatControl
            // Background Music (background_music.wav)
            URL bgMusicUrl = AssetLoader.class.getResource("/assets/sounds/background_music.wav");
            if (bgMusicUrl != null) {
                try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(bgMusicUrl)) {
                    backgroundMusicClip = AudioSystem.getClip();
                    backgroundMusicClip.open(audioStream);
                    if (backgroundMusicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        backgroundMusicGainControl = (FloatControl) backgroundMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
                    } else { // NEW: Handle case where control is not supported
                        System.err.println("Master Gain control not supported for background music.");
                    }
                    System.out.println("Background music loaded.");
                } catch (UnsupportedAudioFileException | LineUnavailableException e) {
                    System.err.println("Error loading background music: " + e.getMessage());
                }
            } else {
                System.err.println("Background music not found: /assets/sounds/background_music.wav");
            }

            // Bonus Music (musicbonus.wav)
            URL bonusMusicUrl = AssetLoader.class.getResource("/assets/sounds/music_bonus.wav");
            if (bonusMusicUrl != null) {
                try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(bonusMusicUrl)) {
                    bonusMusicClip = AudioSystem.getClip();
                    bonusMusicClip.open(audioStream);
                    if (bonusMusicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        bonusMusicGainControl = (FloatControl) bonusMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
                    } else { // NEW
                        System.err.println("Master Gain control not supported for bonus music.");
                    }
                    System.out.println("Bonus music loaded.");
                } catch (UnsupportedAudioFileException | LineUnavailableException e) {
                    System.err.println("Error loading bonus music: " + e.getMessage());
                }
            } else {
                System.err.println("Bonus music not found: /assets/sounds/music_bonus.wav");
            }

            // Bomb Effect Sound (musicefekbom.wav)
            URL bombEffectUrl = AssetLoader.class.getResource("/assets/sounds/music_efek_bom.wav");
            if (bombEffectUrl != null) {
                try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(bombEffectUrl)) {
                    bombEffectClip = AudioSystem.getClip();
                    bombEffectClip.open(audioStream);
                    if (bombEffectClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        bombEffectGainControl = (FloatControl) bombEffectClip.getControl(FloatControl.Type.MASTER_GAIN);
                    } else { // NEW
                        System.err.println("Master Gain control not supported for bomb effect sound.");
                    }
                    System.out.println("Bomb effect sound loaded.");
                } catch (UnsupportedAudioFileException | LineUnavailableException e) {
                    System.err.println("Error loading bomb effect sound: " + e.getMessage());
                }
            } else {
                System.err.println("Bomb effect sound not found: /assets/sounds/music_efek_bom.wav");
            }

            // Main Menu Music (main_music.wav)
            URL mainMenuMusicUrl = AssetLoader.class.getResource("/assets/sounds/main_music.wav");
            if (mainMenuMusicUrl != null) {
                try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(mainMenuMusicUrl)) {
                    mainMenuMusicClip = AudioSystem.getClip();
                    mainMenuMusicClip.open(audioStream);
                    if (mainMenuMusicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        mainMenuMusicGainControl = (FloatControl) mainMenuMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
                    } else { // NEW
                        System.err.println("Master Gain control not supported for main menu music.");
                    }
                    System.out.println("Main menu music loaded.");
                } catch (UnsupportedAudioFileException | LineUnavailableException e) {
                    System.err.println("Error loading main menu music: " + e.getMessage());
                }
            } else {
                System.err.println("Main menu music not found: /assets/sounds/main_music.wav");
            }


        } catch (IOException e) {
            System.err.println("Error loading assets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Metode utilitas untuk mengatur volume klip
    public static void setClipVolume(FloatControl gainControl, float volume) {
        if (gainControl != null) {
            float minDb = gainControl.getMinimum();
            float maxDb = gainControl.getMaximum();
            
            if (volume <= 0.0f) { 
                gainControl.setValue(minDb);
            } else {
                float dB = (float) (Math.log10(volume) * 20.0);
                dB = Math.max(minDb, Math.min(dB, maxDb));
                gainControl.setValue(dB);
            }
        }
    }
}