import java.awt.Graphics2D;

/** A wet ramp: gravity pulls harder and there is almost no grip, so climbing it is slow and you slide back fast. */
public class WetSlopeTile extends SlopeTile {
    public WetSlopeTile(boolean risingRight) {
        super(risingRight);
    }

    @Override
    public boolean isSlippery() { return true; }

    @Override
    public void draw(Graphics2D g, TileRenderer renderer, TileMap map, int col, int row) {
        super.draw(g, renderer, map, col, row);
        renderer.drawWetSlope(g, col * GameConfig.TILE, row * GameConfig.TILE, isRisingRight());
    }
}
