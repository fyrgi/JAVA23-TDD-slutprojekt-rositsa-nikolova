public interface BankInterface {
    public Card getCardById(String id);
    public boolean isCardValid(String userId);

    //public double currentBalance(String cardId);
}
