package model;

public class FailedOrder {
    private final int maxTries = 4;
    private int currentTry = 0;
    private StandingOrder order;

    public FailedOrder(StandingOrder order) {
        this.order = order;
    }

    public void resetCurrentTry(){
        currentTry = 0;
    }
    public void increaseCurrentTry() {
        this.currentTry++;
    }

    public int getCurrentTry() {
        return currentTry;
    }
    public StandingOrder getOrder() {
        return order;
    }

    public boolean OverFailedAttempts(){
        return maxTries <= currentTry;
    }
}
