import java.awt.Rectangle;

/**
 * Base class for every object that lives in a level (player, tissues, falling objects, door).
 * Position is the centre of the bottom edge, so x is the middle and y is where it stands.
 */
public abstract class Entity implements Updatable, Drawable {
    protected double x, y;
    protected int width, height;

    protected Entity(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) Math.round(x - width / 2.0), (int) Math.round(y - height), width, height);
    }

    public boolean intersects(Entity other) {
        return getBounds().intersects(other.getBounds());
    }

    public double getX() { return x; }
    public double getY() { return y; }
}
