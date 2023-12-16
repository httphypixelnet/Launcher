package org.httphypixelnet.Launcher;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class AuthCodeHandler implements HttpHandler {
    private static String authCode = null;
    private String codeVerifier;

    public AuthCodeHandler(String codeVerifier) {
        this.codeVerifier = codeVerifier;
        authCode = null;
    }

    public static String getAuthCode() {
        return authCode;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equalsIgnoreCase("GET")) {
            // Extract request parameters
            URI uri = exchange.getRequestURI();
            String query = uri.getRawQuery();
            Map<String, String> params = parseQuery(query);

            if (params.containsKey("code")) {
                // Store auth code and close the response
                authCode = params.get("code");
                String response = "Authorization code received. You may now close this tab.";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    // Parse query string into key-value pairs
    private Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                try {
                    result.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                            URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    // Should never happen
                }
            }
        }
        return result;
    }
}
