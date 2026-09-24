package server;

import com.google.gson.Gson;
import model.Message;
import model.Room;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson = new Gson();
    private String currentNickname;
    private String currentRoomId;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public String getCurrentRoomId() { return currentRoomId; }

    public void sendMessage(Message msg) {
        out.println(gson.toJson(msg));
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
                String action = msg.getAction();

                if ("JOIN_LOBBY".equals(action)) {
                    String nameInput = msg.getNickname();
                    // Kiếm tra trùng tên
                    if (ServerManager.onlineUsers.containsKey(nameInput)) {
                        sendMessage(new Message("JOIN_LOBBY_FAIL", nameInput, "Nickname đã có người sử dụng!"));
                    } else {
                        this.currentNickname = nameInput;
                        ServerManager.onlineUsers.put(currentNickname, this);
                        sendMessage(new Message("JOIN_LOBBY_SUCCESS", currentNickname, ""));
                    }
                } 
                else if ("GET_ROOM_LIST".equals(action)) {
                    ServerManager.broadcastRoomList();
                } 
                else if ("CREATE_ROOM".equals(action)) {
                    Room newRoom = RoomManager.createRoom(currentNickname);
                    this.currentRoomId = newRoom.getRoomId();
                    sendMessage(new Message("CREATE_ROOM_SUCCESS", currentNickname, "", newRoom.getRoomId()));
                    ServerManager.broadcastRoomList(); // Cập nhật danh sách cho người khác
                } 
                else if ("JOIN_ROOM".equals(action)) {
                    String targetRoomId = msg.getRoomId();
                    Room room = RoomManager.rooms.get(targetRoomId);

                    if (room != null && "WAITING".equals(room.getStatus()) && room.getGuestNickname() == null) {
                        room.setGuestNickname(currentNickname);
                        this.currentRoomId = targetRoomId;

                        sendMessage(new Message("JOIN_ROOM_SUCCESS", currentNickname, room.getHostNickname(), targetRoomId));

                        ClientHandler hostHandler = ServerManager.onlineUsers.get(room.getHostNickname());
                        if (hostHandler != null) {
                            hostHandler.sendMessage(new Message("GUEST_JOINED", currentNickname, "", targetRoomId));
                        }
                        ServerManager.broadcastRoomList();
                    } else {
                        sendMessage(new Message("JOIN_ROOM_FAIL", currentNickname, "Phòng không tồn tại hoặc đã đầy!"));
                    }
                } 
                else if ("READY".equals(action)) {
                    Room room = RoomManager.rooms.get(currentRoomId);
                    if (room != null) {
                        room.setGuestReady(true);
                        ClientHandler hostHandler = ServerManager.onlineUsers.get(room.getHostNickname());
                        if (hostHandler != null) {
                            hostHandler.sendMessage(new Message("GUEST_READY", currentNickname, "", currentRoomId));
                        }
                    }
                } 
                else if ("LEAVE_ROOM".equals(action)) {
                    handleLeaveRoom();
                }
                else if ("START_GAME".equals(action)) {
                    Room room = RoomManager.rooms.get(currentRoomId);
                    if (room != null && room.isGuestReady()) {
                        room.setStatus("PLAYING");
                        Message startMsg = new Message("GAME_STARTED", "", "Bắt đầu!", currentRoomId);
                        sendMessage(startMsg);
                        
                        ClientHandler guestHandler = ServerManager.onlineUsers.get(room.getGuestNickname());
                        if (guestHandler != null) guestHandler.sendMessage(startMsg);
                        
                        ServerManager.broadcastRoomList();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("[TB]: Client " + currentNickname + " ngắt kết nối.");
        } finally {
            handleLeaveRoom();
            if (currentNickname != null) ServerManager.onlineUsers.remove(currentNickname);
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void handleLeaveRoom() {
        if (currentRoomId != null) {
            Room room = RoomManager.rooms.get(currentRoomId);
            if (room != null) {
                if (currentNickname.equals(room.getHostNickname())) {
                    // Host hủy phòng
                    ClientHandler guestHandler = ServerManager.onlineUsers.get(room.getGuestNickname());
                    if (guestHandler != null) {
                        guestHandler.sendMessage(new Message("HOST_LEFT", "", "", currentRoomId));
                    }
                    RoomManager.rooms.remove(currentRoomId);
                } else {
                    // Guest thoát phòng
                    room.setGuestNickname(null);
                    room.setGuestReady(false);
                    ClientHandler hostHandler = ServerManager.onlineUsers.get(room.getHostNickname());
                    if (hostHandler != null) {
                        hostHandler.sendMessage(new Message("GUEST_LEFT", currentNickname, "", currentRoomId));
                    }
                }
            }
            this.currentRoomId = null;
            ServerManager.broadcastRoomList(); // Thông báo phòng đã trống/hủy
        }
    }
}