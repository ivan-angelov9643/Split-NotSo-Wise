import bg.sofia.uni.fmi.mjt.splitnotsowise.server.Server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = Server.getInstance();
        server.start();
    }
}