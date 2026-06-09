package adventure_game;

/**
 * A phased boss. Is-an {@link NPC}, so GameState's enemy.takeTurn(...) drives it
 * polymorphically. Constructed at LITERAL stats — never call levelingUp() on a
 * Boss (the reference levels bosses 3x, which inflates Faucci to ~327k = unwinnable).
 */
public class Boss extends NPC {

    private final Phase[] phases;   // index 0 = phase 1 (gate 1.0), hardest gate last
    private int phaseIndex = 0;
    private int turnInFight = 0;

    public Boss(String name, int health, int level, int baseDamage, Phase[] phases) {
        super(name, health, level, baseDamage);
        if (phases == null || phases.length == 0) {
            throw new IllegalArgumentException("a boss needs at least one phase");
        }
        this.phases = phases.clone();
    }

    /** e.g. "P2/3" for the combat banner. */
    public String getPhaseLabel() {
        return "P" + (phaseIndex + 1) + "/" + phases.length;
    }

    @Override
    public void takeTurn(Character other, MessageLog output) {
        if (this.isStunned()) {
            this.decreaseTurnsStunned();
            return;
        }
        advancePhaseByHp(output);
        Move move = phases[phaseIndex].moveForTurn(turnInFight);
        executeMove(move, other, output);
        turnInFight++;
    }

    private void advancePhaseByHp(MessageLog output) {
        while (phaseIndex + 1 < phases.length
                && getHealth() <= phases[phaseIndex + 1].gatePercent() * getMaxHealth()) {
            phaseIndex++;
            applyOnEnter(phases[phaseIndex], output);
        }
    }

    private void applyOnEnter(Phase phase, MessageLog output) {
        // ENRAGE on-enter added in Task 6.
    }

    private void executeMove(Move move, Character other, MessageLog output) {
        switch (move) {
            case STRIKE:
            default:
                this.attack(other, output);
                break;
        }
    }

    @Override
    public void levelingUp(MessageLog output) {
        // Bosses are constructed at literal stats; levelingUp is intentionally a no-op.
    }
}
