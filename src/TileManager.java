import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TileManager {
    GamePanel gp;
    public Tile[] tile;
    public int mapTileNum[][];

    public TileManager(GamePanel gp) {
        this.gp = gp;
        tile = new Tile[10];
        mapTileNum = new int[gp.maxScreenRow][gp.maxScreenCol];
        
        for(int i = 0; i < tile.length; i++) {
            tile[i] = new Tile();
        }
        
        getTileImage();
    }

    public void getTileImage() {
        try {
            BufferedImage interiorSheet = ImageIO.read(new File("res/InteriorTilesLITE.png"));
            BufferedImage urbanA5Sheet = ImageIO.read(new File("res/CGS_Urban_A5.png"));
            BufferedImage officeSheet = ImageIO.read(new File("res/!CGS_Office.png")); // โหลด Asset ออฟฟิศ[cite: 64]

            // 0: พื้นหลังโล่งๆ (สีพื้นผนัง)
            tile[0].image = interiorSheet.getSubimage(0, 0, 16, 16);
            
            // 1: บล็อกทางเดิน/กำแพง (ชนได้)
            tile[1].image = interiorSheet.getSubimage(64, 32, 16, 16); // ลายกระเบื้อง
            tile[1].collision = true;
            
            // 2: บ่อกรด[cite: 60, 67]
            tile[2].image = urbanA5Sheet.getSubimage(0, 32, 16, 16); 
            tile[2].isAcid = true;
            
            // 4: ประตูห้องน้ำ
            tile[4].image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gDoor = tile[4].image.createGraphics();
            gDoor.setColor(new Color(139, 69, 19)); 
            gDoor.fillRect(0, 0, 16, 16);
            gDoor.setColor(Color.YELLOW); 
            gDoor.fillOval(10, 7, 4, 4); 
            gDoor.dispose();
            tile[4].isDoor = true;

            // 5: โต๊ะคอมพิวเตอร์ออฟฟิศ (วาดเป็นพื้นหลัง ไม่ชน)[cite: 64]
            tile[5].image = officeSheet.getSubimage(0, 0, 16, 16); 

        } catch (Exception e) {
            System.out.println("⚠️ โหลดภาพฉากออฟฟิศไม่สำเร็จ!");
        }
    }

    public void loadMap() {
        // แผนที่ด่านออฟฟิศ สไตล์ตลุยด่านมีชั้นต่างๆ (0=ว่าง, 1=พื้นเดิน, 2=กรด, 4=ประตู, 5=โต๊ะคอม)[cite: 60]
        int[][] map1 = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 1}, // ชั้นบนสุดมีประตู
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1},
            {1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1}, // แพลตฟอร์มชั้น 2
            {1, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 1}, // มีโต๊ะคอมประดับฉาก
            {1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1}, 
            {1, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, // มีโต๊ะคอมประดับฉาก
            {1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 2, 2, 0, 1}, // ชั้นล่างสุดเป็นบ่อกรด[cite: 60]
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };

        for (int row = 0; row < gp.maxScreenRow; row++) {
            for (int col = 0; col < gp.maxScreenCol; col++) {
                mapTileNum[row][col] = map1[row][col];
            }
        }
    }

    public void draw(Graphics2D g2) {
        int col = 0, row = 0, x = 0, y = 0;
        
        while (row < gp.maxScreenRow) {
            int tileNum = mapTileNum[row][col];
            if (tileNum >= 0 && tileNum < tile.length && tile[tileNum] != null && tile[tileNum].image != null) {
                
                if (tileNum == 4) {
                    // ประตูจะโผล่เมื่อเก็บของครบ
                    if (gp.collectedTissue >= gp.targetTissue) {
                        g2.drawImage(tile[tileNum].image, x, y, gp.tileSize, gp.tileSize, null);
                    }
                } else {
                    g2.drawImage(tile[tileNum].image, x, y, gp.tileSize, gp.tileSize, null);
                }
            }
            
            col++; 
            x += gp.tileSize;
            
            if (col == gp.maxScreenCol) {
                col = 0; x = 0; 
                row++; y += gp.tileSize;
            }
        }
    }
}