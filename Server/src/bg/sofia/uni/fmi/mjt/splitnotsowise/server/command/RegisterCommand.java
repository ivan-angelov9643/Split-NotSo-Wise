package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.hasher.PasswordHasher;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.UserManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.RegistrationStatus;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.user.User;

public class RegisterCommand implements Command {
    private static final String SUCCESS_MESSAGE = "registered successfully";
    private static final String FAIL_MESSAGE = "registration failed";
    private static final String USERNAME_ALREADY_EXISTS_MESSAGE = "username is taken";
    private static final String LOGGED_IN_MESSAGE = "can't register new account when you are logged in";
    private String message;
    private final boolean isLoggedIn;
    private final User user;
    private final UserManager userManager;

    public RegisterCommand(boolean isLoggedIn, String firstName, String lastName, String username, String password,
                           UserManager userManager) {
        NotNullChecker.check(isLoggedIn, firstName, lastName, username, password, userManager);
        this.isLoggedIn = isLoggedIn;
        user = new User(firstName, lastName, username, PasswordHasher.hash(password));
        this.userManager = userManager;
    }

    @Override
    public void execute() {
        if (isLoggedIn) {
            message = LOGGED_IN_MESSAGE;
            return;
        }
        RegistrationStatus result;
        try {
            result = userManager.registerUser(user);
        } catch (DataStorageException e) {
            message = FAIL_MESSAGE;
            return;
        }
        message = switch (result) {
            case SUCCESS -> SUCCESS_MESSAGE;
            case USERNAME_ALREADY_EXISTS -> USERNAME_ALREADY_EXISTS_MESSAGE;
        };
    }

    @Override
    public String getMessage() {
        return message;
    }
}
