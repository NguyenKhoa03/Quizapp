package client;

import com.formdev.flatlaf.FlatDarkLaf;
import com.google.gson.Gson;
import model.Message;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ClientMain extends JFrame {
    private JTextField txtIp, txtPort, txtNickname;
    private JButton btnConnect;
    private JLabel lblStatus;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson = new Gson();

    public ClientMain() {
        setTitle("Quiz App - Dang Nhap");
        setSize(380, 260);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 2, 10, 10));

        add(new JLabel("  IP Server:"));
        txtIp = new JTextField("127.0.0.1");
        add(txtIp);

        add(new JLabel("  Port:"));
        txtPort = new JTextField("8888");
        add(txtPort);

        add(new JLabel("  Nickname:"));
        txtNickname = new JTextField("Player1");
        add(txtNickname);

        btnConnect = new JButton("Vao Sanh Cho");
        add(btnConnect);

        lblStatus = new JLabel("Chua ket noi", SwingConstants.CENTER);
        add(lblStatus);

        btnConnect.addActionListener(e -> connectAndJoinLobby());
    }

    private void connectAndJoinLobby() {
        String ip = txtIp.getText().trim();
        int port = Integer.parseInt(txtPort.getText().trim());
        String nickname = txtNickname.getText().trim();

        if (nickname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap Nickname!");
            return;
        }

        new Thread(() -> {
            try {
                socket = new Socket(ip, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                if ("CONNECTED_SUCCESS".equals(in.readLine())) {
                    // Gửi tin nhắn JOIN_LOBBY bằng JSON
                    Message msg = new Message("JOIN_LOBBY", nickname, "");
                    out.println(gson.toJson(msg));

                    String response = in.readLine();
                    Message resMsg = gson.fromJson(response, Message.class);

                    if ("JOIN_LOBBY_SUCCESS".equals(resMsg.getAction())) {
                        SwingUtilities.invokeLater(() -> {
                            Point currentLocation = getLocation();
                            LobbyFrame lobby = new LobbyFrame(nickname, currentLocation);
                            lobby.setVisible(true);
                            this.dispose(); // Đóng cửa sổ đăng nhập
                        });
                    }
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Loi ket noi!");
                    lblStatus.setForeground(Color.RED);
                });
            }
        }).start();
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> new ClientMain().setVisible(true));
    }
}