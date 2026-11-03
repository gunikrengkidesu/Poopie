import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Short sound effects from res/Sound. Each sound is loaded into a few copies that take turns, so the same
 * sound can play again while the last one is still ringing (e.g. two tissues picked up one after the other).
 * Volume and the mute switch come from AudioSettings. Sounds only play after enable() (called when the real game window starts), so screenshots and tests stay silent.
 */
public final class Sfx {
    public static final String TAP = "tap", ENTER = "enter", GAME_OVER = "gameover",
            LEVEL_CLEAR = "completion-level", ALL_CLEAR = "completion-all",
            JUMP = "jump", COLLECT = "collect", PUNCH = "punch";
    private static final String[] ALL = {TAP, ENTER, GAME_OVER, LEVEL_CLEAR, ALL_CLEAR, JUMP, COLLECT, PUNCH};
    private static final int COPIES = 3;
    private static final Map<String, Clip[]> clips = new HashMap<>();
    private static final Map<String, Integer> nextCopy = new HashMap<>();
    private static volatile boolean enabled;

    private Sfx() { }

    /** Turns sound effects on and loads them in the background. */
    public static void enable() {
        if (enabled) return;
        enabled = true;
        Thread loader = new Thread(() -> { for (String name : ALL) clip(name); }, "sfx-loader");
        loader.setDaemon(true);
        loader.start();
    }

    /** Plays a sound from the start, on the copy whose turn it is (earlier plays keep ringing). */
    public static void play(String name) {
        if (!enabled || AudioSettings.isMuted() || AudioSettings.getEffects() <= 0.001) return;
        Clip c = nextClip(name);
        if (c == null) return;
        synchronized (c) {
            if (c.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
                float db = AudioSettings.gainDb(AudioSettings.getEffects(), 0f);
                gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db)));
            }
            c.stop();
            c.flush();
            c.setFramePosition(0);
            c.start();
        }
    }

    private static synchronized Clip nextClip(String name) {
        Clip[] copies = clip(name);
        if (copies == null) return null;
        int i = nextCopy.getOrDefault(name, 0);
        nextCopy.put(name, (i + 1) % copies.length);
        return copies[i];
    }

    private static synchronized Clip[] clip(String name) {
        if (clips.containsKey(name)) return clips.get(name);
        Clip[] copies = null;
        String file = "res/Sound/" + name + ".wav";
        try (InputStream raw = Assets.open(file)) {
            if (raw == null) System.err.println("Missing sound: " + file);
            else {
                AudioInputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(raw));
                javax.sound.sampled.AudioFormat format = in.getFormat();
                byte[] data = in.readAllBytes();
                copies = new Clip[COPIES];
                for (int i = 0; i < COPIES; i++) {
                    copies[i] = AudioSystem.getClip();
                    copies[i].open(format, data, 0, data.length);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load sound " + file + ": " + e.getMessage());
            copies = null;
        }
        clips.put(name, copies);
        return copies;
    }
}
