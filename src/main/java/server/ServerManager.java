package server;

import java.util.concurrent.ConcurrentHashMap;

public class ServerManager {
    // Lưu Danh sách Nickname -> ClientHandler đang Online
    public static ConcurrentHashMap<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();
}