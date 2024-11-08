import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
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
        when(bankMock.getCardById("12345")).thenReturn(cardPinMock);
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
    @DisplayName("Correct PIN-code and reset of failed attempts.")
    public void testEnterCorrectPin() {
        when(bankMock.getCardById(cardPinMock.getCardId())).thenReturn(cardPinMock);
        when(cardPinMock.getPin()).thenReturn("1234");
        atm.insertCard(cardPinMock.getCardId());
        boolean result = atm.enterPin(cardPinMock.getCardId(), "1234");

        assertTrue(result);
        verify(bankMock, times(1)).resetFailedAttempts(cardPinMock.getCardId());
    }

    @Test
    @DisplayName("Wrong PIN-code and increase in failed attempts")
    public void testEnterIncorrectPin() {
        when(bankMock.getCardById(cardPinMock.getCardId())).thenReturn(cardPinMock);
        when(cardPinMock.getPin()).thenReturn("1234");
        atm.insertCard(cardPinMock.getCardId());
        boolean result = atm.enterPin(cardPinMock.getCardId(), "5678");
        assertFalse(result);

        ATMOperationException exception = assertThrows(
                ATMOperationException.class,
                () -> atm.enterPin(cardPinMock.getCardId(), "5678")
        );
        assertEquals("Card has been locked due to too many incorrect PIN entries.", exception.getMessage());

        verify(bankMock, times(1)).incrementFailedAttempts(cardPinMock.getCardId());
    }

    @Test
    @DisplayName("Locked card after pin being wrong 3 times")
    public void testCardLockAfterThreeFailedAttempts() {
        String cardId = cardMock.getCardId();

        when(bankMock.getCardById(cardId)).thenReturn(cardMock);
        when(bankMock.getFailedAttempts(cardId)).thenReturn(0, 1, 2, 3);
        when(bankMock.isLocked(cardId)).thenReturn(false);

        doAnswer(invocation -> {
            when(bankMock.isLocked(cardId)).thenReturn(true);
            return null;
        }).when(bankMock).lockCard(cardId);

        for (int attempts = bankMock.getFailedAttempts(cardId); attempts < atm.getMaxAttempts(); attempts++) {
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

    @ParameterizedTest
    @DisplayName("Withdraw success")
    @ValueSource(ints = {10, 600, 20, 999, 1000})
    public void testSuccessfulWithdraw(int requestedAmount){
        double currentBalance = 1000;
        double newBalance = currentBalance - requestedAmount;
        String cardId = cardMock.getCardId();
        when(bankMock.getBalance(cardId)).thenReturn(currentBalance);

        doAnswer(invocation -> {
            when(bankMock.getBalance(cardId)).thenReturn(newBalance);
            return null;
        }).when(bankMock).setBalance(newBalance);

        atm.withdraw(cardId, requestedAmount);
        verify(bankMock, times(1)).setBalance(newBalance);
        assertEquals(newBalance, bankMock.getBalance(cardId));
    }

    @ParameterizedTest
    @DisplayName("Withdraw unsuccessful")
    @ValueSource(ints = {9, -300, 1001})
    public void testUnsuccessfulWithdraw(int requestedAmount){
        double currentBalance = 1000;
        String cardId = cardMock.getCardId();

        when(bankMock.getBalance(cardId)).thenReturn(currentBalance);
        atm.withdraw(cardId, requestedAmount);
        verify(bankMock, times(0)).setBalance(anyDouble());
        assertEquals(currentBalance, bankMock.getBalance(cardId));
    }

    @Test
    @DisplayName("Bank name")
    public void testGetBankName() {
        // Mock the static method `getBankName` in the `Bank` class
        try (MockedStatic<Bank> mockedStaticBank = mockStatic(Bank.class)) {

            mockedStaticBank.when(Bank::getBankName).thenReturn("Swedbank");
            assertEquals("Swedbank", atm.getBankName());
            mockedStaticBank.verify(Bank::getBankName, times(1));
        }
    }

    @Test
    @DisplayName("Customer cancels operations")
    public void testSessionIsClosed() {
        when(bankMock.isCardValid(cardMock.getCardId())).thenReturn(true);
        when(bankMock.getCardById(cardMock.getCardId())).thenReturn(cardMock);

        boolean isInserted = atm.insertCard(cardMock.getCardId());
        assertTrue(isInserted);
        assertTrue(atm.liveSession);

        boolean isSessionLive = atm.endSession(cardMock.getCardId());
        assertFalse(isSessionLive);
        assertNull(atm.getCurrentCard());
    }

    @Test
    @DisplayName("Card is inserted")
    public void testInsertedCard(){
        String cardId = cardMock.getCardId();
        when(bankMock.getCardById(cardId)).thenReturn(cardMock);
        when(bankMock.isCardValid(cardId)).thenReturn(true);
        boolean isInserted = atm.insertCard(cardMock.getCardId());
        assertTrue(isInserted);
        assertEquals(cardMock, atm.getCurrentCard());
        verify(bankMock, times(1)).getCardById(cardId);
    }

    @Test
    @DisplayName("Card is not inserted")
    public void testNotInsertedCard(){
        String cardId = cardMock.getCardId();
        when(bankMock.getCardById(cardId)).thenReturn(cardMock);
        when(bankMock.isCardValid(cardId)).thenReturn(false);
        boolean isInserted = atm.insertCard(cardMock.getCardId());
        assertFalse(isInserted);
        verify(bankMock, times(1)).getCardById(cardId);
    }

    @Test
    @DisplayName("Successful deposit")
    public void testAddedToBalance(){
        double currentBalance = 50;
        double deposit = 30.55;
        double newBalance = currentBalance + deposit;
        String cardId = cardMock.getCardId();

        when(bankMock.getBalance(cardId)).thenReturn(currentBalance);

        doAnswer(invocation -> {
            when(bankMock.getBalance(cardId)).thenReturn(newBalance);
            return null;
        }).when(bankMock).setBalance(newBalance);

        atm.deposit(cardId, deposit);
        verify(bankMock, times(1)).setBalance(newBalance);
        assertEquals(newBalance, bankMock.getBalance(cardId));
    }

    @ParameterizedTest
    @DisplayName("Unsuccessful deposit")
    @ValueSource(doubles = {0.00, -12.55, -158+32})
    public void testNotAddedToBalance(double attemptedDeposit){
        double currentBalance = 50;
        String cardId = cardMock.getCardId();

        when(bankMock.getBalance(cardId)).thenReturn(currentBalance);
        atm.deposit(cardId, attemptedDeposit);

        verify(bankMock, times(0)).setBalance(anyDouble());
        assertEquals(currentBalance, bankMock.getBalance(cardId));
    }

}
