package adventure_game;

/**
 * A phased boss. Is-an {@link NPC}, so GameState's enemy.takeTurn(...) drives it
 * polymorphically. Constructed at LITERAL stats — never call levelingUp() on a
 * Boss (the reference levels bosses 3x, which inflates Faucci to ~327k = unwinnable).
 */
public class Boss extends NPC {

    static final double SLAM_MULTIPLIER = 1.8;
    static final int SUMMON_TURNS = 3;
    static final double SUMMON_CHIP_FRACTION = 0.5;
    static final int SWARM_HITS = 3;
    static final double SWARM_HIT_FRACTION = 0.5;
    static final double HEAL_FRACTION = 0.03;
    static final double ENRAGE_MULTIPLIER = 1.5;

    private boolean enraged = false;

    private final Phase[] phases;   // index 0 = phase 1 (gate 1.0), hardest gate last
    private int phaseIndex = 0;
    private int turnInFight = 0;
    private int hordeTurns = 0;

    public Boss(String name, int health, int level, int baseDamage, Phase[] phases) {
        super(name, health, level, baseDamage);
        if (phases == null || phases.length == 0) {
            throw new IllegalArgumentException("a boss needs at least one phase");
        }
        this.phases = phases.clone();
    }

    /** Remaining turns the summoned horde will chip the target. */
    public int activeHordeTurns() { return hordeTurns; }

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
        resolveHorde(other, output);
        Move move = phases[phaseIndex].moveForTurn(turnInFight);
        executeMove(move, other, output);
        turnInFight++;
    }

    private void resolveHorde(Character other, MessageLog output) {
        if (hordeTurns > 0) {
            int chip = (int) Math.ceil(getEffectiveDamage() * SUMMON_CHIP_FRACTION);
            other.modifyHealth(-chip);
            hordeTurns--;
            output.append("The summoned horde gnaws at " + other.getName()
                    + " for " + chip + " damage.");
        }
    }

    private void advancePhaseByHp(MessageLog output) {
        while (phaseIndex + 1 < phases.length
                && getHealth() <= phases[phaseIndex + 1].gatePercent() * getMaxHealth()) {
            phaseIndex++;
            applyOnEnter(phases[phaseIndex], output);
        }
    }

    @Override
    public int getEffectiveDamage() {
        int base = getBaseDamage();
        return enraged ? (int) (base * ENRAGE_MULTIPLIER) : base;
    }

    private void applyOnEnter(Phase phase, MessageLog output) {
        if (phase.onEnter() == Move.ENRAGE && !enraged) {
            enraged = true;
            output.append(getName() + " ENRAGES! Its blows now land far harder.");
        }
    }

    private void executeMove(Move move, Character other, MessageLog output) {
        switch (move) {
            case SLAM:
                this.setTempDamageBuff(SLAM_MULTIPLIER);
                this.attack(other, output);
                break;
            case SUMMON:
                // Re-summoning refreshes the horde window (resets, not stacks).
                hordeTurns = SUMMON_TURNS;
                output.append(getName() + " summons a horde from the dark!");
                break;
            case SWARM:
                for (int i = 0; i < SWARM_HITS; i++) {
                    this.setTempDamageBuff(SWARM_HIT_FRACTION);
                    this.attack(other, output);
                }
                break;
            case HEAL: {
                int heal = (int) Math.ceil(getMaxHealth() * HEAL_FRACTION);
                this.modifyHealth(heal);
                output.append(getName() + " regenerates " + heal + " health!");
                break;
            }
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
