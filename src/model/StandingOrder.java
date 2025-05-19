package model;


import java.time.LocalDate;

public class StandingOrder {
    private String standingOrderID;
    private String standingOrderName;
    private LocalDate standingOrderTime;
    private String standingOrderDescription;

    public StandingOrder(String standingOrderName, LocalDate standingOrderTime, String standingOrderDescription) {
        this.standingOrderName = standingOrderName;
        this.standingOrderTime = standingOrderTime;
        this.standingOrderDescription = standingOrderDescription;
        createStandingOrderID();
    }

    private void createStandingOrderID() {
    }
}
