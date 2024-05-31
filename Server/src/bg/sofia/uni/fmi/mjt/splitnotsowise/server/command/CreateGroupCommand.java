package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.GroupManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.CreateGroupStatus;

public class CreateGroupCommand implements Command {
    private static final String SUCCESS_MESSAGE = "group created successfully";
    private static final String FAIL_MESSAGE = "creating group failed";
    private static final String NOT_LOGGED_IN_MESSAGE = "can't create group when not logged in";
    private static final String GROUP_NAME_ALREADY_EXISTS_MESSAGE = "group name is taken";
    private static final String MEMBERS_CONTAIN_CREATORS_USERNAME_MESSAGE = "don't put your name in members";
    private static final String MEMBER_DOES_NOT_EXIST_MESSAGE = "user with this username doesn't exist";
    private String message;
    private final boolean isLoggedIn;
    private final String groupName;
    private final String groupCreator;
    private final String[] members;
    private final GroupManager groupManager;
    public CreateGroupCommand(boolean isLoggedIn, String groupName, String groupCreator, GroupManager groupManager,
                              String... members) {
        NotNullChecker.check(isLoggedIn, groupName, groupManager, members);
        this.isLoggedIn = isLoggedIn;
        this.groupName = groupName;
        this.groupCreator = groupCreator;
        this.members = members;
        this.groupManager = groupManager;
    }

    @Override
    public void execute() {
        if (!isLoggedIn) {
            message = NOT_LOGGED_IN_MESSAGE;
            return;
        }
        CreateGroupStatus result;
        try {
            result = groupManager.createGroup(groupName, groupCreator, members);
        } catch (DataStorageException e) {
            message = FAIL_MESSAGE;
            return;
        }
        message = switch (result) {
            case SUCCESS -> SUCCESS_MESSAGE;
            case GROUP_NAME_ALREADY_EXISTS -> GROUP_NAME_ALREADY_EXISTS_MESSAGE;
            case MEMBERS_CONTAIN_CREATORS_USERNAME -> MEMBERS_CONTAIN_CREATORS_USERNAME_MESSAGE;
            case MEMBER_DOES_NOT_EXIST -> MEMBER_DOES_NOT_EXIST_MESSAGE;
        };
    }

    @Override
    public String getMessage() {
        return message;
    }
}
