package bg.sofia.uni.fmi.mjt.splitnotsowise.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private static final int SERVER_PORT = 1234;
    private static final int BUFFER_SIZE = 1024;
    private static Client instance;
    private Client() {

    }

    public static Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    public void connectToServer() {
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {
            socketChannel.connect(new InetSocketAddress("localhost", SERVER_PORT));
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            while (true) {
                String message = scanner.nextLine();
                if (message.isBlank()) continue;
                socketChannel.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));

                buffer.clear();
                int bytesRead = socketChannel.read(buffer);
                if (bytesRead == -1) {
                    break;
                }

                buffer.flip();
                byte[] replyBytes = new byte[buffer.remaining()];
                buffer.get(replyBytes);
                String reply = new String(replyBytes, StandardCharsets.UTF_8);
                System.out.println(reply);
                if ("quit".equals(message)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("an error occurred, check error_logs.txt file");
            ErrorLogger.log(e);
        }
    }
}