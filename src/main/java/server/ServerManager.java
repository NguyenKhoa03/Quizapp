package server;

import com.google.gson.Gson;
import model.Message;
import model.Room;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServerManager {
    public static ConcurrentHashMap<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();

    public static void broadcastRoomList() {
        List<Room> waitingList = RoomManager.getWaitingRooms();
        String jsonList = new Gson().toJson(waitingList);
        Message msg = new Message("ROOM_LIST_RESPONSE", "", jsonList);

        for (ClientHandler client : onlineUsers.values()) {
            if (client.getCurrentRoomId() == null) { // Chỉ gửi cho người đang ở Sảnh chờ
                client.sendMessage(msg);
            }
        }
    }
}