/**
 * How hard a level is, from 1 to 4 stars (level 1 has 1 star ... level 4 has 4). Everything (time, falling
 * objects, snakes) is read from the star rating, so the stars on the level select screen tell the truth.
 */
public final class Difficulty {
    public static final int MAX_STARS = 4;
    //                                          stars:   1     2     3     4
    private static final double[] SECONDS_PER_ROLL  = {18.0, 16.0, 14.5, 13.0}; // time limit per tissue
    private static final double[] DROP_RATE         = {0.80, 1.10, 1.40, 1.70}; // how often shelves drop things
    private static final int[] WARNING_FRAMES       = {  60,   50,   42,   34}; // wobble time before a drop
    private static final double[] FALL_GRAVITY      = {0.28, 0.32, 0.36, 0.40}; // how fast things fall
    private static final double[] SNAKE_SPEED       = {   0,  0.7,  0.9, 0.55}; // snakes from 2 stars up
    private static final double[] SNAKE_CHASE_SPEED = {   0,    0,    0, 0.95}; // 4 stars: snakes speed up when you come close

    private final int stars;

    private Difficulty(int stars) {
        this.stars = Math.max(1, Math.min(MAX_STARS, stars));
    }

    public static Difficulty ofStars(int stars) {
        return new Difficulty(stars);
    }

    public int getStars() { return stars; }

    /** Time limit in seconds for a room with this many tissue rolls. */
    public int getTimeLimit(int tissueCount) {
        return (int) Math.round(tissueCount * SECONDS_PER_ROLL[stars - 1] / 5.0) * 5;
    }

    public double getDropRate() { return DROP_RATE[stars - 1]; }
    public int getWarningFrames() { return WARNING_FRAMES[stars - 1]; }
    public double getFallGravity() { return FALL_GRAVITY[stars - 1]; }

    public boolean hasSnakes() { return stars >= 2; }
    public double getSnakeSpeed() { return SNAKE_SPEED[stars - 1]; }

    /** 4 stars: a snake chases the player along its whole floor until they climb to a higher floor. */
    public double getSnakeChaseSpeed() { return SNAKE_CHASE_SPEED[stars - 1]; }
}
