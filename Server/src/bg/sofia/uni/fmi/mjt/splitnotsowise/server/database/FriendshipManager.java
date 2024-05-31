package bg.sofia.uni.fmi.mjt.splitnotsowise.server.database;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.AddFriendshipStatus;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.user.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static bg.sofia.uni.fmi.mjt.splitnotsowise.server.Server.PROJECT_NAME;

public class FriendshipManager {
    private static final String FRIENDS_FILE_NAME = "friends.txt";
    private static final Path FRIENDS_FILE_PATH = Paths.get(PROJECT_NAME, FRIENDS_FILE_NAME);
    private static Path friendsFilePath;
    private static FriendshipManager instance;
    private final Map<String, Set<String>> friendsByUsername;
    private UserManager userManager;
    private NotificationManager notificationManager;
    private boolean dataLoaded;
    private FriendshipManager() {
        this.friendsByUsername = new HashMap<>();
        dataLoaded = false;
    }

    public void initialize() throws DataStorageException {
        if (!dataLoaded) {
            loadFriendships();
            dataLoaded = true;
        }
    }

    public static void resetInstance() {
        instance = new FriendshipManager();
    }

    public static void setInstance(FriendshipManager friendshipManager) {
        NotNullChecker.check(friendshipManager);
        instance = friendshipManager;
    }

    public static FriendshipManager getInstance() {
        if (instance == null) {
            instance = new FriendshipManager();
        }
        return instance;
    }

    private void initializeManagers() {
        userManager = UserManager.getInstance();
        notificationManager = NotificationManager.getInstance();
    }

    private void initializeFilePath() {
        if (friendsFilePath == null) {
            friendsFilePath = FRIENDS_FILE_PATH;
        }
    }

    public Map<String, Set<String>> getFriendsByUsernameMap() {
        return friendsByUsername;
    }

    public static void setFriendsFilePath(Path path) {
        NotNullChecker.check(path);
        friendsFilePath = path;
    }

    public static void resetFriendsFilePath() {
        friendsFilePath = FRIENDS_FILE_PATH;
    }

    public void loadFriendships() throws DataStorageException {
        initializeFilePath();
        try (BufferedReader br = new BufferedReader(new FileReader(friendsFilePath.toString()))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] data = line.split(",");
                String friend1 = data[0].trim();
                String friend2 = data[1].trim();
                updateFriendsMap(friend1, friend2);
            }
        } catch (IOException e) {
            throw new DataStorageException("an error occurred when loading the friends file", e);
        }
    }

    public boolean friendshipExist(String friend1, String friend2) {
        NotNullChecker.check(friend1, friend2);
        return friendsByUsername.containsKey(friend1) &&
            friendsByUsername.containsKey(friend2) &&
            friendsByUsername.get(friend1).contains(friend2) &&
            friendsByUsername.get(friend2).contains(friend1);
    }

    private synchronized void updateFriendsMap(String friend1, String friend2) {
        NotNullChecker.check(friend1, friend2);
        friendsByUsername.computeIfAbsent(friend1, k -> new HashSet<>()).add(friend2);
        friendsByUsername.computeIfAbsent(friend2, k -> new HashSet<>()).add(friend1);
    }

    public AddFriendshipStatus addFriendship(String userWhoAdded, String userWhoWasAdded) throws DataStorageException {
        NotNullChecker.check(userWhoAdded, userWhoWasAdded);
        if (userWhoAdded.equals(userWhoWasAdded)) {
            return AddFriendshipStatus.SAME_USERNAMES;
        }
        initializeManagers();
        initializeFilePath();
        Map<String, User> userByUsername = userManager.getUserByUsernameMap();
        if (!userByUsername.containsKey(userWhoWasAdded)) {
            return AddFriendshipStatus.USER_DOES_NOT_EXIST;
        }
        if (friendshipExist(userWhoAdded, userWhoWasAdded)) {
            return AddFriendshipStatus.FRIENDSHIP_ALREADY_EXISTS;
        }
        synchronized (this) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(friendsFilePath.toString(), true))) {
                String line = userWhoAdded + ',' + userWhoWasAdded;
                bw.write(line);
                bw.newLine();
                updateFriendsMap(userWhoAdded, userWhoWasAdded);
                notificationManager.addFriendAddedNotification(userWhoWasAdded, userWhoAdded);
                return AddFriendshipStatus.SUCCESS;
            } catch (IOException e) {
                throw new DataStorageException("an error occurred when writing to the friends file", e);
            }
        }
    }
}
