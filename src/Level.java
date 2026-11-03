import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** One attempt at one room: the map, the player, the tissues, the hazards, the doors and the clock. */
public class Level implements Updatable, Drawable {
    public enum Status { PLAYING, ENTERING_DOOR, DYING, FAILED, COMPLETE }

    private final Room room;
    private final TileMap map;
    private final Player player;
    private final Door door;
    private final EntranceDoor entrance;
    private final List<Collectible> tissues = new ArrayList<>();
    private final List<Hazard> hazards = new ArrayList<>();
    private Status status = Status.PLAYING;
    private double timeLeft;
    private int collected;
    private String failReason = "";
    private int dyingFrames;

    public Level(Room room, CharacterSkin skin, KeyHandler keys, Random random) {
        this.room = room;
        this.map = new TileMap(room);
        this.player = new Player(map, keys, skin);
        this.door = new Door(map);
        this.entrance = new EntranceDoor(map);
        door.setShowsToilet(room.getNumber() == GameConfig.LEVEL_COUNT); // last level: the real toilet
        this.timeLeft = room.getTimeLimit();
        tissues.addAll(new TissueSpawner(map, random).spawn(room.getTissueCount()));
        int i = 0;
        for (int[] cell : map.getHazardCells())
            hazards.add(new FallingObject(map, cell[0], cell[1], room.getFallingSprite(i++), room.getDifficulty(), random));
        if (room.getDifficulty().hasSnakes())
            for (int[] cell : map.getSnakeCells()) hazards.add(new Snake(map, cell[0], cell[1], room.getDifficulty()));
        door.setProgress(0, tissues.size());
    }

    @Override
    public void update() {
        map.update();
        door.update();
        entrance.update();
        for (Collectible t : tissues) t.update();
        for (Hazard h : hazards) h.update();

        if (status == Status.DYING) {
            player.update();
            if (player.deathAnimationFinished() && ++dyingFrames > 25) status = Status.FAILED;
            return;
        }
        if (status == Status.ENTERING_DOOR) {
            player.update();
            if (player.enterAnimationFinished()) {
                player.hide();
                door.close();
            }
            if (door.isClosed()) status = Status.COMPLETE;
            return;
        }
        if (status != Status.PLAYING) return;

        player.update();
        for (Collectible t : tissues) {
            if (player.intersects(t) && t.collect()) {
                collected++;
                Sfx.play(Sfx.COLLECT);
                player.playPickup();
                door.setProgress(collected, tissues.size());
                if (collected >= tissues.size()) door.open();
            }
        }
        for (Hazard h : hazards) {
            h.observe(player);
            if (player.isPunchActive()) h.punched(player.getPunchBounds(), player.getFacing());
            if (h.hits(player)) { fail(h.getDeathMessage()); return; }
        }
        timeLeft -= 1.0 / GameConfig.FPS;
        if (timeLeft <= 0) { timeLeft = 0; fail("TIME'S UP!"); return; }
        if (player.getY() > GameConfig.HEIGHT + 40) { fail("FELL OUT OF THE ROOM!"); return; }
        if (door.canEnter(player)) {
            status = Status.ENTERING_DOOR;
            player.enterDoor(door.getX());
        }
    }

    private void fail(String reason) {
        failReason = reason;
        player.kill();
        dyingFrames = 0;
        status = Status.DYING;
    }

    @Override
    public void draw(Graphics2D g) {
        map.draw(g);
        entrance.draw(g);
        door.draw(g);
        for (Collectible t : tissues) t.draw(g);
        for (Hazard h : hazards) h.draw(g);
        player.draw(g);
    }

    public Room getRoom() { return room; }
    public Status getStatus() { return status; }
    public double getTimeLeft() { return timeLeft; }
    public double getTimeUsed() { return room.getTimeLimit() - timeLeft; }
    public int getCollected() { return collected; }
    public int getRequired() { return tissues.size(); }
    public String getFailReason() { return failReason; }
    public boolean hasLadders() {
        for (int r = 0; r < GameConfig.ROWS; r++)
            for (int c = 0; c < GameConfig.COLS; c++) if (map.tile(c, r).isLadder()) return true;
        return false;
    }

    public boolean hasSnakes() {
        for (Hazard h : hazards) if (h instanceof Snake) return true;
        return false;
    }
}
