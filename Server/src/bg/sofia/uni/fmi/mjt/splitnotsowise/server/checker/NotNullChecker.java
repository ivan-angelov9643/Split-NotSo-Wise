package bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker;

public class NotNullChecker {
    public static void check(Object... args) {
        for (Object arg: args) {
            if (arg == null) {
                throw new IllegalArgumentException("all arguments must be not null");
            }
        }
    }
}
