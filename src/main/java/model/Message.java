package model;

public class Message {
    private String action;
    private String nickname;
    private String content;
    private String roomId;

    // Constructor 3 tham số
    public Message(String action, String nickname, String content) {
        this.action = action;
        this.nickname = nickname;
        this.content = content;
        this.roomId = null;
    }

    // Constructor 4 tham số
    public Message(String action, String nickname, String content, String roomId) {
        this.action = action;
        this.nickname = nickname;
        this.content = content;
        this.roomId = roomId;
    }

    public String getAction() { return action; }
    public String getNickname() { return nickname; }
    public String getContent() { return content; }
    public String getRoomId() { return roomId; }
}