import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank();
        Scanner scanner = new Scanner(System.in);

        bank.addAccount(new Account("KZ111", "Алишер", 100000.0, 1234));
        bank.addAccount(new Account("KZ222", "Тимур", 200000.0, 4321));

        System.out.println("=== ДОБРО ПОЖАЛОВАТЬ В БАНКОВСКУЮ СИСТЕМУ ===");

        while (true) {
            System.out.println("\nГлавное меню:");
            System.out.println("1. Войти в существующий счет");
            System.out.println("2. Создать новый банковский счет");
            System.out.println("3. Выйти из программы");
            System.out.print("Выберите действие: ");

            String action = scanner.nextLine();

            if (action.equals("3") || action.equalsIgnoreCase("exit")) {
                System.out.println("Выход из программы. До свидания!");
                break;
            }

            if (action.equals("2")) {
                System.out.println("\n--- РЕГИСТРАЦИЯ НОВОГО СЧЕТА ---");
                System.out.print("Введите номер счета (например, KZ333): ");
                String accNum = scanner.nextLine();

                if (bank.getAccount(accNum) != null) {
                    System.out.println("Ошибка: Счет с таким номером уже существует!");
                    continue;
                }

                System.out.print("Введите имя владельца: ");
                String ownerName = scanner.nextLine();

                System.out.print("Введите начальный баланс (тенге): ");
                double initialDeposit = scanner.nextDouble();

                System.out.print("Установите 4-значный PIN-код (число): ");
                int pinCode = scanner.nextInt();
                scanner.nextLine();

                Account newAccount = new Account(accNum, ownerName, initialDeposit, pinCode);
                bank.addAccount(newAccount);
                System.out.println("Счет успешно создан! Номер счета: " + accNum);
                continue;
            }

            if (action.equals("1")) {
                System.out.print("Введите номер счета: ");
                String accNum = scanner.nextLine();

                Account currentAccount = bank.getAccount(accNum);
                if (currentAccount == null) {
                    System.out.println("Ошибка: Счет с таким номером не найден.");
                    continue;
                }

                if (currentAccount.isLocked()) {
                    System.out.println("Счет заблокирован из-за трехкратного неверного ввода PIN-кода.");
                    continue;
                }

                boolean isAuthenticated = false;
                for (int i = 0; i < 3; i++) {
                    System.out.print("Введите PIN-код: ");
                    int pin = scanner.nextInt();
                    scanner.nextLine();

                    if (currentAccount.validatePin(pin)) {
                        isAuthenticated = true;
                        break;
                    } else {
                        if (currentAccount.isLocked()) {
                            System.out.println("Счет заблокирован! Превышено количество попыток.");
                            break;
                        }
                        System.out.println("Неверный PIN! Осталось попыток: " + currentAccount.getFailedAttemptsLeft());
                    }
                }

                if (!isAuthenticated) continue;

                boolean sessionActive = true;
                while (sessionActive) {
                    System.out.println("\n--- ЛИЧНЫЙ КАБИНЕТ (" + currentAccount.getOwnerName() + ") ---");
                    System.out.println("1. Проверить баланс и долг");
                    System.out.println("2. Пополнить счет");
                    System.out.println("3. Перевод на другой счет");
                    System.out.println("4. Оформить кредит");
                    System.out.println("5. Погасить кредит");
                    System.out.println("6. История операций");
                    System.out.println("7. Выйти из аккаунта");
                    System.out.print("Выберите пункт меню: ");

                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 1:
                            System.out.println("Текущий баланс: " + currentAccount.getBalance() + " тенге");
                            System.out.println("Активный кредит (долг): " + currentAccount.getLoanDebt() + " тенге");
                            break;
                        case 2:
                            System.out.print("Введите сумму для пополнения: ");
                            double depAmount = scanner.nextDouble();
                            scanner.nextLine();
                            currentAccount.deposit(depAmount);
                            System.out.println("Счет успешно пополнен. Новый баланс: " + currentAccount.getBalance() + " тенге");
                            break;
                        case 3:
                            System.out.print("Введите номер счета получателя: ");
                            String targetAcc = scanner.nextLine();
                            System.out.print("Введите сумму перевода: ");
                            double trAmount = scanner.nextDouble();
                            scanner.nextLine();

                            if (bank.transfer(currentAccount.getAccountNumber(), targetAcc, trAmount)) {
                                System.out.println("Перевод успешно выполнен!");
                            } else {
                                System.out.println("Ошибка перевода (проверьте сумму, баланс или номер счета получателя).");
                            }
                            break;
                        case 4:
                            System.out.print("Введите сумму кредита: ");
                            double loanAmount = scanner.nextDouble();
                            scanner.nextLine();
                            if (currentAccount.takeLoan(loanAmount)) {
                                System.out.println("Кредит успешно одобрен и зачислен на баланс!");
                            }
                            break;
                        case 5:
                            System.out.println("Ваш текущий долг по кредиту: " + currentAccount.getLoanDebt() + " тенге");
                            System.out.print("Введите сумму для погашения: ");
                            double payAmount = scanner.nextDouble();
                            scanner.nextLine();
                            if (currentAccount.payLoan(payAmount)) {
                                System.out.println("Платеж прошел успешно! Остаток долга: " + currentAccount.getLoanDebt() + " тенге");
                            }
                            break;
                        case 6:
                            System.out.println("\n--- История транзакций ---");
                            for (String record : currentAccount.getTransactionHistory()) {
                                System.out.println("- " + record);
                            }
                            break;
                        case 7:
                            sessionActive = false;
                            System.out.println("Выход из аккаунта...");
                            break;
                        default:
                            System.out.println("Неверный выбор. Попробуйте снова.");
                    }
                }
            }
        }
        scanner.close();
    }
}