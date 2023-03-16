package org.SweatyJujuNon.Launcher;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

public class Utils {

    public static final String TOKEN_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    public static final String AUTH_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize/";
    public static final String SCOPE = "XboxLive.signin offline_access";
    public static final String CLIENT_ID = "41bf7724-369e-4223-996c-88e62ce3d151";
    public static final String REDIRECT_URL = "http://localhost:8000/auth-response";


    public static String[] getSecureLoginData(String clientId, String redirectUri, String state) throws NoSuchAlgorithmException {
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        String codeChallengeMethod = "S256";

        if (state == null) {
            state = generateRandomState();
        }

        URI url = URI.create(AUTH_URL).resolve("?client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_mode=query"
                + "&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8)
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8)
                + "&code_challenge=" + URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8)
                + "&code_challenge_method=" + URLEncoder.encode(codeChallengeMethod, StandardCharsets.UTF_8));
        String[] r = new String[3];
        r[0] = url.toString();
        r[1] = state;
        r[2] = codeVerifier;
        return r;
    }

    private static String generateRandomState() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String generateCodeVerifier() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String getAuthorizationToken(String clientId, String clientSecret, String redirectUri, String authCode, String codeVerifier) throws IOException, ParseException {
        String parameters = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode("XboxLive.signin offline_access", StandardCharsets.UTF_8)
                + "&code=" + URLEncoder.encode(authCode, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&grant_type=authorization_code";

        if (clientSecret != null) {
            parameters += "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8);
        }

        if (codeVerifier != null) {
            parameters += "&code_verifier=" + URLEncoder.encode(codeVerifier, StandardCharsets.UTF_8);
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(TOKEN_URL);

        httpPost.setHeader("Content-Type","application/x-www-form-urlencoded");
        httpPost.setHeader("user-agent", "minecraft-launcher-lib/5.2");
        httpPost.setHeader("Origin", "https://localhost");
        StringEntity params = new StringEntity(parameters);
        httpPost.setEntity(params);
        CloseableHttpResponse response = httpClient.execute(httpPost);

        return EntityUtils.toString(response.getEntity());
    }
}
