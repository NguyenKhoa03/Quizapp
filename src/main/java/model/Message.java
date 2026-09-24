package model;

public class Message {
    private String action;
    private String nickname;
    private String content;

    public Message(String action, String nickname, String content) {
        this.action = action;
        this.nickname = nickname;
        this.content = content;
    }

    public String getAction() { return action; }
    public String getNickname() { return nickname; }
    public String getContent() { return content; }
}   