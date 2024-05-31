package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;

import java.nio.channels.SelectionKey;

public class LogoutCommand implements Command {

    private static final String SUCCESS_MESSAGE = "logged out successfully";
    private static final String NOT_LOGGED_IN_MESSAGE = "can't logout when not logged in";
    private String message;
    private final SelectionKey key;

    public LogoutCommand(SelectionKey key) {
        NotNullChecker.check(key);
        this.key = key;
    }

    @Override
    public void execute() {
        if (key.attachment() != null) {
            key.attach(null);
            message = SUCCESS_MESSAGE;
        } else {
            message = NOT_LOGGED_IN_MESSAGE;
        }
    }

    @Override
    public String getMessage() {
        return message;
    }
}
