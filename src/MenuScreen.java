import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Base for menu screens: the living room at sunset (HomeScene, without the hopping tissue box) under a
 * dark layer, with tissue rolls drifting down.
 */
public abstract class MenuScreen extends Screen {
    protected long ticks;
    /** Speaker button in the top-right corner, and the SOUND pop-up (volume sliders) it opens. */
    private final SoundButton sound = new SoundButton();
    private final SoundPanel soundPanel = new SoundPanel();

    protected MenuScreen(GamePanel game) {
        super(game);
    }

    @Override
    public void update() {
        ticks++;
        soundPanel.update();
    }

    // ------------------------------------------------------------------ speaker button + SOUND pop-up
    // Each menu calls these first; while the pop-up is open it takes all the input.

    /** Returns true if the SOUND pop-up used the key. */
    protected boolean soundKeyPressed(int code) {
        if (!soundPanel.isOpen()) return false;
        soundPanel.keyPressed(code);
        return true;
    }

    /** Returns true if the SOUND pop-up is open (the menu behind it should ignore the mouse). */
    protected boolean soundMouseMoved(int x, int y) {
        if (soundPanel.isOpen()) { soundPanel.mouseMoved(x, y); return true; }
        sound.mouseMoved(x, y);
        return false;
    }

    /** Returns true if the click went to the speaker button or the pop-up. */
    protected boolean soundMouseClicked(int x, int y) {
        if (soundPanel.isOpen()) { soundPanel.mouseClicked(x, y); return true; }
        if (!sound.mouseClicked(x, y)) return false;
        soundPanel.open();
        return true;
    }

    @Override
    public void mouseDragged(int x, int y) {
        if (soundPanel.isOpen()) soundPanel.mouseDragged(x, y);
        else mouseMoved(x, y);
    }

    @Override
    public void mouseReleased(int x, int y) {
        soundPanel.mouseReleased();
    }

    /** Draws the speaker button, and the pop-up on top of everything when it is open. */
    protected void drawSound(Graphics2D g) {
        sound.draw(g);
        soundPanel.draw(g);
    }

    protected void drawBackdrop(Graphics2D g, int darkness) {
        long t = game.getFrameCount();
        HomeScene.draw(g, t, false);
        UI.dim(g, darkness);
        drawFallingRolls(g, t);
    }

    private void drawFallingRolls(Graphics2D g, long t0) {
        BufferedImage roll = Assets.image("res/Obj/toilet_paper.png");
        if (roll == null) return;
        for (int i = 0; i < 9; i++) {
            double t = (t0 * (0.6 + (i % 3) * 0.25) + i * 97) % (GameConfig.HEIGHT + 80);
            int x = (i * 113 + 40) % GameConfig.WIDTH;
            Graphics2D gg = (Graphics2D) g.create();
            gg.rotate((t0 + i * 40) / 40.0, x + 14, t - 26);
            gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
            gg.drawImage(roll, x, (int) t - 40, 28, 28, null);
            gg.dispose();
        }
    }
}
