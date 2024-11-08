import java.time.YearMonth;

public interface BankInterface {
    public Card getCardById(String id);
    public boolean isCardValid(String cardId);

    public int getFailedAttempts(String cardId);
    public boolean isLocked(String cardId);
    public void lockCard(String cardId);
    public void incrementFailedAttempts(String cardId);
    public void resetFailedAttempts(String cardId);
    public double getBalance(String cardId);
    public void setBalance(double balance);
    public void setPin(String cardId, String pin);
    void setFailedAttempts(String cardId, int attempts);
}
