public class ATM {
    public int maxAttempts = 3;
    private Bank bank;
    private Card currentCard;

    boolean liveSession = false;

    public ATM(Bank bank) {
        this.bank = bank;
    }

    public boolean insertCard(String cardId) throws ATMOperationException {
        currentCard = bank.getCardById(cardId);
        if(bank.isCardValid(cardId) == false){
            endSession(cardId);
            throw new ATMOperationException("The card is not valid");
        } else {
            liveSession = true;
            return true;
        }
    }

    public boolean enterPin(String cardId, String pin) throws ATMOperationException {
        currentCard = bank.getCardById(cardId);
        if(currentCard.getPin().equals(pin)){
            bank.resetFailedAttempts(cardId);
            return true;
        } else {
            // Increment failed attempts first, then retrieve the current count
            bank.incrementFailedAttempts(cardId);
            int failedAttempts = bank.getFailedAttempts(cardId);
            int leftAttempts = maxAttempts - failedAttempts;

            if (failedAttempts < maxAttempts) { // Less than max, so not locked yet
                throw new ATMOperationException("Wrong pin.\nYour card will be locked after " + leftAttempts + " attempt(s).");
            } else { // Max attempts reached, lock the card
                bank.lockCard(cardId);
                throw new ATMOperationException("Your card is locked! Contact your bank");
            }
        }
    }

    public double checkBalance(String cardId) {
        double balance = bank.getBalance(cardId);
        return balance;
    }

    public void deposit(String cardId, double amount) throws ATMOperationException{
        double currentBalance = bank.getBalance(cardId);
        if(amount > 0){
            bank.setBalance(currentBalance+=amount);
        } else {
            throw new ATMOperationException("The ATM did not detect an amount.");
        }
    }

    public boolean withdraw(String cardId, int amount) throws ATMOperationException {
        if(amount > bank.getBalance(cardId)){
            throw new ATMOperationException("Your balance is not enough.");
        } else if (amount < 10) {
            throw new ATMOperationException("Minimum withdraw amount is 10");
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

    public boolean endSession(String cardId) {
        currentCard = null;
        this.liveSession = false;
        return liveSession;
    }

    public Card getCurrentCard() {
        return currentCard;
    }

    public boolean changePin(String cardId, String pin, String newPin, String repatNewPin) throws ATMOperationException {
        pin = pin.trim();
        newPin = newPin.trim();
        repatNewPin = repatNewPin.trim();

        if (pin.equals(currentCard.getPin())) {
            bank.resetFailedAttempts(cardId);
            if (newPin.equals(repatNewPin)) {
                currentCard.setPin(newPin);
                return true;
            } else {
                throw new ATMOperationException("Your new PIN entries mismatch. The operation is aborted.");
            }
        } else {
            bank.incrementFailedAttempts(cardId);
            throw new ATMOperationException("You have provided a faulty PIN value.\nOperation aborted.");
        }
    }
}
