package transaction;

public interface ScheduledTransaction extends Transaction {
    boolean execute();
}
