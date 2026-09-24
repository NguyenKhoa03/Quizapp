package server;

import com.google.gson.Gson;
import model.Message;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson = new Gson();
    private String currentNickname;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("CONNECTED_SUCCESS");

            String clientMsg;
            while ((clientMsg = in.readLine()) != null) {
                Message msg = gson.fromJson(clientMsg, Message.class);

                if ("JOIN_LOBBY".equals(msg.getAction())) {
                    this.currentNickname = msg.getNickname();
                    ServerManager.onlineUsers.put(currentNickname, this);
                    System.out.println("[LOBBY]: " + currentNickname + " da vao sanh cho.");

                    Message res = new Message("JOIN_LOBBY_SUCCESS", currentNickname, "Vao sanh cho thanh cong!");
                    out.println(gson.toJson(res));
                }
            }
        } catch (IOException e) {
            System.out.println("[TB]: Client ngat ket noi.");
        } finally {
            if (currentNickname != null) {
                ServerManager.onlineUsers.remove(currentNickname);
            }
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }
}