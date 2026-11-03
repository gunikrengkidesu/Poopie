import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/** Level finished: shows the time and, the first time it is cleared, the room that was just unlocked. */
public class LevelClearScreen extends OverlayScreen {
    private final boolean newUnlock;

    public LevelClearScreen(GamePanel game, PlayScreen play, boolean newUnlock) {
        super(game, play);
        this.newUnlock = newUnlock;
        int next = play.getLevelNumber() + 1;
        buttons.add("NEXT LEVEL", () -> game.startLevel(next))
               .add("PLAY AGAIN", this::playAgain)
               .add("LEVEL SELECT", () -> levelSelect(next))
               .add("QUIT", this::quit);
    }

    @Override
    protected void drawPopup(Graphics2D g) {
        Level level = play.getLevel();
        int n = play.getLevelNumber();
        int h = panelHeight(newUnlock ? 160 : 136), top = GameConfig.HEIGHT / 2 - h / 2, cx = GameConfig.WIDTH / 2;
        UI.panel(g, 520, h);
        g.setFont(UI.font(Font.BOLD, 24));
        g.setColor(new Color(40, 150, 70));
        UI.drawCentered(g, "LEVEL " + n + " CLEAR!", cx, top + 62);
        g.setFont(UI.font(Font.BOLD, 12));
        g.setColor(new Color(110, 70, 50));
        UI.drawCentered(g, level.getRoom().getTitle().toUpperCase() + ": " + level.getRequired() + " ROLLS COLLECTED", cx, top + 100);
        int secs = (int) Math.round(level.getTimeUsed());
        UI.drawCentered(g, String.format("TIME: %d:%02d", secs / 60, secs % 60), cx, top + 124);
        if (newUnlock) {
            g.setColor(new Color(200, 120, 30));
            UI.drawCentered(g, "UNLOCKED: LEVEL " + (n + 1) + " - " + Room.get(n + 1).getTitle().toUpperCase(), cx, top + 152);
        }
        drawButtons(g, top, h);
    }
}
