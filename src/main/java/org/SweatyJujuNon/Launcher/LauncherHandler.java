package org.SweatyJujuNon.Launcher;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class LauncherHandler {

    static class FileHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String path = t.getRequestURI().getPath().substring(1);
            if (path.isEmpty()) {
                path = "index.html";
            }

            InputStream stream = LauncherHandler.class.getResourceAsStream("/web/"+path);
            String fileExtension = path.split("\\.")[1];
            if (stream == null) {
                String response = "404 Not Found";
                t.sendResponseHeaders(404, response.length());
                t.getResponseBody().write(response.getBytes());
                t.getResponseBody().close();
                return;
            }

            byte[] bytes = stream.readAllBytes();


            if (Objects.equals(fileExtension, "png")) {
                t.getResponseHeaders().set("Content-Type", "image/png");
            } else if (Objects.equals(fileExtension, "jpg")) {
                t.getResponseHeaders().set("Content-Type", "image/jpeg");
            } else if (Objects.equals(fileExtension, "svg")) {
                t.getResponseHeaders().set("Content-Type", "image/svg+xml");
            } else if (Objects.equals(fileExtension, "html")) {
                t.getResponseHeaders().set("Content-Type", "text/html");
            } else if (Objects.equals(fileExtension, "css")) {
                t.getResponseHeaders().set("Content-Type", "text/css");
            } else if (Objects.equals(fileExtension, "javascript")) {
                t.getResponseHeaders().set("Content-Type", "application/javascript");
            }
            t.sendResponseHeaders(200, bytes.length);
            t.getResponseBody().write(bytes);
            t.getResponseBody().close();
        }
    }
}
