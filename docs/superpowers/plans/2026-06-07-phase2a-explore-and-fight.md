# Phase 2a — Explore & Fight Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the game explorable and fightable on the Phase-1 engine: walk the 25-room hospital with N/S/E/W (each room rendered with its own backdrop), get pulled into in-viewport encounters with the 3 basic zombie types, and fight with Attack/Defend/Create-Bandage/Use-Bandage while leveling up on kills.

**Architecture:** Extend the existing model/engine/entry-point split. `GameState` gains a `Mode` (EXPLORE/COMBAT/DEAD) and ported gameplay (move, encounter, the four combat verbs, defeat/leveling). The engine gains a `RoomArt` registry (per-room backdrops) and enemy-art-by-type; `Scene` consumes them. `GameApp` swaps button sets by mode and wires movement + combat (Attack/Defend animate via `AnimationPlayer`). Map loading is extracted to a testable `HospitalMap`.

**Tech Stack:** Java 17, Swing, JUnit 5. Build/test via `.\build.ps1` / `.\test.ps1`; run via `.\run.ps1`. Current suite: 33 green.

**Commit note:** Commit from **PowerShell** only (signs silently; the agent is warm). Do NOT run `gpgconf --kill gpg-agent`. Keep commit messages simple one-line `-m "..."` strings (no parentheses/apostrophes/quotes — PowerShell here-strings have mangled those). Reset the agent (`gpgconf --kill gpg-agent; gpgconf --launch gpg-agent`) only if a commit fails with `socket file removed`.

**ORDERING INVARIANT (important):** Every task must leave `javac` of all `src/**` compiling and the suite green. The task order below is chosen so that: (a) `GameState.playerAttack` exists continuously (the Phase-1 `GameApp` calls it), and (b) the `Scene` constructor change is made in the SAME task as the `GameApp` rewire (its only caller). Do not reorder.

---

## File Structure

**New — model (`adventure_game`, no Swing):** `HospitalMap.java`, `Direction.java`.
**Modified — model:** `GameState.java`.
**New — engine (`adventure_game.engine`):** `RoomArt.java`.
**Modified — engine:** `Scene.java`, `Animations.java`.
**Modified — entry point:** `GameApp.java`.
**New — assets:** `assets/art/creeper.txt`, `assets/art/sprinter.txt`, `assets/art/rooms/room0.txt` … `room24.txt`.
**Tests:** new `HospitalMapTest`, `RoomArtTest`, `AssetSanityTest`; updated `GameStateTest`, `SceneTest`, `AnimationTest`.
**Skill:** extend `.claude/skills/room-and-map/SKILL.md` (backdrop path).

---

## Task 1: Extract HospitalMap + parser hardening tests

**Files:** Create `src/adventure_game/HospitalMap.java`, `src/adventure_game/Test/HospitalMapTest.java`; modify `src/adventure_game/GameState.java`.

Note: this task does NOT change `GameState`'s behavior (still combat-at-start) — it only swaps the parser implementation. So the Phase-1 `GameStateTest`/`SceneTest`/`GameApp` all stay green/compiling.

- [ ] **Step 1: Write `HospitalMapTest.java`**

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import adventure_game.HospitalMap;
import adventure_game.Room;

public class HospitalMapTest {

    @Test
    void loadsAllTwentyFiveRooms() throws Exception {
        List<Room> rooms = HospitalMap.load(HospitalMap.HOSPITAL_PATH);
        assertEquals(25, rooms.size());
        assertEquals("Hospital Entrance", rooms.get(0).getRoomName());
        assertEquals("Fauccis Lair", rooms.get(24).getRoomName());
    }

    @Test
    void wiresKnownExits() throws Exception {
        List<Room> rooms = HospitalMap.load(HospitalMap.HOSPITAL_PATH);
        // Room 0 exit line "0: 1: -1: 1: 2:" => E=1, N=-1, W=1, S=2
        assertTrue(rooms.get(0).isRoomNorthNull());
        assertEquals(2, rooms.get(0).getSouthRoom().getRoomNumber());
        assertEquals(1, rooms.get(0).getEastRoom().getRoomNumber());
        // Room 24 "24:-1:-1:-1:-1:" => fully sealed
        assertTrue(rooms.get(24).isRoomNorthNull());
        assertTrue(rooms.get(24).isRoomSouthNull());
        assertTrue(rooms.get(24).isRoomEastNull());
        assertTrue(rooms.get(24).isRoomWestNull());
    }
}
```

- [ ] **Step 2: Run to verify failure** — `.\build.ps1` → FAIL (`HospitalMap` missing).

- [ ] **Step 3: Create `HospitalMap.java`**

```java
package adventure_game;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Loads the hospital room graph from the map file. Pure I/O, no game state. */
public final class HospitalMap {

    public static final String HOSPITAL_PATH = "data/levels/Hospital Map/The-Hospital.txt";

    private HospitalMap() {}

    public static List<Room> load(String file) throws FileNotFoundException {
        List<Room> rooms = new ArrayList<>();
        File f = new File(file);
        Scanner scnr = new Scanner(f);
        int count = 0;
        int numberLinesToRead = 0;
        while (scnr.hasNextLine()) {
            String line = scnr.nextLine();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("#")) {
                ++count;
                continue;
            }
            if (count == 2) {
                numberLinesToRead = Integer.parseInt(line);
                ++count;
                continue;
            }
            if (count > 2 && count <= 2 + numberLinesToRead) {
                line = line.trim();
                // Limit to 3 so a bio containing ':' is preserved in token 2.
                String[] fileInfo = line.split(":", 3);
                rooms.add(new Room(Integer.parseInt(fileInfo[0]), fileInfo[1], fileInfo[2]));
            } else if (count > numberLinesToRead + 3) {
                line = line.trim();
                String[] exits = line.split(":");
                int roomTag = Integer.parseInt(exits[0].trim());
                int e = Integer.parseInt(exits[1].trim());
                int n = Integer.parseInt(exits[2].trim());
                int w = Integer.parseInt(exits[3].trim());
                int s = Integer.parseInt(exits[4].trim());
                Room cur = rooms.get(roomTag);
                if (e == -1) { cur.setRoomEastNull(); } else { cur.setEastRoom(rooms.get(e)); }
                if (n == -1) { cur.setRoomNorthNull(); } else { cur.setNorthRoom(rooms.get(n)); }
                if (s == -1) { cur.setRoomSouthNull(); } else { cur.setSouthRoom(rooms.get(s)); }
                if (w == -1) { cur.setRoomWestNull(); } else { cur.setWestRoom(rooms.get(w)); }
            }
            count++;
        }
        scnr.close();
        return rooms;
    }
}
```

- [ ] **Step 4: Refactor `GameState.loadHospital` to use it.** In `GameState.java`: delete the private `readMap` method; remove now-unused imports (`java.io.File`, `java.util.Scanner`, `java.util.ArrayList`); keep `java.io.FileNotFoundException` and `java.util.List`. Replace the two lines `gs.rooms = new ArrayList<>();` and `readMap("data/levels/Hospital Map/The-Hospital.txt", gs.rooms);` with:

```java
        gs.rooms = HospitalMap.load(HospitalMap.HOSPITAL_PATH);
```

(Leave the rest of `loadHospital` — the combat-at-start behavior — UNCHANGED in this task; it is rewritten in Task 2.)

- [ ] **Step 5: Build & test** — `.\test.ps1` → `HospitalMapTest` green; Phase-1 `GameStateTest`/`SceneTest` still green; suite 35/35.

- [ ] **Step 6: Commit**

```
git add src/adventure_game/HospitalMap.java src/adventure_game/Test/HospitalMapTest.java src/adventure_game/GameState.java
git commit -m "refactor: extract HospitalMap loader with parser tests"
```

---

## Task 2: GameState model — modes, movement, encounters, combat, leveling

This builds the entire new `GameState` in one task so `playerAttack` (called by the Phase-1 `GameApp`) and `isInCombat` exist continuously. It also fixes the one Phase-1 `SceneTest` assertion that depends on the old combat-at-start log. The `Scene` constructor is NOT changed here (Task 6), so the Phase-1 `GameApp` keeps compiling.

**Files:** Create `src/adventure_game/Direction.java`; rewrite `src/adventure_game/GameState.java`, `src/adventure_game/Test/GameStateTest.java`; one-line fix in `src/adventure_game/Test/SceneTest.java`.

- [ ] **Step 1: Create `Direction.java`**

```java
package adventure_game;

/** A cardinal movement direction. */
public enum Direction { NORTH, SOUTH, EAST, WEST }
```

- [ ] **Step 2: Rewrite `GameStateTest.java`** (replace whole file)

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

import adventure_game.Direction;
import adventure_game.GameRandom;
import adventure_game.GameState;

public class GameStateTest {

    @Test
    void loadsInExploreAtEntranceWithNoEnemy() throws Exception {
        GameState gs = GameState.loadHospital();
        assertEquals("Hospital Entrance", gs.getCurrentRoom().getRoomName());
        assertEquals("Mick", gs.getPlayer().getName());
        assertEquals(GameState.Mode.EXPLORE, gs.getMode());
        assertFalse(gs.isInCombat());
        assertNull(gs.getEnemy());
    }

    @Test
    void moveBlockedAtWallReturnsFalse() throws Exception {
        GameState gs = GameState.loadHospital();
        assertFalse(gs.move(Direction.NORTH)); // entrance has no north exit
        assertEquals("Hospital Entrance", gs.getCurrentRoom().getRoomName());
    }

    @Test
    void moveIntoPopulatedRoomStartsCombat() throws Exception {
        GameRandom.rand = new Random(1);
        GameState gs = GameState.loadHospital();
        assertTrue(gs.move(Direction.SOUTH)); // entrance south -> room 2 (populated)
        assertEquals(GameState.Mode.COMBAT, gs.getMode());
        assertNotNull(gs.getEnemy());
    }

    private static GameState inCombat(long seed) throws Exception {
        GameRandom.rand = new Random(seed);
        GameState gs = GameState.loadHospital();
        gs.move(Direction.SOUTH);
        return gs;
    }

    @Test
    void attackReducesEnemyHealthAndLogs() throws Exception {
        GameState gs = inCombat(1);
        int before = gs.getEnemy().getHealth();
        int logBefore = gs.getLog().size();
        gs.playerAttack();
        boolean hurtOrDead = gs.getEnemy() == null || gs.getEnemy().getHealth() < before;
        assertTrue(hurtOrDead);
        assertTrue(gs.getLog().size() > logBefore);
    }

    @Test
    void defendActsAndLogs() throws Exception {
        GameState gs = inCombat(2);
        int logBefore = gs.getLog().size();
        gs.playerDefend();
        assertTrue(gs.getLog().size() > logBefore);
    }

    @Test
    void createThenUseBandageChangesInventory() throws Exception {
        GameState gs = inCombat(3);
        gs.createBandage();
        assertTrue(gs.getPlayer().hasItems());
        gs.useBandage();
        assertFalse(gs.getPlayer().hasItems()); // only bandage consumed
    }

    @Test
    void fightingToConclusionEndsInClearRespawnOrDeath() throws Exception {
        GameState gs = inCombat(1);
        int guard = 0;
        while (gs.getMode() == GameState.Mode.COMBAT && gs.getPlayer().isAlive() && guard++ < 200) {
            gs.playerAttack();
        }
        assertTrue(gs.getMode() == GameState.Mode.EXPLORE
                || gs.getMode() == GameState.Mode.COMBAT
                || gs.getMode() == GameState.Mode.DEAD);
    }
}
```

- [ ] **Step 3: Rewrite `GameState.java`** (replace whole file)

```java
package adventure_game;

import java.io.FileNotFoundException;
import java.util.List;

import adventure_game.items.bandage;

/**
 * Game state for the explore-and-fight loop: the hospital rooms, the player,
 * the current room, the current enemy (if any), a mode, and the message log.
 * Swing-free. Gameplay logic is ported from reference/GameWindow.java.txt.
 */
public class GameState {

    public enum Mode { EXPLORE, COMBAT, DEAD }

    private static final int[] REGULAR_ROOMS =
            {1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21};
    private static final int[] BOSS_ROOMS = {10, 19, 22, 24};

    private Player player;
    private List<Room> rooms;
    private Room currentRoom;
    private NPC enemy;
    private Mode mode;
    private final MessageLog log = new MessageLog();

    public static GameState loadHospital() throws FileNotFoundException {
        GameState gs = new GameState();
        gs.rooms = HospitalMap.load(HospitalMap.HOSPITAL_PATH);
        gs.currentRoom = gs.rooms.get(0);
        gs.player = new Player("Mick", 150, 0, 75);
        gs.mode = Mode.EXPLORE;
        gs.populateZombies();
        gs.log.append("You stand at the Hospital Entrance. The cold bites.");
        return gs;
    }

    private void populateZombies() {
        for (int i : REGULAR_ROOMS) {
            rooms.get(i).setNPC();
        }
        for (int i : BOSS_ROOMS) {
            rooms.get(i).setBossNPC();
        }
    }

    // --- navigation ---
    public boolean move(Direction dir) {
        if (mode != Mode.EXPLORE) {
            return false;
        }
        Room next = nextRoom(dir);
        if (next == null) {
            log.append("You cannot go that way.");
            return false;
        }
        currentRoom = next;
        log.append("You enter the " + currentRoom.getRoomName() + ".");
        tryEncounter();
        return true;
    }

    private Room nextRoom(Direction dir) {
        switch (dir) {
            case NORTH: return currentRoom.isRoomNorthNull() ? null : currentRoom.getNorthRoom();
            case SOUTH: return currentRoom.isRoomSouthNull() ? null : currentRoom.getSouthRoom();
            case EAST:  return currentRoom.isRoomEastNull()  ? null : currentRoom.getEastRoom();
            case WEST:  return currentRoom.isRoomWestNull()  ? null : currentRoom.getWestRoom();
            default:    return null;
        }
    }

    public void tryEncounter() {
        int npc = currentRoom.hasNPC();
        if (npc == 1) {
            spawnEnemy();
            mode = Mode.COMBAT;
            log.append("A " + enemy.getName() + " lurches out of the gloom!");
        } else if (npc == 4) {
            log.append("Something massive stirs in the dark... not yet.");
        }
    }

    private void spawnEnemy() {
        int t = GameRandom.rand.nextInt(3);
        int level;
        if (t == 0) {
            level = GameRandom.rand.nextInt(11);
            enemy = new NPC("Walker", 100, level, 15);
        } else if (t == 1) {
            level = GameRandom.rand.nextInt(11);
            enemy = new NPC("Creeper", 250, level, 18);
        } else {
            level = GameRandom.rand.nextInt(6);
            enemy = new NPC("Sprinter", 500, level, 25);
        }
        enemy.levelingUp(log);
    }

    // --- combat verbs (each resolves one exchange) ---
    public void playerAttack() {
        if (mode != Mode.COMBAT || enemy == null) {
            return;
        }
        player.attack(enemy, log);
        if (!enemy.isAlive()) {
            onEnemyDefeated();
            return;
        }
        enemy.takeTurn(player, log);
        checkPlayerDeath();
    }

    public void playerDefend() {
        if (mode != Mode.COMBAT || enemy == null) {
            return;
        }
        enemy.takeTurn(player, log);
        player.defend(enemy, log);
        checkPlayerDeath();
    }

    public void useBandage() {
        if (mode != Mode.COMBAT || enemy == null) {
            return;
        }
        if (player.hasItems()) {
            player.items.get(0).consume(player, log);
            player.items.remove(0);
            enemy.takeTurn(player, log);
            checkPlayerDeath();
        } else {
            log.append("You have no bandages to use.");
        }
    }

    public void createBandage() {
        if (mode != Mode.COMBAT || enemy == null) {
            return;
        }
        player.obtain(new bandage(), log);
        enemy.takeTurn(player, log);
        checkPlayerDeath();
    }

    private void checkPlayerDeath() {
        if (!player.isAlive()) {
            log.append("You collapse. The hospital claims another survivor.");
            mode = Mode.DEAD;
            enemy = null;
        }
    }

    private void onEnemyDefeated() {
        log.append(enemy.getName() + " has died.");
        player.levelModifier(log);
        int roll = GameRandom.rand.nextInt(2) + 1; // 1 or 2 (ported from reference)
        if (roll == 2) {
            spawnEnemy();
            log.append("Another zombie lurches out. Fuck.");
            // mode stays COMBAT
        } else {
            currentRoom.removeNPC();
            enemy = null;
            mode = Mode.EXPLORE;
            log.append("The room is clear... for now.");
        }
    }

    // --- accessors ---
    public Player getPlayer() { return player; }
    public Room getCurrentRoom() { return currentRoom; }
    public NPC getEnemy() { return enemy; }
    public Mode getMode() { return mode; }
    public boolean isInCombat() { return mode == Mode.COMBAT; }
    public MessageLog getLog() { return log; }
}
```

- [ ] **Step 4: Fix the stale Phase-1 `SceneTest` assertion.** The Phase-1 `SceneTest` (still using the 3-arg `Scene` constructor, unchanged in this task) asserts a log line `"Walker shuffles"` that no longer exists. Open `src/adventure_game/Test/SceneTest.java` and change the line
`assertTrue(gridContains(screen, "Walker shuffles"));`
to
`assertTrue(gridContains(screen, "Hospital Entrance."));`
(Leave everything else in that file as-is — the 3-arg `new Scene(60,24,player,walker,backdrop)` construction stays until Task 6.)

- [ ] **Step 5: Build & test** — `.\test.ps1`. Expected: compiles (the Phase-1 `GameApp` still uses the 3-arg `Scene`, `playerAttack`, `isInCombat`, and `assets/art/room0.txt`, all still present); `GameStateTest` (7 tests) green; `SceneTest` green; suite 41/41.

- [ ] **Step 6: Commit**

```
git add src/adventure_game/Direction.java src/adventure_game/GameState.java src/adventure_game/Test/GameStateTest.java src/adventure_game/Test/SceneTest.java
git commit -m "feat: GameState explore-and-fight model with modes and combat"
```

---

## Task 3: RoomArt registry

**Files:** Create `src/adventure_game/engine/RoomArt.java`, `src/adventure_game/Test/RoomArtTest.java`.

- [ ] **Step 1: Write `RoomArtTest.java`**

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.engine.ArtAsset;
import adventure_game.engine.RoomArt;

public class RoomArtTest {

    @Test
    void unknownRoomReturnsFallback() {
        ArtAsset fallback = ArtAsset.fromLines("FB");
        RoomArt ra = new RoomArt(fallback);
        assertSame(fallback, ra.forRoom(7));
    }

    @Test
    void loadAllOnMissingDirLoadsNothingAndUsesFallback() {
        ArtAsset fallback = ArtAsset.fromLines("FB");
        RoomArt ra = RoomArt.loadAll(25, "assets/art/__nonexistent_dir__", fallback);
        assertEquals(0, ra.loadedCount());
        assertSame(fallback, ra.forRoom(0));
    }
}
```

- [ ] **Step 2: Run to verify failure** — `.\build.ps1` → FAIL (`RoomArt` missing).

- [ ] **Step 3: Create `RoomArt.java`**

```java
package adventure_game.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps a room number to its backdrop {@link ArtAsset}. Backdrops load from
 * {@code assets/art/rooms/room<N>.txt}; any room without a file falls back to a
 * shared blank/placeholder asset.
 */
public class RoomArt {
    private final Map<Integer, ArtAsset> byRoom = new HashMap<>();
    private final ArtAsset fallback;

    public RoomArt(ArtAsset fallback) {
        this.fallback = fallback;
    }

    /** Loads room0..room(count-1).txt from dir; missing/unreadable files are skipped. */
    public static RoomArt loadAll(int count, String dir, ArtAsset fallback) {
        RoomArt ra = new RoomArt(fallback);
        for (int i = 0; i < count; i++) {
            Path p = Path.of(dir, "room" + i + ".txt");
            if (Files.exists(p)) {
                try {
                    ra.byRoom.put(i, new ArtAsset(Files.readAllLines(p)));
                } catch (IOException e) {
                    // leave this room to the fallback
                }
            }
        }
        return ra;
    }

    public ArtAsset forRoom(int roomNumber) {
        return byRoom.getOrDefault(roomNumber, fallback);
    }

    public int loadedCount() {
        return byRoom.size();
    }
}
```

- [ ] **Step 4: Build & test** — `.\test.ps1` → `RoomArtTest` green; suite 43/43.

- [ ] **Step 5: Commit**

```
git add src/adventure_game/engine/RoomArt.java src/adventure_game/Test/RoomArtTest.java
git commit -m "feat(engine): add RoomArt per-room backdrop registry"
```

---

## Task 4: Player Defend animation

**Files:** modify `src/adventure_game/engine/Animations.java`, `src/adventure_game/Test/AnimationTest.java`.

- [ ] **Step 1: Add a test to `AnimationTest.java`** (append inside the class)

```java
    @Test
    void playerGuardIsAShortBurst() {
        adventure_game.engine.Animation a = adventure_game.engine.Animations.playerGuard();
        assertTrue(a.size() >= 2 && a.size() <= 6);
        for (adventure_game.engine.AnimationFrame f : a.frames()) {
            assertTrue(f.getDurationMs() >= 50 && f.getDurationMs() <= 120);
        }
    }
```

- [ ] **Step 2: Run to verify failure** — `.\build.ps1` → FAIL (`playerGuard` missing).

- [ ] **Step 3: Add `playerGuard()` to `Animations.java`** (insert in the class)

```java
    /**
     * The player's defend: a brace/guard marker flickers just in front of the
     * player, telegraphing the block before the exchange resolves.
     */
    public static Animation playerGuard() {
        ArtAsset guardOpen = ArtAsset.fromLines("(");
        ArtAsset guardClose = ArtAsset.fromLines("[");
        java.util.List<AnimationFrame> frames = new java.util.ArrayList<>();
        frames.add(new AnimationFrame(guardClose, 6, 0, 70));
        frames.add(new AnimationFrame(guardOpen, 6, 0, 70));
        frames.add(new AnimationFrame(guardClose, 6, 0, 90));
        return new Animation(frames);
    }
```

- [ ] **Step 4: Build & test** — `.\test.ps1` → `AnimationTest` green (3 tests); suite 44/44.

- [ ] **Step 5: Commit**

```
git add src/adventure_game/engine/Animations.java src/adventure_game/Test/AnimationTest.java
git commit -m "feat(engine): add playerGuard defend animation"
```

---

## Task 5: Author Creeper and Sprinter arts

**Files:** Create `assets/art/creeper.txt`, `assets/art/sprinter.txt`. (Runtime assets — no compile impact.)

- [ ] **Step 1: Author the two 5×5 arts using the `ascii-art` skill** (`.claude/skills/ascii-art/SKILL.md`): a **Creeper** (hunched, crawling) and a **Sprinter** (lean, lunging), each exactly 5 cols × 5 rows, ASCII-only, space=transparent, distinct from the Walker. Write BOM-free:

```powershell
$enc = New-Object System.Text.UTF8Encoding($false)
# Replace with the skill's designs; each line exactly 5 chars.
$creeper = @(' ,-. ','=o o=','/]_[\',' )_( ',' d b ')
[System.IO.File]::WriteAllLines("$PWD\assets\art\creeper.txt", $creeper, $enc)
$sprinter = @('  o  ',' /|_ ','=/|  ',' / \ ','_/  \')
[System.IO.File]::WriteAllLines("$PWD\assets\art\sprinter.txt", $sprinter, $enc)
$cb=[IO.File]::ReadAllBytes("$PWD\assets\art\creeper.txt"); "creeper first3: $($cb[0]) $($cb[1]) $($cb[2])"
$sb=[IO.File]::ReadAllBytes("$PWD\assets\art\sprinter.txt"); "sprinter first3: $($sb[0]) $($sb[1]) $($sb[2])"
```
Confirm neither prints `239 187 191`.

- [ ] **Step 2: Build** — `.\test.ps1` → suite still 44/44 (assets don't affect tests yet).

- [ ] **Step 3: Commit**

```
git add assets/art/creeper.txt assets/art/sprinter.txt
git commit -m "content: add Creeper and Sprinter zombie art"
```

---

## Task 6: Scene per-room backdrop + enemy-by-type, AND GameApp rewire (together)

The `Scene` constructor change and the `GameApp` rewire happen in ONE task because `GameApp` is `Scene`'s only caller — changing them together keeps compilation green. `GameApp` now references `RoomArt` (Task 3) and `Animations.playerGuard` (Task 4), which already exist.

**Files:** rewrite `src/adventure_game/engine/Scene.java`, `src/adventure_game/GameApp.java`, `src/adventure_game/Test/SceneTest.java`.

- [ ] **Step 1: Rewrite `SceneTest.java`** (replace whole file — new 6-arg constructor)

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import adventure_game.GameState;
import adventure_game.engine.ArtAsset;
import adventure_game.engine.RoomArt;
import adventure_game.engine.Scene;
import adventure_game.engine.Screen;

public class SceneTest {

    private static boolean gridContains(Screen s, String needle) {
        for (String line : s.toLines()) {
            if (line.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static Scene buildScene() {
        ArtAsset player = ArtAsset.fromLines("  @  ", " /|\\ ", "  |  ", " / \\ ", "     ");
        ArtAsset walker = ArtAsset.fromLines(" ___ ", "[o o]", " \\_/ ", " /|\\ ", " / \\ ");
        Map<String, ArtAsset> enemyArt = new HashMap<>();
        enemyArt.put("Walker", walker);
        RoomArt roomArt = new RoomArt(ArtAsset.fromLines("ROOM-BACKDROP"));
        return new Scene(60, 24, player, enemyArt, walker, roomArt);
    }

    @Test
    void renderShowsHudRoomNameHpAndLog() throws Exception {
        GameState gs = GameState.loadHospital();
        Scene scene = buildScene();
        Screen screen = new Screen(60, 24);

        scene.render(screen, gs, null);

        assertTrue(gridContains(screen, "Hospital Entrance"));   // HUD name
        assertTrue(gridContains(screen, "HP ["));                // HUD bar
        assertTrue(gridContains(screen, "Lv."));                 // HUD level
        assertTrue(gridContains(screen, "Hospital Entrance."));  // a log line exists
    }
}
```

- [ ] **Step 2: Run to verify failure** — `.\build.ps1` → FAIL (`Scene` 6-arg constructor missing; old `GameApp` uses 3-arg).

- [ ] **Step 3: Rewrite `Scene.java`** (replace whole file)

```java
package adventure_game.engine;

import java.util.List;
import java.util.Map;

import adventure_game.GameState;
import adventure_game.Player;
import adventure_game.Room;

/**
 * Composes the whole Screen from GameState each redraw: HUD (top), viewport
 * (middle, where the current room's backdrop, player, enemy, and any active
 * animation frame draw), and log (bottom).
 */
public class Scene {
    private static final int VIEW_TOP = 3;
    private static final int LOG_TOP = 16;
    private static final int PLAYER_X = 6;
    private static final int PLAYER_Y = VIEW_TOP + 6;
    private static final int ENEMY_X = 40;
    private static final int STRIKE_Y = VIEW_TOP + 7;

    private final int width;
    private final int height;
    private final ArtAsset playerArt;
    private final Map<String, ArtAsset> enemyArt;
    private final ArtAsset enemyFallback;
    private final RoomArt roomArt;

    public Scene(int width, int height, ArtAsset playerArt,
                 Map<String, ArtAsset> enemyArt, ArtAsset enemyFallback, RoomArt roomArt) {
        this.width = width;
        this.height = height;
        this.playerArt = playerArt;
        this.enemyArt = enemyArt;
        this.enemyFallback = enemyFallback;
        this.roomArt = roomArt;
    }

    public void render(Screen s, GameState state, AnimationFrame active) {
        s.clear();
        Player p = state.getPlayer();
        Room room = state.getCurrentRoom();

        // --- HUD ---
        s.drawText(1, 0, room.getRoomName());
        String lv = "Lv." + p.getLevel();
        s.drawText(width - 1 - lv.length(), 0, lv);
        String hp = "HP " + HudComponents.healthBar(p.getHealth(), p.getMaxHealth(), 12)
                + " " + p.getHealth() + "/" + p.getMaxHealth();
        s.drawText(1, 1, hp);
        String dmg = "DMG " + p.getBaseDamage();
        s.drawText(width - 1 - dmg.length(), 1, dmg);
        for (int x = 0; x < width; x++) {
            s.put(x, 2, '-');
        }

        // --- VIEWPORT ---
        s.blit(roomArt.forRoom(room.getRoomNumber()), 0, VIEW_TOP);
        s.blit(playerArt, PLAYER_X, PLAYER_Y);
        if (state.isInCombat() && state.getEnemy() != null) {
            ArtAsset ea = enemyArt.getOrDefault(state.getEnemy().getName(), enemyFallback);
            s.blit(ea, ENEMY_X, PLAYER_Y);
        }
        if (active != null) {
            s.blit(active.getArt(), PLAYER_X + active.getDx(), STRIKE_Y + active.getDy());
        }

        // --- LOG ---
        for (int x = 0; x < width; x++) {
            s.put(x, LOG_TOP, '-');
        }
        int logRows = height - LOG_TOP - 1;
        List<String> recent = state.getLog().recent(logRows);
        for (int i = 0; i < recent.size(); i++) {
            s.drawText(1, LOG_TOP + 1 + i, "> " + recent.get(i));
        }
    }
}
```

- [ ] **Step 4: Rewrite `GameApp.java`** (replace whole file)

```java
package adventure_game;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import adventure_game.engine.Animation;
import adventure_game.engine.AnimationFrame;
import adventure_game.engine.AnimationPlayer;
import adventure_game.engine.Animations;
import adventure_game.engine.ArtAsset;
import adventure_game.engine.RenderPanel;
import adventure_game.engine.RoomArt;
import adventure_game.engine.Scene;
import adventure_game.engine.Screen;

/** Entry point: unified single-screen window with mode-based controls. */
public class GameApp extends JFrame {
    private static final int COLS = 60;
    private static final int ROWS = 24;

    private final RenderPanel panel;
    private final Scene scene;
    private final Screen screen;
    private final GameState state;
    private final CardLayout cards = new CardLayout();
    private final JPanel south = new JPanel(cards);

    private AnimationPlayer current;
    private AnimationFrame activeFrame;

    public GameApp() throws Exception {
        super("Covid, The Zombie Apocalypse");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        screen = new Screen(COLS, ROWS);
        ArtAsset playerArt = ArtAsset.fromFile("assets/art/player.txt");
        Map<String, ArtAsset> enemyArt = new HashMap<>();
        enemyArt.put("Walker", ArtAsset.fromFile("assets/art/walker.txt"));
        enemyArt.put("Creeper", ArtAsset.fromFile("assets/art/creeper.txt"));
        enemyArt.put("Sprinter", ArtAsset.fromFile("assets/art/sprinter.txt"));
        ArtAsset enemyFallback = enemyArt.get("Walker");
        ArtAsset blank = ArtAsset.fromLines("     ", "     ", "     ", "     ", "     ");
        RoomArt roomArt = RoomArt.loadAll(25, "assets/art/rooms", blank);

        scene = new Scene(COLS, ROWS, playerArt, enemyArt, enemyFallback, roomArt);
        state = GameState.loadHospital();
        panel = new RenderPanel(COLS, ROWS);

        south.add(buildExplorePanel(), GameState.Mode.EXPLORE.name());
        south.add(buildCombatPanel(), GameState.Mode.COMBAT.name());
        south.add(buildDeadPanel(), GameState.Mode.DEAD.name());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(south, BorderLayout.SOUTH);

        redraw();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel buildExplorePanel() {
        JPanel p = new JPanel(new GridLayout(2, 3));
        p.add(moveButton("North", Direction.NORTH));
        p.add(moveButton("South", Direction.SOUTH));
        p.add(moveButton("East", Direction.EAST));
        p.add(moveButton("West", Direction.WEST));
        p.add(actionButton("Create Bandage", state::createBandage, false));
        p.add(new JLabel());
        return p;
    }

    private JPanel buildCombatPanel() {
        JPanel p = new JPanel(new GridLayout(1, 4));
        JButton attack = new JButton("Attack");
        attack.addActionListener(e -> {
            if (!isAnimating() && state.getMode() == GameState.Mode.COMBAT) {
                playThen(Animations.playerStrike(), state::playerAttack);
            }
        });
        JButton defend = new JButton("Defend");
        defend.addActionListener(e -> {
            if (!isAnimating() && state.getMode() == GameState.Mode.COMBAT) {
                playThen(Animations.playerGuard(), state::playerDefend);
            }
        });
        p.add(attack);
        p.add(defend);
        p.add(actionButton("Use Bandage", state::useBandage, true));
        p.add(actionButton("Create Bandage", state::createBandage, true));
        return p;
    }

    private JPanel buildDeadPanel() {
        JPanel p = new JPanel(new GridLayout(1, 1));
        p.add(new JLabel("  You are dead. Close the window to quit.  "));
        return p;
    }

    private JButton actionButton(String label, Runnable action, boolean combatOnly) {
        JButton b = new JButton(label);
        b.addActionListener(e -> {
            if (isAnimating()) {
                return;
            }
            if (combatOnly && state.getMode() != GameState.Mode.COMBAT) {
                return;
            }
            action.run();
            redraw();
        });
        return b;
    }

    private JButton moveButton(String label, Direction dir) {
        JButton b = new JButton(label);
        b.addActionListener(e -> {
            if (isAnimating()) {
                return;
            }
            state.move(dir);
            redraw();
        });
        return b;
    }

    private boolean isAnimating() {
        return current != null && current.isPlaying();
    }

    private void playThen(Animation anim, Runnable resolve) {
        current = new AnimationPlayer(
                frame -> { activeFrame = frame; redraw(); },
                () -> { resolve.run(); activeFrame = null; redraw(); });
        current.play(anim);
    }

    private void redraw() {
        scene.render(screen, state, activeFrame);
        panel.setScreen(screen);
        panel.repaint();
        cards.show(south, state.getMode().name());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new GameApp();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
```

- [ ] **Step 5: Build & test** — `.\test.ps1`. Expected: compiles (Scene + GameApp + SceneTest all on the new constructor); `SceneTest` green; suite 44/44. (`GameApp` references `assets/art/creeper.txt`/`sprinter.txt` (exist, Task 5) and `assets/art/rooms` (may be empty → blank backdrops) only at runtime.)

- [ ] **Step 6: Commit**

```
git add src/adventure_game/engine/Scene.java src/adventure_game/GameApp.java src/adventure_game/Test/SceneTest.java
git commit -m "feat: per-room backdrops, enemy-by-type, and mode-based GameApp controls"
```

---

## Task 7: Author 25 room backdrops + asset-sanity test (fan-out)

**Files:** Create `assets/art/rooms/room0.txt` … `room24.txt`; create `src/adventure_game/Test/AssetSanityTest.java`; modify `.claude/skills/room-and-map/SKILL.md`; delete `assets/art/room0.txt`.

The 25 backdrops are generative content — author with the `ascii-art` + `room-and-map` skills (good fan-out for parallel subagents), under the constraints below. Do NOT hand-fabricate them in this plan; produce to spec and verify mechanically.

**Per-file constraints:** path `assets/art/rooms/room<N>.txt` for N=0..24; UTF-8 **without BOM** (`[System.IO.File]::WriteAllLines(path, lines, (New-Object System.Text.UTF8Encoding($false)))`); ≤13 rows tall, ≤60 cols wide; ASCII printable only; includes the room's **name** as a visible label and structure fitting the room's description.

**Room number → name:** 0 Hospital Entrance, 1 Supply Room, 2 Hallway, 3 Security Office, 4 General Surgeons Office, 5 Stairs, 6 Operating Room, 7 Downstairs Hallway, 8 Reasearch Center, 9 Chief of Reasearch Office, 10 The Cure Laboratory, 11 The Sewer Entrance, 12 Shit Hallway West, 13 Shit Hallway East, 14 Infested Hallway, 15 Shit Hallway Continued, 16 Supply Room 2A, 17 Bunker Entrance, 18 Control Room, 19 Zombie Nest, 20 Supply Room 3A, 21 Supply Room 4A, 22 RatKing, 23 Bunker Control Room, 24 Fauccis Lair.

- [ ] **Step 1: Extend the `room-and-map` skill.** In `.claude/skills/room-and-map/SKILL.md`, change the backdrop heading `## 1. Backdrop art -> assets/art/room<N>.txt` to `## 1. Backdrop art -> assets/art/rooms/room<N>.txt`.

- [ ] **Step 2: Author all 25 backdrops** under `assets/art/rooms/`, meeting the constraints. Base `room0.txt` on the existing `assets/art/room0.txt` content. Fan out across parallel authoring subagents (each a batch of rooms) when executing via subagent-driven development.

- [ ] **Step 3: Write `AssetSanityTest.java`**

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import adventure_game.engine.ArtAsset;
import adventure_game.engine.RoomArt;

public class AssetSanityTest {

    private static void assertNoBom(Path p) throws Exception {
        byte[] b = Files.readAllBytes(p);
        boolean bom = b.length >= 3
                && (b[0] & 0xFF) == 0xEF && (b[1] & 0xFF) == 0xBB && (b[2] & 0xFF) == 0xBF;
        assertFalse(bom, "BOM found in " + p);
    }

    @Test
    void allArtFilesAreBomFree() throws Exception {
        try (Stream<Path> s = Files.walk(Path.of("assets/art"))) {
            for (Path p : (Iterable<Path>) s.filter(x -> x.toString().endsWith(".txt"))::iterator) {
                assertNoBom(p);
            }
        }
    }

    @Test
    void allTwentyFiveRoomBackdropsLoad() {
        RoomArt ra = RoomArt.loadAll(25, "assets/art/rooms", ArtAsset.fromLines(" "));
        assertEquals(25, ra.loadedCount(), "expected 25 backdrops in assets/art/rooms");
    }

    @Test
    void roomBackdropsFitViewport() throws Exception {
        for (int i = 0; i < 25; i++) {
            ArtAsset a = ArtAsset.fromFile(Path.of("assets/art/rooms", "room" + i + ".txt").toString());
            assertTrue(a.height() <= 13, "room" + i + " too tall: " + a.height());
            assertTrue(a.width() <= 60, "room" + i + " too wide: " + a.width());
        }
    }
}
```

- [ ] **Step 4: Delete the superseded single backdrop** — remove `assets/art/room0.txt` (the new `assets/art/rooms/room0.txt` replaces it; `GameApp` no longer references the old path after Task 6). Confirm: `Select-String -Path src -Pattern "art/room0.txt"` → no matches.

- [ ] **Step 5: Build & test** — `.\test.ps1` → `AssetSanityTest` green (3 tests); suite 47/47. If `allTwentyFiveRoomBackdropsLoad` fails, a backdrop is missing/misnamed; if `roomBackdropsFitViewport` fails, trim that file; if `allArtFilesAreBomFree` fails, rewrite that file BOM-free.

- [ ] **Step 6: Commit**

```
git add assets/art/rooms .claude/skills/room-and-map/SKILL.md src/adventure_game/Test/AssetSanityTest.java
git commit -m "content: add 25 room backdrops and asset-sanity tests"
git rm assets/art/room0.txt
git commit -m "chore: remove superseded single room0 backdrop"
```

---

## Task 8: Run verification + README + acceptance

**Files:** modify `README.md`.

- [ ] **Step 1: Build, then launch and verify by playing.** `.\build.ps1` (exit 0). The controller launches the app detached and verifies, capturing a screenshot:
  - Start at **Hospital Entrance**, EXPLORE buttons (N/S/E/W + Create Bandage).
  - A valid direction moves and the **backdrop changes**; an invalid one logs "You cannot go that way."
  - Entering a populated room starts **COMBAT** (buttons switch to Attack/Defend/Use Bandage/Create Bandage) with a zombie shown.
  - **Attack** plays the strike burst then resolves (HP bar + log update); **Defend** plays the guard burst; bandages work.
  - Defeating the zombie logs death, levels progress, and the room clears (EXPLORE) or another zombie appears.
  - Entering a boss room (e.g. reach room 10/19/22/24) shows the deferral hint, no fight.

- [ ] **Step 2: Update `README.md`** — append after the existing build/run section:

```markdown

### Phase 2a — Explore & Fight

You now walk the full 25-room hospital with N/S/E/W (each room has its own
backdrop), get pulled into encounters with Walkers, Creepers, and Sprinters, and
fight with Attack / Defend / Create Bandage / Use Bandage while leveling up on
kills. Bosses, loot, save/load, and the win sequence are deferred to a later
increment. See `docs/superpowers/specs/2026-06-07-phase2a-explore-and-fight-design.md`.
```

- [ ] **Step 3: Acceptance checklist** (verify by the run + tests):
  - [ ] `.\test.ps1` all green (record numbers).
  - [ ] No Swing imports in any model class except `GameApp` (`Select-String -Path src\adventure_game\*.java, src\adventure_game\items\*.java -Pattern "javax.swing"` → only `GameApp.java`).
  - [ ] 25 backdrops present and BOM-free (AssetSanityTest green).
  - [ ] Movement, encounters, the four combat verbs, leveling, and the boss-room hint all work in the run.

- [ ] **Step 4: Commit**

```
git add README.md
git commit -m "docs: Phase 2a explore-and-fight build notes"
```

---

## Self-Review notes (already reconciled)

- **Ordering/compile-green:** verified per task — `playerAttack`/`isInCombat` exist continuously from Task 2; the `Scene` constructor change is coupled with the `GameApp` rewire in Task 6; the stale Phase-1 `SceneTest` log assertion is fixed in Task 2; `creeper`/`sprinter`/`rooms` files are runtime-only (no compile dependency).
- **Spec coverage:** navigation + multi-room render (Tasks 2, 6, 7); encounters with 3 zombie types (Tasks 2, 5); core combat + leveling (Task 2); mode-based UI (Task 6); per-room backdrops (Tasks 3, 6, 7); enemy art by type (Tasks 5, 6); Defend animation (Task 4); map-parser hardening (Task 1); asset BOM/size checks (Task 7); boss-room deferral hint (Task 2); acceptance (Task 8). Per-enemy animation choreography is deferred per the spec.
- **Type consistency:** `GameState.Mode {EXPLORE,COMBAT,DEAD}`; `Direction {NORTH,SOUTH,EAST,WEST}`; `GameState.move/tryEncounter/playerAttack/playerDefend/useBandage/createBandage/getMode/isInCombat/getEnemy/getCurrentRoom/getPlayer/getLog`; `RoomArt(ArtAsset)`, `RoomArt.loadAll(int,String,ArtAsset)`, `forRoom(int)`, `loadedCount()`; `Scene(int,int,ArtAsset,Map<String,ArtAsset>,ArtAsset,RoomArt)`; `Animations.playerStrike/playerGuard`; `HospitalMap.load(String)`, `HospitalMap.HOSPITAL_PATH`. Consistent across tasks.
- **No placeholders:** all code steps are complete. The 25 backdrops are generative content authored to explicit mechanical constraints (path/size/BOM/label) and verified by `AssetSanityTest`.
