package uk.ac.bris.cs.scotlandyard.model.handlers;

import ch.qos.logback.core.util.EnvUtil;
import com.sun.webkit.network.data.Handler;
import uk.ac.bris.cs.scotlandyard.model.GameSetup;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Player;

import java.util.List;
import java.util.Set;

public class HandlerFactory {
    private static HandlerFactory handlerFactory;

    private HandlerFactory(){

    }
    public synchronized static HandlerFactory getInstance(){
        if(null == handlerFactory)
            handlerFactory = new HandlerFactory();
        return handlerFactory;
    }

    public Set<Move> makeMoves(Enum moves, GameSetup setup, List<Player> detectives, Player player, int source){
        IMoveHandler moveHandler;
        if(null != moves && moves == TypesOfMove.SINGLE){
            moveHandler = new SingleMoveHandler();
            return moveHandler.handleMoves(setup, detectives,player,source);
        }else if(null != moves && moves == TypesOfMove.DOUBLE){
            moveHandler = new DoubleMoveHandler();
            return moveHandler.handleMoves(setup, detectives,player,source);
        }
        return null;
    }
}
