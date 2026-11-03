import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

/** Loads every image from res/ once and hands out named sprites cut from the tile sheets. */
public final class Assets {
    private static final Map<String, BufferedImage> files = new HashMap<>();
    private static final Map<String, BufferedImage> sprites = new HashMap<>();
    private static final String HOUSE = "res/Obj/house_interior.png";
    private static final String OFFICE = "res/Bg/Office.png";
    private static final String SHOP = "res/Bg/Shop_1.png";
    private static final String URBAN = "res/Obj/UrbanB.png";
    private static final String DECOR = "res/Obj/Decorations (32x32).png";
    private static final String POOL = "res/Bg/TileSet_2.png";
    private static final String LRK_LIVING = "res/Obj/livingroom_LRK.png";
    private static final String LRK_CABINETS = "res/Obj/cabinets_LRK.png";
    private static final String LRK_DECOR = "res/Obj/decorations_LRK.png";
    private static final String LRK_DOORS = "res/Door/doorswindowsstairs_LRK.png";
    private static final String FURNITURE2 = "res/Obj/Furniture-2.png";
    private static final String OBJECTS = "res/Obj/Objects.png";
    private static final String SHELVES = "res/Obj/filledbookshelves.png";
    private static final String INTERIORS = "res/Obj/Interiors_free_32x32.png";

    private Assets() {}

    /**
     * Opens a file from res/, working whether the game is started from the project root, src/ or an IDE.
     * Returns null when the file cannot be found. The caller closes the stream.
     */
    public static InputStream open(String path) {
        try {
            File classFolder = new File(Assets.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (classFolder.isFile()) classFolder = classFolder.getParentFile();
            String[] candidates = {
                path, "../" + path, "../../" + path,
                new File(classFolder, path).getPath(),
                new File(classFolder, "../" + path).getPath(),
                new File(classFolder, "../../" + path).getPath()
            };
            for (String candidate : candidates) {
                File f = new File(candidate);
                if (f.isFile()) return new java.io.FileInputStream(f);
            }
        } catch (Exception ignored) {
            // fall through to the classpath
        }
        return Assets.class.getResourceAsStream("/" + path);
    }

    /** Loads (and caches) an image from res/. */
    public static BufferedImage image(String path) {
        if (files.containsKey(path)) return files.get(path);
        BufferedImage img = null;
        try (InputStream in = open(path)) {
            if (in != null) img = ImageIO.read(in);
        } catch (Exception e) {
            System.err.println("Cannot load " + path + ": " + e.getMessage());
        }
        if (img == null) System.err.println("Missing asset: " + path);
        files.put(path, img);
        return img;
    }

    public static BufferedImage crop(String sheet, int x, int y, int w, int h) {
        BufferedImage src = image(sheet);
        if (src == null || x + w > src.getWidth() || y + h > src.getHeight()) return null;
        return src.getSubimage(x, y, w, h);
    }

    public static BufferedImage flipX(BufferedImage img) {
        if (img == null) return null;
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-img.getWidth(), 0);
        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(img, out);
        return out;
    }

    // ---------------------------------------------------------------- furniture & props
    private static void def(String name, String sheet, int x, int y, int w, int h) {
        sprites.put(name, crop(sheet, x, y, w, h));
    }

    /** Named props cut from the sheets in res/Obj and res/Bg, or made from single pictures. */
    public static BufferedImage sprite(String name) {
        if (sprites.isEmpty()) {
            // drawn 2-4x bigger so they come out about as big as the props they replaced
            enlarged("sofa", "res/Obj/Couch_large_red.png", 3);
            enlarged("armchair", "res/Obj/Couch_small_2_red.png", 4);
            enlarged("clock", "res/Obj/Clock_1_blue.png", 2);
            enlarged("books", "res/Obj/Books_outside_5.png", 2);
            enlarged("smallLamp", "res/Obj/Lamp_bedroom_3.png", 2);
            enlarged("ironBed", "res/Obj/Bed_red.png", 4);
            enlarged("oven", "res/Obj/Oven.png", 3);
            enlarged("fridgeWhite", "res/Obj/Refrigerator_large_white.png", 2);
            enlarged("fridgeOpen", "res/Obj/Fridge.png", 2);
            def("plant", "res/Obj/decorations_LRK.png", 80, 80, 16, 31);
            def("landscape", "res/Obj/decorations_LRK.png", 112, 96, 32, 16);
            def("mirror", "res/Obj/Furniture-2.png", 80, 105, 16, 20);
            whole("ovenOn", "res/Obj/Sprite-0010-32x26.png");
            whole("fridgeDark", "res/Obj/refrigerator.png");
            def("stoveH", HOUSE, 200, 130, 32, 38);
            def("nightstand", HOUSE, 201, 420, 30, 35);
            def("deskPC2", HOUSE, 340, 228, 40, 39);
            def("bookshelfH", HOUSE, 344, 26, 32, 55);
            def("washer", HOUSE, 441, 417, 30, 39);
            def("toiletH", HOUSE, 541, 518, 22, 38);
            def("sinkH", HOUSE, 540, 616, 24, 20);
            def("mirrorH", HOUSE, 534, 408, 36, 43);
            def("plantH", HOUSE, 252, 528, 33, 41);
            def("lockers", HOUSE, 336, 402, 95, 53);
            def("picture1", HOUSE, 300, 528, 24, 33);
            def("picture2", HOUSE, 348, 528, 24, 36);
            def("picture4", HOUSE, 296, 628, 32, 25);
            def("shower", HOUSE, 480, 507, 38, 68);
            def("jukebox", HOUSE, 104, 217, 32, 47);
            def("bedDouble", HOUSE, 144, 604, 48, 60);
            // livingroom_LRK.png (side view)
            def("sofaBeige", LRK_LIVING, 16, 32, 48, 32);
            def("sofaGrey", LRK_LIVING, 16, 96, 48, 32);
            def("armchairBeige", LRK_LIVING, 240, 32, 32, 32);
            def("armchairGrey", LRK_LIVING, 240, 96, 32, 32);
            def("fireplace", LRK_LIVING, 480, 80, 32, 48);
            def("diningTable", LRK_LIVING, 16, 160, 48, 32);
            def("diningChair", LRK_LIVING, 128, 144, 16, 32);
            def("roundSideTable", LRK_LIVING, 568, 182, 32, 16);
            def("tvOn", LRK_LIVING, 358, 374, 36, 20);
            def("tvStand", LRK_CABINETS, 528, 32, 48, 25);
            // cabinets_LRK.png
            def("bookcase", LRK_CABINETS, 16, 16, 48, 48);
            def("glassCabinet", LRK_CABINETS, 208, 19, 48, 45);
            def("dresser", LRK_CABINETS, 336, 30, 48, 34);
            // decorations_LRK.png
            def("floorLamp", LRK_DECOR, 48, 15, 16, 47);
            def("floorLamp2", LRK_DECOR, 16, 16, 16, 46);
            def("tableLamp", LRK_DECOR, 120, 22, 16, 25);
            def("paintingCity", LRK_DECOR, 112, 64, 32, 16);
            def("paintingHills", LRK_DECOR, 112, 96, 32, 16);
            def("plantLRK", LRK_DECOR, 48, 80, 16, 31);
            def("plantLRK2", LRK_DECOR, 80, 80, 16, 31);
            def("plantLRK3", LRK_DECOR, 16, 81, 15, 30);
            def("vaseLRK", LRK_DECOR, 66, 121, 13, 19);
            def("floorLamp3", LRK_DECOR, 80, 16, 16, 46);
            def("tableLamp2", LRK_DECOR, 152, 22, 16, 25);
            def("clockLRK", LRK_DECOR, 16, 128, 16, 16);
            def("mirrorLRK", LRK_DECOR, 160, 64, 16, 46);
            // doorswindowsstairs_LRK.png
            def("doorLRK", LRK_DOORS, 113, 16, 30, 48);
            def("windowDay", LRK_DOORS, 208, 17, 48, 30);
            def("windowNight", LRK_DOORS, 208, 49, 48, 30);
            // side-view bedroom set (k / g / w / Dood)
            def("pottedFern", "res/Obj/g.png", 406, 118, 64, 56);
            def("curtains", "res/Obj/w.png", 154, 172, 138, 98);
            def("curtainsOpen", "res/Obj/w1.png", 150, 172, 142, 98);
            def("pottedFern2", "res/Obj/g.png", 406, 118, 64, 56);
            def("woodWardrobe", "res/Obj/Dood.png", 400, 176, 112, 182);
            // Furniture-2.png
            def("bathtub", FURNITURE2, 0, 130, 48, 30);
            def("toiletF2", FURNITURE2, 2, 100, 14, 28);
            def("toiletSmall", FURNITURE2, 48, 108, 16, 20);
            def("pedestalSink", FURNITURE2, 64, 104, 16, 24);
            def("bathMirror", FURNITURE2, 80, 105, 16, 20);
            def("curtainWindow", FURNITURE2, 64, 32, 64, 32);
            def("bedsideTable", FURNITURE2, 226, 52, 30, 28);
            def("openWardrobe", FURNITURE2, 160, 2, 32, 46);
            def("flowerVase", FURNITURE2, 112, 6, 16, 22);
            def("chairFront", FURNITURE2, 65, 69, 14, 27);
            def("chairSide", FURNITURE2, 81, 68, 14, 28);
            def("chairSideL", FURNITURE2, 113, 68, 14, 28);
            def("rugOval", FURNITURE2, 146, 80, 28, 16);
            def("lampWhite", FURNITURE2, 243, 34, 9, 13);
            def("lampYellow", FURNITURE2, 131, 82, 9, 13);
            def("deskWood", FURNITURE2, 128, 53, 49, 29);
            def("tableWood", FURNITURE2, 193, 60, 33, 36);
            def("nightstandF2", FURNITURE2, 224, 53, 17, 27);
            def("smallWindowF2", FURNITURE2, 22, 3, 19, 26);
            def("windowNarrow", FURNITURE2, 4, 32, 23, 32);
            def("windowBlindsHalf", FURNITURE2, 54, 3, 19, 26);
            def("windowBlinds", FURNITURE2, 86, 3, 19, 26);
            def("windowFour", FURNITURE2, 32, 32, 31, 32);
            def("chairBack", FURNITURE2, 97, 75, 14, 21);
            // Objects.png (also used as falling objects)
            def("jar", OBJECTS, 80, 0, 16, 16);
            def("bottle", OBJECTS, 96, 0, 16, 16);
            def("burgerPlate", OBJECTS, 0, 32, 16, 16);
            def("monaLisa", OBJECTS, 32, 64, 16, 16);
            def("pearlPortrait", OBJECTS, 48, 64, 16, 16);
            def("vanGogh", OBJECTS, 112, 64, 16, 16);
            def("portraitBlue", OBJECTS, 64, 64, 16, 16);
            def("portraitHat", OBJECTS, 80, 64, 16, 16);
            def("portraitRed", OBJECTS, 96, 64, 16, 16);
            // filledbookshelves.png
            def("bookshelfWide", SHELVES, 0, 0, 128, 40);
            def("bookshelfMedium", SHELVES, 22, 48, 36, 40);
            // Interiors_free_32x32.png
            def("bigPlant", INTERIORS, 334, 1426, 38, 62);
            def("redLamp", INTERIORS, 452, 1650, 26, 46);
            // library: empty bookshelves, overlays, furniture.png, Wall.png
            def("emptyShelfWide", "res/Obj/emptybookshelves1.png", 0, 0, 128, 40);
            def("emptyShelfWideDark", "res/Obj/emptybookshelves1.png", 0, 96, 128, 40);
            def("emptyShelfSmall", "res/Obj/emptybookshelves1.png", 22, 48, 36, 40);
            def("emptyShelfNarrow", "res/Obj/emptybookshelves1.png", 0, 48, 16, 40);
            def("sideShelf", SHELVES, 22, 48, 36, 40);
            def("sideShelf2", "res/Obj/emptybookshelves1.png", 22, 48, 36, 40);
            def("filledShelfDark", "res/Obj/filledbookshelves.png", 0, 96, 128, 40);
            def("ivy", "res/Bg/overlay.png", 16, 3, 64, 13);
            def("wallLamp", "res/Bg/overlay.png", 83, 8, 10, 20);
            def("candle", "res/Bg/overlay.png", 129, 9, 6, 22);
            def("archWall", "res/Bg/overlaywalls.png", 0, 0, 128, 64);
            def("miniPainting", "res/Bg/overlaywalls.png", 49, 64, 14, 15);
            def("miniPainting2", "res/Bg/overlaywalls.png", 2, 64, 12, 15);
            def("readingTable", "res/Obj/furniture.png", 129, 39, 30, 25);
            def("woodChair", "res/Obj/furniture.png", 133, 64, 21, 32);
            def("sofaPurple", "res/Obj/furniture.png", 50, 48, 28, 34);
            def("panelWindow", "res/Bg/Wall.png", 32, 16, 32, 32);
            def("panelWindowDark", "res/Bg/Wall.png", 0, 16, 32, 32);
            // promo pictures from the asset packs, hung as framed posters
            def("paintingLarge", "res/Obj/Painting_Large.png", 0, 0, 16, 16);
            def("posterShowcase", "res/Bg/showcase.png", 0, 0, 850, 615);
            // Office.png
            def("cityDay", OFFICE, 0, 407, 48, 73);
            def("citySunset", OFFICE, 0, 503, 48, 73);
            def("cityNight", OFFICE, 0, 599, 48, 73);
            def("microwaveCounter", OFFICE, 48, 505, 48, 50);
            def("coffeeCounter", OFFICE, 48, 594, 48, 53);
            def("copier", OFFICE, 96, 610, 48, 37);
            def("drawerCabinet", OFFICE, 105, 408, 30, 47);
            def("printerCabinet", OFFICE, 105, 504, 30, 54);
            def("waterCooler", OFFICE, 61, 687, 22, 57);
            def("cityDusk", OFFICE, 0, 695, 48, 73);
            def("plainDesk", OFFICE, 48, 226, 48, 40);
            def("officeDesk", OFFICE, 48, 22, 48, 50);
            def("officeDeskBlue", OFFICE, 48, 118, 48, 50);
            def("officeDeskTV", OFFICE, 96, 124, 48, 44);
            // Shop_1.png
            def("pinball", SHOP, 9, 209, 30, 58);
            def("arcade", SHOP, 56, 210, 32, 54);
            def("snackShelf", SHOP, 436, 206, 40, 56);
            def("pantryShelf", SHOP, 481, 198, 48, 64);
            def("drinkFridge", SHOP, 535, 206, 34, 58);
            def("vanity", SHOP, 528, 686, 48, 58);
            def("toiletStall", SHOP, 528, 576, 48, 96);
            // UrbanB.png
            def("shutterWindow", URBAN, 150, 296, 36, 28);
            def("tallWindow", URBAN, 108, 292, 24, 56);
            def("squareWindow", URBAN, 56, 342, 32, 32);
            def("trashCan", URBAN, 157, 637, 22, 38);
            def("pinkWindow", URBAN, 156, 342, 26, 34);
            def("flowerPlanter", URBAN, 338, 736, 46, 32);
            def("fruitBox", URBAN, 344, 484, 40, 30);
            // Decorations (32x32).png
            def("shelf", DECOR, 88, 64, 32, 16);
            def("archWindow", DECOR, 72, 104, 34, 42);
            // TileSet_2.png
            def("stripedTowel", POOL, 288, 405, 48, 100);
            def("swimRing", POOL, 340, 405, 48, 48);
            def("lifeBuoy", POOL, 340, 455, 48, 48);
            defSmallObjects();
        }
        return sprites.get(name);
    }

    /** The single-object pictures in res/Obj (most are drawn on an 8px grid, so they are shown 3-4x bigger). */
    private static void defSmallObjects() {
        String o = "res/Obj/";
        // living room
        whole("sofaRed", o + "Sofa.png");
        whole("softChairRed", o + "SoftChair.png");
        whole("tvRetro", o + "Television.png");
        whole("tvTable", o + "TelevisionTable.png");
        whole("tvBig", o + "ChannelPixel-34x31.png");
        whole("floorLampTall", o + "FloorLamp.png");
        whole("standingLamp", o + "LampLight Saga-23x67.png");
        whole("plantSmall", o + "Plant.png");
        whole("paintingSmall", o + "Painting_Small.png");
        whole("shelfLarge", o + "Shelf_large.png");
        whole("shelfMedium", o + "Shelf_medium.png");
        // one matching bedroom set (same artist): beds, wardrobe, drawers, lamps and kids' things
        whole("bedYellow", o + "Bed_yellow.png");
        whole("bedRed", o + "Bed_red.png");
        whole("bunkBottom", o + "Bunkbed_bottom_red.png");
        whole("bunkTop", o + "Bunkbed_top_green.png");
        whole("wardrobe", o + "Cabinet_1.png");
        whole("drawerA", o + "Drawer_medium_1_a.png");
        whole("drawerC", o + "Drawer_medium_1_c.png");
        whole("lampStandBlue", o + "Lamp_stand_1.png");
        whole("lampStandYellow", o + "Lamp_stand_2.png");
        whole("bedLamp", o + "Lamp_bedroom_3.png");
        whole("posterHeart", o + "Poster_1.png");
        whole("windowSquare", o + "Window_square.png");
        whole("windowRound", o + "Window_circle.png");
        whole("curtainRed", o + "Curtain_medium_red.png");
        whole("curtainGreen", o + "Curtain_medium_green.png");
        whole("backpack", o + "Backpack_blue.png");
        whole("skateboard", o + "Skateboard_green.png");
        whole("trophy", o + "Trophy.png");
        whole("armchairBlue", o + "Couch_small_2_blue.png");
        whole("calendar", o + "Calendar_2.png");
        whole("lowShelf", o + "Bookshelf_small.png");
        whole("alarmClock", o + "Alarm_clock_red.png");
        whole("toyBox", o + "Box_blue_small.png");
        whole("radio", o + "Radio.png");
        whole("sideTable", o + "DecorTable.png");
        whole("tvCabinet", o + "Sprite-0015-76x29.png");
        whole("wallShelfBooks", o + "Sprite-0012-64x32.png");
        // kitchen
        whole("fridgeSmall", o + "Fridge.png");
        whole("fridgeTall", o + "FridgePixelQuest-34x81.png");
        whole("fridgeMagnets", o + "refrigerator.png");
        whole("fridgeMagnetsSmall", o + "refrigerator.png");
        whole("teapot", o + "Teapot.png");
        whole("cookingPot", o + "Pot.png");
        whole("mug", o + "Mug.png");
        whole("stoveBlue", o + "Sprite-0010-61x50.png");
        whole("stoveBlueSmall", o + "Sprite-0010-32x26.png");
        whole("chairSmall", o + "Chair.png");
        whole("tableSmall", o + "Table_Single.png");
        row("counterRow", o + "KitchenCounter_Left.png", o + "KitchenCounter_Mid.png", o + "KitchenCounter_Oven.png",
                o + "KitchenCounter_Mid.png", o + "KitchenCounter_Right.png");
        row("tableLong", o + "Table_Left.png", o + "Table_Mid.png", o + "Table_Right.png");
        // kitchen set (same artist as the bedroom set): fridges, sink and counter pieces, wall racks, small things
        whole("kFridge", o + "Refrigerator_large_white.png");
        whole("kNotes", o + "Refrigerator_sticky_notes_1.png");
        whole("kSinkL", o + "Sink-2.png");
        whole("kSinkM", o + "Sink_connecting.png");
        whole("kSinkR", o + "Sink_end.png");
        whole("kKnifeBar", o + "Magnet_knife_holder.png");
        whole("kKnife", o + "Knife_1.png");
        whole("kHook", o + "Kitchen_utensil_hook.png");
        whole("kPans", o + "Hanging_cooking_pans.png");
        whole("kCounterL", o + "KitchenCounter_Left.png");
        whole("kCounterM", o + "KitchenCounter_Mid.png");
        whole("kCounterOven", o + "KitchenCounter_Oven.png");
        whole("kCounterR", o + "KitchenCounter_Right.png");
        whole("kMicrowave", o + "Microwave_white.png");
        whole("kTable", o + "Table_long.png");
        whole("kFlowerpot", o + "Flowerpot_medium_yellow.png");
        whole("kPoppy", o + "Flora_poppy.png");
        whole("kBlender", o + "Blender_blue.png");
        whole("kPan", o + "Cooking_pan.png");
        whole("tableLeft", o + "Table_Left.png");
        whole("tableRight", o + "Table_Right.png");
        whole("laptop", o + "Laptop.png");
        whole("aloe", o + "Flora_aloe.png");
        // living room set
        whole("couchBlue", o + "Couch_large_blue.png");
        whole("couchSideBlue", o + "Couch_small_2_blue_side.png");
        whole("cabL", o + "Countertop.png");
        whole("cabM", o + "Countertop_connecting.png");
        whole("cabR", o + "Countertop_end.png");
        whole("vaseRed", o + "Vase_red.png");
        whole("aster", o + "Flora_aster.png");
        whole("daisy", o + "Flora_daisy.png");
        whole("potGreen", o + "Flowerpot_medium_green.png");
        whole("bat", o + "Baseball_bat.png");
        whole("coffeeTable", o + "Coffee_table_1.png");
        whole("booksRow", o + "Books_inside_1.png");
        // bedroom
        whole("bedBlue", o + "bed-1.png");
        whole("dresserSmall", o + "Dresser.png");
        whole("tableLampWhite", o + "TableLamp'.png");
        whole("lowDresser", o + "Sprite-0007-108x24.png");
        whole("windowBlue", o + "window-32x32.png");
        whole("windowBlueWide", o + "window-56x43.png");
        // study room and library
        whole("bookshelfBlitz", o + "bookshelf-2.png");
        whole("bookshelfTall", o + "bookshelf-1.png");
        whole("bookshelfLarge", o + "bookshelfs-3.png");
        whole("bookshelfSmall", o + "bookshelfs-4.png");
        whole("candleSmall", o + "Candle.png");
        row("wallShelfLong", o + "Shelf_Left.png", o + "Shelf_Mid.png", o + "Shelf_Mid.png", o + "Shelf_Right.png");
        column("ceilingLamp", o + "CeilingLamp_Top.png", o + "CeilingLamp_Mid.png", o + "CeilingLamp_Mid.png",
                o + "CeilingLamp_Bottom.png");
        // toilet
        whole("toiletSmall2", o + "Toilet.png");
        whole("sinkSmall", o + "Sink.png");
        whole("bathtubShower", o + "BarthTub.png");
        whole("rubberDuck", o + "RubberDuck.png");
        whole("bathMat", o + "Sprite-0013-26x9.png");
    }

    /** A whole picture drawn {@code times} bigger, keeping its pixels sharp. */
    private static void enlarged(String name, String path, int times) {
        BufferedImage img = image(path);
        if (img == null) return;
        BufferedImage out = new BufferedImage(img.getWidth() * times, img.getHeight() * times, BufferedImage.TYPE_INT_ARGB);
        new AffineTransformOp(AffineTransform.getScaleInstance(times, times), AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
                .filter(img, out);
        sprites.put(name, out);
    }

    private static void whole(String name, String path) {
        BufferedImage img = image(path);
        if (img != null) sprites.put(name, img);
    }

    /** Pieces joined side by side (bottoms lined up), e.g. a counter made of left, middle and right parts. */
    private static void row(String name, String... paths) {
        joined(name, true, paths);
    }

    /** Pieces stacked top to bottom, e.g. a ceiling lamp: mount, chain, shade. */
    private static void column(String name, String... paths) {
        joined(name, false, paths);
    }

    private static void joined(String name, boolean sideways, String... paths) {
        int w = 0, h = 0;
        for (String p : paths) {
            BufferedImage img = image(p);
            if (img == null) return;
            w = sideways ? w + img.getWidth() : Math.max(w, img.getWidth());
            h = sideways ? Math.max(h, img.getHeight()) : h + img.getHeight();
        }
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = out.createGraphics();
        int at = 0;
        for (String p : paths) {
            BufferedImage img = image(p);
            if (sideways) { g.drawImage(img, at, h - img.getHeight(), null); at += img.getWidth(); }
            else { g.drawImage(img, (w - img.getWidth()) / 2, at, null); at += img.getHeight(); }
        }
        g.dispose();
        sprites.put(name, out);
    }

    private static final Set<String> missing = new HashSet<>();

    /** True if the file exists (no error message when it does not). */
    public static boolean exists(String path) {
        if (files.containsKey(path)) return files.get(path) != null;
        if (missing.contains(path)) return false;
        try {
            File classFolder = new File(Assets.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (classFolder.isFile()) classFolder = classFolder.getParentFile();
            String[] candidates = {
                path, "../" + path, "../../" + path,
                new File(classFolder, path).getPath(),
                new File(classFolder, "../" + path).getPath(),
                new File(classFolder, "../../" + path).getPath()
            };
            for (String c : candidates) if (new File(c).isFile()) return true;
            if (Assets.class.getResource("/" + path) != null) return true;
        } catch (Exception ignored) { }
        missing.add(path);
        return false;
    }

    /** Lowest row that has a visible pixel. */
    public static int opaqueBottom(BufferedImage img) {
        for (int y = img.getHeight() - 1; y >= 0; y--)
            for (int x = 0; x < img.getWidth(); x++)
                if ((img.getRGB(x, y) >>> 24) > 20) return y;
        return img.getHeight() - 1;
    }
}
