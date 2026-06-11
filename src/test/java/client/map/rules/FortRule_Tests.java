package client.map.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import client.map.model.ClientHalfMap;
import client.map.model.ClientHalfMapNode;
import client.map.model.EClientTerrain;

public class FortRule_Tests {

    @Test
    public void validate_withOneFortOnGrass_shouldHaveNoErrors() {
        ClientHalfMap map = new ClientHalfMap(Set.of(
                node(0, 0, EClientTerrain.Grass, true),
                node(1, 0, EClientTerrain.Grass, false)));

        RuleNotifications notifications = new RuleNotifications();
        
        FortRule rule = new FortRule();

        rule.validate(map, notifications);

        assertFalse(notifications.hasError());
    }

    @Test
    public void validate_withoutFort_shouldHaveError() {
        ClientHalfMap map = new ClientHalfMap(Set.of(
                node(0, 0, EClientTerrain.Grass, false),
                node(1, 0, EClientTerrain.Grass, false)
        ));

        RuleNotifications notifications = new RuleNotifications();
        FortRule rule = new FortRule();

        rule.validate(map, notifications);

        assertTrue(notifications.hasError());
    }

    @Test
    public void validate_withFortOnWater_shouldHaveError() {
        ClientHalfMap map = new ClientHalfMap(Set.of(
                node(0, 0, EClientTerrain.Water, true),
                node(1, 0, EClientTerrain.Grass, false)
        ));

        RuleNotifications notifications = new RuleNotifications();
        FortRule rule = new FortRule();

        rule.validate(map, notifications);
        
        assertTrue(notifications.hasError());
    }
    
    private ClientHalfMapNode node(int x, int y, EClientTerrain terrain, boolean fort) {
        return new ClientHalfMapNode(
                x,
                y,
                fort,
                false,    
                terrain
        );
    }
}