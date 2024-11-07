import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ATMTest {
    private Bank bankMock;
    private ATM atm, atmSpy;
    private Card cardMock, cardSpy;
    private Bank bankSpy;
    private LocalDate today = LocalDate.now();
    private LocalDate expiresIn4Years = today.plusYears(4);
    private YearMonth expieresYearMonth = YearMonth.from(expiresIn4Years);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        bankMock = mock(Bank.class);
        atm = new ATM(bankMock);
        cardMock = mock(Card.class);

        // Setup mock and spy for testing
        when(bankMock.getCardById("12345")).thenReturn(cardMock);
        // ako imame spy znachi shte imame prekaleno dylyg kod.
        cardSpy = new Card("123456", "1234", expieresYearMonth, 1233.32, false, 0);
        when(bankMock.getCardById("123456")).thenReturn(cardSpy);
    }


    @Test
    @DisplayName("Not a valid card")
    public void testCardIsNotValid() {
        when(bankMock.isCardValid("12345")).thenReturn(false);
        boolean result = bankMock.isCardValid("12345");
        assertFalse(result);
    }

    @Test
    @DisplayName("Valid card")
    public void testCardIsValid() {
        when(bankMock.isCardValid("12345")).thenReturn(true);
        boolean result = bankMock.isCardValid("12345");
        assertTrue(result);
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
        //when(cardMock.getFailedAttempts()).thenReturn(0);
        atm.insertCard("12345");
        boolean result = atm.enterPin("12345", "5678");

        assertFalse(result);
        //verify(cardMock, times(1)).incrementFailedAttempts();
    }

    @Test
    @DisplayName("Wrong pin 3 times. Card is locked")
    public void testCardLockAfterThreeFailedAttempts() {
        when(cardMock.getPin()).thenReturn("1234");

        atm.insertCard("12345");

        atm.enterPin("12345", "0000");
        atm.enterPin("12345", "0000");
        atm.enterPin("12345", "0000");

        verify(cardMock, times(3)).incrementFailedAttempts();
        verify(cardMock, times(1)).lockCard();
        assertTrue(cardMock.isLocked());
    }


    @Test
    @DisplayName("Check the user's balance")
    public void testCheckCardsBalance(){
        double balance = atm.checkBalance("123456");
        System.out.println(balance);
        assertEquals(1000.0, balance);
    }
}
