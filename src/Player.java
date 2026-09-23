import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Player {
    GamePanel gp;
    KeyHandler keyH;
    public int x, y;
    public int speed;
    public BufferedImage playerImage;
    public Rectangle solidArea = new Rectangle(10, 16, 28, 32); 
    public boolean atBathroomDoor = false; 
    
    // ระบบฟิสิกส์กระโดด (Platformer Physics)[cite: 60]
    public double yVelocity = 0;
    public double gravity = 0.6; // แรงโน้มถ่วงดึงลง
    public double jumpForce = -12.5; // แรงกระโดด (ค่าติดลบคือพุ่งขึ้น)
    public boolean isJumping = true; 

    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;
        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues() {
        x = 100;
        y = gp.screenHeight - 150;
        speed = 5;
        yVelocity = 0;
        isJumping = true;
    }

    public void getPlayerImage() {
        try {
            playerImage = ImageIO.read(new File("res/CGS_Char_3.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        // 1. การเดินซ้าย-ขวา[cite: 60]
        if (keyH.leftPressed) {
            x -= speed;
            if (x < 0) x = 0; // ล็อคขอบซ้าย
        }
        if (keyH.rightPressed) {
            x += speed;
            if (x > gp.screenWidth - gp.tileSize) x = gp.screenWidth - gp.tileSize; // ล็อคขอบขวา
        }
        
        // 2. การกระโดด (กระโดดได้เมื่อเท้าติดพื้นเท่านั้น)[cite: 60]
        if (keyH.upPressed && !isJumping) {
            yVelocity = jumpForce;
            isJumping = true;
        }

        // 3. ใช้แรงโน้มถ่วงดึงตัวละครลงมาเสมอ
        yVelocity += gravity;
        y += (int) yVelocity;

        // ล็อคไม่ให้ตกทะลุขอบล่างจอ (กันเหนียว)
        if (y > gp.screenHeight - gp.tileSize) {
            y = gp.screenHeight - gp.tileSize;
            yVelocity = 0;
            isJumping = false;
        }

        checkObstacles();
        checkItemCollision();
        checkFinishLevel();
    }
    
    public void checkObstacles() {
        int leftCol = (x + solidArea.x) / gp.tileSize;
        int rightCol = (x + solidArea.x + solidArea.width) / gp.tileSize;
        int topRow = (y + solidArea.y) / gp.tileSize;
        int bottomRow = (y + solidArea.y + solidArea.height) / gp.tileSize;

        boolean hitFloor = false;

        if (bottomRow < gp.maxScreenRow && leftCol >= 0 && rightCol < gp.maxScreenCol) {
            int tileNum1 = gp.tileM.mapTileNum[bottomRow][leftCol];
            int tileNum2 = gp.tileM.mapTileNum[bottomRow][rightCol];
            
            // เช็คว่าเหยียบพื้นหรือไม่ (ป้องกันการตกทะลุบล็อก)
            if ((gp.tileM.tile[tileNum1] != null && gp.tileM.tile[tileNum1].collision) ||
                (gp.tileM.tile[tileNum2] != null && gp.tileM.tile[tileNum2].collision)) {
                
                if (yVelocity > 0) { // ถ้าร่วงลงมาโดนพื้น ให้หยุด
                    hitFloor = true;
                    yVelocity = 0;
                    isJumping = false; // อนุญาตให้กระโดดใหม่ได้
                    y = (bottomRow * gp.tileSize) - solidArea.y - solidArea.height; // ดันตัวให้อยู่บนพื้น
                }
            }

            // เช็คบ่อกรด[cite: 60]
            if ((gp.tileM.tile[tileNum1] != null && gp.tileM.tile[tileNum1].isAcid) ||
                (gp.tileM.tile[tileNum2] != null && gp.tileM.tile[tileNum2].isAcid)) {
                gp.gameState = gp.GAME_OVER_STATE; 
            }
            
            // เช็คประตูทางออก
            if ((gp.tileM.tile[tileNum1] != null && gp.tileM.tile[tileNum1].isDoor) ||
                (gp.tileM.tile[tileNum2] != null && gp.tileM.tile[tileNum2].isDoor)) {
                atBathroomDoor = true;
            } else {
                atBathroomDoor = false;
            }
        }

        // ถ้าเดินออกนอกขอบบล็อก ให้ตัวละครร่วงลงมา
        if (!hitFloor && yVelocity > 0) {
            isJumping = true;
        }
    }

    public void checkItemCollision() {
        Rectangle playerRect = new Rectangle(x + solidArea.x, y + solidArea.y, solidArea.width, solidArea.height);
        for (int i = 0; i < gp.items.length; i++) {
            if (gp.items[i] != null && gp.items[i].isActive) {
                Rectangle itemRect = new Rectangle(gp.items[i].x, gp.items[i].y, gp.items[i].solidArea.width, gp.items[i].solidArea.height);
                if (playerRect.intersects(itemRect)) {
                    gp.items[i].isActive = false; 
                    gp.collectedTissue++; 
                }
            }
        }
    }

    public void checkFinishLevel() {
        if(atBathroomDoor) {
            if(gp.collectedTissue >= gp.targetTissue) {
                if(gp.currentLevel == gp.maxUnlockedLevel && gp.maxUnlockedLevel < 5) {
                    gp.maxUnlockedLevel++; 
                }
                gp.gameState = gp.LEVEL_SELECT_STATE; 
            }
        }
    }

    public void draw(Graphics2D g2) {
        if(playerImage != null) {
            // แก้ไขภาพขาดครึ่งโดยใช้สัดส่วนภาพที่กว้างขึ้น (สมมติว่าภาพจริงคือ 32x48)
            g2.drawImage(playerImage, x, y, x + gp.tileSize, y + gp.tileSize, 0, 0, 32, 48, null);
        } else {
            g2.setColor(Color.BLUE);
            g2.fillRect(x, y, gp.tileSize, gp.tileSize);
        }
    }
}