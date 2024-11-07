public class ATM {
    private Bank bank = new Bank();

    private Card currentCard;

    public ATM(Bank bank) {
        this.bank = bank;
    }

    public boolean insertCard(String cardId) {
        currentCard = bank.getCardById(cardId);
        if(bank.isCardValid(cardId)==false){
            return true;
        } else {
            return false;
        }
    }

    public boolean enterPin(String cardId, String pin) {
        if(currentCard.getPin().equals(pin)){
            currentCard.resetFailedAttempts();
            return true;
        } else {
            int failedAttempts = currentCard.getFailedAttempts();
            System.out.println(failedAttempts);
            if(failedAttempts < 3){
                currentCard.incrementFailedAttempts();
            } else {
                currentCard.lockCard();
            }
            return false;
        }
    }

    public double checkBalance(String cardId) {
        currentCard = bank.getCardById(cardId);
        System.out.println(currentCard.getCardId());
        double balance = currentCard.getBalance();
        System.out.println(balance);
        return balance;
    }

    public void deposit(double amount) {
    }

    public boolean withdraw(double amount) {

        return true;
    }

}
