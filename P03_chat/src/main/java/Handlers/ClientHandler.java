package Handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Set<ClientHandler> clients;
    private final ConcurrentHashMap<String, ClientHandler> users;
    private BufferedReader in;
    private PrintWriter out;
    private String username = "";

    public ClientHandler(Socket clientSocket, Set<ClientHandler> clients, ConcurrentHashMap<String, ClientHandler> users) {
        this.socket = clientSocket;
        this.clients = clients;
        this.users = users;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            out.println("Conexión establecida");
            out.println("Introduce tu nombre de usuario:");
            while (true) {
                String proposed = in.readLine();
                if (proposed == null) return;
                proposed = proposed.trim();
                if (proposed.isEmpty()) {
                    out.println("Nombre inválido, intenta de nuevo:");
                    continue;
                }
                if (users.containsKey(proposed)) {
                    out.println("Nombre en uso, elige otro:");
                    continue;
                }
                username = proposed;
                users.put(username, this);
                broadcast("+" + username + " se ha unido al chat");
                out.println("Comandos: /name NUEVO | /msg USUARIO MENSAJE | /all MENSAJE | /exit");
                break;
            }

            String inputMessage;
            while ((inputMessage = in.readLine()) != null) {
                inputMessage = inputMessage.trim();
                if (inputMessage.equalsIgnoreCase("/exit")) break;
                if (inputMessage.startsWith("/name ")) {
                    String newName = inputMessage.substring(6).trim();
                    changeUserName(newName);
                } else if (inputMessage.startsWith("/msg ")) {
                    String[] parts = inputMessage.split("\\s+", 3);
                    if (parts.length < 3) {
                        out.println("Uso: /msg USUARIO MENSAJE");
                    } else {
                        privateMessage(parts[1], parts[2]);
                    }
                } else if (inputMessage.startsWith("/all ")) {
                    globalMessage(inputMessage.substring(5).trim());
                } else {
                    out.println("Comando no reconocido");
                }
            }
        } catch (IOException ex) {
            System.getLogger(ClientHandler.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
            clients.remove(this);
            if (!username.isEmpty()) {
                users.remove(username);
                broadcast("-" + username + " salió del chat");
            }
        }
    }

    private void changeUserName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            out.println("Nombre inválido");
            return;
        }
        newName = newName.trim();
        if (users.containsKey(newName)) {
            out.println("Nombre en uso");
            return;
        }
        String old = this.username;
        users.remove(old);
        this.username = newName;
        users.put(newName, this);
        out.println("Tu nombre ahora es: " + newName);
        broadcast(old + " ahora es " + newName);
    }

    private void privateMessage(String targetUser, String message) {
        ClientHandler target = users.get(targetUser);
        if (target == null) {
            out.println("Usuario no encontrado: " + targetUser);
            return;
        }
        target.out.println("(privado de " + username + "): " + message);
        out.println("(privado a " + targetUser + "): " + message);
    }

    private void globalMessage(String message) {
        int count = 0;
        for (ClientHandler c : clients) {
            if (c != this) {
                c.out.println(username + ": " + message);
                count++;
            }
        }
        out.println("Enviado a " + count + " usuarios");
    }

    private void broadcast(String msg) {
        for (ClientHandler c : clients) {
            c.out.println(msg);
        }
    }
}
