package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.DebtManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.SplitFriendStatus;

public class SplitFriendCommand implements Command {
    private static final String SUCCESS_MESSAGE = "amount split successfully";
    private static final String FAIL_MESSAGE = "splitting amount failed";
    private static final String NOT_LOGGED_IN_MESSAGE = "can't split amount when not logged in";
    private static final String FRIENDSHIP_DOES_NOT_EXIST_MESSAGE = "you are not friends";
    private static final String USER_DOES_NOT_EXIST_MESSAGE = "user with this username doesn't exist";
    private String message;
    private final boolean isLoggedIn;
    private final double amount;
    private final String payer;
    private final String payee;
    private final DebtManager debtManager;

    public SplitFriendCommand(boolean isLoggedIn, String amount, String payer, String payee, DebtManager debtManager) {
        NotNullChecker.check(isLoggedIn, amount, payer, debtManager);
        this.isLoggedIn = isLoggedIn;
        this.amount = Double.parseDouble(amount);
        this.payer = payer;
        this.payee = payee;
        this.debtManager = debtManager;
    }

    @Override
    public void execute() {
        if (!isLoggedIn) {
            message = NOT_LOGGED_IN_MESSAGE;
            return;
        }
        SplitFriendStatus result;
        try {
            result = debtManager.splitFriend(payer, payee, amount);
        } catch (DataStorageException e) {
            message = FAIL_MESSAGE;
            return;
        }
        message = switch (result) {
            case SUCCESS -> SUCCESS_MESSAGE;
            case FRIENDSHIP_DOES_NOT_EXIST -> FRIENDSHIP_DOES_NOT_EXIST_MESSAGE;
            case USER_DOES_NOT_EXIST -> USER_DOES_NOT_EXIST_MESSAGE;
        };
    }

    @Override
    public String getMessage() {
        return message;
    }
}
