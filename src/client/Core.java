package client;

import common.Constants;

import javax.swing.*;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Core extends Thread {
    public static int resumeTimeout = -1;

    static class HeartBeat extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                    TicTacToeClient.out.write(Constants.DEFAULT_RESPONSE + "\n");
                    TicTacToeClient.out.flush();
                    // TODO: Send the heartbeat message to the server
                } catch (Exception e) {
                    e.printStackTrace();
                    // TODO: Handle the exception

                    TicTacToeClient.chatArea.append("Disconnect from server, exiting in 5 seconds...\n");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                    System.exit(0);
                }

            }
        }

    }

    public static class Countdown {
        int seconds = 20;

        boolean cancelled = false;
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (resumeTimeout != -1 && seconds > resumeTimeout) {
                    seconds = resumeTimeout;
                }
                if (seconds > 0 && !cancelled) {
                    seconds--;
                    TicTacToeClient.resumeTimeout = seconds;
                    TicTacToeClient.timerValueLabel.setText(Integer.toString(seconds));
                } else if (cancelled) {
                    seconds = 20;
                    resumeTimeout = -1;
                    TicTacToeClient.resumeTimeout = seconds;
                    TicTacToeClient.timerValueLabel.setText("20");
                    timer.cancel();
                } else {
                    System.out.println("time is up");
                    TicTacToeClient.pickRandomPosition();
                    seconds = 20;
                    resumeTimeout = -1;
                    TicTacToeClient.resumeTimeout = seconds;
                    TicTacToeClient.timerValueLabel.setText("20");
                    timer.cancel();
                }
            }
        };

        public void start() {
            timer.scheduleAtFixedRate(task, 0, 1000);
        }
    }

    @Override
    public void run() {
        try {
            TicTacToeClient.requestServer(Constants.NewPlayer + Constants.MESSAGE_DELIMITER + TicTacToeClient.username);
            String response = TicTacToeClient.in.readLine();
            HeartBeat heartBeat = new HeartBeat(); // 注册心跳线程
            heartBeat.start(); // 开始后台心跳线程
            while (response.equals(Constants.DEFAULT_RESPONSE)) {
                response = TicTacToeClient.in.readLine();
            }
            System.out.println("Response from server：" + response);
            String[] responseArray = response.split(Constants.MESSAGE_DELIMITER);

            // 获取共同参数
            TicTacToeClient.gameId = Integer.parseInt(responseArray[1]);
            TicTacToeClient.rank = Integer.parseInt(responseArray[2]);
            TicTacToeClient.opponent.name = responseArray[3];
            TicTacToeClient.opponent.rank = Integer.parseInt(responseArray[4]);

            // 逻辑判断
            if (responseArray[0].equals(Constants.ResumeGame)) {
                // Resume the game.
                resumeGame(responseArray);
            } else {
                // Start a new game.
                startNewGame(responseArray);
            }

            //
            for (response = TicTacToeClient.in.readLine(); response != null; response = TicTacToeClient.in.readLine()) {
                System.out.println("Response from server：" + response);

                if (response.equals(Constants.DEFAULT_RESPONSE)) {
                    continue;
                }
                responseArray = response.split(Constants.MESSAGE_DELIMITER);
                switch (responseArray[0]) {
                    case Constants.Turn:
                        TicTacToeClient.setTurn(responseArray[1], responseArray[2]);
                        TicTacToeClient.startTimer();
                        break;
                    case Constants.GameOver:
                        handleGameOver(responseArray);
                        break;
                    case Constants.Chat:
                        TicTacToeClient.updateChatArea(responseArray[1] + "\n");
                        break;
                    case Constants.Draw:
                        handleDraw();
                        break;
                    case Constants.NewGame:
                        handleNewGame(responseArray);
                        break;

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void handleGameOver(String[] responseArray) {
        if (responseArray[1].equals(TicTacToeClient.username)) {
            TicTacToeClient.rank += 5;
        } else if (responseArray[1].equals(TicTacToeClient.opponent.name)) {
            TicTacToeClient.rank -= 5;
            TicTacToeClient.rank = Math.max(TicTacToeClient.rank, 0);
        } else {
            TicTacToeClient.rank += 2;
        }

        if (responseArray[1].equals("Nobody")) {
            TicTacToeClient.currentTurnLabel.setText("Game Drawn");
        } else if (responseArray[1].equals(TicTacToeClient.opponent.name)) {
            TicTacToeClient.currentTurnLabel
                    .setText(String.format("RANK#%d %s Wins!", TicTacToeClient.opponent.rank, responseArray[1]));
        } else {
            TicTacToeClient.currentTurnLabel
                    .setText(String.format("RANK#%d %s Wins!", TicTacToeClient.rank, responseArray[1]));

        }

        if (TicTacToeClient.countdown != null) {
            TicTacToeClient.countdown.cancelled = true;
        }
        playAgainPopup();
    }

    private void handleDraw() {
        TicTacToeClient.rank += 2;
        TicTacToeClient.currentTurnLabel.setText("Game Drawn");
        if (TicTacToeClient.countdown != null) {
            TicTacToeClient.countdown.cancelled = true;
        }
        playAgainPopup();
    }

    private void handleNewGame(String[] responseArray) {
        // restart a new game
        TicTacToeClient.gameId = Integer.parseInt(responseArray[1]);
        TicTacToeClient.rank = Integer.parseInt(responseArray[2]);
        TicTacToeClient.opponent.name = responseArray[3];
        TicTacToeClient.opponent.rank = Integer.parseInt(responseArray[4]);
        if (responseArray[5].equalsIgnoreCase("x")) {
            TicTacToeClient.disPlaySymbol = "X";
            TicTacToeClient.isMyTurn = true;
            TicTacToeClient.currentTurnLabel.setText(
                    String.format("RANK#%d %s's Turn(%s)", TicTacToeClient.rank, TicTacToeClient.username, "X"));
        } else {
            TicTacToeClient.disPlaySymbol = "O";
            TicTacToeClient.currentTurnLabel.setText(String.format("RANK#%d %s's Turn(%s)",
                    TicTacToeClient.opponent.rank, TicTacToeClient.opponent.name, "X"));
        }
    }

    private void startNewGame(String[] responseArray) {
        if (responseArray[5].equals("X")) {
            TicTacToeClient.disPlaySymbol = "X";
            TicTacToeClient.isMyTurn = true;
            TicTacToeClient.currentTurnLabel.setText(String.format("RANK#%d %s's Turn(%s)", TicTacToeClient.rank,
                    TicTacToeClient.username, TicTacToeClient.disPlaySymbol));
        } else if (responseArray[5].equals("O")) {
            TicTacToeClient.disPlaySymbol = "O";
            TicTacToeClient.isMyTurn = false;
            TicTacToeClient.currentTurnLabel.setText(String.format("RANK#%d %s's Turn(%s)",
                    TicTacToeClient.opponent.rank, TicTacToeClient.opponent.name, "X"));
        }
    }

    private void resumeGame(String[] responseArray) {
        TicTacToeClient.disPlaySymbol = responseArray[5];
        // Resume the game board.
        TicTacToeClient.resumeGameBoard(responseArray[6], responseArray[7]);
        // Resume the timeout for the first time.
        System.out.println(responseArray[8] + " " + responseArray[9]);
        if (TicTacToeClient.disPlaySymbol.equals("X")) {
            resumeTimeout = Integer.parseInt(responseArray[8]);
        } else {
            resumeTimeout = Integer.parseInt(responseArray[9]);
        }
        System.out.println("Get resume time from server: " + resumeTimeout);
    }

    public void playAgainPopup() {
        int reply = JOptionPane.showConfirmDialog(TicTacToeClient.frame, "Play again?", "Game Over",
                JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
            TicTacToeClient.resetBoard();
            TicTacToeClient.requestServer(Constants.NewPlayer + Constants.MESSAGE_DELIMITER + TicTacToeClient.username);
        } else {
            TicTacToeClient.quitClient(TicTacToeClient.gameId, TicTacToeClient.disPlaySymbol);
        }
    }

}
