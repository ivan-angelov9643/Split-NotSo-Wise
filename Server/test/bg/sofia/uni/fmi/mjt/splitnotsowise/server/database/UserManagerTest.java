package bg.sofia.uni.fmi.mjt.splitnotsowise.server.database;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.UserManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.hasher.PasswordHasher;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.operationsstatus.RegistrationStatus;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserManagerTest {

    private static UserManager userManager;
    public final Path testUsersFilePath = Paths.get("testUsers.txt");

    @BeforeEach
    public void setUp() throws IOException {
        Files.createFile(testUsersFilePath);
        UserManager.setUsersFilePath(testUsersFilePath);

        UserManager.resetInstance();
        userManager = UserManager.getInstance();
        userManager.getUserByUsernameMap().clear();
    }


    @AfterEach
    public void cleanup() throws IOException {
        UserManager.resetUsersFilePath();
        Files.delete(testUsersFilePath);
    }

    @Test
    public void testLoadUsers() throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testUsersFilePath.toString()))) {
            writer.write("first name, last name, username, password hash\n");
            writer.write("First1,Last1,user1," + PasswordHasher.hash("password") + "\n");
            writer.write("First2,Last2,user2," + PasswordHasher.hash("password") + "\n");
        }

        userManager.loadUsers();

        Map<String, User> userByUsernameMap = userManager.getUserByUsernameMap();
        assertEquals(2, userByUsernameMap.size());
        assertEquals("First1", userByUsernameMap.get("user1").firstName());
        assertEquals("Last1", userByUsernameMap.get("user1").lastName());
        assertEquals("First2", userByUsernameMap.get("user2").firstName());
        assertEquals("Last2", userByUsernameMap.get("user2").lastName());
    }

    @Test
    public void testRegisterUserSuccess() throws DataStorageException {
        User newUser = new User("First1", "Last1", "user1",
            PasswordHasher.hash("password"));
        RegistrationStatus status = userManager.registerUser(newUser);
        Map<String, User> userByUsernameMap = userManager.getUserByUsernameMap();

        assertEquals(RegistrationStatus.SUCCESS, status);
        assertEquals(newUser, userByUsernameMap.get("user1"));
    }

    @Test
    public void testRegisterUserUsernameAlreadyExists() throws DataStorageException {
        User existingUser = new User("First1", "Last2", "user1",
            PasswordHasher.hash("password"));
        Map<String, User> userByUsernameMap = userManager.getUserByUsernameMap();
        userByUsernameMap.put("user1", existingUser);
        User newUser = new User("First2", "Last2", "user1",
            PasswordHasher.hash("password"));
        RegistrationStatus status = userManager.registerUser(newUser);

        assertEquals(RegistrationStatus.USERNAME_ALREADY_EXISTS, status);
        assertEquals(existingUser, userByUsernameMap.get("user1"));
    }
}
