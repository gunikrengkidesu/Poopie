import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/** A pixel-art volume slider: label, a track you click or drag along, and the volume in percent. */
public class VolumeSlider {
    private static final int TRACK_H = 12, KNOB_W = 14, KNOB_H = 26;
    private final String label;
    private final Rectangle track;
    private final DoubleSupplier value;
    private final DoubleConsumer setter;
    private final Runnable onRelease;
    private boolean dragging, hovered;

    /** @param x,y left end and vertical centre of the track  @param onRelease run after a change (e.g. play a test sound) */
    public VolumeSlider(String label, int x, int y, int width, DoubleSupplier value, DoubleConsumer setter, Runnable onRelease) {
        this.label = label;
        this.track = new Rectangle(x, y - TRACK_H / 2, width, TRACK_H);
        this.value = value;
        this.setter = setter;
        this.onRelease = onRelease;
    }

    /** The area that reacts to the mouse: the track plus some room above and below it. */
    private Rectangle hitArea() {
        return new Rectangle(track.x - KNOB_W / 2, track.y - 12, track.width + KNOB_W, track.height + 24);
    }

    public void mouseMoved(int mx, int my) {
        boolean now = hitArea().contains(mx, my);
        if (now && !hovered) Sfx.play(Sfx.TAP);
        hovered = now;
        if (now) Pointer.over();
    }

    /** Starts dragging when the press is on the slider; returns whether it was. */
    public boolean mousePressed(int mx, int my) {
        if (!hitArea().contains(mx, my)) return false;
        dragging = true;
        setFrom(mx);
        return true;
    }

    public void mouseDragged(int mx, int my) {
        if (dragging) { setFrom(mx); Pointer.over(); }
        else mouseMoved(mx, my);
    }

    public void mouseReleased() {
        if (!dragging) return;
        dragging = false;
        onRelease.run();
    }

    private void setFrom(int mx) {
        setter.accept(Math.max(0, Math.min(1, (mx - track.x) / (double) track.width)));
    }

    public void draw(Graphics2D g) {
        double v = value.getAsDouble();
        boolean active = hovered || dragging;
        g.setFont(UI.font(Font.BOLD, 12));
        g.setColor(new Color(110, 70, 50));
        UI.drawString(g, label, track.x - 118, track.y + TRACK_H / 2 + 6);
        // track: dark groove, filled part in orange (pixel steps, no anti-aliasing)
        Object aa = g.getRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setColor(new Color(60, 36, 24));
        g.fillRect(track.x - 3, track.y - 3, track.width + 6, track.height + 6);
        g.setColor(new Color(214, 196, 170));
        g.fillRect(track.x, track.y, track.width, track.height);
        int filled = (int) Math.round(track.width * v);
        g.setColor(new Color(255, 170, 50));
        g.fillRect(track.x, track.y, filled, track.height);
        g.setColor(new Color(255, 214, 120));
        g.fillRect(track.x, track.y, filled, 3);
        // tick marks every 25%
        g.setColor(new Color(60, 36, 24, 120));
        for (int i = 1; i < 4; i++) g.fillRect(track.x + track.width * i / 4 - 1, track.y + track.height - 4, 2, 4);
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, aa);
        // knob
        int kx = track.x + filled - KNOB_W / 2, ky = track.y + TRACK_H / 2 - KNOB_H / 2 - (active ? 2 : 0);
        UI.pixelBox(g, kx, ky, KNOB_W, KNOB_H, active ? new Color(255, 186, 64) : new Color(236, 226, 206), new Color(60, 36, 24), 2);
        // percent
        g.setFont(UI.font(Font.BOLD, 10));
        g.setColor(new Color(110, 70, 50));
        UI.drawString(g, Math.round(v * 100) + "%", track.x + track.width + 16, track.y + TRACK_H / 2 + 5);
    }
}
