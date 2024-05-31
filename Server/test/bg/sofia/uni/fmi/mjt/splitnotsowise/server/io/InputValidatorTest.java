package bg.sofia.uni.fmi.mjt.splitnotsowise.server.io;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.io.InputValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputValidatorTest {

    private final InputValidator inputValidator = InputValidator.getInstance();

    @Test
    public void testValidateInputArgsHelp() {
        String[] args = {"help"};
        boolean result = inputValidator.validateInputArgs(args);
        assertTrue(result);
    }

    @Test
    public void testValidateInputArgsLogout() {
        String[] args = {"logout"};
        boolean result = inputValidator.validateInputArgs(args);
        assertTrue(result);
    }

    @Test
    public void testValidateInputArgsStatus() {
        String[] args = {"status"};
        boolean result = inputValidator.validateInputArgs(args);
        assertTrue(result);
    }

    @Test
    public void testValidateInputArgsQuit() {
        String[] args = {"quit"};
        boolean result = inputValidator.validateInputArgs(args);
        assertTrue(result);
    }

    @Test
    public void testValidateInputArgsGroups() {
        String[] args = {"groups"};
        boolean result = inputValidator.validateInputArgs(args);
        assertTrue(result);
    }

    @Test
    public void testValidateInputArgsNotifications() {
        String[] args = {"notifications"};
        boolean result = inputValidator.validateInputArgs(args);
        assertTrue(result);
    }

    @Test
    public void testValidateInputArgsPaymentHistory() {
        String[] args = {"payment-history"};
        boolean result = inputValidator.validateInputArgs(args);
        assertTrue(result);
    }

    @Test
    public void testValidateInputArgsRegisterValid() {
        String[] args = {"register", "First1", "Last1", "user1", "password"};
        boolean result = inputValidator.validateInputArgs(args);
        assertTrue(result);
    }

    @Test
    public void testValidateInputArgsRegisterInvalid() {
        String[] args = {"register", "First1", "Last1"};
        boolean result = inputValidator.validateInputArgs(args);
        assertFalse(result);
    }

    @Test
    public void testValidateInputArgsLoginValid() {
        String[] args = {"login", "user1", "password"};
        boolean result = inputValidator.validateInputArgs(args);
        assertTrue(result);
    }

    @Test
    public void testValidateInputArgsLoginInvalid() {
        String[] args = {"login", "user1"};
        boolean result = inputValidator.validateInputArgs(args);
        assertFalse(result);
    }

    @Test
    public void testValidateInputArgsAddFriendValid() {
        String[] args = {"add-friend", "user2"};
        boolean result = inputValidator.validateInputArgs(args);
        assertTrue(result);
    }

    @Test
    public void testValidateInputArgsAddFriendInvalid() {
        String[] args = {"add-friend"};
        boolean result = inputValidator.validateInputArgs(args);
        assertFalse(result);
    }

    @Test
    public void testValidateInputArgsCreateGroupValid() {
        String[] args = {"create-group", "group1", "user1", "user2"};
        boolean result = inputValidator.validateInputArgs(args);
        assertTrue(result);
    }

    @Test
    public void testValidateInputArgsCreateGroupInvalid() {
        String[] args = {"create-group", "group1"};
        boolean result = inputValidator.validateInputArgs(args);
        assertFalse(result);
    }

    @Test
    public void testValidateInputArgsSplitFriendValid() {
        String[] args = {"split-friend", "10", "user2"};
        boolean result = inputValidator.validateInputArgs(args);
        assertTrue(result);
    }

    @Test
    public void testValidateInputArgsSplitFriendInvalid() {
        String[] args = {"split-friend", "10"};
        boolean result = inputValidator.validateInputArgs(args);
        assertFalse(result);
    }

    @Test
    public void testValidateInputArgsSplitGroupValid() {
        String[] args = {"split-group", "10", "group1"};
        boolean result = inputValidator.validateInputArgs(args);
        assertTrue(result);
    }

    @Test
    public void testValidateInputArgsSplitGroupInvalid() {
        String[] args = {"split-group", "10", "group1", "user1"};
        boolean result = inputValidator.validateInputArgs(args);
        assertFalse(result);
    }

    @Test
    public void testValidateInputArgsPaidValid() {
        String[] args = {"paid", "10", "user2"};
        boolean result = inputValidator.validateInputArgs(args);
        assertTrue(result);
    }

    @Test
    public void testValidateInputArgsPaidInvalid() {
        String[] args = {"paid", "10"};
        boolean result = inputValidator.validateInputArgs(args);
        assertFalse(result);
    }
}
