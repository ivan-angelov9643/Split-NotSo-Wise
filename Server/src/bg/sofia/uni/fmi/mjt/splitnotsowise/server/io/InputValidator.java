package bg.sofia.uni.fmi.mjt.splitnotsowise.server.io;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.constants.NumbersConstants;

import static bg.sofia.uni.fmi.mjt.splitnotsowise.server.io.IOHandler.*;

public class InputValidator {
    private static InputValidator instance;

    private InputValidator() {
    }

    public static InputValidator getInstance() {
        if (instance == null) {
            instance = new InputValidator();
        }
        return instance;
    }

    public static void setInstance(InputValidator inputValidator) {
        NotNullChecker.check(inputValidator);
        instance = inputValidator;
    }

    public static void resetInstance() {
        instance = new InputValidator();
    }

    private boolean validateRegister(String[] tokens) {
        NotNullChecker.check((Object) tokens);
        if (tokens.length != NumbersConstants.FIVE) {
            return false;
        }
        String firstName = tokens[NumbersConstants.ONE];
        String lastName = tokens[NumbersConstants.TWO];
        String username = tokens[NumbersConstants.THREE];
        return validateName(firstName) && validateName(lastName) &&
            validateName(username);
    }

    private boolean validateCreateGroup(String[] tokens) {
        NotNullChecker.check((Object) tokens);
        if (tokens.length < NumbersConstants.FOUR) {
            return false;
        }
        String groupName = tokens[1];
        return validateName(groupName);
    }

    private boolean validateSplit(String[] tokens) {
        NotNullChecker.check((Object) tokens);
        if (tokens.length != NumbersConstants.THREE) {
            return false;
        }
        String amount = tokens[1];
        return validateDouble(amount);
    }

    private boolean validatePaid(String[] tokens) {
        NotNullChecker.check((Object) tokens);
        if (tokens.length != NumbersConstants.THREE) {
            return false;
        }

        String amount = tokens[1];
        return validateDouble(amount);
    }

    private boolean validateName(String username) {
        NotNullChecker.check(username);
        return username.matches("[a-zA-Z0-9]+");
    }

    private boolean validateDouble(String str) {
        NotNullChecker.check(str);
        try {
            double d = Double.parseDouble(str);
            return d > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean validateInputArgs(String[] tokens) {
        NotNullChecker.check((Object) tokens);
        return switch (tokens[0]) {
            case HELP, LOGOUT, STATUS, QUIT, GROUPS, NOTIFICATIONS, PAYMENT_HISTORY -> tokens.length == NumbersConstants.ONE;
            case REGISTER -> validateRegister(tokens);
            case LOGIN -> tokens.length == NumbersConstants.THREE;
            case ADD_FRIEND -> tokens.length == NumbersConstants.TWO;
            case CREATE_GROUP -> validateCreateGroup(tokens);
            case SPLIT_FRIEND, SPLIT_GROUP -> validateSplit(tokens);
            case PAID -> validatePaid(tokens);
            default -> true;
        };
    }
}
