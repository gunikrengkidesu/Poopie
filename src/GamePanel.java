import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * The game window's canvas. Runs the 60 FPS loop, forwards key presses to the current Screen and
 * draws it at 960x704, scaled to fit the window.
 */
public class GamePanel extends JPanel implements Runnable {
    private final KeyHandler keys = new KeyHandler();
    private final Progress progress;
    private final List<CharacterSkin> characters;
    private int selectedCharacter;
    private Screen screen;
    private long frameCount;

    private Thread loop;
    private final java.util.concurrent.ConcurrentLinkedQueue<int[]> mouseEvents = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private final BufferedImage[] buffers = new BufferedImage[2];
    private volatile BufferedImage front;
    private int back;

    public GamePanel(Progress progress) {
        this.progress = progress;
        this.characters = Collections.unmodifiableList(Arrays.asList(
                new CharacterSkin("VICKY", "Char-1", "Character1F_3_"),
                new CharacterSkin("HENRY", "Char-2", "Character1M_1_")));
        for (int i = 0; i < buffers.length; i++)
            buffers[i] = new BufferedImage(GameConfig.WIDTH, GameConfig.HEIGHT, BufferedImage.TYPE_INT_RGB);
        setPreferredSize(new Dimension(GameConfig.WIDTH, GameConfig.HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(keys);
        java.awt.event.MouseAdapter mouse = new java.awt.event.MouseAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) { queueMouse(0, e); }
            @Override public void mouseDragged(java.awt.event.MouseEvent e) { queueMouse(2, e); }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { queueMouse(3, e); }
            @Override public void mousePressed(java.awt.event.MouseEvent e) { requestFocusInWindow(); queueMouse(1, e); }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        screen = new TitleScreen(this);
    }

    // ------------------------------------------------------------------ shared game state
    public KeyHandler getKeys() { return keys; }
    public Progress getProgress() { return progress; }
    public List<CharacterSkin> getCharacters() { return characters; }
    public CharacterSkin getSelectedCharacter() { return characters.get(selectedCharacter); }
    public int getSelectedCharacterIndex() { return selectedCharacter; }
    public long getFrameCount() { return frameCount; }

    public void setSelectedCharacterIndex(int index) {
        selectedCharacter = Math.max(0, Math.min(characters.size() - 1, index));
    }

    public void setScreen(Screen next) {
        screen = next;
        showPointer(false); // the new screen sets it again when the mouse moves
    }

    private boolean handCursor;

    /** Hand cursor over buttons, cards and sliders; the normal arrow everywhere else. */
    private void showPointer(boolean hand) {
        if (hand == handCursor) return;
        handCursor = hand;
        java.awt.Cursor c = java.awt.Cursor.getPredefinedCursor(hand ? java.awt.Cursor.HAND_CURSOR : java.awt.Cursor.DEFAULT_CURSOR);
        javax.swing.SwingUtilities.invokeLater(() -> setCursor(c));
    }

    public PlayScreen startLevel(int number) {
        return startLevel(number, new Random());
    }

    public PlayScreen startLevel(int number, Random random) {
        PlayScreen play = new PlayScreen(this, number, random);
        setScreen(play);
        return play;
    }

    // ------------------------------------------------------------------ loop
    public void start() {
        AudioSettings.loadFromHome();
        Music.start();
        Sfx.enable();
        loop = new Thread(this, "game-loop");
        loop.start();
    }

    @Override
    public void run() {
        final double frameNanos = 1_000_000_000.0 / GameConfig.FPS;
        double next = System.nanoTime() + frameNanos;
        while (loop != null) {
            update();
            render();
            repaint();
            try {
                double wait = (next - System.nanoTime()) / 1_000_000;
                if (wait < 0) { wait = 0; next = System.nanoTime(); }
                Thread.sleep((long) wait);
                next += frameNanos;
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    /** One frame of game logic (also used by tests). */
    public void update() {
        frameCount++;
        int code;
        while ((code = keys.poll()) != -1) {
            if (KeyHandler.isConfirm(code) && isMenu()) Sfx.play(Sfx.ENTER);
            screen.keyPressed(code);
        }
        int[] m;
        while ((m = mouseEvents.poll()) != null) {
            if (m[0] == 1 && isMenu()) Sfx.play(Sfx.ENTER);
            if (m[0] == 1) screen.mouseClicked(m[1], m[2]);
            else if (m[0] == 2) { Pointer.reset(); screen.mouseDragged(m[1], m[2]); showPointer(Pointer.isOver()); }
            else if (m[0] == 3) screen.mouseReleased(m[1], m[2]);
            else { Pointer.reset(); screen.mouseMoved(m[1], m[2]); showPointer(Pointer.isOver()); }
        }
        screen.update();
    }

    /** A menu or pop-up that is listening to input (not gameplay). */
    private boolean isMenu() {
        return !(screen instanceof PlayScreen) && screen.acceptsInput();
    }

    private void render() {
        BufferedImage img = buffers[back];
        Graphics2D g = img.createGraphics();
        drawFrame(g);
        g.dispose();
        front = img;
        back = 1 - back;
    }

    private void drawFrame(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        screen.draw(g);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage img = front;
        if (img == null) return;
        Graphics2D g2 = (Graphics2D) g;
        double scale = Math.min((double) getWidth() / GameConfig.WIDTH, (double) getHeight() / GameConfig.HEIGHT);
        int w = (int) (GameConfig.WIDTH * scale), h = (int) (GameConfig.HEIGHT * scale);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, scale >= 1
                ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR : RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(img, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
    }

    /** Converts a window position to game pixels (the picture is scaled and centred) and queues it. */
    private void queueMouse(int type, java.awt.event.MouseEvent e) {
        double scale = Math.min((double) getWidth() / GameConfig.WIDTH, (double) getHeight() / GameConfig.HEIGHT);
        int ox = (int) ((getWidth() - GameConfig.WIDTH * scale) / 2), oy = (int) ((getHeight() - GameConfig.HEIGHT * scale) / 2);
        mouseEvents.add(new int[] {type, (int) ((e.getX() - ox) / scale), (int) ((e.getY() - oy) / scale)});
    }

    /** Saves the current screen as a PNG (no window needed). */
    public void saveScreenshot(File out) throws Exception {
        BufferedImage img = new BufferedImage(GameConfig.WIDTH, GameConfig.HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        drawFrame(g);
        g.dispose();
        ImageIO.write(img, "png", out);
    }
}
