import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * The home screen background: a cosy living room at sunset (window, curtains, floor lamp, sofa, rug) with a
 * tissue box hopping on the rug and tissue sheets floating around it. Drawn on a small pixel canvas and
 * scaled up 3x with no smoothing.
 */
public final class HomeScene {
    // 320 x 235 pixels, shown 3x bigger = 960 x 705 (the game screen is 960 x 704)
    private static final int W = 320, H = 235, SCALE = 3;
    private static final int DY = 55; // the scene was laid out 180 high: everything sits this much lower

    private static final int INK = 0x2a1a3a, PAPER = 0xfffaf0, PINK = 0xff6f91, GOLD = 0xffd27a;

    private static BufferedImage background;
    private static final BufferedImage scene = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);

    private HomeScene() {}

    // ------------------------------------------------------------------ small drawing helpers
    private static void rect(Graphics2D g, int rgb, int x, int y, int w, int h) {
        g.setColor(new Color(rgb));
        g.fillRect(x, y, w, h);
    }

    private static void rect(Graphics2D g, int rgb, int alpha, int x, int y, int w, int h) {
        g.setColor(new Color((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, alpha));
        g.fillRect(x, y, w, h);
    }

    private static void outline(Graphics2D g, int rgb, int x, int y, int w, int h) {
        rect(g, rgb, x, y, w, 1);
        rect(g, rgb, x, y + h - 1, w, 1);
        rect(g, rgb, x, y, 1, h);
        rect(g, rgb, x + w - 1, y, 1, h);
    }

    private static void sprite(Graphics2D g, String[] map, Map<Character, Integer> pal, int x, int y, int s) {
        for (int j = 0; j < map.length; j++)
            for (int i = 0; i < map[j].length(); i++) {
                char k = map[j].charAt(i);
                if (k != '.') rect(g, pal.get(k), x + i * s, y + j * s, s, s);
            }
    }

    // ------------------------------------------------------------------ the room (drawn once)
    private static BufferedImage background() {
        if (background != null) return background;
        background = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = background.createGraphics();
        int d = DY;

        // wall with wallpaper stripes
        rect(g, 0x3b3358, 0, 0, W, 132 + d);
        for (int x = 0; x < W; x += 16) {
            rect(g, 0x443b66, x, 0, 8, 126 + d);
            for (int y = 14; y < 120 + d; y += 14) rect(g, 0x51477a, x + 3, y + ((x / 16) % 2) * 7, 2, 2);
        }
        rect(g, 0x2e2747, 0, 0, W, 6);
        rect(g, 0x352d50, 0, 6, W, 2);

        // baseboard
        rect(g, 0xe8dcc4, 0, 124 + d, W, 8);
        rect(g, 0xb8a98a, 0, 130 + d, W, 2);

        // wood floor
        rect(g, 0x9c6b45, 0, 132 + d, W, 48);
        for (int r = 0; r < 4; r++) {
            int fy = 132 + d + r * 12;
            rect(g, 0xad7a52, 0, fy + 1, W, 1);
            rect(g, 0x80553a, 0, fy + 11, W, 1);
            for (int sx = (r % 2) * 20; sx < W; sx += 40) rect(g, 0x80553a, sx, fy, 1, 12);
        }

        // window with a sunset over the city
        rect(g, 0xf1e6cf, 218, 16 + d, 76, 74);
        int[] sky = {0x6b5aa6, 0x8b63a8, 0xb86aa0, 0xe0719a, 0xff8a6e, 0xffa56b, 0xffc27a, 0xffdc94};
        for (int i = 0; i < sky.length; i++) rect(g, sky[i], 222, 20 + d + i * 7, 64, 7);
        rect(g, 0xfff4c2, 248, 60 + d, 12, 2);
        rect(g, 0xfff4c2, 246, 62 + d, 16, 4);
        rect(g, 0xfff4c2, 246, 66 + d, 16, 12);
        int[][] skyline = {{222, 10, 72}, {234, 8, 66}, {244, 12, 74}, {258, 9, 68}, {269, 8, 72}, {279, 7, 70}};
        for (int[] s : skyline) rect(g, 0x3b2a52, s[0], s[2] + d, s[1], 82 - s[2]);
        int[][] lights = {{225, 76}, {237, 71}, {261, 73}, {271, 77}, {281, 75}, {247, 78}};
        for (int[] l : lights) rect(g, GOLD, l[0], l[1] + d, 2, 2);
        rect(g, 0xf1e6cf, 253, 20 + d, 3, 62);
        rect(g, 0xf1e6cf, 222, 49 + d, 64, 3);
        outline(g, 0xb8a98a, 218, 16 + d, 76, 74);
        rect(g, 0xd8c9a8, 213, 88 + d, 86, 4);
        rect(g, 0xb8a98a, 213, 92 + d, 86, 2);

        // curtains and rod
        rect(g, 0x6b4a2e, 198, 10 + d, 116, 3);
        for (int cx : new int[] {206, 292}) {
            rect(g, 0xd9486b, cx, 13 + d, 14, 82);
            rect(g, 0xb83a59, cx + 4, 13 + d, 2, 82);
            rect(g, 0xb83a59, cx + 9, 13 + d, 2, 82);
            rect(g, 0xff7d9b, cx, 13 + d, 1, 82);
        }

        // floor lamp
        rect(g, GOLD, 307, 58 + d, 12, 12);
        rect(g, 0xfff0b8, 307, 58 + d, 12, 2);
        outline(g, 0xc9962f, 307, 58 + d, 12, 12);
        rect(g, 0x3a2a22, 312, 70 + d, 2, 58);
        rect(g, 0x3a2a22, 308, 128 + d, 10, 3);

        // rug
        rect(g, 0xa63d58, 146, 146 + d, 164, 26);
        outline(g, 0x6e2238, 146, 146 + d, 164, 26);
        rect(g, 0xf0d9a8, 150, 150 + d, 156, 1);
        rect(g, 0xf0d9a8, 150, 167 + d, 156, 1);
        rect(g, 0xf0d9a8, 150, 150 + d, 1, 18);
        rect(g, 0xf0d9a8, 305, 150 + d, 1, 18);
        for (int rx = 156; rx < 302; rx += 10) {
            rect(g, 0xc9526e, rx, 156 + d, 2, 2);
            rect(g, 0xc9526e, rx + 5, 160 + d, 2, 2);
        }

        // sofa
        int dark = 0x1f1630;
        rect(g, 0x3c2418, 115, 208, 131 + d, 104, 4);
        rect(g, 0x3a2a22, 218, 130 + d, 4, 5);
        rect(g, 0x3a2a22, 296, 130 + d, 4, 5);
        rect(g, 0x2f8f9d, 216, 98 + d, 84, 26);
        rect(g, 0x3aa3b2, 216, 98 + d, 84, 3);
        for (int i = 0; i < 3; i++) {
            rect(g, 0x36a0af, 219 + i * 27, 102 + d, 25, 17);
            rect(g, 0x2c8795, 219 + i * 27, 117 + d, 25, 2);
            outline(g, 0x236b77, 219 + i * 27, 102 + d, 25, 17);
        }
        rect(g, 0x2c8795, 216, 118 + d, 84, 13);
        rect(g, 0x38a5b4, 216, 118 + d, 84, 2);
        for (int ax : new int[] {208, 296}) {
            rect(g, 0x236b77, ax, 106 + d, 12, 26);
            rect(g, 0x2f8f9d, ax, 106 + d, 12, 3);
            outline(g, dark, ax, 106 + d, 12, 26);
        }
        outline(g, dark, 216, 98 + d, 84, 33);
        // pillow
        rect(g, PINK, 226, 104 + d, 13, 13);
        rect(g, 0xff9db4, 227, 105 + d, 11, 2);
        outline(g, 0x8f2a48, 226, 104 + d, 13, 13);

        g.dispose();
        return background;
    }

    // ------------------------------------------------------------------ moving things
    private static final String[] HERO = {
            "......oooo......",
            ".....owwwwo.....",
            "....owwgwwo.....",
            "...owwwwwwwo....",
            "....oowwwwoo....",
            "..oooooooooooo..",
            ".ohhhhbbbbbbbbo.",
            ".obbkbbbbbbkbbo.",
            ".obbkbbbbbbkbbo.",
            ".obbbbbbbbbbbbo.",
            ".obbbbkkkkbbbbo.",
            ".obbbbbbbbbbbbo.",
            ".oddddddddddddo.",
            "..oooooooooooo..",
            "..oo........oo..",
            "................",
    };
    private static final String[] SHEET = {
            "..oooo..",
            ".owwwwo.",
            "owwgwwwo",
            "owwwwgwo",
            "owgwwwwo",
            ".owwwwo.",
            "..oooo..",
    };
    private static final Map<Character, Integer> HERO_PAL = new HashMap<>(), SHEET_PAL = new HashMap<>();
    static {
        HERO_PAL.put('o', INK); HERO_PAL.put('w', PAPER); HERO_PAL.put('g', 0xd9d2c4);
        HERO_PAL.put('b', PINK); HERO_PAL.put('h', 0xff9db4); HERO_PAL.put('d', 0xd94f75); HERO_PAL.put('k', INK);
        SHEET_PAL.put('o', INK); SHEET_PAL.put('w', PAPER); SHEET_PAL.put('g', 0xd9d2c4);
    }
    private static final int[][] SHEETS = {{158, 50}, {184, 68}, {200, 84}, {176, 36}, {152, 84}};
    private static final double[] SHEET_PHASE = {0, 1.7, 3.1, 4.4, 5.6};

    private static void renderScene(double t, boolean withHero) {
        Graphics2D g = scene.createGraphics();
        g.setComposite(java.awt.AlphaComposite.Src);
        g.drawImage(background(), 0, 0, null);
        g.setComposite(java.awt.AlphaComposite.SrcOver);
        int d = DY;

        // dust in the sunlight
        for (int m = 0; m < 16; m++) {
            int mx = 150 + ((m * 37) % 150) + (int) Math.round(Math.sin(t * 0.7 + m) * 3);
            int my = (int) Math.round(130 + d - ((t * (3 + (m % 3)) + m * 13) % 90));
            rect(g, 0xffe9a8, 140, mx, my, 1, 1);
        }

        if (!withHero) { g.dispose(); return; }
        // the tissue box hopping on the rug, with its shadow
        double hop = Math.abs(Math.sin(t * 2.6)) * 14;
        int hx = 152, hy = (int) Math.round(102 + d - hop);
        int sw = (int) Math.round(38 - hop * 0.9);
        rect(g, 0x1e0a1e, 97, hx + 24 - sw / 2, 149 + d, sw, 3);
        sprite(g, HERO, HERO_PAL, hx, hy, 3);
        if (t % 3.4 > 3.25) { // blink
            rect(g, PINK, hx + 12, hy + 21, 3, 6);
            rect(g, PINK, hx + 33, hy + 21, 3, 6);
            rect(g, INK, hx + 12, hy + 24, 3, 3);
            rect(g, INK, hx + 33, hy + 24, 3, 3);
        }

        // tissue sheets floating around it
        for (int i = 0; i < SHEETS.length; i++) {
            double p = SHEET_PHASE[i];
            int sx = SHEETS[i][0] + (int) Math.round(Math.sin(t * 0.8 + p) * 6);
            int sy = SHEETS[i][1] + d + (int) Math.round(Math.sin(t * 1.3 + p) * 4);
            sprite(g, SHEET, SHEET_PAL, sx, sy, 2);
            if (Math.sin(t * 2 + p * 2) > 0.85) {
                rect(g, 0xfff4c2, sx + 18, sy - 2, 2, 2);
                rect(g, 0xfff4c2, sx - 4, sy + 12, 2, 2);
            }
        }
        g.dispose();
    }

    /** Draws the whole room over the game screen. @param ticks frames since the screen opened */
    public static void draw(Graphics2D g, long ticks) {
        draw(g, ticks, true);
    }

    /** @param withHero false leaves out the hopping tissue box and its sheets (for menus with cards in the middle) */
    public static void draw(Graphics2D g, long ticks, boolean withHero) {
        renderScene(ticks / (double) GameConfig.FPS, withHero);
        int w = W * SCALE, h = H * SCALE;
        Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(scene, 0, 0, w, h, null);
        if (interp != null) g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);

        // faint scanlines and a dark vignette round the edges
        g.setColor(new Color(0, 0, 0, 26));
        for (int y = 0; y < h; y += 3) g.fillRect(0, y, w, 1);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(w / 2.0, h * 0.45), (float) (w * 0.62),
                new float[] {0.55f, 1f}, new Color[] {new Color(20, 10, 35, 0), new Color(20, 10, 35, 140)}));
        g.fillRect(0, 0, w, h);
    }
}
