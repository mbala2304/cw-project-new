package uk.ac.bris.cs.scotlandyard.model.handlers;

import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Player;

import java.util.List;
import java.util.Set;

public interface IMoveHandler {
    Set<Move> handleMoves(GameSetup setup, List<Player> detectives, Player player, int source);
}
