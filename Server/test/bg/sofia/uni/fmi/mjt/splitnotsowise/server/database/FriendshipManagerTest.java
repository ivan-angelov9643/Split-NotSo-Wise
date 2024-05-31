package bg.sofia.uni.fmi.mjt.splitnotsowise.server.database;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.FriendshipManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.NotificationManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.UserManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.hasher.PasswordHasher;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.AddFriendshipStatus;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FriendshipManagerTest {

    private static UserManager userManager;
    private static NotificationManager notificationManager;
    private static FriendshipManager friendshipManager;
    public final Path testFriendsFilePath = Paths.get("testFriends.txt");

    @BeforeEach
    public void setUp() throws IOException {
        Files.createFile(testFriendsFilePath);
        FriendshipManager.setFriendsFilePath(testFriendsFilePath);

        userManager = mock();
        notificationManager = mock();
        UserManager.setInstance(userManager);
        NotificationManager.setInstance(notificationManager);

        FriendshipManager.resetInstance();
        friendshipManager = FriendshipManager.getInstance();
        friendshipManager.getFriendsByUsernameMap().clear();
    }

    @AfterEach
    public void cleanup() throws IOException {
        UserManager.resetInstance();
        NotificationManager.resetInstance();

        FriendshipManager.resetFriendsFilePath();
        Files.delete(testFriendsFilePath);
    }

    @Test
    public void testLoadFriendships() throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFriendsFilePath.toString()))) {
            writer.write("friend1, friend2\n");
            writer.write("user1,user2\n");
            writer.write("user2,user3\n");
        }

        friendshipManager.loadFriendships();

        Map<String, Set<String>> friendsByUsernameMap = friendshipManager.getFriendsByUsernameMap();
        assertEquals(3, friendsByUsernameMap.size());
        assertTrue(friendsByUsernameMap.containsKey("user1"));
        assertTrue(friendsByUsernameMap.containsKey("user2"));
        assertTrue(friendsByUsernameMap.containsKey("user3"));
        assertTrue(friendsByUsernameMap.get("user1").contains("user2"));
        assertTrue(friendsByUsernameMap.get("user2").contains("user1"));
        assertTrue(friendsByUsernameMap.get("user2").contains("user3"));
        assertTrue(friendsByUsernameMap.get("user3").contains("user2"));
    }

    @Test
    public void testFriendshipExist() {
        Map<String, Set<String>> friendsByUsernameMap = friendshipManager.getFriendsByUsernameMap();
        friendsByUsernameMap.put("user1", Set.of("user2"));
        friendsByUsernameMap.put("user2", Set.of("user1", "user3"));
        friendsByUsernameMap.put("user3", Set.of("user2"));

        assertTrue(friendshipManager.friendshipExist("user1", "user2"));
        assertTrue(friendshipManager.friendshipExist("user2", "user1"));
        assertTrue(friendshipManager.friendshipExist("user2", "user3"));
        assertTrue(friendshipManager.friendshipExist("user3", "user2"));

        assertFalse(friendshipManager.friendshipExist("user1", "user3"));
        assertFalse(friendshipManager.friendshipExist("user3", "user1"));
    }

    @Test
    public void testAddFriendshipSuccess() throws DataStorageException {
        User user1 = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));
        User user2 = new User("First2", "Last2", "user2",
            PasswordHasher.hash("password"));
        Map<String, User> usersByUsernameMap = new HashMap<>(Map.of("user1", user1, "user2", user2));
        when(userManager.getUserByUsernameMap()).thenReturn(usersByUsernameMap);

        AddFriendshipStatus status = friendshipManager.addFriendship("user1", "user2");
        Map<String, Set<String>> friendsByUsernameMap = friendshipManager.getFriendsByUsernameMap();

        assertEquals(AddFriendshipStatus.SUCCESS, status);
        assertTrue(friendsByUsernameMap.containsKey("user1"));
        assertTrue(friendsByUsernameMap.containsKey("user2"));
        assertTrue(friendsByUsernameMap.get("user1").contains("user2"));
        assertTrue(friendsByUsernameMap.get("user2").contains("user1"));
    }

    @Test
    public void testAddFriendshipSameUsernames() throws DataStorageException {
        AddFriendshipStatus status = friendshipManager.addFriendship("user1", "user1");

        assertEquals(AddFriendshipStatus.SAME_USERNAMES, status);
    }

    @Test
    public void testAddFriendshipUserDoesNotExist() throws DataStorageException {
        when(userManager.getUserByUsernameMap()).thenReturn(Map.of());

        AddFriendshipStatus status = friendshipManager.addFriendship("user1", "user2");

        assertEquals(AddFriendshipStatus.USER_DOES_NOT_EXIST, status);
    }

    @Test
    public void testAddFriendshipAlreadyExists() throws DataStorageException {
        User user1 = new User("First1", "Last1", "user1", "password1");
        User user2 = new User("First2", "Last2", "user2", "password2");
        Map<String, User> usersByUsernameMap = new HashMap<>(Map.of("user1", user1, "user2", user2));
        when(userManager.getUserByUsernameMap()).thenReturn(usersByUsernameMap);

        friendshipManager.addFriendship("user1", "user2");
        AddFriendshipStatus status = friendshipManager.addFriendship("user1", "user2");

        assertEquals(AddFriendshipStatus.FRIENDSHIP_ALREADY_EXISTS, status);
    }
}
