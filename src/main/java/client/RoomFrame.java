package client;

import com.google.gson.Gson;
import model.Message;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.PrintWriter;

public class RoomFrame extends JFrame {
    private String nickname;
    private String roomId;
    private boolean isHost;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson = new Gson();
    private boolean isListening = true;

    private JLabel lblRoomInfo, lblHostStatus, lblGuestStatus;
    private JButton btnAction, btnLeaveRoom;

    public RoomFrame(String nickname, String roomId, boolean isHost, String opponentName, PrintWriter out, BufferedReader in, Point location) {
        this.nickname = nickname;
        this.roomId = roomId;
        this.isHost = isHost;
        this.out = out;
        this.in = in;

        setTitle("Phòng Chờ Game - ID: " + roomId);
        setSize(420, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (location != null) setLocation(location);
        else setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));

        lblRoomInfo = new JLabel("PHÒNG CHỜ ID: " + roomId, SwingConstants.CENTER);
        lblRoomInfo.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblRoomInfo, BorderLayout.NORTH);

        JPanel panelCenter = new JPanel(new GridLayout(2, 1, 10, 10));
        String hostText = isHost ? "Chủ phòng (Bạn): " + nickname : "Chủ phòng: " + opponentName;
        String guestText = !isHost ? "Khách (Bạn): " + nickname : (opponentName.isEmpty() ? "Khách: (Đang chờ...)" : "Khách: " + opponentName);

        lblHostStatus = new JLabel(hostText, SwingConstants.CENTER);
        lblGuestStatus = new JLabel(guestText + (isHost && opponentName.isEmpty() ? "" : " [CHƯA SẴN SÀNG]"), SwingConstants.CENTER);

        panelCenter.add(lblHostStatus);
        panelCenter.add(lblGuestStatus);
        add(panelCenter, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        btnAction = new JButton(isHost ? "BẮT ĐẦU" : "SẴN SÀNG");
        if (isHost) btnAction.setEnabled(false);

        btnLeaveRoom = new JButton(isHost ? "HỦY PHÒNG" : "THOÁT PHÒNG");
        btnLeaveRoom.setBackground(new Color(220, 53, 69));

        bottomPanel.add(btnAction);
        bottomPanel.add(btnLeaveRoom);
        add(bottomPanel, BorderLayout.SOUTH);

        btnAction.addActionListener(e -> {
            if (isHost) {
                out.println(gson.toJson(new Message("START_GAME", nickname, "", roomId)));
            } else {
                out.println(gson.toJson(new Message("READY", nickname, "", roomId)));
                btnAction.setEnabled(false);
                btnAction.setText("ĐÃ SẴN SÀNG");
                lblGuestStatus.setText("Khách (Bạn): " + nickname + " [ĐÃ SẴN SÀNG]");
            }
        });

        btnLeaveRoom.addActionListener(e -> {
            out.println(gson.toJson(new Message("LEAVE_ROOM", nickname, "", roomId)));
            returnToLobby();
        });

        listenToServer();
    }

    private void listenToServer() {
        new Thread(() -> {
            try {
                String response;
                while (isListening && (response = in.readLine()) != null) {
                    Message msg = gson.fromJson(response, Message.class);
                    String action = msg.getAction();

                    SwingUtilities.invokeLater(() -> {
                        if ("GUEST_JOINED".equals(action)) {
                            lblGuestStatus.setText("Khách: " + msg.getNickname() + " [CHƯA SẴN SÀNG]");
                        } else if ("GUEST_READY".equals(action)) {
                            lblGuestStatus.setText(lblGuestStatus.getText().replace("[CHƯA SẴN SÀNG]", "[ĐÃ SẴN SÀNG]"));
                            if (isHost) btnAction.setEnabled(true);
                        } else if ("GUEST_LEFT".equals(action)) {
                            lblGuestStatus.setText("Khách: (Đang chờ...)");
                            if (isHost) btnAction.setEnabled(false);
                        } else if ("HOST_LEFT".equals(action)) {
                            JOptionPane.showMessageDialog(this, "Chủ phòng đã giải tán phòng!");
                            returnToLobby();
                        } else if ("GAME_STARTED".equals(action)) {
                            JOptionPane.showMessageDialog(this, "TRẬN ĐẤU BẮT ĐẦU!");
                        }
                    });
                }
            } catch (Exception e) {
                System.out.println("Thoát luồng RoomFrame");
            }
        }).start();
    }

    public void returnToLobby() {
        isListening = false;
        LobbyFrame lobby = new LobbyFrame(nickname, out, in, getLocation());
        lobby.setVisible(true);
        this.dispose();
    }
}