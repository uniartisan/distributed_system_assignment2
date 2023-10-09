package server;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import common.Constants;
import common.Player;

public class Connection extends Thread {
    public BufferedReader in;
    public BufferedWriter out;
    private Random random = new Random();
    public GameInfo game;
    public Countdown countdown;


    public Connection(Socket socket) throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    class HeartBeat extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    out.write(Constants.DEFAULT_RESPONSE + "\n");
                    out.flush();

                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                if (game != null) {
                    game.isResumed = false;
                }
                countdown = new Countdown();
                countdown.start();
            }

        }
    }


    class Countdown {
        int seconds = 30;
        boolean cancelled = false;
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                if (game != null && game.isResumed) {
                    seconds = 30;
                    timer.cancel();
                    cancelled = true;
                } else if (game != null && seconds > 0 && !cancelled) {
                    System.out.println("resume time left: " + seconds);
                    seconds--;
                } else if (game != null) {
                    System.out.println("resume time out");
                    seconds = 30;
                    timer.cancel();
                    cancelled = true;
                    game.isTimeout = true;
                    Player updatedX = Server.players.get(game.x.name);
                    Player updatedO = Server.players.get(game.o.name);
                    updatedX.rank += 2;
                    updatedO.rank += 2;
                    Server.players.put(game.x.name, updatedX);
                    Server.players.put(game.o.name, updatedO);
                    try {
                        game.x.out.write(Constants.Draw + "\n");
                        game.x.out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        game.o.out.write(Constants.Draw + "\n");
                        game.o.out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }
        };
        public void start(){
            timer.scheduleAtFixedRate(task, 0, 1000);
        }

    }
    public static void sendMessage(BufferedWriter out, String msg) throws IOException {
        out.write(msg + "\n");
        out.flush();
        System.out.println("resp >> " + msg);
    }

    public void run() {
        System.out.println("Connection running");



        String input;
        try {
            for (input = in.readLine(); input != null; input = in.readLine()) {
                System.out.println("req >>" + input);
                // <CMD>#<PARAMS#SPLIT#WITH#DELIMITER>
                String[] parsed = input.split(Constants.MESSAGE_DELIMITER);
                switch (parsed[0]) {
                    case Constants.DEFAULT_RESPONSE:
                        break;
                    case Constants.NewPlayer:
                        System.out.println("Games: " + Server.games.size());
                        HeartBeat heartBeat = new HeartBeat();
                        heartBeat.start();
                        // 清空旧数据
                        game = null;
                        for (Map.Entry<Integer, GameInfo> g : Server.games.entrySet()) {
                            if (g.getValue().o.name.equals(parsed[1]) || g.getValue().x.name.equals(parsed[1])) {
                                game = g.getValue();
                                break;
                            }
                        }
                        if (game == null || game.isTimeout) {
                            Player player;
                            Player p = Server.players.get(parsed[1]);
                            if (p != null) {
                                p.out = out;
                                player = p;
                            } else {
                                player = new Player(parsed[1], out);
                                Server.players.put(parsed[1], player);
                                System.out.println("New player added: " + player.name);
                            }
                            if (Server.waitingPool.isEmpty()) {
                                Server.waitingPool.add(player);
                                sendMessage(out, Constants.DEFAULT_RESPONSE);
                                System.out.println("New waiter added: " + player.name);
                            } else{
                                Player opponent = Server.waitingPool.remove(0);
                                if (random.nextBoolean()) {
                                    // player first
                                    int gameId = Server.generateGameId();
                                    sendMessage(opponent.out, Constants.NewGame + Constants.MESSAGE_DELIMITER + gameId + Constants.MESSAGE_DELIMITER
                                            + opponent.rank + Constants.MESSAGE_DELIMITER + player.name + Constants.MESSAGE_DELIMITER + player.rank + Constants.MESSAGE_DELIMITER + "O");
                                    GameInfo newGame = new GameInfo(gameId, player, opponent);
                                    Server.games.put(gameId, newGame);
                                    sendMessage(out, Constants.NewGame + Constants.MESSAGE_DELIMITER + gameId + Constants.MESSAGE_DELIMITER
                                            + player.rank + Constants.MESSAGE_DELIMITER + opponent.name + Constants.MESSAGE_DELIMITER + opponent.rank + Constants.MESSAGE_DELIMITER + "X");
                                } else {
                                    // opponent first
                                    int gameId = Server.generateGameId();
                                    sendMessage(opponent.out, Constants.NewGame + Constants.MESSAGE_DELIMITER + gameId + Constants.MESSAGE_DELIMITER
                                            + opponent.rank + Constants.MESSAGE_DELIMITER + player.name + Constants.MESSAGE_DELIMITER + player.rank + Constants.MESSAGE_DELIMITER + "X");
                                    GameInfo newGame = new GameInfo(gameId, opponent, player);
                                    Server.games.put(gameId, newGame);
                                    sendMessage(out, Constants.NewGame + Constants.MESSAGE_DELIMITER + gameId + Constants.MESSAGE_DELIMITER + player.rank
                                            + Constants.MESSAGE_DELIMITER + opponent.name + Constants.MESSAGE_DELIMITER + opponent.rank + Constants.MESSAGE_DELIMITER + "O");
                                }
                                System.out.println(opponent.name);
                            }
                        } else {
                            // game is not null, which means there's an ongoing game that should be resumed
                            System.out.println("resume " + game.x + game.o + " " + game.gameId);
                            sendMessage(out, Constants.DEFAULT_RESPONSE);

                            Player player, opponent;
                            String chess;
                            game.isResumed = true;

                            if (game.o.name.equals(parsed[1])) {
                                player = new Player(parsed[1], out);
                                opponent = game.x;
                                chess = "O";
                                GameInfo updatedInfo = game;
                                updatedInfo.o = player;
                                Server.games.put(game.gameId, updatedInfo);
                            } else {
                                player = new Player(parsed[1], out);
                                opponent = game.o;
                                chess = "X";
                                GameInfo updatedInfo = game;
                                updatedInfo.x = player;
                                Server.games.put(game.gameId, updatedInfo);
                            }

                            sendMessage(out, Constants.ResumeGame + Constants.MESSAGE_DELIMITER + game.gameId + Constants.MESSAGE_DELIMITER + player.rank + Constants.MESSAGE_DELIMITER + opponent.name +
                                    Constants.MESSAGE_DELIMITER + opponent.rank + Constants.MESSAGE_DELIMITER + chess + Constants.MESSAGE_DELIMITER + game.getPos("X") + Constants.MESSAGE_DELIMITER + game.getPos("O"));
                        }

                            break;
                    case Constants.Turn:
                        game = Server.games.get(Integer.parseInt(parsed[1]));
                        boolean isX = parsed[2].equals("X");
                        boolean isGameOver = game.updateBoard(parsed[2], parsed[3]);

                        if (isX) {
                            sendMessage(game.x.out, Constants.DEFAULT_RESPONSE);
                            sendMessage(game.o.out, Constants.Turn + Constants.MESSAGE_DELIMITER + parsed[2] + Constants.MESSAGE_DELIMITER + parsed[3]);
                        } else {
                            sendMessage(game.o.out, Constants.DEFAULT_RESPONSE);
                            sendMessage(game.x.out, Constants.Turn + Constants.MESSAGE_DELIMITER + parsed[2] + Constants.MESSAGE_DELIMITER + parsed[3]);
                        }
                        if (isGameOver) {
                            Player updatedX = Server.players.get(game.x.name);
                            Player updatedO = Server.players.get(game.o.name);
                            if (game.remainedTurns == 0) {
                                updatedX.rank += 2;
                                updatedO.rank += 2;
                                sendMessage(game.o.out, Constants.GameOver + Constants.MESSAGE_DELIMITER + "Nobody");
                                sendMessage(game.x.out, Constants.GameOver + Constants.MESSAGE_DELIMITER + "Nobody");
                            } else {
                                if (isX) {
                                    updatedX.rank += 5;
                                    updatedO.rank = Math.max(updatedO.rank - 5, 0);
                                    sendMessage(game.x.out, Constants.GameOver + Constants.MESSAGE_DELIMITER + game.x.name);
                                    sendMessage(game.o.out, Constants.GameOver + Constants.MESSAGE_DELIMITER + game.x.name);
                                } else {
                                    updatedO.rank += 5;
                                    updatedX.rank = Math.max(updatedX.rank - 5, 0);
                                    sendMessage(game.x.out, Constants.GameOver + Constants.MESSAGE_DELIMITER + game.o.name);
                                    sendMessage(game.o.out, Constants.GameOver + Constants.MESSAGE_DELIMITER + game.o.name);
                                }
                            }
                            Server.players.put(game.x.name, updatedX);
                            Server.players.put(game.o.name, updatedO);
                            Server.games.remove(game.gameId);
                            game = null;
                        }
                        break;
                    case Constants.Chat:
                        game = Server.games.get(Integer.parseInt(parsed[1]));
                        String msg = String.format("RANK#%d %s: %s", Integer.parseInt(parsed[2]), parsed[3], parsed[4]);
                        sendMessage(game.x.out, Constants.Chat + Constants.MESSAGE_DELIMITER + msg);
                        sendMessage(game.o.out, Constants.Chat + Constants.MESSAGE_DELIMITER + msg);
                        break;
                    case Constants.Quit:
                        game = Server.games.get(Integer.parseInt(parsed[1]));
                        Player updatedX = Server.players.get(game.x.name);
                        Player updatedO = Server.players.get(game.o.name);

                        if (parsed[2].equals("X")) {
                            updatedO.rank += 5;
                            updatedX.rank = Math.max(updatedX.rank - 5, 0);
                            sendMessage(game.o.out, Constants.GameOver + Constants.MESSAGE_DELIMITER + game.o.name);
                        } else {
                            updatedX.rank += 5;
                            updatedO.rank = Math.max(updatedO.rank - 5, 0);
                            sendMessage(game.x.out, Constants.GameOver + Constants.MESSAGE_DELIMITER + game.x.name);
                        }
                        Server.players.put(game.x.name, updatedX);
                        Server.players.put(game.o.name, updatedO);
                        Server.games.remove(game.gameId);
                        game = null;
                        break;
                    default:
                        sendMessage(out, Constants.DEFAULT_RESPONSE);
                        break;

                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        }

    }


}

