package client.map.placement;

import java.util.Collection;

import client.map.model.ClientFullMapNode;

public class FirstHalfPositionFinder {

    public FirstHalfPosition find(Collection<ClientFullMapNode> nodes) {

        int minX = nodes.stream()
                .mapToInt(ClientFullMapNode::getX)
                .min()
                .orElse(0);

        int maxX = nodes.stream()
                .mapToInt(ClientFullMapNode::getX)
                .max()
                .orElse(0);

        int minY = nodes.stream()
                .mapToInt(ClientFullMapNode::getY)
                .min()
                .orElse(0);

        int maxY = nodes.stream()
                .mapToInt(ClientFullMapNode::getY)
                .max()
                .orElse(0);

        // first half is at top-left / origin
        if (minX == 0 && maxX < 10 && minY == 0 && maxY < 5) {
            return FirstHalfPosition.ORIGIN;
        }

        // first half is on the right side
        if (minX >= 10) {
            return FirstHalfPosition.RIGHT;
        }

        // first half is on the bottom side
        if (minY >= 5) {
            return FirstHalfPosition.BOTTOM;
        }

        return FirstHalfPosition.NOTFOUND;
    }
}