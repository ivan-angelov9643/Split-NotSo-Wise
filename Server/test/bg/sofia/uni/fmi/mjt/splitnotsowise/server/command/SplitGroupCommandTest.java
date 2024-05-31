package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.SplitGroupCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.DebtManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.SplitGroupStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SplitGroupCommandTest {

    private DebtManager debtManager;
    private SplitGroupCommand splitGroupCommand;

    @BeforeEach
    public void setUp() {
        debtManager = mock();
        splitGroupCommand = new SplitGroupCommand(true, "10.0", "group1", "user2",
            debtManager);
    }

    @Test
    public void testExecuteSuccess() throws DataStorageException {
        when(debtManager.splitGroup(anyString(), anyString(), anyDouble())).thenReturn(SplitGroupStatus.SUCCESS);

        splitGroupCommand.execute();

        assertEquals("amount split successfully", splitGroupCommand.getMessage());
        verify(debtManager, times(1)).splitGroup("group1", "user2", 10);
    }

    @Test
    public void testExecuteUserNotInGroup() throws DataStorageException {
        when(debtManager.splitGroup(anyString(), anyString(), anyDouble()))
            .thenReturn(SplitGroupStatus.USER_NOT_IN_GROUP);

        splitGroupCommand.execute();

        assertEquals("you are not in this group", splitGroupCommand.getMessage());
        verify(debtManager, times(1)).splitGroup("group1", "user2",
            10.0);
    }

    @Test
    public void testExecuteGroupDoesNotExist() throws DataStorageException {
        when(debtManager.splitGroup(anyString(), anyString(), anyDouble()))
            .thenReturn(SplitGroupStatus.GROUP_DOES_NOT_EXIST);

        splitGroupCommand.execute();

        assertEquals("group with this name doesn't exist", splitGroupCommand.getMessage());
        verify(debtManager, times(1)).splitGroup("group1", "user2",
            10.0);
    }

    @Test
    public void testExecuteDataStorageException() throws DataStorageException {
        when(debtManager.splitGroup(anyString(), anyString(), anyDouble()))
            .thenThrow(new DataStorageException("error"));

        splitGroupCommand.execute();

        assertEquals("splitting amount failed", splitGroupCommand.getMessage());
        verify(debtManager, times(1)).splitGroup("group1", "user2",
            10.0);
    }

    @Test
    public void testExecuteNotLoggedIn() throws DataStorageException {
        SplitGroupCommand notLoggedInCommand =
            new SplitGroupCommand(false, "10.0", "group1", "user2", debtManager);

        notLoggedInCommand.execute();

        assertEquals("can't split amount when not logged in", notLoggedInCommand.getMessage());
        verify(debtManager, never()).splitGroup(any(), any(), anyDouble());
    }
}
