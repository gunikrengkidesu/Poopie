/** Fixed sizes shared by the whole game. */
public final class GameConfig {
    public static final int TILE = 32;
    public static final int COLS = 30;
    public static final int ROWS = 22;
    public static final int WIDTH = TILE * COLS;   // 960
    public static final int HEIGHT = TILE * ROWS;  // 704
    public static final int FPS = 60;
    public static final int LEVEL_COUNT = 4;

    private GameConfig() { }
}
