package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.CheckGroupsCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.GroupManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CheckGroupsCommandTest {

    private GroupManager groupManager;
    private CheckGroupsCommand checkGroupsCommand;

    @BeforeEach
    public void setUp() {
        groupManager = mock();
        checkGroupsCommand = new CheckGroupsCommand(true, "user1", groupManager);
    }

    @Test
    public void testExecuteNoGroups() {
        when(groupManager.getGroupNamesByUsernameMap()).thenReturn(new HashMap<>());

        checkGroupsCommand.execute();

        assertEquals("you are not member of any groups", checkGroupsCommand.getMessage());
    }

    @Test
    public void testExecuteWithGroups() {
        Map<String, Set<String>> groupMembersByGroupName = new HashMap<>();
        Map<String, Set<String>> groupNamesByUsername = new HashMap<>();
        groupNamesByUsername.put("user1", new HashSet<>(Set.of("group1")));
        groupMembersByGroupName.put("group1", new HashSet<>(Set.of("user1", "user2")));

        when(groupManager.getGroupNamesByUsernameMap()).thenReturn(groupNamesByUsername);
        when(groupManager.getGroupMembersByGroupNameMap()).thenReturn(groupMembersByGroupName);

        checkGroupsCommand.execute();

        String expectedMessage = "Groups:" + System.lineSeparator() +
            "* group1: user1,user2";

        assertEquals(expectedMessage, checkGroupsCommand.getMessage());
    }

    @Test
    public void testExecuteNotLoggedIn() {
        CheckGroupsCommand notLoggedInCommand = new CheckGroupsCommand(false, "user1", groupManager);

        notLoggedInCommand.execute();

        assertEquals("can't see groups when not logged in", notLoggedInCommand.getMessage());
    }
}
