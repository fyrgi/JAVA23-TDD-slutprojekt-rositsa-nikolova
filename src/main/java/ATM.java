public class ATM {
    private Bank bank = new Bank();

    private Account currentAccount;

    private Card currentCard;

    public ATM(Bank bank) {
        this.bank = bank;
    }

    public boolean insertCard(String cardId) {
        currentAccount = bank.getAccountByCardId(cardId);
        if(bank.isCardLocked(cardId)==false){
            return true;
        } else {
            return false;
        }
    }

    public boolean enterPin(String cardId, String pin) {
        if(currentAccount.getCard(cardId).getPin().equals(pin)){
            return true;
        } else {
            return false;
        }
    }

    public double checkBalance(String cardId) {
        currentAccount = bank.getAccountByCardId(cardId);
        System.out.println(currentAccount.getCard(cardId));
        double balance = currentAccount.getBalance();
        return balance;
    }

    public void deposit(double amount) {
    }

    public boolean withdraw(double amount) {
        return true;
    }

}
