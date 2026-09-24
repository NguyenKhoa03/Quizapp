package model;

public class Room {
    private String roomId;
    private String hostNickname;
    private String guestNickname;
    private boolean isGuestReady = false;
    private String status = "WAITING"; // WAITING, PLAYING

    public Room(String roomId, String hostNickname) {
        this.roomId = roomId;
        this.hostNickname = hostNickname;
    }

    public String getRoomId() { return roomId; }
    public String getHostNickname() { return hostNickname; }
    public String getGuestNickname() { return guestNickname; }
    public void setGuestNickname(String guestNickname) { this.guestNickname = guestNickname; }
    public boolean isGuestReady() { return isGuestReady; }
    public void setGuestReady(boolean guestReady) { isGuestReady = guestReady; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPlayerCount() {
        return (guestNickname == null) ? "1/2" : "2/2";
    }
}   