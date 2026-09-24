package server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Gửi phản hồi chào mừng kết nối thành công
            out.println("CONNECTED_SUCCESS");

            String clientMsg;
            while ((clientMsg = in.readLine()) != null) {
                System.out.println("[SERVER NHAN]: " + clientMsg);
                out.println("SERVER_ECHO: " + clientMsg);
            }
        } catch (IOException e) {
            System.out.println("[TB]: Client da ngat ket noi.");
        } finally {
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }
}