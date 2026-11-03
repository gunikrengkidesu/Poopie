import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Scatters tissue rolls over random reachable spots, spread out across the room. */
public class TissueSpawner {
    private final TileMap map;
    private final Random random;

    public TissueSpawner(TileMap map, Random random) {
        this.map = map;
        this.random = random;
    }

    public List<Tissue> spawn(int count) {
        List<int[]> candidates = new ArrayList<>();
        Set<Integer> safe = new ReachabilityAnalyzer(map).safeSquares();
        for (int key : safe) {
            int r = ReachabilityAnalyzer.rowOf(key), c = ReachabilityAnalyzer.colOf(key);
            if (isGoodSpot(r, c)) candidates.add(new int[] {r, c});
        }
        Collections.shuffle(candidates, random);

        List<int[]> chosen = new ArrayList<>();
        // try to keep rolls far apart; relax the spacing if the room is too small
        for (int spacing = 7; spacing >= 0 && chosen.size() < count; spacing -= 2) {
            for (int[] cand : candidates) {
                if (chosen.size() >= count) break;
                if (chosen.contains(cand)) continue;
                boolean farEnough = true;
                for (int[] other : chosen)
                    if (Math.abs(other[0] - cand[0]) + Math.abs(other[1] - cand[1]) < spacing) { farEnough = false; break; }
                if (farEnough) chosen.add(cand);
            }
        }
        List<Tissue> tissues = new ArrayList<>();
        for (int i = 0; i < chosen.size(); i++) tissues.add(new Tissue(chosen.get(i)[1], chosen.get(i)[0], i));
        return tissues;
    }

    /** Flat dry floor, not on the start square, not in the doorway and not inside a shelf. */
    private boolean isGoodSpot(int r, int c) {
        if (map.tile(c, r).isSlope()) return false;
        if (map.tile(c, r + 1).isSlippery()) return false; // wet floors are kept clear
        if (r == map.getSpawnRow() && Math.abs(c - map.getSpawnCol()) <= 2) return false;
        if (r == map.getDoorRow() && Math.abs(c - map.getDoorCol()) <= 1) return false;
        for (int[] h : map.getHazardCells()) if (h[0] == c && h[1] == r) return false;
        for (int[] s : map.getSnakeCells()) if (s[1] == r && Math.abs(s[0] - c) <= 1) return false;
        return true;
    }
}
