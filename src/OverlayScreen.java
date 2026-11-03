import java.awt.Graphics2D;

/** A pop-up drawn on top of the frozen gameplay screen, with a column of pixel buttons. */
public abstract class OverlayScreen extends Screen {
    protected static final int BUTTON_W = 300, BUTTON_H = 44, BUTTON_GAP = 14;
    protected final PlayScreen play;
    protected final ButtonList buttons = new ButtonList();
    protected long ticks;

    protected OverlayScreen(GamePanel game, PlayScreen play) {
        super(game);
        this.play = play;
    }

    @Override
    public void update() {
        ticks++;
    }

    @Override
    public void keyPressed(int code) {
        if (ticks < 20) return; // ignore keys still held from playing
        buttons.keyPressed(code);
    }

    @Override
    public boolean acceptsInput() { return ticks >= 20; }

    @Override
    public void mouseMoved(int x, int y) { buttons.mouseMoved(x, y); }

    @Override
    public void mouseClicked(int x, int y) {
        if (ticks >= 20) buttons.mouseClicked(x, y);
    }

    @Override
    public final void draw(Graphics2D g) {
        play.draw(g);
        UI.dim(g, 150);
        drawPopup(g);
    }

    protected abstract void drawPopup(Graphics2D g);

    /** Draws the buttons under the text, finishing `bottomMargin` pixels above the panel's bottom edge. */
    protected void drawButtons(Graphics2D g, int panelTop, int panelHeight) {
        int top = panelTop + panelHeight - 36 - buttons.height(BUTTON_H, BUTTON_GAP);
        buttons.draw(g, GameConfig.WIDTH / 2, top, BUTTON_W, BUTTON_H, BUTTON_GAP, ticks);
    }

    protected int panelHeight(int textHeight) {
        return textHeight + buttons.height(BUTTON_H, BUTTON_GAP) + 60;
    }

    // ------------------------------------------------------------------ shared button actions
    protected void playAgain() {
        play.restart();
        game.setScreen(play);
    }

    protected void levelSelect(int selectLevel) {
        game.setScreen(new LevelSelectScreen(game, selectLevel));
    }

    protected void quit() {
        System.exit(0);
    }
}
