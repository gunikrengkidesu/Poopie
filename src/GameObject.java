import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class GameObject {
    public BufferedImage image;
    public int x, y;
    public boolean isActive = true; // ยังไม่ถูกเก็บใช่ไหม
    // กำหนด Hitbox (ขอบเขตการชน)
    public Rectangle solidArea = new Rectangle(0, 0, 48, 48); 

    public GameObject(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        try {
            // โหลดรูปทิชชู่
            image = ImageIO.read(new File("res/toilet_paper.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2, GamePanel gp) {
        if (isActive) {
            g2.drawImage(image, x, y, gp.tileSize, gp.tileSize, null);
        }
    }
}