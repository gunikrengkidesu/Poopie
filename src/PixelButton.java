import java.awt.Graphics2D;
import java.awt.Rectangle;

/** One clickable pixel-art button with a fixed place on the screen. */
public class PixelButton {
    private final String label;
    private final Rectangle area;
    private final Runnable action;
    private boolean hovered;

    public PixelButton(String label, int x, int y, int width, int height, Runnable action) {
        this.label = label;
        this.area = new Rectangle(x, y, width, height);
        this.action = action;
    }

    public void mouseMoved(int mx, int my) {
        boolean now = area.contains(mx, my);
        if (now && !hovered) Sfx.play(Sfx.TAP);
        hovered = now;
        if (now) Pointer.over();
    }

    /** Runs the action if the click was on the button; returns whether it was. */
    public boolean mouseClicked(int mx, int my) {
        if (!area.contains(mx, my)) return false;
        action.run();
        return true;
    }

    public void draw(Graphics2D g, long ticks) {
        UI.pixelButton(g, area.x, area.y, area.width, area.height, label, hovered, ticks);
    }
}
