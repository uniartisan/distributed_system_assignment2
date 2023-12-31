/**
 * Project: Distributed Tic-Tac-Toe
 * Author: Zhiyuan Li
 * Student ID: 1453610
 * Date: 2023.10.12
 */

package server;

import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static String ip = "localhost"; // no need to config unless using cloud service like AWS
    public static int port = 8888;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("args input err, start with default port: " + port);
            System.exit(0);
        } else {
            ip = args[0];
            port = Integer.parseInt(args[1]);
        }
        System.out.println("Server started at " + ip + ":" + port);
        // Using try-with-resources for automatic resource management
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                Connection connection = new Connection(socket);
                connection.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
