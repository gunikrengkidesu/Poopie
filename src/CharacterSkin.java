import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** All animation frames of one playable character, facing both ways. */
public class CharacterSkin {
    private static final String[] ANIMATIONS = {"idle", "wait", "talk", "walk", "run", "jump", "fall", "land", "climb", "punch", "damage", "death",
                                               "pickup_ground", "pickup_wall", "use"};

    private final String name;
    private final Map<String, BufferedImage[]> right = new HashMap<>();
    private final Map<String, BufferedImage[]> left = new HashMap<>();
    private final Map<String, Integer> baselines = new HashMap<>();

    /** Loads frames named res/Char/{folder}/{prefix}{animation}_{n}.png */
    public CharacterSkin(String name, String folder, String prefix) {
        this.name = name;
        for (String anim : ANIMATIONS) {
            List<BufferedImage> frames = new ArrayList<>();
            int bottom = 0;
            for (int i = 0; i < 20; i++) {
                String path = "res/Char/" + folder + "/" + prefix + anim + "_" + i + ".png";
                if (!Assets.exists(path)) break;
                BufferedImage img = Assets.image(path);
                if (img == null) break;
                frames.add(img);
                bottom = Math.max(bottom, Assets.opaqueBottom(img));
            }
            if (frames.isEmpty()) continue;
            BufferedImage[] r = frames.toArray(new BufferedImage[0]);
            BufferedImage[] l = new BufferedImage[r.length];
            for (int i = 0; i < r.length; i++) l[i] = Assets.flipX(r[i]);
            right.put(anim, r);
            left.put(anim, l);
            // each animation sits at a different height in its frames, so remember where the feet are
            baselines.put(anim, bottom);
        }
    }

    public String getName() { return name; }

    public BufferedImage frame(String anim, int index, boolean facingRight) {
        Map<String, BufferedImage[]> set = facingRight ? right : left;
        BufferedImage[] frames = set.containsKey(anim) ? set.get(anim) : set.get("idle");
        if (frames == null || frames.length == 0) return null;
        return frames[Math.floorMod(index, frames.length)];
    }

    public int frameCount(String anim) {
        BufferedImage[] frames = right.get(anim);
        return frames == null ? 0 : frames.length;
    }

    /** Pixel row of the feet inside the 64x64 frame. */
    public int baseline(String anim) {
        Integer b = baselines.containsKey(anim) ? baselines.get(anim) : baselines.get("idle");
        return b == null ? 63 : b;
    }
}
