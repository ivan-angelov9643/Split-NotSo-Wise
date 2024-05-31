import bg.sofia.uni.fmi.mjt.splitnotsowise.client.Client;

public class Main {
    public static void main(String[] args) {
        Client client = Client.getInstance();
        client.connectToServer();
    }
}