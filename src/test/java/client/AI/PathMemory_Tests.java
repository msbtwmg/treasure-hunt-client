package client.AI;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PathMemory_Tests {

	 @Test
	    public void updatePosition_shouldMarkVisitedPosition() {
	        PathMemory memory = new PathMemory();
	        Position position = new Position(1, 2);

	        memory.updatePosition(position);

	        assertTrue(memory.visited(position));
	    }
	 

	 @Test
	    public void addUnreachableTarget_shouldRememberTarget() {
	        PathMemory memory = new PathMemory();
	        Position position = new Position(3, 4) ;

	        memory.addUnreachableTarget(position);
	        assertTrue(memory.isUnreachableTarget(position));
	    }
	 
	 
	 @Test
	    public void updatePosition_withSameFivePositions_shouldBeStuck() {
	        PathMemory memory = new PathMemory();
	        Position position = new Position(1, 2);
	        
	        memory.updatePosition(position);
	        memory.updatePosition(position);
	        memory.updatePosition(position);
	        memory.updatePosition(position);
	        memory.updatePosition(position);
	        memory.updatePosition(position);
	        memory.updatePosition(position);

	        assertTrue(memory.isStuck());
	    }
	 
	 
	 @Test
	 public void resetStuckCounter_shouldResetStuckState() {
	     PathMemory memory = new PathMemory();
	     Position position = new Position(1, 2);

  	     memory.updatePosition(position);
         memory.updatePosition(position);
         memory.updatePosition(position);
         memory.updatePosition(position);
         memory.updatePosition(position);
         memory.updatePosition(position);
         memory.updatePosition(position);

	     memory.resetStuckCounter();
	     assertFalse(memory.isStuck());
	 }
	 

}
