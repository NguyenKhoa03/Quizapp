package client;

import javax.swing.*;
import java.awt.*;

public class LobbyFrame extends JFrame {
    private String nickname;

    public LobbyFrame(String nickname, Point location) {
        this.nickname = nickname;

        setTitle("Quiz App - Sanh Cho (Lobby)");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (location != null) setLocation(location);
        else setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));

        JLabel lblWelcome = new JLabel("Xin chao: " + nickname, SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 16));
        lblWelcome.setForeground(new Color(0, 180, 216));
        add(lblWelcome, BorderLayout.NORTH);

        JPanel panelCenter = new JPanel(new GridLayout(2, 1, 10, 10));
        JButton btnCreateRoom = new JButton("TAO PHONG");
        JButton btnJoinRoom = new JButton("VAO PHONG");

        panelCenter.add(btnCreateRoom);
        panelCenter.add(btnJoinRoom);
        add(panelCenter, BorderLayout.CENTER);
    }
}