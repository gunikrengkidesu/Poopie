import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Something above the player's head that drops. Clocks, pictures and mirrors hang from a nail on the
 * wall and swing as a warning; everything else (cups, vases, books...) sits on a little shelf and wobbles.
 * Getting hit on the head ends the run.
 */
public class FallingObject extends Hazard {
    private enum State { WAITING, SHAKING, FALLING, BROKEN, RESPAWNING }

    private final Random random;
    private final double shelfTop, floorY;
    private final Difficulty difficulty;
    private final BufferedImage image;
    private final boolean hanging;

    private State state = State.WAITING;
    private double vy;
    private int timer;
    private long ticks;
    private final double[][] shards = new double[6][4];

    public FallingObject(TileMap map, int col, int row, String sprite, Difficulty difficulty, Random random) {
        super(col * GameConfig.TILE + GameConfig.TILE / 2.0, 0, 20, 20);
        this.random = random;
        this.difficulty = difficulty;
        this.image = Assets.sprite(sprite);
        this.hanging = hangsOnWall(sprite);
        if (image != null) {
            double scale = Math.min(28.0 / image.getWidth(), 28.0 / image.getHeight());
            scale = Math.max(scale, 0.7);
            if (image.getWidth() * scale < 18) scale = 18.0 / image.getWidth();
            width = (int) Math.round(image.getWidth() * scale);
            height = (int) Math.round(image.getHeight() * scale);
        }
        this.shelfTop = row * GameConfig.TILE + 6;
        this.floorY = map.floorBelow(x, shelfTop + height + 2);
        reset(true);
    }

    /** Wall decorations hang from a nail instead of standing on a shelf. */
    private static boolean hangsOnWall(String sprite) {
        return sprite.equals("clock") || sprite.equals("mirror") || sprite.startsWith("picture")
                || sprite.startsWith("painting") || sprite.endsWith("Painting") || sprite.endsWith("Portrait")
                || sprite.equals("monaLisa") || sprite.equals("vanGogh");
    }

    private void reset(boolean firstTime) {
        state = State.WAITING;
        y = shelfTop + height;
        vy = 0;
        timer = (int) ((firstTime ? 90 : 60) + random.nextInt(150) / difficulty.getDropRate());
    }

    @Override
    public boolean isDangerous() {
        return state == State.FALLING;
    }

    @Override
    public String getDeathMessage() {
        return "HIT ON THE HEAD!";
    }

    @Override
    public Rectangle getBounds() {
        Rectangle r = super.getBounds();
        return new Rectangle(r.x + 3, r.y + 3, r.width - 6, r.height - 4);
    }

    /** The shelf notices someone standing underneath and drops sooner. */
    @Override
    public void observe(Player player) {
        boolean below = Math.abs(player.getX() - x) < GameConfig.TILE * 1.2
                && player.getY() > shelfTop && player.getY() <= floorY + 2;
        if (state == State.WAITING && below && timer > 25) timer -= 2;
    }

    @Override
    public void update() {
        ticks++;
        switch (state) {
            case WAITING:
                if (--timer <= 0) { state = State.SHAKING; timer = difficulty.getWarningFrames(); }
                break;
            case SHAKING:
                if (--timer <= 0) { state = State.FALLING; vy = 1; }
                break;
            case FALLING:
                vy = Math.min(vy + difficulty.getFallGravity(), 12);
                y += vy;
                if (y >= floorY) breakOnFloor();
                break;
            case BROKEN:
                for (double[] s : shards) { s[0] += s[2]; s[1] += s[3]; s[3] += 0.3; }
                if (--timer <= 0) { state = State.RESPAWNING; timer = 70; }
                break;
            case RESPAWNING:
                if (--timer <= 0) reset(false);
                break;
            default:
                break;
        }
    }

    private void breakOnFloor() {
        y = floorY;
        state = State.BROKEN;
        timer = 32;
        for (double[] s : shards) {
            s[0] = x;
            s[1] = floorY - 6;
            s[2] = (random.nextDouble() - 0.5) * 5;
            s[3] = -1.5 - random.nextDouble() * 3;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (hanging) drawNail(g); else drawShelf(g);
        if (state == State.SHAKING || state == State.FALLING) drawWarningShadow(g);
        if (state == State.SHAKING && (ticks / 6) % 2 == 0) {
            g.setColor(new Color(230, 40, 40));
            g.setFont(UI.font(Font.BOLD, 16));
            UI.drawString(g, "!", (int) x - 7, (int) shelfTop - 2);
        }
        double restingBottom = shelfTop + height;
        switch (state) {
            case WAITING:
                if (hanging) drawHanging(g, 1f, 0);
                else drawItem(g, x, restingBottom, 1f, 0);
                break;
            case SHAKING: {
                if (hanging) { // swings on its nail, faster and wider as the string gives way
                    double t = 1 - timer / (double) difficulty.getWarningFrames();
                    drawHanging(g, 1f, Math.sin(ticks * (0.35 + 0.25 * t)) * (8 + 10 * t));
                } else {
                    double wobble = Math.sin(ticks * 1.3) * 2.5;
                    drawItem(g, x + wobble, restingBottom, 1f, wobble * 3);
                }
                break;
            }
            case FALLING:
                drawItem(g, x, y, 1f, vy * 2);
                break;
            case BROKEN:
                g.setColor(new Color(90, 70, 60, Math.max(0, timer * 7)));
                for (double[] s : shards) g.fillRect((int) s[0], (int) s[1], 4, 4);
                break;
            case RESPAWNING: {
                float alpha = Math.max(0f, Math.min(1f, 1f - timer / 70f));
                if (hanging) drawHanging(g, alpha, 0);
                else drawItem(g, x, restingBottom, alpha, 0);
                break;
            }
            default:
                break;
        }
    }

    /** The little wall shelf the item stands on: the long one for wide items, the short one for small ones. */
    private void drawShelf(Graphics2D g) {
        boolean wide = width > 22;
        BufferedImage shelf = Assets.sprite(wide ? "shelfLarge" : "shelfMedium");
        int w = wide ? 56 : 28, sx = (int) Math.round(x - w / 2.0), sy = (int) (shelfTop + height);
        if (shelf != null) {
            g.drawImage(shelf, sx, sy - 1, w, 10, null);
            return;
        }
        g.setColor(new Color(110, 72, 45));
        g.fillRect(sx, sy, w, 5);
    }

    private double nailY() { return shelfTop - 4; }

    /** A small nail in the wall (it stays there after the item falls). */
    private void drawNail(Graphics2D g) {
        int nx = (int) Math.round(x), ny = (int) Math.round(nailY());
        g.setColor(new Color(70, 60, 55));
        g.fillRect(nx - 2, ny - 2, 4, 4);
        g.setColor(new Color(170, 165, 160));
        g.fillRect(nx - 1, ny - 2, 2, 1);
    }

    /** The item hanging from its nail on a string, turned by `angleDeg` around the nail. */
    private void drawHanging(Graphics2D g2, float alpha, double angleDeg) {
        Graphics2D g = (Graphics2D) g2.create();
        if (alpha < 1f) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        double nx = x, ny = nailY();
        g.rotate(Math.toRadians(angleDeg), nx, ny);
        int left = (int) Math.round(x - width / 2.0 + 3), right = (int) Math.round(x + width / 2.0 - 3);
        int top = (int) Math.round(shelfTop + 2);
        g.setColor(new Color(90, 70, 55));
        g.drawLine((int) nx, (int) ny, left, top);
        g.drawLine((int) nx, (int) ny, right, top);
        if (image != null) g.drawImage(image, (int) Math.round(x - width / 2.0), (int) Math.round(shelfTop), width, height, null);
        else {
            g.setColor(new Color(160, 90, 60));
            g.fillRect((int) (x - width / 2.0), (int) shelfTop, width, height);
        }
        g.dispose();
        drawNail(g2);
    }

    private void drawWarningShadow(Graphics2D g) {
        double progress = state == State.FALLING
                ? Math.min(1, (y - shelfTop - height) / Math.max(1, floorY - shelfTop - height)) : 0.2;
        int w = (int) (12 + 16 * progress);
        g.setColor(new Color(0, 0, 0, (int) (60 + 80 * progress)));
        g.fillOval((int) x - w / 2, (int) floorY - 4, w, 6);
    }

    private void drawItem(Graphics2D g2, double cx, double bottom, float alpha, double rotationDeg) {
        double top = bottom - height;
        Graphics2D g = (Graphics2D) g2.create();
        if (alpha < 1f) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.rotate(Math.toRadians(rotationDeg), cx, top + height / 2.0);
        if (image != null) g.drawImage(image, (int) Math.round(cx - width / 2.0), (int) Math.round(top), width, height, null);
        else {
            g.setColor(new Color(160, 90, 60));
            g.fillRect((int) (cx - width / 2.0), (int) top, width, height);
        }
        g.dispose();
    }
}
