package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.FriendshipManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.AddFriendshipStatus;

public class AddFriendCommand implements Command {
    private static final String SUCCESS_MESSAGE = "friend added successfully";
    private static final String FAIL_MESSAGE = "adding friend failed";
    private static final String NOT_LOGGED_IN_MESSAGE = "can't add friend when not logged in";
    private static final String FRIENDSHIP_ALREADY_EXISTS_MESSAGE = "you are already friends";
    private static final String USER_DOES_NOT_EXIST_MESSAGE = "user with this username doesn't exist";
    private static final String SAME_USERNAMES_MESSAGE = "can't add yourself as a friend";
    private String message;
    private final boolean isLoggedIn;
    private final String friend1;
    private final String friend2;
    private final FriendshipManager friendshipManager;

    public AddFriendCommand(boolean isLoggedIn, String friend1, String friend2, FriendshipManager friendshipManager) {
        NotNullChecker.check(isLoggedIn, friend2, friendshipManager);
        this.isLoggedIn = isLoggedIn;
        this.friend1 = friend1;
        this.friend2 = friend2;
        this.friendshipManager = friendshipManager;
    }

    @Override
    public void execute() {
        if (!isLoggedIn) {
            message = NOT_LOGGED_IN_MESSAGE;
            return;
        }
        AddFriendshipStatus result;
        try {
            result = friendshipManager.addFriendship(friend1, friend2);
        } catch (DataStorageException e) {
            message = FAIL_MESSAGE;
            return;
        }
        message = switch (result) {
            case SUCCESS -> SUCCESS_MESSAGE;
            case FRIENDSHIP_ALREADY_EXISTS -> FRIENDSHIP_ALREADY_EXISTS_MESSAGE;
            case USER_DOES_NOT_EXIST -> USER_DOES_NOT_EXIST_MESSAGE;
            case SAME_USERNAMES -> SAME_USERNAMES_MESSAGE;
        };
    }

    @Override
    public String getMessage() {
        return message;
    }
}
