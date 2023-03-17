package org.SweatyJujuNon.Launcher;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
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
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Utils {

    public static final String TOKEN_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    public static final String AUTH_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize/";
    public static final String SCOPE = "XboxLive.signin offline_access";
    public static final String CLIENT_ID = "41bf7724-369e-4223-996c-88e62ce3d151";
    public static final String REDIRECT_URL = "http://localhost:8000/auth-response";
    public static final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3";
    public static final Gson gson = new Gson();


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
        httpPost.setHeader("user-agent", userAgent);
        httpPost.setHeader("Origin", "https://localhost:8000");
        StringEntity params = new StringEntity(parameters);
        httpPost.setEntity(params);
        CloseableHttpResponse response = httpClient.execute(httpPost);

        return gson.fromJson(EntityUtils.toString(response.getEntity()), LinkedTreeMap.class).get("access_token").toString();
    }

    public static LinkedTreeMap authenticateWithXBL(String accessToken) throws IOException, ParseException {
        String request_body = String.format("{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"d=%s\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}", accessToken);

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost("https://user.auth.xboxlive.com/user/authenticate");
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("user-agent", userAgent);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setEntity(new StringEntity(request_body));

        CloseableHttpResponse response = httpClient.execute(httpPost);

        return gson.fromJson(EntityUtils.toString(response.getEntity()), LinkedTreeMap.class);
    }

    public static String authenticateWithXSTS(String xblToken) throws Exception {
        JSONObject properties = new JSONObject();
        properties.put("SandboxId", "RETAIL");
        properties.put("UserTokens", new String[]{xblToken});

        JSONObject parameters = new JSONObject();
        parameters.put("Properties", properties);
        parameters.put("RelyingParty", "rp://api.minecraftservices.com/");
        parameters.put("TokenType", "JWT");

        String request_body = parameters.toString();

        // Set the HTTP request headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("user-agent", userAgent);
        headers.put("Accept", "application/json");

        // Send the HTTP POST request to the Xbox Live authentication server using Apache HttpClient
        String url = "https://xsts.auth.xboxlive.com/xsts/authorize";
        String response = sendPostRequest(url, request_body, headers);

        // Parse the JSON response
        JSONObject json_response = new JSONObject(response);
        return json_response.getString("Token");
    }

    public static JSONObject authenticateWithMinecraft(String userhash, String xsts_token) throws Exception {
        JSONObject parameters = new JSONObject();
        parameters.put("identityToken", String.format("XBL3.0 x=%s;%s", userhash, xsts_token));

        StringEntity requestEntity = new StringEntity(parameters.toString());;

        HttpPost httpPost = new HttpPost("https://api.minecraftservices.com/authentication/login_with_xbox");
        httpPost.setEntity(requestEntity);
        httpPost.setHeader("Content-Type","application/json");
        httpPost.setHeader("user-agent", userAgent);
        httpPost.setHeader("Accept", "application/json");

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {

            String response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            return new JSONObject(response);
        }
    }

    public static String sendPostRequest(String url, String payload, Map<String, String> headers) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(new URI(url));

        // Set the HTTP request method and headers
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpPost.setHeader(entry.getKey(), entry.getValue());
        }

        // Set the payload in the request body
        StringEntity entity = new StringEntity(payload);
        httpPost.setEntity(entity);

        // Send the request and get the response
        CloseableHttpResponse httpResponse = client.execute(httpPost);


        return EntityUtils.toString(httpResponse.getEntity());
    }

    public static JSONObject getMinecraftProfile(String access_token) throws Exception {
        HttpGet httpGet = new HttpGet("https://api.minecraftservices.com/minecraft/profile");
        httpGet.setHeader("Authorization", "Bearer " + access_token);
        httpGet.setHeader("user-agent", "minecraft-launcher-lib/5.2");
        httpGet.setHeader("Accept", "application/json");

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {

            String response = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            return new JSONObject(response);
        }
    }


}
