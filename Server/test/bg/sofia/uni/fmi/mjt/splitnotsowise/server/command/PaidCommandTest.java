package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.DebtManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.PaidStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PaidCommandTest {

    private DebtManager debtManager;
    private PaidCommand paidCommand;

    @BeforeEach
    public void setUp() {
        debtManager = mock();
        paidCommand = new PaidCommand(true, "50.0", "user1", "user2", debtManager);
    }

    @Test
    public void testExecuteSuccess() throws DataStorageException {
        when(debtManager.payDebt(anyString(), anyString(), anyDouble())).thenReturn(PaidStatus.SUCCESS);

        paidCommand.execute();

        assertEquals("amount got paid successfully", paidCommand.getMessage());
        verify(debtManager, times(1)).payDebt("user1", "user2", 50);
    }

    @Test
    public void testExecutePayerDoesNotOwePayee() throws DataStorageException {
        when(debtManager.payDebt(anyString(), anyString(), anyDouble()))
            .thenReturn(PaidStatus.PAYER_DOES_NOT_OWE_PAYEE);

        paidCommand.execute();

        assertEquals("this user doesn't owe you money", paidCommand.getMessage());
        verify(debtManager, times(1)).payDebt("user1", "user2", 50);
    }

    @Test
    public void testExecuteUserDoesNotExist() throws DataStorageException {
        when(debtManager.payDebt(anyString(), anyString(), anyDouble()))
            .thenReturn(PaidStatus.USER_DOES_NOT_EXIST);

        paidCommand.execute();

        assertEquals("user with this username doesn't exist", paidCommand.getMessage());
        verify(debtManager, times(1)).payDebt("user1", "user2", 50);
    }

    @Test
    public void testExecuteSameUsernames() throws DataStorageException {
        when(debtManager.payDebt(anyString(), anyString(), anyDouble())).thenReturn(PaidStatus.SAME_USERNAMES);

        paidCommand.execute();

        assertEquals("can't get paid by yourself", paidCommand.getMessage());
        verify(debtManager, times(1)).payDebt("user1", "user2", 50);
    }

    @Test
    public void testExecuteDataStorageException() throws DataStorageException {
        when(debtManager.payDebt(anyString(), anyString(), anyDouble()))
            .thenThrow(new DataStorageException("error"));

        paidCommand.execute();

        assertEquals("getting paid amount failed", paidCommand.getMessage());
        verify(debtManager, times(1)).payDebt("user1", "user2", 50);
    }

    @Test
    public void testExecuteNotLoggedIn() throws DataStorageException {
        PaidCommand notLoggedInCommand =
            new PaidCommand(false, "50.0", "user1", "user2", debtManager);

        notLoggedInCommand.execute();

        assertEquals("can't get paid amount when not logged in", notLoggedInCommand.getMessage());
        verify(debtManager, never()).payDebt(any(), any(), anyDouble());
    }
}
