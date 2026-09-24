package client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Message;
import model.Room;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.List;

public class LobbyFrame extends JFrame {
    private String nickname;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson = new Gson();
    private volatile boolean isListening = true;

    private JTable roomTable;
    private DefaultTableModel tableModel;
    private JTextField txtRoomIdInput;

    public LobbyFrame(String nickname, PrintWriter out, BufferedReader in, Point location) {
        this.nickname = nickname;
        this.out = out;
        this.in = in;

        setTitle("Sảnh Chờ - " + nickname);
        setSize(550, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (location != null) setLocation(location);
        else setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel lblWelcome = new JLabel("  Xin chào: " + nickname, SwingConstants.LEFT);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 14));
        JButton btnCreateRoom = new JButton("TẠO PHÒNG MỚI");
        topPanel.add(lblWelcome, BorderLayout.WEST);
        topPanel.add(btnCreateRoom, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID Phòng", "Chủ Phòng", "Số Lượng", "Trạng Thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        roomTable = new JTable(tableModel);
        add(new JScrollPane(roomTable), BorderLayout.CENTER);

        roomTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = roomTable.getSelectedRow();
                if (selectedRow != -1) {
                    String selectedRoomId = tableModel.getValueAt(selectedRow, 0).toString();
                    txtRoomIdInput.setText(selectedRoomId);
                    if (e.getClickCount() == 2) joinRoom(selectedRoomId);
                }
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(new JLabel("ID Phòng:"));
        txtRoomIdInput = new JTextField(8);
        JButton btnJoinById = new JButton("Vào Phòng");
        JButton btnRefresh = new JButton("Làm Mới List");

        bottomPanel.add(txtRoomIdInput);
        bottomPanel.add(btnJoinById);
        bottomPanel.add(btnRefresh);
        add(bottomPanel, BorderLayout.SOUTH);

        btnCreateRoom.addActionListener(e -> out.println(gson.toJson(new Message("CREATE_ROOM", nickname, ""))));
        btnRefresh.addActionListener(e -> out.println(gson.toJson(new Message("GET_ROOM_LIST", nickname, ""))));
        btnJoinById.addActionListener(e -> joinRoom(txtRoomIdInput.getText().trim()));

        listenToServer();
        out.println(gson.toJson(new Message("GET_ROOM_LIST", nickname, "")));
    }

    private void joinRoom(String roomId) {
        if (roomId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hoặc nhập ID phòng!");
            return;
        }
        out.println(gson.toJson(new Message("JOIN_ROOM", nickname, "", roomId)));
    }

    private void listenToServer() {
        new Thread(() -> {
            try {
                String response;
                while (isListening && (response = in.readLine()) != null) {
                    Message msg = gson.fromJson(response, Message.class);
                    String action = msg.getAction();

                    if ("CREATE_ROOM_SUCCESS".equals(action)) {
                        isListening = false;
                        SwingUtilities.invokeLater(() -> {
                            RoomFrame roomFrame = new RoomFrame(nickname, msg.getRoomId(), true, "", out, in, getLocation());
                            roomFrame.setVisible(true);
                            this.dispose();
                        });
                        break; // Ngắt luồng đọc của Lobby lập tức
                    } 
                    else if ("JOIN_ROOM_SUCCESS".equals(action)) {
                        isListening = false;
                        SwingUtilities.invokeLater(() -> {
                            RoomFrame roomFrame = new RoomFrame(nickname, msg.getRoomId(), false, msg.getContent(), out, in, getLocation());
                            roomFrame.setVisible(true);
                            this.dispose();
                        });
                        break; // Ngắt luồng đọc của Lobby lập tức
                    } 
                    else if ("ROOM_LIST_RESPONSE".equals(action)) {
                        SwingUtilities.invokeLater(() -> {
                            tableModel.setRowCount(0);
                            java.lang.reflect.Type listType = new TypeToken<List<Room>>(){}.getType();
                            List<Room> rooms = gson.fromJson(msg.getContent(), listType);
                            for (Room r : rooms) {
                                tableModel.addRow(new Object[]{r.getRoomId(), r.getHostNickname(), r.getPlayerCount(), r.getStatus()});
                            }
                        });
                    } 
                    else if ("JOIN_ROOM_FAIL".equals(action)) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msg.getContent()));
                    }
                }
            } catch (Exception e) {
                System.out.println("Thoát luồng đọc Lobby.");
            }
        }).start();
    }
}