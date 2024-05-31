package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.DebtManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.SplitGroupStatus;

public class SplitGroupCommand implements Command {
    private static final String SUCCESS_MESSAGE = "amount split successfully";
    private static final String FAIL_MESSAGE = "splitting amount failed";
    private static final String NOT_LOGGED_IN_MESSAGE = "can't split amount when not logged in";
    private static final String USER_NOT_IN_GROUP_MESSAGE = "you are not in this group";
    private static final String GROUP_DOES_NOT_EXIST_MESSAGE = "group with this name doesn't exist";
    private String message;
    private final boolean isLoggedIn;
    private final double amount;
    private final String payee;
    private final String groupName;
    private final DebtManager debtManager;

    public SplitGroupCommand(boolean isLoggedIn, String amount, String groupName, String payee,
                             DebtManager debtManager) {
        NotNullChecker.check(isLoggedIn, amount, groupName, debtManager);
        this.isLoggedIn = isLoggedIn;
        this.amount = Double.parseDouble(amount);
        this.payee = payee;
        this.groupName = groupName;
        this.debtManager = debtManager;
    }

    @Override
    public void execute() {
        if (!isLoggedIn) {
            message = NOT_LOGGED_IN_MESSAGE;
            return;
        }
        SplitGroupStatus result;
        try {
            result = debtManager.splitGroup(groupName, payee, amount);
        } catch (DataStorageException e) {
            message = FAIL_MESSAGE;
            return;
        }
        message = switch (result) {
            case SUCCESS -> SUCCESS_MESSAGE;
            case USER_NOT_IN_GROUP -> USER_NOT_IN_GROUP_MESSAGE;
            case GROUP_DOES_NOT_EXIST -> GROUP_DOES_NOT_EXIST_MESSAGE;
        };
    }

    @Override
    public String getMessage() {
        return message;
    }
}
