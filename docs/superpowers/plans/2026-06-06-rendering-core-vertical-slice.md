# Rendering Core — Phase 1 Vertical Slice Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert the append-only Swing text RPG into an animated, ASCII-rendered single-screen game, proven by one end-to-end slice: one hospital room rendered on a character grid with HUD + log, the player `@` and one Walker on screen, where pressing **Attack** plays an in-viewport animation burst, then resolves the hit and updates the HP bar and log.

**Architecture:** Split into a Swing-free **model** (`GameState` + existing `Character`/`Player`/`NPC`/`Room`/`items`, with UI output replaced by a model-owned `MessageLog`) and a **renderer** (`Screen` frame buffer → `RenderPanel` monospaced surface, composed by `Scene`, animated by `AnimationPlayer` via `javax.swing.Timer`). An event-driven redraw cycle replaces the scrolling `JTextArea`.

**Tech Stack:** Java 17 (Temurin), Swing, JUnit 5 (`lib/junit-platform-console-standalone-1.9.2.jar`). No build tool — raw `javac`/`java` driven by PowerShell scripts.

**Commit note (this repo):** Commits are GPG-signed. Run commits from **PowerShell** (the Bash/MSYS `gpg` lacks the key). If a commit fails with `socket file removed - retrying binding`, run `gpgconf --kill gpg-agent; gpgconf --launch gpg-agent` first, then re-commit.

---

## File Structure

**New — model (no Swing imports):**
- `src/adventure_game/GameRandom.java` — shared `static Random rand` (removes the model's dependency on `GameWindow`).
- `src/adventure_game/MessageLog.java` — line-based message sink that replaces `JTextArea` output in the model.
- `src/adventure_game/GameState.java` — slice game state + logic: load the hospital map, hold player/current room/enemy/log, resolve one attack exchange.

**New — engine (rendering, package `adventure_game.engine`):**
- `Screen.java` — `char[][]` frame buffer + drawing primitives.
- `ArtAsset.java` — a piece of ASCII art (lines + size), loadable from `assets/art/*.txt`.
- `AnimationFrame.java` — one frame: art + (dx,dy) offset + duration ms.
- `Animation.java` — ordered list of frames.
- `Animations.java` — factory for concrete animations (the player strike).
- `AnimationPlayer.java` — `javax.swing.Timer`-driven burst player.
- `HudComponents.java` — reusable on-grid UI pieces (health bar).
- `Scene.java` — composes `GameState` (+ active frame) into a `Screen`.
- `RenderPanel.java` — `JPanel` that paints a `Screen` in a monospaced font.
- `adventure_game/GameApp.java` — new entry point: window + button row + redraw cycle.

**New — assets / scripts / skills:**
- `assets/art/player.txt`, `assets/art/walker.txt`, `assets/art/room0.txt`
- `build.ps1`, `test.ps1`, `run.ps1`, `.gitignore`
- `.claude/skills/ascii-art/SKILL.md`, `.claude/skills/ascii-animation/SKILL.md`, `.claude/skills/hud-component/SKILL.md`, `.claude/skills/room-and-map/SKILL.md`

**Modified — model decoupling (`JTextArea output` → `MessageLog output`, `GameWindow.rand` → `GameRandom.rand`):**
- `src/adventure_game/Character.java`, `Player.java`, `NPC.java`
- `src/adventure_game/items/Consumable.java`, `Weapons.java`, `bandage.java`, `antiBiotics.java`, `Knife.java`, `Pistols.java`, `AssaultRifle.java`
- Tests: `CharacterTests.java`, `bandageTest.java`, `KnifeTest.java`, `PistolTest.java`, `ARTest.java`

**Relocated (out of build, kept as porting reference):**
- `src/adventure_game/GameWindow.java` → `reference/GameWindow.java.txt`

---

## Task 0: Build scripts, .gitignore, baseline

**Files:**
- Create: `build.ps1`, `test.ps1`, `run.ps1`, `.gitignore`

- [ ] **Step 1: Create `.gitignore`**

```
out/
*.class
```

- [ ] **Step 2: Create `build.ps1`**

```powershell
$ErrorActionPreference = 'Stop'
$jar = "lib\junit-platform-console-standalone-1.9.2.jar"
$src = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }
if (Test-Path out) { Remove-Item -Recurse -Force out }
New-Item -ItemType Directory out | Out-Null
javac -cp $jar -d out @src
Write-Output "build exit: $LASTEXITCODE"
```

- [ ] **Step 3: Create `test.ps1`**

```powershell
$ErrorActionPreference = 'Stop'
.\build.ps1
$jar = "lib\junit-platform-console-standalone-1.9.2.jar"
java -jar $jar -cp out --scan-classpath --disable-banner
```

- [ ] **Step 4: Create `run.ps1`**

```powershell
$ErrorActionPreference = 'Stop'
.\build.ps1
java -cp out adventure_game.GameApp
```

- [ ] **Step 5: Run the baseline**

Run (PowerShell): `.\test.ps1`
Expected: compiles; `[ 14 tests found ]`, `[ 7 tests successful ]`, `[ 7 tests failed ]` — all 7 failures are `NullPointerException ... JTextArea.append`. This is the documented starting point; later tasks make it 14/14.

- [ ] **Step 6: Commit**

```powershell
git add .gitignore build.ps1 test.ps1 run.ps1
git commit -m "build: add javac/junit PowerShell scripts and .gitignore"
```

---

## Task 1: GameRandom + retire GameWindow from the build

Decouples the model from `GameWindow` (only `GameWindow.rand` is referenced externally) and removes the old Swing god-class from compilation. After this task the model still uses `JTextArea` (fixed in Task 2), so the 7 failing tests stay failing but the project still compiles.

**Files:**
- Create: `src/adventure_game/GameRandom.java`
- Modify: `Character.java`, `items/bandage.java`, `items/AssaultRifle.java`, `items/Knife.java`, `items/Pistols.java`
- Relocate: `src/adventure_game/GameWindow.java` → `reference/GameWindow.java.txt`

- [ ] **Step 1: Create `src/adventure_game/GameRandom.java`**

```java
package adventure_game;

import java.util.Random;

/**
 * Shared source of randomness for the game model.
 *
 * Replaces the former static {@code GameWindow.rand} so that model and
 * item classes do not depend on any Swing/UI class.
 */
public final class GameRandom {
    public static Random rand = new Random();

    private GameRandom() {}
}
```

- [ ] **Step 2: Repoint `rand` references in `Character.java`**

In `src/adventure_game/Character.java`, replace both occurrences of `GameWindow.rand` with `GameRandom.rand` (lines 136 and 159). No import needed (same package).

- [ ] **Step 3: Repoint `rand` references in the four item files**

In each of `items/bandage.java`, `items/AssaultRifle.java`, `items/Knife.java`, `items/Pistols.java`:
- Replace `import adventure_game.GameWindow;` with `import adventure_game.GameRandom;`
- Replace every `GameWindow.rand` with `GameRandom.rand`.

(`bandage.java`: lines 22, 24. `AssaultRifle.java`: line 50. `Knife.java`: line 53. `Pistols.java`: line 50.)

- [ ] **Step 4: Relocate the old GameWindow out of the build**

```powershell
New-Item -ItemType Directory -Force reference | Out-Null
git mv src/adventure_game/GameWindow.java reference/GameWindow.java.txt
```

(Kept as a reference for porting move/combat/loot/save logic into `GameState` in later phases. The `.txt` extension keeps it out of `javac` source globbing.)

- [ ] **Step 5: Build and run tests**

Run: `.\test.ps1`
Expected: compiles cleanly; still `[ 7 tests successful ]`, `[ 7 tests failed ]` (same NPEs — not fixed yet). The point of this step is that removing `GameWindow` did **not** break compilation.

- [ ] **Step 6: Commit**

```powershell
git add -A
git commit -m "refactor: extract GameRandom, move GameWindow out of build"
```

---

## Task 2: MessageLog + decouple the model from JTextArea

The keystone change. Introduce `MessageLog` and swap every `JTextArea output` parameter in the model for `MessageLog output`. Method bodies are unchanged except the parameter type (`append` exists on both). Update the 5 tests to pass a real `MessageLog`, which fixes the 7 NPEs.

**Files:**
- Create: `src/adventure_game/MessageLog.java`, `src/adventure_game/Test/MessageLogTest.java`
- Modify: `Character.java`, `Player.java`, `NPC.java`, `items/Consumable.java`, `items/Weapons.java`, `items/bandage.java`, `items/antiBiotics.java`, `items/Knife.java`, `items/Pistols.java`, `items/AssaultRifle.java`, and tests `CharacterTests.java`, `bandageTest.java`, `KnifeTest.java`, `PistolTest.java`, `ARTest.java`

- [ ] **Step 1: Write the failing test `MessageLogTest.java`**

`src/adventure_game/Test/MessageLogTest.java`:

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import adventure_game.MessageLog;

public class MessageLogTest {

    @Test
    void appendSplitsOnNewlinesAndSkipsBlanks() {
        MessageLog log = new MessageLog();
        log.append("first line\nsecond line\n");
        assertEquals(2, log.size());
        assertEquals("first line", log.lines().get(0));
        assertEquals("second line", log.lines().get(1));
    }

    @Test
    void recentReturnsTailInOrder() {
        MessageLog log = new MessageLog();
        log.append("a");
        log.append("b");
        log.append("c");
        List<String> last2 = log.recent(2);
        assertEquals(2, last2.size());
        assertEquals("b", last2.get(0));
        assertEquals("c", last2.get(1));
    }
}
```

- [ ] **Step 2: Run it to verify it fails**

Run: `.\build.ps1`
Expected: FAIL to compile — `MessageLog` does not exist yet.

- [ ] **Step 3: Create `src/adventure_game/MessageLog.java`**

```java
package adventure_game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Model-side message sink. Replaces direct writes to a Swing
 * {@code JTextArea}. The model appends narration here; the renderer
 * reads it to fill the on-screen log region.
 *
 * {@link #append(String)} accepts multi-line strings (the legacy code
 * appended strings containing "\n") and stores them as individual,
 * trimmed, non-blank lines.
 */
public class MessageLog {
    private final List<String> lines = new ArrayList<>();

    public void append(String text) {
        if (text == null) {
            return;
        }
        for (String part : text.split("\n")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                lines.add(trimmed);
            }
        }
    }

    public List<String> lines() {
        return Collections.unmodifiableList(lines);
    }

    public List<String> recent(int n) {
        int from = Math.max(0, lines.size() - n);
        return new ArrayList<>(lines.subList(from, lines.size()));
    }

    public void clear() {
        lines.clear();
    }

    public int size() {
        return lines.size();
    }
}
```

- [ ] **Step 4: Verify MessageLogTest passes**

Run: `.\build.ps1`
Expected: compiles.

- [ ] **Step 5: Change the two interface signatures**

`items/Consumable.java` — replace the import and method:

```java
package adventure_game.items;

import adventure_game.Character;
import adventure_game.MessageLog;

public interface Consumable {
    public void consume(Character owner, MessageLog output);
}
```

`items/Weapons.java` — replace the import and method:

```java
package adventure_game.items;

import adventure_game.Character;
import adventure_game.MessageLog;

public interface Weapons {
    public void pickUpItem(Character owner, MessageLog output);
}
```

- [ ] **Step 6: Change signatures in `Character.java`, `Player.java`, `NPC.java`**

In all three files:
- Remove `import javax.swing.JTextArea;`
- Add `import adventure_game.MessageLog;` only where the class is outside package `adventure_game`. (These three are in `adventure_game`, so **no import needed**; just delete the `JTextArea` import.)
- Replace every parameter `JTextArea output` with `MessageLog output`. This affects: `Character.attack`, `Character.defend`, `Character.obtain`, `Character.levelModifier`, the abstract `Character.takeTurn`, the abstract `Character.levelingUp`; `Player.takeTurn`, `Player.levelingUp`; `NPC.takeTurn`, `NPC.levelingUp`.

No body changes — every `output.append(...)` call works unchanged.

- [ ] **Step 7: Change signatures in the five item implementation files**

In `items/bandage.java`, `items/antiBiotics.java`, `items/Knife.java`, `items/Pistols.java`, `items/AssaultRifle.java`:
- Remove `import javax.swing.JTextArea;`
- Add `import adventure_game.MessageLog;`
- Replace `JTextArea output` with `MessageLog output` in the `consume` / `pickUpItem` signature.

- [ ] **Step 8: Update the five tests to use MessageLog**

In `CharacterTests.java`:
- Replace `import javax.swing.JTextArea;` with `import adventure_game.MessageLog;`
- Replace the field `private JTextArea output;` with `private MessageLog output = new MessageLog();`
- In `obtainTest`, the `Consumable a` field is null; change `c.obtain(a, output)` to obtain a real item: replace the line with `c.obtain(new bandage(), output);` (the `items.*` import is already present).

In `bandageTest.java`: replace `import javax.swing.JTextArea;` with `import adventure_game.MessageLog;`, and `private JTextArea t;` with `private MessageLog t = new MessageLog();`.

In `KnifeTest.java`, `PistolTest.java`, `ARTest.java`: replace the `JTextArea` import with `import adventure_game.MessageLog;` and change the local/field `JTextArea` output passed into `pickUpItem(...)` to a `new MessageLog()`. (Open each file; wherever a `JTextArea` is declared and passed as the second arg, make it a `MessageLog`.)

- [ ] **Step 9: Build and run the full suite**

Run: `.\test.ps1`
Expected: compiles; `[ 16 tests found ]` (14 original + 2 MessageLog), `[ 16 tests successful ]`, `[ 0 tests failed ]`. The 7 former NPE failures are now green.

- [ ] **Step 10: Verify the model has no Swing imports**

Run: `Select-String -Path src\adventure_game\*.java, src\adventure_game\items\*.java -Pattern "javax.swing"`
Expected: **no matches**.

- [ ] **Step 11: Commit**

```powershell
git add -A
git commit -m "refactor: decouple model from Swing via MessageLog (tests 16/16 green)"
```

---

## Task 3: Screen frame buffer

**Files:**
- Create: `src/adventure_game/engine/Screen.java`, `src/adventure_game/Test/ScreenTest.java`

- [ ] **Step 1: Write the failing test `ScreenTest.java`**

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.engine.ArtAsset;
import adventure_game.engine.Screen;

public class ScreenTest {

    @Test
    void clearFillsWithSpaces() {
        Screen s = new Screen(4, 2);
        s.clear();
        assertEquals("    ", s.toLines()[0]);
        assertEquals("    ", s.toLines()[1]);
    }

    @Test
    void drawTextWritesLeftToRightAndClips() {
        Screen s = new Screen(4, 1);
        s.clear();
        s.drawText(1, 0, "ABCDEF"); // clips at width 4
        assertEquals(" ABC", s.toLines()[0]);
    }

    @Test
    void putIgnoresOutOfBounds() {
        Screen s = new Screen(2, 2);
        s.clear();
        s.put(5, 5, 'X'); // no exception, no effect
        assertEquals("  ", s.toLines()[0]);
    }

    @Test
    void blitTreatsSpacesAsTransparent() {
        Screen s = new Screen(3, 2);
        s.clear();
        s.drawText(0, 0, "###");
        ArtAsset art = ArtAsset.fromLines("A A"); // middle space is transparent
        s.blit(art, 0, 0);
        assertEquals("A#A", s.toLines()[0]);
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `.\build.ps1`
Expected: FAIL to compile — `Screen` and `ArtAsset` do not exist. (`ArtAsset` is created in Task 4; this test is the driver for both. If running incrementally, comment out the `blitTreatsSpacesAsTransparent` test until Task 4, then re-enable.)

- [ ] **Step 3: Create `src/adventure_game/engine/Screen.java`**

```java
package adventure_game.engine;

/**
 * A fixed-size character frame buffer. The renderer recomposes the whole
 * Screen each redraw (the opposite of an append-only log) and a
 * {@link RenderPanel} paints it in a monospaced font.
 */
public class Screen {
    private final int width;
    private final int height;
    private final char[][] cells; // [row][col]

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new char[height][width];
        clear();
    }

    public int width() { return width; }

    public int height() { return height; }

    public void clear() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = ' ';
            }
        }
    }

    public void put(int x, int y, char c) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return;
        }
        cells[y][x] = c;
    }

    public char get(int x, int y) {
        return cells[y][x];
    }

    public void drawText(int x, int y, String s) {
        for (int i = 0; i < s.length(); i++) {
            put(x + i, y, s.charAt(i));
        }
    }

    public void drawBox(int x, int y, int w, int h) {
        for (int i = 1; i < w - 1; i++) {
            put(x + i, y, '-');
            put(x + i, y + h - 1, '-');
        }
        for (int j = 1; j < h - 1; j++) {
            put(x, y + j, '|');
            put(x + w - 1, y + j, '|');
        }
        put(x, y, '+');
        put(x + w - 1, y, '+');
        put(x, y + h - 1, '+');
        put(x + w - 1, y + h - 1, '+');
    }

    /** Draws art with space treated as transparent (composites over backdrop). */
    public void blit(ArtAsset art, int x, int y) {
        java.util.List<String> lines = art.lines();
        for (int row = 0; row < lines.size(); row++) {
            String line = lines.get(row);
            for (int col = 0; col < line.length(); col++) {
                char c = line.charAt(col);
                if (c != ' ') {
                    put(x + col, y + row, c);
                }
            }
        }
    }

    public String[] toLines() {
        String[] out = new String[height];
        for (int y = 0; y < height; y++) {
            out[y] = new String(cells[y]);
        }
        return out;
    }
}
```

- [ ] **Step 4: Verify (after Task 4 supplies ArtAsset) the Screen tests pass**

Run: `.\test.ps1` (re-enable the `blit` test once `ArtAsset` exists).
Expected: Screen tests green. If running Task 3 in isolation, the three non-blit tests pass and the blit test is temporarily commented out.

- [ ] **Step 5: Commit**

```powershell
git add src/adventure_game/engine/Screen.java src/adventure_game/Test/ScreenTest.java
git commit -m "feat(engine): add Screen character frame buffer"
```

---

## Task 4: ArtAsset + author player/walker art + create the ascii-art skill

**Files:**
- Create: `src/adventure_game/engine/ArtAsset.java`, `src/adventure_game/Test/ArtAssetTest.java`
- Create: `assets/art/player.txt`, `assets/art/walker.txt`
- Create: `.claude/skills/ascii-art/SKILL.md`

- [ ] **Step 1: Write the failing test `ArtAssetTest.java`**

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.engine.ArtAsset;

public class ArtAssetTest {

    @Test
    void fromLinesReportsSize() {
        ArtAsset a = ArtAsset.fromLines("abc", "de");
        assertEquals(2, a.height());
        assertEquals(3, a.width()); // widest line
        assertEquals("abc", a.lines().get(0));
    }

    @Test
    void fromFileLoadsAsset() throws Exception {
        ArtAsset a = ArtAsset.fromFile("assets/art/player.txt");
        assertTrue(a.height() >= 1);
        assertTrue(a.width() >= 1);
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `.\build.ps1`
Expected: FAIL — `ArtAsset` does not exist.

- [ ] **Step 3: Create `src/adventure_game/engine/ArtAsset.java`**

```java
package adventure_game.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A piece of ASCII art: a list of text lines plus its bounding size.
 * Space characters are treated as transparent when blitted (see
 * {@link Screen#blit}).
 */
public class ArtAsset {
    private final List<String> lines;
    private final int width;
    private final int height;

    public ArtAsset(List<String> lines) {
        this.lines = new ArrayList<>(lines);
        int w = 0;
        for (String line : lines) {
            w = Math.max(w, line.length());
        }
        this.width = w;
        this.height = lines.size();
    }

    public static ArtAsset fromLines(String... lines) {
        return new ArtAsset(Arrays.asList(lines));
    }

    public static ArtAsset fromFile(String path) throws IOException {
        return new ArtAsset(Files.readAllLines(Path.of(path)));
    }

    public List<String> lines() {
        return Collections.unmodifiableList(lines);
    }

    public int width() { return width; }

    public int height() { return height; }
}
```

- [ ] **Step 4: Create the `ascii-art` skill**

Use the `superpowers:writing-skills` skill to create `.claude/skills/ascii-art/SKILL.md` with this content:

```markdown
---
name: ascii-art
description: Use when authoring ASCII art for an entity, item, or boss in the PostProject zombie game, so every sprite is grid-aligned, correctly sized, and drops straight into the engine.
---

# ASCII Art Authoring (PostProject)

Produce ASCII art that the engine's `ArtAsset`/`Screen.blit` can render.

## Project standard
- **Entities** (player, zombies, bosses): exactly **5 columns wide x 5 rows tall**.
- **Items**: **3 wide x 3 tall**.
- Use **space for transparency** — spaces are NOT drawn, so the backdrop shows through. Pad every line to the full width with spaces.
- Anchor: bottom-centered (feet on the last row).
- ASCII only (printable 0x20-0x7E). No box-drawing/Unicode (the render font is Monospaced).

## Output
- Save to `assets/art/<name>.txt`, one art line per file line, no trailing blank line beyond the row count.
- After saving, state the file path and the exact width x height.

## Checklist
- [ ] Every line is the standard width (pad with spaces).
- [ ] Row count equals the standard height.
- [ ] Only printable ASCII used.
- [ ] Reads as the intended subject at a glance.
- [ ] Saved under `assets/art/` and size reported.
```

- [ ] **Step 5: Author `assets/art/player.txt` (5x5, space=transparent)**

```
  @  
 /|\ 
  |  
 / \ 
     
```

- [ ] **Step 6: Author `assets/art/walker.txt` (5x5)**

```
 ___ 
[o o]
 \_/ 
 /|\ 
 / \ 
```

- [ ] **Step 7: Run tests**

Run: `.\test.ps1`
Expected: `ArtAssetTest` green; re-enable `ScreenTest.blitTreatsSpacesAsTransparent` and confirm it is green; full suite still all-green.

- [ ] **Step 8: Commit**

```powershell
git add src/adventure_game/engine/ArtAsset.java src/adventure_game/Test/ArtAssetTest.java assets/art/player.txt assets/art/walker.txt .claude/skills/ascii-art/SKILL.md
git commit -m "feat(engine): add ArtAsset, player/walker art, ascii-art skill"
```

---

## Task 5: Animation + AnimationFrame + Animations factory + create the ascii-animation skill

**Files:**
- Create: `src/adventure_game/engine/AnimationFrame.java`, `src/adventure_game/engine/Animation.java`, `src/adventure_game/engine/Animations.java`, `src/adventure_game/Test/AnimationTest.java`
- Create: `.claude/skills/ascii-animation/SKILL.md`

- [ ] **Step 1: Write the failing test `AnimationTest.java`**

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.engine.Animation;
import adventure_game.engine.AnimationFrame;
import adventure_game.engine.Animations;
import adventure_game.engine.ArtAsset;

public class AnimationTest {

    @Test
    void frameExposesArtOffsetAndDuration() {
        AnimationFrame f = new AnimationFrame(ArtAsset.fromLines(">"), 12, 0, 60);
        assertEquals(12, f.getDx());
        assertEquals(0, f.getDy());
        assertEquals(60, f.getDurationMs());
        assertEquals(">", f.getArt().lines().get(0));
    }

    @Test
    void playerStrikeAdvancesAndEndsOnImpact() {
        Animation a = Animations.playerStrike();
        assertTrue(a.size() >= 2);
        AnimationFrame first = a.frames().get(0);
        AnimationFrame last = a.frames().get(a.size() - 1);
        assertTrue(last.getDx() > first.getDx());      // travels toward the enemy
        assertEquals("*", last.getArt().lines().get(0)); // impact marker
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `.\build.ps1`
Expected: FAIL — the three engine classes do not exist.

- [ ] **Step 3: Create `AnimationFrame.java`**

```java
package adventure_game.engine;

/** One frame of an animation: art drawn at an (dx,dy) offset for a duration. */
public class AnimationFrame {
    private final ArtAsset art;
    private final int dx;
    private final int dy;
    private final int durationMs;

    public AnimationFrame(ArtAsset art, int dx, int dy, int durationMs) {
        this.art = art;
        this.dx = dx;
        this.dy = dy;
        this.durationMs = durationMs;
    }

    public ArtAsset getArt() { return art; }
    public int getDx() { return dx; }
    public int getDy() { return dy; }
    public int getDurationMs() { return durationMs; }
}
```

- [ ] **Step 4: Create `Animation.java`**

```java
package adventure_game.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** An ordered sequence of frames played as one animation burst. */
public class Animation {
    private final List<AnimationFrame> frames;

    public Animation(List<AnimationFrame> frames) {
        this.frames = new ArrayList<>(frames);
    }

    public List<AnimationFrame> frames() {
        return Collections.unmodifiableList(frames);
    }

    public int size() {
        return frames.size();
    }
}
```

- [ ] **Step 5: Create `Animations.java`**

```java
package adventure_game.engine;

import java.util.ArrayList;
import java.util.List;

/** Factory for concrete animations used by the game. */
public final class Animations {

    private Animations() {}

    /**
     * The player's attack: a strike marker travels left-to-right from the
     * player toward the enemy, ending on an impact "*". Offsets are columns
     * relative to the player's blit position; Scene applies them.
     */
    public static Animation playerStrike() {
        ArtAsset strike = ArtAsset.fromLines(">");
        ArtAsset impact = ArtAsset.fromLines("*");
        List<AnimationFrame> frames = new ArrayList<>();
        frames.add(new AnimationFrame(strike, 8, 0, 60));
        frames.add(new AnimationFrame(strike, 16, 0, 60));
        frames.add(new AnimationFrame(strike, 24, 0, 60));
        frames.add(new AnimationFrame(strike, 30, 0, 60));
        frames.add(new AnimationFrame(impact, 34, 0, 90));
        return new Animation(frames);
    }
}
```

- [ ] **Step 6: Create the `ascii-animation` skill**

Use `superpowers:writing-skills` to create `.claude/skills/ascii-animation/SKILL.md`:

```markdown
---
name: ascii-animation
description: Use when authoring an animation (attack, lunge, hit-flash, death, idle) for the PostProject zombie game, to emit a frame sequence in the engine's AnimationFrame format.
---

# ASCII Animation Authoring (PostProject)

Turn a base art + an action into an `Animation` (a `List<AnimationFrame>`),
added as a factory method on `adventure_game.engine.Animations`.

## Frame format
`new AnimationFrame(ArtAsset art, int dx, int dy, int durationMs)`
- `dx,dy`: column/row offset from the actor's blit position (Scene adds them).
- `durationMs`: 50-120 ms per frame is the readable range for bursts.

## Conventions
- An action is one factory method returning `new Animation(frames)`.
- Bursts settle: the LAST frame is the impact/peak; the engine resolves game
  state on settle, so do not encode the "after" state in frames.
- Keep arts ASCII-only and small; reuse `assets/art` pieces where possible.
- 3-6 frames per burst.

## Checklist
- [ ] Factory method added to `Animations`.
- [ ] Offsets move in the intended direction; last frame is the peak.
- [ ] Per-frame durations in 50-120 ms.
- [ ] A unit test asserts size, direction (dx), and the final frame's art.
```

- [ ] **Step 7: Run tests**

Run: `.\test.ps1`
Expected: `AnimationTest` green; full suite all-green.

- [ ] **Step 8: Commit**

```powershell
git add src/adventure_game/engine/AnimationFrame.java src/adventure_game/engine/Animation.java src/adventure_game/engine/Animations.java src/adventure_game/Test/AnimationTest.java .claude/skills/ascii-animation/SKILL.md
git commit -m "feat(engine): add Animation/AnimationFrame/Animations + ascii-animation skill"
```

---

## Task 6: AnimationPlayer (Swing Timer burst driver)

**Files:**
- Create: `src/adventure_game/engine/AnimationPlayer.java`

This class drives `javax.swing.Timer`; it is verified manually in the Task 10 end-to-end run (Timer-based timing is awkward to unit test and not worth the harness). Keep it tiny and obviously correct.

- [ ] **Step 1: Create `AnimationPlayer.java`**

```java
package adventure_game.engine;

import java.util.function.Consumer;

import javax.swing.Timer;

/**
 * Plays an {@link Animation} as a burst on the Swing event thread.
 *
 * For each frame it calls {@code onFrame(frame)} (the caller stores it as
 * the active frame and repaints), waiting the frame's duration. After the
 * last frame it calls {@code onFrame(null)} then {@code onSettle} (the
 * caller resolves game state and does a full redraw).
 */
public class AnimationPlayer {
    private final Consumer<AnimationFrame> onFrame;
    private final Runnable onSettle;
    private Timer timer;
    private Animation anim;
    private int index;

    public AnimationPlayer(Consumer<AnimationFrame> onFrame, Runnable onSettle) {
        this.onFrame = onFrame;
        this.onSettle = onSettle;
    }

    public boolean isPlaying() {
        return timer != null && timer.isRunning();
    }

    public void play(Animation animation) {
        if (isPlaying()) {
            return;
        }
        this.anim = animation;
        this.index = 0;
        if (animation.size() == 0) {
            onSettle.run();
            return;
        }
        onFrame.accept(animation.frames().get(0));
        timer = new Timer(animation.frames().get(0).getDurationMs(), null);
        timer.setRepeats(true);
        timer.addActionListener(e -> {
            index++;
            if (index >= anim.size()) {
                timer.stop();
                onFrame.accept(null);
                onSettle.run();
                return;
            }
            AnimationFrame frame = anim.frames().get(index);
            onFrame.accept(frame);
            timer.setDelay(frame.getDurationMs());
        });
        timer.start();
    }
}
```

- [ ] **Step 2: Build**

Run: `.\build.ps1`
Expected: compiles; tests unchanged and green.

- [ ] **Step 3: Commit**

```powershell
git add src/adventure_game/engine/AnimationPlayer.java
git commit -m "feat(engine): add AnimationPlayer Swing-Timer burst driver"
```

---

## Task 7: HudComponents + create the hud-component skill

**Files:**
- Create: `src/adventure_game/engine/HudComponents.java`, `src/adventure_game/Test/HudComponentsTest.java`
- Create: `.claude/skills/hud-component/SKILL.md`

- [ ] **Step 1: Write the failing test `HudComponentsTest.java`**

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.engine.HudComponents;

public class HudComponentsTest {

    @Test
    void fullBarIsAllFilled() {
        assertEquals("[||||||||||]", HudComponents.healthBar(100, 100, 10));
    }

    @Test
    void emptyBarIsAllDots() {
        assertEquals("[..........]", HudComponents.healthBar(0, 100, 10));
    }

    @Test
    void halfBarIsHalfFilled() {
        assertEquals("[|||||.....]", HudComponents.healthBar(50, 100, 10));
    }

    @Test
    void clampsOverAndUnderflow() {
        assertEquals("[||||||||||]", HudComponents.healthBar(999, 100, 10));
        assertEquals("[..........]", HudComponents.healthBar(-5, 100, 10));
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `.\build.ps1`
Expected: FAIL — `HudComponents` does not exist.

- [ ] **Step 3: Create `HudComponents.java`**

```java
package adventure_game.engine;

/** Reusable on-grid UI pieces rendered as text, drawn via Screen.drawText. */
public final class HudComponents {

    private HudComponents() {}

    /** A bracketed bar of `width` cells: '|' filled, '.' empty. */
    public static String healthBar(int current, int max, int width) {
        if (max <= 0) {
            max = 1;
        }
        int filled = (int) Math.round((double) current / max * width);
        if (filled < 0) {
            filled = 0;
        }
        if (filled > width) {
            filled = width;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < width; i++) {
            sb.append(i < filled ? '|' : '.');
        }
        sb.append(']');
        return sb.toString();
    }
}
```

- [ ] **Step 4: Create the `hud-component` skill**

Use `superpowers:writing-skills` to create `.claude/skills/hud-component/SKILL.md`:

```markdown
---
name: hud-component
description: Use when building a reusable on-grid UI piece (bar, panel, menu, button row) for the PostProject zombie game, so HUD elements share one style and render through Screen.
---

# HUD Component Authoring (PostProject)

Build reusable text UI pieces as pure functions returning Strings (or
drawing onto a `Screen`), added to `adventure_game.engine.HudComponents`.

## Style rules
- ASCII only. Bars use `[`...`]` with `|` (filled) and `.` (empty).
- Panels/boxes use `+ - |` (matches `Screen.drawBox`).
- Components are pure and deterministic: same inputs -> same string. No I/O.
- Clamp numeric inputs (never overflow the declared width).

## Output
- A static method on `HudComponents` with a focused signature
  (e.g. `healthBar(int current, int max, int width)`).
- A unit test covering full / empty / mid / clamped cases.

## Checklist
- [ ] Pure function, deterministic, clamped.
- [ ] ASCII-only, width-exact output.
- [ ] Unit test with boundary cases.
```

- [ ] **Step 5: Run tests**

Run: `.\test.ps1`
Expected: `HudComponentsTest` green; full suite all-green.

- [ ] **Step 6: Commit**

```powershell
git add src/adventure_game/engine/HudComponents.java src/adventure_game/Test/HudComponentsTest.java .claude/skills/hud-component/SKILL.md
git commit -m "feat(engine): add HudComponents health bar + hud-component skill"
```

---

## Task 8: GameState (load map, hold slice state, resolve one attack)

**Files:**
- Create: `src/adventure_game/GameState.java`, `src/adventure_game/Test/GameStateTest.java`

- [ ] **Step 1: Write the failing test `GameStateTest.java`**

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.GameState;

public class GameStateTest {

    @Test
    void loadsHospitalWithEntrancePlayerAndEnemy() throws Exception {
        GameState gs = GameState.loadHospital();
        assertEquals("Hospital Entrance", gs.getCurrentRoom().getRoomName());
        assertEquals("Mick", gs.getPlayer().getName());
        assertNotNull(gs.getEnemy());
        assertTrue(gs.isInCombat());
    }

    @Test
    void playerAttackDamagesEnemyAndLogs() throws Exception {
        GameState gs = GameState.loadHospital();
        int before = gs.getEnemy().getHealth();
        int logBefore = gs.getLog().size();
        gs.playerAttack();
        assertTrue(gs.getEnemy().getHealth() < before || !gs.isInCombat());
        assertTrue(gs.getLog().size() > logBefore);
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `.\build.ps1`
Expected: FAIL — `GameState` does not exist.

- [ ] **Step 3: Create `GameState.java`**

The `readMap` body is ported verbatim from `reference/GameWindow.java.txt` (same parsing), changed only to write into the passed `rooms` list and use a repo-relative path.

```java
package adventure_game;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Phase-1 slice game state: loads the hospital map, holds the player, the
 * current room, one enemy, and the message log, and resolves a single
 * attack exchange. Swing-free.
 */
public class GameState {
    private Player player;
    private List<Room> rooms;
    private Room currentRoom;
    private NPC enemy;
    private boolean inCombat;
    private final MessageLog log = new MessageLog();

    public static GameState loadHospital() throws FileNotFoundException {
        GameState gs = new GameState();
        gs.rooms = new ArrayList<>();
        readMap("data/levels/Hospital Map/The-Hospital.txt", gs.rooms);
        gs.currentRoom = gs.rooms.get(0);
        gs.player = new Player("Mick", 150, 0, 75);
        gs.enemy = new NPC("Walker", 100, 0, 15);
        gs.inCombat = true;
        gs.log.append("A Walker shuffles toward you.");
        return gs;
    }

    /** Resolve one attack: player hits enemy; if it survives, it hits back. */
    public void playerAttack() {
        if (!inCombat || enemy == null) {
            return;
        }
        player.attack(enemy, log);
        if (enemy.isAlive()) {
            enemy.takeTurn(player, log);
        } else {
            log.append(enemy.getName() + " has died.");
            inCombat = false;
        }
    }

    public Player getPlayer() { return player; }
    public Room getCurrentRoom() { return currentRoom; }
    public NPC getEnemy() { return enemy; }
    public boolean isInCombat() { return inCombat; }
    public MessageLog getLog() { return log; }

    private static void readMap(String file, List<Room> rooms) throws FileNotFoundException {
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
                String[] fileInfo = line.split(":");
                int roomNumber = Integer.parseInt(fileInfo[0]);
                String roomName = fileInfo[1];
                String roomBio = fileInfo[2];
                rooms.add(new Room(roomNumber, roomName, roomBio));
            } else if (count > numberLinesToRead + 3) {
                line = line.trim();
                String[] exits = line.split(":");
                int roomTag = Integer.parseInt(exits[0].trim());
                int roomEast = Integer.parseInt(exits[1].trim());
                int roomNorth = Integer.parseInt(exits[2].trim());
                int roomWest = Integer.parseInt(exits[3].trim());
                int roomSouth = Integer.parseInt(exits[4].trim());
                Room cur = rooms.get(roomTag);
                if (roomEast == -1) { cur.setRoomEastNull(); } else { cur.setEastRoom(rooms.get(roomEast)); }
                if (roomNorth == -1) { cur.setRoomNorthNull(); } else { cur.setNorthRoom(rooms.get(roomNorth)); }
                if (roomSouth == -1) { cur.setRoomSouthNull(); } else { cur.setSouthRoom(rooms.get(roomSouth)); }
                if (roomWest == -1) { cur.setRoomWestNull(); } else { cur.setWestRoom(rooms.get(roomWest)); }
            }
            count++;
        }
        scnr.close();
    }
}
```

- [ ] **Step 4: Run tests**

Run: `.\test.ps1`
Expected: `GameStateTest` green. (Note: the map path is relative to the repo root; `.\test.ps1` runs from there.) Full suite all-green.

- [ ] **Step 5: Commit**

```powershell
git add src/adventure_game/GameState.java src/adventure_game/Test/GameStateTest.java
git commit -m "feat(model): add GameState (load hospital, resolve one attack)"
```

---

## Task 9: Scene (compose state into Screen) + room backdrop + create the room-and-map skill

**Files:**
- Create: `src/adventure_game/engine/Scene.java`, `src/adventure_game/Test/SceneTest.java`
- Create: `assets/art/room0.txt`
- Create: `.claude/skills/room-and-map/SKILL.md`

- [ ] **Step 1: Author `assets/art/room0.txt`** (viewport backdrop, 13 rows tall max, ASCII-only)

```
============================================================
  ||   HOSPITAL ENTRANCE        ||   ||                    
  ||                            ||   ||                    
__||____________________________||___||____________________
                                                            
                                                            
                                                            
                                                            
____________________________________________________________
```

- [ ] **Step 2: Write the failing test `SceneTest.java`**

```java
package adventure_game.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import adventure_game.GameState;
import adventure_game.engine.ArtAsset;
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

    @Test
    void renderDrawsRoomNameHpBarAndLog() throws Exception {
        GameState gs = GameState.loadHospital();
        ArtAsset player = ArtAsset.fromFile("assets/art/player.txt");
        ArtAsset walker = ArtAsset.fromFile("assets/art/walker.txt");
        ArtAsset backdrop = ArtAsset.fromFile("assets/art/room0.txt");
        Scene scene = new Scene(60, 24, player, walker, backdrop);
        Screen screen = new Screen(60, 24);

        scene.render(screen, gs, null);

        assertTrue(gridContains(screen, "Hospital Entrance")); // HUD name
        assertTrue(gridContains(screen, "HP ["));              // HUD bar
        assertTrue(gridContains(screen, "Lv."));               // HUD level
        assertTrue(gridContains(screen, "Walker shuffles"));   // log line
    }
}
```

- [ ] **Step 3: Run to verify it fails**

Run: `.\build.ps1`
Expected: FAIL — `Scene` does not exist.

- [ ] **Step 4: Create `Scene.java`**

```java
package adventure_game.engine;

import java.util.List;

import adventure_game.GameState;
import adventure_game.Player;
import adventure_game.Room;

/**
 * Composes the whole Screen from GameState each redraw: HUD (top), viewport
 * (middle, where art and animation frames draw), and log (bottom).
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
    private final ArtAsset walkerArt;
    private final ArtAsset backdrop;

    public Scene(int width, int height, ArtAsset playerArt, ArtAsset walkerArt, ArtAsset backdrop) {
        this.width = width;
        this.height = height;
        this.playerArt = playerArt;
        this.walkerArt = walkerArt;
        this.backdrop = backdrop;
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
        s.blit(backdrop, 0, VIEW_TOP);
        s.blit(playerArt, PLAYER_X, PLAYER_Y);
        if (state.isInCombat() && state.getEnemy() != null) {
            s.blit(walkerArt, ENEMY_X, PLAYER_Y);
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

- [ ] **Step 5: Create the `room-and-map` skill**

Use `superpowers:writing-skills` to create `.claude/skills/room-and-map/SKILL.md`:

```markdown
---
name: room-and-map
description: Use when adding or editing a room in the PostProject hospital - authors the viewport backdrop art AND the matching map .txt lines (room entry + exits) so geometry and data stay consistent.
---

# Room & Map Authoring (PostProject)

A room needs two artifacts that MUST agree:

## 1. Backdrop art -> assets/art/room<N>.txt
- Width = viewport width (currently 60 columns).
- Height <= viewport rows (currently 13: Scene rows 3-15).
- ASCII only; space = transparent (player/enemy composite on top).
- A floor line near the bottom so characters stand on something.

## 2. Map data -> data/levels/Hospital Map/The-Hospital.txt
Two sections, separated by `#` comment lines (do not renumber existing rooms):
- Room entry: `N:Room Name:Room bio sentence(s).`
- Exit line (after the `#  E   N   W   S` header): `N: E: N: W: S:` where each
  is a target room number or `-1` for no exit.

## Consistency checklist
- [ ] Backdrop exits visually match the exit line directions.
- [ ] Room number identical in both files; not already used.
- [ ] Exit targets are valid room numbers that exist.
- [ ] Reciprocal exits agree (if A's east is B, B's west should be A) unless a one-way is intended.
- [ ] Bio mentions the real exits.
```

- [ ] **Step 6: Run tests**

Run: `.\test.ps1`
Expected: `SceneTest` green; full suite all-green.

- [ ] **Step 7: Commit**

```powershell
git add src/adventure_game/engine/Scene.java src/adventure_game/Test/SceneTest.java assets/art/room0.txt .claude/skills/room-and-map/SKILL.md
git commit -m "feat(engine): add Scene compositor, room0 backdrop, room-and-map skill"
```

---

## Task 10: RenderPanel + GameApp entry point (end-to-end slice)

**Files:**
- Create: `src/adventure_game/engine/RenderPanel.java`, `src/adventure_game/GameApp.java`

These are GUI classes verified by **running the app** (manual visual check), since headless unit tests can't observe painting.

- [ ] **Step 1: Create `RenderPanel.java`**

```java
package adventure_game.engine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JPanel;

/** Paints a Screen in a monospaced font, one row per text line. */
public class RenderPanel extends JPanel {
    private final int cols;
    private final int rows;
    private final int cellW;
    private final int cellH;
    private final int ascent;
    private Screen screen;

    public RenderPanel(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        setBackground(Color.BLACK);
        Font font = new Font("Monospaced", Font.PLAIN, 16);
        setFont(font);
        FontMetrics fm = getFontMetrics(font);
        this.cellW = fm.charWidth('M');
        this.cellH = fm.getHeight();
        this.ascent = fm.getAscent();
        setPreferredSize(new Dimension(cols * cellW + 20, rows * cellH + 20));
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (screen == null) {
            return;
        }
        g.setFont(getFont());
        g.setColor(new Color(120, 230, 120));
        String[] lines = screen.toLines();
        for (int y = 0; y < lines.length; y++) {
            g.drawString(lines[y], 10, 10 + ascent + y * cellH);
        }
    }
}
```

- [ ] **Step 2: Create `GameApp.java`**

```java
package adventure_game;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import adventure_game.engine.Animations;
import adventure_game.engine.AnimationFrame;
import adventure_game.engine.AnimationPlayer;
import adventure_game.engine.ArtAsset;
import adventure_game.engine.RenderPanel;
import adventure_game.engine.Scene;
import adventure_game.engine.Screen;

/** Entry point: unified single-screen window with the redraw cycle. */
public class GameApp extends JFrame {
    private static final int COLS = 60;
    private static final int ROWS = 24;

    private final RenderPanel panel;
    private final Scene scene;
    private final Screen screen;
    private final GameState state;
    private final AnimationPlayer animationPlayer;
    private AnimationFrame activeFrame;

    public GameApp() throws Exception {
        super("Covid, The Zombie Apocalypse");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        screen = new Screen(COLS, ROWS);
        ArtAsset playerArt = ArtAsset.fromFile("assets/art/player.txt");
        ArtAsset walkerArt = ArtAsset.fromFile("assets/art/walker.txt");
        ArtAsset backdrop = ArtAsset.fromFile("assets/art/room0.txt");
        scene = new Scene(COLS, ROWS, playerArt, walkerArt, backdrop);
        state = GameState.loadHospital();
        panel = new RenderPanel(COLS, ROWS);

        animationPlayer = new AnimationPlayer(
                frame -> { activeFrame = frame; redraw(); },
                () -> { state.playerAttack(); activeFrame = null; redraw(); });

        JPanel buttons = new JPanel(new GridLayout(2, 4));
        buttons.add(directionButton("North"));
        buttons.add(directionButton("South"));
        buttons.add(directionButton("East"));
        buttons.add(directionButton("West"));

        JButton attack = new JButton("Attack");
        attack.addActionListener(e -> {
            if (!animationPlayer.isPlaying() && state.isInCombat()) {
                animationPlayer.play(Animations.playerStrike());
            }
        });
        buttons.add(attack);

        buttons.add(stubButton("Item"));
        buttons.add(stubButton("Save"));
        buttons.add(stubButton("-"));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);

        redraw();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JButton directionButton(String label) {
        JButton b = new JButton(label);
        b.addActionListener(e -> {
            state.getLog().append("Movement is not wired up yet (Phase 1 slice).");
            redraw();
        });
        return b;
    }

    private JButton stubButton(String label) {
        JButton b = new JButton(label);
        b.addActionListener(e -> {
            state.getLog().append(label + " is not wired up yet (Phase 1 slice).");
            redraw();
        });
        return b;
    }

    private void redraw() {
        scene.render(screen, state, activeFrame);
        panel.setScreen(screen);
        panel.repaint();
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

- [ ] **Step 3: Build**

Run: `.\build.ps1`
Expected: compiles cleanly.

- [ ] **Step 4: Run the slice and verify visually**

Run: `.\run.ps1`
Expected, in one window:
- Top HUD shows `Hospital Entrance`, `Lv.0`, `HP [||||||||||||] 150/150`, and `DMG 75`.
- Viewport shows the room backdrop with the player art at the left and the Walker art at the right.
- Log shows `> A Walker shuffles toward you.`
- Pressing **Attack** plays the `>` strike traveling left-to-right ending in a `*`, then the HP bar / log update (the Walker hits back, the player's HP bar drops) and the scene settles.
- Repeated Attacks eventually show `> Walker has died.` and the Walker art disappears.

- [ ] **Step 5: Run the test suite once more**

Run: `.\test.ps1`
Expected: all green (the new GUI classes add no tests but must not break compilation).

- [ ] **Step 6: Commit**

```powershell
git add src/adventure_game/engine/RenderPanel.java src/adventure_game/GameApp.java
git commit -m "feat: GameApp unified screen + RenderPanel (vertical slice runs)"
```

---

## Task 11: README + spec acceptance check

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Append a build/run section to `README.md`**

Add at the end of `README.md`:

```markdown
## Building & running (Phase 1)

Requires JDK 17+ on PATH. From the repository root, in PowerShell:

- `./build.ps1` — compile to `out/`
- `./test.ps1` — compile and run the JUnit suite
- `./run.ps1` — launch the game (`adventure_game.GameApp`)

The Phase 1 vertical slice renders one room with the player and one Walker;
**Attack** plays an animation burst and resolves the hit. See
`docs/superpowers/specs/2026-06-06-zombie-game-rendering-core-design.md`.
```

- [ ] **Step 2: Verify the spec's Phase 1 acceptance criteria**

Confirm each, by inspection / the prior runs:
- [ ] Builds and runs from this repo via `./run.ps1` (Task 10).
- [ ] Single window, four-region layout (Task 10 visual check).
- [ ] HUD shows room name, level, HP **bar**, damage (Task 9 + 10).
- [ ] Viewport shows backdrop + `@` + Walker (Task 10).
- [ ] Attack plays a multi-frame viewport animation, then HP bar + log update and settle (Task 10).
- [ ] All tests pass (`./test.ps1` => 0 failed; Task 2 turned the 7 NPEs green).
- [ ] No Swing imports in `Character`/`Player`/`NPC`/`Room`/`items` (Task 2 Step 10).

- [ ] **Step 3: Commit**

```powershell
git add README.md
git commit -m "docs: add Phase 1 build/run instructions"
```

---

## Self-Review notes (already reconciled)

- **Spec coverage:** model/renderer split (Tasks 1-2, 3-10), frame buffer (3), RenderPanel monospaced (10), ArtAsset/Animation/AnimationPlayer (4,5,6), unified screen (9,10), four skills (4,5,7,9), model decoupling + no-Swing-imports (2), path fix + build/run (0,8,11), tests green (2). Vertical slice acceptance (11).
- **Type consistency:** `MessageLog.append/lines/recent/size`; `Screen.put/drawText/drawBox/blit/toLines`; `ArtAsset.fromLines/fromFile/lines/width/height`; `AnimationFrame.getArt/getDx/getDy/getDurationMs`; `Animation.frames/size`; `AnimationPlayer(Consumer<AnimationFrame>, Runnable).play`; `HudComponents.healthBar(int,int,int)`; `Scene(int,int,ArtAsset,ArtAsset,ArtAsset).render(Screen,GameState,AnimationFrame)`; `GameState.loadHospital/playerAttack/getPlayer/getCurrentRoom/getEnemy/isInCombat/getLog` — all used consistently across tasks.
- **No placeholders:** every code/asset/skill step contains full content; stub buttons are intentional, labeled slice scope, not TODO gaps.
