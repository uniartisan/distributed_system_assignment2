package client;

import common.Constants;

import java.io.IOException;
import java.util.Timer;

public class Core extends Thread {
    class HeartBeat extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
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

    class CountDown {
        int seconds = 30;
        boolean isCancelled = false;
        Timer timer = new Timer();
        // Timer Task = () -> {
        // if (seconds > 0) {
        // seconds--;
        // } else {
        //
        //
        // }};
    }

    // public TicTacToeClient ticTacToeClient;
    // public void Core (TicTacToeClient ticTacToeClient){
    // this.ticTacToeClient = ticTacToeClient;
    // }

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

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
