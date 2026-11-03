import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

/** Gameplay: runs a Level and draws the HUD. Opens pop-up screens when the run ends or is paused. */
public class PlayScreen extends Screen {
    private final int levelNumber;
    private final Random random;
    private Level level;
    private long ticks;
    private static final java.awt.Rectangle PAUSE_BUTTON = new java.awt.Rectangle(GameConfig.WIDTH / 2 + 70, 2, 34, 30);
    private boolean pauseHovered;

    public PlayScreen(GamePanel game, int levelNumber, Random random) {
        super(game);
        this.levelNumber = levelNumber;
        this.random = random;
        restart();
    }

    public final void restart() {
        level = new Level(Room.get(levelNumber), game.getSelectedCharacter(), game.getKeys(), random);
        game.getKeys().clear();
        ticks = 0;
    }

    public Level getLevel() { return level; }
    public int getLevelNumber() { return levelNumber; }

    @Override
    public void mouseMoved(int x, int y) {
        boolean now = PAUSE_BUTTON.contains(x, y);
        if (now && !pauseHovered) Sfx.play(Sfx.TAP);
        pauseHovered = now;
        if (now) Pointer.over();
    }

    @Override
    public void mouseClicked(int x, int y) {
        if (PAUSE_BUTTON.contains(x, y) && level.getStatus() == Level.Status.PLAYING) {
            Sfx.play(Sfx.ENTER);
            game.setScreen(new PauseScreen(game, this));
        }
    }

    @Override
    public void keyPressed(int code) {
        if (level.getStatus() != Level.Status.PLAYING) return;
        if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_P) game.setScreen(new PauseScreen(game, this));
    }

    @Override
    public void update() {
        ticks++;
        level.update();
        switch (level.getStatus()) {
            case FAILED:
                Sfx.play(Sfx.GAME_OVER);
                game.setScreen(new GameOverScreen(game, this));
                break;
            case COMPLETE:
                boolean newUnlock = game.getProgress().markCleared(levelNumber);
                // the last level cleared for the first time gets the big fanfare
                boolean lastLevel = levelNumber == GameConfig.LEVEL_COUNT;
                Sfx.play(lastLevel && newUnlock ? Sfx.ALL_CLEAR : Sfx.LEVEL_CLEAR);
                game.setScreen(levelNumber == GameConfig.LEVEL_COUNT
                        ? new AllClearScreen(game, this) : new LevelClearScreen(game, this, newUnlock));
                break;
            default:
                break;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        level.draw(g);
        drawHud(g);
    }

    private void drawHud(Graphics2D g) {
        Room room = level.getRoom();
        int w = GameConfig.WIDTH;
        g.setColor(new Color(20, 16, 24, 190));
        g.fillRoundRect(8, 4, 250, 26, 12, 12);
        g.fillRoundRect(w / 2 - 60, 4, 120, 26, 12, 12);
        g.fillRoundRect(w - 140, 4, 132, 26, 12, 12);

        g.setFont(UI.font(Font.BOLD, 10));
        g.setColor(new Color(255, 214, 120));
        UI.drawString(g, "LV" + room.getNumber() + " " + room.getTitle().toUpperCase(), 20, 22);

        int secs = (int) Math.ceil(level.getTimeLeft());
        boolean hurry = level.getTimeLeft() < 15;
        g.setFont(UI.font(Font.BOLD, 16));
        g.setColor(hurry && (ticks / 10) % 2 == 0 ? new Color(255, 90, 80) : Color.WHITE);
        UI.drawCentered(g, String.format("%02d:%02d", secs / 60, secs % 60), w / 2, 25);

        BufferedImage roll = Assets.image("res/Obj/toilet_paper.png");
        if (roll != null) g.drawImage(roll, w - 132, 6, 22, 22, null);
        boolean done = level.getCollected() >= level.getRequired();
        g.setColor(done ? new Color(120, 255, 140) : Color.WHITE);
        g.setFont(UI.font(Font.BOLD, 12));
        UI.drawString(g, "x " + level.getCollected() + " / " + level.getRequired(), w - 104, 23);

        if (done && level.getStatus() == Level.Status.PLAYING) {
            String where = levelNumber == GameConfig.LEVEL_COUNT ? "THE TOILET" : "THE NEXT ROOM";
            UI.banner(g, "ALL ROLLS COLLECTED! GO TO " + where + "!", GameConfig.HEIGHT - 50, new Color(40, 140, 70, 220));
        }
        g.setFont(UI.font(Font.PLAIN, 8));
        g.setColor(new Color(255, 255, 255, 160));
        g.setFont(UI.smallFont(13));
        String hint = "A/D or LEFT/RIGHT: move    W/UP/SPACE: jump    ESC/P: pause";
        if (level.hasSnakes()) hint += "    X: punch snakes";
        hint += "    M: sound on/off";
        if (level.hasLadders()) hint += "    W/UP: climb ladders";
        UI.drawString(g, hint, 14, GameConfig.HEIGHT - 10);
        drawPauseButton(g);
    }

    /** Pixel-art pause button (two bars) next to the timer. */
    private void drawPauseButton(Graphics2D g) {
        java.awt.Rectangle b = PAUSE_BUTTON;
        UI.pixelBox(g, b.x, b.y, b.width, b.height,
                    pauseHovered ? new Color(255, 186, 64) : new Color(236, 226, 206), new Color(60, 36, 24), 3);
        g.setColor(new Color(60, 36, 24));
        g.fillRect(b.x + 11, b.y + 8, 4, b.height - 16);
        g.fillRect(b.x + b.width - 15, b.y + 8, 4, b.height - 16);
    }
}
