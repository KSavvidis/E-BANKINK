package manager;

import model.Account;
import model.BankAccount;
import model.Bill;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class TransactionManager {
    private  AccountManager accountManager;
    private  BillManager billManager;
    private final StatementManager statementManager = new StatementManager();
    private final String accountsFilePath = "data/accounts/accounts.csv";

    public TransactionManager(AccountManager accountManager, BillManager billManager) {
        this.accountManager = accountManager;
        this.billManager = billManager;
    }

    public TransactionManager() {
    }

    public void performDeposit(String iban, Scanner sc) {
        Account account = accountManager.findByIban(iban);
        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        System.out.print("Enter amount to deposit: ");
        double amount = sc.nextDouble();
        sc.nextLine(); // Consume newline

        if (amount <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }

        account.setBalance(account.getBalance() + amount);
        updateAccountInFile(account);

        statementManager.recordTransaction(account, "ATM Deposit", amount, LocalDate.now());

        System.out.printf("Deposited %.2f successfully. New balance: %.2f\n", amount, account.getBalance());
    }

    public void performWithdraw(String iban, Scanner sc) {
        Account account = accountManager.findByIban(iban);
        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        System.out.print("Enter amount to withdraw: ");
        double amount = sc.nextDouble();
        sc.nextLine(); // Consume newline

        if (amount <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }

        account.setBalance(account.getBalance() - amount);
        updateAccountInFile(account);

        statementManager.recordTransaction(account, "ATM Withdraw", amount, LocalDate.now());

        System.out.printf("Withdrew %.2f successfully. New balance: %.2f\n", amount, account.getBalance());
    }

    public void performTransfer(Account senderAccount, Scanner sc) {

        if (senderAccount == null) {
            System.out.println("Sender account not found.");
            return;
        }

        System.out.print("Enter recipient IBAN: ");
        String recipientIban = sc.nextLine();

        if (senderAccount.getIban().equals(recipientIban)) {
            System.out.println("Cannot transfer to the same account.");
            return;
        }

        Account recipient = accountManager.findByIban(recipientIban);
        if (recipient == null) {
            System.out.println("Recipient account not found.");
            return;
        }

        System.out.print("Enter amount to transfer: ");
        double amount = sc.nextDouble();
        sc.nextLine();


        if (amount <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }

        if (senderAccount.getBalance() < amount) {
            System.out.println("Insufficient balance.");
            return;
        }

        System.out.print("Enter transfer reason: ");
        String reason = sc.nextLine();

        senderAccount.setBalance(senderAccount.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount);

        updateAccountInFile(senderAccount);
        updateAccountInFile(recipient);

        statementManager.recordTransaction(senderAccount,  "Transfer to " + recipientIban + " - " + reason, -amount, LocalDate.now());
        statementManager.recordTransaction(recipient, "Transfer from " + senderAccount.getIban() + " - " + reason, amount, LocalDate.now());

        System.out.printf("Transferred %.2f successfully to %s.\n", amount, recipientIban);
        System.out.printf("Sender new balance: %.2f\n", senderAccount.getBalance());
    }
// kati na kanoume me autes edw
    public void performOrderTransfer(Account senderAccount, Account receiverAccount, double amount, String description, LocalDate currentDate) {
        if (senderAccount == null) {
            System.out.println("Sender account not found.");
            return;
        }

        if (receiverAccount == null) {
            System.out.println("Recipient account not found.");
            return;
        }

        if (senderAccount.getIban().equals(receiverAccount.getIban())) {
            System.out.println("Cannot transfer to the same account.");
            return;
        }

        senderAccount.setBalance(senderAccount.getBalance() - amount);
        receiverAccount.setBalance(receiverAccount.getBalance() + amount);

        updateAccountInFile(senderAccount);
        updateAccountInFile(receiverAccount);

        statementManager.recordTransaction(senderAccount, "Transfer to " + receiverAccount.getIban() + " - " + description, -amount, currentDate);
        statementManager.recordTransaction(receiverAccount, "Transfer from " + senderAccount.getIban() + " - " + description, amount, currentDate);

        System.out.printf("Transferred %.2f successfully to %s.\n", amount, receiverAccount.getIban());
        System.out.printf("Sender new balance: %.2f\n", senderAccount.getBalance());
    }

    public boolean performOrderTransfers(Account senderAccount, Account receiverAccount, double amount, String description, LocalDate currentDate) {
        if (senderAccount == null) {
            System.out.println("Sender account not found.");
            return false;
        }

        if (receiverAccount == null) {
            System.out.println("Recipient account not found.");
            return false;
        }

        if (senderAccount.getIban().equals(receiverAccount.getIban())) {
            System.out.println("Cannot transfer to the same account.");
            return false;
        }

        if(senderAccount.getBalance() < amount) {
            System.out.println("Transfer from: " + senderAccount.getIban() + " to " + receiverAccount.getIban() + " failed due to insufficient balance.");
            return false;
        }
        senderAccount.setBalance(senderAccount.getBalance() - amount);
        receiverAccount.setBalance(receiverAccount.getBalance() + amount);

        updateAccountInFile(senderAccount);
        updateAccountInFile(receiverAccount);

        statementManager.recordTransaction(senderAccount, "Transfer to " + receiverAccount.getIban() + " - " + description, -amount, currentDate);
        statementManager.recordTransaction(receiverAccount, "Transfer from " + senderAccount.getIban() + " - " + description, amount, currentDate);

        System.out.printf("Transferred %.2f successfully to %s.\n", amount, receiverAccount.getIban());
        System.out.printf("Sender new balance: %.2f\n", senderAccount.getBalance());
        return true;
    }

    public boolean performOrderPayment(Account chargeAccount, Bill bill, LocalDate currentDate) {
        if (chargeAccount.getBalance() < bill.getAmount()) {
            System.out.println("The payment for bill: " + bill.getPaymentCode() + " failed due to insufficient balance.");
            return false;
        }

        chargeAccount.setBalance(chargeAccount.getBalance() - bill.getAmount());
        System.out.printf("Payment of %.2f for bill %s completed. Updated account balance: %.2f\n",
                bill.getAmount(), bill.getPaymentCode(), chargeAccount.getBalance());

        billManager.loadBillsFromIssuedToPaidFile(bill.getPaymentCode());
        updateAccountInFile(chargeAccount);

        statementManager.recordTransaction(chargeAccount, "Bill Payment", -bill.getAmount(), currentDate);
        return true;
    }

    public void performPayment(String iban, String vat, Scanner sc) {
        List<Bill> pendingBills = billManager.getBillsForCustomer(vat);

        if (pendingBills.isEmpty()) {
            System.out.println("You have no bills to pay.");
            return;
        }

        System.out.println("Pending bills:");
        int i = 0;
        for (Bill bill: pendingBills) {
            System.out.printf("%d. RF: %s | Amount: %.2f | Due: %s | Issuer: %s\n",
                    i + 1, bill.getPaymentCode(), bill.getAmount(), bill.getDueDate(), bill.getIssuer().getVAT());
            i++;
        }

        System.out.print("Select bill by RF: ");
        String rf = sc.next();
        sc.nextLine();
        Bill selectedBill = null;
        for(Bill bill: pendingBills) {
            if(rf.equals(bill.getPaymentCode())) {
                selectedBill = bill;
            }
        }
        if(selectedBill == null) {
            System.out.println("Bill not found.");
            return;
        }
        Account selectedAccount = accountManager.findByIban(iban);
        if (selectedAccount == null) {
            System.out.println("No account selected. Aborting payment.");
            return;
        }

        if (selectedAccount.getBalance() < selectedBill.getAmount()) {
            System.out.println("Insufficient funds in the selected account.");
            return;
        }

        selectedAccount.setBalance(selectedAccount.getBalance() - selectedBill.getAmount());
        System.out.printf("Payment of %.2f for bill %s completed. Updated account balance: %.2f\n",
                selectedBill.getAmount(), selectedBill.getPaymentCode(), selectedAccount.getBalance());

        billManager.loadBillsFromIssuedToPaidFile(rf);
        updateAccountInFile(selectedAccount);

        statementManager.recordTransaction(selectedAccount, "Bill Payment", -selectedBill.getAmount(), LocalDate.now());
    }

    private void updateAccountInFile(Account updatedAccount) {
        List<String> updatedLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(accountsFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("iban:" + updatedAccount.getIban())) {
                    updatedLines.add(updatedAccount.marshal());
                } else {
                    updatedLines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return;
        }

        try (PrintWriter writer = new PrintWriter(accountsFilePath)) {
            for (String l : updatedLines) {
                writer.println(l);
            }
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }
}
