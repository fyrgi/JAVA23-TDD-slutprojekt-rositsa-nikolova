import java.time.YearMonth;

public class Card {
    private String cardNumber;
    private String pin;
    private YearMonth expiryDay;
    private String cvc;
    private int failedAttempts = 0;
    private boolean isLocked = false;
    public Card(String cardNumber, String pin, YearMonth expieryDate, String cvc){
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.expiryDay = expieryDate;
        this.cvc = cvc;
    }

    public Card(){}
    public String getCardNumber() { return cardNumber; }
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
    public int getFailedAttempts() { return failedAttempts; }
    public boolean isLocked() { return isLocked; }
    public void lockCard() { this.isLocked = true; }
    public void incrementFailedAttempts() { this.failedAttempts++; }
    public void resetFailedAttempts() { this.failedAttempts = 0; }
}
