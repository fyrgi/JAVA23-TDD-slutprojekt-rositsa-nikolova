import java.time.YearMonth;

public class Card {
    private String cardId;
    private String cardNumber;
    private String pin;
    private YearMonth expiryDate;
    private double balance;
    private int failedAttempts = 0;
    private boolean isLocked = false;
    public Card(String cardNumber, String pin, YearMonth expiryDate, double balance, boolean isLocked, int failedAttempts){
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.expiryDate = expiryDate;
        this.isLocked = isLocked;
        this.balance = balance;
        this.failedAttempts = failedAttempts;
    }

    public Card(){}
    public String getCardNumber() { return cardNumber; }
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
    public int getFailedAttempts() { return failedAttempts; }
    public boolean isLocked() { return isLocked; }
    public void lockCard() { this.isLocked = true; }
    public void incrementFailedAttempts() { this.failedAttempts = failedAttempts + 1; }
    public void resetFailedAttempts() { this.failedAttempts = 0; }

    public double getBalance() { return balance; }

    public void setBalance(double balance) { this.balance = balance; }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }
    public YearMonth getExpiryDate() {
        return expiryDate;
    }
}
