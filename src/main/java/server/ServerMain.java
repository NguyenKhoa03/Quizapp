package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    private static final int PORT = 8888;

    public static void main(String[] args) {
        System.out.println("=== SERVER DANG CHAY TAI PORT " + PORT + " ===");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[MOI KET NOI]: Tu IP " + socket.getInetAddress());
                
                // Mở luồng xử lý riêng cho Client này
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}