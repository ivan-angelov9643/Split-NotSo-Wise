package bg.sofia.uni.fmi.mjt.splitnotsowise.server.hasher;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {
    private static final String ALGORITHM = "MD5";
    private static final int HEX_FF = 0xff;
    private static final int HEX_100 = 0x100;
    private static final int RADIX = 16;
    public static String hash(String password) {
        NotNullChecker.check(password);
        String generatedPassword;
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(password.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(Integer.toString((b & HEX_FF) + HEX_100, RADIX).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("no such algorithm");
        }

        return generatedPassword;
    }

    public static void main(String[] args) {
        String passwordToHash = "password";
        String hashedPassword = hash(passwordToHash);
        System.out.println(hashedPassword);
    }
}

