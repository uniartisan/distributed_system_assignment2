package common;

import java.io.BufferedWriter;

public class Player {
    public String name;
    public int rank;
    public BufferedWriter out;
    public Player(String name, BufferedWriter out) {
        this.name = name;
        this.out = out;
        this.rank = 0;
    }
    public Player() {
    }
}

