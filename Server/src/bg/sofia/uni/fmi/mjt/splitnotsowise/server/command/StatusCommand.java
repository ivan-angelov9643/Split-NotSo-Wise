package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.DebtManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.UserManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.user.User;

import java.util.Map;

public class StatusCommand implements Command {
    private static final String NO_MONEY_RELATIONS = "you don't have any money relations";
    private static final String NOT_LOGGED_IN_MESSAGE = "can't see status when not logged in";
    private String message;
    private final boolean isLoggedIn;
    private final String username;
    private final UserManager userManager;
    private final DebtManager debtManager;

    public StatusCommand(boolean isLoggedIn, String username, UserManager userManager, DebtManager debtManager) {
        NotNullChecker.check(isLoggedIn, userManager, debtManager);
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
        Map<String, Double> payers = debtManager.getPayeesMap().get(username);
        Map<String, Double> payees = debtManager.getPayersMap().get(username);
        Map<String, User> usersByUsername = userManager.getUserByUsernameMap();
        if (payers == null && payees == null ||
            (payers != null && payers.isEmpty()) &&
                (payees != null && payees.isEmpty())) {
            message = NO_MONEY_RELATIONS;
            return;
        }
        buildMessage(payers, payees, usersByUsername);
    }

    private void buildMessage(Map<String, Double> payers, Map<String, Double> payees,
                              Map<String, User> usersByUsername) {
        StringBuilder messageBuilder = new StringBuilder();
        if (payers != null) {
            for (String payer : payers.keySet()) {
                messageBuilder.append("* ").append(usersByUsername.get(payer).firstName()).append(" ")
                    .append(usersByUsername.get(payer).lastName()).append(" (").append(payer).append("): owes you ")
                    .append(payers.get(payer)).append(" lv").append(System.lineSeparator());
            }
        }
        if (payees != null) {
            for (String payee : payees.keySet()) {
                messageBuilder.append("* ").append(usersByUsername.get(payee).firstName()).append(" ")
                    .append(usersByUsername.get(payee).lastName()).append(" (").append(payee).append("): you owe ")
                    .append(payees.get(payee)).append(" lv").append(System.lineSeparator());
            }
        }
        messageBuilder.delete(messageBuilder.lastIndexOf(System.lineSeparator()), messageBuilder.length());
        message = messageBuilder.toString();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
