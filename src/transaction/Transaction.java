package transaction;

import java.util.Scanner;
import model.Account;
import manager.AccountManager;

public interface Transaction {
    void execute(Account account, Scanner sc);
}
