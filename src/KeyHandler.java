import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Collects keyboard input. Key presses are queued and handled on the game thread,
 * so menus and gameplay never race with drawing.
 */
public class KeyHandler implements KeyListener {
    private final ConcurrentLinkedQueue<Integer> pressed = new ConcurrentLinkedQueue<>();
    private volatile boolean left, right, jump, up, down;
    private volatile boolean jumpQueued, punchQueued;

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (isLeft(code)) left = true;
        if (isRight(code)) right = true;
        if (isUp(code)) up = true;
        if (isPunch(code)) punchQueued = true;
        if (code == KeyEvent.VK_M) AudioSettings.toggleMuted(); // M: all sound on/off
        if (isJump(code)) {
            if (!jump) jumpQueued = true;
            jump = true;
        }
        pressed.add(code);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (isLeft(code)) left = false;
        if (isRight(code)) right = false;
        if (isUp(code)) up = false;
        if (isJump(code)) jump = false;
    }

    /** Next queued key press, or -1 when there is none. */
    public int poll() {
        Integer c = pressed.poll();
        return c == null ? -1 : c;
    }

    public void clear() {
        pressed.clear();
        jumpQueued = false;
        punchQueued = false;
    }

    /** Lets automated tests hold keys without a real keyboard. */
    void simulate(boolean l, boolean r, boolean j) {
        simulate(l, r, j, false, false);
    }

    void simulate(boolean l, boolean r, boolean j, boolean u, boolean d) {
        left = l; right = r; up = u; down = d;
        if (j && !jump) jumpQueued = true;
        jump = j;
    }

    public boolean leftHeld() { return left; }
    public boolean rightHeld() { return right; }
    public boolean jumpHeld() { return jump; }
    public boolean upHeld() { return up; }
    public boolean downHeld() { return down; }

    public boolean consumePunch() {
        boolean p = punchQueued;
        punchQueued = false;
        return p;
    }

    public boolean consumeJump() {
        boolean j = jumpQueued;
        jumpQueued = false;
        return j;
    }

    // moving: A or LEFT, D or RIGHT; jumping: W, UP or SPACE (S and DOWN do nothing in the game)
    public static boolean isLeft(int c) { return c == KeyEvent.VK_LEFT || c == KeyEvent.VK_A; }
    public static boolean isRight(int c) { return c == KeyEvent.VK_RIGHT || c == KeyEvent.VK_D; }
    public static boolean isUp(int c) { return c == KeyEvent.VK_UP || c == KeyEvent.VK_W; }
    public static boolean isJump(int c) { return c == KeyEvent.VK_UP || c == KeyEvent.VK_W || c == KeyEvent.VK_SPACE; }
    public static boolean isPunch(int c) { return c == KeyEvent.VK_X || c == KeyEvent.VK_J; }
    public static boolean isConfirm(int c) { return c == KeyEvent.VK_ENTER || c == KeyEvent.VK_SPACE; }
}
