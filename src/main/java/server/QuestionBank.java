package server;

import model.Question;
import java.util.ArrayList;
import java.util.List;

public class QuestionBank {
    public static List<Question> get20Questions() {
        List<Question> list = new ArrayList<>();
        list.add(new Question(1, "What is the synonym of 'Happy'?", new String[]{"Sad", "Joyful", "Angry", "Tired"}, 1));
        list.add(new Question(2, "Choose the correct past tense of 'Go':", new String[]{"Gone", "Goes", "Went", "Going"}, 2));
        list.add(new Question(3, "Which word is a noun?", new String[]{"Quickly", "Apple", "Beautiful", "Run"}, 1));
        list.add(new Question(4, "Antonym of 'Big':", new String[]{"Huge", "Small", "Tall", "Large"}, 1));
        list.add(new Question(5, "Fill in: She ___ to school every day.", new String[]{"go", "goes", "went", "going"}, 1));
        list.add(new Question(6, "What is the plural form of 'Child'?", new String[]{"Childs", "Children", "Childrens", "Childes"}, 1));
        list.add(new Question(7, "Find the adjective: 'It is a sunny day.'", new String[]{"It", "Is", "Sunny", "Day"}, 2));
        list.add(new Question(8, "Which animal says 'Meow'?", new String[]{"Dog", "Cat", "Cow", "Duck"}, 1));
        list.add(new Question(9, "Choose the correct spelling:", new String[]{"Recieve", "Receive", "Receve", "Ricieve"}, 1));
        list.add(new Question(10, "Opposite of 'Hot':", new String[]{"Warm", "Cold", "Boiling", "Spicy"}, 1));
        list.add(new Question(11, "Fill in: I have been living here ___ 2010.", new String[]{"for", "since", "in", "at"}, 1));
        list.add(new Question(12, "What is the capital of England?", new String[]{"Paris", "London", "Berlin", "Madrid"}, 1));
        list.add(new Question(13, "Which one is a verb?", new String[]{"Table", "Jump", "Blue", "Happy"}, 1));
        list.add(new Question(14, "Past participle of 'Eat':", new String[]{"Ate", "Eaten", "Eats", "Eating"}, 1));
        list.add(new Question(15, "Fill in: Look ___ those birds in the sky!", new String[]{"at", "on", "in", "to"}, 0));
        list.add(new Question(16, "Opposite of 'Fast':", new String[]{"Quick", "Slow", "High", "Short"}, 1));
        list.add(new Question(17, "Find the correct article: ___ European country.", new String[]{"An", "A", "The", "No article"}, 1));
        list.add(new Question(18, "Which word means 'Very cold'?", new String[]{"Freezing", "Melted", "Warm", "Hot"}, 0));
        list.add(new Question(19, "Fill in: They ___ playing football now.", new String[]{"is", "am", "are", "be"}, 2));
        list.add(new Question(20, "Synonym of 'Smart':", new String[]{"Clever", "Stupid", "Slow", "Quiet"}, 0));
        return list;
    }
}