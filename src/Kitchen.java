import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/** Room 2: Kitchen */
public class Kitchen extends Room {
    private static final String[] LAYOUT = {
            "##############################",
            "#............................#",
            "#..............F.............#",
            "#............................#",
            "#...D........................#",
            "##########===#########../#####",
            "#...........F........../######",
            "#...................../#######",
            "#..................S./########",
            "######\\..####===#########....#",
            "#######\\...........F.........#",
            "########\\....................#",
            "#########\\...................#",
            "#.....#########====#####../###",
            "#........F.............../####",
            "#......................./#####",
            "#................S...../######",
            "###\\..####====################",
            "####\\..........F.............#",
            "#####\\.......................#",
            "######\\............/#\\.....P.#",
            "############=====#############",
    };

    public Kitchen() {
        super(2, "Kitchen Room", "stoveH", 2, 5,
              new String[] {"jar", "bottle", "burgerPlate"}, new Color(196, 229, 220), new Color(160, 204, 196));
    }

    @Override
    public String[] getLayout() {
        return LAYOUT;
    }

    @Override
    protected void drawWallpaper(Graphics2D g, int w, int h) {
        wallpaperImage(g, "res/Bg/Wallpaper1_Tile.png", 4, w, h); // peach striped wallpaper
    }

    /** Size of the kitchen set (its sprites are tiny, 3-28 px). */
    private static final double K = 2.5;

    /** Row where something standing on a piece `pixels` tall (at size K) sits. */
    private static double on(double floor, int pixels) {
        return floor - pixels * K / GameConfig.TILE;
    }

    /** Width in tiles of a piece `pixels` wide at size K. */
    private static double w(int pixels) {
        return pixels * K / GameConfig.TILE;
    }

    /** A counter: left end, `mids` middle pieces, an oven piece after `ovenAfter` middles, right end. Returns the right edge. */
    private static double counter(List<Decor> list, double col, double floor, int mids, int ovenAfter) {
        add(list, "kCounterL", col, floor, K);
        col += w(8);
        for (int i = 0; i <= mids; i++) {
            if (i == ovenAfter) { add(list, "kCounterOven", col, floor, K); col += w(16); }
            if (i < mids) { add(list, "kCounterM", col, floor, K); col += w(8); }
        }
        add(list, "kCounterR", col, floor, K);
        return col + w(8);
    }

    /** A short wall shelf (2 tiles) with two small things standing on it. */
    private static void shelf(List<Decor> list, double col, double bottom, String left, String right) {
        add(list, "wallShelfLong", col, bottom, 2);
        double top = bottom - 6 * 2.0 / GameConfig.TILE;
        add(list, left, col + 0.3, top, K);
        add(list, right, col + 1.2, top, K);
    }

    /** A potted poppy. */
    private static void poppy(List<Decor> list, double col, double floor) {
        add(list, "kFlowerpot", col, floor, K);
        add(list, "kPoppy", col + w(1), on(floor, 3), K);
    }

    /** A magnetic knife bar with knives hanging under it (`bottom` = bottom row of the knives). */
    private static void knives(List<Decor> list, double col, double bottom) {
        add(list, "kKnifeBar", col, bottom - w(6), K);
        add(list, "kKnife", col + w(2), bottom, K);
        add(list, "kKnife", col + w(6), bottom, K);
    }

    /** A hook rail with pans hanging from it (`bottom` = bottom row of the pans). */
    private static void pans(List<Decor> list, double col, double bottom) {
        add(list, "kHook", col, bottom - w(8), K);
        add(list, "kPans", col, bottom, K);
    }

    @Override
    protected void addFurniture(List<Decor> list) {
        // top floor (exit on the left): stove counter with a teapot, pans above | poppy | fridge with notes | poppy, window
        counter(list, 6.2, 5, 0, 0);
        add(list, "teapot", 7.15, on(5, 8), K);
        pans(list, 7.2, 3.6);
        poppy(list, 13.4, 5);
        add(list, "kFridge", 16.6, 5, K);
        add(list, "kNotes", 16.95, 3.75, K);
        add(list, "windowFour", 18.6, 3.0, 1.3);
        poppy(list, 20.3, 5);
        // third floor: window on the left ledge | double sink, pans above | table and chairs with a blender
        add(list, "windowNarrow", 3.0, 7.7, 1.3);
        add(list, "kSinkL", 9.2, 9, K);
        add(list, "kSinkR", 9.2 + w(14), 9, K);
        pans(list, 9.95, 7.6);
        add(list, "chairSmall", 16.15, 9, K);
        add(list, "kTable", 16.15 + w(8), 9, K);
        add(list, "kBlender", 17.7, on(9, 7), K);
        addFlipped(list, "chairSmall", 16.15 + w(8) + w(28), 9, K);
        // second floor: long counter with microwave and blender, knives and pans above | poppy, fridge
        double end = counter(list, 10.1, 13, 2, 2);
        add(list, "kMicrowave", 10.1 + w(8), on(13, 8), K);
        add(list, "mug", 10.1 + w(8) + w(10), on(13, 8), K);
        add(list, "kPan", end - w(8) - w(16) + w(3), on(13, 8), K);
        knives(list, 10.6, 11.4);
        pans(list, end - w(16) - w(8), 11.4);
        poppy(list, 20.2, 13);
        add(list, "kFridge", 21.8, 13, K);
        add(list, "kNotes", 22.15, 11.75, K);
        // first floor: window | small fridge | dining table with a teapot and a mug | poppy
        add(list, "windowFour", 2.6, 15.7, 1.3);
        add(list, "fridgeSmall", 6.2, 17, K);
        add(list, "chairSmall", 14.6, 17, K);
        add(list, "kTable", 14.6 + w(8), 17, K);
        add(list, "teapot", 15.6, on(17, 7), K);
        add(list, "mug", 16.5, on(17, 7), K);
        addFlipped(list, "chairSmall", 14.6 + w(8) + w(28), 17, K);
        poppy(list, 20.6, 17);
        // decorations in the empty corners: plants, pictures and wall shelves
        add(list, "plant", 1.25, 5, 1.3);                       // left of the exit
        add(list, "paintingLarge", 1.1, 3.4, 2);
        add(list, "paintingLarge", 10.9, 3.4, 2.5);             // over the top puddle
        shelf(list, 22.3, 3.2, "teapot", "kBlender");           // top right, over the ramp
        add(list, "plant", 27.4, 5, 1.5);                       // right-hand ledge
        add(list, "paintingSmall", 1.3, 6.9, 3);                // left ledge, third floor
        add(list, "plantLRK", 4.3, 9, 1.3);
        add(list, "paintingSmall", 6.6, 7.6, 3);
        shelf(list, 25.3, 10.8, "mug", "kFlowerpot");           // right side, second floor
        add(list, "plant", 27.5, 13, 1.4);
        add(list, "paintingLarge", 11.4, 15.6, 2.5);            // over the first-floor puddle
        add(list, "plant", 1.15, 17, 1.3);                      // left ledge, first floor
        // ground: fridge with notes, stove counter with a pan, pans above | poppy | window over the hill | sinks with knives above
        add(list, "kFridge", 7.1, 21, K);
        add(list, "kNotes", 7.45, 19.75, K);
        counter(list, 8.6, 21, 0, 0);
        add(list, "kPan", 8.6 + w(8) + w(3), on(21, 8), K);
        pans(list, 9.1, 19.6);
        poppy(list, 17.5, 21);
        add(list, "windowNarrow", 19.95, 19.65, 1.3);
        add(list, "kSinkL", 22.3, 21, K);
        add(list, "kSinkR", 22.3 + w(14), 21, K);
        add(list, "mug", 22.6, on(21, 10), K);
        knives(list, 22.9, 19.6);
    }
}
