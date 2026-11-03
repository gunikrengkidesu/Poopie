/** One screen of the game (title, menus, gameplay, pop-ups). The game panel shows one at a time. */
public abstract class Screen implements Updatable, Drawable {
    protected final GamePanel game;

    protected Screen(GamePanel game) {
        this.game = game;
    }

    /** Called once for every key press. */
    public abstract void keyPressed(int keyCode);

    /** Mouse position in game pixels (960x704), whatever the window size. */
    public void mouseMoved(int x, int y) { }

    public void mouseClicked(int x, int y) { }

    /** Mouse moved with the button held down (e.g. dragging a slider). */
    public void mouseDragged(int x, int y) { mouseMoved(x, y); }

    public void mouseReleased(int x, int y) { }

    /** False while the screen is still ignoring input (e.g. a pop-up that just opened). */
    public boolean acceptsInput() { return true; }

    @Override
    public void update() { }
}
