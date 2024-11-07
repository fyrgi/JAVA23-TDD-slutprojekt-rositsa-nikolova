import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class Bank implements BankInterface {

    private Map<String, Card> cardOfCustomer = new HashMap<>();

    public Card getCardById(String id) {
        return cardOfCustomer.get(id);
    }

    public boolean isCardValid(String cardId) {
        Card card = cardOfCustomer.get(cardId.trim());
        if(card.getExpiryDate().isAfter(YearMonth.now())){
            System.out.println("expired card");
            return false;
        }

        if(card.isLocked()) {
            System.out.println("the card is locked");
            return false;
        }

        return true;
    }

    public static String getBankName() {
        return "MockBank";
    }
}
