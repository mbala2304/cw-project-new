package uk.ac.bris.cs.scotlandyard.model.handlers;

import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DoubleMoveHandler extends SingleMoveHandler{
    @Override
    public Set<Move> handleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
            HashSet<Move> dMoves = new HashSet<>();
            ArrayList<Move> firstM = new ArrayList<>(super.handleMoves(setup, detectives, player, source));
            HashSet<Move> secondM;
            if(/*player.isMrX() &&*/ player.has(ScotlandYard.Ticket.DOUBLE)) {
                for (Move firstSM : firstM){
                    Move.SingleMove firstSingleMove = (Move.SingleMove)firstSM;
                    secondM = new HashSet<>(super.handleMoves(setup, detectives, player, firstSingleMove.destination));
                    for (Move secondSM : secondM){
                        Move.SingleMove secondSingleMove = (Move.SingleMove)secondSM;

                        if(secondSingleMove.ticket == firstSingleMove.ticket && player.hasAtLeast(secondSingleMove.ticket, 2)){
                            dMoves.add(new Move.DoubleMove(player.piece(), source, firstSingleMove.ticket, firstSingleMove.destination, secondSingleMove.ticket, secondSingleMove.destination));
                        }
                        else if(secondSingleMove.ticket != firstSingleMove.ticket){
                            dMoves.add(new Move.DoubleMove(player.piece(), source, firstSingleMove.ticket, firstSingleMove.destination, secondSingleMove.ticket, secondSingleMove.destination));
                        }
                    }
                }
            }
            return dMoves;
    }
}
