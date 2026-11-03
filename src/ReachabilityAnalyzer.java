import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Works out which squares the player can stand in, using simple moves: walk, walk on ramps, drop off
 * ledges, jump up to 2 squares high and climb ladders. Used to put tissues only where they can be collected.
 */
public class ReachabilityAnalyzer {
    private static final int ROWS = GameConfig.ROWS, COLS = GameConfig.COLS;
    private final TileMap map;

    public ReachabilityAnalyzer(TileMap map) {
        this.map = map;
    }

    /** Squares reachable from the start AND from which the start can be reached again (no dead ends). */
    public Set<Integer> safeSquares() {
        int start = key(map.getSpawnRow(), map.getSpawnCol());
        Set<Integer> forward = search(start, false);
        Set<Integer> backward = search(start, true);
        forward.retainAll(backward);
        return forward;
    }

    public static int key(int row, int col) { return row * COLS + col; }
    public static int rowOf(int key) { return key / COLS; }
    public static int colOf(int key) { return key % COLS; }

    private Set<Integer> search(int start, boolean reverse) {
        // collect the graph once, then walk it forwards or backwards
        Set<Integer> seen = new HashSet<>();
        Deque<Integer> queue = new ArrayDeque<>();
        if (!reverse) {
            seen.add(start);
            queue.add(start);
            while (!queue.isEmpty()) {
                int n = queue.poll();
                for (int m : moves(rowOf(n), colOf(n))) if (seen.add(m)) queue.add(m);
            }
            return seen;
        }
        Set<Integer> all = search(start, false);
        seen.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            int target = queue.poll();
            for (int n : all) {
                if (seen.contains(n)) continue;
                if (moves(rowOf(n), colOf(n)).contains(target)) { seen.add(n); queue.add(n); }
            }
        }
        return seen;
    }

    private boolean blocked(int r, int c) { return map.tile(c, r).blocksSideways(); }
    private boolean slope(int r, int c) { return map.tile(c, r).isSlope(); }
    private boolean solidish(int r, int c) { return blocked(r, c) || slope(r, c); }
    private boolean ladder(int r, int c) { return map.tile(c, r).isLadder(); }

    /** Can the player's body occupy square (r, c) while standing? */
    private boolean canStand(int r, int c) {
        if (r <= 0 || r >= ROWS - 1 || c <= 0 || c >= COLS - 1) return false;
        if (blocked(r, c)) return false;
        if (slope(r, c) || ladder(r, c)) return true;
        return map.tile(c, r + 1).isFlatFloor();
    }

    private int landingRow(int fromRow, int col, boolean stopAtSlope) {
        int rr = fromRow;
        while (rr < ROWS - 1 && !canStand(rr, col) && !solidish(rr + 1, col) && !(stopAtSlope && slope(rr, col))) rr++;
        if (canStand(rr, col)) return rr;
        // falling onto a ramp: you land on the ramp square itself
        return rr + 1 < ROWS - 1 && slope(rr + 1, col) ? rr + 1 : -1;
    }

    private List<Integer> moves(int r, int c) {
        List<Integer> out = new ArrayList<>();
        Tile here = map.tile(c, r);
        // climbing: up while on a ladder, down onto a ladder that starts under your feet or that you are on
        if (ladder(r, c) && canStand(r - 1, c)) out.add(key(r - 1, c));
        if ((ladder(r, c) || ladder(r + 1, c)) && canStand(r + 1, c)) out.add(key(r + 1, c));
        for (int d = -1; d <= 1; d += 2) {
            int nc = c + d;
            // walking (one square sideways, possibly one up or down along a ramp)
            List<int[]> candidates = new ArrayList<>();
            boolean climbing = here.isSlope() && ((SlopeTile) here).isRisingRight() == (d == 1);
            if (climbing) candidates.add(new int[] {r - 1, nc});
            candidates.add(new int[] {r, nc});
            if (slope(r + 1, nc)) candidates.add(new int[] {r + 1, nc});
            boolean walked = false;
            for (int[] cand : candidates) {
                if (canStand(cand[0], cand[1])) { out.add(key(cand[0], cand[1])); walked = true; break; }
            }
            // stepping off a ledge
            if (!walked && !blocked(r, nc) && !slope(r, nc)) {
                int land = landingRow(r, nc, false);
                if (land >= 0) out.add(key(land, nc));
            }
        }
        // jumping: up to 2 squares high, up to 3-4 squares across
        for (int up = 0; up <= 2; up++) {
            boolean clear = true;
            for (int k = 0; k < up; k++) if (solidish(r - 1 - k, c)) clear = false;
            if (!clear) continue;
            int top = r - up;
            for (int d = -1; d <= 1; d += 2) {
                int maxDist = up == 2 ? 3 : 4;
                for (int dist = 1; dist <= maxDist; dist++) {
                    int nc = c + d * dist;
                    if (nc <= 0 || nc >= COLS - 1) break;
                    if (blocked(top, nc)) break;
                    int land = landingRow(top, nc, true);
                    if (land >= 0) out.add(key(land, nc));
                    if (solidish(top, nc)) break;
                }
            }
        }
        return out;
    }
}
