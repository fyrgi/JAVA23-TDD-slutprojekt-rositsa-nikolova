import java.util.HashMap;
import java.util.Map;

public class Account {

    private double balance;
    int idCard = 0;
    Map<String, Card> cards = new HashMap<>();
    public Account(double balance) {
        this.balance = balance;
    }
    public Account(){}

    // Getters och Setters
    public void setCards(Card card) {
        Integer nextCard = idCard++;
        this.cards.put(String.valueOf(nextCard), card);
    }

    public int getIdCard(){
        return this.idCard;
    }
    public Card getCard(String id) { return cards.get(id); }
    public double getBalance() { return balance; }
    public void deposit(double amount) { this.balance += amount; }
    public void withdraw(double amount) { this.balance -= amount; }
}
