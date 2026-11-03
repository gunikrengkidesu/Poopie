import java.awt.Graphics2D;

/**
 * One square of the room. Subclasses describe how the square behaves (floor, ceiling, slope,
 * slipperiness) and how it looks. Tiles hold no per-square state, so one shared instance of each
 * kind is reused everywhere (flyweight).
 */
public abstract class Tile {
    public static final Tile EMPTY = new EmptyTile();
    public static final Tile SOLID = new SolidTile();
    public static final Tile WET_FLOOR = new WetFloorTile();
    public static final Tile SLOPE_UP_RIGHT = new SlopeTile(true);
    public static final Tile SLOPE_UP_LEFT = new SlopeTile(false);
    public static final Tile WET_SLOPE_UP_RIGHT = new WetSlopeTile(true);
    public static final Tile LADDER = new LadderTile(false);
    public static final Tile LADDER_TOP = new LadderTile(true);
    public static final Tile WET_SLOPE_UP_LEFT = new WetSlopeTile(false);
    public static final Tile UNDER_SLOPE_UP_RIGHT = new SlopeUndersideTile(true);
    public static final Tile UNDER_SLOPE_UP_LEFT = new SlopeUndersideTile(false);

    /** Builds the tile for one layout character (markers such as P, D, F become empty squares). */
    public static Tile fromChar(char c) {
        switch (c) {
            case '#': return SOLID;
            case '=': return WET_FLOOR;
            case '/': return SLOPE_UP_RIGHT;
            case '\\': return SLOPE_UP_LEFT;
            case '{': return WET_SLOPE_UP_RIGHT;
            case '}': return WET_SLOPE_UP_LEFT;
            case 'H': return LADDER;
            default: return EMPTY;
        }
    }

    public boolean isEmpty() { return false; }

    /** Blocks sideways movement as a full square. */
    public boolean blocksSideways() { return false; }

    /** Has a flat top you can stand on. */
    public boolean isFlatFloor() { return false; }

    public boolean isSlope() { return false; }

    public boolean isSlippery() { return false; }

    public boolean isLadder() { return false; }

    /** A floor you can land on from above but jump or climb through from below. */
    public boolean isOneWay() { return false; }

    /** -1 when gravity pulls you left along this tile, +1 for right, 0 for flat ground. */
    public int downhillDirection() { return 0; }

    /** Height of the walkable surface below the tile top at local x (0..TILE), or NaN for none. */
    public double floorOffset(double localX) { return Double.NaN; }

    /** Height of the underside below the tile top at local x, or NaN if a head passes through. */
    public double ceilingOffset(double localX) { return Double.NaN; }

    public abstract void draw(Graphics2D g, TileRenderer renderer, TileMap map, int col, int row);
}
