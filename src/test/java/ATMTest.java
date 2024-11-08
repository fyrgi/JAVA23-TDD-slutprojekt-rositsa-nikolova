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

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        bankMock = mock(Bank.class);
        atm = new ATM(bankMock);
        cardPinMock = mock(Card.class);
        when(bankMock.getCardById("12345")).thenReturn(cardPinMock);
        when(bankMock.isCardValid(cardPinMock.getCardId())).thenReturn(true);
        cardMock = new Card("123456", "1234");
        when(bankMock.getCardById("123456")).thenReturn(cardMock);
    }

    @Test
    @DisplayName("Not a valid card")
    public void testCardIsNotValid() throws ATMOperationException{
        when(bankMock.getCardById(cardMock.getCardId())).thenReturn(cardMock);
        when(bankMock.isCardValid(cardMock.getCardId())).thenReturn(false);
        ATMOperationException exception = assertThrows(
                ATMOperationException.class,
                () -> atm.insertCard(cardMock.getCardId()),
                "The card is not valid"
        );

        assertEquals("The card is not valid", exception.getMessage());
        assertNull(atm.getCurrentCard());
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
    public void testEnterCorrectPin() throws ATMOperationException {
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
        String correctPin = "1234";
        String incorrectPin = "5678";
        String cardId = cardPinMock.getCardId();

        when(bankMock.getCardById(cardId)).thenReturn(cardPinMock);
        when(cardPinMock.getPin()).thenReturn(correctPin);

        atm.insertCard(cardId);

        when(bankMock.getFailedAttempts(cardId)).thenReturn(1);

        int maxAttempts = atm.getMaxAttempts();
        int failedAttempts = bankMock.getFailedAttempts(cardId);
        int leftAttempts = maxAttempts - failedAttempts;

        ATMOperationException exception = assertThrows(
                ATMOperationException.class,
                () -> atm.enterPin(cardId, incorrectPin),
                String.format("Wrong pin.\nYour card will be locked after %s attempt(s).", leftAttempts)
        );

        assertEquals(String.format("Wrong pin.\nYour card will be locked after %s attempt(s).", leftAttempts), exception.getMessage());
        verify(bankMock, times(1)).incrementFailedAttempts(cardId);
    }

    @Test
    @DisplayName("Locked card after pin being wrong 3 times")
    public void testCardLockAfterThreeFailedAttempts() {
        String cardId = cardMock.getCardId();
        String incorrectPin = "0000";
        String correctPin = "1234";

        when(bankMock.getCardById(cardId)).thenReturn(cardPinMock);
        when(cardPinMock.getPin()).thenReturn(correctPin);
        when(bankMock.getFailedAttempts(cardId)).thenReturn(1, 2, 3);

        doAnswer(invocation -> {
            when(bankMock.isLocked(cardId)).thenReturn(true);
            return null;
        }).when(bankMock).lockCard(cardId);

        for (int i = 0; i < atm.getMaxAttempts(); i++) {
            try {
                atm.enterPin(cardId, incorrectPin);
            } catch (ATMOperationException e) {
                if (i < atm.getMaxAttempts() - 1) {
                    assertEquals(
                            String.format("Wrong pin.\nYour card will be locked after %s attempt(s).", atm.getMaxAttempts() - i - 1),
                            e.getMessage()
                    );
                } else {
                    assertEquals("Your card is locked! Contact your bank", e.getMessage());
                }
            }
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
    @DisplayName("Withdraw extends balance")
    @ValueSource(ints = {1001, 6456})
    public void testRequestedWithdrawIsHigherThanBalance(int requestedAmount){
        double currentBalance = 1000;
        String cardId = cardMock.getCardId();
        when(bankMock.getBalance(cardId)).thenReturn(currentBalance);

        ATMOperationException exception = assertThrows(
                ATMOperationException.class,
                () -> atm.withdraw(cardId, requestedAmount),
                String.format("Your balance is not enough.")
        );
        assertEquals("Your balance is not enough.", exception.getMessage());
        verify(bankMock, times(0)).setBalance(anyDouble());
        assertEquals(currentBalance, bankMock.getBalance(cardId));
    }

    @ParameterizedTest
    @DisplayName("Withdraw is under the minimum")
    @ValueSource(ints = {9, 0})
    public void testRequestedWithdrawIsUnderTheMinimum(int requestedAmount){
        double currentBalance = 1000;
        String cardId = cardMock.getCardId();
        when(bankMock.getBalance(cardId)).thenReturn(currentBalance);

        ATMOperationException exception = assertThrows(
                ATMOperationException.class,
                () -> atm.withdraw(cardId, requestedAmount),
                String.format("Minimum withdraw amount is 10")
        );
        assertEquals("Minimum withdraw amount is 10", exception.getMessage());
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
    public void testNotInsertedCard() throws ATMOperationException{
        String cardId = cardMock.getCardId();
        when(bankMock.getCardById(cardId)).thenReturn(cardMock);
        when(bankMock.isCardValid(cardId)).thenReturn(false);
        ATMOperationException exception = assertThrows(
                ATMOperationException.class,
                () -> atm.insertCard(cardMock.getCardId()),
                "The card is not valid"
        );

        assertEquals("The card is not valid", exception.getMessage());
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
    public void testNotAddedToBalance(double attemptedDeposit) throws ATMOperationException{
        double currentBalance = 50;
        String cardId = cardMock.getCardId();

        when(bankMock.getBalance(cardId)).thenReturn(currentBalance);

        ATMOperationException exception = assertThrows(
                ATMOperationException.class,
                () -> atm.deposit(cardId, attemptedDeposit),
                String.format("The ATM did not detect an amount.")
        );

        assertEquals("The ATM did not detect an amount.", exception.getMessage());
        verify(bankMock, times(0)).setBalance(anyDouble());
        assertEquals(currentBalance, bankMock.getBalance(cardId));


    }

    @Test
    @DisplayName("Change pin and reset failed attempts")
    public void testChangePin() throws ATMOperationException{
        String cardId = "123456";
        String oldPin = "1234";
        String newPin = "8888";
        String repatNewPin = "8888";
        when(bankMock.getCardById(cardId)).thenReturn(cardPinMock);
        when(bankMock.isCardValid(cardId)).thenReturn(true);
        when(cardPinMock.getPin()).thenReturn(oldPin);

        atm.insertCard(cardId);
        boolean result = atm.changePin(cardId, oldPin, newPin, repatNewPin);
        assertTrue(result);

        verify(bankMock, times(1)).resetFailedAttempts(cardId);
        verify(bankMock, times(1)).setPin(cardId, newPin);
    }

    @Test
    @DisplayName("Pin not provided when changing. Increase failed attempts")
    public void testChangePinWithoutAuth() throws ATMOperationException {
        when(bankMock.getCardById(cardPinMock.getCardId())).thenReturn(cardPinMock);
        when(cardPinMock.getPin()).thenReturn("");
        atm.insertCard(cardPinMock.getCardId());

        String newPin = "8888", repatNewPin = "8888";

        assertThrows(
                ATMOperationException.class,
                () -> atm.changePin(cardPinMock.getCardId(), "1234", newPin, repatNewPin),
                "You have provided a faulty PIN value.\nOperation aborted.");

        verify(bankMock, times(1)).incrementFailedAttempts(cardPinMock.getCardId());
        verify(cardPinMock, never()).setPin(newPin);
    }

    @Test
    @DisplayName("New pin and repeat pin mismatch")
    public void testMissmatchingPins() throws ATMOperationException {
        String cardId = cardPinMock.getCardId();
        when(bankMock.getCardById(cardId)).thenReturn(cardPinMock);
        when(cardPinMock.getPin()).thenReturn("1234");
        atm.insertCard(cardPinMock.getCardId());

        String newPin = "0000", repatNewPin = "8888";

        assertThrows(
                ATMOperationException.class,
                () -> atm.changePin(cardPinMock.getCardId(), "1234", newPin, repatNewPin),
                "Your new PIN entries mismatch. The operation is aborted.");

        verify(cardPinMock, never()).setPin(newPin);
    }
}
