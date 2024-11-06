import java.util.HashMap;
import java.util.Map;

public class Bank implements BankInterface {

    private Map<String, Account> accountOfCustomer = new HashMap<>();

    public Account getAccountByCardId(String id) {
        return accountOfCustomer.get(id);
    }

    public boolean isCardLocked(String cardId) {
        Account account = accountOfCustomer.get(cardId.trim());
        System.out.println(account.getBalance());
        return account != null && account.getCard(cardId).isLocked();
    }

    public static String getBankName() {
        return "MockBank";
    }
}
