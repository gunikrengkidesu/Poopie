import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/** Room 1: Living Room */
public class LivingRoom extends Room {
    private static final String[] LAYOUT = {
            "##############################",
            "#........F...................#",
            "#............................#",
            "#..D................{#}......#",
            "########\\..###################",
            "#........\\....F..............#",
            "#.........\\..................#",
            "#..........\\..../######\\.....#",
            "#..........#############\\....#",
            "#.......................##...#",
            "#............................#",
            "#...############\\........../##",
            "#.....F..........\\......../###",
            "###}.............#############",
            "####}........................#",
            "#############\\...............#",
            "#.............\\..............#",
            "#.............#####===###../##",
            "#........................./###",
            "#......................../####",
            "#.P...................../#####",
            "####################===#######",
    };

    public LivingRoom() {
        super(1, "Living Room", "sofa", 1, 5,
              new String[] {"clock", "monaLisa", "vaseLRK"}, new Color(236, 214, 178), new Color(214, 184, 146));
    }

    @Override
    public String[] getLayout() {
        return LAYOUT;
    }

    @Override
    protected void drawWallpaper(Graphics2D g, int w, int h) {
        paneledWall(g, w, h); // the same cream wall as the bedroom
    }

    /** Size of the living-room set (its sprites are tiny, 5-30 px). */
    private static final double K = 2.5;

    /** Row where something standing on a piece `pixels` tall (at size K) sits. */
    private static double on(double floor, int pixels) {
        return floor - pixels * K / GameConfig.TILE;
    }

    /** Width in tiles of a piece `pixels` wide at size K. */
    private static double w(int pixels) {
        return pixels * K / GameConfig.TILE;
    }

    /** A TV on its low table. */
    private static void tv(List<Decor> list, double col, double floor) {
        add(list, "tvTable", col, floor, K);
        add(list, "tvRetro", col, on(floor, 8), K);
    }

    /** A low sideboard (`mids` connecting pieces); returns its left edge. */
    private static double sideboard(List<Decor> list, double col, double floor, int mids) {
        add(list, "cabL", col, floor, K);
        double x = col + w(14);
        for (int i = 0; i < mids; i++) { add(list, "cabM", x, floor, K); x += w(16); }
        add(list, "cabR", x, floor, K);
        return col;
    }

    /** A flower in a pot (`flower` is aster or daisy, `pot` kFlowerpot or potGreen). */
    private static void flowerPot(List<Decor> list, double col, double floor, String pot, String flower) {
        add(list, pot, col, floor, K);
        int fw = flower.equals("aster") ? 8 : 5;
        add(list, flower, col + w(7 - fw) / 2, on(floor, 3), K);
    }

    /** A small wall shelf with something standing on it. */
    private static void wallShelf(List<Decor> list, double col, double bottom, String thing, int thingW) {
        add(list, "shelfMedium", col, bottom, K);
        add(list, thing, col + w(14 - thingW) / 2, bottom - w(4), K);
    }

    @Override
    protected void addFurniture(List<Decor> list) {
        // ---- top floor: plant by the exit | big painting over the sofa, sofa, side table with a lamp | armchair, flowers, lamp
        flowerPot(list, 6.2, 4, "potGreen", "daisy");
        add(list, "paintingLarge", 12.6, 2.6, K);
        add(list, "couchBlue", 11.6, 4, K);
        add(list, "sideTable", 14.6, 4, K);
        add(list, "tableLampWhite", 14.6, on(4, 8), K);
        add(list, "floorLampTall", 16.0, 4, K);
        add(list, "armchairBlue", 23.4, 4, K);
        flowerPot(list, 25.0, 4, "kFlowerpot", "aster");
        add(list, "floorLampTall", 27.4, 4, K);
        // ---- the raised block: only things on the wall (a painting, a book shelf, a calendar)
        add(list, "paintingLarge", 17.6, 6.5, K);
        wallShelf(list, 19.3, 6.1, "booksRow", 10);
        add(list, "calendar", 21.1, 6.3, K);
        flowerPot(list, 12.3, 8, "potGreen", "aster");
        // ---- middle platform: sideboard with radio, trophy, vase and lamp | sofa between a painting and a lamp
        add(list, "windowNarrow", 6.03, 9, 1.6);
        sideboard(list, 4.2, 11, 1);
        add(list, "radio", 4.35, on(11, 7), K);
        add(list, "trophy", 5.25, on(11, 7), K);
        add(list, "vaseRed", 6.25, on(11, 7), K);
        add(list, "tableLampWhite", 6.9, on(11, 7), K);
        add(list, "paintingSmall", 10.75, 9.75, K);
        add(list, "couchBlue", 9.6, 11, K);
        add(list, "sideTable", 12.6, 11, K);
        flowerPot(list, 12.6, on(11, 8), "potGreen", "daisy");
        add(list, "floorLampTall", 14.0, 11, K);
        // ---- right floor: armchair, radio table | sofa under the curtained window | flowers, lamp, skateboard
        add(list, "curtainGreen", 19.85, 11.7, K);
        add(list, "windowFour", 20.15, 11.63, 1.6);
        add(list, "curtainGreen", 21.7, 11.7, K);
        add(list, "armchairBlue", 18.2, 13, K);
        add(list, "couchBlue", 19.8, 13, K);
        add(list, "sideTable", 22.8, 13, K);
        add(list, "radio", 22.8, on(13, 8), K);
        flowerPot(list, 23.9, 13, "kFlowerpot", "aster");
        add(list, "floorLampTall", 25.3, 13, K);
        add(list, "skateboard", 24.6, 13, K);
        // ---- lower-left platform: fireplace | kid's corner: backpack on the wall, bat against it | lamp
        add(list, "fireplace", 7.25, 15, 1.4);
        add(list, "backpack", 9.5, 13.9, K);
        add(list, "bat", 8.8, 15, K);
        wallShelf(list, 10.4, 13.2, "trophy", 10);
        add(list, "floorLampTall", 11.6, 15, K);
        // ---- long floor: sideboard with books and a lamp, shelf above | (puddle under the window) | armchair, flowers, lamp
        add(list, "curtainWindow", 19.68, 15.75, 1.3);
        sideboard(list, 15.1, 17, 0);
        add(list, "booksRow", 15.3, on(17, 7), K);
        add(list, "tableLampWhite", 16.75, on(17, 7), K);
        wallShelf(list, 15.8, 15.2, "vaseRed", 6);
        add(list, "armchairBlue", 22.1, 17, K);
        add(list, "sideTable", 23.5, 17, K);
        flowerPot(list, 23.5, on(17, 8), "potGreen", "daisy");
        add(list, "windowNarrow", 25.45, 17.88, 2);
        // ---- ground: TV, coffee table, sofa, side table with a lamp | flowers, wall shelf, armchair, bat
        add(list, "paintingLarge", 5.0, 18.0, K);
        tv(list, 4.8, 21);
        add(list, "coffeeTable", 6.7, 21, K);
        add(list, "couchBlue", 8.4, 21, K);
        add(list, "paintingSmall", 9.4, 18.6, K);
        add(list, "sideTable", 11.4, 21, K);
        add(list, "tableLampWhite", 11.4, on(21, 8), K);
        flowerPot(list, 12.8, 21, "kFlowerpot", "aster");
        wallShelf(list, 12.75, 18.95, "radio", 8);
        add(list, "floorLampTall", 14.9, 21, K);
        add(list, "armchairBlue", 16.4, 21, K);
        add(list, "skateboard", 18.2, 21, K);
    }
}
