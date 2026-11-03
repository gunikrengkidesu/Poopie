import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * The tissue hunter. Platformer physics with:
 *  - slopes: gravity pulls you down the ramp, so you slide down when you let go and climb more slowly
 *  - wet floors: you drift slowly but have almost no grip, so you must push hard to stop or turn
 *  - forgiving jumps (coyote time + jump buffering, short hop when the key is released early)
 */
public class Player extends Entity {
    public static final int HALF_WIDTH = 8, HEIGHT = 30;
    private static final double GRAVITY = 0.5, MAX_FALL = 11, JUMP_SPEED = -9.4;
    private static final double RUN_SPEED = 3.4, UPHILL_SPEED = 2.3;
    private static final double SLOPE_PULL = 0.15, SLIDE_SPEED = 2.3;   // sliding down a ramp
    // wet floor: you drift slowly, but there is almost no grip, so stopping or turning takes a long push
    private static final double WET_SPEED = 2.2, WET_ACCEL = 0.04, WET_FRICTION = 0.01;
    // wet ramp: stronger pull and little grip, but still climbable (slowly)
    private static final double WET_SLOPE_PULL = 0.25, WET_SLOPE_ACCEL = 0.35, WET_UPHILL_SPEED = 1.3, WET_SLIDE_SPEED = 3.4;
    private static final double STEP_UP = 12, SNAP_DOWN = 10;

    private final TileMap map;
    private final KeyHandler keys;
    private final CharacterSkin skin;

    private double vx, vy;
    private boolean onGround = true, onWetFloor, onSlope, facingRight, dead;
    private int coyoteFrames, jumpBufferFrames, dustFrames;
    private int idleFrames, pickupFrames, hurtFrames, punchFrames;
    private String pickupAnimation = "pickup_ground";
    private boolean climbing;
    private static final double CLIMB_SPEED = 2.2;
    private boolean enteringDoor, hidden;
    private long ticks;
    private String animation = "idle";
    private int animFrame, animTick;

    public Player(TileMap map, KeyHandler keys, CharacterSkin skin) {
        super((map.getSpawnCol() + 0.5) * GameConfig.TILE, (map.getSpawnRow() + 1) * GameConfig.TILE,
              HALF_WIDTH * 2, HEIGHT);
        this.map = map;
        this.keys = keys;
        this.skin = skin;
        this.facingRight = map.getSpawnCol() < GameConfig.COLS / 2;
    }

    // ------------------------------------------------------------------ state
    public boolean isDead() { return dead; }
    public boolean isOnGround() { return onGround; }

    /** Area that falling objects must touch: a bit taller than the hitbox so it matches the drawn head. */
    public Rectangle getHurtBounds() {
        return new Rectangle((int) (x - HALF_WIDTH + 1), (int) (y - HEIGHT - 10), HALF_WIDTH * 2 - 2, HEIGHT + 10);
    }

    /** Hit: a short "ouch" (damage animation) and then the death animation. */
    public void kill() {
        if (dead) return;
        dead = true;
        vx = 0;
        vy = Math.min(vy, 0);
        hurtFrames = 14;
        setAnimation(skin.frameCount("damage") > 0 ? "damage" : "death");
    }

    /** Picking up a tissue: a crouching grab on the ground, or a reaching grab in mid-air. */
    public void playPickup() {
        if (climbing || punchFrames > 0) return;
        if (onGround && Math.abs(vx) < 1) pickupAnimation = "pickup_ground";
        else if (!onGround) pickupAnimation = "pickup_wall";
        else return;
        if (skin.frameCount(pickupAnimation) == 0) return;
        pickupFrames = skin.frameCount(pickupAnimation) * (onGround ? 3 : 2);
        setAnimation(pickupAnimation);
    }

    // ------------------------------------------------------------------ punch
    /** True during the part of the punch where the fist is out. */
    public boolean isPunchActive() {
        int total = Math.max(1, skin.frameCount("punch")) * 3;
        return punchFrames > 0 && punchFrames < total - 3 && punchFrames > 4;
    }

    /** Area in front of the player that the punch reaches. */
    public Rectangle getPunchBounds() {
        int w = 24;
        return new Rectangle((int) (facingRight ? x + 2 : x - 2 - w), (int) (y - 26), w, 22);
    }

    public int getFacing() { return facingRight ? 1 : -1; }

    /** Walks into the doorway and plays the "use" animation (opening the bathroom door). */
    public void enterDoor(double doorX) {
        enteringDoor = true;
        x = doorX;
        vx = vy = 0;
        facingRight = true;
        setAnimation(skin.frameCount("use") > 0 ? "use" : "idle");
    }

    public boolean enterAnimationFinished() {
        int n = skin.frameCount("use");
        return n == 0 || (animation.equals("use") && animFrame >= n - 1 && animTick >= 4);
    }

    /** Once the player has gone through the door they are no longer drawn. */
    public void hide() { hidden = true; }

    public boolean deathAnimationFinished() {
        if (!animation.equals("death")) return false;
        int n = skin.frameCount("death");
        return n == 0 ? animTick > 60 : animFrame >= n - 1 && animTick >= 12;
    }

    // ------------------------------------------------------------------ update
    @Override
    public void update() {
        ticks++;
        if (dead) {
            vy = Math.min(vy + GRAVITY, MAX_FALL);
            moveVertically(vy);
            if (hurtFrames > 0 && --hurtFrames == 0) setAnimation("death");
            animate();
            return;
        }
        if (enteringDoor) {
            animate();
            return;
        }
        if (keys.consumePunch() && punchFrames == 0 && !climbing && skin.frameCount("punch") > 0) {
            punchFrames = skin.frameCount("punch") * 3;
            Sfx.play(Sfx.PUNCH);
            pickupFrames = 0;
            setAnimation("punch");
        }
        if (punchFrames > 0) punchFrames--;
        if (climbing || startClimbing()) {
            updateClimbing();
            animate();
            return;
        }
        int dir = (keys.rightHeld() ? 1 : 0) - (keys.leftHeld() ? 1 : 0);
        if (dir != 0) facingRight = dir > 0;

        updateHorizontalSpeed(dir);
        updateJump();
        vy = Math.min(vy + GRAVITY, MAX_FALL);
        moveHorizontally(vx);
        moveVertically(vy);
        x = Math.max(HALF_WIDTH + 1, Math.min(GameConfig.WIDTH - HALF_WIDTH - 1, x));
        readGround();

        if (onWetFloor && Math.abs(vx) > 1.5 && ticks % 5 == 0) dustFrames = 8;
        if (dustFrames > 0) dustFrames--;
        chooseAnimation(dir);
        animate();
    }

    // ------------------------------------------------------------------ ladders
    private boolean ladderAtBody() {
        return map.tileAt(x, y - 4).isLadder() || map.tileAt(x, y - HEIGHT / 2.0).isLadder();
    }

    /** UP next to a ladder, or DOWN on top of one, grabs it. */
    private boolean startClimbing() {
        boolean grabUp = keys.upHeld() && ladderAtBody();
        boolean grabDown = keys.downHeld() && (map.tileAt(x, y + 1).isLadder() || (ladderAtBody() && !onGround));
        if (!grabUp && !grabDown) return false;
        climbing = true;
        x = (Math.floor(x / GameConfig.TILE) + 0.5) * GameConfig.TILE; // centre on the ladder
        vx = vy = 0;
        keys.consumeJump();
        jumpBufferFrames = 0;
        punchFrames = 0;
        setAnimation("climb");
        return true;
    }

    private void updateClimbing() {
        int t = GameConfig.TILE;
        keys.consumeJump(); // UP is also the jump key: while on a ladder it only climbs
        int dir = (keys.rightHeld() ? 1 : 0) - (keys.leftHeld() ? 1 : 0);
        if (dir != 0) { // step off the side of the ladder
            climbing = false;
            facingRight = dir > 0;
            vx = dir * 2.0;
            onGround = false;
            return;
        }
        int v = (keys.downHeld() ? 1 : 0) - (keys.upHeld() ? 1 : 0);
        double ny = y + v * CLIMB_SPEED;
        if (v < 0 && !map.tileAt(x, ny - 1).isLadder()) {
            // climbed past the top rung: stand on top of the ladder
            double top = Math.ceil((ny - 1) / t) * t;
            if (map.tileAt(x, top + 1).isLadder()) {
                y = top;
                climbing = false;
                onGround = true;
                return;
            }
        }
        if (v > 0) {
            double floor = map.groundBetween(x, y + 0.01, ny, false); // real floor at the bottom
            if (!Double.isNaN(floor)) {
                y = floor;
                climbing = false;
                onGround = true;
                return;
            }
        }
        y = ny;
        if (!ladderAtBody() && !map.tileAt(x, y + 1).isLadder()) { climbing = false; onGround = false; return; }
        onGround = false;
        setAnimation("climb");
        if (v == 0) animTick = 0; // hold still on the rungs
    }

    private void updateHorizontalSpeed(int dir) {
        int downhill = onSlope ? groundTile().downhillDirection() : 0;
        boolean wetSlope = onSlope && onWetFloor;
        double maxSpeed = onWetFloor ? WET_SPEED : RUN_SPEED;
        if (downhill != 0 && dir == -downhill) maxSpeed = wetSlope ? WET_UPHILL_SPEED : UPHILL_SPEED;
        if (wetSlope && dir == downhill) maxSpeed = RUN_SPEED;
        double accel = onGround ? (wetSlope ? WET_SLOPE_ACCEL : onWetFloor ? WET_ACCEL : 0.75) : 0.45;
        double friction = onGround ? (onWetFloor ? WET_FRICTION : 0.8) : 0.12;
        if (downhill != 0 && dir == 0) friction = 0; // nothing holds you on a ramp

        if (dir != 0) {
            if (vx * dir < maxSpeed) {
                vx += dir * accel;
                if (vx * dir > maxSpeed) vx = dir * maxSpeed;
            } else {
                vx -= dir * friction;
                if (vx * dir < maxSpeed) vx = dir * maxSpeed;
            }
        } else if (Math.abs(vx) <= friction) {
            vx = 0;
        } else {
            vx -= Math.signum(vx) * friction;
        }

        // gravity along the ramp
        if (downhill != 0) {
            double pull = wetSlope ? WET_SLOPE_PULL : SLOPE_PULL;
            vx += downhill * pull;
            double cap = dir == 0 ? (wetSlope ? WET_SLIDE_SPEED : SLIDE_SPEED) : maxSpeed + pull; // letting go: slide
            if (Math.abs(vx) > cap) vx = Math.signum(vx) * cap;
        }
    }

    private void updateJump() {
        if (keys.consumeJump()) jumpBufferFrames = 7;
        if (onGround) coyoteFrames = 6; else if (coyoteFrames > 0) coyoteFrames--;
        if (jumpBufferFrames > 0) jumpBufferFrames--;
        if (jumpBufferFrames > 0 && coyoteFrames > 0) {
            vy = JUMP_SPEED;
            Sfx.play(Sfx.JUMP);
            onGround = false;
            coyoteFrames = 0;
            jumpBufferFrames = 0;
        }
        if (!keys.jumpHeld() && vy < -3.5) vy = -3.5;
    }

    /** The tile you are standing on: the ramp square your feet are in, or the square under them. */
    private Tile groundTile() {
        Tile inside = map.tileAt(x, y - 0.5);
        return inside.isSlope() ? inside : map.tileAt(x, y + 0.5);
    }

    private void readGround() {
        onWetFloor = false;
        onSlope = false;
        if (!onGround) return;
        onSlope = groundTile().isSlope();
        if (groundTile().isSlippery()) onWetFloor = true;
        for (double sx : new double[] {x - HALF_WIDTH + 1, x, x + HALF_WIDTH - 1})
            if (map.tileAt(sx, y + 0.5).isSlippery()) onWetFloor = true;
    }

    private void moveHorizontally(double dx) {
        if (dx == 0) return;
        int t = GameConfig.TILE;
        double nx = x + dx;
        double top = y - HEIGHT + 1;
        double bottom = y - (onGround ? STEP_UP : 6);
        double edge = dx > 0 ? nx + HALF_WIDTH : nx - HALF_WIDTH;
        int col = (int) Math.floor(edge / t);
        for (int r = (int) Math.floor(top / t); r <= (int) Math.floor(bottom / t); r++) {
            Tile tile = map.tile(col, r);
            if (tile.blocksSideways()) {
                x = dx > 0 ? col * t - HALF_WIDTH - 0.01 : (col + 1) * t + HALF_WIDTH + 0.01;
                vx = 0;
                return;
            }
            if (tile.isSlope()) {
                double floor = map.floorAt(edge, r);
                if (floor < bottom && (r + 1) * t > top) { vx = 0; return; }
            }
        }
        x = nx;
    }

    private void moveVertically(double dy) {
        int t = GameConfig.TILE;
        double[] samples = {x - HALF_WIDTH + 1, x, x + HALF_WIDTH - 1};
        if (dy < 0) {
            double oldTop = y - HEIGHT, newTop = oldTop + dy;
            double hit = Double.NEGATIVE_INFINITY;
            for (double sx : samples)
                for (int r = (int) Math.floor(newTop / t); r <= (int) Math.floor(oldTop / t); r++) {
                    double c = map.ceilingAt(sx, r);
                    if (!Double.isNaN(c) && c <= oldTop + 0.5 && c > newTop) hit = Math.max(hit, c);
                }
            if (hit > Double.NEGATIVE_INFINITY) { y = hit + HEIGHT; vy = 0.5; }
            else y += dy;
            onGround = false;
            return;
        }
        double start = y - (onGround ? STEP_UP : 6);
        double end = y + dy + (onGround ? SNAP_DOWN : 0);
        // the centre of the feet decides; the edges only matter when standing on the lip of a ledge
        double ground = map.groundBetween(x, start, end);
        if (Double.isNaN(ground)) {
            for (double sx : new double[] {samples[0], samples[2]}) {
                double g = map.groundBetween(sx, start, end);
                if (!Double.isNaN(g) && (Double.isNaN(ground) || g < ground)) ground = g;
            }
        }
        if (!Double.isNaN(ground)) {
            y = ground;
            vy = 0;
            onGround = true;
        } else {
            y += dy;
            onGround = false;
        }
    }

    // ------------------------------------------------------------------ animation
    private void chooseAnimation(int dir) {
        boolean moving = Math.abs(vx) > 0.6;
        idleFrames = (onGround && !moving && dir == 0) ? idleFrames + 1 : 0;
        if (punchFrames > 0) return; // the punch plays to the end, even while moving
        if (pickupFrames > 0) {
            pickupFrames--;
            boolean keep = pickupAnimation.equals("pickup_wall") ? !onGround : onGround && dir == 0 && !moving;
            if (keep) return; // let the pick-up animation finish
            pickupFrames = 0;
        }
        if (!onGround) setAnimation(vy < 0 ? "jump" : "fall");
        else if (onSlope && dir == 0 && moving) {
            facingRight = vx > 0;
            setAnimation("land"); // sliding down a ramp
        } else if (moving && (dir != 0 || !onWetFloor)) setAnimation(Math.abs(vx) < 2.6 ? "walk" : "run");
        else if (idleFrames > 240) setAnimation("wait"); // bored after 4 seconds
        else setAnimation("idle");
    }

    private void setAnimation(String name) {
        if (skin.frameCount(name) == 0 && !name.equals("death")) name = name.equals("run") ? "walk" : "idle";
        if (!name.equals(animation)) {
            animation = name;
            animFrame = 0;
            animTick = 0;
        }
    }

    private void animate() {
        int speed;
        switch (animation) {
            case "run": speed = 5; break;
            case "walk": speed = 6; break;
            case "use": speed = 4; break;
            case "pickup_ground": case "punch": speed = 3; break;
            case "pickup_wall": speed = 2; break;
            case "climb": speed = 5; break;
            case "damage": speed = 7; break;
            case "idle": case "wait": speed = 10; break;
            default: speed = 8;
        }
        if (++animTick < speed) return;
        int n = Math.max(1, skin.frameCount(animation));
        if (animation.equals("death") || animation.equals("use")) { // play once and hold the last frame
            if (animFrame < n - 1) { animFrame++; animTick = 0; }
        } else {
            animFrame = (animFrame + 1) % n;
            animTick = 0;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (hidden) return;
        if (dustFrames > 0) {
            g.setColor(new Color(200, 235, 255, 30 * dustFrames));
            g.fillOval((int) x + (vx > 0 ? -12 : 6), (int) y - 6, 8, 6);
        }
        BufferedImage frame = skin.frame(animation, animFrame, facingRight);
        if (frame == null) {
            g.setColor(Color.BLUE);
            g.fill(getBounds());
            return;
        }
        double scale = 1.45;
        int w = (int) (frame.getWidth() * scale), h = (int) (frame.getHeight() * scale);
        int dx = (int) Math.round(x - w / 2.0);
        int dy = (int) Math.round(y - (skin.baseline(animation) + 1) * scale);
        g.drawImage(frame, dx, dy, w, h, null);
    }
}
