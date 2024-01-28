package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final int SERVER_PORT = 2000;
    private static final ConcurrentHashMap<String, ClientManager> activeClients = new ConcurrentHashMap<>();
    private ExecutorService clientThreadExecutor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        new ChatServer().start();
    }


    //crea el server
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server started on port " + SERVER_PORT);

            while (!serverSocket.isClosed()) {
                ClientManager clientManager = new ClientManager(serverSocket.accept());
                clientThreadExecutor.submit(clientManager);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static ConcurrentHashMap<String, ClientManager> getActiveClients() {
        return activeClients;
    }
}
