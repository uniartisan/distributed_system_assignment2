package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.*;

import common.Constants;
import common.Player;

public class TicTacToeClient extends Thread {

    public static boolean isMyTurn = false;

    public boolean isGameOver = false;
    public static int gameId = -1;
    public static int rankNumber = -1;
    public static int rank = 0;
    public static Player opponent = new Player(); // 对手信息
    public static String username = "";
    public static String serverHost = "";
    public static int serverPort = 0;
    public static String disPlaySymbol = "";
    public static JLabel currentTurnLabel;
    public static HashMap<String, JButton> buttonHashMap = new HashMap<>();
    public static Core.Countdown countdown;

    public static BufferedWriter out;
    public static BufferedReader in;
    public static JFrame frame;
    private static final int MAX_MESSAGES = 10;
    public static Deque<String> msgList = new LinkedList<>();
    private JButton[][] boardButtons;

    private JLabel timerTitleLabel;
    static public JLabel timerValueLabel;
    public static JTextArea chatArea;
    private JTextField chatInput;
    private JButton quitButton;

    public TicTacToeClient() {
        frame = new JFrame("Distributed Tic-Tac-Toe");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());

        // Timer section on top-left
        JPanel timerPanel = new JPanel(new GridLayout(3, 1));
        timerTitleLabel = new JLabel("Timer", JLabel.CENTER);
        // 倒计时，异步操作
        timerValueLabel = new JLabel("20", JLabel.CENTER);
        JLabel currentUser = new JLabel("User："+username, JLabel.CENTER);
        timerPanel.add(timerTitleLabel);
        timerPanel.add(currentUser);
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
                int hashMapKey = i * 3 + j + 1;
                buttonHashMap.put(Integer.toString(hashMapKey), boardButtons[i][j]);
                boardButtons[i][j].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO: Send the move to the server
                        JButton button = (JButton) e.getSource();
                        turn(button, Integer.toString(hashMapKey));
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
                if(!chatInput.getText().isEmpty()){
                    requestServer(Constants.Chat + Constants.MESSAGE_DELIMITER + gameId + Constants.MESSAGE_DELIMITER + rank + Constants.MESSAGE_DELIMITER + username + Constants.MESSAGE_DELIMITER + chatInput.getText());
                }

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
                // end the quit message to the server
                quitClient(TicTacToeClient.gameId, TicTacToeClient.disPlaySymbol);
            }
        });

        frame.pack();
        frame.setVisible(true);
        Core core = new Core();
        core.start();
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            // FIXME: This is just a test, remove it later
            Random random = new Random();
            username = "user-" + random.nextInt(100000);
            serverHost = "localhost";
            serverPort = 8888;
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
        connectServer();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    TicTacToeClient window = new TicTacToeClient();
                    window.frame.setVisible(true);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public static void connectServer() {
        try {
            Socket socket = new Socket(serverHost, serverPort);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void requestServer(String message) {
        try {
            System.out.println("客户端发送的消息为：" + message);
            out.write(message + "\n");
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void turn(JButton btnBoard, String position) {
        if (isMyTurn) {
            btnBoard.setText(disPlaySymbol);
            btnBoard.setEnabled(false);
            btnBoard.requestFocusInWindow();
            // Set textfiled to show the latest status
            currentTurnLabel.setText(String.format("RANK#%d %s's Turn(%s)", rank, username, disPlaySymbol.equals("X") ? "O" : "X"));
            // Send the move to the server
            requestServer(Constants.Turn + Constants.MESSAGE_DELIMITER + gameId + Constants.MESSAGE_DELIMITER + disPlaySymbol + Constants.MESSAGE_DELIMITER +position);
            isMyTurn = false;
        } else {
             JOptionPane.showMessageDialog(frame, "It's not your turn!");
        }
        // Check countdown
        if (countdown != null) {
            countdown.cancelled = true;
        }
    }

    public static void pickRandomPosition() {
        // 超时未选中，随机选中一个位置
        ArrayList<JButton> availableButtons = new ArrayList<>();
        for(int i = 1; i <= 9; i++){
            JButton btnBoard = buttonHashMap.get(Integer.toString(i));
            if (btnBoard.isEnabled()){
                availableButtons.add(btnBoard);
            }
        }
        // 检查是否有可用按钮
        if (!availableButtons.isEmpty()) {
            // 随机选择一个可用按钮
            Random random = new Random();
            int randomIndex = random.nextInt(availableButtons.size());
            System.out.println("随机选中的按钮为：" + randomIndex);
            JButton selectedButton = availableButtons.get(randomIndex);
            selectedButton.doClick();
        } else {
            // 没有可用按钮的情况下执行的操作
            // 处理没有可用按钮的情况
            // 这里应该不会发生！
        }
    }
    public static void setTurn(String display, String pos) {
        JButton btnBoard = buttonHashMap.get(pos);
        btnBoard.setText(display);
        btnBoard.setEnabled(false);
        currentTurnLabel.setText(String.format("RANK#%d %s's Turn(%s)", rank, username, display.equals("X") ? "O" : "X"));
        isMyTurn = true;
        countdown = new Core.Countdown();
        countdown.start();
    }
    public static void quitClient(int gameId, String disPlaySymbol) {
        requestServer(Constants.Quit + Constants.MESSAGE_DELIMITER + gameId + Constants.MESSAGE_DELIMITER + disPlaySymbol);
        System.exit(0);
    }

    public static void resetBoard() {
        for (int i = 1; i <= 9; i++) {
            JButton btnBoard = buttonHashMap.get(Integer.toString(i));
            btnBoard.setText("");
            btnBoard.setEnabled(true);
        }
        currentTurnLabel.setText("Finding player...");
        chatArea.setText("");
        isMyTurn = false;
        if (countdown != null) {
            countdown.cancelled = true;
        }
    }
    public static void resumeGameBoard(String x, String o) {
        if (x.equals("-")){
            x = "";
        }
        if(o.equals("-")){
            o = "";
        }
        char[] xs = x.toCharArray();
        char[] os = o.toCharArray();
        // Update the board, according to user profile. (X or O)
        for(char i : xs){
            JButton btnBoard = buttonHashMap.get(Character.toString(i));
            btnBoard.setText("X");
            btnBoard.setEnabled(false);
        }
        for(char i : os){
            JButton btnBoard = buttonHashMap.get(Character.toString(i));
            btnBoard.setText("O");
            btnBoard.setEnabled(false);
        }

        if ((xs.length == os.length && disPlaySymbol.equals("X")) || (xs.length > os.length && disPlaySymbol.equals("O"))) {
            currentTurnLabel.setText(String.format("RANK#%d %s's Turn(%s)", rank, username, disPlaySymbol.equals("X") ? "O" : "X"));
            isMyTurn = true;
            countdown = new Core.Countdown();
            countdown.start();
        } else {
            isMyTurn = false;
            currentTurnLabel.setText(String.format("RANK#%d %s's Turn(%s)", opponent.rank, opponent.name, disPlaySymbol.equals("X") ? "O" : "X"));
        }
    }


    public static void updateChatArea(String newMsg) {
        if (msgList.size() == MAX_MESSAGES) {
            msgList.pollFirst();  // 移除最早的消息
        }
        msgList.offerLast(newMsg);  // 添加新消息至队列尾部
        chatArea.setText(String.join("", msgList));  // 将队列中的消息拼接成字符串并显示在聊天区域
    }
}
