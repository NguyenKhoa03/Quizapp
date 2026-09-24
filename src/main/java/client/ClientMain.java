package client;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ClientMain extends JFrame {
    private JTextField txtIp, txtPort;
    private JButton btnConnect;
    private JLabel lblStatus;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientMain() {
        setTitle("Quiz App - Ket Noi Server");
        setSize(380, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("  IP Server:"));
        txtIp = new JTextField("127.0.0.1");
        add(txtIp);

        add(new JLabel("  Port:"));
        txtPort = new JTextField("8888");
        add(txtPort);

        btnConnect = new JButton("Ket Noi");
        add(btnConnect);

        lblStatus = new JLabel("Chua ket noi", SwingConstants.CENTER);
        add(lblStatus);

        btnConnect.addActionListener(e -> connectToServer());
    }

    private void connectToServer() {
        String ip = txtIp.getText().trim();
        int port = Integer.parseInt(txtPort.getText().trim());

        // Chạy kết nối trên Luồng ngầm (tránh treo giao diện)
        new Thread(() -> {
            try {
                socket = new Socket(ip, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String serverResponse = in.readLine();
                if ("CONNECTED_SUCCESS".equals(serverResponse)) {
                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setText("Ket noi thanh cong!");
                        lblStatus.setForeground(Color.GREEN);
                        btnConnect.setEnabled(false);
                    });
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