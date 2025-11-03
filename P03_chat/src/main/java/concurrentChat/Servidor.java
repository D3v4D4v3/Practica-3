package concurrentChat;

import Handlers.ClientHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Servidor {

    private static final int PORT = 8080;
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private static final ConcurrentHashMap<String, ClientHandler> users = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ExecutorService pool = Executors.newCachedThreadPool();
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.err.println("Server active in localhost:" + PORT);
            while (true) {
                System.out.println("Esperando conexión...");
                Socket socket = server.accept();
                System.out.println("Nuevo cliente IP:" + socket.getInetAddress());
                ClientHandler client = new ClientHandler(socket, clients, users);
                clients.add(client);
                pool.execute(client);
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
}
