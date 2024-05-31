package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.GroupManager;

import java.util.Map;
import java.util.Set;

public class CheckGroupsCommand implements Command {
    private static final String NO_GROUPS = "you are not member of any groups";
    private static final String NOT_LOGGED_IN_MESSAGE = "can't see groups when not logged in";
    private String message;
    private final boolean isLoggedIn;
    private final String username;
    private final GroupManager groupManager;

    public CheckGroupsCommand(boolean isLoggedIn, String username, GroupManager groupManager) {
        NotNullChecker.check(isLoggedIn, groupManager);
        this.isLoggedIn = isLoggedIn;
        this.username = username;
        this.groupManager = groupManager;
    }

    @Override
    public void execute() {
        if (!isLoggedIn) {
            message = NOT_LOGGED_IN_MESSAGE;
            return;
        }
        Map<String, Set<String>> groupMembersByGroupName = groupManager.getGroupMembersByGroupNameMap();
        Set<String> groupNames = groupManager.getGroupNamesByUsernameMap().get(username);
        if (groupNames == null || groupNames.isEmpty()) {
            message = NO_GROUPS;
            return;
        }
        buildMessage(groupMembersByGroupName, groupNames);
    }

    private void buildMessage(Map<String, Set<String>> groupMembersByGroupName, Set<String> groupNames) {
        StringBuilder messageBuilder = new StringBuilder()
            .append("Groups:").append(System.lineSeparator());
        for (String groupName : groupNames) {
            messageBuilder.append("* ").append(groupName).append(": ");
            for (String member : groupMembersByGroupName.get(groupName)) {
                messageBuilder.append(member).append(',');
            }
            messageBuilder.deleteCharAt(messageBuilder.lastIndexOf(","));
            messageBuilder.append(System.lineSeparator());
        }
        messageBuilder.delete(messageBuilder.lastIndexOf(System.lineSeparator()), messageBuilder.length());
        message = messageBuilder.toString();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
