import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * The door behind the start point: the player has just come in through it from the previous room.
 * It is open when the level starts, then swings shut behind the player. It is only scenery.
 */
public class EntranceDoor implements Updatable, Drawable {
    // same white door as the exit (doorswindowsstairs_LRK.png): open doorway, half-open, closed
    private static final String SHEET = "res/Door/doorswindowsstairs_LRK.png";
    private static final int ROW_Y = 176, FRAME_W = 30;
    private static final int DOORWAY_X = 17, CLOSED_X = 113, HALF_OPEN_X = 161;
    private static final double SCALE = 1.25;
    private static final int CLOSE_AT = 35, CLOSED_AT = 43; // frames after the start

    private final int cx, bottom;
    private long ticks;

    public EntranceDoor(TileMap map) {
        cx = (int) ((map.getSpawnCol() + 0.5) * GameConfig.TILE);
        bottom = (map.getSpawnRow() + 1) * GameConfig.TILE;
    }

    @Override
    public void update() {
        ticks++;
    }

    @Override
    public void draw(Graphics2D g) {
        int frameX = ticks < CLOSE_AT ? DOORWAY_X : ticks < CLOSED_AT ? HALF_OPEN_X : CLOSED_X;
        int frameH = frameX == HALF_OPEN_X ? 51 : 48; // the swinging door hangs a little below the frame
        BufferedImage frame = Assets.crop(SHEET, frameX, ROW_Y, FRAME_W, frameH);
        if (frame == null) return;
        int w = (int) Math.round(FRAME_W * SCALE), h = (int) Math.round(frameH * SCALE);
        g.drawImage(frame, cx - w / 2, bottom - (int) Math.round(48 * SCALE), w, h, null);
    }
}
