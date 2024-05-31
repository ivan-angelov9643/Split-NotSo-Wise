package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.StatusCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.DebtManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.UserManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.hasher.PasswordHasher;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatusCommandTest {

    private UserManager userManager;
    private DebtManager debtManager;
    private StatusCommand statusCommand;

    @BeforeEach
    public void setUp() {
        userManager = mock();
        debtManager = mock();
        statusCommand = new StatusCommand(true, "user1", userManager, debtManager);
    }

    @Test
    public void testExecuteNoMoneyRelations() {
        when(debtManager.getPayeesMap()).thenReturn(new HashMap<>());
        when(debtManager.getPayersMap()).thenReturn(new HashMap<>());

        statusCommand.execute();

        assertEquals("you don't have any money relations", statusCommand.getMessage());
    }

    @Test
    public void testExecuteWithPayers() {
        Map<String, Map<String, Double>> payeesMap = new HashMap<>(Map.of("user1", Map.of("user2", 10.0)));
        when(debtManager.getPayeesMap()).thenReturn(payeesMap);
        when(debtManager.getPayersMap()).thenReturn(new HashMap<>());

        Map<String, User> userMap = new HashMap<>();
        userMap.put("user2", new User("First2", "Last2", "user2",
            PasswordHasher.hash("password")));
        when(userManager.getUserByUsernameMap()).thenReturn(userMap);

        statusCommand.execute();

        assertEquals("* First2 Last2 (user2): owes you 10.0 lv", statusCommand.getMessage());
    }

    @Test
    public void testExecuteWithPayees() {
        Map<String, Map<String, Double>> payersMap = new HashMap<>(Map.of("user1", Map.of("user2", 5.0)));
        when(debtManager.getPayersMap()).thenReturn(payersMap);
        when(debtManager.getPayeesMap()).thenReturn(new HashMap<>());

        Map<String, User> userMap = new HashMap<>();
        userMap.put("user2", new User("First2", "Last2", "user2",
            PasswordHasher.hash("password")));
        when(userManager.getUserByUsernameMap()).thenReturn(userMap);

        statusCommand.execute();

        assertEquals("* First2 Last2 (user2): you owe 5.0 lv", statusCommand.getMessage());
    }

    @Test
    public void testExecuteWithPayersAndPayees() {
        Map<String, Map<String, Double>> payeesMap = new HashMap<>(Map.of("user1", Map.of("user2", 10.0)));
        Map<String, Map<String, Double>> payersMap = new HashMap<>(Map.of("user1", Map.of("user3", 5.0)));

        when(debtManager.getPayersMap()).thenReturn(payersMap);
        when(debtManager.getPayeesMap()).thenReturn(payeesMap);

        Map<String, User> userMap = new HashMap<>();
        userMap.put("user2", new User("First2", "Last2", "user2",
            PasswordHasher.hash("password")));
        userMap.put("user3", new User("First3", "Last3", "user3",
            PasswordHasher.hash("password")));
        when(userManager.getUserByUsernameMap()).thenReturn(userMap);

        statusCommand.execute();

        String expectedMessage = "* First2 Last2 (user2): owes you 10.0 lv" +
            System.lineSeparator() +
            "* First3 Last3 (user3): you owe 5.0 lv";

        assertEquals(expectedMessage, statusCommand.getMessage());
    }

    @Test
    public void testExecuteNotLoggedIn() {
        StatusCommand notLoggedInCommand =
            new StatusCommand(false, "user1", userManager, debtManager);

        notLoggedInCommand.execute();

        assertEquals("can't see status when not logged in", notLoggedInCommand.getMessage());
    }
}
