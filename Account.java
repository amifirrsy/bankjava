import java.util.ArrayList;
import java.util.List;

public class Account {
    private String accountNumber;
    private String ownerName;
    private double balance;
    private int pinCode;
    private int failedAttempts = 0;
    private boolean isLocked = false;
    private List<String> transactionHistory;
    
    // Новое поле для кредита
    private double loanDebt = 0.0;

    public Account(String accountNumber, String ownerName, double initialDeposit, int pinCode) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = initialDeposit;
        this.pinCode = pinCode;
        this.transactionHistory = new ArrayList<>();
        addTransaction("Счет создан. Начальный баланс: " + initialDeposit + " тенге");
    }

    public boolean validatePin(int inputPin) {
        if (isLocked) {
            return false;
        }
        if (this.pinCode == inputPin) {
            failedAttempts = 0;
            return true;
        } else {
            failedAttempts++;
            if (failedAttempts >= 3) {
                isLocked = true;
            }
            return false;
        }
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            addTransaction("Пополнение: +" + amount + " тенге");
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            addTransaction("Снятие: -" + amount + " тенге");
            return true;
        }
        return false;
    }

    // --- Методы для работы с кредитом ---
    
    public boolean takeLoan(double amount) {
        if (amount <= 0) return false;
        
        // Ограничение: максимальный кредит не может превышать баланс * 5 (или минимум 50 000 тенге)
        double maxAllowedLoan = Math.max(balance * 5, 50000.0);
        
        if (loanDebt > 0) {
            System.out.println("Ошибка: У вас уже есть активный кредит. Сначала погасите текущий долг.");
            return false;
        }
        
        if (amount > maxAllowedLoan) {
            System.out.println("Ошибка: Запрашиваемая сумма превышает ваш кредитный лимит (" + maxAllowedLoan + " тенге).");
            return false;
        }

        loanDebt += amount;
        balance += amount; // Деньги зачисляются на основной счет
        addTransaction("Оформлен кредит: +" + amount + " тенге");
        return true;
    }

    public boolean payLoan(double amount) {
        if (amount <= 0) return false;
        
        if (loanDebt <= 0) {
            System.out.println("У вас нет активных кредитов.");
            return false;
        }

        double payment = Math.min(amount, loanDebt); // Нельзя заплатить больше, чем должен
        
        if (balance < payment) {
            System.out.println("Ошибка: Недостаточно средств на балансе для погашения выбранной суммы.");
            return false;
        }

        balance -= payment;
        loanDebt -= payment;
        addTransaction("Погашение кредита: -" + payment + " тенге");
        return true;
    }

    public void addTransaction(String record) {
        transactionHistory.add(record);
    }

    // Геттеры
    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName() { return ownerName; }
    public double getBalance() { return balance; }
    public double getLoanDebt() { return loanDebt; }
    public boolean isLocked() { return isLocked; }
    public int getFailedAttemptsLeft() { return 3 - failedAttempts; }
    public List<String> getTransactionHistory() { return transactionHistory; }
}