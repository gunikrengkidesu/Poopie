import java.awt.Graphics2D;

/** A 45 degree ramp. Gravity pulls anyone standing on it towards the low side. */
public class SlopeTile extends Tile {
    private final boolean risingRight;

    public SlopeTile(boolean risingRight) {
        this.risingRight = risingRight;
    }

    public boolean isRisingRight() { return risingRight; }

    @Override
    public boolean isSlope() { return true; }

    @Override
    public int downhillDirection() { return risingRight ? -1 : 1; }

    @Override
    public double floorOffset(double localX) {
        return risingRight ? GameConfig.TILE - localX : localX;
    }

    @Override
    public double ceilingOffset(double localX) { return GameConfig.TILE; }

    @Override
    public void draw(Graphics2D g, TileRenderer renderer, TileMap map, int col, int row) {
        renderer.drawSlope(g, col * GameConfig.TILE, row * GameConfig.TILE, risingRight);
    }
}
