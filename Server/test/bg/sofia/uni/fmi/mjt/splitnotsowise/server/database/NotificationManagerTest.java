package bg.sofia.uni.fmi.mjt.splitnotsowise.server.database;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.NotificationManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.UserManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.hasher.PasswordHasher;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NotificationManagerTest {
    private static UserManager userManager;
    private static NotificationManager notificationManager;

    @BeforeEach
    public void setUp() throws IOException {
        NotificationManager.setProjectName("");
        NotificationManager.setNotificationsFileNamePostfix("_test_notifications.txt");

        userManager = mock();
        UserManager.setInstance(userManager);

        NotificationManager.resetInstance();
        notificationManager = NotificationManager.getInstance();
        notificationManager.getNotificationsByUsernameMap().clear();
    }


    @AfterEach
    public void cleanup() {
        UserManager.resetInstance();

        NotificationManager.resetProjectName();
        NotificationManager.resetNotificationsFileNamePostfix();
    }

    @Test
    public void testLoadNotifications() throws Exception {
        Path testNotificationsFilePath = Path.of("user1_test_notifications.txt");
        if (Files.exists(testNotificationsFilePath)) {
            Files.delete(testNotificationsFilePath);
        }
        Files.createFile(testNotificationsFilePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testNotificationsFilePath.toString()))) {
            writer.write("user2 approved your payment of 10.0 lv\n");
            writer.write("user3 added 5.0 lv to your debt to him\n");
            writer.write("user4 added you as a friend\n");
            writer.write("user5 added you to \"group1\" group\n");
        }

        Map<String, List<String>> notificationsByUsernameMap = notificationManager.getNotificationsByUsernameMap();
        when(userManager.getUserByUsernameMap()).thenReturn(Map.of(
            "user1", new User("User1", "Last1", "user1",
                PasswordHasher.hash("password")),
            "user2", new User("User2", "Last2", "user2",
                PasswordHasher.hash("password")),
            "user3", new User("User3", "Last3", "user3",
                PasswordHasher.hash("password")),
            "user4", new User("User4", "Last4", "user4",
                PasswordHasher.hash("password")),
            "user5", new User("User5", "Last5", "user5",
                PasswordHasher.hash("password"))));

        notificationManager.loadNotifications();

        List<String> expected = List.of("user2 approved your payment of 10.0 lv",
            "user3 added 5.0 lv to your debt to him",
            "user4 added you as a friend",
            "user5 added you to \"group1\" group");

        assertEquals(1, notificationsByUsernameMap.size());
        assertTrue(notificationsByUsernameMap.containsKey("user1"));

        assertEquals(4, notificationsByUsernameMap.get("user1").size());
        assertIterableEquals(expected, notificationsByUsernameMap.get("user1"));
        Files.delete(testNotificationsFilePath);
    }

    @Test
    public void testAddPaymentApprovedNotification() throws DataStorageException {
        notificationManager.addPaymentApprovedNotification("user1", "user2", 10.0);
        List<String> notifications = notificationManager.getNotifications("user1");

        assertEquals(1, notifications.size());
        assertEquals("user2 approved your payment of 10.0 lv", notifications.get(0));
    }

    @Test
    public void testAddAmountSplitNotification() throws DataStorageException {
        notificationManager.addAmountSplitNotification("user1", "user2", 5.0);
        List<String> notifications = notificationManager.getNotifications("user1");

        assertEquals(1, notifications.size());
        assertEquals("user2 added 5.0 lv to your debt to him", notifications.get(0));
    }

    @Test
    public void testAddFriendAddedNotification() throws DataStorageException {
        notificationManager.addFriendAddedNotification("user1", "user2");
        List<String> notifications = notificationManager.getNotifications("user1");

        assertEquals(1, notifications.size());
        assertEquals("user2 added you as a friend", notifications.get(0));
    }

    @Test
    public void testAddAddedToGroupNotification() throws DataStorageException {
        notificationManager.addAddedToGroupNotification("user1", "user2", "group1");
        List<String> notifications = notificationManager.getNotifications("user1");

        assertEquals(1, notifications.size());
        assertEquals("user2 added you to \"group1\" group", notifications.get(0));
    }
}
