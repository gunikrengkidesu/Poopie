import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/** The bathroom door. Locked until every tissue is collected, then it swings open. */
public class Door extends Entity {
    private int openTimer = -1, closeTimer = -1;
    private int collected, required;
    private boolean showsToilet; // last level: the open door shows the bathroom with the toilet
    private long ticks;

    public Door(TileMap map) {
        super((map.getDoorCol() + 0.5) * GameConfig.TILE, (map.getDoorRow() + 1) * GameConfig.TILE, 24, 44);
    }

    /** In the last level the open door looks into the bathroom, toilet and all. */
    public void setShowsToilet(boolean showsToilet) {
        this.showsToilet = showsToilet;
    }

    public void setProgress(int collected, int required) {
        this.collected = collected;
        this.required = required;
    }

    public void open() {
        if (openTimer < 0) openTimer = 0;
    }

    public boolean isOpen() {
        return openTimer >= 0 && closeTimer < 0;
    }

    /** Swings shut behind the player. */
    public void close() {
        if (closeTimer < 0) closeTimer = 0;
    }

    public boolean isClosed() {
        return closeTimer > 30;
    }

    /** True once the door has finished opening and the player is standing in the doorway. */
    public boolean canEnter(Player player) {
        return openTimer > 25 && player.isOnGround() && getBounds().intersects(player.getBounds());
    }

    @Override
    public void update() {
        ticks++;
        if (openTimer >= 0) openTimer++;
        if (closeTimer >= 0) closeTimer++;
    }

    // the white door row of doorswindowsstairs_LRK.png: dark doorway, closed door, half-open door
    private static final String SHEET = "res/Door/doorswindowsstairs_LRK.png";
    private static final int ROW_Y = 176, FRAME_W = 30;
    private static final int DOORWAY_X = 17, EMPTY_FRAME_X = 65, CLOSED_X = 113, HALF_OPEN_X = 161;
    private static final double SCALE = 1.25;

    @Override
    public void draw(Graphics2D g) {
        int cx = (int) x, bottom = (int) y;
        int doorH = (int) Math.round(48 * SCALE), top = bottom - doorH;
        // the tissue counter goes above the door; a door near the ceiling has it beside it
        boolean beside = top - 24 < 40;
        int labelX = beside ? cx + 52 : cx;                 // centre of the counter
        int counterY = beside ? top + 16 : top - 24;

        // closed until every tissue is collected; opens (half-open, then wide open) and shuts behind the player
        int frameX;
        if (closeTimer >= 0) frameX = closeTimer < 8 ? DOORWAY_X : closeTimer < 16 ? HALF_OPEN_X : CLOSED_X;
        else if (!isOpen()) frameX = CLOSED_X;
        else frameX = openTimer < 10 ? HALF_OPEN_X : DOORWAY_X;
        if (isOpen()) {
            int glow = 90 + (int) (Math.sin(ticks / 8.0) * 50);
            g.setColor(new Color(255, 245, 160, glow));
            g.fillOval(cx - 32, top - 6, 64, doorH + 14);
        }
        int w = (int) Math.round(FRAME_W * SCALE);
        if (showsToilet && frameX == DOORWAY_X) {
            drawBathroom(g, cx - w / 2, top, w, doorH);
            frameX = EMPTY_FRAME_X;                         // just the frame, so the bathroom shows through
        }
        int frameH = frameX == HALF_OPEN_X ? 51 : 48; // the swinging door hangs a little below the frame
        BufferedImage frame = Assets.crop(SHEET, frameX, ROW_Y, FRAME_W, frameH);
        if (frame != null) g.drawImage(frame, cx - w / 2, top, w, (int) Math.round(frameH * SCALE), null);

        if (openTimer < 0) {
            // tissue counter
            g.setColor(new Color(40, 30, 30, 200));
            g.fillRoundRect(labelX - 24, counterY, 48, 20, 8, 8);
            BufferedImage roll = Assets.image("res/Obj/toilet_paper.png");
            if (roll != null) g.drawImage(roll, labelX - 21, counterY + 2, 16, 16, null);
            g.setColor(Color.WHITE);
            g.setFont(UI.font(Font.BOLD, 8));
            UI.drawString(g, collected + "/" + required, labelX - 3, counterY + 14);
        } else if (closeTimer < 0 && (ticks / 15) % 2 == 0) {
            // bouncing arrow: come in!
            g.setColor(new Color(60, 200, 90));
            int ay = counterY + 4 + (int) (Math.sin(ticks / 6.0) * 3);
            if (beside) g.fillPolygon(new int[] {labelX + 6, labelX + 6, labelX - 6}, new int[] {ay - 6, ay + 10, ay + 2}, 3); // points at the door
            else g.fillPolygon(new int[] {cx - 9, cx + 9, cx}, new int[] {ay, ay, ay + 12}, 3);
        }
    }

    /** The bathroom seen through the open door: tiled wall, floor, and a toilet waiting at the back. */
    private void drawBathroom(Graphics2D g, int x, int y, int w, int h) {
        int b = (int) Math.round(3 * SCALE);              // the frame's border
        int ix = x + b, iy = y + b, iw = w - 2 * b, ih = h - b;
        g.setColor(new Color(196, 226, 240));             // pale blue tiles
        g.fillRect(ix, iy, iw, ih);
        g.setColor(new Color(160, 200, 222));
        for (int ty = iy + 6; ty < iy + ih; ty += 7) g.fillRect(ix, ty, iw, 1);
        for (int tx = ix + 6; tx < ix + iw; tx += 7) g.fillRect(tx, iy, 1, ih);
        int floorY = iy + ih - 9;
        g.setColor(new Color(120, 150, 170));             // floor
        g.fillRect(ix, floorY, iw, 9);
        g.setColor(new Color(255, 250, 210, 120));        // warm light from the ceiling
        g.fillOval(ix + iw / 2 - 12, iy - 6, 24, 18);
        BufferedImage toilet = Assets.sprite("toiletH");
        if (toilet != null) {
            int th = (int) (ih * 0.5), tw = toilet.getWidth() * th / toilet.getHeight();
            g.drawImage(toilet, ix + iw / 2 - tw / 2, floorY + 3 - th, tw, th, null);
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int) x - 12, (int) y - 44, 24, 44);
    }
}
