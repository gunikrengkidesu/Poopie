import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Sound settings: music volume, sound effect volume (0..1 each) and a switch that silences everything.
 * Kept in a small text file in the user's home folder so they are remembered next time.
 */
public final class AudioSettings {
    private static double music = 0.8, effects = 0.8;
    private static boolean muted;
    private static File file; // null in screenshots/tests: nothing is saved

    private AudioSettings() { }

    /** Reads the saved settings (called when the real game window starts). */
    public static void loadFromHome() {
        file = new File(System.getProperty("user.home"), ".poopie_audio.txt");
        try {
            if (file.isFile()) {
                for (String line : new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).split("\\s+")) {
                    String[] kv = line.split("=");
                    if (kv.length != 2) continue;
                    if (kv[0].equals("music")) music = clamp(Double.parseDouble(kv[1]));
                    if (kv[0].equals("effects")) effects = clamp(Double.parseDouble(kv[1]));
                    if (kv[0].equals("muted")) muted = kv[1].equals("1");
                }
            }
        } catch (Exception e) {
            System.err.println("Could not read sound settings: " + e.getMessage());
        }
    }

    public static synchronized void save() {
        if (file == null) return;
        try {
            String text = String.format(java.util.Locale.ROOT, "music=%.2f\neffects=%.2f\nmuted=%d\n", music, effects, muted ? 1 : 0);
            Files.write(file.toPath(), text.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("Could not save sound settings: " + e.getMessage());
        }
    }

    public static double getMusic() { return music; }
    public static double getEffects() { return effects; }
    public static boolean isMuted() { return muted; }

    /** Changing a volume also switches the sound back on, so the change can be heard. */
    public static void setMusic(double v) {
        music = clamp(v);
        muted = false;
        Music.applyVolume();
    }

    public static void setEffects(double v) {
        effects = clamp(v);
        muted = false;
        Music.applyVolume();
    }

    /** Silences (or brings back) all music and sound effects. */
    public static void toggleMuted() {
        muted = !muted;
        Music.applyVolume();
        save();
    }

    /** Clip gain in decibels for a volume of 0..1, added to `baseDb`. */
    static float gainDb(double volume, float baseDb) {
        return (float) (baseDb + 20 * Math.log10(Math.max(volume, 0.0001)));
    }

    private static double clamp(double v) {
        return Math.max(0, Math.min(1, v));
    }
}
