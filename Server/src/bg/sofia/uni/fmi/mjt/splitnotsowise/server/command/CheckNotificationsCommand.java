package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.NotificationManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;

import java.util.List;

public class CheckNotificationsCommand implements Command {
    private static final String NO_NOTIFICATIONS = "no notifications to show";
    private static final String NOT_LOGGED_IN_MESSAGE = "can't see notifications when not logged in";
    private String message;
    private final boolean isLoggedIn;
    private final String username;
    private final NotificationManager notificationManager;

    public CheckNotificationsCommand(boolean isLoggedIn, String username, NotificationManager notificationManager) {
        NotNullChecker.check(isLoggedIn, notificationManager);
        this.isLoggedIn = isLoggedIn;
        this.username = username;
        this.notificationManager = notificationManager;
    }

    @Override
    public void execute() {
        if (!isLoggedIn) {
            message = NOT_LOGGED_IN_MESSAGE;
            return;
        }
        List<String> notifications;
        try {
            notifications = notificationManager.getNotifications(username);
        } catch (DataStorageException e) {
            message = e.getMessage();
            return;
        }
        buildMessage(notifications);
    }

    private void buildMessage(List<String> notifications) {
        StringBuilder messageBuilder = new StringBuilder();
        if (notifications.isEmpty()) {
            messageBuilder.append(NO_NOTIFICATIONS);
        } else {
            messageBuilder.append("Notifications: ").append(System.lineSeparator());
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
