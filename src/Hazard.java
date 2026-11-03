/** Something that ends the run when it touches the player. */
public abstract class Hazard extends Entity {
    protected Hazard(double x, double y, int width, int height) {
        super(x, y, width, height);
    }

    /** Whether touching it right now is deadly. */
    public abstract boolean isDangerous();

    /** Message shown on the game-over screen. */
    public abstract String getDeathMessage();

    /** Lets a hazard react to where the player is (does nothing by default). */
    public void observe(Player player) { }

    /** Called while the player's punch is out; hazards that can be punched override this. */
    public void punched(java.awt.Rectangle fist, int fromDirection) { }

    public boolean hits(Player player) {
        return isDangerous() && !player.isDead() && getBounds().intersects(player.getHurtBounds());
    }
}
