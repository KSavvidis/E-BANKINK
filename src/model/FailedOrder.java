package model;

import java.time.LocalDate;

public class FailedOrder {
    private final int maxTries = 2;
    private int currentTry = 0;
    private StandingOrder order;
    private LocalDate lastAttemptDate = null;
    public FailedOrder(StandingOrder order) {
        this.order = order;
    }

    public LocalDate getLastAttemptDate() {
        return lastAttemptDate;
    }

    public void setLastAttemptDate(LocalDate lastAttemptDate) {
        this.lastAttemptDate = lastAttemptDate;
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
