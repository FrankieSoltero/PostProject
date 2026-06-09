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
