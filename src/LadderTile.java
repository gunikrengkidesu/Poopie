import java.awt.Color;
import java.awt.Graphics2D;

/**
 * A library ladder. Hold UP or DOWN while touching it to climb. The top rung is a one-way floor:
 * you can stand on it, but you pass through it when climbing up from below.
 */
public class LadderTile extends Tile {
    private final boolean top;

    public LadderTile(boolean top) {
        this.top = top;
    }

    @Override
    public boolean isLadder() { return true; }

    /** Only the top rung can be stood on. */
    @Override
    public double floorOffset(double localX) { return top ? 0 : Double.NaN; }

    @Override
    public boolean isOneWay() { return top; }

    @Override
    public boolean isFlatFloor() { return top; }

    @Override
    public void draw(Graphics2D g, TileRenderer renderer, TileMap map, int col, int row) {
        int t = GameConfig.TILE, x = col * t, y = row * t;
        Color rail = new Color(120, 74, 40), dark = new Color(70, 42, 24), light = new Color(170, 112, 64);
        g.setColor(dark);
        g.fillRect(x + 5, y, 6, t);
        g.fillRect(x + t - 11, y, 6, t);
        g.setColor(rail);
        g.fillRect(x + 6, y, 4, t);
        g.fillRect(x + t - 10, y, 4, t);
        for (int ry = 4; ry < t; ry += 10) {
            g.setColor(dark);
            g.fillRect(x + 8, y + ry, t - 16, 5);
            g.setColor(light);
            g.fillRect(x + 8, y + ry, t - 16, 2);
        }
        if (top) { // a small landing so the top rung reads as something you can stand on
            g.setColor(dark);
            g.fillRect(x + 2, y, t - 4, 4);
            g.setColor(light);
            g.fillRect(x + 2, y, t - 4, 2);
        }
    }
}
