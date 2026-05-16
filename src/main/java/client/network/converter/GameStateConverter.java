package client.network.converter;

import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.map.model.*;


import client.game.model.ClientGameState;
import client.game.model.EClientGameState;
import client.game.model.EClientMove;
import messagesbase.messagesfromclient.EMove;
import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.EFortState;
import messagesbase.messagesfromserver.EPlayerGameState;
import messagesbase.messagesfromserver.EPlayerPositionState;
import messagesbase.messagesfromserver.ETreasureState;
import messagesbase.messagesfromserver.FullMapNode;
import messagesbase.messagesfromserver.GameState;
import messagesbase.messagesfromserver.PlayerState;

public final class GameStateConverter {
	private static final Logger logger =  LoggerFactory.getLogger(GameStateConverter.class);

	
	private GameStateConverter() {}
	/**
	 * Converts the server GameState into a client version ClientGameState
	 *
	 * @param gameState server gamestate
	 * @param playerId current player's ID
	 * @return converted client game state
	 */
	
	public static ClientGameState toClientGameState(GameState gameState, String playerId) {

	    logger.debug("Converting GameState {}", gameState.getGameStateId());

	    // Map conversion
	    Set<ClientFullMapNode> nodes = gameState.getMap().getMapNodes().stream()
	            .map(GameStateConverter::toClientNode)
	            .collect(Collectors.toSet());

	    ClientFullMap fullMap = new ClientFullMap(nodes);

	    int x = -1;
	    int y = -1;

	    for (FullMapNode n : gameState.getMap().getMapNodes()) {
	        if (n.getPlayerPositionState() == EPlayerPositionState.MyPlayerPosition
	                || n.getPlayerPositionState() == EPlayerPositionState.BothPlayerPosition) {

	            x = n.getX();
	            y = n.getY();

	            logger.debug("My position found: ({}, {})", x, y);
	            break;
	        }
	    }

	    if (x == -1 || y == -1) {
	        logger.warn("My position not found in FullMap yet");
	    }

	    // PlayerState conversion
	    EClientGameState myGameState = null;
	    boolean hasTreasure = false;

	    for (PlayerState ps : gameState.getPlayers()) {
	        if (ps.getUniquePlayerID().equals(playerId)) {

	            myGameState = toClientGameState(ps.getState());
	            hasTreasure = ps.hasCollectedTreasure();

	            logger.debug("PlayerState converted");
	            break;
	        }
	    }
	    // TODO: check mistake with 2 hasTreasure, one should be when viisble other if collected
	    return new ClientGameState(
	            fullMap,
	            x,
	            y,
	            myGameState,
	            hasTreasure,
	            gameState.getGameStateId(),
	            hasTreasure
	    );
	}
	
    private static ClientFullMapNode toClientNode(FullMapNode n) {
        return new ClientFullMapNode(
            n.getX(),
            n.getY(),
            toClientTerrain(n.getTerrain()),
            toClientFortState(n.getFortState()),
            toClientTreasureState(n.getTreasureState()),
            toClientPositionState(n.getPlayerPositionState())
        ); 
    }
    
	public static EClientTerrain toClientTerrain(ETerrain terrain) {
	    return switch (terrain) {
	        case Grass -> EClientTerrain.Grass;
	        case Water -> EClientTerrain.Water;
	        case Mountain -> EClientTerrain.Mountain;
	    };
	}

   
	private static EClientFortState toClientFortState(EFortState fortState) {
	    return switch (fortState) {
	        case MyFortPresent    -> EClientFortState.MyFortPresent;
	        case EnemyFortPresent -> EClientFortState.EnemyFortPresent;
	        case NoOrUnknownFortState -> EClientFortState.NoOrUnknownFortState;
	    };
	}
	
	private static EClientTreasureState toClientTreasureState(ETreasureState treasureState) {
		   return switch (treasureState) {
	        case MyTreasureIsPresent -> EClientTreasureState.MyTreasureIsPresent;
	        case NoOrUnknownTreasureState -> EClientTreasureState.NoOrUnknownTreasureState;
	    };
	}
	
	private static EClientPositionState toClientPositionState(EPlayerPositionState positionState) {
		   return switch (positionState) {
	        case BothPlayerPosition -> EClientPositionState.BothPlayerPosition;
	        case MyPlayerPosition -> EClientPositionState.MyPlayerPosition;
	        case EnemyPlayerPosition -> EClientPositionState.EnemyPlayerPosition;
	        case NoPlayerPresent -> EClientPositionState.NoPlayerPresent;
	    };
	}
	
	private static EClientGameState toClientGameState(EPlayerGameState state) {
	    return switch (state) {
	        case MustAct -> EClientGameState.MustAct;
	        case Won     -> EClientGameState.Won;
	        case Lost    -> EClientGameState.Lost;
	        case MustWait -> EClientGameState.MustWait;
	    };
	}
	
    public static EClientMove toClientMove(EMove move) {
        return switch (move) {
            case Up    -> EClientMove.Up;
            case Down  -> EClientMove.Down;
            case Left  -> EClientMove.Left;
            case Right -> EClientMove.Right;
        };
    }

    public static EMove toServerMove(EClientMove move) {
        return switch (move) {
            case Up    -> EMove.Up;
            case Down  -> EMove.Down;
            case Left  -> EMove.Left;
            case Right -> EMove.Right;
        };
    }
    // TODO: better method name vllt  toServerPlayerMove, createPlayerMove. 
    public static PlayerMove toServerClientMove(String playerId, EClientMove move) {
        final EMove movement = toServerMove(move);
        return PlayerMove.of(playerId, movement);
    }
}

