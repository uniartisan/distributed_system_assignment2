package server;

import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static String ip = "localhost"; // no need to config unless using cloud service like AWS
    public static int port = 8888;

    public static void main(String[] args) {
//        if (args.length != 2) {
//            System.out.println("args input err, start with default port: " + port);
//        } else {
//            ip = args[0];
//            port = Integer.parseInt(args[1]);
//        }
        System.out.println("Server started at " + ip + ":" + port);
        try {
            new Server(); // This line seems unnecessary or is missing context
            ServerSocket serverSocket = new ServerSocket(port);
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

