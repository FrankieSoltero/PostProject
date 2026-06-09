# Phase 2b-3 — Save / Load Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Save the full run to a named slot and load it back, on the Swing UI.

**Architecture:** A Swing-free `SaveData` DTO captures the run (player fields, current room, all 25 rooms' flags). `GameState.toSaveData()`/`fromSave()` snapshot and rebuild state (rebuild a fresh map via `HospitalMap.load`, re-apply saved flags, install a restored player). `SaveGame` does plain-text `key:value` file I/O and slot listing. `Player.fromSave` + a small `Character` restore hook round-trip the player exactly. `GameApp` adds Save/Load buttons with `JOptionPane` dialogs and swaps the live `GameState` on load.

**Tech Stack:** Java (no build tool; `build.ps1` compiles all `src` to `out`), JUnit 5 (console jar; `@TempDir` for file tests), PowerShell scripts.

**Spec:** `docs/superpowers/specs/2026-06-09-phase2b3-save-load-design.md`

---

## Conventions for every task

- **Full suite:** `.\test.ps1`. **One class:** `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.<ClassName> --disable-banner`
- **RED for a new test class:** `build.ps1` compiles tests with `src`, so a test referencing a not-yet-created symbol fails the build (compile error) — that IS the RED signal.
- Model classes (`SaveData`, `SaveGame`, `GameState`, `Player`, `Character`) stay **Swing-free** (no `javax.swing`/`java.awt`). Only `GameApp` imports Swing.
- Tests are in `src/adventure_game/Test/`, package `adventure_game.Test` (public API only).
- **Committing:** GPG-signed; commit via the **PowerShell** tool only (never Bash). Warm agent signs silently; ONLY on `gpg failed to sign ... socket file removed - retrying binding` run `gpgconf --kill gpg-agent; gpgconf --launch gpg-agent` and retry once. Trailer: `Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>` (PowerShell single-quoted here-string, closing `'@` at column 0).

---

## File Structure

**New production files:**
- `src/adventure_game/SaveData.java` — immutable DTO of the run snapshot.
- `src/adventure_game/SaveGame.java` — plain-text file I/O + slot listing (Swing-free).

**New test files:**
- `src/adventure_game/Test/PlayerSaveTest.java`
- `src/adventure_game/Test/SaveLoadTest.java` (GameState in-memory round-trip)
- `src/adventure_game/Test/SaveGameTest.java` (file I/O, `@TempDir`)

**Modified files:**
- `src/adventure_game/Character.java` — add `restoreVitals(...)` (protected).
- `src/adventure_game/Player.java` — add `restoreWeapon(...)` and static `fromSave(...)`; import `bandage`.
- `src/adventure_game/GameState.java` — add `toSaveData()` and static `fromSave(SaveData)`.
- `src/adventure_game/GameApp.java` — non-final `state`; swap-safe button actions; Save/Load buttons + dialogs.

---

## Task 1: Player round-trip — `Character.restoreVitals` + `Player.fromSave`

**Files:**
- Modify: `src/adventure_game/Character.java`, `src/adventure_game/Player.java`
- Test: `src/adventure_game/Test/PlayerSaveTest.java`

- [ ] **Step 1: Write the failing test** — create `src/adventure_game/Test/PlayerSaveTest.java`:

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.MessageLog;
import adventure_game.Player;

public class PlayerSaveTest {

    @Test
    void fromSaveReproducesAllPlayerState() {
        Player p = Player.fromSave("Mick", 80, 200, 7, 2, 130, 45, "AssaultRifle", 3,
                new MessageLog());
        assertEquals("Mick", p.getName());
        assertEquals(80, p.getHealth());
        assertEquals(200, p.getMaxHealth());
        assertEquals(7, p.getLevel());
        assertEquals(2, p.getlevelUpXp());
        assertEquals(130, p.getBaseDamage());
        assertEquals(45, p.getWeaponBonus());
        assertEquals("AssaultRifle", p.getWeaponName());
        assertEquals(175, p.getEffectiveDamage()); // 130 + 45
        assertEquals(3, p.bandageCount());
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `.\build.ps1`
Expected: FAIL — `cannot find symbol: method fromSave`.

- [ ] **Step 3: Add `restoreVitals` to `Character`**

In `src/adventure_game/Character.java`, add this method (anywhere in the class body, e.g. after `modifyDamage`). It can set the private `health`/`levelUpXp` and protected `maxHealth`/`level`/`baseDamage` because it lives in `Character`:

```java
    /**
     * Restore persisted vitals exactly (used only by save/load). Sets the fields
     * directly, bypassing the level-up math.
     */
    protected void restoreVitals(int maxHealth, int health, int level, int levelUpXp, int baseDamage) {
        this.maxHealth = maxHealth;
        this.health = health;
        this.level = level;
        this.levelUpXp = levelUpXp;
        this.baseDamage = baseDamage;
    }
```

- [ ] **Step 4: Add `restoreWeapon` + `fromSave` to `Player`**

In `src/adventure_game/Player.java`:

(a) Add the import near the top (below `package adventure_game;`):
```java
import adventure_game.items.bandage;
```

(b) Add these members (e.g. after `getWeaponBonus()`):
```java
    /** Restore the equipped weapon fields directly (used only by save/load). */
    void restoreWeapon(int bonus, String name) {
        this.weaponBonus = bonus;
        this.weaponName = name;
    }

    /** Rebuild a Player from persisted save fields. */
    public static Player fromSave(String name, int health, int maxHealth, int level,
            int levelUpXp, int baseDamage, int weaponBonus, String weaponName,
            int bandages, MessageLog sink) {
        Player p = new Player(name, maxHealth, level, baseDamage);
        p.restoreVitals(maxHealth, health, level, levelUpXp, baseDamage);
        p.restoreWeapon(weaponBonus, weaponName);
        for (int i = 0; i < bandages; i++) {
            p.obtain(new bandage(), sink);
        }
        return p;
    }
```

- [ ] **Step 5: Run to verify it passes**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.PlayerSaveTest --disable-banner`
Expected: PASS.

- [ ] **Step 6: Run full suite**

Run: `.\test.ps1`
Expected: all green (existing + 1 new).

- [ ] **Step 7: Commit**

```
git add src/adventure_game/Character.java src/adventure_game/Player.java src/adventure_game/Test/PlayerSaveTest.java
git commit -m "<feat: Player.fromSave + Character.restoreVitals for save/load + trailer>"
```
Subject: `feat: Player.fromSave + Character.restoreVitals round-trip`

---

## Task 2: `SaveData` DTO + `GameState.toSaveData`/`fromSave`

**Files:**
- Create: `src/adventure_game/SaveData.java`
- Modify: `src/adventure_game/GameState.java`
- Test: `src/adventure_game/Test/SaveLoadTest.java`

- [ ] **Step 1: Write the failing test** — create `src/adventure_game/Test/SaveLoadTest.java`:

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.GameState;
import adventure_game.SaveData;
import adventure_game.Weapon;

public class SaveLoadTest {

    @Test
    void toSaveDataThenFromSaveRoundTripsPlayerAndRooms() throws Exception {
        GameState gs = GameState.loadHospital();
        gs.getPlayer().equipWeapon(new Weapon("AssaultRifle", 50), gs.getLog());
        gs.getRoom(2).removeNPC(); // simulate a cleared room

        SaveData snap = gs.toSaveData();
        GameState restored = GameState.fromSave(snap);
        SaveData snap2 = restored.toSaveData();

        assertEquals(snap.weaponName, snap2.weaponName);
        assertEquals(snap.weaponBonus, snap2.weaponBonus);
        assertEquals(snap.level, snap2.level);
        assertEquals(snap.currentRoom, snap2.currentRoom);
        // Restored room state:
        assertEquals(-1, restored.getRoom(2).hasNPC(), "cleared room stays cleared");
        assertEquals(4, restored.getRoom(10).hasNPC(), "boss room stays a boss");
        assertTrue(restored.getRoom(24).hasCure(), "cure persists in room 24");
        assertEquals(GameState.Mode.EXPLORE, restored.getMode());
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `.\build.ps1`
Expected: FAIL — `cannot find symbol: class SaveData` (and `toSaveData`/`fromSave`).

- [ ] **Step 3: Create `SaveData`** — `src/adventure_game/SaveData.java`:

```java
package adventure_game;

/**
 * Immutable snapshot of a run for save/load: the player fields, the current room
 * number, and every room's flags. Swing-free DTO; the boundary type between
 * GameState and SaveGame.
 */
public final class SaveData {
    public final String playerName;
    public final int health;
    public final int maxHealth;
    public final int level;
    public final int levelUpXp;
    public final int baseDamage;
    public final int weaponBonus;
    public final String weaponName;
    public final int bandages;
    public final int currentRoom;
    /** [roomNumber][0..3] = hasNPC, hasWeapon, hasItem, hasCure (raw flag ints). */
    public final int[][] roomFlags;

    public SaveData(String playerName, int health, int maxHealth, int level, int levelUpXp,
            int baseDamage, int weaponBonus, String weaponName, int bandages,
            int currentRoom, int[][] roomFlags) {
        this.playerName = playerName;
        this.health = health;
        this.maxHealth = maxHealth;
        this.level = level;
        this.levelUpXp = levelUpXp;
        this.baseDamage = baseDamage;
        this.weaponBonus = weaponBonus;
        this.weaponName = weaponName;
        this.bandages = bandages;
        this.currentRoom = currentRoom;
        this.roomFlags = roomFlags;
    }
}
```

- [ ] **Step 4: Add `toSaveData` and `fromSave` to `GameState`**

In `src/adventure_game/GameState.java`, add these methods (e.g. just above the accessors block). They live in `GameState`, so they can touch its private fields and use the existing `Room`/`Player` API:

```java
    /** Snapshot the current run into a SaveData. */
    public SaveData toSaveData() {
        int[][] flags = new int[rooms.size()][4];
        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            flags[i][0] = r.hasNPC();
            flags[i][1] = r.hasWeapon();
            flags[i][2] = r.hasItem();
            flags[i][3] = r.hasCure() ? 1 : -1;
        }
        return new SaveData(player.getName(), player.getHealth(), player.getMaxHealth(),
                player.getLevel(), player.getlevelUpXp(), player.getBaseDamage(),
                player.getWeaponBonus(), player.getWeaponName(), player.bandageCount(),
                currentRoom.getRoomNumber(), flags);
    }

    /** Rebuild a fresh map and re-apply a saved run state. */
    public static GameState fromSave(SaveData s) throws FileNotFoundException {
        GameState gs = new GameState();
        gs.rooms = HospitalMap.load(HospitalMap.HOSPITAL_PATH);
        for (int i = 0; i < gs.rooms.size(); i++) {
            Room r = gs.rooms.get(i);
            int[] f = s.roomFlags[i];
            if (f[0] == 1) { r.setNPC(); } else if (f[0] == 4) { r.setBossNPC(); } else { r.removeNPC(); }
            if (f[1] == 1) { r.setWeapon(); } else { r.removeWeapon(); }
            if (f[2] == 1) { r.setItem(); } else { r.removeItem(); }
            if (f[3] == 1) { r.setRoomCure(); }
            // a fresh map has hasCure == -1, so no removeCure is needed
        }
        gs.player = Player.fromSave(s.playerName, s.health, s.maxHealth, s.level,
                s.levelUpXp, s.baseDamage, s.weaponBonus, s.weaponName, s.bandages,
                new MessageLog());
        gs.currentRoom = gs.rooms.get(s.currentRoom);
        gs.enemy = null;
        gs.mode = Mode.EXPLORE;
        return gs;
    }
```

(`FileNotFoundException` is already imported in `GameState.java`.)

- [ ] **Step 5: Run to verify it passes**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.SaveLoadTest --disable-banner`
Expected: PASS.

- [ ] **Step 6: Run full suite**

Run: `.\test.ps1`
Expected: all green.

- [ ] **Step 7: Commit**

```
git add src/adventure_game/SaveData.java src/adventure_game/GameState.java src/adventure_game/Test/SaveLoadTest.java
git commit -m "<feat: SaveData DTO + GameState snapshot/restore + trailer>"
```
Subject: `feat: SaveData DTO and GameState snapshot/restore`

---

## Task 3: `SaveGame` — plain-text file I/O + slot listing

**Files:**
- Create: `src/adventure_game/SaveGame.java`
- Test: `src/adventure_game/Test/SaveGameTest.java`

- [ ] **Step 1: Write the failing test** — create `src/adventure_game/Test/SaveGameTest.java`:

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import adventure_game.GameState;
import adventure_game.SaveGame;
import adventure_game.Weapon;

public class SaveGameTest {

    @Test
    void saveThenLoadFromFileRoundTrips(@TempDir Path dir) throws Exception {
        GameState gs = GameState.loadHospital();
        gs.getPlayer().equipWeapon(new Weapon("Pistol", 30), gs.getLog());
        gs.getRoom(2).removeNPC();

        Path f = SaveGame.slotFile(dir, "run1");
        SaveGame.save(gs, f);
        assertTrue(Files.exists(f));

        GameState loaded = SaveGame.load(f);
        assertEquals("Pistol", loaded.getPlayer().getWeaponName());
        assertEquals(30, loaded.getPlayer().getWeaponBonus());
        assertEquals(-1, loaded.getRoom(2).hasNPC());
        assertEquals(4, loaded.getRoom(10).hasNPC());
        assertTrue(loaded.getRoom(24).hasCure());
    }

    @Test
    void listSlotsReturnsSortedSavedNames(@TempDir Path dir) throws Exception {
        GameState gs = GameState.loadHospital();
        SaveGame.save(gs, SaveGame.slotFile(dir, "beta"));
        SaveGame.save(gs, SaveGame.slotFile(dir, "alpha"));
        assertEquals(List.of("alpha", "beta"), SaveGame.listSlots(dir));
    }

    @Test
    void listSlotsEmptyForMissingDir(@TempDir Path dir) throws Exception {
        assertTrue(SaveGame.listSlots(dir.resolve("does-not-exist")).isEmpty());
    }

    @Test
    void loadMalformedFileThrows(@TempDir Path dir) throws Exception {
        Path f = dir.resolve("bad.txt");
        Files.write(f, List.of("not a real save"));
        assertThrows(Exception.class, () -> SaveGame.load(f));
    }

    @Test
    void saveIsBomFree(@TempDir Path dir) throws Exception {
        GameState gs = GameState.loadHospital();
        Path f = SaveGame.slotFile(dir, "run");
        SaveGame.save(gs, f);
        byte[] b = Files.readAllBytes(f);
        boolean bom = b.length >= 3 && (b[0] & 0xFF) == 0xEF && (b[1] & 0xFF) == 0xBB && (b[2] & 0xFF) == 0xBF;
        assertFalse(bom);
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `.\build.ps1`
Expected: FAIL — `cannot find symbol: class SaveGame`.

- [ ] **Step 3: Create `SaveGame`** — `src/adventure_game/SaveGame.java`:

```java
package adventure_game;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Plain-text save-slot I/O (Swing-free). Format (UTF-8, no BOM):
 *   player:&lt;name&gt;:&lt;health&gt;:&lt;maxHealth&gt;:&lt;level&gt;:&lt;levelUpXp&gt;:&lt;baseDamage&gt;:&lt;weaponBonus&gt;:&lt;weaponName&gt;:&lt;bandages&gt;
 *   room:&lt;currentRoom&gt;
 *   flags:&lt;npc,weapon,item,cure&gt;;... (one record per room, index == room number)
 */
public final class SaveGame {

    private SaveGame() {}

    public static Path slotFile(Path dir, String slot) {
        return dir.resolve(slot + ".txt");
    }

    public static List<String> listSlots(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            return new ArrayList<>();
        }
        try (Stream<Path> s = Files.list(dir)) {
            return s.map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(".txt"))
                    .map(n -> n.substring(0, n.length() - 4))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    public static void save(GameState state, Path file) throws IOException {
        SaveData s = state.toSaveData();
        List<String> lines = new ArrayList<>();
        lines.add(String.join(":", "player", s.playerName,
                Integer.toString(s.health), Integer.toString(s.maxHealth),
                Integer.toString(s.level), Integer.toString(s.levelUpXp),
                Integer.toString(s.baseDamage), Integer.toString(s.weaponBonus),
                s.weaponName, Integer.toString(s.bandages)));
        lines.add("room:" + s.currentRoom);
        StringBuilder fb = new StringBuilder("flags:");
        for (int i = 0; i < s.roomFlags.length; i++) {
            int[] f = s.roomFlags[i];
            if (i > 0) {
                fb.append(";");
            }
            fb.append(f[0]).append(",").append(f[1]).append(",").append(f[2]).append(",").append(f[3]);
        }
        lines.add(fb.toString());
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    public static GameState load(Path file) throws IOException {
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            String playerLine = null, roomLine = null, flagsLine = null;
            for (String ln : lines) {
                if (ln.startsWith("player:")) playerLine = ln;
                else if (ln.startsWith("room:")) roomLine = ln;
                else if (ln.startsWith("flags:")) flagsLine = ln;
            }
            if (playerLine == null || roomLine == null || flagsLine == null) {
                throw new IllegalArgumentException("missing save lines");
            }
            String[] pf = playerLine.split(":");
            if (pf.length != 10) {
                throw new IllegalArgumentException("bad player line");
            }
            String name = pf[1];
            int health = Integer.parseInt(pf[2]);
            int maxHealth = Integer.parseInt(pf[3]);
            int level = Integer.parseInt(pf[4]);
            int levelUpXp = Integer.parseInt(pf[5]);
            int baseDamage = Integer.parseInt(pf[6]);
            int weaponBonus = Integer.parseInt(pf[7]);
            String weaponName = pf[8];
            int bandages = Integer.parseInt(pf[9]);
            int currentRoom = Integer.parseInt(roomLine.substring("room:".length()).trim());

            String[] recs = flagsLine.substring("flags:".length()).split(";");
            int[][] flags = new int[recs.length][4];
            for (int i = 0; i < recs.length; i++) {
                String[] parts = recs[i].split(",");
                for (int j = 0; j < 4; j++) {
                    flags[i][j] = Integer.parseInt(parts[j].trim());
                }
            }
            SaveData sd = new SaveData(name, health, maxHealth, level, levelUpXp,
                    baseDamage, weaponBonus, weaponName, bandages, currentRoom, flags);
            return GameState.fromSave(sd);
        } catch (RuntimeException ex) {
            throw new IOException("malformed save file: " + file, ex);
        }
    }
}
```

- [ ] **Step 4: Run to verify it passes**

Run: `.\build.ps1; java -jar lib\junit-platform-console-standalone-1.9.2.jar -cp out --select-class adventure_game.Test.SaveGameTest --disable-banner`
Expected: PASS (5 tests).

- [ ] **Step 5: Run full suite**

Run: `.\test.ps1`
Expected: all green.

- [ ] **Step 6: Commit**

```
git add src/adventure_game/SaveGame.java src/adventure_game/Test/SaveGameTest.java
git commit -m "<feat: SaveGame plain-text slot I/O and listing + trailer>"
```
Subject: `feat: SaveGame plain-text slot I/O and listing`

---

## Task 4: `GameApp` — Save/Load buttons, dialogs, and live-state swap

**Files:**
- Modify: `src/adventure_game/GameApp.java`

No new unit tests (Swing glue; verified by the manual run in Task 5). The change must keep the full suite compiling/green.

- [ ] **Step 1: Make `state` swappable**

In `src/adventure_game/GameApp.java`, change the field declaration:
```java
    private final GameState state;
```
to:
```java
    private GameState state;
```
And add the saves-dir constant + imports. Add to the imports block:
```java
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.JOptionPane;
```
Add near the other `static final` constants (below `ROWS`):
```java
    private static final Path SAVES_DIR = Path.of("saves");
```

- [ ] **Step 2: Make button actions swap-safe**

The three `actionButton(..., state::method, ...)` call sites capture the *current* `state` instance at build time, which would break after a Load swap. Change them to lambdas that read the `state` field at click time.

In `buildExplorePanel`, change:
```java
        p.add(actionButton("Create Bandage", state::createBandage, false));
```
to:
```java
        p.add(actionButton("Create Bandage", () -> state.createBandage(), false));
```

In `buildCombatPanel`, change:
```java
        p.add(actionButton("Use Bandage", state::useBandage, true));
        p.add(actionButton("Create Bandage", state::createBandage, true));
```
to:
```java
        p.add(actionButton("Use Bandage", () -> state.useBandage(), true));
        p.add(actionButton("Create Bandage", () -> state.createBandage(), true));
```

(The combat Attack/Defend listeners already evaluate `state::playerAttack`/`state::playerDefend` inside the click lambda, so they read the field at click time and need no change.)

- [ ] **Step 3: Add Save/Load buttons to the Explore panel**

Replace `buildExplorePanel` with a 3×3 grid that adds Save Game and Load Game:
```java
    private JPanel buildExplorePanel() {
        JPanel p = new JPanel(new GridLayout(3, 3));
        p.add(moveButton("North", Direction.NORTH));
        p.add(moveButton("South", Direction.SOUTH));
        p.add(moveButton("East", Direction.EAST));
        p.add(moveButton("West", Direction.WEST));
        p.add(actionButton("Create Bandage", () -> state.createBandage(), false));
        p.add(simpleButton("Save Game", this::onSave));
        p.add(simpleButton("Load Game", this::onLoad));
        p.add(new JLabel());
        p.add(new JLabel());
        return p;
    }

    private JButton simpleButton(String label, Runnable action) {
        JButton b = new JButton(label);
        b.addActionListener(e -> {
            if (isAnimating()) {
                return;
            }
            action.run();
        });
        return b;
    }
```

- [ ] **Step 4: Add the save/load handlers**

Add these methods (e.g. after `buildWonPanel`):
```java
    private void onSave() {
        String name = JOptionPane.showInputDialog(this, "Save slot name:");
        if (name == null) {
            return; // cancelled
        }
        name = name.trim();
        if (name.isEmpty() || !name.matches("[A-Za-z0-9 _-]+")) {
            state.getLog().append("Invalid slot name. Use letters, digits, space, - or _.");
            redraw();
            return;
        }
        try {
            SaveGame.save(state, SaveGame.slotFile(SAVES_DIR, name));
            state.getLog().append("Game saved to slot '" + name + "'.");
        } catch (IOException ex) {
            state.getLog().append("Save failed: " + ex.getMessage());
        }
        redraw();
    }

    private void onLoad() {
        try {
            java.util.List<String> slots = SaveGame.listSlots(SAVES_DIR);
            if (slots.isEmpty()) {
                state.getLog().append("No saved games found.");
                redraw();
                return;
            }
            String choice = (String) JOptionPane.showInputDialog(this, "Load which slot?",
                    "Load Game", JOptionPane.QUESTION_MESSAGE, null,
                    slots.toArray(), slots.get(0));
            if (choice == null) {
                return; // cancelled
            }
            state = SaveGame.load(SaveGame.slotFile(SAVES_DIR, choice));
            state.getLog().append("Loaded slot '" + choice + "'.");
            redraw();
        } catch (IOException ex) {
            state.getLog().append("Load failed: " + ex.getMessage());
            redraw();
        }
    }
```

- [ ] **Step 5: Build and run the full suite (compile + regression)**

Run: `.\test.ps1`
Expected: all green (no new tests, but GameApp must compile and nothing regress).

- [ ] **Step 6: Commit**

```
git add src/adventure_game/GameApp.java
git commit -m "<feat: Save/Load buttons with slot dialogs and live-state swap + trailer>"
```
Subject: `feat: Save/Load buttons with slot dialogs and live-state swap`

---

## Task 5: Manual verification

**Files:** none (tuning/fixes only if the manual run reveals issues).

- [ ] **Step 1: Full automated suite**

Run: `.\test.ps1`
Expected: every test green (prior total + PlayerSave + SaveLoad + SaveGame tests).

- [ ] **Step 2: Manual playthrough**

Run: `.\run.ps1` and verify:
- In Explore, **Save Game** prompts for a name; entering e.g. `run1` logs "Game saved to slot 'run1'." and creates `saves/run1.txt`.
- Move to another room, pick up loot / clear a room, then **Load Game** lists `run1`; choosing it restores the earlier position and progress (room, HP, weapon, bandages, cleared rooms); the buttons still work on the restored game (move/attack/bandage act on the loaded state).
- **Load Game** with no saves logs "No saved games found."
- Saving again under `run1` overwrites it.

- [ ] **Step 3: Fixes (only if needed)**

If the manual run reveals a defect, fix it, re-run `.\test.ps1`, and commit:
```
git add -A
git commit -m "<fix: ... + trailer>"
```

- [ ] **Step 4: Final confirmation**

Run: `.\test.ps1`
Expected: all green. Phase 2b-3 complete: the run can be saved to named slots and loaded back.

---

## Self-Review (performed against the spec)

- **Spec coverage:** Full run state (§1, §4) → Tasks 1–2; plain-text format + slots (§3, §2-arch) → Task 3; Save/Load buttons + dialogs + state swap (§5) → Task 4; testing (§6) → tests in Tasks 1–3; manual GUI (§6) → Task 5. All covered.
- **Swing-free:** `SaveData`, `SaveGame`, `GameState`, `Player`, `Character` add no Swing imports; only `GameApp` imports `JOptionPane`.
- **State-swap safety (spec §5, §8 risk):** `state` is non-final; the three captured `state::method` button args become `() -> state.method()`; combat Attack/Defend already read the field at click time. Task 4 Step 2 covers this explicitly.
- **Type consistency:** `Player.fromSave(name, health, maxHealth, level, levelUpXp, baseDamage, weaponBonus, weaponName, bandages, sink)` and the `SaveData` field order match the `player:` line order in `SaveGame.save`/`load` and `GameState.toSaveData`. `restoreVitals(maxHealth, health, level, levelUpXp, baseDamage)` is consistent across Character/Player/usage.
- **No placeholders:** every code step shows complete code; every run step shows the command + expected result.
- **Delimiter safety (spec §8):** slot names restricted to `[A-Za-z0-9 _-]+`; player line parsed by fixed 10-field count; current weapon names are `:`-free.
