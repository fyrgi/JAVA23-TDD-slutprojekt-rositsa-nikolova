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
    private ATM atm;
    private Card cardMock, cardPinMock;
    private LocalDate today = LocalDate.now();
    private LocalDate expiresIn4Years = today.plusYears(4);
    private YearMonth expieresYearMonth = YearMonth.from(expiresIn4Years);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        bankMock = mock(Bank.class);
        atm = new ATM(bankMock);
        cardPinMock = mock(Card.class);
        // Setup mock and spy for testing
        when(bankMock.getCardById("12345")).thenReturn(cardPinMock);
        // ako imame spy znachi shte imame prekaleno dylyg kod.
        cardMock = new Card("123456", "1234");
        when(bankMock.getCardById("123456")).thenReturn(cardMock);
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
        when(cardPinMock.getPin()).thenReturn("1234");
        atm.insertCard("12345");
        boolean result = atm.enterPin("12345", "1234");

        assertTrue(result);
    }

    @Test
    @DisplayName("Wrong pin")
    public void testEnterIncorrectPin() {
        when(cardPinMock.getPin()).thenReturn("1234");
        atm.insertCard("12345");
        boolean result = atm.enterPin("12345", "5678");

        assertFalse(result);
        //verify(cardMock, times(1)).incrementFailedAttempts();
    }

    @Test
    @DisplayName("Wrong pin 3 times. Card is locked")
    public void testCardLockAfterThreeFailedAttempts() {
        String cardId = cardMock.getCardId();

        when(bankMock.getCardById(cardId)).thenReturn(cardMock);
        when(bankMock.getFailedAttempts(cardId)).thenReturn(1, 2, 3);
        when(bankMock.isLocked(cardId)).thenReturn(false);

        doAnswer(invocation -> {
            when(bankMock.isLocked(cardId)).thenReturn(true);
            return null;
        }).when(bankMock).lockCard(cardId);

        for (int attempts = 0; attempts < atm.getMaxAttempts(); attempts++) {
            boolean result = atm.enterPin(cardId, "0000");
        }
        
        verify(bankMock, times(1)).lockCard(cardId);
        assertTrue(bankMock.isLocked(cardId));
    }


    @Test
    @DisplayName("Check the user's balance")
    public void testCheckCardsBalance(){
        when(bankMock.getBalance(cardMock.getCardId())).thenReturn(1000.00);
        double balance = atm.checkBalance("123456");
        assertEquals(1000.0, balance);
    }
}
