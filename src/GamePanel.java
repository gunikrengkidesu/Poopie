import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {
    public final int tileSize = 48; // 16x16 pixel * scale 3
    public final int maxScreenCol = 16;
    public final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    public TileManager tileM = new TileManager(this);
    public GameObject[] items = new GameObject[5]; // สมมติ 1 ด่านมีทิชชู่ 5 อัน

    // สร้างฟังก์ชันเตรียมของ (เรียกตอนเริ่มเกม หรือ เปลี่ยนด่าน)
    public void setupGame() {
        // 1. รีเซ็ตเวลาและจำนวนของที่เก็บได้
        playTime = 70.0; 
        collectedTissue = 0;
        
        // 2. เคลียร์ทิชชู่เก่าออกจากฉากให้หมดก่อน
        for(int i = 0; i < items.length; i++) {
            items[i] = null;
        }

        // 3. วางทิชชู่ใหม่ตามด่านปัจจุบัน
        if(currentLevel == 1) {
            targetTissue = 3; // ตั้งเป้าว่าต้องเก็บ 3 ชิ้น
            items[0] = new GameObject(5 * tileSize, 3 * tileSize); 
            items[1] = new GameObject(10 * tileSize, 2 * tileSize);
            items[2] = new GameObject(12 * tileSize, 8 * tileSize); // เพิ่มชิ้นที่ 3 ให้ครบ!
            
        } else if(currentLevel == 2) {
            targetTissue = 4; // ด่าน 2 ต้องเก็บ 4 ชิ้น
            items[0] = new GameObject(3 * tileSize, 3 * tileSize); 
            items[1] = new GameObject(8 * tileSize, 2 * tileSize);
            items[2] = new GameObject(13 * tileSize, 6 * tileSize);
            items[3] = new GameObject(5 * tileSize, 9 * tileSize);
        }
        // คุณสามารถเพิ่ม else if (currentLevel == 3) ... เพื่อวางทิชชู่ด่านต่อไปได้เลยครับ
    }

    Thread gameThread;
    public KeyHandler keyH = new KeyHandler(this);
    public Player player = new Player(this, keyH);
    
    // สถานะเกม
    public int gameState;
    public final int TITLE_STATE = 0;
    public final int PLAY_STATE = 1;
    public final int GAME_OVER_STATE = 2;
    public final int LEVEL_SELECT_STATE = 3; // เพิ่มสถานะหน้าเลือกด่าน
    
    public int commandNum = 0; 
    public int levelSelectNum = 1; // ตำแหน่งเคอร์เซอร์หน้าเลือกด่าน
    public int maxUnlockedLevel = 1; // ตัวแปรเก็บว่าปลดล็อคถึงด่านไหนแล้ว 
    
    // ระบบด่านและ UI
    public int currentLevel = 1;
    public double playTime = 70.0; // 01.10 นาทีตามเอกสาร
    public int collectedTissue = 0;
    public int targetTissue = 3;
    BufferedImage tissueImage;
    BufferedImage titleBg;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(new Color(240, 240, 240, 240)); // สีพื้นหลัง
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        this.gameState = TITLE_STATE;
        
        try {
            tissueImage = ImageIO.read(new File("res/toilet_paper.png"));
            titleBg = ImageIO.read(new File("res/title_bg.png")); // โหลดรูปหน้าเริ่มเกม
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / 60; // 60 FPS
        double nextDrawTime = System.nanoTime() + drawInterval;
        while (gameThread != null) {
            update();
            repaint();
            try {
                double remainingTime = (nextDrawTime - System.nanoTime()) / 1000000;
                if (remainingTime < 0) remainingTime = 0;
                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void update() {
        if (gameState == PLAY_STATE) {
            player.update();
            playTime -= (1.0/60.0);
            if(playTime <= 0) {
                gameState = GAME_OVER_STATE; // เวลาหมด = จบเกม
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        double scaleX = (double) getWidth() / screenWidth;
        double scaleY = (double) getHeight() / screenHeight;
        g2.scale(scaleX, scaleY);
        
        if (gameState == TITLE_STATE) {
            // 1. วาดรูปบ้านเห็ดเป็นพื้นหลังให้เต็มจอ[cite: 52]
            if(titleBg != null) {
                g2.drawImage(titleBg, 0, 0, screenWidth, screenHeight, null);
            }

            g2.setFont(new Font("Dogica Pixel", Font.BOLD, 40));
            int menuX = screenWidth / 2 - 100;
            int menuY = screenHeight / 2 + 30;

            // 2. วาดเมนู 3 ตัวเลือก[cite: 52]
            g2.setColor(Color.BLACK);
            g2.drawString("1 PLAYER", menuX + 3, menuY + 3);
            g2.drawString("EXIT", menuX + 3, menuY + 143);

            g2.setColor(new Color(255, 180, 50));
            g2.drawString("1 PLAYER", menuX, menuY);
            g2.drawString("EXIT", menuX, menuY + 140);

            // 3. วาดลูกศรเลือกเมนู[cite: 52]
            int cursorY = menuY;
            if(commandNum == 0) cursorY = menuY;
            else if(commandNum == 1) cursorY = menuY + 70;
            else if(commandNum == 2) cursorY = menuY + 140;

            g2.drawString(">", menuX - 40, cursorY);

        } else if (gameState == LEVEL_SELECT_STATE) {
            // ================== หน้าเลือกด่าน ==================
            g2.setColor(new Color(50, 150, 200));
            g2.fillRect(0, 0, screenWidth, screenHeight);
            
            g2.setFont(new Font("Monospaced", Font.BOLD, 60));
            g2.setColor(Color.WHITE);
            g2.drawString("SELECT LEVEL", screenWidth/2 - 200, 150);

            // วาดกล่องด่าน 5 ด่าน
            int boxWidth = 80;
            int startX = (screenWidth - (5 * boxWidth) - (4 * 20)) / 2; 

            for(int i = 1; i <= 5; i++) {
                int x = startX + ((i-1) * (boxWidth + 20));
                int y = screenHeight/2 - 40;

                // ตรวจสอบว่าด่านปลดล็อคหรือยัง
                if(i <= maxUnlockedLevel) {
                    g2.setColor(new Color(255, 180, 50)); // สีส้ม (ปลดล็อคแล้ว)
                } else {
                    g2.setColor(Color.GRAY); // สีเทา (ยังไม่ปลดล็อค)
                }
                g2.fillRoundRect(x, y, boxWidth, boxWidth, 15, 15);

                // กรอบสีขาวสำหรับด่านที่กำลังเลือกอยู่
                if(levelSelectNum == i) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(5));
                    g2.drawRoundRect(x-5, y-5, boxWidth+10, boxWidth+10, 15, 15);
                }

                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Monospaced", Font.BOLD, 40));
                g2.drawString(String.valueOf(i), x + 25, y + 55);
            }
            
            g2.setFont(new Font("Monospaced", Font.PLAIN, 20));
            g2.setColor(Color.WHITE);
            g2.drawString("Press ESC to return to Title", 20, screenHeight - 30);

        } else if (gameState == PLAY_STATE) {
            // โค้ดวาดฉากเกมเดิมของคุณ (ไม่เปลี่ยนแปลง)[cite: 54]
            tileM.draw(g2); 
            for (int i = 0; i < items.length; i++) {
                if (items[i] != null) items[i].draw(g2, this); 
            }
            player.draw(g2); 
            g2.setFont(new Font("Arial", Font.BOLD, 28));
            g2.setColor(Color.YELLOW); 
            String timeText = "Time: " + String.format("%.2f", playTime);
            int timeLength = (int)g2.getFontMetrics().getStringBounds(timeText, g2).getWidth();
            g2.drawString(timeText, (screenWidth / 2) - (timeLength / 2), 40);
            if(tissueImage != null) {
                g2.drawImage(tissueImage, screenWidth - 140, 15, 32, 32, null);
            }
            g2.drawString(" x " + collectedTissue + "/" + targetTissue, screenWidth - 100, 40);
            
        } else if (gameState == GAME_OVER_STATE) {
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 60));
            g2.drawString("GAME OVER", screenWidth/2 - 170, screenHeight/2 - 50);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.PLAIN, 30));
            g2.drawString("Press R to RESUME or ESC for MENU", screenWidth/2 - 270, screenHeight/2 + 50);
        }
        g2.dispose();
    }

}