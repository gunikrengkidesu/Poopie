import java.awt.Graphics2D;

/** A wet, slippery floor: you keep sliding after letting go and turn around slowly. */
public class WetFloorTile extends SolidTile {
    @Override
    public boolean isSlippery() { return true; }

    @Override
    public void draw(Graphics2D g, TileRenderer renderer, TileMap map, int col, int row) {
        super.draw(g, renderer, map, col, row);
        renderer.drawWetOverlay(g, col * GameConfig.TILE, row * GameConfig.TILE);
    }
}
