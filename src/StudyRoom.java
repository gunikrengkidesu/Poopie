import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/** Room 4: Study & Library (the study room and the library in one) */
public class StudyRoom extends Room {
    private static final String[] LAYOUT = {
            "##############################",
            "#.......F....................#",
            "#............................#",
            "#............../####==#####..#",
            "#\\............/..F........../#",
            "##\\........../............./##",
            "###\\......S./............./###",
            "######===###.........{########",
            "#..F................{........#",
            "#..................{.........#",
            "#............S....{..........#",
            "#...##===#########...........#",
            "#.....F......................#",
            "###..........................#",
            "###.....................D....#",
            "#######\\.............######..#",
            "########\\..............F.....#",
            "#########\\.....#####}........#",
            "#############\\......#}.......#",
            "##############\\.....##}......#",
            "###############\\....###}..P..#",
            "##############################",
    };

    public StudyRoom() {
        super(4, "Study Room", "deskPC2", 4, 6,
              new String[] {"books", "clock", "vanGogh", "monaLisa"}, new Color(196, 214, 170), new Color(150, 176, 128));
    }

    @Override
    public String[] getLayout() {
        return LAYOUT;
    }

    @Override
    protected void drawWallpaper(Graphics2D g, int w, int h) {
        wallpaperImage(g, "res/Bg/Wallpaper1_Tile.png", 4, w, h); // peach striped wallpaper, like the kitchen
    }

    @Override
    protected void addFurniture(List<Decor> list) {
        // top-left wall: window, clock and a landscape painting
        add(list, "windowNarrow", 3, 3.5, 1.6);
        add(list, "paintingHills", 6.5, 3, 1.5);
        add(list, "clockLRK", 12.6, 2.4, 1.6);
        // top platform: a big bookshelf and a vase on a side table | a lamp on a nightstand and a plant
        add(list, "bookshelfLarge", 16.1, 3, 2.4);
        add(list, "sideTable", 18.1, 3, 4);
        add(list, "flowerVase", 18.2, 2, 1.6);
        add(list, "sideTable", 22.3, 3, 2.5);                     // decor table with a potted aloe
        add(list, "kFlowerpot", 22.34, 2.375, 2.5);
        add(list, "aloe", 22.22, 2.14, 2.5);
        add(list, "plantLRK", 26, 3, 1.4);
        // portrait gallery under the top platform
        addFramed(list, "monaLisa", 15.7, 6.3, 2);
        addFramed(list, "portraitBlue", 18.7, 6.3, 2);
        addFramed(list, "vanGogh", 20.2, 6.3, 2);
        // left platform: a tall bookshelf, a filled shelf and a floor lamp (the pool stays clear)
        add(list, "windowFour", 10.0, 4.6, 1.6);
        add(list, "bookshelfTall", 4.05, 7, 1.2);
        add(list, "bookshelfMedium", 9.1, 7, 1.15);
        add(list, "floorLampTall", 10.55, 7, 2.5);
        // long middle platform: plant | shelf, table-and-chairs set, tall bookshelf, lamp, plant
        add(list, "plantLRK3", 4.2, 11, 1.4);
        add(list, "windowNarrow", 1.4, 10.4, 1.4);
        add(list, "bookshelfTall", 9.1, 11, 1.2);               // a tall bookshelf and a laptop desk with a chair
        add(list, "chairSmall", 10.35, 11, 2.5);
        add(list, "tableLeft", 10.975, 11, 2.5);
        add(list, "tableRight", 11.6, 11, 2.5);
        add(list, "laptop", 11.21, 10.375, 2.5);
        add(list, "bookshelfTall", 13.4, 11, 1.2);
        add(list, "floorLamp", 15.0, 11, 1.4);
        add(list, "plantLRK2", 16.3, 11, 1.4);
        // middle wall and the right-hand wall
        add(list, "paintingCity", 9, 14.6, 1.4);
        addFramed(list, "pearlPortrait", 11.4, 14.2, 2);
        addFramed(list, "portraitHat", 13.2, 14.2, 2);
        add(list, "wallShelfBooks", 16.97, 14, 1);
        add(list, "windowFour", 21.6, 11.3, 1.6);
        add(list, "curtainWindow", 26.0, 12.4, 1.3);
        // left ledge: a plant
        add(list, "plantLRK2", 3.05, 15, 1.4);
        // exit ledge: an empty shelf | door | plant
        add(list, "bookshelfBlitz", 21.0, 15, 1.1);
        add(list, "plantLRK3", 26.05, 15, 1.3);
        // the shelf on the stairs and the platform above the reading nook
        add(list, "bookshelfLarge", 10.25, 18, 2);
        add(list, "floorLampTall", 11.9, 18, 2);
        add(list, "waterCooler", 16.8, 17, 1);
        // reading nook under the platform: a low book table and a floor lamp beside it
        add(list, "wallShelfBooks", 16.2, 21, 1.2);
        add(list, "floorLampTall", 18.75, 21, 2.5);
        // by the start door: a portrait and a tall bookshelf
        addFramed(list, "portraitRed", 27.7, 17.9, 1.6);
        add(list, "bookshelfTall", 27.88, 21, 1.3);
    }
}
