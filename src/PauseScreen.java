import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/** Paused game: music and sound effect volume, then resume, restart, leave or quit. */
public class PauseScreen extends OverlayScreen {
    private static final int PANEL_W = 460, TEXT_H = 150;
    private final VolumeSlider musicSlider, effectsSlider;

    public PauseScreen(GamePanel game, PlayScreen play) {
        super(game, play);
        buttons.add("RESUME", this::resume)
               .add("PLAY AGAIN", this::playAgain)
               .add("LEVEL SELECT", () -> levelSelect(play.getLevelNumber()))
               .add("QUIT", this::quit);
        int top = GameConfig.HEIGHT / 2 - panelHeight(TEXT_H) / 2, x = GameConfig.WIDTH / 2 - 80;
        musicSlider = new VolumeSlider("MUSIC", x, top + 98, 180,
                AudioSettings::getMusic, AudioSettings::setMusic, AudioSettings::save);
        // after changing the effects volume, play a sound so the new level can be heard
        effectsSlider = new VolumeSlider("EFFECTS", x, top + 140, 180,
                AudioSettings::getEffects, AudioSettings::setEffects, () -> { AudioSettings.save(); Sfx.play(Sfx.COLLECT); });
    }

    @Override
    public void mouseMoved(int x, int y) {
        super.mouseMoved(x, y);
        musicSlider.mouseMoved(x, y);
        effectsSlider.mouseMoved(x, y);
    }

    @Override
    public void mouseClicked(int x, int y) {
        if (ticks < 20) return;
        if (musicSlider.mousePressed(x, y) || effectsSlider.mousePressed(x, y)) return;
        super.mouseClicked(x, y);
    }

    @Override
    public void mouseDragged(int x, int y) {
        musicSlider.mouseDragged(x, y);
        effectsSlider.mouseDragged(x, y);
    }

    @Override
    public void mouseReleased(int x, int y) {
        musicSlider.mouseReleased();
        effectsSlider.mouseReleased();
    }

    @Override
    public void keyPressed(int code) {
        if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_P) { resume(); return; }
        buttons.keyPressed(code); // no delay needed: the player opened this on purpose
    }

    private void resume() {
        game.getKeys().clear();
        game.setScreen(play);
    }

    @Override
    protected void drawPopup(Graphics2D g) {
        int h = panelHeight(TEXT_H), top = GameConfig.HEIGHT / 2 - h / 2;
        UI.panel(g, PANEL_W, h);
        g.setColor(new Color(110, 70, 50));
        g.setFont(UI.font(Font.BOLD, 24));
        UI.drawCentered(g, "PAUSED", GameConfig.WIDTH / 2, top + 58);
        musicSlider.draw(g);
        effectsSlider.draw(g);
        if (AudioSettings.isMuted()) {
            g.setFont(UI.smallFont(13));
            g.setColor(new Color(200, 60, 50));
            UI.drawCentered(g, "SOUND IS OFF  (press M or move a slider)", GameConfig.WIDTH / 2, top + 172);
        }
        drawButtons(g, top, h);
    }
}
