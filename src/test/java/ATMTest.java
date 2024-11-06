import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ATMTest {
    private Bank bankMock;
    private ATM atm;
    private Account accountMock;
    private Card cardMock;
    private Account accountSpy;

    private Bank bankSpy;

    private LocalDate today = LocalDate.now();
    private LocalDate expiresIn4Years = today.plusYears(4);
    private YearMonth expieresYearMonth = YearMonth.from(expiresIn4Years);

    @BeforeEach
    public void setUp() {
        bankMock = mock(Bank.class);
        atm = new ATM(bankMock);
        accountMock = mock(Account.class);
        cardMock = mock(Card.class);

        // Setup mock behavior
        when(bankMock.getAccountByCardId("12345")).thenReturn(accountMock);
        when(accountMock.getCard("12345")).thenReturn(cardMock);

        accountSpy = spy(new Account(1000.0));
        accountSpy.setCards(new Card("123456", "1234", expieresYearMonth, "111"));
        //when(bankSpy.getCardDataByCardId("123456")).thenReturn(accountSpy);
        when(bankMock.getAccountByCardId("123456")).thenReturn(accountSpy);
    }

    @Test
    @DisplayName("Not locked card.")
    public void testCardIsNotLocked() {
        when(bankMock.isCardLocked("12345")).thenReturn(false);
        boolean result = bankMock.isCardLocked("12345");
        assertFalse(result);
    }

    @Test
    @DisplayName("Locked card")
    public void testLockedCard() {
        when(bankMock.isCardLocked("12345")).thenReturn(true);
        boolean result1 = bankMock.isCardLocked("12345");
        assertTrue(result1);
    }

    @Test
    @DisplayName("Correct PIN-code")
    public void testEnterCorrectPin() {
        // Stub card's PIN
        when(cardMock.getPin()).thenReturn("1234");

        atm.insertCard("12345");
        boolean result = atm.enterPin("12345", "1234");

        assertTrue(result);
    }

    @Test
    @DisplayName("Wrong pin")
    public void testEnterIncorrectPin() {
        when(cardMock.getPin()).thenReturn("1234");

        atm.insertCard("12345");
        boolean result = atm.enterPin("12345", "5678");

        assertFalse(result);
        verify(cardMock, times(1)).incrementFailedAttempts();
    }

    @Test
    @DisplayName("Wrong pin 3 times. Card is locked")
    public void testCardLockAfterThreeFailedAttempts() {
        when(cardMock.getPin()).thenReturn("1234");

        atm.insertCard("123456");

        atm.enterPin("123456", "0000");
        atm.enterPin("123456", "0000");
        atm.enterPin("123456", "0000");

        verify(cardMock, times(3)).incrementFailedAttempts();
        verify(cardMock, times(1)).lockCard();
        assertTrue(cardMock.isLocked());
    }

    @Test
    @DisplayName("Card connected to the account")
    public void testCardConnectedToTheAccount(){
        // created with spy
        System.out.println(bankMock.getAccountByCardId("123456").getBalance());
        System.out.println(bankMock.isCardLocked("123456"));

        // not existing
        System.out.println(bankMock.getAccountByCardId("123455").getBalance());
        System.out.println(bankMock.isCardLocked("123455"));
    }

    @Test
    @DisplayName("Checck account's balance")
    public void testCheckAccountBalance(){
        double balance = atm.checkBalance("123456");
        System.out.println(balance);
        assertEquals(1000.0, balance);
    }
}
