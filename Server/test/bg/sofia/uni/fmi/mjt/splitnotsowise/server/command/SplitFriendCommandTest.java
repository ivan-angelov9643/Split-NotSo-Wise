package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.SplitFriendCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.DebtManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.SplitFriendStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SplitFriendCommandTest {

    private DebtManager debtManager;
    private SplitFriendCommand splitFriendCommand;

    @BeforeEach
    public void setUp() {
        debtManager = mock();
        splitFriendCommand = new SplitFriendCommand(true, "10.0", "user1", "user2",
            debtManager);
    }

    @Test
    public void testExecuteSuccess() throws DataStorageException {
        when(debtManager.splitFriend(anyString(), anyString(), anyDouble())).thenReturn(SplitFriendStatus.SUCCESS);

        splitFriendCommand.execute();

        assertEquals("amount split successfully", splitFriendCommand.getMessage());
        verify(debtManager, times(1)).splitFriend("user1", "user2", 10);
    }

    @Test
    public void testExecuteFriendshipDoesNotExist() throws DataStorageException {
        when(debtManager.splitFriend(anyString(), anyString(), anyDouble()))
            .thenReturn(SplitFriendStatus.FRIENDSHIP_DOES_NOT_EXIST);

        splitFriendCommand.execute();

        assertEquals("you are not friends", splitFriendCommand.getMessage());
        verify(debtManager, times(1)).splitFriend("user1", "user2", 10);
    }

    @Test
    public void testExecuteUserDoesNotExist() throws DataStorageException {
        when(debtManager.splitFriend(anyString(), anyString(), anyDouble()))
            .thenReturn(SplitFriendStatus.USER_DOES_NOT_EXIST);

        splitFriendCommand.execute();

        assertEquals("user with this username doesn't exist", splitFriendCommand.getMessage());
        verify(debtManager, times(1)).splitFriend("user1", "user2", 10);
    }

    @Test
    public void testExecuteDataStorageException() throws DataStorageException {
        when(debtManager.splitFriend(anyString(), anyString(), anyDouble()))
            .thenThrow(new DataStorageException("error"));

        splitFriendCommand.execute();

        assertEquals("splitting amount failed", splitFriendCommand.getMessage());
        verify(debtManager, times(1)).splitFriend("user1", "user2", 10);
    }

    @Test
    public void testExecuteNotLoggedIn() throws DataStorageException {
        SplitFriendCommand notLoggedInCommand =
            new SplitFriendCommand(false, "10.0", "user1", "user2", debtManager);

        notLoggedInCommand.execute();

        assertEquals("can't split amount when not logged in", notLoggedInCommand.getMessage());
        verify(debtManager, never()).splitFriend(any(), any(), anyDouble());
    }
}
