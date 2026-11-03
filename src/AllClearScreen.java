import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Shown after the bathroom: the whole game is finished. */
public class AllClearScreen extends OverlayScreen {
    public AllClearScreen(GamePanel game, PlayScreen play) {
        super(game, play);
        buttons.add("PLAY AGAIN", this::playAgain)
               .add("LEVEL SELECT", () -> levelSelect(play.getLevelNumber()))
               .add("QUIT", this::quit);
    }

    @Override
    protected void drawPopup(Graphics2D g) {
        int h = panelHeight(170), top = GameConfig.HEIGHT / 2 - h / 2, cx = GameConfig.WIDTH / 2;
        UI.panel(g, 580, h);
        g.setFont(UI.font(Font.BOLD, 24));
        g.setColor(new Color(40, 150, 70));
        UI.drawCentered(g, "MISSION COMPLETE!", cx, top + 62);
        g.setFont(UI.font(Font.BOLD, 12));
        g.setColor(new Color(110, 70, 50));
        UI.drawCentered(g, "EVERY ROOM IN THE HOUSE IS STOCKED!", cx, top + 100);
        UI.drawCentered(g, "NOW YOU CAN USE THE BATHROOM IN PEACE :)", cx, top + 124);
        BufferedImage roll = Assets.image("res/Obj/toilet_paper.png");
        for (int i = 0; roll != null && i < 5; i++) {
            int y = top + 140 + (int) (Math.sin((ticks + i * 12) / 8.0) * 5);
            g.drawImage(roll, cx - 124 + i * 50, y, 28, 28, null);
        }
        drawButtons(g, top, h);
    }
}
