package adventure_game;

/**
 * One HP-gated stage of a boss fight. Active when the boss's HP fraction is at or
 * below {@code gatePercent}. {@code pattern} is the repeating per-turn move list;
 * {@code onEnter} (nullable) is a one-shot move applied the first time the boss
 * crosses into this phase (used for ENRAGE).
 */
public final class Phase {
    private final double gatePercent;
    private final Move onEnter;     // nullable
    private final Move[] pattern;

    public Phase(double gatePercent, Move onEnter, Move[] pattern) {
        if (pattern == null || pattern.length == 0) {
            throw new IllegalArgumentException("pattern must be non-empty");
        }
        this.gatePercent = gatePercent;
        this.onEnter = onEnter;
        this.pattern = pattern.clone();
    }

    public double gatePercent() { return gatePercent; }
    public Move onEnter() { return onEnter; }
    public Move moveForTurn(int turn) { return pattern[turn % pattern.length]; }
}
