package client.network.converter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.map.HalfMapCreator.Position;
import client.map.model.ClientHalfMapNode;
import client.map.model.EClientTerrain;

import messagesbase.messagesfromclient.ETerrain;
import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;

public class HalfMapConverter {

    private static final Logger logger = LoggerFactory.getLogger(HalfMapConverter.class);

    public static PlayerHalfMap toServerHalfMap( String playerId, Collection<ClientHalfMapNode> nodes) {

        logger.debug("Converting {} client nodes to server nodes", nodes.size());

        Set<PlayerHalfMapNode> serverNodes = nodes.stream()
                .map(n -> new PlayerHalfMapNode(
                        n.getX(),
                        n.getY(),
                        n.myFortPresent(),
                        toServerTerrain(n.getTerrain())
                ))
                .collect(Collectors.toSet());

        logger.debug("Converted {} server nodes", serverNodes.size());

        return new PlayerHalfMap(playerId, serverNodes);
    }

    private static ETerrain toServerTerrain(EClientTerrain terrain) {
        return switch (terrain) {
            case Grass -> ETerrain.Grass;
            case Water -> ETerrain.Water;
            case Mountain -> ETerrain.Mountain;
        };
    }
}