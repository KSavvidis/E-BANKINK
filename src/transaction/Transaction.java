package transaction;

import java.util.Scanner;
import model.Account;

public interface Transaction {
    void execute(Account account, Scanner sc);
}
