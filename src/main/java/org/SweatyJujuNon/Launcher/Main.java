package org.SweatyJujuNon.Launcher;

import com.sun.net.httpserver.HttpServer;

import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.util.Random;

public class Main {
    private static final Random random = new Random();
    public static final int Number = 1;
    public static void main(String[] args) throws Exception {
        if (Number == 1) {
            int port = random.nextInt(8001,65535);
            while (isPortInUse(port)) {
                port = random.nextInt(8001,65535);
            }

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);


            server.createContext("/", new LauncherHandler.FileHandler());

            server.setExecutor(null);
            server.start();

            SocketTesting socketTesting = new SocketTesting();
            socketTesting.start();
            Desktop.getDesktop().browse(URI.create("http://localhost:"+port));
        }
    }

    public static boolean isPortInUse(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

}