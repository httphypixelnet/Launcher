package org.httphypixelnet.Launcher;

import com.google.gson.internal.LinkedTreeMap;
import com.sun.net.httpserver.HttpServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.httphypixelnet.Launcher.Launcher.*;
import static org.httphypixelnet.Launcher.Utils.CLIENT_ID;
import static org.httphypixelnet.Launcher.Utils.REDIRECT_URL;

public class SocketTesting extends WebSocketServer {

    private static final int TCP_PORT = 4444;

    public static Set<WebSocket> conns;

    public SocketTesting() {
        super(new InetSocketAddress(TCP_PORT));
        conns = new HashSet<>();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conns.add(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        conns.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        new Thread(() -> {
            if (message.startsWith("launchGame")) {
                conn.send("Launching Game!");
                try {
                    if (!Launcher.mcRunning) {
                        try {
                            Launcher.launch(message.replace("launchGame=", ""));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (message.startsWith("getAccountList")) {
                conn.send(String.format("accounts=%s", gson.toJson(profiles.get("profiles").keySet())));
            } else if (message.startsWith("signIn")) {
                try {
                    String[] data = Utils.getSecureLoginData(CLIENT_ID, REDIRECT_URL, null);
                    String login_url = data[0];
                    String state = data[1];
                    String codeVerifier = data[2];

                    Desktop desktop = Desktop.getDesktop();
                    desktop.browse(new URI(login_url));


                    HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
                    server.createContext("/", new AuthCodeHandler(codeVerifier));
                    server.start();

                    while (AuthCodeHandler.getAuthCode() == null) {
                        Thread.sleep(100);
                    }

                    String authCode = AuthCodeHandler.getAuthCode();
                    server.stop(0);

                    String auth_token = Utils.getAuthorizationToken(CLIENT_ID, null, REDIRECT_URL, authCode, codeVerifier);

                    LinkedTreeMap xblRequest = Utils.authenticateWithXBL(auth_token);
                    String xblToken = xblRequest.get("Token").toString();
                    String userhash = ((LinkedTreeMap) ((ArrayList) ((LinkedTreeMap) xblRequest.get("DisplayClaims")).get("xui")).get(0)).get("uhs").toString();

                    String xstsToken = Utils.authenticateWithXSTS(xblToken);

                    String accessToken = Utils.authenticateWithMinecraft(userhash, xstsToken).get("access_token").toString();

                    JSONObject profile = Utils.getMinecraftProfile(accessToken);


                    profiles.putIfAbsent("profiles", new LinkedTreeMap<>());
                    LinkedTreeMap<String, Object> profileMap = new LinkedTreeMap<>();
                    profileMap.put("name", profile.get("name").toString());
                    profileMap.put("uuid", profile.get("id").toString());
                    profileMap.put("token", accessToken);
                    profileMap.put("refreshToken", "");

                    profiles.get("profiles").putIfAbsent(profile.get("name").toString(), profileMap);

                    try (FileOutputStream fos = new FileOutputStream("profiles.json");
                         OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                        osw.write(gson.toJson(profiles));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    server.stop(0);

                    conn.send("signIn=" + profile.get("name").toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (message.startsWith("signOut")) {
                profiles.get("profiles").remove(message.replace("signOut=", ""));
                try (FileOutputStream fos = new FileOutputStream("profiles.json");
                     OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                    osw.write(gson.toJson(profiles));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }/* else if (message.startsWith("selectedAccount=")) {
                for (Map.Entry<String, LinkedTreeMap> profile : profiles.get("profiles").entrySet()) {
                    if (profile.getValue().get("name") == message.replace("selectedAccount=","")) {
                        profile.getValue().put("selected",true);
                    } else {
                        profile.getValue().put("selected",false);
                    }
                }
                try (FileOutputStream fos = new FileOutputStream("profiles.json");
                     OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                    osw.write(gson.toJson(profiles));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
        }).start();
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        if (conn != null) {
            conns.remove(conn);
        }
    }

    @Override
    public void onStart() {

    }
}