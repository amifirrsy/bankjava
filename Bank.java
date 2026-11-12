import java.util.HashMap;
import java.util.Map;

public class Bank {
    private Map<String, Account> accounts = new HashMap<>();

    public void addAccount(Account account) {
        accounts.put(account.getAccountNumber(), account);
    }

    public Account getAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public boolean transfer(String fromAccNum, String toAccNum, double amount) {
        Account fromAcc = accounts.get(fromAccNum);
        Account toAcc = accounts.get(toAccNum);

        if (fromAcc != null && toAcc != null && fromAcc.withdraw(amount)) {
            toAcc.deposit(amount);
            fromAcc.addTransaction("Перевод пользователю " + toAcc.getOwnerName() + " (-" + amount + ")");
            toAcc.addTransaction("Получен перевод от " + fromAcc.getOwnerName() + " (+" + amount + ")");
            return true;
        }
        return false;
    }
}