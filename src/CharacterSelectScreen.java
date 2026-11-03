import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/** Pick one of the characters in res/Char. */
public class CharacterSelectScreen extends MenuScreen {
    private int selected;
    private int hoveredCard = -1;
    private final java.awt.Rectangle[] cards = new java.awt.Rectangle[8];
    private final PixelButton back, ok;

    public CharacterSelectScreen(GamePanel game) {
        super(game);
        selected = game.getSelectedCharacterIndex();
        back = new PixelButton("BACK", 24, GameConfig.HEIGHT - 76, 160, 44, () -> game.setScreen(new TitleScreen(game)));
        ok = new PixelButton("OK", GameConfig.WIDTH - 184, GameConfig.HEIGHT - 76, 160, 44, this::confirm);
    }

    @Override
    public void keyPressed(int code) {
        if (soundKeyPressed(code)) return;
        int count = game.getCharacters().size();
        int before = selected;
        if (KeyHandler.isLeft(code)) selected = Math.max(0, selected - 1);
        if (KeyHandler.isRight(code)) selected = Math.min(count - 1, selected + 1);
        if (selected != before) Sfx.play(Sfx.TAP);
        if (KeyHandler.isConfirm(code)) confirm();
        if (code == KeyEvent.VK_ESCAPE) game.setScreen(new TitleScreen(game));
    }

    private void confirm() {
        game.setSelectedCharacterIndex(selected);
        game.setScreen(new LevelSelectScreen(game));
    }

    @Override
    public void mouseMoved(int x, int y) {
        if (soundMouseMoved(x, y)) return;
        back.mouseMoved(x, y);
        ok.mouseMoved(x, y);
        // tap when the cursor moves onto a card
        int over = -1;
        for (int i = 0; i < cards.length; i++) if (cards[i] != null && cards[i].contains(x, y)) over = i;
        if (over != -1 && over != hoveredCard) Sfx.play(Sfx.TAP);
        hoveredCard = over;
        if (over != -1) Pointer.over();
        if (over != -1) selected = over; // pointing at a card chooses it
    }

    /** Click a card to choose it, click it again (or OK) to continue. */
    @Override
    public void mouseClicked(int x, int y) {
        if (soundMouseClicked(x, y) || back.mouseClicked(x, y) || ok.mouseClicked(x, y)) return;
        for (int i = 0; i < game.getCharacters().size(); i++) {
            if (cards[i] == null || !cards[i].contains(x, y)) continue;
            if (selected == i) confirm();
            else selected = i;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        drawBackdrop(g, 150);
        int cx = GameConfig.WIDTH / 2;
        g.setFont(UI.font(Font.BOLD, 24));
        UI.shadowText(g, "CHOOSE YOUR CHARACTER", cx, 110, Color.WHITE, new Color(60, 30, 20));

        int count = game.getCharacters().size();
        int cardW = 230, cardH = 300, gap = 70;
        int startX = cx - (count * cardW + (count - 1) * gap) / 2;
        for (int i = 0; i < count; i++) {
            CharacterSkin skin = game.getCharacters().get(i);
            boolean sel = selected == i;
            int x = startX + i * (cardW + gap), y = 170;
            if (sel) y -= 8 + (int) (Math.sin(ticks / 10.0) * 3);
            cards[i] = new java.awt.Rectangle(x, y, cardW, cardH);
            drawCardFrame(g, x, y, cardW, cardH, sel);

            g.setColor(new Color(0, 0, 0, 40));
            g.fillOval(x + cardW / 2 - 50, y + 222, 100, 16);
            String anim = sel && skin.frameCount("talk") > 0 ? "talk" : "idle"; // the chosen one says hi
            BufferedImage f = skin.frame(anim, (int) (ticks / (sel ? 5 : 9)), true);
            if (f != null) {
                double scale = 3.6;
                int w = (int) (64 * scale);
                g.drawImage(f, x + cardW / 2 - w / 2, (int) (y + 230 - (skin.baseline(anim) + 1) * scale), w, w, null);
            }
            g.setFont(UI.font(Font.BOLD, 16));
            g.setColor(new Color(80, 50, 30));
            UI.drawCentered(g, skin.getName(), x + cardW / 2, y + 275);
        }
        g.setFont(UI.smallFont(13));
        g.setColor(new Color(255, 255, 255, 220));
        //UI.drawCentered(g, "LEFT/RIGHT or click: choose    ENTER: OK    ESC: back", cx, GameConfig.HEIGHT - 48);
        back.draw(g, ticks);
        ok.draw(g, ticks);
        drawSound(g);
    }

    /** Pixel-art card: notched corners and a thick outline; the chosen card gets a gold frame and a little arrow. */
    private void drawCardFrame(Graphics2D g, int x, int y, int w, int h, boolean chosen) {
        int p = 6;
        if (chosen) {
            UI.pixelBox(g, x - p, y - p, w + 2 * p, h + 2 * p, new Color(255, 190, 60), new Color(70, 42, 28), p);
            UI.pixelBox(g, x + p, y + p, w - 2 * p, h - 2 * p, new Color(255, 240, 205), new Color(200, 120, 30), p);
            // pixel arrow above the card, bobbing
            int ax = x + w / 2, ay = y - 22 + (int) (Math.sin(ticks / 8.0) * 3);
            g.setColor(new Color(70, 42, 28));
            for (int i = 0; i < 4; i++) g.fillRect(ax - (4 - i) * 4 - 2, ay + i * 4 - 2, (4 - i) * 8 + 4, 8);
            g.setColor(new Color(255, 190, 60));
            for (int i = 0; i < 4; i++) g.fillRect(ax - (4 - i) * 4, ay + i * 4, (4 - i) * 8, 4);
        } else {
            UI.pixelBox(g, x, y, w, h, new Color(214, 206, 198), new Color(70, 42, 28), p);
        }
    }
}
