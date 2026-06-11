package client.map.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RuleNotifications_Tests {
	
	  private RuleNotifications notifications;

	    @BeforeEach
	    public void setUp() {
	        notifications = new RuleNotifications();
	    }
	    
	    
	    @Test 
	    public void hasError_shouldReturnFlase() {
	    	assertFalse(notifications.hasError());
	    }
	    
	    @Test
	    public void addError_shouldSaveError() {
	    	notifications.addError("ERROR. No Fort exists");
	    	
	    	assertTrue(notifications.hasError());
	    	assertEquals(1, notifications.getErrors().size()); 
	    }
	    
	    // TODO: mock? 

}
