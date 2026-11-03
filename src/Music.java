import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Background music: loops res/Sound/bgm.wav for the whole game. Its volume and the mute switch come from
 * AudioSettings (pause menu slider, the speaker button on the menus, or M).
 * (Java can't play MP3 on its own, so the song is kept as a WAV copy of the MP3 in res/Sound.)
 */
public final class Music {
    private static final String FILE = "res/Sound/bgm.wav";
    private static final float VOLUME_DB = -10f; // full slider: a bit quieter than full volume
    private static Clip clip;

    private Music() { }

    /** Starts looping the music in the background (does nothing if it is already playing or can't be loaded). */
    public static synchronized void start() {
        if (clip != null) return;
        Thread loader = new Thread(Music::load, "music-loader");
        loader.setDaemon(true);
        loader.start();
    }

    private static void load() {
        try (InputStream raw = Assets.open(FILE)) {
            if (raw == null) { System.err.println("Missing music: " + FILE); return; }
            AudioInputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(raw));
            Clip c = AudioSystem.getClip();
            c.open(in);
            synchronized (Music.class) {
                clip = c;
                applyVolume();
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            System.err.println("Could not play music: " + e.getMessage());
        }
    }

    /** Sets the music to the current volume slider, silent when sound is switched off or the slider is at 0. */
    public static synchronized void applyVolume() {
        if (clip == null) return;
        boolean silent = AudioSettings.isMuted() || AudioSettings.getMusic() <= 0.001;
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float db = AudioSettings.gainDb(AudioSettings.getMusic(), VOLUME_DB);
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db)));
        }
        if (clip.isControlSupported(BooleanControl.Type.MUTE))
            ((BooleanControl) clip.getControl(BooleanControl.Type.MUTE)).setValue(silent);
    }
}
