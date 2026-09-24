package server;

import com.google.gson.Gson;
import model.Message;
import model.Question;
import java.util.List;

public class GameMatch implements Runnable {
    private String roomId;
    private ClientHandler host;
    private ClientHandler guest;
    private int hostScore = 0;
    private int guestScore = 0;
    private boolean hostFinished = false;
    private boolean guestFinished = false;
    private boolean isRunning = true;

    public GameMatch(String roomId, ClientHandler host, ClientHandler guest) {
        this.roomId = roomId;
        this.host = host;
        this.guest = guest;
    }

    public void updateScore(String nickname, int score, boolean finished) {
        if (nickname.equals(host.getNickname())) {
            this.hostScore = score;
            this.hostFinished = finished;
            guest.sendMessage(new Message("OPPONENT_SCORE", nickname, "", roomId, 0, score));
        } else {
            this.guestScore = score;
            this.guestFinished = finished;
            host.sendMessage(new Message("OPPONENT_SCORE", nickname, "", roomId, 0, score));
        }

        if (hostFinished && guestFinished) {
            endGame();
        }
    }

    @Override
    public void run() {
        List<Question> questions = QuestionBank.get20Questions();
        String jsonQuestions = new Gson().toJson(questions);

        Message startMsg = new Message("GAME_STARTED", "", jsonQuestions, roomId);
        host.sendMessage(startMsg);
        guest.sendMessage(startMsg);

        int timeLeft = 60;
        while (isRunning && timeLeft >= 0) {
            Message tickMsg = new Message("TIMER_TICK", "", "", roomId, timeLeft, 0);
            host.sendMessage(tickMsg);
            guest.sendMessage(tickMsg);

            if (timeLeft == 0) {
                endGame();
                break;
            }

            try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
            timeLeft--;
        }
    }

    private synchronized void endGame() {
        if (!isRunning) return;
        isRunning = false;

        Message hostMsg;
        Message guestMsg;

        if (hostScore > guestScore) {
            hostMsg = new Message("GAME_OVER", host.getNickname(), "WIN|" + hostScore + "|" + guestScore, roomId);
            guestMsg = new Message("GAME_OVER", guest.getNickname(), "LOSE|" + guestScore + "|" + hostScore, roomId);
        } else if (guestScore > hostScore) {
            hostMsg = new Message("GAME_OVER", host.getNickname(), "LOSE|" + hostScore + "|" + guestScore, roomId);
            guestMsg = new Message("GAME_OVER", guest.getNickname(), "WIN|" + guestScore + "|" + hostScore, roomId);
        } else {
            hostMsg = new Message("GAME_OVER", host.getNickname(), "DRAW|" + hostScore + "|" + guestScore, roomId);
            guestMsg = new Message("GAME_OVER", guest.getNickname(), "DRAW|" + guestScore + "|" + hostScore, roomId);
        }

        host.sendMessage(hostMsg);
        guest.sendMessage(guestMsg);

        RoomManager.rooms.remove(roomId);
        ServerManager.broadcastRoomList();
    }
}