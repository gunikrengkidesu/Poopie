import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/** Room 3: Bedroom */
public class Bedroom extends Room {
    private static final String[] LAYOUT = {
            "##############################",
            "#.........F..................#",
            "#............................#",
            "#............../###\\.....D...#",
            "###\\...###===#################",
            "####\\................F.......#",
            "#####\\.......................#",
            "######\\.......S..............#",
            "################====###.../###",
            "#.............F........../####",
            "#......................./#####",
            "#....................../######",
            "###\\...#######=====###########",
            "####\\...............F........#",
            "#####\\.......................#",
            "######\\...........S..........#",
            "##########=====########...{###",
            "#.......F................{####",
            "#.......................{#####",
            "#.P....................{######",
            "##############################",
            "##############################",
    };

    public Bedroom() {
        super(3, "Bedroom", "bedDouble", 3, 6,
              new String[] {"smallLamp", "pearlPortrait", "books", "mirror"}, new Color(205, 196, 232), new Color(170, 158, 210));
    }

    @Override
    public String[] getLayout() {
        return LAYOUT;
    }


    @Override
    protected void drawWallpaper(Graphics2D g, int w, int h) {
        paneledWall(g, w, h);
    }

    /** Size of the matching bedroom set (its sprites are tiny, 10-26 px). */
    private static final double K = 2.5;

    /** Row where something standing on a piece of furniture `pixels` tall (at size K) sits. */
    private static double on(double floor, int pixels) {
        return floor - pixels * K / GameConfig.TILE;
    }

    @Override
    protected void addFurniture(List<Decor> list) {
        // ===== bedroom 1 (top three floors): the kids' room =====
        // top floor: window | bunk bed | calendar, plant | (hill) | drawers with a trophy, poster, skateboard | exit
        add(list, "windowNarrow", 3.6, 3.0, 1.3);
        add(list, "plantH", 1.2, 4, 1);
        add(list, "bunkBottom", 7.2, 4, K);
        add(list, "bunkTop", 7.2, on(4, 14), K);
        add(list, "calendar", 12.9, 2.4, K);
        add(list, "plantLRK", 13.4, 4, 1.3);
        add(list, "drawerA", 20.3, 4, K);
        add(list, "trophy", 20.55, on(4, 13), K);
        add(list, "posterHeart", 22.2, 2.8, K);
        add(list, "skateboard", 21.5, 4, K);
        add(list, "plantH", 27.2, 4, 1);
        // floor 3: blue armchair, low bookshelf with an alarm clock, backpack, lamp, wardrobe | drawers, mirror
        add(list, "windowFour", 10.2, 6.7, 1.3);
        add(list, "armchairBlue", 7.3, 8, K);
        add(list, "lowShelf", 8.6, 8, K);
        add(list, "alarmClock", 9.0, on(8, 12), K);
        add(list, "backpack", 11.9, 8, K);
        add(list, "lampStandYellow", 12.9, 8, K);
        add(list, "wardrobe", 14.0, 8, K);
        add(list, "posterHeart", 17.6, 6.6, K);
        add(list, "drawerC", 20.2, 8, K);
        add(list, "bedLamp", 20.4, on(8, 13), K);
        add(list, "mirrorH", 21.3, 8, 0.85);
        add(list, "vaseLRK", 27.4, 8, 1.6);
        // floor 2: play corner with a toy box | bookshelf and plant
        add(list, "windowNarrow", 4.2, 10.8, 1.3);
        add(list, "plantSmall", 1.2, 12, 4);
        add(list, "toyBox", 7.3, 12, K);
        add(list, "lowShelf", 8.5, 12, K);
        add(list, "trophy", 8.85, on(12, 12), K);
        add(list, "paintingSmall", 10.4, 10.4, K);
        add(list, "lampStandBlue", 12.4, 12, K);
        add(list, "lowShelf", 19.4, 12, K);
        add(list, "backpack", 19.85, on(12, 12), K);
        add(list, "plantLRK", 21.5, 12, 1.3);
        add(list, "windowNarrow", 24.4, 7.0, 1.3);

        // ===== bedroom 2 (bottom two floors): the master bedroom =====
        // floor 1: drawers with a lamp | curtained window over the puddle | wardrobe, dresser, mirror, plant
        add(list, "drawerC", 7.3, 16, K);
        add(list, "bedLamp", 7.5, on(16, 13), K);
        add(list, "curtainWindow", 10.6, 14.8, 1.3);
        add(list, "wardrobe", 15.3, 16, K);
        add(list, "dresserSmall", 16.7, 16, K);
        add(list, "mirrorH", 18.3, 16, 0.85);
        add(list, "drawerA", 19.9, 16, K);
        add(list, "pottedFern", 21.2, 16, 0.5);
        add(list, "picture2", 24.5, 15.5, 1);
        add(list, "plantH", 27.2, 16, 1);
        // ground: dresser by the start | the double bed between two bedside drawers, a painting above | lamp, armchair
        add(list, "dresser", 4.3, 20, 1);
        add(list, "drawerA", 11.0, 20, K);
        add(list, "bedLamp", 11.2, on(20, 13), K);
        add(list, "bedRed", 12.15, 20, 3);
        add(list, "drawerA", 14.5, 20, K);
        add(list, "bedLamp", 14.7, on(20, 13), K);
        addFramed(list, "paintingLarge", 12.75, 18.5, 2);
        add(list, "lampStandBlue", 16.6, 20, K);
        add(list, "armchair", 20.2, 20, 0.75);
    }
}
