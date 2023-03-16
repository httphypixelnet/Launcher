package org.SweatyJujuNon.Launcher;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.stream.JsonReader;
import com.sun.net.httpserver.HttpServer;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.security.NoSuchAlgorithmException;

import static org.SweatyJujuNon.Launcher.Utils.CLIENT_ID;
import static org.SweatyJujuNon.Launcher.Utils.REDIRECT_URL;

public class ServerHandler {
    static Gson gson = new Gson();
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, ParseException, InterruptedException {
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
        System.out.println("Authorization code received is: " + authCode);

        LinkedTreeMap data = gson.fromJson(Utils.getAuthorizationToken(CLIENT_ID, null, REDIRECT_URL, authCode, codeVerifier), LinkedTreeMap.class);


        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("https://api.minecraftservices.com/minecraft/profile");
        httpGet.setHeader("Authorization","Bearer "+data.get("access_token"));
        CloseableHttpResponse response = httpClient.execute(httpGet);

        System.out.println(response);

        System.out.println(EntityUtils.toString(response.getEntity()));

        System.out.println(Utils.getAuthorizationToken(CLIENT_ID, null, REDIRECT_URL, authCode, codeVerifier));
        server.stop(0);

    }

}
