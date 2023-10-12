/**
 * Project: Distributed Tic-Tac-Toe
 * Author: Zhiyuan Li
 * Student ID: 1453610
 * Date: 2023.10.12
 */

package common;

import java.io.BufferedWriter;

public class Player {
    public String name;
    public int rank;
    public BufferedWriter out;
    public int waitTimeout;

    public Player(String name, BufferedWriter out, int rank, int waitTimeout) {
        this.name = name;
        this.out = out;
        this.rank = rank;
        this.waitTimeout = waitTimeout;
    }

    public Player() {
    }
}
