public class ATM {
    public int maxAttempts = 3;
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
        currentCard = bank.getCardById(cardId);
        if(currentCard.getPin().equals(pin)){
            bank.resetFailedAttempts(cardId);
            return true;
        } else {
            int failedAttempts = bank.getFailedAttempts(cardId);
            if(failedAttempts < maxAttempts){
                bank.incrementFailedAttempts(cardId);
            } else {
                bank.lockCard(cardId);
            }
            return false;
        }
    }

    public double checkBalance(String cardId) {
        double balance = bank.getBalance(cardId);
        System.out.println(balance);
        return balance;
    }

    public void deposit(double amount) {
    }

    public boolean withdraw(String cardId, int amount) {
        if(amount < 10 || amount > bank.getBalance(cardId)){
            return false;
        } else {
            bank.setBalance(bank.getBalance(cardId) - amount);
            return true;
        }
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public String getBankName() {
        return Bank.getBankName();
    }
}
