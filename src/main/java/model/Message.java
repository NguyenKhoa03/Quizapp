package model;

public class Message {
    private String action;
    private String nickname;
    private String content;
    private String roomId;
    private int time;
    private int score;

    public Message(String action, String nickname, String content) {
        this(action, nickname, content, null, 0, 0);
    }

    public Message(String action, String nickname, String content, String roomId) {
        this(action, nickname, content, roomId, 0, 0);
    }

    public Message(String action, String nickname, String content, String roomId, int time, int score) {
        this.action = action;
        this.nickname = nickname;
        this.content = content;
        this.roomId = roomId;
        this.time = time;
        this.score = score;
    }

    public String getAction() { return action; }
    public String getNickname() { return nickname; }
    public String getContent() { return content; }
    public String getRoomId() { return roomId; }
    public int getTime() { return time; }
    public int getScore() { return score; }
}