package server;

import model.Room;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {
    public static ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, GameMatch> matches = new ConcurrentHashMap<>();
    private static int roomCounter = 100;

    public static synchronized Room createRoom(String hostNickname) {
        String roomId = "R" + (++roomCounter);
        Room room = new Room(roomId, hostNickname);
        rooms.put(roomId, room);
        return room;
    }

    public static List<Room> getWaitingRooms() {
        List<Room> waitingList = new ArrayList<>();
        for (Room r : rooms.values()) {
            if ("WAITING".equals(r.getStatus()) && r.getGuestNickname() == null) {
                waitingList.add(r);
            }
        }
        return waitingList;
    }
}