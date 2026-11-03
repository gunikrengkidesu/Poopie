import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/** The speaker button in the top-right corner of the menus: opens the SOUND pop-up (shows a red cross while muted). */
public class SoundButton {
    private static final int SIZE = 44;
    private final Rectangle area = new Rectangle(GameConfig.WIDTH - SIZE - 16, 16, SIZE, SIZE);
    private boolean hovered;
    // pixel icons: a speaker with sound waves, and a speaker with a red cross
    private static final String[] ON = {
            "....#......",
            "...##...#..",
            "#####.#..#.",
            "#####..#.#.",
            "#####..#.#.",
            "#####..#.#.",
            "#####.#..#.",
            "...##...#..",
            "....#......",
    };
    private static final String[] MUTED = {
            "....#......",
            "...##......",
            "#####.x...x",
            "#####..x.x.",
            "#####...x..",
            "#####..x.x.",
            "#####.x...x",
            "...##......",
            "....#......",
    };

    public void mouseMoved(int mx, int my) {
        boolean now = area.contains(mx, my);
        if (now && !hovered) Sfx.play(Sfx.TAP);
        hovered = now;
        if (now) Pointer.over();
    }

    /** Whether the click was on the button. */
    public boolean mouseClicked(int mx, int my) {
        return area.contains(mx, my);
    }

    public void draw(Graphics2D g) {
        boolean muted = AudioSettings.isMuted();
        int lift = hovered ? 2 : 0, x = area.x, y = area.y - lift;
        UI.pixelBox(g, x, y, SIZE, SIZE, hovered ? new Color(255, 186, 64) : new Color(236, 226, 206), new Color(60, 36, 24), 4);
        Object aa = g.getRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
        String[] icon = muted ? MUTED : ON;
        int p = 3, ox = x + (SIZE - icon[0].length() * p) / 2, oy = y + (SIZE - icon.length * p) / 2;
        for (int r = 0; r < icon.length; r++)
            for (int c = 0; c < icon[r].length(); c++) {
                char ch = icon[r].charAt(c);
                if (ch == '.') continue;
                g.setColor(ch == 'x' ? new Color(200, 40, 40) : new Color(60, 36, 24));
                g.fillRect(ox + c * p, oy + r * p, p, p);
            }
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, aa);
    }
}
