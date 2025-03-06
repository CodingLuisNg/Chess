import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SoundPlayer {
    public static void playSound(String resourcePath) {
        new Thread(() -> {
            try (InputStream audioSrc = SoundPlayer.class.getResourceAsStream(resourcePath);
                 BufferedInputStream bufferedIn = new BufferedInputStream(audioSrc);
                 AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn)) {

                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();

                // Allow the sound to complete before closing resources
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });

            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
