package bg.sofia.uni.fmi.mjt.splitnotsowise.server.database;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.GroupManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.NotificationManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.UserManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.hasher.PasswordHasher;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.CreateGroupStatus;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GroupManagerTest {

    private static UserManager userManager;
    private static NotificationManager notificationManager;
    private static GroupManager groupManager;
    public final Path testGroupsFilePath = Paths.get("testGroups.txt");

    @BeforeEach
    public void setUp() throws IOException {
        Files.createFile(testGroupsFilePath);
        GroupManager.setGroupsFilePath(testGroupsFilePath);

        userManager = mock();
        notificationManager = mock();
        UserManager.setInstance(userManager);
        NotificationManager.setInstance(notificationManager);

        GroupManager.resetInstance();
        groupManager = GroupManager.getInstance();
        groupManager.getGroupMembersByGroupNameMap().clear();
    }

    @AfterEach
    public void cleanup() throws IOException {
        UserManager.resetInstance();
        NotificationManager.resetInstance();

        GroupManager.resetGroupsFilePath();
        Files.delete(testGroupsFilePath);
    }

    @Test
    public void testLoadGroups() throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testGroupsFilePath.toString()))) {
            writer.write("group name, members\n");
            writer.write("group1,user1;user2;user3\n");
            writer.write("group2,user2;user3;user4\n");
        }

        groupManager.loadGroups();
        Map<String, Set<String>> groupMembersByGroupNameMap = groupManager.getGroupMembersByGroupNameMap();
        Map<String, Set<String>> groupNamesByUsernameMap = groupManager.getGroupNamesByUsernameMap();

        assertEquals(2, groupMembersByGroupNameMap.size());
        assertTrue(groupMembersByGroupNameMap.containsKey("group1"));
        assertTrue(groupMembersByGroupNameMap.containsKey("group2"));
        assertTrue(groupNamesByUsernameMap.containsKey("user1"));
        assertTrue(groupNamesByUsernameMap.containsKey("user2"));
        assertTrue(groupNamesByUsernameMap.get("user1").contains("group1"));
        assertTrue(groupNamesByUsernameMap.get("user2").contains("group1"));
        assertTrue(groupNamesByUsernameMap.get("user2").contains("group2"));
        assertTrue(groupMembersByGroupNameMap.get("group1").contains("user1"));
        assertTrue(groupMembersByGroupNameMap.get("group1").contains("user2"));
        assertTrue(groupMembersByGroupNameMap.get("group1").contains("user3"));
        assertTrue(groupMembersByGroupNameMap.get("group2").contains("user2"));
        assertTrue(groupMembersByGroupNameMap.get("group2").contains("user3"));
        assertTrue(groupMembersByGroupNameMap.get("group2").contains("user4"));
    }

    @Test
    public void testCreateGroupSuccess() throws DataStorageException {
        User user1 = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));
        User user2 = new User("First2", "Last2", "user2",
            PasswordHasher.hash("password"));
        Map<String, User> usersByUsernameMap = new HashMap<>(Map.of("user1", user1, "user2", user2));
        when(userManager.getUserByUsernameMap()).thenReturn(usersByUsernameMap);

        CreateGroupStatus status = groupManager.createGroup("group1", "user1", "user2");
        Map<String, Set<String>> groupMembersByGroupNameMap = groupManager.getGroupMembersByGroupNameMap();
        Map<String, Set<String>> groupNamesByUsernameMap = groupManager.getGroupNamesByUsernameMap();

        assertEquals(CreateGroupStatus.SUCCESS, status);
        assertTrue(groupMembersByGroupNameMap.containsKey("group1"));
        assertTrue(groupMembersByGroupNameMap.get("group1").contains("user1"));
        assertTrue(groupMembersByGroupNameMap.get("group1").contains("user2"));
        assertTrue(groupNamesByUsernameMap.containsKey("user1"));
        assertTrue(groupNamesByUsernameMap.containsKey("user2"));
        assertTrue(groupNamesByUsernameMap.get("user1").contains("group1"));
        assertTrue(groupNamesByUsernameMap.get("user2").contains("group1"));
    }

    @Test
    public void testCreateGroupAlreadyExists() throws DataStorageException {
        User user1 = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));
        User user2 = new User("First2", "Last2", "user2",
            PasswordHasher.hash("password"));
        Map<String, User> usersByUsernameMap = new HashMap<>(Map.of("user1", user1, "user2", user2));
        when(userManager.getUserByUsernameMap()).thenReturn(usersByUsernameMap);

        groupManager.createGroup("group1", "user1", "user2");
        CreateGroupStatus status = groupManager.createGroup("group1", "user3", "user4");

        assertEquals(CreateGroupStatus.GROUP_NAME_ALREADY_EXISTS, status);
    }

    @Test
    public void testCreateGroupMemberDoesNotExist() throws DataStorageException {
        when(userManager.getUserByUsernameMap()).thenReturn(Map.of());

        CreateGroupStatus status = groupManager.createGroup("group1", "user1", "user2");

        assertEquals(CreateGroupStatus.MEMBER_DOES_NOT_EXIST, status);
    }

    @Test
    public void testCreateGroupMembersContainCreator() throws DataStorageException {
        User user1 = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));
        User user2 = new User("First2", "Last2", "user2",
            PasswordHasher.hash("password"));
        Map<String, User> usersByUsernameMap = new HashMap<>(Map.of("user1", user1, "user2", user2));
        when(userManager.getUserByUsernameMap()).thenReturn(usersByUsernameMap);

        CreateGroupStatus status = groupManager.createGroup("group1", "user1",
            "user1", "user2");

        assertEquals(CreateGroupStatus.MEMBERS_CONTAIN_CREATORS_USERNAME, status);
    }
}
