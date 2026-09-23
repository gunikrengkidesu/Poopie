import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    public boolean upPressed, leftPressed, rightPressed; // ลบ downPressed ออก
    GamePanel gp;

    public KeyHandler(GamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        
        if (gp.gameState == gp.TITLE_STATE) {
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                gp.commandNum--; 
                if(gp.commandNum < 0) gp.commandNum = 2; 
            }
            if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                gp.commandNum++; 
                if(gp.commandNum > 2) gp.commandNum = 0; 
            }
            if (code == KeyEvent.VK_ENTER) {
                if (gp.commandNum == 0) gp.gameState = gp.LEVEL_SELECT_STATE;
                else if (gp.commandNum == 2) System.exit(0);
            }
            
        } else if (gp.gameState == gp.LEVEL_SELECT_STATE) {
            if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                gp.levelSelectNum--;
                if(gp.levelSelectNum < 1) gp.levelSelectNum = 1;
            }
            if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                gp.levelSelectNum++;
                if(gp.levelSelectNum > 5) gp.levelSelectNum = 5;
            }
            if (code == KeyEvent.VK_ENTER) {
                if (gp.levelSelectNum <= gp.maxUnlockedLevel) {
                    gp.currentLevel = gp.levelSelectNum; 
                    gp.setupGame(); 
                    gp.player.setDefaultValues(); 
                    gp.gameState = gp.PLAY_STATE; 
                }
            }
            if (code == KeyEvent.VK_ESCAPE) gp.gameState = gp.TITLE_STATE;

        } else if (gp.gameState == gp.PLAY_STATE) {
            // ระบบควบคุมเกมแนว Platformer[cite: 60]
            if (code == KeyEvent.VK_W) upPressed = true; // W = กระโดด
            if (code == KeyEvent.VK_A) leftPressed = true; // A = ซ้าย
            if (code == KeyEvent.VK_D) rightPressed = true; // D = ขวา

        } else if (gp.gameState == gp.GAME_OVER_STATE) {
            if (code == KeyEvent.VK_R) { 
                gp.setupGame();
                gp.player.setDefaultValues();
                gp.gameState = gp.PLAY_STATE;
            }
            if (code == KeyEvent.VK_ESCAPE) gp.gameState = gp.TITLE_STATE;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W) upPressed = false;
        if (code == KeyEvent.VK_A) leftPressed = false;
        if (code == KeyEvent.VK_D) rightPressed = false;
    }
}