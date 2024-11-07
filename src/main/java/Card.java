import java.time.YearMonth;

public class Card {
    private String cardId, pin;

    public Card(String cardId, String pin){
        this.cardId = cardId;
        this.pin = pin;
    }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }


    public String getCardId() {
        return cardId;
    }

}
