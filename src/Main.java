import java.io.File;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length >= 2 && args[0].equals("--shot")) {
            screenshots(new File(args[1]));
            return;
        }
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("POOPIE");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            GamePanel panel = new GamePanel(Progress.inHomeFolder());
            window.add(panel);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
            panel.requestFocusInWindow();
            panel.start();
        });
    }

    /** Saves a picture of every screen and level into a folder (headless, does not touch the save file). */
    private static void screenshots(File dir) throws Exception {
        System.setProperty("java.awt.headless", "true");
        dir.mkdirs();
        Progress progress = new Progress(new File(dir, "preview_save.txt"));
        GamePanel game = new GamePanel(progress);
        for (int i = 0; i < 40; i++) game.update();
        game.saveScreenshot(new File(dir, "title.png"));
        game.setScreen(new CharacterSelectScreen(game));
        game.saveScreenshot(new File(dir, "character.png"));
        progress.setUnlockedForPreview(3);
        game.setScreen(new LevelSelectScreen(game, 2));
        game.saveScreenshot(new File(dir, "levels.png"));
        for (int n = 1; n <= GameConfig.LEVEL_COUNT; n++) {
            game.setSelectedCharacterIndex(n % 2);
            game.startLevel(n, new Random(n));
            for (int i = 0; i < 200; i++) game.update();
            game.saveScreenshot(new File(dir, "level" + n + ".png"));
        }
    }
}
