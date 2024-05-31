package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.LogoutCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.channels.SelectionKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogoutCommandTest {
    private SelectionKey key;
    private LogoutCommand logoutCommand;

    @BeforeEach
    public void setUp() {
        key = mock();
        logoutCommand = new LogoutCommand(key);
    }

    @Test
    public void testExecuteSuccess() {
        when(key.attachment()).thenReturn(new Object());

        logoutCommand.execute();

        assertEquals("logged out successfully", logoutCommand.getMessage());
        verify(key, times(1)).attachment();
        verify(key, times(1)).attach(null);
    }

    @Test
    public void testExecuteNotLoggedIn() {
        when(key.attachment()).thenReturn(null);

        logoutCommand.execute();

        assertEquals("can't logout when not logged in", logoutCommand.getMessage());
        verify(key, times(1)).attachment();
    }
}
