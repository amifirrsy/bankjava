import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ServerApp {
    private static Bank bank = new Bank();

    public static void main(String[] args) throws IOException {
        // Инициализация стартовых счетов
        bank.addAccount(new Account("KZ111", "Алишер", 50000.0, 1234));
        bank.addAccount(new Account("KZ222", "Тимур", 20000.0, 4321));

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Раздача главной страницы
        server.createContext("/", exchange -> {
            byte[] response = Files.readAllBytes(Paths.get("src/index.html"));
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        });

        // API входа
        server.createContext("/api/login", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes());
            Map<String, String> data = parseJson(body);
            Account acc = bank.getAccount(data.get("accountNumber"));

            String jsonResponse;
            if (acc == null || acc.isLocked() || !acc.validatePin(Integer.parseInt(data.get("pin")))) {
                jsonResponse = "{\"success\":false, \"message\":\"Неверный счет, PIN или счет заблокирован\"}";
            } else {
                jsonResponse = "{\"success\":true, " + accountToJson(acc) + "}";
            }
            sendJson(exchange, jsonResponse);
        });

        // API регистрации
        server.createContext("/api/register", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes());
            Map<String, String> data = parseJson(body);
            String accNum = data.get("accountNumber");

            if (bank.getAccount(accNum) != null) {
                sendJson(exchange, "{\"success\":false, \"message\":\"Счет уже существует\"}");
                return;
            }

            Account newAcc = new Account(
                accNum, 
                data.get("ownerName"), 
                Double.parseDouble(data.get("balance")), 
                Integer.parseInt(data.get("pin"))
            );
            bank.addAccount(newAcc);
            sendJson(exchange, "{\"success\":true, \"message\":\"Счет успешно создан\"}");
        });

        // API операций (Пополнение, Перевод, Кредит)
        server.createContext("/api/action", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes());
            Map<String, String> data = parseJson(body);
            Account acc = bank.getAccount(data.get("accountNumber"));
            String action = data.get("action");
            boolean success = true;

            if ("deposit".equals(action)) {
                acc.deposit(Double.parseDouble(data.get("amount")));
            } else if ("transfer".equals(action)) {
                success = bank.transfer(acc.getAccountNumber(), data.get("targetAccount"), Double.parseDouble(data.get("amount")));
            } else if ("loan".equals(action)) {
                success = acc.takeLoan(Double.parseDouble(data.get("amount")));
            } else if ("payLoan".equals(action)) {
                success = acc.payLoan(Double.parseDouble(data.get("amount")));
            }

            String jsonResponse = success ? "{\"success\":true, " + accountToJson(acc) + "}" : "{\"success\":false, \"message\":\"Ошибка выполнения операции\"}";
            sendJson(exchange, jsonResponse);
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Сервер запущен! Откройте в браузере: http://localhost:8080");
    }

    private static void sendJson(HttpExchange exchange, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, json.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes());
        os.close();
    }

    // Простой парсер для JSON (чтобы не подключать сторонние библиотеки)
    private static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.replaceAll("[{}\"]", "");
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
        return map;
    }

    private static String accountToJson(Account acc) {
        StringBuilder hist = new StringBuilder("[");
        for (int i = 0; i < acc.getTransactionHistory().size(); i++) {
            hist.append("\"").append(acc.getTransactionHistory().get(i)).append("\"");
            if (i < acc.getTransactionHistory().size() - 1) hist.append(",");
        }
        hist.append("]");

        return "\"account\": {" +
                "\"accountNumber\":\"" + acc.getAccountNumber() + "\"," +
                "\"ownerName\":\"" + acc.getOwnerName() + "\"," +
                "\"balance\":" + acc.getBalance() + "," +
                "\"loanDebt\":" + acc.getLoanDebt() + "," +
                "\"transactionHistory\":" + hist + "}";
    }
}