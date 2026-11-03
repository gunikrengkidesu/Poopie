import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** A piece of background furniture. It is only drawn; it never blocks the player. */
public class Decor implements Drawable {
    private final String sprite;
    private final double col, bottomRow, scale;
    private final boolean flipped;
    private boolean framed;

    /** @param col left edge in tiles  @param bottomRow the tile row whose top edge the item stands on */
    public Decor(String sprite, double col, double bottomRow, double scale, boolean flipped) {
        this.sprite = sprite;
        this.col = col;
        this.bottomRow = bottomRow;
        this.scale = scale;
        this.flipped = flipped;
    }

    /** Hangs the picture in a wooden frame (used for posters). */
    public Decor framed() {
        framed = true;
        return this;
    }

    /** Where the item is drawn, in pixels (empty if the sprite is missing). */
    public java.awt.Rectangle getBounds() {
        BufferedImage img = Assets.sprite(sprite);
        if (img == null) return new java.awt.Rectangle();
        int t = GameConfig.TILE;
        int w = (int) Math.round(img.getWidth() * scale), h = (int) Math.round(img.getHeight() * scale);
        return new java.awt.Rectangle((int) Math.round(col * t), (int) Math.round(bottomRow * t) - h, w, h);
    }

    @Override
    public void draw(Graphics2D g) {
        BufferedImage img = Assets.sprite(sprite);
        if (img == null) return;
        if (flipped) img = Assets.flipX(img);
        int t = GameConfig.TILE;
        int w = (int) Math.round(img.getWidth() * scale), h = (int) Math.round(img.getHeight() * scale);
        int x = (int) Math.round(col * t), y = (int) Math.round(bottomRow * t) - h;
        if (framed) {
            g.setColor(new java.awt.Color(60, 36, 22));
            g.fillRect(x - 5, y - 5, w + 10, h + 10);
            g.setColor(new java.awt.Color(150, 98, 58));
            g.fillRect(x - 3, y - 3, w + 6, h + 6);
        }
        g.drawImage(img, x, y, w, h, null);
    }
}
