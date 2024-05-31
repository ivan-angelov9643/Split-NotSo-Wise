package bg.sofia.uni.fmi.mjt.splitnotsowise.server.database;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.DebtManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.FriendshipManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.GroupManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.NotificationManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.UserManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.hasher.PasswordHasher;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.PaidStatus;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.SplitFriendStatus;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.SplitGroupStatus;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DebtManagerTest {

    private DebtManager debtManager;
    private UserManager userManager;
    private FriendshipManager friendshipManager;
    private GroupManager groupManager;
    private NotificationManager notificationManager;

    @BeforeEach
    public void setUp() {
        userManager = Mockito.mock();
        friendshipManager = Mockito.mock();
        groupManager = Mockito.mock();
        notificationManager = Mockito.mock();

        UserManager.setInstance(userManager);
        FriendshipManager.setInstance(friendshipManager);
        GroupManager.setInstance(groupManager);
        NotificationManager.setInstance(notificationManager);

        DebtManager.setProjectName("");
        DebtManager.setDebtsFileNamePostfix("_test_debts.txt");
        DebtManager.setPaymentsFileNamePostfix("_test_payments.txt");

        DebtManager.resetInstance();
        debtManager = DebtManager.getInstance();
    }

    @AfterEach
    public void cleanup() {
        UserManager.resetInstance();
        FriendshipManager.resetInstance();
        GroupManager.resetInstance();
        NotificationManager.resetInstance();

        DebtManager.resetProjectName();
        DebtManager.resetDebtsFileNamePostfix();
        DebtManager.resetPaymentsFileNamePostfix();
    }

    @Test
    public void testLoadDebts() throws IOException, DataStorageException {
        Path testDebtsFilePath = Path.of("user1_test_debts.txt");
        if (Files.exists(testDebtsFilePath)) {
            Files.delete(testDebtsFilePath);
        }
        Files.createFile(testDebtsFilePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testDebtsFilePath.toString()))) {
            writer.write("payee, amount\n");
            writer.write("user2,10.0\n");
            writer.write("user3,5.0\n");
        }

        Map<String, User> userByUsername = Map.of(
            "user1", new User("First1", "Last1", "user1",
                PasswordHasher.hash("password")),
            "user2", new User("First2", "Last2", "user2",
                PasswordHasher.hash("password")),
            "user3", new User("First3", "Last3", "user3",
                PasswordHasher.hash("password"))
        );
        when(userManager.getUserByUsernameMap()).thenReturn(userByUsername);

        debtManager.loadDebts();

        Map<String, Map<String, Double>> payers = debtManager.getPayersMap();
        Map<String, Map<String, Double>> payees = debtManager.getPayeesMap();

        assertTrue(payers.containsKey("user1"));
        assertEquals(10.0, payers.get("user1").get("user2"));
        assertEquals(5.0, payers.get("user1").get("user3"));

        assertTrue(payees.containsKey("user2"));
        assertTrue(payees.containsKey("user3"));
        assertEquals(10.0, payees.get("user2").get("user1"));
        assertEquals(5.0, payees.get("user3").get("user1"));

        Files.delete(testDebtsFilePath);
    }

    @Test
    public void testLoadPayments() throws IOException, DataStorageException {
        Path testPaymentsFilePath = Path.of("user1_test_payments.txt");
        if (Files.exists(testPaymentsFilePath)) {
            Files.delete(testPaymentsFilePath);
        }
        Files.createFile(testPaymentsFilePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testPaymentsFilePath.toString()))) {
            writer.write("payee, amount\n");
            writer.write("user2,10.0\n");
            writer.write("user3,5.0\n");
        }

        Map<String, User> userByUsername = Map.of(
            "user1", new User("First1", "Last1", "user1",
                PasswordHasher.hash("password")),
            "user2", new User("First2", "Last2", "user2",
                PasswordHasher.hash("password")),
            "user3", new User("First3", "Last3", "user3",
                PasswordHasher.hash("password"))
        );
        when(userManager.getUserByUsernameMap()).thenReturn(userByUsername);
        when(friendshipManager.friendshipExist("user2", "user1")).thenReturn(true);
        when(friendshipManager.friendshipExist("user3", "user1")).thenReturn(true);

        debtManager.splitFriend("user1", "user2", 20);
        debtManager.splitFriend("user1", "user3", 10);

        debtManager.loadPayments();

        Map<String, Map<String, List<Double>>> paymentsByUsername = debtManager.getPaymentsByUsernameMap();

        assertEquals(1, paymentsByUsername.size());
        assertTrue(paymentsByUsername.containsKey("user1"));
        assertEquals(2, paymentsByUsername.get("user1").size());
        assertEquals(1, paymentsByUsername.get("user1").get("user2").size());
        assertEquals(1, paymentsByUsername.get("user1").get("user3").size());
        assertEquals(10.0, paymentsByUsername.get("user1").get("user2").get(0));
        assertEquals(5.0, paymentsByUsername.get("user1").get("user3").get(0));

        Files.delete(testPaymentsFilePath);
    }

    @Test
    public void testSplitFriendUserNotExist() throws DataStorageException {
        when(userManager.getUserByUsernameMap()).thenReturn(Map.of());

        SplitFriendStatus result = debtManager.splitFriend("user1", "user2", 10.0);

        assertEquals(SplitFriendStatus.USER_DOES_NOT_EXIST, result);
    }

    @Test
    public void testSplitFriendFriendshipNotExist() throws DataStorageException {
        Map<String, User> userByUsername = Map.of(
            "user1", new User("First1", "Last1", "user1",
                PasswordHasher.hash("password"))
        );
        when(userManager.getUserByUsernameMap()).thenReturn(userByUsername);
        when(friendshipManager.friendshipExist(anyString(), anyString())).thenReturn(false);

        SplitFriendStatus result = debtManager.splitFriend("user1", "user2", 10.0);

        assertEquals(SplitFriendStatus.FRIENDSHIP_DOES_NOT_EXIST, result);
    }

    @Test
    public void testSplitFriendSuccess() throws DataStorageException, IOException {
        Map<String, User> userByUsername = Map.of(
            "user1", new User("First1", "Last2", "user1",
                PasswordHasher.hash("password")),
            "user2", new User("First2", "Last2", "user2",
                PasswordHasher.hash("password"))
        );
        when(userManager.getUserByUsernameMap()).thenReturn(userByUsername);
        when(friendshipManager.friendshipExist(anyString(), anyString())).thenReturn(true);

        SplitFriendStatus result = debtManager.splitFriend("user1", "user2", 10.0);

        assertEquals(SplitFriendStatus.SUCCESS, result);
        verify(notificationManager, times(1))
            .addAmountSplitNotification("user1", "user2", 5.0);

        Files.delete(Path.of("user1_test_debts.txt"));
    }

    @Test
    public void testSplitGroupGroupDoesNotExist() throws DataStorageException {
        when(groupManager.getGroupMembersByGroupNameMap()).thenReturn(Map.of());

        SplitGroupStatus result = debtManager.splitGroup("group1", "user1", 10.0);

        assertEquals(SplitGroupStatus.GROUP_DOES_NOT_EXIST, result);
    }

    @Test
    public void testSplitGroupUserNotInGroup() throws DataStorageException {
        when(groupManager.getGroupMembersByGroupNameMap()).thenReturn(Map.of("group1", Set.of("user2")));

        SplitGroupStatus result = debtManager.splitGroup("group1", "user1", 10.0);

        assertEquals(SplitGroupStatus.USER_NOT_IN_GROUP, result);
    }

    @Test
    public void testSplitGroupSuccess() throws DataStorageException, IOException {
        when(groupManager.getGroupMembersByGroupNameMap()).thenReturn(Map.of("group1", Set.of("user1", "user2")));

        SplitGroupStatus result = debtManager.splitGroup("group1", "user1", 10.0);

        assertEquals(SplitGroupStatus.SUCCESS, result);
        verify(notificationManager, times(1))
            .addAmountSplitNotification(anyString(), anyString(), anyDouble());

        Files.delete(Path.of("user2_test_debts.txt"));
    }

    @Test
    public void testPayDebtSameUsernames() throws DataStorageException {
        PaidStatus result = debtManager.payDebt("user1", "user1", 10.0);

        assertEquals(PaidStatus.SAME_USERNAMES, result);
    }

    @Test
    public void testPayDebtUserDoesNotExist() throws DataStorageException {
        when(userManager.getUserByUsernameMap()).thenReturn(Map.of());

        PaidStatus result = debtManager.payDebt("user1", "user2", 10.0);

        assertEquals(PaidStatus.USER_DOES_NOT_EXIST, result);
    }

    @Test
    public void testPayDebtPayerDoesNotOwePayee() throws DataStorageException {
        Map<String, User> userByUsername = Map.of(
            "user1", new User("First1", "Last1", "user1",
                PasswordHasher.hash("password")),
            "user2", new User("First2", "Last2", "user2",
                PasswordHasher.hash("password")));
        when(userManager.getUserByUsernameMap()).thenReturn(userByUsername);

        PaidStatus result = debtManager.payDebt("user1", "user2", 10.0);

        assertEquals(PaidStatus.PAYER_DOES_NOT_OWE_PAYEE, result);
    }

    @Test
    public void testPayDebtSuccess() throws IOException, DataStorageException {
        Path testDebtsFilePath = Path.of("user1_test_debts.txt");
        Path testPaymentsFilePath = Path.of("user1_test_payments.txt");
        if (Files.exists(testDebtsFilePath)) {
            Files.delete(testDebtsFilePath);
        }
        if (Files.exists(testPaymentsFilePath)) {
            Files.delete(testPaymentsFilePath);
        }
        Files.createFile(testDebtsFilePath);
        Files.createFile(testPaymentsFilePath);

        Map<String, User> userByUsername = Map.of(
            "user1", new User("First1", "Last1", "user1",
                PasswordHasher.hash("password")),
            "user2", new User("First2", "Last2", "user2",
                PasswordHasher.hash("password")));
        when(userManager.getUserByUsernameMap()).thenReturn(userByUsername);
        when(friendshipManager.friendshipExist("user2", "user1")).thenReturn(true);

        debtManager.splitFriend("user1", "user2", 10.0);
        PaidStatus result = debtManager.payDebt("user1", "user2", 10.0);

        assertEquals(PaidStatus.SUCCESS, result);
        verify(notificationManager, times(1))
            .addPaymentApprovedNotification(anyString(), anyString(), anyDouble());

        Files.delete(testDebtsFilePath);
        Files.delete(testPaymentsFilePath);
    }
}
