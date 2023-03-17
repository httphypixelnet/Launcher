package org.SweatyJujuNon.Launcher;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonReader;
import com.sun.net.httpserver.HttpServer;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.SweatyJujuNon.Launcher.Utils.*;

public class ServerHandler {
    static Gson gson = new Gson();
    public static void main(String[] args) throws Exception {
        String[] l = Utils.getSecureLoginData(CLIENT_ID, REDIRECT_URL, null);
        String login_url = l[0];
        String state = l[1];
        String codeVerifier = l[2];

        System.out.println(login_url);

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new AuthCodeHandler(codeVerifier));
        server.start();

        while (AuthCodeHandler.getAuthCode() == null) {
            Thread.sleep(100);
        }

        String authCode = AuthCodeHandler.getAuthCode();
        server.stop(0);

        String token = Utils.getAuthorizationToken(CLIENT_ID, null, REDIRECT_URL, authCode, codeVerifier);

        LinkedTreeMap xblRequest = Utils.authenticateWithXBL(token);
        String xblToken = xblRequest.get("Token").toString();
        String userhash = ((LinkedTreeMap)((ArrayList)((LinkedTreeMap)xblRequest.get("DisplayClaims")).get("xui")).get(0)).get("uhs").toString();

        String xstsToken = Utils.authenticateWithXSTS(xblToken);

        String accessToken = Utils.authenticateWithMinecraft(userhash,xstsToken).get("access_token").toString();

        JSONObject profile = Utils.getMinecraftProfile(accessToken);
        System.out.println(profile);
    }

}
