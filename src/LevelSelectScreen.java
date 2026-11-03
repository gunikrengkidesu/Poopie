import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/** Choose a room. Locked rooms open only after the previous one is cleared. */
public class LevelSelectScreen extends MenuScreen {
    private int selected;
    private int hoveredCard = -1;
    private int lockedNotice = -1;    // locked level that was just clicked (shows the LOCKED banner)
    private long lockedNoticeUntil;
    private final double[] lift = new double[GameConfig.LEVEL_COUNT + 1]; // how far each card has popped up
    private final java.awt.Rectangle[] cards = new java.awt.Rectangle[GameConfig.LEVEL_COUNT + 1];
    private final PixelButton back, play;

    public LevelSelectScreen(GamePanel game) {
        this(game, game.getProgress().getHighestUnlocked());
    }

    public LevelSelectScreen(GamePanel game, int selectedLevel) {
        super(game);
        selected = Math.max(1, Math.min(game.getProgress().getHighestUnlocked(), selectedLevel));
        back = new PixelButton("BACK", 24, GameConfig.HEIGHT - 76, 160, 44, () -> game.setScreen(new CharacterSelectScreen(game)));
        play = new PixelButton("PLAY", GameConfig.WIDTH - 184, GameConfig.HEIGHT - 76, 160, 44, this::playSelected);
    }

    @Override
    public void keyPressed(int code) {
        if (soundKeyPressed(code)) return;
        int before = selected;
        // the arrows move between unlocked levels only
        if (KeyHandler.isLeft(code)) selected = Math.max(1, selected - 1);
        if (KeyHandler.isRight(code)) selected = Math.min(game.getProgress().getHighestUnlocked(), selected + 1);
        if (selected != before) Sfx.play(Sfx.TAP);
        if (KeyHandler.isConfirm(code)) playSelected();
        if (code == KeyEvent.VK_ESCAPE) game.setScreen(new CharacterSelectScreen(game));
    }

    private void playSelected() {
        if (game.getProgress().isUnlocked(selected)) game.startLevel(selected);
    }

    @Override
    public void mouseMoved(int x, int y) {
        if (soundMouseMoved(x, y)) return;
        back.mouseMoved(x, y);
        play.mouseMoved(x, y);
        // pointing at an unlocked card picks it and pops it up (with a tap); locked cards stay put
        int over = -1;
        for (int i = 0; i < cards.length; i++)
            if (cards[i] != null && cards[i].contains(x, y) && game.getProgress().isUnlocked(i)) over = i;
        if (over != -1 && over != hoveredCard) {
            Sfx.play(Sfx.TAP);
            selected = over;
        }
        hoveredCard = over;
        if (over != -1) Pointer.over();
    }

    /** Click a card to choose it, click the chosen card again (or PLAY) to start. Locked cards only show a notice. */
    @Override
    public void mouseClicked(int x, int y) {
        if (soundMouseClicked(x, y) || back.mouseClicked(x, y) || play.mouseClicked(x, y)) return;
        for (int n = 1; n <= GameConfig.LEVEL_COUNT; n++) {
            if (cards[n] == null || !cards[n].contains(x, y)) continue;
            if (!game.getProgress().isUnlocked(n)) { lockedNotice = n; lockedNoticeUntil = ticks + 120; }
            else if (selected == n) playSelected();
            else selected = n;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        drawBackdrop(g, 150);
        Progress progress = game.getProgress();
        int cx = GameConfig.WIDTH / 2;
        g.setFont(UI.font(Font.BOLD, 24));
        UI.shadowText(g, "SELECT LEVEL", cx, 105, Color.WHITE, new Color(60, 30, 20));
        g.setFont(UI.smallFont(16));
        g.setColor(new Color(255, 255, 255, 210));
        UI.drawCentered(g, "Clear a level to unlock the next one. Stars show how hard a room is.", cx, 140);

        int cardW = 190, cardH = 252, gap = 18; // 4 rooms: wide cards
        int startX = cx - (GameConfig.LEVEL_COUNT * cardW + (GameConfig.LEVEL_COUNT - 1) * gap) / 2;
        for (int n = 1; n <= GameConfig.LEVEL_COUNT; n++) {
            Room room = Room.get(n);
            boolean unlocked = progress.isUnlocked(n);
            boolean sel = selected == n && unlocked;
            lift[n] += ((sel ? 16 : 0) - lift[n]) * 0.3;          // pop up smoothly when chosen
            int x = startX + (n - 1) * (cardW + gap), y = 200 - (int) Math.round(lift[n]);
            cards[n] = new java.awt.Rectangle(x, 184, cardW, cardH + 16); // fixed hit area so the pop doesn't flicker
            if (sel) {                                              // soft shadow under the raised card
                g.setColor(new Color(0, 0, 0, 70));
                g.fillRoundRect(x + 4, 200 + 8, cardW, cardH, 22, 22);
            }
            g.setPaint(unlocked ? new GradientPaint(x, y, room.getWallTop(), x, y + cardH, room.getWallBottom())
                                : new GradientPaint(x, y, new Color(120, 120, 125), x, y + cardH, new Color(80, 80, 85)));
            g.fillRoundRect(x, y, cardW, cardH, 22, 22);
            g.setStroke(new BasicStroke(sel ? 6 : 3));
            g.setColor(sel ? new Color(255, 170, 40) : new Color(90, 70, 60));
            g.drawRoundRect(x, y, cardW, cardH, 22, 22);
            g.setStroke(new BasicStroke(1));

            Color text = unlocked ? new Color(80, 50, 30) : new Color(40, 40, 45);
            g.setFont(UI.font(Font.BOLD, 24));
            g.setColor(text);
            UI.drawCentered(g, String.valueOf(n), x + cardW / 2, y + 45);
            BufferedImage icon = Assets.sprite(room.getIcon());
            if (icon != null) {
                double s = Math.min(84.0 / icon.getWidth(), 80.0 / icon.getHeight());
                int w = (int) (icon.getWidth() * s), h = (int) (icon.getHeight() * s);
                Composite old = g.getComposite();
                if (!unlocked) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g.drawImage(icon, x + cardW / 2 - w / 2, y + 140 - h, w, h, null);
                g.setComposite(old);
            }
            String title = room.getTitle().toUpperCase();
            g.setFont(UI.font(Font.BOLD, 12));
            if (UI.stringWidth(g, title) > cardW - 12) g.setFont(UI.font(Font.BOLD, 10)); // long names get a smaller font
            g.setColor(text);
            UI.drawCentered(g, title, x + cardW / 2, y + 168);
            drawStars(g, x + cardW / 2, y + 186, room.getDifficulty().getStars(), unlocked);
            int t = room.getTimeLimit();
            g.setFont(UI.smallBoldFont(13));
            g.setColor(text);
            UI.drawCentered(g, room.getTissueCount() + " rolls  " + String.format("%d:%02d", t / 60, t % 60),
                            x + cardW / 2, y + 216);

            if (!unlocked) drawLock(g, x + cardW / 2, y + 100);
            else badge(g, x + cardW / 2, y + 225, progress.isCleared(n) ? "CLEARED" : "NEW!",
                       progress.isCleared(n) ? new Color(40, 160, 80) : new Color(255, 170, 40));
        }
        if (lockedNotice > 0 && ticks < lockedNoticeUntil)
            UI.banner(g, "LOCKED - CLEAR LEVEL " + (lockedNotice - 1) + " FIRST", 520, new Color(160, 60, 50, 230));
        back.draw(g, ticks);
        play.draw(g, ticks);
        drawSound(g);
    }

    private void badge(Graphics2D g, int cx, int y, String text, Color color) {
        g.setColor(color);
        g.fillRoundRect(cx - 40, y, 80, 20, 10, 10);
        g.setColor(color.getGreen() > 150 && color.getRed() > 200 ? new Color(70, 40, 20) : Color.WHITE);
        g.setFont(UI.font(Font.BOLD, 8));
        UI.drawCentered(g, text, cx, y + 14);
    }

    /** Difficulty as small stars (one per possible star), filled up to `stars`. */
    private void drawStars(Graphics2D g, int cx, int cy, int stars, boolean unlocked) {
        for (int i = 0; i < Difficulty.MAX_STARS; i++) {
            int sx = cx - (Difficulty.MAX_STARS - 1) * 10 + i * 20;
            int[] xs = new int[10], ys = new int[10];
            for (int k = 0; k < 10; k++) {
                double r = k % 2 == 0 ? 8 : 3.5, a = -Math.PI / 2 + k * Math.PI / 5;
                xs[k] = (int) Math.round(sx + r * Math.cos(a));
                ys[k] = (int) Math.round(cy + r * Math.sin(a));
            }
            boolean filled = i < stars;
            g.setColor(!unlocked ? new Color(60, 60, 65) : filled ? new Color(255, 180, 40) : new Color(255, 255, 255, 120));
            g.fillPolygon(xs, ys, 10);
            g.setColor(new Color(90, 60, 30, unlocked ? 200 : 80));
            g.drawPolygon(xs, ys, 10);
        }
    }

    private void drawLock(Graphics2D g, int cx, int cy) {
        g.setColor(new Color(40, 40, 45));
        g.setStroke(new BasicStroke(6));
        g.drawArc(cx - 16, cy - 34, 32, 36, 0, 180);
        g.setStroke(new BasicStroke(1));
        g.setColor(new Color(240, 190, 60));
        g.fillRoundRect(cx - 24, cy - 16, 48, 38, 8, 8);
        g.setColor(new Color(60, 40, 20));
        g.fillOval(cx - 5, cy - 6, 10, 10);
        g.fillRect(cx - 2, cy, 4, 12);
    }
}
