package server;

import common.Player;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    public static final AtomicInteger idGenerator = new AtomicInteger(0);
    public static final ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<>();
    public static final CopyOnWriteArrayList<Player> waitingPool = new CopyOnWriteArrayList<>();
    public static final ConcurrentHashMap<Integer, GameInfo> games = new ConcurrentHashMap<>();

    public static int generateGameId() {
        return idGenerator.incrementAndGet();
    }

    public static ConcurrentHashMap<String, Player> getPlayers() {
        return players;
    }

    public static CopyOnWriteArrayList<Player> getWaitingPool() {
        return waitingPool;
    }

    public static ConcurrentHashMap<Integer, GameInfo> getGames() {
        return games;
    }
}
