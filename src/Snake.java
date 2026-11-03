import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * A snake that slithers back and forth along its platform (only in rooms with 2 stars or more).
 * It turns back at wet floor and never crosses it.
 * When the player comes close on the same floor it turns towards them; in 4-star rooms it also speeds up.
 * Touching it ends the run. A punch knocks it back and leaves it dizzy for a few seconds.
 * Sprite: res/Obj/SnakeGreen-Walk.png.
 */
public class Snake extends Hazard {
    private static final String SHEET = "res/Obj/SnakeGreen-Walk.png";
    private static final int FRAMES = 7;
    private static final int STUN_FRAMES = 180;
    private static final int MAX_RANGE = 5;      // tiles it may wander from its start
    private final double minX, maxX, speed, chaseSpeed;
    private double knockback;
    private int direction = 1, stunned, pause;
    private boolean chasing;
    private long ticks;

    public Snake(TileMap map, int col, int row, Difficulty difficulty) {
        super((col + 0.5) * GameConfig.TILE, (row + 1) * GameConfig.TILE, 30, 22);
        this.speed = difficulty.getSnakeSpeed();
        this.chaseSpeed = difficulty.getSnakeChaseSpeed();
        int t = GameConfig.TILE;
        int range = MAX_RANGE;
        int left = col, right = col;
        while (left > col - range && walkable(map, left - 1, row)) left--;
        while (right < col + range && walkable(map, right + 1, row)) right++;
        minX = left * t + width / 2.0;
        maxX = (right + 1) * t - width / 2.0;
    }

    /** Flat, dry floor with nothing in the way: a snake turns back at wet floor and never crosses it. */
    private static boolean walkable(TileMap map, int c, int r) {
        Tile body = map.tile(c, r), floor = map.tile(c, r + 1);
        return !body.blocksSideways() && !body.isSlope() && !body.isLadder()
                && floor.isFlatFloor() && !floor.isOneWay() && !floor.isSlippery();
    }

    @Override
    public boolean isDangerous() { return stunned == 0; }

    @Override
    public String getDeathMessage() { return "BITTEN BY A SNAKE!"; }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int) (x - width / 2.0) + 2, (int) (y - height), width - 4, height);
    }

    /** Turns towards the player when they are close on the same floor; in 4-star rooms it also speeds up. */
    @Override
    public void observe(Player player) {
        chasing = false;
        if (stunned > 0 || player.isDead()) return;
        double dx = player.getX() - x;
        boolean close = Math.abs(player.getY() - y) < 8 && Math.abs(dx) < GameConfig.TILE * 3;
        if (!close) return;
        if (chaseSpeed > 0) { chasing = true; pause = 0; }
        else if (pause > 0) return;
        direction = dx > 0 ? 1 : -1;
    }

    @Override
    public void punched(Rectangle fist, int fromDirection) {
        if (stunned > 0 || !fist.intersects(getBounds())) return;
        stunned = STUN_FRAMES;
        knockback = fromDirection * 4;
        direction = -fromDirection;
    }

    @Override
    public void update() {
        ticks++;
        if (stunned > 0) {
            stunned--;
            x += knockback;
            knockback *= 0.85;
        } else if (pause > 0) {
            pause--;
        } else if (chasing) {
            x += direction * chaseSpeed;
        } else {
            x += direction * speed;
            if (ticks % 240 == 0) pause = 40; // stops now and then to flick its tongue
        }
        // at the end of its floor: a chasing snake waits there facing the player, otherwise it turns back
        if (x < minX) { x = minX; if (!chasing) direction = 1; }
        if (x > maxX) { x = maxX; if (!chasing) direction = -1; }
    }

    @Override
    public void draw(Graphics2D g) {
        // res/Obj/SnakeGreen-Walk.png: 7 frames of 32x32, facing right
        BufferedImage sheet = Assets.image(SHEET);
        boolean dizzy = stunned > 0;
        int frame = dizzy || pause > 0 ? 0 : (int) ((ticks / 6) % FRAMES);
        double scale = 1.3;
        int size = (int) Math.round(32 * scale);
        int dx = (int) Math.round(x - size / 2.0), dy = (int) Math.round(y - size);
        if (sheet != null) {
            Graphics2D gg = (Graphics2D) g.create();
            if (dizzy) gg.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER,
                    (ticks / 5) % 2 == 0 ? 0.55f : 0.85f));
            if (direction < 0) gg.drawImage(sheet, dx + size, dy, dx, dy + size, frame * 32, 0, frame * 32 + 32, 32, null);
            else gg.drawImage(sheet, dx, dy, dx + size, dy + size, frame * 32, 0, frame * 32 + 32, 32, null);
            gg.dispose();
        } else {
            g.setColor(new Color(70, 170, 70));
            g.fill(getBounds());
        }
        int hx = (int) Math.round(x + direction * size * 0.25), hy = (int) y - size + 8;
        if (!dizzy && pause > 0 && (ticks / 6) % 2 == 0) { // flicking tongue while it stops
            g.setColor(new Color(220, 40, 60));
            int tx = hx + direction * 8;
            g.fillRect(Math.min(tx, tx + direction * 6), hy + 4, 6, 2);
        }
        if (dizzy) { // spinning stars over a dizzy snake
            g.setColor(new Color(255, 220, 60));
            g.setFont(UI.font(Font.BOLD, 8));
            for (int i = 0; i < 3; i++) {
                double a = ticks * 0.12 + i * 2.1;
                UI.drawString(g, "*", (float) (hx + Math.cos(a) * 10 - 3), (float) (hy - 4 + Math.sin(a) * 3));
            }
        }
    }
}
