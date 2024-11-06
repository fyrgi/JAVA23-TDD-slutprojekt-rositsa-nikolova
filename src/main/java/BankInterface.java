public interface BankInterface {
    public Account getAccountByCardId(String id);
    public boolean isCardLocked(String userId);
}
