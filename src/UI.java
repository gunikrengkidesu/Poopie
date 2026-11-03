import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

/** Shared drawing helpers for text, panels and menus. */
public final class UI {
    private static Font pixelFont;  // Press Start 2P: titles, buttons and the HUD
    private static Font smallRegular, smallBold;  // Pixelify Sans: easier to read for small text

    private UI() { }

    /**
     * Press Start 2P at the given size. The font has a single weight, so the style is ignored.
     * It is drawn on an 8 pixel grid: sizes that are multiples of 8 look the sharpest.
     */
    public static Font font(int style, float size) {
        if (pixelFont == null) pixelFont = loadFont("res/fonts/PressStart2P-Regular.ttf");
        return pixelFont.deriveFont(Font.PLAIN, size);
    }

    /** Pixelify Sans, used for small explanatory text (hints, descriptions). */
    public static Font smallFont(float size) {
        if (smallRegular == null) smallRegular = loadFont("res/fonts/PixelifySans-Regular.ttf");
        return smallRegular.deriveFont(Font.PLAIN, size);
    }

    public static Font smallBoldFont(float size) {
        if (smallBold == null) smallBold = loadFont("res/fonts/PixelifySans-Bold.ttf");
        return smallBold.deriveFont(Font.PLAIN, size);
    }

    private static Font loadFont(String path) {
        try (InputStream in = Assets.open(path)) {
            if (in != null) {
                Font f = Font.createFont(Font.TRUETYPE_FONT, in);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
                return f;
            }
        } catch (Exception e) {
            System.err.println("Could not load " + path + ": " + e.getMessage());
        }
        return new Font(Font.MONOSPACED, Font.BOLD, 12);
    }

    public static void drawString(Graphics2D g, String text, float x, float y) {
        if (text == null || text.isEmpty()) return;
        g.drawString(text, x, y);
    }

    public static int stringWidth(Graphics2D g, String text) {
        return text == null ? 0 : g.getFontMetrics().stringWidth(text);
    }

    public static void drawCentered(Graphics2D g, String text, int cx, int baseline) {
        drawString(g, text, cx - stringWidth(g, text) / 2f, baseline);
    }

    public static void shadowText(Graphics2D g, String text, int cx, int baseline, Color fg, Color shadow) {
        g.setColor(shadow);
        drawCentered(g, text, cx + 4, baseline + 4);
        g.setColor(fg);
        drawCentered(g, text, cx, baseline);
    }

    public static void dim(Graphics2D g, int alpha) {
        g.setColor(new Color(10, 8, 16, alpha));
        g.fillRect(0, 0, GameConfig.WIDTH, GameConfig.HEIGHT);
    }

    /** Cream pixel-art panel centred on the screen. */
    public static void panel(Graphics2D g, int w, int h) {
        pixelBox(g, GameConfig.WIDTH / 2 - w / 2, GameConfig.HEIGHT / 2 - h / 2, w, h,
                 new Color(255, 246, 228), new Color(70, 42, 28), 6);
    }

    /**
     * A box drawn the pixel-art way: square notched corners, a thick dark outline, a light edge on top,
     * a darker edge at the bottom and a hard drop shadow. `p` is the size of one "pixel".
     */
    public static void pixelBox(Graphics2D g, int x, int y, int w, int h, Color fill, Color border, int p) {
        Object aa = g.getRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setColor(new Color(0, 0, 0, 90));                        // drop shadow
        notched(g, x + p, y + p, w, h, p);
        g.setColor(border);                                         // outline
        notched(g, x, y, w, h, p);
        g.setColor(fill);                                           // body
        notched(g, x + p, y + p, w - 2 * p, h - 2 * p, p);
        g.setColor(shade(fill, 1.12));                              // light top edge
        g.fillRect(x + 2 * p, y + p, w - 4 * p, p);
        g.setColor(shade(fill, 0.82));                              // dark bottom edge
        g.fillRect(x + 2 * p, y + h - 2 * p, w - 4 * p, p);
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, aa);
    }

    /** Rectangle with one pixel cut out of every corner. */
    private static void notched(Graphics2D g, int x, int y, int w, int h, int p) {
        g.fillRect(x + p, y, w - 2 * p, h);
        g.fillRect(x, y + p, w, h - 2 * p);
    }

    private static Color shade(Color c, double k) {
        return new Color(Math.min(255, (int) (c.getRed() * k)), Math.min(255, (int) (c.getGreen() * k)),
                         Math.min(255, (int) (c.getBlue() * k)), c.getAlpha());
    }

    /** A pixel-art button. The selected one is orange, raised a little and marked with an arrow. */
    public static void pixelButton(Graphics2D g, int x, int y, int w, int h, String label, boolean selected, long ticks) {
        int lift = selected ? 2 : 0;
        Color fill = selected ? new Color(255, 186, 64) : new Color(236, 226, 206);
        pixelBox(g, x, y - lift, w, h, fill, new Color(60, 36, 24), 4);
        g.setFont(font(Font.BOLD, 16));
        g.setColor(selected ? new Color(70, 36, 16) : new Color(110, 92, 80));
        drawCentered(g, label, x + w / 2, y - lift + h / 2 + 8);
        if (selected) {
            int ax = x - 22 + (int) (Math.sin(ticks / 6.0) * 3), ay = y - lift + h / 2;
            g.setColor(new Color(255, 186, 64));
            g.fillRect(ax, ay - 8, 4, 16);
            g.fillRect(ax + 4, ay - 6, 4, 12);
            g.fillRect(ax + 8, ay - 4, 4, 8);
            g.fillRect(ax + 12, ay - 2, 4, 4);
        }
    }

    public static void banner(Graphics2D g, String text, int baseline, Color background) {
        g.setFont(font(Font.BOLD, 12));
        int w = stringWidth(g, text) + 40;
        g.setColor(background);
        g.fillRoundRect(GameConfig.WIDTH / 2 - w / 2, baseline - 22, w, 34, 16, 16);
        g.setColor(Color.WHITE);
        drawCentered(g, text, GameConfig.WIDTH / 2, baseline);
    }

}
