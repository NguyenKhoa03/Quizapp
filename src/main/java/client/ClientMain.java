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
    private JButton btnConnect, btnJoinLobby;
    private JLabel lblStatus;
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson = new Gson();

    public ClientMain() {
        setTitle("Quiz App - Kết Nối & Đăng Nhập");
        setSize(400, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2, 8, 8));

        add(new JLabel("  IP Server:"));
        txtIp = new JTextField("127.0.0.1");
        add(txtIp);

        add(new JLabel("  Port:"));
        txtPort = new JTextField("8888");
        add(txtPort);

        btnConnect = new JButton("1. Kết Nối Server");
        add(btnConnect);

        lblStatus = new JLabel("Chưa kết nối", SwingConstants.CENTER);
        add(lblStatus);

        add(new JLabel("  Nickname:"));
        txtNickname = new JTextField("Player1");
        txtNickname.setEnabled(false);
        add(txtNickname);

        btnJoinLobby = new JButton("2. Vào Sảnh Chờ");
        btnJoinLobby.setEnabled(false);
        add(btnJoinLobby);

        btnConnect.addActionListener(e -> connectToServer());
        btnJoinLobby.addActionListener(e -> joinLobby());
    }

    private void connectToServer() {
        String ip = txtIp.getText().trim();
        int port = Integer.parseInt(txtPort.getText().trim());

        new Thread(() -> {
            try {
                socket = new Socket(ip, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                if ("CONNECTED_SUCCESS".equals(in.readLine())) {
                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setText("Đã kết nối Server!");
                        lblStatus.setForeground(Color.GREEN);
                        txtIp.setEnabled(false);
                        txtPort.setEnabled(false);
                        btnConnect.setEnabled(false);
                        txtNickname.setEnabled(true);
                        btnJoinLobby.setEnabled(true);
                    });
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    lblStatus.setText("Lỗi kết nối!");
                    lblStatus.setForeground(Color.RED);
                });
            }
        }).start();
    }

    private void joinLobby() {
        String nickname = txtNickname.getText().trim();
        if (nickname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Nickname!");
            return;
        }

        new Thread(() -> {
            try {
                out.println(gson.toJson(new Message("JOIN_LOBBY", nickname, "")));
                String response = in.readLine();
                Message resMsg = gson.fromJson(response, Message.class);

                if ("JOIN_LOBBY_SUCCESS".equals(resMsg.getAction())) {
                    SwingUtilities.invokeLater(() -> {
                        LobbyFrame lobby = new LobbyFrame(nickname, out, in, getLocation());
                        lobby.setVisible(true);
                        this.dispose();
                    });
                } else if ("JOIN_LOBBY_FAIL".equals(resMsg.getAction())) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, resMsg.getContent()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> new ClientMain().setVisible(true));
    }
}