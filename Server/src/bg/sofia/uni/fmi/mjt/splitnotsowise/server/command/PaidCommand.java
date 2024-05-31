package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.DebtManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.PaidStatus;

public class PaidCommand implements Command {
    private static final String SUCCESS_MESSAGE = "amount got paid successfully";
    private static final String FAIL_MESSAGE = "getting paid amount failed";
    private static final String NOT_LOGGED_IN_MESSAGE = "can't get paid amount when not logged in";
    private static final String PAYER_DOES_NOT_OWE_PAYEE_MESSAGE = "this user doesn't owe you money";
    private static final String USER_DOES_NOT_EXIST_MESSAGE = "user with this username doesn't exist";
    private static final String SAME_USERNAMES_MESSAGE = "can't get paid by yourself";
    private String message;
    private final boolean isLoggedIn;
    private final double amount;
    private final String payer;
    private final String payee;

    private final DebtManager debtManager;

    public PaidCommand(boolean isLoggedIn, String amount, String payer, String payee, DebtManager debtManager) {
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
        PaidStatus result;
        try {
            result = debtManager.payDebt(payer, payee, amount);
        } catch (DataStorageException e) {
            message = FAIL_MESSAGE;
            return;
        }
        message = switch (result) {
            case SUCCESS -> SUCCESS_MESSAGE;
            case PAYER_DOES_NOT_OWE_PAYEE -> PAYER_DOES_NOT_OWE_PAYEE_MESSAGE;
            case USER_DOES_NOT_EXIST -> USER_DOES_NOT_EXIST_MESSAGE;
            case SAME_USERNAMES -> SAME_USERNAMES_MESSAGE;
        };
    }

    @Override
    public String getMessage() {
        return message;
    }
}
