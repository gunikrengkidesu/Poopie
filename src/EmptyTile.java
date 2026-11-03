import java.awt.Graphics2D;

/** Open air. */
public class EmptyTile extends Tile {
    @Override
    public boolean isEmpty() { return true; }

    @Override
    public void draw(Graphics2D g, TileRenderer renderer, TileMap map, int col, int row) {
        // nothing to draw: the room's wallpaper shows through
    }
}
