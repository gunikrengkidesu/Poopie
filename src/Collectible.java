/** Something the player picks up by touching it. */
public abstract class Collectible extends Entity {
    private boolean collected;

    protected Collectible(double x, double y, int width, int height) {
        super(x, y, width, height);
    }

    public boolean isCollected() { return collected; }

    /** Collects the item once; returns false if it was already taken. */
    public final boolean collect() {
        if (collected) return false;
        collected = true;
        onCollected();
        return true;
    }

    /** Hook for the pick-up effect. */
    protected abstract void onCollected();
}
