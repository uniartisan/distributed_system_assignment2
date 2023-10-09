package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;

public class TicTacToeClient extends Thread {

    public static boolean isMyTurn = false;

    public boolean isGameOver = false;
    public static String username = "";
    public static String serverHost = "";
    public static int serverPort = 0;
    public static String disPlaySymbol = "";
    public static HashMap<String, JButton> buttonHashMap = new HashMap<>();

    public static BufferedWriter out;
    public static BufferedReader in;
    private JFrame frame;
    private JButton[][] boardButtons;
    private JLabel currentTurnLabel;
    private JLabel timerTitleLabel;
    private JLabel timerValueLabel;
    public static JTextArea chatArea;
    private JTextField chatInput;
    private JButton quitButton;

    public TicTacToeClient() {
        frame = new JFrame("Distributed Tic-Tac-Toe");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());

        // Timer section on top-left
        JPanel timerPanel = new JPanel(new GridLayout(2, 1));
        timerTitleLabel = new JLabel("Timer", JLabel.CENTER);
        // 倒计时，异步操作，暂时未实现
        // TODO: Implement the timer
        timerValueLabel = new JLabel("17", JLabel.CENTER);
        timerPanel.add(timerTitleLabel);
        timerPanel.add(timerValueLabel);
        timerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(timerPanel, BorderLayout.WEST);

        // Current Turn section above the board
        JPanel turnPanel = new JPanel(new BorderLayout());
        // 用于显示当前轮到谁的回合
        currentTurnLabel = new JLabel("Finding Player...", JLabel.CENTER);
        turnPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        turnPanel.add(currentTurnLabel, BorderLayout.CENTER);
        frame.add(turnPanel, BorderLayout.NORTH);

        // Board section
        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        boardButtons = new JButton[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                boardButtons[i][j] = new JButton("");
                boardButtons[i][j].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO: Send the move to the server
                        // TODO: Update the board, according to user profile. (X or O)
                        // TODO: Check if the game is over
                        // TODO: Check if is the user's turn
                        JButton button = (JButton) e.getSource();
                        // FIXME: This is just a test, remove it later
                        button.setText("X");
                    }
                });
                boardPanel.add(boardButtons[i][j]);
            }
        }
        frame.add(boardPanel, BorderLayout.CENTER);

        // Chat section
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatArea = new JTextArea(10, 15);
        chatArea.setEditable(false);
        chatInput = new JTextField(15);
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatPanel.add(chatInput, BorderLayout.SOUTH);
        frame.add(chatPanel, BorderLayout.EAST);

        chatInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Send the chat message to the server
                // TODO

                // Clear the chat input
                chatInput.setText("");
            }
        });

        // Quit button at the bottom-left
        quitButton = new JButton("QUIT");
        quitButton.setPreferredSize(new Dimension(70, 30)); // 设定了新的尺寸
        // 创建一个新的面板并使用流布局，然后添加quitButton
        JPanel quitPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quitPanel.add(quitButton);

        timerPanel.add(quitPanel, BorderLayout.SOUTH); // 将quitPanel添加到timerPanel的南部
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: Send the quit message to the server
                // FIXME
                quitClient(0, username);
            }
        });

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            // FIXME: This is just a test, remove it later
            Random random = new Random();
            username = "user-" + random.nextInt(100000);
            System.out.println("No username provided, using a random one: " + username);
            // System.out.println("Usage: java -jar TicTacToeClient.jar <username>
            // <server-ip> <server-port>");
            // System.out.println("Error: Invalid number of arguments provided");
            // System.exit(0);
        } else {
            username = args[0];
            serverHost = args[1];
            serverPort = Integer.parseInt(args[2]);
        }
        new TicTacToeClient();
    }

    public static void connectServer() {
        try {
            // FIXME: IP, port
            Socket socket = new Socket("localhost", 8080);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void requestServer(String message) {
        try {
            System.out.println("客户端发送的消息为：" + message);
            // TODO: Send the message to the server

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void turn(JButton btnBoard, String position) {
        if (isMyTurn) {
            btnBoard.setText(disPlaySymbol);
            btnBoard.setEnabled(false);
            // Set textfiled to show the latest status
            // TODO
            // requestServer("turn:"+position+gameid);
            isMyTurn = false;
        } else {
            // JOptionPane.showMessageDialog(frame, "It's not your turn!");
        }
        // Check countdown
        // TODO
    }

    public static void pickRandomPosition() {
        // 超时未选中，随机选中一个位置
        // TODO
    }

    public static void quitClient(int gameid, String username) {
        // TODO
        System.exit(0);
    }

}
