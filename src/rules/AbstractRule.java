package rules;

public abstract class AbstractRule implements FraudRule {
    private final String name;
    private final int weight; // how many risk points this rule adds

    protected AbstractRule(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    @Override public String getName() { return name; }
    protected int getWeight() { return weight; }
}