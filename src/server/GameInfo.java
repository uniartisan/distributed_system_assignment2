package server;

import common.Player;

public class GameInfo {
    public int gameId;
    public int remainedTurns = 9;
    public String[][] board = new String[3][3];
    public Player x, o;
    public boolean isTimeout = false;
    public volatile boolean isResumed;


    public GameInfo(int gameId, Player x, Player o) {
        this.gameId = gameId;
        this.x = x;
        this.o = o;
        initializeBoard();
    }

    private void initializeBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = "";
            }
        }
    }

    public boolean updateBoard(String chess, String posString) {
        int pos = Integer.parseInt(posString) - 1;
        int row = pos / 3;
        int col = pos % 3;
        board[row][col] = chess;
        remainedTurns--;
        return checkGameOver();
    }

    public boolean checkGameOver() {
        if (remainedTurns == 0) return true; // draw

        // Check rows and columns
        for (int i = 0; i < 3; i++) {
            if (!board[i][0].isEmpty() && board[i][0].equals(board[i][1]) && board[i][0].equals(board[i][2])) {
                return true;
            }
            if (!board[0][i].isEmpty() && board[0][i].equals(board[1][i]) && board[0][i].equals(board[2][i])) {
                return true;
            }
        }

        // Check diagonals
        if (!board[0][0].isEmpty() && board[0][0].equals(board[1][1]) && board[0][0].equals(board[2][2])) {
            return true;
        }
        if (!board[0][2].isEmpty() && board[0][2].equals(board[1][1]) && board[0][2].equals(board[2][0])) {
            return true;
        }

        return false;
    }

    public String getPos(String chess) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].equals(chess)) {
                    res.append(i * 3 + j + 1);
                }
            }
        }
        if (res.isEmpty()){
            res.append("-"); // no chess on the board
        }
        return res.toString();
    }
}
