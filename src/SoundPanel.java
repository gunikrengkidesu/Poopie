import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

/**
 * The SOUND pop-up opened by the speaker button on the menus: music and effects volume sliders (the same
 * as in the pause menu), a button that switches all sound off/on, and CLOSE.
 */
public class SoundPanel {
    private static final int W = 460, H = 290;
    private final int left = GameConfig.WIDTH / 2 - W / 2, top = GameConfig.HEIGHT / 2 - H / 2;
    private final VolumeSlider musicSlider, effectsSlider;
    private final Rectangle toggleArea = new Rectangle(left + 40, top + H - 84, 220, 44);
    private final Rectangle closeArea = new Rectangle(left + W - 180, top + H - 84, 140, 44);
    private boolean open;
    private int hovered = -1; // 0 = sound on/off, 1 = close
    private long ticks;

    public SoundPanel() {
        int x = GameConfig.WIDTH / 2 - 80;
        musicSlider = new VolumeSlider("MUSIC", x, top + 98, 180,
                AudioSettings::getMusic, AudioSettings::setMusic, AudioSettings::save);
        // after changing the effects volume, play a sound so the new level can be heard
        effectsSlider = new VolumeSlider("EFFECTS", x, top + 146, 180,
                AudioSettings::getEffects, AudioSettings::setEffects, () -> { AudioSettings.save(); Sfx.play(Sfx.COLLECT); });
    }

    public boolean isOpen() { return open; }

    public void open() {
        open = true;
        hovered = -1;
    }

    public void close() { open = false; }

    public void update() { ticks++; }

    public void keyPressed(int code) {
        if (code == KeyEvent.VK_ESCAPE || KeyHandler.isConfirm(code)) close();
    }

    public void mouseMoved(int x, int y) {
        musicSlider.mouseMoved(x, y);
        effectsSlider.mouseMoved(x, y);
        int now = toggleArea.contains(x, y) ? 0 : closeArea.contains(x, y) ? 1 : -1;
        if (now >= 0 && now != hovered) Sfx.play(Sfx.TAP);
        hovered = now;
        if (now >= 0) Pointer.over();
    }

    public void mouseClicked(int x, int y) {
        if (musicSlider.mousePressed(x, y) || effectsSlider.mousePressed(x, y)) return;
        if (toggleArea.contains(x, y)) AudioSettings.toggleMuted();
        else if (closeArea.contains(x, y)) close();
        else if (!new Rectangle(left, top, W, H).contains(x, y)) close(); // click outside the pop-up
    }

    public void mouseDragged(int x, int y) {
        musicSlider.mouseDragged(x, y);
        effectsSlider.mouseDragged(x, y);
    }

    public void mouseReleased() {
        musicSlider.mouseReleased();
        effectsSlider.mouseReleased();
    }

    public void draw(Graphics2D g) {
        if (!open) return;
        UI.dim(g, 150);
        UI.pixelBox(g, left, top, W, H, new Color(255, 246, 228), new Color(70, 42, 28), 6);
        g.setColor(new Color(110, 70, 50));
        g.setFont(UI.font(Font.BOLD, 24));
        UI.drawCentered(g, "SOUND", GameConfig.WIDTH / 2, top + 54);
        musicSlider.draw(g);
        effectsSlider.draw(g);
        boolean muted = AudioSettings.isMuted();
        if (muted) {
            g.setFont(UI.smallFont(13));
            g.setColor(new Color(200, 60, 50));
            UI.drawCentered(g, "ALL SOUND IS OFF", GameConfig.WIDTH / 2, top + 184);
        }
        UI.pixelButton(g, toggleArea.x, toggleArea.y, toggleArea.width, toggleArea.height,
                muted ? "SOUND: OFF" : "SOUND: ON", hovered == 0, ticks);
        UI.pixelButton(g, closeArea.x, closeArea.y, closeArea.width, closeArea.height, "CLOSE", hovered == 1, ticks);
    }
}
