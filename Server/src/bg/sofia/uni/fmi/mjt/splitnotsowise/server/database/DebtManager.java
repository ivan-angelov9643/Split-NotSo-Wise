package bg.sofia.uni.fmi.mjt.splitnotsowise.server.database;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.PaidStatus;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.SplitFriendStatus;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.SplitGroupStatus;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.user.User;

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

public class DebtManager {
    private static final String DEBTS_FILE_NAME_POSTFIX = "_debts.txt";
    private static final String PAYMENTS_FILE_NAME_POSTFIX = "_payments.txt";
    public static String projectName;
    public static String debtsFileNamePostfix;
    public static String paymentsFileNamePostfix;
    private static final String DEBTS_FILE_HEADER = "payee,amount";
    private static final String PAYMENTS_FILE_HEADER = "payee,amount";
    private static DebtManager instance;
    private final Map<String, Map<String, Double>> payers;
    private final Map<String, Map<String, Double>> payees;
    private final Map<String, Map<String, List<Double>>> paymentsByUsername;
    private UserManager userManager;
    private FriendshipManager friendshipManager;
    private GroupManager groupManager;
    private NotificationManager notificationManager;
    private boolean dataLoaded;
    private DebtManager() {
        payers = new HashMap<>();
        payees = new HashMap<>();
        paymentsByUsername = new HashMap<>();
        dataLoaded = false;
    }

    public void initialize() throws DataStorageException {
        if (!dataLoaded) {
            loadDebts();
            loadPayments();
            dataLoaded = true;
        }
    }

    public static void resetInstance() {
        instance = new DebtManager();
    }

    public static void setInstance(DebtManager debtManager) {
        NotNullChecker.check(debtManager);
        instance = debtManager;
    }

    public static DebtManager getInstance() {
        if (instance == null) {
            instance = new DebtManager();
        }
        return instance;
    }

    private void initializeManagers() {
        userManager = UserManager.getInstance();
        friendshipManager = FriendshipManager.getInstance();
        groupManager = GroupManager.getInstance();
        notificationManager = NotificationManager.getInstance();
    }

    private void initializeFilesInfo() {
        if (projectName == null) {
            projectName = PROJECT_NAME;
        }
        if (debtsFileNamePostfix == null) {
            debtsFileNamePostfix = DEBTS_FILE_NAME_POSTFIX;
        }
        if (paymentsFileNamePostfix == null) {
            paymentsFileNamePostfix = PAYMENTS_FILE_NAME_POSTFIX;
        }
    }

    public Map<String, Map<String, Double>> getPayersMap() {
        return payers;
    }

    public Map<String, Map<String, Double>> getPayeesMap() {
        return payees;
    }

    public Map<String, Map<String, List<Double>>> getPaymentsByUsernameMap() {
        return paymentsByUsername;
    }

    public static void setProjectName(String name) {
        NotNullChecker.check(name);
        projectName = name;
    }

    public static void setDebtsFileNamePostfix(String fileNamePostfix) {
        NotNullChecker.check(fileNamePostfix);
        debtsFileNamePostfix = fileNamePostfix;
    }

    public static void setPaymentsFileNamePostfix(String fileNamePostfix) {
        NotNullChecker.check(fileNamePostfix);
        paymentsFileNamePostfix = fileNamePostfix;
    }

    public static void resetProjectName() {
        projectName = PROJECT_NAME;
    }

    public static void resetDebtsFileNamePostfix() {
        debtsFileNamePostfix = DEBTS_FILE_NAME_POSTFIX;
    }

    public static void resetPaymentsFileNamePostfix() {
        paymentsFileNamePostfix = PAYMENTS_FILE_NAME_POSTFIX;
    }

    private static double round(double amount) {
        NotNullChecker.check(amount);
        final double hundred = 100;
        return Math.round(amount * hundred) / hundred;
    }

    private synchronized void addToDebtsMaps(String payer, String payee, double amount) {
        NotNullChecker.check(payer, payee, amount);
        payers.computeIfAbsent(payer, k -> new HashMap<>());
        payees.computeIfAbsent(payee, k -> new HashMap<>());

        payers.computeIfAbsent(payee, k -> new HashMap<>());
        payees.computeIfAbsent(payer, k -> new HashMap<>());

        Double payeeToPayerDebt = payers.get(payee).get(payer);
        Double payerToPayeeDebt = payers.get(payer).get(payee);

        if (payeeToPayerDebt != null) {
            double amountOwedByPayeeToPayer = payeeToPayerDebt;

            if (amount < amountOwedByPayeeToPayer) {
                addDebt(payee, payer, round(amountOwedByPayeeToPayer - amount));
            } else if (amountOwedByPayeeToPayer < amount) {
                clearDebt(payee, payer);
                addDebt(payer, payee, round(amount - amountOwedByPayeeToPayer));
            } else {
                clearDebt(payee, payer);
            }
        } else if (payerToPayeeDebt != null) {
            mergeDebt(payer, payee, amount);
        } else {
            addDebt(payer, payee, amount);
        }
    }

    private synchronized void addToPaymentsMap(String payer, String payee, double amountPaid) {
        NotNullChecker.check(payer, payee, amountPaid);
        paymentsByUsername.computeIfAbsent(payer, k -> new HashMap<>());
        paymentsByUsername.get(payer).computeIfAbsent(payee, k -> new ArrayList<>());
        paymentsByUsername.get(payer).get(payee).add(amountPaid);
    }

    private synchronized void clearDebt(String payer, String payee) {
        NotNullChecker.check(payer, payee);
        payers.get(payer).remove(payee);
        payees.get(payee).remove(payer);
    }

    private synchronized void mergeDebt(String payer, String payee, double amount) {
        NotNullChecker.check(payer, payee, amount);
        payers.get(payer).merge(payee, amount, Double::sum);
        payees.get(payee).merge(payer, amount, Double::sum);
    }

    private synchronized void addDebt(String payer, String payee, double amount) {
        NotNullChecker.check(payer, payee, amount);
        if (amount == 0) {
            clearDebt(payer, payee);
        } else {
            payers.get(payer).put(payee, amount);
            payees.get(payee).put(payer, amount);
        }
    }

    private void recalculateDebts(String payer, String payee, double amountPaid) {
        NotNullChecker.check(payer, payee, amountPaid);
        double owedAmount = 0;
        if (payers.containsKey(payer) && payers.get(payer).containsKey(payee)) {
            owedAmount = payers.get(payer).get(payee);
        }
        double newAmount = owedAmount - amountPaid;
        newAmount = round(newAmount);
        if (newAmount >= 0) {
            addDebt(payer, payee, newAmount);
        } else {
            clearDebt(payer, payee);
            addDebt(payee, payer, -newAmount);
        }
        addToPaymentsMap(payer, payee, amountPaid);
    }

    public void loadDebts() throws DataStorageException {
        initializeManagers();
        initializeFilesInfo();
        Set<String> usernames = userManager.getUserByUsernameMap().keySet();

        for (String payer : usernames) {
            Path debtsFilePath = Paths.get(projectName, payer + debtsFileNamePostfix);

            if (Files.exists(debtsFilePath)) {
                try (BufferedReader br = new BufferedReader(new FileReader(debtsFilePath.toString()))) {
                    br.readLine();
                    String line;

                    while ((line = br.readLine()) != null) {
                        if (line.isBlank()) {
                            continue;
                        }
                        String[] data = line.split(",");
                        String payee = data[0].trim();
                        double amount = round(Double.parseDouble(data[1]));
                        addToDebtsMaps(payer, payee, amount);
                    }
                } catch (IOException e) {
                    throw new DataStorageException("an error occurred when loading the debts file", e);
                }
            }
        }
    }

    public void loadPayments() throws DataStorageException {
        initializeManagers();
        initializeFilesInfo();
        Set<String> usernames = userManager.getUserByUsernameMap().keySet();

        for (String payer : usernames) {
            Path paymentsFilePath = Paths.get(projectName, payer + paymentsFileNamePostfix);

            if (Files.exists(paymentsFilePath)) {
                try (BufferedReader br = new BufferedReader(new FileReader(paymentsFilePath.toString()))) {
                    br.readLine();
                    String line;

                    while ((line = br.readLine()) != null) {
                        if (line.isBlank()) {
                            continue;
                        }
                        String[] data = line.split(",");
                        String payee = data[0].trim();
                        double amountPaid = round(Double.parseDouble(data[1]));
                        recalculateDebts(payer, payee, amountPaid);
                    }
                } catch (IOException e) {
                    throw new DataStorageException("an error occurred when loading the payments file", e);
                }
            }
        }
    }

    private synchronized void addToDebtsFile(String payer, String payee, double owedAmount)
        throws DataStorageException {
        NotNullChecker.check(payer, payee, owedAmount);
        initializeFilesInfo();
        Path payerDebtsFilePath = Paths.get(projectName, payer + debtsFileNamePostfix);
        boolean needsHeader = !Files.exists(payerDebtsFilePath);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(payerDebtsFilePath.toString(), true))) {
            if (needsHeader) {
                bw.write(DEBTS_FILE_HEADER);
                bw.newLine();
            }
            String line = payee + ',' + owedAmount;
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            throw new DataStorageException("an error occurred when updating debts file", e);
        }
    }

    public SplitFriendStatus splitFriend(String payer, String payee, double amount) throws DataStorageException {
        NotNullChecker.check(payer, payee, amount);
        initializeManagers();
        initializeFilesInfo();
        Map<String, User> userByUsername = userManager.getUserByUsernameMap();
        if (!userByUsername.containsKey(payer)) {
            return SplitFriendStatus.USER_DOES_NOT_EXIST;
        }
        if (!friendshipManager.friendshipExist(payee, payer)) {
            return SplitFriendStatus.FRIENDSHIP_DOES_NOT_EXIST;
        }
        double owedAmount = round(amount / 2);
        addToDebtsFile(payer, payee, owedAmount);
        addToDebtsMaps(payer, payee, owedAmount);
        notificationManager.addAmountSplitNotification(payer, payee, owedAmount);
        return SplitFriendStatus.SUCCESS;
    }

    public SplitGroupStatus splitGroup(String groupName, String payee, double amount) throws DataStorageException {
        NotNullChecker.check(groupName, payee, amount);
        initializeManagers();
        initializeFilesInfo();
        Map<String, Set<String>> groupMembersByGroupName = groupManager.getGroupMembersByGroupNameMap();
        if (!groupMembersByGroupName.containsKey(groupName)) {
            return SplitGroupStatus.GROUP_DOES_NOT_EXIST;
        }
        Set<String> members = groupMembersByGroupName.get(groupName);
        if (!members.contains(payee)) {
            return SplitGroupStatus.USER_NOT_IN_GROUP;
        }
        for (String payer : members) {
            if (payer.equals(payee)) {
                continue;
            }
            double owedAmount = round(amount / members.size());
            addToDebtsFile(payer, payee, owedAmount);
            addToDebtsMaps(payer, payee, owedAmount);
            notificationManager.addAmountSplitNotification(payer, payee, owedAmount);
        }
        return SplitGroupStatus.SUCCESS;
    }

    public PaidStatus payDebt(String payer, String payee, double amountPaid) throws DataStorageException {
        NotNullChecker.check(payer, payee, amountPaid);
        if (payer.equals(payee)) {
            return PaidStatus.SAME_USERNAMES;
        }
        initializeManagers();
        initializeFilesInfo();
        if (!userManager.getUserByUsernameMap().containsKey(payer)) {
            return PaidStatus.USER_DOES_NOT_EXIST;
        }
        if (!payers.containsKey(payer) || !payers.get(payer).containsKey(payee)) {
            return PaidStatus.PAYER_DOES_NOT_OWE_PAYEE;
        }
        Path paymentsFilePath = Paths.get(projectName, payer + paymentsFileNamePostfix);
        boolean needsHeader = !Files.exists(paymentsFilePath);
        synchronized (this) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(paymentsFilePath.toString(), true))) {
                if (needsHeader) {
                    bw.write(PAYMENTS_FILE_HEADER);
                }
                bw.write(payee + ',' + amountPaid);
                bw.newLine();
            } catch (IOException e) {
                throw new DataStorageException("an error occurred when updating payments file", e);
            }
        }
        recalculateDebts(payer, payee, amountPaid);
        notificationManager.addPaymentApprovedNotification(payer, payee, amountPaid);
        return PaidStatus.SUCCESS;
    }
}
