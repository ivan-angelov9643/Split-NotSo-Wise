package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.AddFriendCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.FriendshipManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.AddFriendshipStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AddFriendCommandTest {

    private FriendshipManager friendshipManager;

    private AddFriendCommand addFriendCommand;

    @BeforeEach
    public void setUp() {
        friendshipManager = mock();
        addFriendCommand = new AddFriendCommand(true, "user1", "user2", friendshipManager);
    }


    @Test
    public void testExecuteSuccess() throws DataStorageException {
        when(friendshipManager.addFriendship(anyString(), anyString())).thenReturn(AddFriendshipStatus.SUCCESS);

        addFriendCommand.execute();

        assertEquals("friend added successfully", addFriendCommand.getMessage());
        verify(friendshipManager, times(1)).addFriendship("user1",
            "user2");
    }

    @Test
    public void testExecuteFriendshipAlreadyExists() throws DataStorageException {
        when(friendshipManager.addFriendship(anyString(), anyString()))
            .thenReturn(AddFriendshipStatus.FRIENDSHIP_ALREADY_EXISTS);

        addFriendCommand.execute();

        assertEquals("you are already friends", addFriendCommand.getMessage());
        verify(friendshipManager, times(1)).addFriendship("user1",
            "user2");
    }

    @Test
    public void testExecuteUserDoesNotExist() throws DataStorageException {
        when(friendshipManager.addFriendship(anyString(), anyString()))
            .thenReturn(AddFriendshipStatus.USER_DOES_NOT_EXIST);

        addFriendCommand.execute();

        assertEquals("user with this username doesn't exist", addFriendCommand.getMessage());
        verify(friendshipManager, times(1)).addFriendship("user1",
            "user2");
    }

    @Test
    public void testExecuteSameUsernames() throws DataStorageException {
        when(friendshipManager.addFriendship(anyString(), anyString()))
            .thenReturn(AddFriendshipStatus.SAME_USERNAMES);

        addFriendCommand.execute();

        assertEquals("can't add yourself as a friend", addFriendCommand.getMessage());
        verify(friendshipManager, times(1)).addFriendship("user1",
            "user2");
    }

    @Test
    public void testExecuteDataStorageException() throws DataStorageException {
        when(friendshipManager.addFriendship(anyString(), anyString())).thenThrow(new DataStorageException("error"));

        addFriendCommand.execute();

        assertEquals("adding friend failed", addFriendCommand.getMessage());
        verify(friendshipManager, times(1)).addFriendship("user1",
            "user2");
    }

    @Test
    public void testExecuteNotLoggedIn() throws DataStorageException {
        AddFriendCommand notLoggedInCommand =
            new AddFriendCommand(false, "user1", "user2", friendshipManager);

        notLoggedInCommand.execute();

        assertEquals("can't add friend when not logged in", notLoggedInCommand.getMessage());
        verify(friendshipManager, never()).addFriendship(any(), any());
    }
}
