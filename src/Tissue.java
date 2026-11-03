import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/** A toilet-paper roll. Bobs up and down until collected, then floats away with a "+1". */
public class Tissue extends Collectible {
    private static final int SIZE = 24;
    private final int phase;
    private long ticks;
    private int pickupTimer = -1;

    /** Places the roll in the middle of grid square (col, row). */
    public Tissue(int col, int row, int phase) {
        super(col * GameConfig.TILE + GameConfig.TILE / 2.0, (row + 1) * GameConfig.TILE - 4, SIZE, SIZE);
        this.phase = phase;
    }

    @Override
    public Rectangle getBounds() {
        Rectangle r = super.getBounds();
        r.grow(-2, -2);
        return r;
    }

    @Override
    protected void onCollected() {
        pickupTimer = 0;
    }

    @Override
    public void update() {
        ticks++;
        if (pickupTimer >= 0 && pickupTimer < 30) pickupTimer++;
    }

    @Override
    public void draw(Graphics2D g) {
        BufferedImage img = Assets.image("res/Obj/toilet_paper.png");
        int cx = (int) x, cy = (int) (y - SIZE / 2.0);
        if (!isCollected()) {
            cy += (int) (Math.sin((ticks + phase * 17) / 14.0) * 3);
            int glow = 70 + (int) (Math.sin((ticks + phase * 9) / 9.0) * 30);
            g.setColor(new Color(255, 255, 200, glow));
            g.fillOval(cx - 16, cy - 16, 32, 32);
            if (img != null) g.drawImage(img, cx - SIZE / 2, cy - SIZE / 2, SIZE, SIZE, null);
            long s = (ticks + phase * 23) % 70;
            if (s < 10) {
                g.setColor(new Color(255, 255, 255, (int) (230 - s * 20)));
                g.fillRect(cx + 7, cy - 12, 2, 6);
                g.fillRect(cx + 5, cy - 10, 6, 2);
            }
        } else if (pickupTimer >= 0 && pickupTimer < 30 && img != null) {
            float a = 1f - pickupTimer / 30f;
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
            int size = SIZE + pickupTimer;
            int yy = cy - pickupTimer;
            g.drawImage(img, cx - size / 2, yy - size / 2, size, size, null);
            g.setComposite(old);
            g.setColor(new Color(255, 230, 120, (int) (255 * a)));
            g.setFont(UI.font(Font.BOLD, 8));
            UI.drawString(g, "+1", cx + 10, yy - 10);
        }
    }
}
