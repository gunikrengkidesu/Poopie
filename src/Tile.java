import java.awt.image.BufferedImage;

public class Tile {
    public BufferedImage image;
    public boolean collision = false; // ชนได้ไหม (เช่น กำแพง)
    public boolean isAcid = false;    // เป็นบ่อกรดไหม
    public boolean isSlippery = false; // เป็นพื้นลื่นไหม
    public boolean isDoor = false;
}