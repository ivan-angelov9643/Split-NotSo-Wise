package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.CheckNotificationsCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.NotificationManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CheckNotificationsCommandTest {

    private NotificationManager notificationManager;
    private CheckNotificationsCommand checkNotificationsCommand;

    @BeforeEach
    public void setUp() {
        notificationManager = mock();
        checkNotificationsCommand = new CheckNotificationsCommand(true, "user1", notificationManager);
    }

    @Test
    public void testExecuteWithNotifications() throws DataStorageException {
        List<String> notifications = List.of("notification1", "notification2");
        when(notificationManager.getNotifications(anyString())).thenReturn(notifications);

        checkNotificationsCommand.execute();

        String expectedMessage = "Notifications: " + System.lineSeparator() +
            "* notification1" + System.lineSeparator() +
            "* notification2";

        assertEquals(expectedMessage, checkNotificationsCommand.getMessage());
    }

    @Test
    public void testExecuteNoNotifications() throws DataStorageException {
        when(notificationManager.getNotifications(anyString())).thenReturn(List.of());

        checkNotificationsCommand.execute();

        assertEquals("no notifications to show", checkNotificationsCommand.getMessage());
    }

    @Test
    public void testExecuteNotLoggedIn() {
        CheckNotificationsCommand notLoggedInCommand = new CheckNotificationsCommand(false,
            "user1", notificationManager);

        notLoggedInCommand.execute();

        assertEquals("can't see notifications when not logged in", notLoggedInCommand.getMessage());
    }
}
