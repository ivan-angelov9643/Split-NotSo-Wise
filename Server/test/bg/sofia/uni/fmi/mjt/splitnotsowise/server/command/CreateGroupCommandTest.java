package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.CreateGroupCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.GroupManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.CreateGroupStatus;
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

public class CreateGroupCommandTest {

    private GroupManager groupManager;
    private CreateGroupCommand createGroupCommand;

    @BeforeEach
    public void setUp() {
        groupManager = mock();
        createGroupCommand = new CreateGroupCommand(true, "group1", "user1",
            groupManager, "user2", "user3");
    }

    @Test
    public void testExecuteSuccess() throws DataStorageException {
        when(groupManager.createGroup(anyString(), anyString(), any(String[].class)))
            .thenReturn(CreateGroupStatus.SUCCESS);

        createGroupCommand.execute();

        assertEquals("group created successfully", createGroupCommand.getMessage());
        verify(groupManager, times(1)).createGroup("group1",
            "user1", "user2", "user3");
    }

    @Test
    public void testExecuteGroupNameAlreadyExists() throws DataStorageException {
        when(groupManager.createGroup(anyString(), anyString(), any(String[].class)))
            .thenReturn(CreateGroupStatus.GROUP_NAME_ALREADY_EXISTS);

        createGroupCommand.execute();

        assertEquals("group name is taken", createGroupCommand.getMessage());
        verify(groupManager, times(1)).createGroup("group1",
            "user1", "user2", "user3");
    }

    @Test
    public void testExecuteMembersContainCreatorsUsername() throws DataStorageException {
        when(groupManager.createGroup(anyString(), anyString(), any(String[].class)))
            .thenReturn(CreateGroupStatus.MEMBERS_CONTAIN_CREATORS_USERNAME);

        createGroupCommand.execute();

        assertEquals("don't put your name in members", createGroupCommand.getMessage());
        verify(groupManager, times(1)).createGroup("group1",
            "user1", "user2", "user3");
    }

    @Test
    public void testExecuteMemberDoesNotExist() throws DataStorageException {
        when(groupManager.createGroup(anyString(), anyString(), any(String[].class)))
            .thenReturn(CreateGroupStatus.MEMBER_DOES_NOT_EXIST);

        createGroupCommand.execute();

        assertEquals("user with this username doesn't exist", createGroupCommand.getMessage());
        verify(groupManager, times(1)).createGroup("group1",
            "user1", "user2", "user3");
    }

    @Test
    public void testExecuteDataStorageException() throws DataStorageException {
        when(groupManager.createGroup(anyString(), anyString(), any(String[].class)))
            .thenThrow(new DataStorageException("error"));

        createGroupCommand.execute();

        assertEquals("creating group failed", createGroupCommand.getMessage());
        verify(groupManager, times(1)).createGroup("group1",
            "user1", "user2", "user3");
    }

    @Test
    public void testExecuteNotLoggedIn() throws DataStorageException {
        CreateGroupCommand notLoggedInCommand = new CreateGroupCommand(false,
            "group1",
            "user1", groupManager, "user2", "user3");

        notLoggedInCommand.execute();

        assertEquals("can't create group when not logged in", notLoggedInCommand.getMessage());
        verify(groupManager, never()).createGroup(any(), any(), any());
    }
}
