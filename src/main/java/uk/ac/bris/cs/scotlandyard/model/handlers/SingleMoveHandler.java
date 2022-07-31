package uk.ac.bris.cs.scotlandyard.model.handlers;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SingleMoveHandler implements IMoveHandler{
    @Override
    public Set<Move> handleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
        Set<Move> sMoves = new HashSet<>();

        for(int destination1 : setup.graph.adjacentNodes(source)) {
            if(detectives.stream().noneMatch(x->x.location() == destination1)) {
                for (ScotlandYard.Transport t1 : setup.graph.edgeValueOrDefault(source, destination1, ImmutableSet.of())) {
                    if (player.has(t1.requiredTicket())) {
                        sMoves.add(new Move.SingleMove(player.piece(), source, t1.requiredTicket(), destination1));
                    }
                }

                if (player.has(ScotlandYard.Ticket.SECRET)
                        && !((setup.graph.edgeValueOrDefault(source, destination1, ImmutableSet.of(ScotlandYard.Transport.FERRY))).contains(ScotlandYard.Transport.FERRY))) {  // could use stream.none match, also set default value to ferry just in case
                    sMoves.add(new Move.SingleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, destination1));
                }
            }
        }
        return sMoves;
    }
}
