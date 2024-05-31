package bg.sofia.uni.fmi.mjt.splitnotsowise.server.database;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.CreateGroupStatus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static bg.sofia.uni.fmi.mjt.splitnotsowise.server.Server.PROJECT_NAME;

public class GroupManager {
    private static final String GROUPS_FILE_NAME = "groups.txt";
    private static final Path GROUPS_FILE_PATH = Paths.get(PROJECT_NAME, GROUPS_FILE_NAME);
    private static Path groupsFilePath;
    private static GroupManager instance;
    private final Map<String, Set<String>> groupMembersByGroupName;
    private final Map<String, Set<String>> groupNamesByUsername;
    private UserManager userManager;
    private NotificationManager notificationManager;
    private boolean dataLoaded;
    private GroupManager() {
        this.groupMembersByGroupName = new HashMap<>();
        this.groupNamesByUsername = new HashMap<>();
        dataLoaded = false;
    }

    public void initialize() throws DataStorageException {
        if (!dataLoaded) {
            loadGroups();
            dataLoaded = true;
        }
    }

    public static void resetInstance() {
        instance = new GroupManager();
    }

    public static void setInstance(GroupManager groupManager) {
        NotNullChecker.check(groupManager);
        instance = groupManager;
    }

    public static GroupManager getInstance() {
        if (instance == null) {
            instance = new GroupManager();
        }
        return instance;
    }

    private void initializeManagers() {
        userManager = UserManager.getInstance();
        notificationManager = NotificationManager.getInstance();
    }

    public void initializeFilePath() {
        if (groupsFilePath == null) {
            groupsFilePath = GROUPS_FILE_PATH;
        }
    }

    public Map<String, Set<String>> getGroupMembersByGroupNameMap() {
        return groupMembersByGroupName;
    }

    public Map<String, Set<String>> getGroupNamesByUsernameMap() {
        return groupNamesByUsername;
    }

    public static void setGroupsFilePath(Path path) {
        NotNullChecker.check(path);
        groupsFilePath = path;
    }

    public static void resetGroupsFilePath() {
        groupsFilePath = GROUPS_FILE_PATH;
    }

    private synchronized void updateGroupsMap(String groupName, String... members) {
        NotNullChecker.check(groupName, members);
        Set<String> memberSet = new HashSet<>(Arrays.asList(members));
        groupMembersByGroupName.put(groupName, memberSet);

        for (String member : members) {
            groupNamesByUsername.computeIfAbsent(member, k -> new HashSet<>()).add(groupName);
        }
    }

    public void loadGroups() throws DataStorageException {
        initializeFilePath();
        try (BufferedReader br = new BufferedReader(new FileReader(groupsFilePath.toString()))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] data = line.split(",");
                String groupName = data[0].trim();
                String[] members = data[1].split(";");
                updateGroupsMap(groupName, members);
            }
        } catch (IOException e) {
            throw new DataStorageException("an error occurred when loading the groups file", e);
        }
    }

    public CreateGroupStatus createGroup(String groupName, String groupCreator, String... members)
        throws DataStorageException {
        NotNullChecker.check(groupName, groupCreator, members);
        if (groupMembersByGroupName.containsKey(groupName)) {
            return CreateGroupStatus.GROUP_NAME_ALREADY_EXISTS;
        }
        synchronized (this) {
            initializeManagers();
            initializeFilePath();
            for (String member : members) {
                if (!userManager.getUserByUsernameMap().containsKey(member)) {
                    return CreateGroupStatus.MEMBER_DOES_NOT_EXIST;
                }
                if (member.equals(groupCreator)) {
                    return CreateGroupStatus.MEMBERS_CONTAIN_CREATORS_USERNAME;
                }
            }
            addGroupToFile(groupName, groupCreator, members);
            updateGroupsMap(groupName, groupCreator, members);
            for (String member : members) {
                notificationManager.addAddedToGroupNotification(member, groupCreator, groupName);
            }
            return CreateGroupStatus.SUCCESS;
        }
    }

    private synchronized void addGroupToFile(String groupName, String groupCreator, String[] members)
        throws DataStorageException {
        NotNullChecker.check(groupName, groupCreator, members);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(groupsFilePath.toString(), true))) {
            StringBuilder line = new StringBuilder()
                .append(groupName).append(',').append(groupCreator).append(';');
            for (String member : members) {
                line.append(member).append(';');
            }
            line.deleteCharAt(line.lastIndexOf(";"));
            bw.write(line.toString());
            bw.newLine();
        } catch (IOException e) {
            throw new DataStorageException("an error occurred when updating the groups file", e);
        }
    }

    private synchronized void updateGroupsMap(String groupName, String groupCreator, String[] members) {
        NotNullChecker.check(groupName, groupCreator, members);
        String[] allMembers = new String[members.length + 1];
        allMembers[0] = groupCreator;
        System.arraycopy(members, 0, allMembers, 1, members.length);
        updateGroupsMap(groupName, allMembers);
    }
}
