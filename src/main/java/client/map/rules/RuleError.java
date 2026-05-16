package client.map.rules;

public class RuleError {

    private final String message;
    private final Exception reason;

    public RuleError(String message) {
        this(message, null);
    }

    public RuleError(String message, Exception reason) {
        this.message = message;
        this.reason = reason;
    }

    public String getMessage() {
        return message;
    }

    public Exception getReason() {
        return reason;
    }
}