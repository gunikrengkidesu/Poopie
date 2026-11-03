import java.awt.Graphics2D;

/** A brick wall or platform square. */
public class SolidTile extends Tile {
    @Override
    public boolean blocksSideways() { return true; }

    @Override
    public boolean isFlatFloor() { return true; }

    @Override
    public double floorOffset(double localX) { return 0; }

    @Override
    public double ceilingOffset(double localX) { return GameConfig.TILE; }

    private static boolean connects(Tile t) {
        return !t.isEmpty() && !t.isLadder();
    }

    @Override
    public void draw(Graphics2D g, TileRenderer renderer, TileMap map, int col, int row) {
        boolean n = connects(map.tile(col, row - 1));
        boolean s = connects(map.tile(col, row + 1)) && !map.tile(col, row + 1).isSlope();
        boolean w = connects(map.tile(col - 1, row));
        boolean e = connects(map.tile(col + 1, row));
        renderer.drawBrick(g, col * GameConfig.TILE, row * GameConfig.TILE, n, s, w, e);
    }
}
