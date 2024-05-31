package bg.sofia.uni.fmi.mjt.splitnotsowise.server.database;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.constants.NumbersConstants;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.RegistrationStatus;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.user.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static bg.sofia.uni.fmi.mjt.splitnotsowise.server.Server.PROJECT_NAME;

public class UserManager {
    private static final String USERS_FILE_NAME = "users.txt";
    public static final Path USERS_FILE_PATH = Paths.get(PROJECT_NAME, USERS_FILE_NAME);
    private static Path usersFilePath;
    private static UserManager instance;
    private final Map<String, User> userByUsername;
    private boolean dataLoaded;
    private UserManager() {
        this.userByUsername = new HashMap<>();
        dataLoaded = false;
    }

    public void initialize() throws DataStorageException {
        if (!dataLoaded) {
            loadUsers();
            dataLoaded = true;
        }
    }

    public void initializeFilePath() {
        if (usersFilePath == null) {
            usersFilePath = USERS_FILE_PATH;
        }
    }

    public static void resetInstance() {
        instance = new UserManager();
    }

    public static void setInstance(UserManager userManager) {
        NotNullChecker.check(userManager);
        instance = userManager;
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public Map<String, User> getUserByUsernameMap() {
        return userByUsername;
    }

    public static void setUsersFilePath(Path path) {
        NotNullChecker.check(path);
        usersFilePath = path;
    }

    public static void resetUsersFilePath() {
        usersFilePath = USERS_FILE_PATH;
    }

    public void loadUsers() throws DataStorageException {
        initializeFilePath();
        try (BufferedReader br = new BufferedReader(new FileReader(usersFilePath.toString()))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] data = line.split(",");
                String firstName = data[0].trim();
                String lastName = data[1].trim();
                String username = data[2].trim();
                String passwordHash = data[NumbersConstants.THREE].trim();
                addToUsersMap(username, new User(firstName, lastName, username, passwordHash));
            }
        } catch (IOException e) {
            throw new DataStorageException("an error occurred when loading the users file", e);
        }
    }

    private synchronized void addToUsersMap(String username, User user) {
        NotNullChecker.check(username, user);
        userByUsername.put(username, user);
    }

    public RegistrationStatus registerUser(User newUser) throws DataStorageException {
        NotNullChecker.check(newUser);
        if (userByUsername.containsKey(newUser.username())) {
            return RegistrationStatus.USERNAME_ALREADY_EXISTS;
        }
        initializeFilePath();
        synchronized (this) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(usersFilePath.toString(), true))) {
                String line = newUser.firstName() + ',' +
                    newUser.lastName() + ',' +
                    newUser.username() + ',' +
                    newUser.passwordHash();
                bw.write(line);
                bw.newLine();
                addToUsersMap(newUser.username(), newUser);
                return RegistrationStatus.SUCCESS;
            } catch (IOException e) {
                throw new DataStorageException("an error occurred when updating the users file", e);
            }
        }
    }
}
