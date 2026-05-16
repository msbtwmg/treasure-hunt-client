package client.map.rules;

import client.map.model.ClientFullMap;
import client.map.model.ClientHalfMap;

public interface MapRule {
    void validate(ClientHalfMap map, RuleNotifications notifications);
}