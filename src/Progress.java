import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** Which levels are unlocked. Saved to a small text file in the user's home folder. */
public class Progress {
    private final File file;
    private int highestUnlocked = 1;

    public Progress(File file) {
        this.file = file;
        load();
    }

    public static Progress inHomeFolder() {
        return new Progress(new File(System.getProperty("user.home"), ".poopie_tissue_save.txt"));
    }

    public boolean isUnlocked(int level) { return level <= highestUnlocked; }

    public boolean isCleared(int level) { return level < highestUnlocked; }

    public int getHighestUnlocked() { return Math.min(GameConfig.LEVEL_COUNT, highestUnlocked); }

    /** Clearing a level unlocks the next one. Returns true only if the next level was locked until now. */
    public boolean markCleared(int level) {
        if (level + 1 > highestUnlocked) {
            highestUnlocked = Math.min(GameConfig.LEVEL_COUNT + 1, level + 1);
            save();
            return true;
        }
        return false;
    }

    /** Forgets every cleared level: only level 1 is unlocked again, as on the very first launch. */
    public void reset() {
        highestUnlocked = 1;
        save();
    }

    /** For screenshots/tests: pretend some levels are unlocked without saving. */
    public void setUnlockedForPreview(int level) { highestUnlocked = level; }

    private void load() {
        try {
            if (file.isFile()) {
                // line 1: highest unlocked level (older saves may have a second line; it is ignored)
                String[] lines = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim().split("\\s+");
                highestUnlocked = Math.max(1, Math.min(GameConfig.LEVEL_COUNT + 1, Integer.parseInt(lines[0])));
            }
        } catch (Exception e) {
            highestUnlocked = 1;
        }
    }

    private void save() {
        try {
            String text = highestUnlocked + "\n";
            Files.write(file.toPath(), text.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("Could not save progress: " + e.getMessage());
        }
    }
}
