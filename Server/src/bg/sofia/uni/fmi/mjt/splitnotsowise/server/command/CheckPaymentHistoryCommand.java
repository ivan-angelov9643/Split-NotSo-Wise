package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.DebtManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.UserManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.user.User;

import java.util.List;
import java.util.Map;

public class CheckPaymentHistoryCommand implements Command {
    private static final String NO_HISTORY = "no history to show";
    private static final String NOT_LOGGED_IN_MESSAGE = "can't see payments history when not logged in";
    private String message;
    private final boolean isLoggedIn;
    private final String username;
    private final UserManager userManager;
    private final DebtManager debtManager;

    public CheckPaymentHistoryCommand(boolean isLoggedIn, String username, UserManager userManager,
                                      DebtManager debtManager) {
        NotNullChecker.check(isLoggedIn, username, debtManager);
        this.isLoggedIn = isLoggedIn;
        this.username = username;
        this.userManager = userManager;
        this.debtManager = debtManager;
    }

    @Override
    public void execute() {
        if (!isLoggedIn) {
            message = NOT_LOGGED_IN_MESSAGE;
            return;
        }
        Map<String, List<Double>> payments = debtManager.getPaymentsByUsernameMap().get(username);
        Map<String, User> usersByUsername = userManager.getUserByUsernameMap();
        buildMessage(payments, usersByUsername);
    }

    private void buildMessage(Map<String, List<Double>> payments, Map<String, User> usersByUsername) {
        StringBuilder messageBuilder = new StringBuilder();
        if (payments == null) {
            messageBuilder.append(NO_HISTORY);
        } else {
            for (String payee : payments.keySet()) {
                for (double amountPaid : payments.get(payee)) {
                    messageBuilder.append("* ").append("you paid ").append(usersByUsername.get(payee).firstName())
                        .append(" ")
                        .append(usersByUsername.get(payee).lastName()).append(" (").append(payee).append(") ")
                        .append(amountPaid).append(" lv").append(System.lineSeparator());
                }
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
