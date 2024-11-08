import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class Bank implements BankInterface {

    private Map<String, Card> cardOfCustomer = new HashMap<>();
    private double balance;
    private int failedAttempts = 0;
    private boolean isLocked = false;

    public Card getCardById(String id) {
        return cardOfCustomer.get(id);
    }

    public void setPin(String cardId, String pin) { cardOfCustomer.get(cardId).setPin(pin);}
    public Bank(){}
    public boolean isCardValid(String cardId) {
        return true;
    }
    public int getFailedAttempts(String cardId) { return failedAttempts; }
    public void setFailedAttempts(String cardId, int attempts) { this.failedAttempts = attempts; };
    public void incrementFailedAttempts(String cardId) { this.failedAttempts = failedAttempts + 1; }
    public boolean isLocked(String cardId) { return isLocked; }
    public void lockCard(String cardId) { this.isLocked = true; }
    public void resetFailedAttempts(String cardId) { this.failedAttempts = 0; }

    public double getBalance(String cardId) { return balance; }

    public void setBalance(double balance) { this.balance = balance; }

    public static String getBankName() {
        return "Swedbank";
    }
}
