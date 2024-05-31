package bg.sofia.uni.fmi.mjt.splitnotsowise.server.io;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.AddFriendCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.CheckGroupsCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.CheckNotificationsCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.CheckPaymentHistoryCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.Command;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.CreateGroupCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.LoginCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.LogoutCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.PaidCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.RegisterCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.SplitFriendCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.SplitGroupCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.command.StatusCommand;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.DebtManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.FriendshipManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.GroupManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.NotificationManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.UserManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.constants.NumbersConstants;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.user.User;

import java.nio.channels.SelectionKey;

public class IOHandler {
    private static final String QUIT_MESSAGE = "bye, see you soon :)";
    private static final String UNKNOWN_COMMAND_MESSAGE = "unknown command, use help";
    private static final String INVALID_ARGUMENTS_MESSAGE = "invalid arguments, use help";
    private static final String HELP_MESSAGE = """
        when not logged in:
            help
            register <first name> <last name> <username> <password>
            login <username> <password>
            quit
        when logged in:
            help
            logout
            add-friend <username>
            create-group <group name> <username> ... <username>
            split-friend <amount> <username>
            split-group <amount> <group name>
            paid <amount> <username>
            payment-history
            status
            groups
            notifications
            quit""";
    public static final String HELP = "help";
    public static final String REGISTER = "register";
    public static final String LOGIN = "login";
    public static final String LOGOUT = "logout";
    public static final String ADD_FRIEND = "add-friend";
    public static final String CREATE_GROUP = "create-group";
    public static final String SPLIT_FRIEND = "split-friend";
    public static final String SPLIT_GROUP = "split-group";
    public static final String PAID = "paid";
    public static final String STATUS = "status";
    public static final String GROUPS = "groups";
    public static final String NOTIFICATIONS = "notifications";
    public static final String PAYMENT_HISTORY = "payment-history";
    public static final String QUIT = "quit";
    private static IOHandler instance;
    private UserManager userManager;
    private FriendshipManager friendshipManager;
    private GroupManager groupManager;
    private DebtManager debtManager;
    private NotificationManager notificationManager;
    private InputValidator inputValidator;

    private IOHandler() {
    }

    public static void resetInstance() {
        instance = new IOHandler();
    }

    public static void setInstance(IOHandler ioHandler) {
        NotNullChecker.check(ioHandler);
        instance = ioHandler;
    }

    public static IOHandler getInstance() {
        if (instance == null) {
            instance = new IOHandler();
        }
        return instance;
    }

    private void initializeManagers() {
        userManager = UserManager.getInstance();
        friendshipManager = FriendshipManager.getInstance();
        groupManager = GroupManager.getInstance();
        debtManager = DebtManager.getInstance();
        notificationManager = NotificationManager.getInstance();
        inputValidator = InputValidator.getInstance();
    }

    public String handle(String input, SelectionKey key) {
        NotNullChecker.check(input, key);
        initializeManagers();
        String[] tokens = input.trim().split(" ");
        if (tokens.length == NumbersConstants.ZERO) {
            return null;
        }
        if (!inputValidator.validateInputArgs(tokens)) {
            return INVALID_ARGUMENTS_MESSAGE;
        }
        User user = (User) key.attachment();
        boolean isLoggedIn = user != null;
        String username = isLoggedIn ? user.username() : null;
        return processCommand(tokens, isLoggedIn, username, key);
    }

    private String processCommand(String[] tokens, boolean isLoggedIn, String username, SelectionKey key) {
        NotNullChecker.check(tokens, isLoggedIn, key);
        Command command;
        switch (tokens[NumbersConstants.ZERO]) {
            case HELP -> {
                return HELP_MESSAGE;
            }
            case QUIT -> {
                return QUIT_MESSAGE;
            }
            default -> command = generateCommandObject(tokens, isLoggedIn, username, key);
        }
        if (command == null) {
            return UNKNOWN_COMMAND_MESSAGE;
        }
        command.execute();
        return command.getMessage();
    }

    private Command generateCommandObject(String[] tokens, boolean isLoggedIn, String username, SelectionKey key) {
        NotNullChecker.check(tokens, isLoggedIn, key);
        return switch (tokens[NumbersConstants.ZERO]) {
            case REGISTER ->
                new RegisterCommand(isLoggedIn, tokens[NumbersConstants.ONE], tokens[NumbersConstants.TWO], tokens[NumbersConstants.THREE], tokens[NumbersConstants.FOUR], userManager);
            case LOGIN -> new LoginCommand(key, tokens[NumbersConstants.ONE], tokens[NumbersConstants.TWO], userManager, notificationManager);
            case LOGOUT -> new LogoutCommand(key);
            case ADD_FRIEND -> new AddFriendCommand(isLoggedIn, username, tokens[NumbersConstants.ONE], friendshipManager);
            case CREATE_GROUP -> {
                String[] members = new String[tokens.length - NumbersConstants.TWO];
                System.arraycopy(tokens, 2, members, NumbersConstants.ZERO, members.length);
                yield new CreateGroupCommand(isLoggedIn, tokens[NumbersConstants.ONE], username, groupManager, members);
            }
            case SPLIT_FRIEND -> new SplitFriendCommand(isLoggedIn, tokens[NumbersConstants.ONE], tokens[NumbersConstants.TWO], username, debtManager);
            case SPLIT_GROUP -> new SplitGroupCommand(isLoggedIn, tokens[NumbersConstants.ONE], tokens[NumbersConstants.TWO], username, debtManager);
            case PAID -> new PaidCommand(isLoggedIn, tokens[NumbersConstants.ONE], tokens[NumbersConstants.TWO], username, debtManager);
            case STATUS -> new StatusCommand(isLoggedIn, username, userManager, debtManager);
            case GROUPS -> new CheckGroupsCommand(isLoggedIn, username, groupManager);
            case NOTIFICATIONS -> new CheckNotificationsCommand(isLoggedIn, username, notificationManager);
            case PAYMENT_HISTORY -> new CheckPaymentHistoryCommand(isLoggedIn, username, userManager, debtManager);
            default -> null;
        };
    }
}
