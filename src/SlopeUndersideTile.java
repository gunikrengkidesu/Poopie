import java.awt.Graphics2D;

/** The lower half of a ramp band (added automatically under every slope square). */
public class SlopeUndersideTile extends Tile {
    private final boolean risingRight;

    public SlopeUndersideTile(boolean risingRight) {
        this.risingRight = risingRight;
    }

    @Override
    public boolean blocksSideways() { return true; }

    @Override
    public double ceilingOffset(double localX) {
        return risingRight ? GameConfig.TILE - localX : localX;
    }

    @Override
    public void draw(Graphics2D g, TileRenderer renderer, TileMap map, int col, int row) {
        renderer.drawSlopeUnderside(g, col * GameConfig.TILE, row * GameConfig.TILE, risingRight);
    }
}
