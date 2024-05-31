package bg.sofia.uni.fmi.mjt.splitnotsowise.server.command;

public interface Command {
    void execute();

    String getMessage();
}