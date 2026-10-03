package rules;

public class RuleResult {
    private final boolean triggered;
    private final int points;
    private final String reason;

    public RuleResult(boolean triggered, int points, String reason) {
        this.triggered = triggered;
        this.points = points;
        this.reason = reason;
    }

    public static RuleResult notTriggered() {
        return new RuleResult(false, 0, "");
    }

    public boolean isTriggered() { return triggered; }
    public int getPoints() { return points; }
    public String getReason() { return reason; }
}