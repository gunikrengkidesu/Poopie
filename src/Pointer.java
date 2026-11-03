/**
 * Whether the mouse is over something that can be clicked, so the window can show a hand cursor.
 * Before each mouse move the game calls reset(); buttons, cards and sliders call over() when the mouse is on them.
 */
public final class Pointer {
    private static boolean over;

    private Pointer() {}

    public static void reset() { over = false; }

    /** The mouse is on something clickable. */
    public static void over() { over = true; }

    public static boolean isOver() { return over; }
}
