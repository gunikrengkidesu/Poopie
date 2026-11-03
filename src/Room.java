import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A room of the house (one level). Each subclass supplies its own layout, wallpaper and furniture.
 *
 * Layout legend:
 *   #  wall / platform        =  slippery wet floor
 *   /  slope rising right     \  slope rising left
 *   {  wet slope rising right    }  wet slope rising left
 *   F  shelf that drops things   P  player start   D  bathroom door
 *   H  ladder                    S  snake 
 * Tissue rolls are not in the layout: they are placed at random each time the level starts.
 */
public abstract class Room {
    private static Room[] all;

    private final int number;
    private final String title, icon;
    private final int tissueCount;
    private final Difficulty difficulty;
    private final String[] fallingSprites;
    private final Color wallTop, wallBottom;
    private List<Decor> furniture;

    protected Room(int number, String title, String icon, int stars, int tissueCount,
                   String[] fallingSprites, Color wallTop, Color wallBottom) {
        this.number = number;
        this.title = title;
        this.icon = icon;
        this.tissueCount = tissueCount;
        this.difficulty = Difficulty.ofStars(stars);
        this.fallingSprites = fallingSprites;
        this.wallTop = wallTop;
        this.wallBottom = wallBottom;
    }

    /** All rooms in play order. */
    public static Room[] all() {
        if (all == null) all = new Room[] {new LivingRoom(), new Kitchen(), new Bedroom(), new StudyRoom()};
        return all;
    }

    public static Room get(int number) {
        return all()[number - 1];
    }

    // ------------------------------------------------------------------ what each room must provide
    public abstract String[] getLayout();

    protected abstract void drawWallpaper(Graphics2D g, int width, int height);

    protected abstract void addFurniture(List<Decor> list);

    // ------------------------------------------------------------------ shared behaviour
    /** Template method: gradient wall, the room's own wallpaper, then a soft shadow. */
    public final void drawBackground(Graphics2D g, int width, int height) {
        g.setPaint(new GradientPaint(0, 0, wallTop, 0, height, wallBottom));
        g.fillRect(0, 0, width, height);
        drawWallpaper(g, width, height);
        g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 0), 0, height, new Color(0, 0, 0, 45)));
        g.fillRect(0, 0, width, height);
    }

    public List<Decor> getFurniture() {
        if (furniture == null) {
            List<Decor> list = new ArrayList<>();
            addFurniture(list);
            furniture = Collections.unmodifiableList(list);
        }
        return furniture;
    }

    private static final String WALL_SHEET = "res/Bg/tilemap.png";
    private static final int WALL_SCALE = 2;

    /**
     * A light cream wall from tilemap.png (x 80-95: wall y 55-93, skirting y 94-95), with a skirting board along
     * every floor, only where a floor really is (not across open stairwells).
     */
    protected void paneledWall(Graphics2D g, int w, int h) {
        String[] map = getLayout();
        wallTile(g, Assets.crop(WALL_SHEET, 80, 55, 16, 39), 0, 0, w, h);
        BufferedImage skirting = Assets.crop(WALL_SHEET, 80, 94, 16, 2);
        int t = GameConfig.TILE;
        for (int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[r].length(); c++) {
                if (!flat(map, r, c)) continue;
                if (r > 0 && open(map, r - 1, c)) wallTile(g, skirting, c * t, r * t - 2 * WALL_SCALE, t, 2 * WALL_SCALE);
            }
        }
    }

    private static boolean flat(String[] map, int r, int c) {
        char ch = map[r].charAt(c);
        return ch == '#' || ch == '=';
    }

    private static boolean open(String[] map, int r, int c) {
        return !flat(map, r, c) && "/\\{}".indexOf(map[r].charAt(c)) < 0;
    }

    /** Repeats a piece of the sheet (scaled up) over a strip of the wall. */
    private static void wallTile(Graphics2D g, BufferedImage img, int x, int y, int w, int h) {
        if (img == null) return;
        int tw = img.getWidth() * WALL_SCALE, th = img.getHeight() * WALL_SCALE;
        Shape clip = g.getClip();
        g.clipRect(x, y, w, h);
        for (int ty = y; ty < y + h; ty += th)
            for (int tx = x; tx < x + w; tx += tw) g.drawImage(img, tx, ty, tw, th, null);
        g.setClip(clip);
    }

    /** Covers the wall with a small repeating image file, scaled up so its pixels match the game's pixel art. */
    protected void wallpaperImage(Graphics2D g, String path, int scale, int width, int height) {
        BufferedImage tex = Assets.image(path);
        if (tex == null) return;
        g.setPaint(new TexturePaint(tex, new Rectangle2D.Double(0, 0, tex.getWidth() * scale, tex.getHeight() * scale)));
        g.fillRect(0, 0, width, height);
    }

    /** Texture for platforms in this room, or null for the default pink bricks. */
    public BufferedImage getPlatformTexture() {
        return null;
    }

    protected static void add(List<Decor> list, String sprite, double col, double bottomRow, double scale) {
        list.add(new Decor(sprite, col, bottomRow, scale, false));
    }

    protected static void addFramed(List<Decor> list, String sprite, double col, double bottomRow, double scale) {
        list.add(new Decor(sprite, col, bottomRow, scale, false).framed());
    }

    protected static void addFlipped(List<Decor> list, String sprite, double col, double bottomRow, double scale) {
        list.add(new Decor(sprite, col, bottomRow, scale, true));
    }

    public int getNumber() { return number; }
    public String getTitle() { return title; }
    public String getIcon() { return icon; }
    public Difficulty getDifficulty() { return difficulty; }
    public int getTimeLimit() { return difficulty.getTimeLimit(tissueCount); }
    public int getTissueCount() { return tissueCount; }
    public String getFallingSprite(int index) { return fallingSprites[index % fallingSprites.length]; }
    public Color getWallTop() { return wallTop; }
    public Color getWallBottom() { return wallBottom; }
}
