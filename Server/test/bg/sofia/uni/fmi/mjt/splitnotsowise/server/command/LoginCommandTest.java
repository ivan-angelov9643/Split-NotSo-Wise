package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.LoginCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.NotificationManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.UserManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.hasher.PasswordHasher;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoginCommandTest {

    private SelectionKey key;
    private UserManager userManager;
    private NotificationManager notificationManager;
    private LoginCommand loginCommand;

    @BeforeEach
    public void setUp() {
        key = mock();
        userManager = mock();
        notificationManager = mock();
        loginCommand = new LoginCommand(key, "user1", "password", userManager, notificationManager);
    }

    @Test
    public void testExecuteSuccessWithNotifications() throws DataStorageException {
        User user = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));
        Map<String, User> userMap = new HashMap<>(Map.of("user1", user));
        List<String> notifications = List.of("notification1", "notification2");

        when(userManager.getUserByUsernameMap()).thenReturn(userMap);
        when(notificationManager.getNotifications(anyString())).thenReturn(notifications);

        loginCommand.execute();

        assertEquals("logged in successfully" + System.lineSeparator() +
            "* notification1" + System.lineSeparator() +
            "* notification2", loginCommand.getMessage());
        verify(notificationManager, times(1)).getNotifications("user1");
    }

    @Test
    public void testExecuteSuccessNoNotifications() throws DataStorageException {
        User user = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));
        Map<String, User> userMap = new HashMap<>(Map.of("user1", user));

        when(userManager.getUserByUsernameMap()).thenReturn(userMap);
        when(notificationManager.getNotifications(anyString())).thenReturn(List.of());

        loginCommand.execute();

        assertEquals("logged in successfully" + System.lineSeparator() +
            "no notifications to show", loginCommand.getMessage());
        verify(notificationManager, times(1)).getNotifications("user1");
    }

    @Test
    public void testExecuteUserDoesNotExist() throws DataStorageException {
        when(userManager.getUserByUsernameMap()).thenReturn(new HashMap<>());

        loginCommand.execute();

        assertEquals("user with this username doesn't exist", loginCommand.getMessage());
        verify(notificationManager, never()).getNotifications(any());
    }

    @Test
    public void testExecuteWrongPassword() throws DataStorageException {
        User user = new User("First1", "Last1", "user1",
            PasswordHasher.hash("differentPassword"));
        Map<String, User> userMap = new HashMap<>(Map.of("user1", user));

        when(userManager.getUserByUsernameMap()).thenReturn(userMap);

        loginCommand.execute();

        assertEquals("wrong password", loginCommand.getMessage());
        verify(notificationManager, never()).getNotifications(any());
    }

    @Test
    public void testExecuteAlreadyLoggedIn() throws DataStorageException {
        User user = new User("First2", "Last2", "user2",
            PasswordHasher.hash("password"));
        when(key.attachment()).thenReturn(user);

        loginCommand.execute();

        assertEquals("can't login into another account when you are logged in", loginCommand.getMessage());
        verify(notificationManager, never()).getNotifications(any());
    }
}
