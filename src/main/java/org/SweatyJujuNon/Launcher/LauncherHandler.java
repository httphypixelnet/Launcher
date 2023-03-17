package org.SweatyJujuNon.Launcher;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LauncherHandler {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Serve files from "web" folder
        server.createContext("/", new FileHandler("web"));

        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class FileHandler implements HttpHandler {
        private String root;

        public FileHandler(String root) {
            this.root = root;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            String path = t.getRequestURI().getPath().substring(1);
            if (path.isEmpty()) {
                path = "index.html";
            }

            File file = new File(root, path);
            if (!file.exists()) {
                String response = "404 Not Found";
                t.sendResponseHeaders(404, response.length());
                t.getResponseBody().write(response.getBytes());
                t.getResponseBody().close();
                return;
            }

            byte[] bytes = Files.readAllBytes(file.toPath());

            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            t.getResponseHeaders().set("Content-Type", mimeType);
            t.sendResponseHeaders(200, bytes.length);
            t.getResponseBody().write(bytes);
            t.getResponseBody().close();
        }
    }
}
