package concurrentChat;

import Handlers.WriteHandler;
import Handlers.ReadHandler;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {

    public static Socket conection = null;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String commands = scanner.nextLine();

        if (commands.startsWith("start-conection")) {
            String address = "localhost";
            int port = 8080;
            String[] parts = commands.trim().split("\\s+");
            if (parts.length >= 3) {
                address = parts[1];
                port = Integer.parseInt(parts[2]);
            } else {
                System.out.print("IP: ");
                address = scanner.nextLine().trim();
                System.out.print("PORT: ");
                port = Integer.parseInt(scanner.nextLine().trim());
            }
            StartConection(address, port);
        } else {
            throw new AssertionError();
        }

        WriteHandler writer = new WriteHandler(conection);
        ReadHandler reader = new ReadHandler(conection);
        Thread writeThread = new Thread(writer);
        Thread readThread = new Thread(reader);
        writeThread.start();
        readThread.start();
    }

    public static void StartConection(String address, int port) {
        try {
            conection = new Socket(address, port);
        } catch (IOException ex) {
            System.getLogger(Cliente.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
}
