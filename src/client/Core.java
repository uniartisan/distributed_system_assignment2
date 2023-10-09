package client;

import common.Constants;

import javax.swing.*;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Core extends Thread {
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
                if (seconds > 0 && !cancelled) {
                    System.out.println("countdown left " + seconds + " s");
                    seconds--;
                    TicTacToeClient.timerValueLabel.setText(Integer.toString(seconds));
                } else if (cancelled) {
                    seconds = 20;
                    TicTacToeClient.timerValueLabel.setText("20");
                    timer.cancel();
                } else {
                    System.out.println("time is up");
                    TicTacToeClient.pickRandomPosition();
                    seconds = 20;
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
            TicTacToeClient.rankNumber = Integer.parseInt(responseArray[2]);
            TicTacToeClient.opponent.name = responseArray[3];
            TicTacToeClient.opponent.rank = Integer.parseInt(responseArray[4]);

            // 逻辑判断
            if (responseArray[0].equals(Constants.ResumeGame)) {
                // Resume the game.
                TicTacToeClient.disPlaySymbol = responseArray[5];
                // Resume the game board.
                System.out.println(responseArray[6]);
                System.out.println(responseArray[7]);
                TicTacToeClient.resumeGameBoard(responseArray[6], responseArray[7]);

            } else {
                // Start a new game.
                if (responseArray[5].equals("X")) {
                    TicTacToeClient.disPlaySymbol = "X";
                    TicTacToeClient.isMyTurn = true;
                    TicTacToeClient.currentTurnLabel.setText(String.format("RANK#%d %s's Turn(%s)", TicTacToeClient.rankNumber, TicTacToeClient.username, TicTacToeClient.disPlaySymbol));
                } else if (responseArray[5].equals("O")) {
                    TicTacToeClient.disPlaySymbol = "O";
                    TicTacToeClient.isMyTurn = false;
                    TicTacToeClient.currentTurnLabel.setText(String.format("RANK#%d %s's Turn(%s)", TicTacToeClient.opponent.rank, TicTacToeClient.opponent.name, "X"));
                }
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
                        break;
                    case Constants.GameOver:
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
                            TicTacToeClient.currentTurnLabel.setText(String.format("RANK#%d %s Wins!", TicTacToeClient.opponent.rank, responseArray[1]));
                        } else {
                            TicTacToeClient.currentTurnLabel.setText(String.format("RANK#%d %s Wins!", TicTacToeClient.rank, responseArray[1]));

                        }

                        if (TicTacToeClient.countdown != null) {
                            TicTacToeClient.countdown.cancelled = true;
                        }
                        playAgainPopup();
                        break;
                    case Constants.Chat:
                        TicTacToeClient.updateChatArea(responseArray[1] + "\n");
                        break;
                    case Constants.Draw:
                        TicTacToeClient.rank += 2;
                        TicTacToeClient.currentTurnLabel.setText("Game Drawn");
                        if (TicTacToeClient.countdown != null) {
                            TicTacToeClient.countdown.cancelled = true;
                        }
                        playAgainPopup();
                        break;
                    case Constants.NewGame:
                        // restart a new game
                        TicTacToeClient.gameId = Integer.parseInt(responseArray[1]);
                        TicTacToeClient.rank = Integer.parseInt(responseArray[2]);
                        TicTacToeClient.opponent.name = responseArray[3];
                        TicTacToeClient.opponent.rank = Integer.parseInt(responseArray[4]);
                        if (responseArray[5].equalsIgnoreCase("x")) {
                            TicTacToeClient.disPlaySymbol = "X";
                            TicTacToeClient.isMyTurn = true;
                            TicTacToeClient.currentTurnLabel.setText(String.format("RANK#%d %s's Turn(%s)", TicTacToeClient.rank, TicTacToeClient.username, "X"));
                        } else {
                            TicTacToeClient.disPlaySymbol = "O";
                            TicTacToeClient.currentTurnLabel.setText(String.format("RANK#%d %s's Turn(%s)", TicTacToeClient.opponent.rank, TicTacToeClient.opponent.name, "X"));
                        }
                        break;

                }


            }
        }catch (IOException e){
            e.printStackTrace();
        }


    }

    public void playAgainPopup() {
        int reply = JOptionPane.showConfirmDialog(TicTacToeClient.frame, "Play again?", "Game Over", JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
            TicTacToeClient.resetBoard();
            TicTacToeClient.requestServer(Constants.NewPlayer + Constants.MESSAGE_DELIMITER + TicTacToeClient.username);
        } else {
            TicTacToeClient.quitClient(TicTacToeClient.gameId, TicTacToeClient.disPlaySymbol);
        }
    }

}
