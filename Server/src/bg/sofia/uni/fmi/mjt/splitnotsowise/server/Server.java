package bg.sofia.uni.fmi.mjt.splitnotsowise.server;

import bg.sofia.uni.fmi.mjt.splitnotsowise.server.checker.NotNullChecker;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.DebtManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.FriendshipManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.GroupManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.NotificationManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.database.UserManager;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.exception.DataStorageException;
import bg.sofia.uni.fmi.mjt.splitnotsowise.server.io.IOHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class Server {
    public static final String PROJECT_NAME = "Server";
    public static final int SERVER_PORT = 1234;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;
    private static Server instance;
    private boolean isServerWorking;
    private Selector selector;
    private ByteBuffer buffer;

    private Server() {
    }

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    public boolean isServerWorking() {
        return isServerWorking;
    }

    public void start() throws IOException {
        try {
            initializeManagers();
        } catch (DataStorageException e) {
            System.out.println("a problem occurred when loading data from files");
            return;
        }
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            buffer = ByteBuffer.allocate(BUFFER_SIZE);

            isServerWorking = true;
            while (isServerWorking) {
                try {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }
                    handleSelectedKeys();

                } catch (IOException e) {
                    System.out.println("error occurred while processing client request: " + e.getMessage());
                }
            }

        }
    }

    private void initializeManagers() throws DataStorageException {
        UserManager.getInstance().initialize();
        FriendshipManager.getInstance().initialize();
        GroupManager.getInstance().initialize();
        DebtManager.getInstance().initialize();
        NotificationManager.getInstance().initialize();
    }

    private void handleSelectedKeys() throws IOException {
        Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            if (key.isReadable()) {
                SocketChannel clientChannel = (SocketChannel) key.channel();
                String clientInput = getClientInput(clientChannel);
                if (clientInput == null) {
                    continue;
                }
                // add try catch
                String output = IOHandler.getInstance().handle(clientInput, key);
                writeClientOutput(clientChannel, output);
            } else if (key.isAcceptable()) {
                accept(key);
            }

            keyIterator.remove();
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        NotNullChecker.check(channel, selector);
        channel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        NotNullChecker.check(clientChannel);
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }

        buffer.flip();
        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        NotNullChecker.check(clientChannel, output);
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();
        clientChannel.write(buffer);
    }

    private void accept(SelectionKey key) throws IOException {
        NotNullChecker.check(key);
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel acceptedSocketChannel = serverSocketChannel.accept();

        acceptedSocketChannel.configureBlocking(false);
        SelectionKey newKey = acceptedSocketChannel.register(selector, SelectionKey.OP_READ);
        newKey.attach(null);
    }

    public void stop() {
        this.isServerWorking = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }
}