package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.RegisterCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.UserManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.RegistrationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class RegisterCommandTest {

    private UserManager userManager;
    private RegisterCommand registerCommand;

    @BeforeEach
    public void setUp() {
        userManager = mock();
        registerCommand = new RegisterCommand(false, "First1", "Last1", "user1",
            "password", userManager);
    }

    @Test
    public void testExecuteSuccess() throws DataStorageException {
        when(userManager.registerUser(any())).thenReturn(RegistrationStatus.SUCCESS);

        registerCommand.execute();

        assertEquals("registered successfully", registerCommand.getMessage());
        verify(userManager, times(1)).registerUser(any());
    }

    @Test
    public void testExecuteUsernameAlreadyExists() throws DataStorageException {
        when(userManager.registerUser(any())).thenReturn(RegistrationStatus.USERNAME_ALREADY_EXISTS);

        registerCommand.execute();

        assertEquals("username is taken", registerCommand.getMessage());
        verify(userManager, times(1)).registerUser(any());
    }

    @Test
    public void testExecuteDataStorageException() throws DataStorageException {
        when(userManager.registerUser(any())).thenThrow(new DataStorageException("error"));

        registerCommand.execute();

        assertEquals("registration failed", registerCommand.getMessage());
        verify(userManager, times(1)).registerUser(any());
    }

    @Test
    public void testExecuteLoggedIn() throws DataStorageException {
        RegisterCommand loggedInCommand = new RegisterCommand(true, "First1", "Last1",
            "user1", "password", userManager);

        loggedInCommand.execute();

        assertEquals("can't register new account when you are logged in", loggedInCommand.getMessage());
        verify(userManager, never()).registerUser(any());
    }

}
