package adventure_game;

/**
 * Builds the four bosses at their literal final stats (no levelingUp) with the
 * Phase 2b-2 phase patterns. Room mapping matches the original source/map lore:
 * 10=HiveMind, 19=Zombie Nest, 22=RatKing, 24=Faucci (cure room).
 */
public final class BossFactory {

    private BossFactory() {}

    public static Boss forRoom(int room) {
        switch (room) {
            case 10: return new Boss("HiveMind",    1000,  6, 30, hiveMindPhases());
            case 19: return new Boss("Zombie Nest", 5000, 10, 50, zombieNestPhases());
            case 22: return new Boss("RatKing",     7500, 15, 50, ratKingPhases());
            case 24: return new Boss("Faucci",     20000, 20, 50, faucciPhases());
            default: throw new IllegalArgumentException("No boss defined for room " + room);
        }
    }

    private static Phase[] hiveMindPhases() {
        return new Phase[]{
            new Phase(1.00, null, new Move[]{Move.STRIKE, Move.STRIKE, Move.SLAM}),
            new Phase(0.66, null, new Move[]{Move.STRIKE, Move.SUMMON, Move.SLAM}),
            new Phase(0.33, null, new Move[]{Move.SUMMON, Move.SLAM, Move.STRIKE, Move.SLAM}),
        };
    }

    private static Phase[] zombieNestPhases() {
        return new Phase[]{
            new Phase(1.00, null, new Move[]{Move.STRIKE, Move.SWARM, Move.STRIKE}),
            new Phase(0.66, null, new Move[]{Move.SWARM, Move.SLAM, Move.STRIKE}),
            new Phase(0.33, null, new Move[]{Move.SWARM, Move.SLAM, Move.SWARM, Move.SLAM}),
        };
    }

    private static Phase[] ratKingPhases() {
        return new Phase[]{
            new Phase(1.00, null,        new Move[]{Move.STRIKE, Move.STRIKE, Move.SLAM}),
            new Phase(0.66, null,        new Move[]{Move.STRIKE, Move.SLAM, Move.STRIKE, Move.SLAM}),
            new Phase(0.33, Move.ENRAGE, new Move[]{Move.SLAM, Move.STRIKE, Move.SLAM}),
        };
    }

    private static Phase[] faucciPhases() {
        return new Phase[]{
            new Phase(1.00, null, new Move[]{Move.STRIKE, Move.SLAM, Move.STRIKE}),
            new Phase(0.66, null, new Move[]{Move.STRIKE, Move.HEAL, Move.SLAM}),
            new Phase(0.33, null, new Move[]{Move.HEAL, Move.SLAM, Move.STRIKE, Move.SLAM}),
        };
    }
}
