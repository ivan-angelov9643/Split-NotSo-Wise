package bg.sofia.uni.fmi.mjt.splitnotsowise.server.database;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static bg.sofia.uni.fmi.mjt.splitnotsowise.server.Server.PROJECT_NAME;

public class NotificationManager {
    public static final String NOTIFICATIONS_FILE_NAME_POSTFIX = "_notifications.txt";
    public static String projectName;
    public static String notificationsFileNamePostfix;
    private static NotificationManager instance;
    private final Map<String, List<String>> notificationsByUsername;
    private UserManager userManager;
    private boolean dataLoaded;
    private NotificationManager() {
        notificationsByUsername = new HashMap<>();
        dataLoaded = false;
    }

    public void initialize() throws DataStorageException {
        if (!dataLoaded) {
            loadNotifications();
            dataLoaded = true;
        }
    }

    public static void resetInstance() {
        instance = new NotificationManager();
    }

    public static void setInstance(NotificationManager notificationManager) {
        NotNullChecker.check(notificationManager);
        instance = notificationManager;
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    private void initializeManagers() {
        userManager = UserManager.getInstance();
    }

    private void initializeFileInfo() {
        if (projectName == null) {
            projectName = PROJECT_NAME;
        }
        if (notificationsFileNamePostfix == null) {
            notificationsFileNamePostfix = NOTIFICATIONS_FILE_NAME_POSTFIX;
        }
    }

    public Map<String, List<String>> getNotificationsByUsernameMap() {
        return notificationsByUsername;
    }

    public static void setProjectName(String name) {
        NotNullChecker.check(name);
        projectName = name;
    }

    public static void setNotificationsFileNamePostfix(String fileNamePostfix) {
        NotNullChecker.check(fileNamePostfix);
        notificationsFileNamePostfix = fileNamePostfix;
    }

    public static void resetProjectName() {
        projectName = PROJECT_NAME;
    }

    public static void resetNotificationsFileNamePostfix() {
        notificationsFileNamePostfix = NOTIFICATIONS_FILE_NAME_POSTFIX;
    }

    private synchronized void addToNotificationsMap(String user, String notification) {
        NotNullChecker.check(user, notification);
        notificationsByUsername.computeIfAbsent(user, k -> new ArrayList<>());
        notificationsByUsername.get(user).add(notification);
    }

    public void loadNotifications() throws DataStorageException {
        initializeManagers();
        initializeFileInfo();
        Set<String> usernames = userManager.getUserByUsernameMap().keySet();

        for (String user : usernames) {
            Path notificationsFilePath = Paths.get(projectName, user + notificationsFileNamePostfix);

            if (Files.exists(notificationsFilePath)) {
                try (BufferedReader br = new BufferedReader(new FileReader(notificationsFilePath.toString()))) {
                    String data;

                    while ((data = br.readLine()) != null) {
                        if (data.isBlank()) {
                            continue;
                        }
                        addToNotificationsMap(user, data);
                    }
                } catch (IOException e) {
                    throw new DataStorageException("an error occurred when loading the notifications file", e);
                }
            }
        }
    }

    private synchronized void addToNotificationFile(String user, String notification) throws DataStorageException {
        NotNullChecker.check(user, notification);
        initializeFileInfo();
        Path payerDebtsFilePath = Paths.get(projectName, user + notificationsFileNamePostfix);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(payerDebtsFilePath.toString(), true))) {
            bw.write(notification);
            bw.newLine();
            addToNotificationsMap(user, notification);
        } catch (IOException e) {
            throw new DataStorageException("an error occurred when updating notifications file", e);
        }
    }

    public void addPaymentApprovedNotification(String payer, String payee, double amount) throws DataStorageException {
        NotNullChecker.check(payer, payee, amount);
        addToNotificationFile(payer, payee + " approved your payment of " + amount + " lv");
    }

    public void addAmountSplitNotification(String payer, String payee, double amount) throws DataStorageException {
        NotNullChecker.check(payer, payee, amount);
        addToNotificationFile(payer, payee + " added " + amount + " lv to your debt to him");
    }

    public void addFriendAddedNotification(String userWhoWasAdded, String userWhoAdded) throws DataStorageException {
        NotNullChecker.check(userWhoWasAdded, userWhoAdded);
        addToNotificationFile(userWhoWasAdded, userWhoAdded + " added you as a friend");
    }

    public void addAddedToGroupNotification(String userWhoWasAdded, String userWhoAdded, String groupName)
        throws DataStorageException {
        NotNullChecker.check(userWhoWasAdded, userWhoAdded, groupName);
        addToNotificationFile(userWhoWasAdded, userWhoAdded + " added you to \"" + groupName + "\" group");
    }

    public List<String> getNotifications(String user) throws DataStorageException {
        NotNullChecker.check(user);
        initializeFileInfo();
        Path notificationsFilePath = Paths.get(projectName, user + notificationsFileNamePostfix);
        List<String> notifications = new ArrayList<>();

        synchronized (this) {
            if (Files.exists(notificationsFilePath)) {
                try (BufferedReader br = new BufferedReader(new FileReader(notificationsFilePath.toString()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (!line.isBlank()) {
                            notifications.add(line);
                        }
                    }
                    br.close();
                    Files.delete(notificationsFilePath);
                } catch (IOException e) {
                    throw new DataStorageException("an error occurred when reading or clearing the notifications file",
                        e);
                }
            }
        }

        return notifications;
    }
}
