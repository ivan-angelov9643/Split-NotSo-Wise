package bg.sofia.uni.fmi.mjt.splitnotsowise.server.io;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.DebtManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.FriendshipManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.GroupManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.NotificationManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.UserManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.hasher.PasswordHasher;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.io.IOHandler;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.io.InputValidator;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.AddFriendshipStatus;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.CreateGroupStatus;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.PaidStatus;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.RegistrationStatus;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.SplitFriendStatus;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.SplitGroupStatus;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IOHandlerTest {

    private IOHandler ioHandler;
    private UserManager userManager;
    private FriendshipManager friendshipManager;
    private GroupManager groupManager;
    private DebtManager debtManager;
    private NotificationManager notificationManager;
    private InputValidator inputValidator;
    private SelectionKey key;

    @BeforeEach
    public void setUp() {
        key = mock();
        userManager = mock();
        notificationManager = mock();
        friendshipManager = mock();
        groupManager = mock();
        debtManager = mock();
        inputValidator = mock();

        UserManager.setInstance(userManager);
        NotificationManager.setInstance(notificationManager);
        FriendshipManager.setInstance(friendshipManager);
        GroupManager.setInstance(groupManager);
        DebtManager.setInstance(debtManager);
        InputValidator.setInstance(inputValidator);

        when(inputValidator.validateInputArgs(any(String[].class))).thenReturn(true);
        IOHandler.resetInstance();
        ioHandler = IOHandler.getInstance();
    }

    @AfterEach
    public void cleanup() {
        UserManager.resetInstance();
        FriendshipManager.resetInstance();
        GroupManager.resetInstance();
        NotificationManager.resetInstance();
        DebtManager.resetInstance();
        InputValidator.resetInstance();
    }

    @Test
    public void testHandleInvalidArguments() {
        when(inputValidator.validateInputArgs(any(String[].class))).thenReturn(false);
        String result = ioHandler.handle("register user1", key);

        assertEquals("invalid arguments, use help", result);
        verify(inputValidator, times(1)).validateInputArgs(any(String[].class));
    }

    @Test
    public void testHandleUnknownCommand() {
        String result = ioHandler.handle("unknown-command", key);

        assertEquals("unknown command, use help", result);
        verify(inputValidator, times(1)).validateInputArgs(any(String[].class));
    }

    @Test
    public void testHandleQuitCommand() {
        String result = ioHandler.handle("quit", key);

        assertEquals("bye, see you soon :)", result);
        verify(inputValidator, times(1)).validateInputArgs(any(String[].class));
    }

    @Test
    public void testHandleHelpCommand() {
        String result = ioHandler.handle("help", key);

        assertEquals("""
        when not logged in:
            help
            register <first name> <last name> <username> <password>
            login <username> <password>
            quit
        when logged in:
            help
            logout
            add-friend <username>
            create-group <group name> <username> ... <username>
            split-friend <amount> <username>
            split-group <amount> <group name>
            paid <amount> <username>
            payment-history
            status
            groups
            notifications
            quit""", result);
        verify(inputValidator, times(1)).validateInputArgs(any(String[].class));
    }

    @Test
    public void testHandleRegisterCommand() throws DataStorageException {
        when(userManager.registerUser(any(User.class))).thenReturn(RegistrationStatus.SUCCESS);

        ioHandler.handle("register First1 Last1 user1 password", key);
        User user = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));

        verify(userManager).registerUser(user);
        verify(inputValidator, times(1)).validateInputArgs(any(String[].class));
    }

    @Test
    public void testHandleLoginCommand() throws DataStorageException {
        when(key.attachment()).thenReturn(null);

        User user1 = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));
        when(userManager.getUserByUsernameMap()).thenReturn(Map.of("user1", user1));
        when(notificationManager.getNotifications(anyString())).thenReturn(List.of());

        ioHandler.handle("login user1 password", key);

        verify(userManager).getUserByUsernameMap();
        verify(notificationManager).getNotifications(anyString());
        verify(inputValidator, times(1)).validateInputArgs(any(String[].class));
    }

    @Test
    public void testHandleLogoutCommand() {
        User user = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));
        when(key.attachment()).thenReturn(user);

        ioHandler.handle("logout", key);

        verify(key).attach(null);
        verify(inputValidator, times(1)).validateInputArgs(any(String[].class));
    }

    @Test
    public void testHandleAddFriendCommand() throws DataStorageException {
        User user = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));
        when(key.attachment()).thenReturn(user);
        when(friendshipManager.addFriendship("user1", "user2"))
            .thenReturn(AddFriendshipStatus.SUCCESS);


        ioHandler.handle("add-friend user2", key);

        verify(friendshipManager).addFriendship("user1", "user2");
        verify(inputValidator, times(1)).validateInputArgs(any(String[].class));
    }

    @Test
    public void testHandleCreateGroupCommand() throws DataStorageException {
        User user = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));
        when(key.attachment()).thenReturn(user);
        when(groupManager.createGroup(any(), any(), any(), any())).thenReturn(CreateGroupStatus.SUCCESS);

        ioHandler.handle("create-group group1 user2 user3", key);

        verify(groupManager).createGroup("group1", "user1", "user2", "user3");
        verify(inputValidator, times(1)).validateInputArgs(any(String[].class));
    }

    @Test
    public void testHandleSplitFriendCommand() throws DataStorageException {
        User user = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));
        when(key.attachment()).thenReturn(user);
        when(debtManager.splitFriend(any(), any(), anyDouble())).thenReturn(SplitFriendStatus.SUCCESS);

        ioHandler.handle("split-friend 10 user2", key);

        verify(debtManager).splitFriend("user2", "user1", 10.0);
        verify(inputValidator, times(1)).validateInputArgs(any(String[].class));
    }

    @Test
    public void testHandleSplitGroupCommand() throws DataStorageException {
        User user = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));
        when(key.attachment()).thenReturn(user);
        when(debtManager.splitGroup(any(), any(), anyDouble())).thenReturn(SplitGroupStatus.SUCCESS);

        ioHandler.handle("split-group 10 group1", key);

        verify(debtManager).splitGroup("group1", "user1", 10.0);
        verify(inputValidator, times(1)).validateInputArgs(any(String[].class));
    }

    @Test
    public void testHandlePaidCommand() throws DataStorageException {
        User user = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));
        when(key.attachment()).thenReturn(user);
        when(debtManager.payDebt(any(), any(), anyDouble())).thenReturn(PaidStatus.SUCCESS);

        ioHandler.handle("paid 10 user2", key);

        verify(debtManager).payDebt("user2", "user1", 10.0);
        verify(inputValidator, times(1)).validateInputArgs(any(String[].class));
    }

    @Test
    public void testHandleStatusCommand() {
        User user = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));
        when(key.attachment()).thenReturn(user);

        ioHandler.handle("status", key);

        verify(debtManager).getPayeesMap();
        verify(debtManager).getPayersMap();
        verify(userManager).getUserByUsernameMap();
        verify(inputValidator, times(1)).validateInputArgs(any(String[].class));
    }

    @Test
    public void testHandleCheckGroupsCommand() {
        User user = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));
        Map<String, Set<String>> groupNamesByUsername = new HashMap<>(Map.of("user1", Set.of("group1")));
        Map<String, Set<String>> groupMembersByGroupName = new HashMap<>(Map.of("group1", Set.of("user1", "user2")));
        when(key.attachment()).thenReturn(user);
        when(groupManager.getGroupNamesByUsernameMap()).thenReturn(groupNamesByUsername);
        when(groupManager.getGroupMembersByGroupNameMap()).thenReturn(groupMembersByGroupName);

        ioHandler.handle("groups", key);

        verify(groupManager).getGroupNamesByUsernameMap();
        verify(inputValidator, times(1)).validateInputArgs(any(String[].class));
    }

    @Test
    public void testHandleCheckNotificationsCommand() throws DataStorageException {
        User user = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));
        when(key.attachment()).thenReturn(user);
        when(notificationManager.getNotifications(any())).thenReturn(List.of("notification1", "notification2"));

        ioHandler.handle("notifications", key);

        verify(notificationManager).getNotifications("user1");
        verify(inputValidator, times(1)).validateInputArgs(any(String[].class));
    }

    @Test
    public void testHandleCheckPaymentHistoryCommand() {
        User user = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));
        User otherUser = new User("First2", "Last2", "user2",
            PasswordHasher.hash("password"));

        Map<String, Map<String, List<Double>>> paymentsByUsername =
            new HashMap<>(Map.of("user1", Map.of("user2", List.of(10.0))));
        Map<String, User> usersByUsername = new HashMap<>(Map.of("user2", otherUser));
        when(key.attachment()).thenReturn(user);
        when(debtManager.getPaymentsByUsernameMap()).thenReturn(paymentsByUsername);
        when(userManager.getUserByUsernameMap()).thenReturn(usersByUsername);

        ioHandler.handle("payment-history", key);

        verify(debtManager).getPaymentsByUsernameMap();
        verify(userManager).getUserByUsernameMap();
        verify(inputValidator, times(1)).validateInputArgs(any(String[].class));
    }
}
