package dev.studyforge.wallet;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferService")
class TransferServiceTest {
    @Mock AccountRepository accounts;
    @Mock TransferNotifier notifier;

    private Account source;
    private Account destination;
    private TransferService service;

    @BeforeEach
    void setUp() {
        source = new Account("source", Money.ofCents(1_000));
        destination = new Account("destination", Money.ofCents(200));
        service = new TransferService(accounts, notifier);
    }

    private void repositoryContainsBothAccounts() {
        when(accounts.findById("source")).thenReturn(Optional.of(source));
        when(accounts.findById("destination")).thenReturn(Optional.of(destination));
    }

    @Nested
    @DisplayName("successful transfers")
    class SuccessfulTransfers {
        @Test
        @DisplayName("updates state and notifies the external collaborator")
        void transfersAndNotifies() {
            repositoryContainsBothAccounts();
            Money amount = Money.ofCents(250); // A real value object, not a mock.

            TransferReceipt result = service.transfer("source", "destination", amount);

            ArgumentCaptor<TransferReceipt> notification = ArgumentCaptor.forClass(TransferReceipt.class);
            assertAll(
                    () -> assertEquals(Money.ofCents(750), source.balance()),
                    () -> assertEquals(Money.ofCents(450), destination.balance()),
                    () -> assertSame(amount, result.amount()));
            verify(notifier).transferCompleted(notification.capture());
            assertEquals(result, notification.getValue());
        }

        @Test
        @DisplayName("allows the exact available balance as a boundary")
        void transfersEntireBalance() {
            repositoryContainsBothAccounts();

            service.transfer("source", "destination", Money.ofCents(1_000));

            assertAll(
                    () -> assertEquals(Money.ofCents(0), source.balance()),
                    () -> assertEquals(Money.ofCents(1_200), destination.balance()));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("invalid requests")
    class InvalidRequests {
        Stream<Arguments> invalidIds() {
            return Stream.of(
                    Arguments.of("same", "same", "Source and destination must differ"));
        }

        @ParameterizedTest
        @MethodSource("invalidIds")
        void rejectsInvalidAccountPair(String from, String to, String message) {
            IllegalArgumentException error = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.transfer(from, to, Money.ofCents(1)));

            assertEquals(message, error.getMessage());
            verifyNoInteractions(accounts, notifier);
        }

        @Test
        void rejectsZeroAmountBeforeCallingDependencies() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.transfer("source", "destination", Money.ofCents(0)));

            verifyNoInteractions(accounts, notifier);
        }

        @ParameterizedTest
        @NullSource
        void rejectsNullAmount(Money amount) {
            assertThrows(NullPointerException.class,
                    () -> service.transfer("source", "destination", amount));
            verifyNoInteractions(accounts, notifier);
        }
    }

    @Nested
    @DisplayName("failed transfers")
    class FailedTransfers {
        @Test
        void reportsMissingAccountAndDoesNotNotify() {
            when(accounts.findById("missing")).thenReturn(Optional.empty());

            AccountNotFoundException error = assertThrows(AccountNotFoundException.class,
                    () -> service.transfer("missing", "destination", Money.ofCents(10)));

            assertEquals("Account not found: missing", error.getMessage());
            verify(notifier, never()).transferCompleted(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("insufficient funds leave both accounts unchanged")
        void preservesStateOnInsufficientFunds() {
            repositoryContainsBothAccounts();

            assertThrows(InsufficientFundsException.class,
                    () -> service.transfer("source", "destination", Money.ofCents(1_001)));

            assertAll(
                    () -> assertEquals(Money.ofCents(1_000), source.balance()),
                    () -> assertEquals(Money.ofCents(200), destination.balance()));
            verify(notifier, never()).transferCompleted(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("arithmetic overflow leaves both accounts unchanged")
        void preservesStateOnOverflow() {
            source = new Account("source", Money.ofCents(10));
            destination = new Account("destination", Money.ofCents(Long.MAX_VALUE));
            repositoryContainsBothAccounts();

            assertThrows(ArithmeticException.class,
                    () -> service.transfer("source", "destination", Money.ofCents(1)));

            assertAll(
                    () -> assertEquals(Money.ofCents(10), source.balance()),
                    () -> assertEquals(Money.ofCents(Long.MAX_VALUE), destination.balance()));
        }
    }
}
