# Phase 2b-1 — Loot & Weapons Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a loot/progression layer: weapons and bandages found in rooms and dropped by zombies, a single equipped weapon (swap-if-better) that boosts the player's effective damage, and HUD readouts for the equipped weapon and bandage count.

**Architecture:** A new immutable `Weapon` value type + a `Loot` helper hold the rules (Swing-free, unit-testable). `Character.getEffectiveDamage()` (default = baseDamage) is overridden by `Player` to add the weapon bonus, and `attack` switches to it (behavior-preserving). `GameState` places room loot at load and collects loot / rolls kill-drops via `Loot`. `Scene` shows the weapon + bandage count. The old stacking `Knife/Pistols/AssaultRifle/Weapons` classes are retired.

**Tech Stack:** Java 17, Swing, JUnit 5. Build/test via `.\build.ps1` / `.\test.ps1`; run via `.\run.ps1`. Current suite: 47 green.

**Commit note:** Commit from **PowerShell** only (signs silently; agent is warm). Do NOT run `gpgconf --kill gpg-agent`. Keep commit messages simple one-line `-m "..."` (no parentheses/apostrophes/quotes). Reset the agent only on a `socket file removed` failure.

**Ordering invariant:** every task leaves `javac` of `src/**` compiling and the suite green. `getEffectiveDamage()` defaults to `baseDamage`, so the `attack` change is behavior-preserving until a weapon is equipped.

---

## File Structure

**New — model (`adventure_game`, Swing-free):**
- `Weapon.java` — immutable `name()`/`bonus()` value type + `randomWeapon()` factory.
- `Loot.java` — `collectRoom(Room, Player, MessageLog)` and `rollKillDrop(Player, MessageLog)`.

**Modified — model:**
- `Character.java` — add `getEffectiveDamage()`; `attack` uses it.
- `Player.java` — weapon fields + `getEffectiveDamage()` override + `equipWeapon`/`getWeaponName`/`getWeaponBonus`/`bandageCount`.
- `GameState.java` — place room loot; collect loot + roll drops in the flow.

**Modified — engine:**
- `Scene.java` — HUD shows effective DMG + weapon + bandage count.

**Retired (deleted):**
- `items/Knife.java`, `items/Pistols.java`, `items/AssaultRifle.java`, `items/Weapons.java`; tests `KnifeTest.java`, `PistolTest.java`, `ARTest.java`.

**New/modified tests:** `WeaponTest`, `LootTest`, `PlayerWeaponTest` (new); `GameStateTest`, `SceneTest` (additions).

---

## Task 1: Weapon value type + loot factory

**Files:** Create `src/adventure_game/Weapon.java`, `src/adventure_game/Test/WeaponTest.java`.

- [ ] **Step 1: Write `WeaponTest.java`**

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

import adventure_game.GameRandom;
import adventure_game.Weapon;

public class WeaponTest {

    @Test
    void exposesNameAndBonus() {
        Weapon w = new Weapon("Knife", 12);
        assertEquals("Knife", w.name());
        assertEquals(12, w.bonus());
    }

    @Test
    void randomWeaponBonusMatchesItsTier() {
        GameRandom.rand = new Random(42);
        for (int i = 0; i < 200; i++) {
            Weapon w = Weapon.randomWeapon();
            switch (w.name()) {
                case "Knife":        assertTrue(w.bonus() >= 5 && w.bonus() <= 20, "knife " + w.bonus()); break;
                case "Pistol":       assertTrue(w.bonus() >= 15 && w.bonus() <= 35, "pistol " + w.bonus()); break;
                case "AssaultRifle": assertTrue(w.bonus() >= 30 && w.bonus() <= 55, "ar " + w.bonus()); break;
                default: fail("unknown weapon " + w.name());
            }
        }
    }
}
```

- [ ] **Step 2: Run to verify failure** — `.\build.ps1` → FAIL (`Weapon` missing).

- [ ] **Step 3: Create `Weapon.java`**

```java
package adventure_game;

/** An equippable weapon: a name and a flat damage bonus while equipped. */
public final class Weapon {
    private final String name;
    private final int bonus;

    public Weapon(String name, int bonus) {
        this.name = name;
        this.bonus = bonus;
    }

    public String name() { return name; }

    public int bonus() { return bonus; }

    /** Rolls a random weapon: a random tier, then a bonus within that tier's range. */
    public static Weapon randomWeapon() {
        int tier = GameRandom.rand.nextInt(3);
        switch (tier) {
            case 0:  return new Weapon("Knife", 5 + GameRandom.rand.nextInt(16));         // 5..20
            case 1:  return new Weapon("Pistol", 15 + GameRandom.rand.nextInt(21));       // 15..35
            default: return new Weapon("AssaultRifle", 30 + GameRandom.rand.nextInt(26)); // 30..55
        }
    }
}
```

- [ ] **Step 4: Run tests** — `.\test.ps1` → `WeaponTest` green; suite 49/49.

- [ ] **Step 5: Commit**

```
git add src/adventure_game/Weapon.java src/adventure_game/Test/WeaponTest.java
git commit -m "feat: add Weapon value type and random loot factory"
```

---

## Task 2: Retire the old stacking weapon classes

The `Knife/Pistols/AssaultRifle/Weapons` classes implement a stacking `pickUpItem` that the equipped model replaces. Nothing in the build references them (only the retired `reference/GameWindow.java.txt`, which is out of build, and their own tests).

**Files:** Delete `items/Knife.java`, `items/Pistols.java`, `items/AssaultRifle.java`, `items/Weapons.java`, `Test/KnifeTest.java`, `Test/PistolTest.java`, `Test/ARTest.java`.

- [ ] **Step 1: Confirm nothing in the build references them.** Run via PowerShell:
`Select-String -Path src\adventure_game\*.java, src\adventure_game\items\*.java, src\adventure_game\engine\*.java -Pattern "Knife|Pistols|AssaultRifle|\bWeapons\b" | Where-Object { $_.Path -notmatch "Knife.java|Pistols.java|AssaultRifle.java|Weapons.java" }`
Expected: NO matches (only the files themselves, which are excluded). If a non-test source references them, STOP and report.

- [ ] **Step 2: Delete the classes and tests**

```
git rm src/adventure_game/items/Knife.java src/adventure_game/items/Pistols.java src/adventure_game/items/AssaultRifle.java src/adventure_game/items/Weapons.java src/adventure_game/Test/KnifeTest.java src/adventure_game/Test/PistolTest.java src/adventure_game/Test/ARTest.java
```

- [ ] **Step 3: Build & test** — `.\test.ps1`. Expected: compiles; the 3 weapon tests are gone; suite drops to ~46 and is all green. (`Consumable` and `bandage` remain.)

- [ ] **Step 4: Commit**

```
git commit -m "refactor: retire stacking weapon classes superseded by Weapon"
```

---

## Task 3: Effective damage + Player weapon equip

**Files:** Modify `src/adventure_game/Character.java`, `src/adventure_game/Player.java`; create `src/adventure_game/Test/PlayerWeaponTest.java`.

- [ ] **Step 1: Write `PlayerWeaponTest.java`**

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

import adventure_game.GameRandom;
import adventure_game.MessageLog;
import adventure_game.NPC;
import adventure_game.Player;
import adventure_game.Weapon;

public class PlayerWeaponTest {

    @Test
    void startsUnarmedWithEffectiveDamageEqualToBase() {
        Player p = new Player("Mick", 150, 0, 75);
        assertEquals("Fists", p.getWeaponName());
        assertEquals(0, p.getWeaponBonus());
        assertEquals(75, p.getEffectiveDamage());
        assertEquals(0, p.bandageCount());
    }

    @Test
    void equippingStrongerWeaponRaisesDamageAndName() {
        Player p = new Player("Mick", 150, 0, 75);
        MessageLog log = new MessageLog();
        p.equipWeapon(new Weapon("AssaultRifle", 40), log);
        assertEquals("AssaultRifle", p.getWeaponName());
        assertEquals(40, p.getWeaponBonus());
        assertEquals(115, p.getEffectiveDamage());
    }

    @Test
    void weakerWeaponIsRejected() {
        Player p = new Player("Mick", 150, 0, 75);
        MessageLog log = new MessageLog();
        p.equipWeapon(new Weapon("AssaultRifle", 40), log);
        p.equipWeapon(new Weapon("Knife", 10), log);
        assertEquals("AssaultRifle", p.getWeaponName());
        assertEquals(40, p.getWeaponBonus());
        assertEquals(115, p.getEffectiveDamage());
    }

    @Test
    void npcEffectiveDamageIsItsBase() {
        NPC n = new NPC("Walker", 100, 0, 15);
        assertEquals(15, n.getEffectiveDamage());
    }

    @Test
    void attackUsesEffectiveDamageWhenArmed() {
        GameRandom.rand = new Random(5);
        MessageLog log = new MessageLog();

        Player unarmed = new Player("A", 150, 0, 10);
        NPC dummy1 = new NPC("Dummy", 100000, 0, 0);
        unarmed.attack(dummy1, log);
        int dmgUnarmed = 100000 - dummy1.getHealth();

        Player armed = new Player("B", 150, 0, 10);
        armed.equipWeapon(new Weapon("AssaultRifle", 90), log); // effective 100
        NPC dummy2 = new NPC("Dummy", 100000, 0, 0);
        armed.attack(dummy2, log);
        int dmgArmed = 100000 - dummy2.getHealth();

        assertTrue(dmgArmed > dmgUnarmed, "armed " + dmgArmed + " vs unarmed " + dmgUnarmed);
    }
}
```

- [ ] **Step 2: Run to verify failure** — `.\build.ps1` → FAIL (`getEffectiveDamage`/`equipWeapon`/`getWeaponName`/`getWeaponBonus`/`bandageCount` missing).

- [ ] **Step 3: Modify `Character.java`.** (a) Add a `getEffectiveDamage()` method. Insert it right after the existing `getBaseDamage()` method (the method that returns `this.baseDamage`):

```java
    /**
     * The damage actually used when this Character attacks. Defaults to the
     * base damage; subclasses (Player) add equipped-weapon bonuses.
     * @return the effective attack damage
     */
    public int getEffectiveDamage() {
        return this.baseDamage;
    }
```

(b) In `attack`, change the damage line from:

```java
        int damage = (int)(this.baseDamage * modifier);
```
to:
```java
        int damage = (int)(this.getEffectiveDamage() * modifier);
```

- [ ] **Step 4: Modify `Player.java`.** Add the weapon state, the override, and the accessors. Replace the whole file with:

```java
package adventure_game;

public class Player extends Character{

    private int weaponBonus = 0;
    private String weaponName = "Fists";

    /**
     * This is the player constructor
     * @param name of type String
     * @param health of type int
     * @param level of type int
     * @param baseDamage of type int
     */
    public Player(String name, int health, int level, int baseDamage){
        super(name, health, level, baseDamage);
    }

    @Override
    public void takeTurn(Character other, MessageLog output){
        output.append("Turn Taken");
    }

    /** Effective damage = innate base damage plus the equipped weapon's bonus. */
    @Override
    public int getEffectiveDamage() {
        return getBaseDamage() + weaponBonus;
    }

    /** Equip the weapon if it beats the current one; otherwise discard it. */
    public void equipWeapon(Weapon w, MessageLog output) {
        if (w.bonus() > weaponBonus) {
            weaponBonus = w.bonus();
            weaponName = w.name();
            output.append("You equip the " + w.name() + " (+" + w.bonus() + ")!");
        } else {
            output.append("You found a " + w.name() + " (+" + w.bonus()
                    + ") but your current weapon is better.");
        }
    }

    public String getWeaponName() { return weaponName; }

    public int getWeaponBonus() { return weaponBonus; }

    /** Number of bandages (consumables) currently carried. */
    public int bandageCount() { return items.size(); }

    @Override
    public void levelingUp(MessageLog output){
        if (this.level == 1) {
            int damageTotal = (int) (this.baseDamage * (0.1));
            this.modifyDamage(damageTotal);
            int healthTotal = (int) (this.maxHealth * (0.25));
            this.maxHealth += healthTotal;
            this.modifyHealth(this.maxHealth);
            output.append("You have level up. you are now level " + this.level + "\nYour health is " + this.getHealth() + "\nYour damage is " + this.baseDamage + "\n");
        }
        else if (this.level == 2) {
            int damageTotal = (int) (this.baseDamage * (0.1));
            this.modifyDamage(damageTotal);
            int healthTotal = (int) (this.maxHealth * (0.25));
            this.maxHealth += healthTotal;
            this.modifyHealth(this.maxHealth);
            output.append("You have level up. you are now level " + this.level + "\nYour health is " + this.getHealth() + "\nYour damage is " + this.baseDamage + "\n");
        }
        else if (this.level == 3) {
            int damageTotal = (int) (this.baseDamage * (0.1));
            this.modifyDamage(damageTotal);
            int healthTotal = (int) (this.maxHealth * (0.25));
            this.maxHealth += healthTotal;
            this.modifyHealth(this.maxHealth);
            output.append("You have level up. you are now level " + this.level + "\nYour health is " + this.getHealth() + "\nYour damage is " + this.baseDamage + "\n");
        }
        else if (this.level == 4) {
            int damageTotal = (int) (this.baseDamage * (0.1));
            this.modifyDamage(damageTotal);
            int healthTotal = (int) (this.maxHealth * (0.25));
            this.maxHealth += healthTotal;
            this.modifyHealth(this.maxHealth);
            output.append("You have level up. you are now level " + this.level + "\nYour health is " + this.getHealth() + "\nYour damage is " + this.baseDamage + "\n");
        }
        else if (this.level == 5) {
            int damageTotal = (int) (this.baseDamage * (0.1));
            this.modifyDamage(damageTotal);
            int healthTotal = (int) (this.maxHealth * (0.25));
            this.maxHealth += healthTotal;
            this.modifyHealth(this.maxHealth);
            output.append("You have level up. you are now level " + this.level + "\nYour health is " + this.getHealth() + "\nYour damage is " + this.baseDamage + "\n");
        }
        else {
            int damageTotal = (int) (this.baseDamage * (0.1));
            this.modifyDamage(damageTotal);
            int healthTotal = (int) (this.maxHealth * (0.3));
            this.maxHealth += healthTotal;
            this.modifyHealth(this.maxHealth);
            output.append("You have level up. you are now level " + this.level + "\nYour health is " + this.getHealth() + "\nYour damage is " + this.baseDamage + "\n");
        }
    }
}
```

(Note: `items` is the package-private `ArrayList<Consumable>` in `Character`; `Player` is in the same package, so `items.size()` is legal.)

- [ ] **Step 5: Run tests** — `.\test.ps1` → `PlayerWeaponTest` green; existing `CharacterTests` still green (effective damage equals base when unarmed, so `attack` math is unchanged); suite ~51.

- [ ] **Step 6: Commit**

```
git add src/adventure_game/Character.java src/adventure_game/Player.java src/adventure_game/Test/PlayerWeaponTest.java
git commit -m "feat: effective damage with equipped weapon on Player"
```

---

## Task 4: Loot helper (collect room loot + kill drops)

**Files:** Create `src/adventure_game/Loot.java`, `src/adventure_game/Test/LootTest.java`.

- [ ] **Step 1: Write `LootTest.java`**

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

import adventure_game.GameRandom;
import adventure_game.Loot;
import adventure_game.MessageLog;
import adventure_game.Player;
import adventure_game.Room;

public class LootTest {

    @Test
    void collectRoomGrantsWeaponAndBandageThenClearsFlags() {
        GameRandom.rand = new Random(1);
        Room r = new Room(0, "Test Room", "bio");
        r.setWeapon();
        r.setItem();
        Player p = new Player("Mick", 150, 0, 75);
        MessageLog log = new MessageLog();

        Loot.collectRoom(r, p, log);

        assertNotEquals("Fists", p.getWeaponName()); // any weapon (bonus>=5) beats fists
        assertTrue(p.bandageCount() >= 1);            // bandage collected
        assertEquals(-1, r.hasWeapon());              // flag cleared
        assertEquals(-1, r.hasItem());

        // Double-collect is a no-op (flags already cleared).
        String weaponBefore = p.getWeaponName();
        int bandagesBefore = p.bandageCount();
        Loot.collectRoom(r, p, log);
        assertEquals(weaponBefore, p.getWeaponName());
        assertEquals(bandagesBefore, p.bandageCount());
    }

    @Test
    void rollKillDropEventuallyDropsLoot() {
        GameRandom.rand = new Random(1);
        Player p = new Player("Mick", 150, 0, 75);
        MessageLog log = new MessageLog();
        for (int i = 0; i < 100; i++) {
            Loot.rollKillDrop(p, log);
        }
        // Over 100 rolls (~1/3 bandage, ~1/4 weapon each) drops are statistically certain.
        assertTrue(p.bandageCount() > 0, "expected at least one bandage drop");
        assertNotEquals("Fists", p.getWeaponName(), "expected at least one weapon drop");
    }
}
```

- [ ] **Step 2: Run to verify failure** — `.\build.ps1` → FAIL (`Loot` missing).

- [ ] **Step 3: Create `Loot.java`**

```java
package adventure_game;

import adventure_game.items.bandage;

/** Loot rules: collecting a room's loot and rolling kill drops. Swing-free. */
public final class Loot {

    private Loot() {}

    /** Grant a (safe) room's weapon/bandage to the player and clear its flags. */
    public static void collectRoom(Room room, Player player, MessageLog log) {
        if (room.hasWeapon() == 1) {
            player.equipWeapon(Weapon.randomWeapon(), log);
            room.removeWeapon();
        }
        if (room.hasItem() == 1) {
            player.obtain(new bandage(), log);
            room.removeItem();
        }
    }

    /** On a kill, a chance to drop a bandage and/or a weapon. */
    public static void rollKillDrop(Player player, MessageLog log) {
        if (GameRandom.rand.nextInt(3) == 0) {
            log.append("The corpse yields a bandage.");
            player.obtain(new bandage(), log);
        }
        if (GameRandom.rand.nextInt(4) == 0) {
            log.append("The zombie dropped a weapon.");
            player.equipWeapon(Weapon.randomWeapon(), log);
        }
    }
}
```

- [ ] **Step 4: Run tests** — `.\test.ps1` → `LootTest` green; suite ~53.

- [ ] **Step 5: Commit**

```
git add src/adventure_game/Loot.java src/adventure_game/Test/LootTest.java
git commit -m "feat: Loot helper for room collection and kill drops"
```

---

## Task 5: Wire loot into GameState

**Files:** Modify `src/adventure_game/GameState.java`, `src/adventure_game/Test/GameStateTest.java`.

- [ ] **Step 1: Add a placement test to `GameStateTest.java`** (append inside the class; `Direction`/`GameRandom`/`Random` imports already present from Phase 2a)

```java
    @Test
    void weaponAndBandageLootArePlacedAtLoad() throws Exception {
        GameRandom.rand = new java.util.Random(1);
        GameState gs = GameState.loadHospital();
        // Entrance east -> room 1 (Supply Room), a weapon+bandage room (still populated,
        // so loot is placed but not yet collected).
        gs.move(Direction.EAST);
        assertEquals(1, gs.getCurrentRoom().hasWeapon());
        assertEquals(1, gs.getCurrentRoom().hasItem());
    }
```

- [ ] **Step 2: Run to verify failure** — `.\build.ps1` then `.\test.ps1` → `weaponAndBandageLootArePlacedAtLoad` FAILS (no loot placed yet: `hasWeapon()` returns -1).

- [ ] **Step 3: Modify `GameState.java`.** Make four edits:

(a) Add the loot-room constants next to the existing room constants (after the `BOSS_ROOMS` line):

```java
    private static final int[] WEAPON_ROOMS = {1, 3, 6, 7, 8, 9, 11, 15, 16, 17, 20, 21};
    private static final int[] ITEM_ROOMS = {1, 3, 4, 7, 11, 17, 20, 21};
```

(b) In `loadHospital`, call `placeRoomLoot()` right after `gs.populateZombies();`:

```java
        gs.populateZombies();
        gs.placeRoomLoot();
```

(c) Add the `placeRoomLoot` method right after `populateZombies()`:

```java
    private void placeRoomLoot() {
        for (int i : WEAPON_ROOMS) {
            rooms.get(i).setWeapon();
        }
        for (int i : ITEM_ROOMS) {
            rooms.get(i).setItem();
        }
    }
```

(d) Wire collection + drops into the flow. In `tryEncounter`, change the trailing branch so an entered *safe* room is looted — replace:

```java
        } else if (npc == 4) {
            log.append("Something massive stirs in the dark... not yet.");
        }
    }
```
with:
```java
        } else if (npc == 4) {
            log.append("Something massive stirs in the dark... not yet.");
        } else {
            Loot.collectRoom(currentRoom, player, log);
        }
    }
```

And in `onEnemyDefeated`, add the kill-drop roll after `levelModifier` and collect loot when the room clears — replace the whole method with:

```java
    private void onEnemyDefeated() {
        log.append(enemy.getName() + " has died.");
        player.levelModifier(log);
        Loot.rollKillDrop(player, log);
        int roll = GameRandom.rand.nextInt(2) + 1;
        if (roll == 2) {
            spawnEnemy();
            log.append("Another zombie lurches out. Fuck.");
        } else {
            currentRoom.removeNPC();
            enemy = null;
            mode = Mode.EXPLORE;
            log.append("The room is clear... for now.");
            Loot.collectRoom(currentRoom, player, log);
        }
    }
```

(`Loot`, `Weapon`, `Room`, `Player` are all in package `adventure_game` — no new imports.)

- [ ] **Step 4: Run tests** — `.\test.ps1` → the placement test passes; all prior tests still green; suite ~54.

- [ ] **Step 5: Commit**

```
git add src/adventure_game/GameState.java src/adventure_game/Test/GameStateTest.java
git commit -m "feat: place and collect room loot and kill drops in GameState"
```

---

## Task 6: HUD shows weapon + bandages + effective damage

**Files:** Modify `src/adventure_game/engine/Scene.java`, `src/adventure_game/Test/SceneTest.java`.

- [ ] **Step 1: Add a HUD test to `SceneTest.java`** (append inside the class)

```java
    @Test
    void hudShowsBandageCountWeaponAndEffectiveDamage() throws Exception {
        GameState gs = GameState.loadHospital();
        Scene scene = buildScene();
        Screen screen = new Screen(60, 24);

        scene.render(screen, gs, null);

        assertTrue(gridContains(screen, "Bandages:0")); // none at start
        assertTrue(gridContains(screen, "Fists"));        // unarmed
        assertTrue(gridContains(screen, "DMG 75"));       // effective = base 75 + 0
    }
```

- [ ] **Step 2: Run to verify failure** — `.\build.ps1` then `.\test.ps1` → `hudShowsBandageCountWeaponAndEffectiveDamage` FAILS (HUD shows neither "Bandages:" nor "Fists" yet).

- [ ] **Step 3: Modify the HUD section of `Scene.java`.** Replace the HUD block (the lines from `s.drawText(1, 0, room.getRoomName());` through the row-2 separator loop) with:

```java
        // --- HUD ---
        s.drawText(1, 0, room.getRoomName());
        String topRight = "Lv." + p.getLevel() + "  Bandages:" + p.bandageCount();
        s.drawText(width - 1 - topRight.length(), 0, topRight);
        String hp = "HP " + HudComponents.healthBar(p.getHealth(), p.getMaxHealth(), 12)
                + " " + p.getHealth() + "/" + p.getMaxHealth();
        s.drawText(1, 1, hp);
        String botRight = "DMG " + p.getEffectiveDamage() + "  " + p.getWeaponName();
        s.drawText(width - 1 - botRight.length(), 1, botRight);
        for (int x = 0; x < width; x++) {
            s.put(x, 2, '-');
        }
```

(`Player p` is already bound at the top of `render`; `bandageCount`, `getEffectiveDamage`, `getWeaponName` come from Task 3.)

- [ ] **Step 4: Run tests** — `.\test.ps1` → the HUD test passes; the existing `renderShowsHudRoomNameHpAndLog` still passes (it asserts "Lv." which `topRight` still contains, plus room name / "HP [" / a log line); suite ~55.

- [ ] **Step 5: Commit**

```
git add src/adventure_game/engine/Scene.java src/adventure_game/Test/SceneTest.java
git commit -m "feat: HUD shows effective damage, weapon, and bandage count"
```

---

## Task 7: Run verification + README + acceptance

**Files:** modify `README.md`.

- [ ] **Step 1: Build, then launch and verify by playing.** `.\build.ps1` (exit 0). The controller launches the app detached and verifies, capturing a screenshot:
  - HUD starts with `Bandages:0`, `Fists`, `DMG 75`.
  - Move EAST into the Supply Room and clear its zombie → the log shows a weapon equipped and/or a bandage found; the HUD `DMG`/weapon name and `Bandages:` update.
  - Killing zombies occasionally logs a bandage/weapon drop; a better weapon swaps in, a worse one is rejected.

- [ ] **Step 2: Update `README.md`** — append after the Phase-2a section:

```markdown

### Phase 2b-1 — Loot & Weapons

Weapons (Knife / Pistol / AssaultRifle) and bandages are now found in rooms and
dropped by defeated zombies. You carry one equipped weapon that adds to your
damage and is swapped when you find a better one; the HUD shows your equipped
weapon, effective damage, and bandage count. Bosses, the win sequence, and
save/load are still to come. See
`docs/superpowers/specs/2026-06-07-phase2b1-loot-and-weapons-design.md`.
```

- [ ] **Step 3: Acceptance checklist** (verify by the run + tests):
  - [ ] `.\test.ps1` all green (record numbers).
  - [ ] No Swing imports in any model class except `GameApp` (`Select-String -Path src\adventure_game\*.java, src\adventure_game\items\*.java -Pattern "javax.swing"` → only `GameApp.java`).
  - [ ] HUD shows weapon + bandages + effective DMG; loot is found from rooms and drops; equip swap-if-better works.
  - [ ] Old `Knife`/`Pistols`/`AssaultRifle`/`Weapons` classes are gone (`Test-Path src\adventure_game\items\Knife.java` → False).

- [ ] **Step 4: Commit**

```
git add README.md
git commit -m "docs: Phase 2b-1 loot and weapons build notes"
```

---

## Self-Review notes (already reconciled)

- **Spec coverage:** Weapon model + factory (Task 1); retire old classes (Task 2); effective damage + equip swap-if-better + `attack` change (Task 3); room finds + kill drops via `Loot` (Task 4) and wiring/placement (Task 5); HUD weapon + bandages + effective DMG (Task 6); acceptance (Task 7). The spec's "no double-collect" is covered by `LootTest`; the "attack uses effective damage / unarmed unchanged" regression by `PlayerWeaponTest`.
- **Compile-green ordering:** `getEffectiveDamage()` defaults to base so Task 3's `attack` change is behavior-preserving; the old weapon classes are unreferenced in-build so Task 2 deletes cleanly; each new class precedes its consumer (`Weapon`→`Loot`/`Player`→`GameState`; Player getters→`Scene`).
- **Type consistency:** `Weapon(name,bonus)`, `name()`, `bonus()`, `Weapon.randomWeapon()`; `Loot.collectRoom(Room,Player,MessageLog)`, `Loot.rollKillDrop(Player,MessageLog)`; `Character.getEffectiveDamage()`; `Player.equipWeapon(Weapon,MessageLog)`/`getWeaponName()`/`getWeaponBonus()`/`bandageCount()`; `GameState.placeRoomLoot()`; `Room.hasWeapon()/hasItem()/setWeapon()/setItem()/removeWeapon()/removeItem()` (existing). Consistent across tasks.
- **No placeholders:** all code steps are complete; test counts are indicative (the gate is "green").
