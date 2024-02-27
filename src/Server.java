import java.io.*;
import java.net.*;

public class Server {
    private static final int PORT = 6666;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server is listening on port " + PORT);

        try {
            while (true) {
                new GameHandler(serverSocket.accept()).start();
            }
        } finally {
            serverSocket.close();
        }
    }
}