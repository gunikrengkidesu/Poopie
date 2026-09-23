import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        System.out.println("โปรแกรมกำลังหาไฟล์ที่: " + new java.io.File(".").getAbsolutePath());
        
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setTitle("POOPIE");
        
        // --- ขยายเต็มจอแบบยังมีแถบหน้าต่างและปุ่มกดด้านบน ---
        window.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        
        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        
        gamePanel.requestFocus();
        gamePanel.setupGame(); 
        gamePanel.startGameThread();
    }
}