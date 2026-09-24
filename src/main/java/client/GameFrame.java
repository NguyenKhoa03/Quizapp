package client;

import com.google.gson.Gson;
import model.Message;
import model.Question;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;

public class GameFrame extends JFrame {
    private String nickname;
    private String roomId;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson = new Gson();
    private List<Question> questions;
    private volatile boolean isListening = true;

    private int currentQuestionIndex = 0;
    private int myScore = 0;
    private int opponentScore = 0;

    private JLabel lblTimer, lblMyScore, lblOpponentScore, lblQuestion, lblProgress;
    private JButton[] btnOptions = new JButton[4];

    public GameFrame(String nickname, String roomId, List<Question> questions, PrintWriter out, BufferedReader in, Point location) {
        this.nickname = nickname;
        this.roomId = roomId;
        this.questions = questions;
        this.out = out;
        this.in = in;

        setTitle("Đang Thi Đấu 1v1 - Phòng " + roomId);
        setSize(600, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (location != null) setLocation(location);
        else setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new GridLayout(1, 3));
        lblMyScore = new JLabel("Bạn: 0 điểm", SwingConstants.LEFT);
        lblMyScore.setFont(new Font("Arial", Font.BOLD, 14));

        lblTimer = new JLabel("Thời gian: 60s", SwingConstants.CENTER);
        lblTimer.setFont(new Font("Arial", Font.BOLD, 18));
        lblTimer.setForeground(Color.ORANGE);

        lblOpponentScore = new JLabel("Đối thủ: 0 điểm", SwingConstants.RIGHT);
        lblOpponentScore.setFont(new Font("Arial", Font.BOLD, 14));

        topPanel.add(lblMyScore);
        topPanel.add(lblTimer);
        topPanel.add(lblOpponentScore);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        lblQuestion = new JLabel("Câu hỏi...", SwingConstants.CENTER);
        lblQuestion.setFont(new Font("Arial", Font.PLAIN, 16));
        centerPanel.add(lblQuestion, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        for (int i = 0; i < 4; i++) {
            btnOptions[i] = new JButton("Đáp án " + (i + 1));
            int optIndex = i;
            btnOptions[i].addActionListener(e -> checkAnswer(optIndex));
            optionsPanel.add(btnOptions[i]);
        }
        centerPanel.add(optionsPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        lblProgress = new JLabel("Câu 1 / 20", SwingConstants.CENTER);
        add(lblProgress, BorderLayout.SOUTH);

        displayQuestion();
        listenToServer();
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questions.size()) {
            Question q = questions.get(currentQuestionIndex);
            lblQuestion.setText("<html><body style='width: 400px; text-align: center;'>" + q.getText() + "</body></html>");
            String[] opts = q.getOptions();
            for (int i = 0; i < 4; i++) {
                btnOptions[i].setText(opts[i]);
                btnOptions[i].setEnabled(true);
            }
            lblProgress.setText("Câu " + (currentQuestionIndex + 1) + " / " + questions.size());
        } else {
            lblQuestion.setText("Bạn đã hoàn thành 20 câu hỏi! Đang chờ kết quả...");
            for (JButton btn : btnOptions) btn.setEnabled(false);
        }
    }

    private void checkAnswer(int selectedIndex) {
        Question q = questions.get(currentQuestionIndex);
        if (selectedIndex == q.getCorrectIndex()) {
            myScore += 1;
            lblMyScore.setText("Bạn: " + myScore + " điểm");
        }
        currentQuestionIndex++;
        sendScoreUpdate(currentQuestionIndex >= questions.size());
        displayQuestion();
    }

    private void sendScoreUpdate(boolean isFinished) {
        String status = isFinished ? "FINISHED" : "IN_PROGRESS";
        out.println(gson.toJson(new Message("UPDATE_SCORE", nickname, status, roomId, 0, myScore)));
    }

    private void listenToServer() {
        new Thread(() -> {
            try {
                String response;
                while (isListening && (response = in.readLine()) != null) {
                    Message msg = gson.fromJson(response, Message.class);
                    String action = msg.getAction();

                    if ("GAME_OVER".equals(action)) {
                        isListening = false;
                        SwingUtilities.invokeLater(() -> showResultDialog(msg.getContent()));
                        break;
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            if ("TIMER_TICK".equals(action)) {
                                lblTimer.setText("Thời gian: " + msg.getTime() + "s");
                            } else if ("OPPONENT_SCORE".equals(action)) {
                                opponentScore = msg.getScore();
                                lblOpponentScore.setText("Đối thủ: " + opponentScore + " điểm");
                            }
                        });
                    }
                }
            } catch (Exception e) {
                System.out.println("Thoát luồng GameFrame");
            }
        }).start();
    }

    private void showResultDialog(String rawContent) {
        try {
            // Parse dạng: WIN|14|3
            String[] parts = rawContent.split("\\|");
            String outcome = parts[0];
            int finalMyScore = parts.length > 1 ? Integer.parseInt(parts[1]) : myScore;
            int finalOppScore = parts.length > 2 ? Integer.parseInt(parts[2]) : opponentScore;

            JDialog dialog = new JDialog(this, "Kết Quả Trận Đấu", true);
            dialog.setSize(380, 380);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout(15, 15));

            JLabel lblTitle = new JLabel("", SwingConstants.CENTER);
            lblTitle.setFont(new Font("Arial", Font.BOLD, 28));

            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

            if ("WIN".equals(outcome)) {
                lblTitle.setText("YOU WIN!");
                lblTitle.setForeground(new Color(40, 167, 69));

                JLabel lblStar = new JLabel();
                lblStar.setAlignmentX(Component.CENTER_ALIGNMENT);
                ImageIcon starIcon = loadStarImage(100, 100);
                if (starIcon != null) {
                    lblStar.setIcon(starIcon);
                } else {
                    lblStar.setText("⭐");
                    lblStar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
                }

                JLabel lblStarReward = new JLabel("+1 SAO", SwingConstants.CENTER);
                lblStarReward.setFont(new Font("Arial", Font.BOLD, 22));
                lblStarReward.setForeground(new Color(255, 193, 7));
                lblStarReward.setAlignmentX(Component.CENTER_ALIGNMENT);

                centerPanel.add(lblStar);
                centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                centerPanel.add(lblStarReward);

            } else if ("LOSE".equals(outcome)) {
                lblTitle.setText("YOU LOSE!");
                lblTitle.setForeground(new Color(220, 53, 69));

                JLabel lblNoStar = new JLabel("+0 SAO", SwingConstants.CENTER);
                lblNoStar.setFont(new Font("Arial", Font.BOLD, 20));
                lblNoStar.setForeground(Color.GRAY);
                lblNoStar.setAlignmentX(Component.CENTER_ALIGNMENT);

                centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
                centerPanel.add(lblNoStar);
            } else {
                lblTitle.setText("DRAW!");
                lblTitle.setForeground(new Color(255, 152, 0));

                JLabel lblNoStar = new JLabel("+0 SAO", SwingConstants.CENTER);
                lblNoStar.setFont(new Font("Arial", Font.BOLD, 20));
                lblNoStar.setForeground(Color.GRAY);
                lblNoStar.setAlignmentX(Component.CENTER_ALIGNMENT);

                centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
                centerPanel.add(lblNoStar);
            }

            JLabel lblScoreDetail = new JLabel("Tỉ số: " + finalMyScore + " - " + finalOppScore, SwingConstants.CENTER);
            lblScoreDetail.setFont(new Font("Arial", Font.PLAIN, 16));
            lblScoreDetail.setAlignmentX(Component.CENTER_ALIGNMENT);

            centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            centerPanel.add(lblScoreDetail);

            JButton btnBack = new JButton("QUAY VỀ SẢNH CHỜ");
            btnBack.setFont(new Font("Arial", Font.BOLD, 14));
            btnBack.addActionListener(e -> {
                dialog.dispose();
                LobbyFrame lobby = new LobbyFrame(nickname, out, in, getLocation());
                lobby.setVisible(true);
                this.dispose();
            });

            JPanel bottomPanel = new JPanel(new FlowLayout());
            bottomPanel.add(btnBack);

            dialog.add(lblTitle, BorderLayout.NORTH);
            dialog.add(centerPanel, BorderLayout.CENTER);
            dialog.add(bottomPanel, BorderLayout.SOUTH);

            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Kết quả trận đấu: " + rawContent);
            LobbyFrame lobby = new LobbyFrame(nickname, out, in, getLocation());
            lobby.setVisible(true);
            this.dispose();
        }
    }

    private ImageIcon loadStarImage(int width, int height) {
        try {
            BufferedImage bimg = null;
            URL imgURL = getClass().getResource("/star.png");
            if (imgURL != null) {
                bimg = ImageIO.read(imgURL);
            } else {
                File f = new File("star.png");
                if (f.exists()) {
                    bimg = ImageIO.read(f);
                }
            }
            if (bimg != null) {
                Image scaled = bimg.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception e) {
            System.out.println("Không nạp được star.png, chuyển sang Emoji.");
        }
        return null;
    }
}