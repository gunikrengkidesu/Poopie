import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/** A vertical list of pixel-art buttons, chosen with the arrow keys and pressed with Enter or Space. */
public class ButtonList {
    private final List<String> labels = new ArrayList<>();
    private final List<Runnable> actions = new ArrayList<>();
    private int selected;
    private int hovered = -1; // button under the mouse, or -1
    private final List<java.awt.Rectangle> areas = new ArrayList<>();

    public ButtonList add(String label, Runnable action) {
        labels.add(label);
        actions.add(action);
        return this;
    }

    public int size() { return labels.size(); }

    public void keyPressed(int code) {
        if (labels.isEmpty()) return;
        if (KeyHandler.isUp(code) || code == KeyEvent.VK_LEFT)
            select((selected - 1 + labels.size()) % labels.size());
        if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_RIGHT)
            select((selected + 1) % labels.size());
        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) actions.get(selected).run();
    }

    /** Draws the buttons stacked downwards from `top`, centred on `cx`. */
    public void draw(Graphics2D g, int cx, int top, int width, int height, int gap, long ticks) {
        areas.clear();
        for (int i = 0; i < labels.size(); i++) {
            int y = top + i * (height + gap);
            areas.add(new java.awt.Rectangle(cx - width / 2, y, width, height));
            UI.pixelButton(g, cx - width / 2, y, width, height, labels.get(i), i == selected, ticks);
        }
    }

    /** Hovering a button selects it, with a tap sound each time the cursor moves onto a button. */
    public void mouseMoved(int x, int y) {
        int over = -1;
        for (int i = 0; i < areas.size(); i++) if (areas.get(i).contains(x, y)) over = i;
        if (over != -1 && over != hovered) Sfx.play(Sfx.TAP);
        hovered = over;
        if (over != -1) { selected = over; Pointer.over(); }
    }

    /** Moves the highlight, with a tap sound when it lands on a different button. */
    private void select(int i) {
        if (i != selected) Sfx.play(Sfx.TAP);
        selected = i;
    }

    public void mouseClicked(int x, int y) {
        for (int i = 0; i < areas.size(); i++)
            if (areas.get(i).contains(x, y)) { selected = i; actions.get(i).run(); return; }
    }

    /** Total height of the stack, for laying out panels. */
    public int height(int buttonHeight, int gap) {
        return labels.size() * buttonHeight + Math.max(0, labels.size() - 1) * gap;
    }
}
