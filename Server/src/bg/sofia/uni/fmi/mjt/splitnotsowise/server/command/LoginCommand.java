package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.hasher.PasswordHasher;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.NotificationManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.UserManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.user.User;

import java.nio.channels.SelectionKey;
import java.util.List;

public class LoginCommand implements Command {
    private static final String SUCCESS_MESSAGE = "logged in successfully";
    private static final String USER_DOES_NOT_EXIST_MESSAGE = "user with this username doesn't exist";
    private static final String WRONG_PASSWORD_MESSAGE = "wrong password";
    private static final String NO_NOTIFICATIONS = "no notifications to show";
    private static final String LOGGED_IN_MESSAGE = "can't login into another account when you are logged in";
    private String message;
    private final SelectionKey key;
    private final String username;
    private final String passwordHash;

    private final UserManager userManager;
    private final NotificationManager notificationManager;
    public LoginCommand(SelectionKey key, String username, String password, UserManager userManager,
                        NotificationManager notificationManager) {
        NotNullChecker.check(key, username, password, userManager, notificationManager);
        this.key = key;
        this.username = username;
        this.passwordHash = PasswordHasher.hash(password);
        this.userManager = userManager;
        this.notificationManager = notificationManager;
    }

    @Override
    public void execute() {
        if (key.attachment() != null) {
            message = LOGGED_IN_MESSAGE;
            return;
        }
        User user = userManager.getUserByUsernameMap().get(username);
        if (user == null) {
            message = USER_DOES_NOT_EXIST_MESSAGE;
            return;
        }
        if (user.passwordHash().equals(passwordHash)) {
            key.attach(user);
            List<String> notifications;
            try {
                notifications = notificationManager.getNotifications(username);
            } catch (DataStorageException e) {
                message = e.getMessage();
                return;
            }
            buildMessage(notifications);
        } else {
            message = WRONG_PASSWORD_MESSAGE;
        }
    }

    private void buildMessage(List<String> notifications) {
        StringBuilder messageBuilder = new StringBuilder().append(SUCCESS_MESSAGE).append(System.lineSeparator());
        if (notifications.isEmpty()) {
            messageBuilder.append(NO_NOTIFICATIONS);
        } else {
            for (String notification : notifications) {
                messageBuilder.append("* ").append(notification).append(System.lineSeparator());
            }
            messageBuilder.delete(messageBuilder.lastIndexOf(System.lineSeparator()), messageBuilder.length());
        }
        message = messageBuilder.toString();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
