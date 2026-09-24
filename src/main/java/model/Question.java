package model;

public class Question {
    private int id;
    private String text;
    private String[] options;
    private int correctIndex;

    public Question(int id, String text, String[] options, int correctIndex) {
        this.id = id;
        this.text = text;
        this.options = options;
        this.correctIndex = correctIndex;
    }

    public int getId() { return id; }
    public String getText() { return text; }
    public String[] getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }
}