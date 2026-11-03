import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The grid of tiles for one room, plus the questions physics needs to ask about it. */
public class TileMap implements Updatable, Drawable {
    private static final int T = GameConfig.TILE;

    private final Room room;
    private final Tile[][] grid = new Tile[GameConfig.ROWS][GameConfig.COLS];
    private final List<int[]> hazardCells = new ArrayList<>();
    private final List<int[]> snakeCells = new ArrayList<>();
    private int spawnCol, spawnRow, doorCol, doorRow;
    private BufferedImage cachedImage;
    private long ticks;

    public TileMap(Room room) {
        this.room = room;
        String[] layout = room.getLayout();
        for (int r = 0; r < GameConfig.ROWS; r++) {
            for (int c = 0; c < GameConfig.COLS; c++) {
                char ch = c < layout[r].length() ? layout[r].charAt(c) : '#';
                grid[r][c] = Tile.fromChar(ch);
                if (ch == 'P') { spawnCol = c; spawnRow = r; }
                if (ch == 'D') { doorCol = c; doorRow = r; }
                if (ch == 'F') hazardCells.add(new int[] {c, r});
                if (ch == 'S') snakeCells.add(new int[] {c, r});
            }
        }
        // the highest rung of each ladder is a one-way floor
        for (int r = 0; r < GameConfig.ROWS; r++)
            for (int c = 0; c < GameConfig.COLS; c++)
                if (grid[r][c] == Tile.LADDER && (r == 0 || !grid[r - 1][c].isLadder())) grid[r][c] = Tile.LADDER_TOP;
        // every ramp gets a diagonal underside so it looks like a thick band
        for (int r = 0; r < GameConfig.ROWS - 1; r++)
            for (int c = 0; c < GameConfig.COLS; c++) {
                if (!grid[r + 1][c].isEmpty()) continue;
                if (!grid[r][c].isSlope()) continue;
                grid[r + 1][c] = ((SlopeTile) grid[r][c]).isRisingRight()
                        ? Tile.UNDER_SLOPE_UP_RIGHT : Tile.UNDER_SLOPE_UP_LEFT;
            }
    }

    // ------------------------------------------------------------------ queries
    /** Tile at a grid square; outside the room counts as solid wall. */
    public Tile tile(int col, int row) {
        if (col < 0 || row < 0 || col >= GameConfig.COLS || row >= GameConfig.ROWS) return Tile.SOLID;
        return grid[row][col];
    }

    public Tile tileAt(double x, double y) {
        return tile((int) Math.floor(x / T), (int) Math.floor(y / T));
    }

    /** World height of the floor in grid row `row` at world x, or NaN when that square has no floor there. */
    public double floorAt(double x, int row) {
        int col = (int) Math.floor(x / T);
        double offset = tile(col, row).floorOffset(x - col * T);
        return Double.isNaN(offset) ? Double.NaN : row * T + offset;
    }

    public double ceilingAt(double x, int row) {
        int col = (int) Math.floor(x / T);
        double offset = tile(col, row).ceilingOffset(x - col * T);
        return Double.isNaN(offset) ? Double.NaN : row * T + offset;
    }

    /** First floor at x between heights y0 and y1, or NaN. */
    public double groundBetween(double x, double y0, double y1) {
        return groundBetween(x, y0, y1, true);
    }

    /** Same, optionally ignoring one-way floors (the top of a ladder while climbing down it). */
    public double groundBetween(double x, double y0, double y1, boolean includeOneWay) {
        for (int r = (int) Math.floor(y0 / T); r <= (int) Math.floor(y1 / T); r++) {
            if (!includeOneWay && tile((int) Math.floor(x / T), r).isOneWay()) continue;
            double f = floorAt(x, r);
            if (!Double.isNaN(f) && f >= y0 - 0.001 && f <= y1) return f;
        }
        return Double.NaN;
    }

    /** Floor straight below a point. */
    public double floorBelow(double x, double y) {
        double f = groundBetween(x, y, GameConfig.HEIGHT);
        return Double.isNaN(f) ? GameConfig.HEIGHT - T : f;
    }

    public Room getRoom() { return room; }
    public int getSpawnCol() { return spawnCol; }
    public int getSpawnRow() { return spawnRow; }
    public int getDoorCol() { return doorCol; }
    public int getDoorRow() { return doorRow; }
    public List<int[]> getHazardCells() { return Collections.unmodifiableList(hazardCells); }
    public List<int[]> getSnakeCells() { return Collections.unmodifiableList(snakeCells); }

    // ------------------------------------------------------------------ update & draw
    @Override
    public void update() {
        ticks++;
    }

    @Override
    public void draw(Graphics2D g) {
        if (cachedImage == null) cachedImage = renderStatic();
        g.drawImage(cachedImage, 0, 0, null);
        drawWetShine(g);
    }

    /** Background, furniture and tiles never change, so they are drawn once and reused. */
    private BufferedImage renderStatic() {
        BufferedImage img = new BufferedImage(GameConfig.WIDTH, GameConfig.HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        room.drawBackground(g, GameConfig.WIDTH, GameConfig.HEIGHT);
        for (Decor d : room.getFurniture()) d.draw(g);
        TileRenderer renderer = new TileRenderer(room.getPlatformTexture());
        for (int r = 0; r < GameConfig.ROWS; r++)
            for (int c = 0; c < GameConfig.COLS; c++) grid[r][c].draw(g, renderer, this, c, r);
        g.dispose();
        return img;
    }

    /** Moving glints and drips so wet floors are easy to spot. */
    private void drawWetShine(Graphics2D g) {
        for (int r = 0; r < GameConfig.ROWS; r++)
            for (int c = 0; c < GameConfig.COLS; c++) {
                Tile tile = grid[r][c];
                if (!tile.isSlippery()) continue;
                int phase = (int) ((ticks / 2 + c * 7 + r * 13) % 90);
                if (phase < 10) {
                    int lx = phase * 3 + 1;
                    g.setColor(new Color(255, 255, 255, 210));
                    g.fillRect(c * T + lx, r * T + (int) tile.floorOffset(lx) - 1, 3, 3);
                }
                if (tile.isSlope()) continue;
                if ((c + r) % 3 == 0) {
                    int dy = (int) ((ticks + c * 11) % 40);
                    g.setColor(new Color(140, 210, 255, Math.max(0, 160 - dy * 3)));
                    g.fillOval(c * T + 14, r * T - 4 - dy / 6, 3, 3);
                }
            }
    }
}
