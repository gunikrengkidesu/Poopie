import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/** Draws brick blocks and ramps using the pink brick set in Terrain (32x32).png. */
public class TileRenderer {
    private static final int T = GameConfig.TILE;
    private static final int BLOCK_X = 32, BLOCK_Y = 224;   // 3x3 block
    private static final int STRIP_Y = BLOCK_Y + 128;       // one-tile-high strip
    private static final int COLUMN_X = 160;                // one-tile-wide column
    private final BufferedImage terrain;
    private final BufferedImage fill;
    private final Color edgeDark, edgeLight;
    private final BufferedImage customTexture;

    /** @param customTexture a room's own platform texture, or null for the pink bricks */
    public TileRenderer(BufferedImage customTexture) {
        this.customTexture = customTexture;
        terrain = Assets.image("res/Bg/Terrain (32x32).png");
        BufferedImage brickFill = terrain == null ? null : terrain.getSubimage(BLOCK_X + T, BLOCK_Y + T, T, T);
        fill = customTexture != null ? customTexture : brickFill;
        edgeDark = terrain == null ? new Color(60, 50, 60) : new Color(terrain.getRGB(BLOCK_X + T + 10, BLOCK_Y));
        edgeLight = terrain == null ? new Color(220, 190, 170) : new Color(terrain.getRGB(BLOCK_X + T + 10, BLOCK_Y + 3));
    }

    /** Draws a brick square, picking the piece with borders on the sides that face open air. */
    public void drawBrick(Graphics2D g, int x, int y, boolean n, boolean s, boolean w, boolean e) {
        if (customTexture != null) { // textured platform with dark outlines on the open sides
            g.drawImage(customTexture, x, y, T, T, null);
            g.setColor(new Color(50, 30, 20));
            if (!n) g.fillRect(x, y, T, 3);
            if (!s) g.fillRect(x, y + T - 3, T, 3);
            if (!w) g.fillRect(x, y, 3, T);
            if (!e) g.fillRect(x + T - 3, y, 3, T);
            if (!n) {
                g.setColor(new Color(255, 230, 190, 120));
                g.fillRect(x, y + 3, T, 2);
            }
            return;
        }
        if (terrain == null) {
            g.setColor(new Color(150, 110, 110));
            g.fillRect(x, y, T, T);
            return;
        }
        int sx, sy;
        if (!n && !s) {
            sy = STRIP_Y;
            sx = (!w && !e) ? COLUMN_X : !w ? BLOCK_X : !e ? BLOCK_X + 2 * T : BLOCK_X + T;
        } else if (!w && !e) {
            sx = COLUMN_X;
            sy = !n ? BLOCK_Y : !s ? BLOCK_Y + 2 * T : BLOCK_Y + T;
        } else {
            sx = !w ? BLOCK_X : !e ? BLOCK_X + 2 * T : BLOCK_X + T;
            sy = !n ? BLOCK_Y : !s ? BLOCK_Y + 2 * T : BLOCK_Y + T;
        }
        g.drawImage(terrain, x, y, x + T, y + T, sx, sy, sx + T, sy + T, null);
    }

    /** A thin film of water on top of a brick (the brick itself stays visible). */
    public void drawWetOverlay(Graphics2D g, int x, int y) {
        g.setColor(new Color(90, 170, 240, 230));
        g.fillRect(x, y, T, 3);
        g.setColor(new Color(235, 250, 255, 220));
        g.fillRect(x + 3, y, 10, 1);
        g.fillRect(x + 18, y, 5, 1);
    }

    /** The same water film, following the surface of a ramp. */
    public void drawWetSlope(Graphics2D g, int x, int y, boolean risingRight) {
        int x1 = risingRight ? x : x + T, x2 = risingRight ? x + T : x;
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(90, 170, 240, 230));
        g.drawLine(x1, y + T + 1, x2, y + 1);
        g.setStroke(new BasicStroke(1f));
    }

    public void drawSlope(Graphics2D g, int x, int y, boolean risingRight) {
        Polygon p = risingRight
                ? new Polygon(new int[] {x, x + T, x + T}, new int[] {y + T, y, y + T}, 3)
                : new Polygon(new int[] {x, x + T, x}, new int[] {y, y + T, y + T}, 3);
        fillTextured(g, p);
        int x1 = risingRight ? x : x + T, x2 = risingRight ? x + T : x;
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(edgeDark);
        g.drawLine(x1, y + T, x2, y);
        g.setStroke(new BasicStroke(2f));
        g.setColor(edgeLight);
        g.drawLine(x1, y + T + 3, x2, y + 3);
        g.setStroke(new BasicStroke(1f));
    }

    public void drawSlopeUnderside(Graphics2D g, int x, int y, boolean risingRight) {
        Polygon p = risingRight
                ? new Polygon(new int[] {x, x + T, x}, new int[] {y, y, y + T}, 3)
                : new Polygon(new int[] {x, x + T, x + T}, new int[] {y, y, y + T}, 3);
        fillTextured(g, p);
        int x1 = risingRight ? x : x + T, x2 = risingRight ? x + T : x;
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(edgeDark);
        g.drawLine(x1, y + T, x2, y);
        g.setStroke(new BasicStroke(1f));
    }

    private void fillTextured(Graphics2D g, Polygon p) {
        Shape oldClip = g.getClip();
        g.clip(p);
        if (fill != null) g.setPaint(new TexturePaint(fill, new Rectangle2D.Double(0, 0, T, T)));
        else g.setColor(new Color(150, 110, 110));
        g.fill(p);
        g.setClip(oldClip);
    }
}
