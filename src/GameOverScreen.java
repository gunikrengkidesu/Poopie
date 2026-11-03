import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/** Hit on the head or out of time. */
public class GameOverScreen extends OverlayScreen {
    public GameOverScreen(GamePanel game, PlayScreen play) {
        super(game, play);
        buttons.add("PLAY AGAIN", this::playAgain)
               .add("LEVEL SELECT", () -> levelSelect(play.getLevelNumber()))
               .add("QUIT", this::quit);
    }

    @Override
    protected void drawPopup(Graphics2D g) {
        Level level = play.getLevel();
        int h = panelHeight(150), top = GameConfig.HEIGHT / 2 - h / 2, cx = GameConfig.WIDTH / 2;
        UI.panel(g, 480, h);
        g.setFont(UI.font(Font.BOLD, 32));
        g.setColor(new Color(200, 50, 50));
        UI.drawCentered(g, "GAME OVER", cx, top + 70);
        g.setFont(UI.font(Font.BOLD, 16));
        g.setColor(new Color(110, 70, 50));
        UI.drawCentered(g, level.getFailReason(), cx, top + 112);
        g.setFont(UI.smallFont(15));
        g.setColor(new Color(90, 80, 80));
        UI.drawCentered(g, "Rolls collected: " + level.getCollected() + " / " + level.getRequired(), cx, top + 142);
        drawButtons(g, top, h);
    }
}
