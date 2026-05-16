package client.map.rules;

import client.map.MapHelper;
import client.map.model.ClientHalfMap;
import client.map.model.ClientHalfMapNode;
import client.map.model.EClientTerrain;
public class FortRule implements MapRule {

    @Override
    public void validate(ClientHalfMap map, RuleNotifications notifications) {
        long fortCount = map.getMapNodes().stream()
                .filter(ClientHalfMapNode::myFortPresent)
                .count();

        boolean fortOnGrass = map.getMapNodes().stream()
                .filter(ClientHalfMapNode::myFortPresent)
                .allMatch(node -> node.getTerrain() == EClientTerrain.Grass);

        if (fortCount != 1 || !fortOnGrass) {
            notifications.addError("Fort is placed on invalid terrain!");
        }
    }
}