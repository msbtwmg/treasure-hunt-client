package client.map.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Collects rule violations.
 */
public class RuleNotifications {

    private List<RuleError> errors = new ArrayList<>();

  
    public void addError(String message) {
        errors.add(new RuleError(message));
    }

  
    public void addError(String message, Exception reason) {
        errors.add(new RuleError(message, reason));
    }


    public boolean hasError() {
        return !errors.isEmpty();
    }
    
    public List<RuleError> getErrors() {
        return Collections.unmodifiableList(errors);
    }
    
    /**
     * Error message combination
     */
    public String errorInformation() {
        return errors.stream()
                .map(RuleError::getMessage)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
