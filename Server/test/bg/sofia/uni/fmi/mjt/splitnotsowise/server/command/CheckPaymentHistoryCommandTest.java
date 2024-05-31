package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.CheckPaymentHistoryCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.DebtManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.UserManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.hasher.PasswordHasher;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CheckPaymentHistoryCommandTest {

    private UserManager userManager;
    private DebtManager debtManager;
    private CheckPaymentHistoryCommand checkPaymentHistoryCommand;

    @BeforeEach
    public void setUp() {
        userManager = mock(UserManager.class);
        debtManager = mock(DebtManager.class);
        checkPaymentHistoryCommand = new CheckPaymentHistoryCommand(true, "user1", userManager,
            debtManager);
    }

    @Test
    public void testExecuteWithPayments() {
        Map<String, User> usersByUsername = Map.of("user2", new User("First2", "Last2",
            "user2", PasswordHasher.hash("password")));
        Map<String, List<Double>> paymentsMap = Map.of("user2", List.of(10.0, 20.0));
        Map<String, Map<String, List<Double>>> paymentsByUsernameMap = Map.of("user1", paymentsMap);

        when(userManager.getUserByUsernameMap()).thenReturn(usersByUsername);
        when(debtManager.getPaymentsByUsernameMap()).thenReturn(paymentsByUsernameMap);

        checkPaymentHistoryCommand.execute();

        String expectedMessage = "* you paid First2 Last2 (user2) 10.0 lv" + System.lineSeparator() +
            "* you paid First2 Last2 (user2) 20.0 lv";

        assertEquals(expectedMessage, checkPaymentHistoryCommand.getMessage());
    }

    @Test
    public void testExecuteWithNoPayments() {
        when(debtManager.getPaymentsByUsernameMap()).thenReturn(Map.of());

        checkPaymentHistoryCommand.execute();

        assertEquals("no history to show", checkPaymentHistoryCommand.getMessage());
    }

    @Test
    public void testExecuteNotLoggedIn() {
        CheckPaymentHistoryCommand notLoggedInCommand = new CheckPaymentHistoryCommand(false,
            "user1", userManager, debtManager);

        notLoggedInCommand.execute();

        assertEquals("can't see payments history when not logged in", notLoggedInCommand.getMessage());
    }
}
