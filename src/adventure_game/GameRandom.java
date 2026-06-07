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
