import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/** First screen: the living room at sunset (HomeScene) with the title and the menu on the left. */
public class TitleScreen extends MenuScreen {
    private static final int LEFT = 42; // left edge of the title, text and buttons
    private static final int BUTTON_W = 280, BUTTON_H = 48, BUTTON_GAP = 16;
    private final ButtonList buttons = new ButtonList();

    public TitleScreen(GamePanel game) {
        super(game);
        buttons.add("START GAME", () -> game.setScreen(new CharacterSelectScreen(game)))
               .add("QUIT", () -> System.exit(0));
    }

    @Override
    public void keyPressed(int code) {
        if (soundKeyPressed(code)) return;
        buttons.keyPressed(code);
    }

    @Override
    public void mouseMoved(int x, int y) {
        if (soundMouseMoved(x, y)) return;
        buttons.mouseMoved(x, y);
    }

    @Override
    public void mouseClicked(int x, int y) {
        if (soundMouseClicked(x, y)) return;
        buttons.mouseClicked(x, y);
    }

    @Override
    public void draw(Graphics2D g) {
        HomeScene.draw(g, ticks);

        // title, top left
        g.setFont(UI.font(Font.BOLD, 72));
        leftShadowText(g, "POOPIE", 120, new Color(255, 200, 80), new Color(80, 40, 20));
        g.setFont(UI.font(Font.BOLD, 24));
        leftShadowText(g, "TISSUE RUSH", 166, Color.WHITE, new Color(60, 30, 20));

        // how to play, under the title
        g.setFont(UI.smallFont(16));
        String[] lines = {"Grab every tissue roll in the room",
                          "before the clock runs out.",
                          "The door only opens when you have them all!",
                          "Dodge falling objects, don't slip on",
                          "wet floors, and punch snakes with X."};
        for (int i = 0; i < lines.length; i++) {
            int y = 212 + i * 24;
            g.setColor(new Color(42, 26, 58));
            UI.drawString(g, lines[i], LEFT + 2, y + 2);
            g.setColor(new Color(255, 250, 240));
            UI.drawString(g, lines[i], LEFT, y);
        }

        // menu, bottom left
        int top = GameConfig.HEIGHT - 72 - (buttons.size() * (BUTTON_H + BUTTON_GAP) - BUTTON_GAP);
        buttons.draw(g, LEFT + BUTTON_W / 2, top, BUTTON_W, BUTTON_H, BUTTON_GAP, ticks);
        drawSound(g);
    }

    /** Shadowed text with its left edge at LEFT (UI.shadowText centres on a point). */
    private static void leftShadowText(Graphics2D g, String text, int baseline, Color fg, Color shadow) {
        int w = g.getFontMetrics().stringWidth(text);
        UI.shadowText(g, text, LEFT + w / 2, baseline, fg, shadow);
    }
}
