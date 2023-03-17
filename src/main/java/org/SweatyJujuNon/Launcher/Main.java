package org.SweatyJujuNon.Launcher;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.sun.net.httpserver.HttpServer;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ParseException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.SweatyJujuNon.Launcher.Utils.CLIENT_ID;
import static org.SweatyJujuNon.Launcher.Utils.REDIRECT_URL;

public class Main {

    public static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        ArrayList<Thread> threads = new ArrayList<>();
        URL url = new URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        String av = "";
        ArrayList versions = ((ArrayList) gson.fromJson(response.toString(), LinkedTreeMap.class).get("versions"));
        url = null;
        String v = "";
        for (Object version : versions) {
            if (((LinkedTreeMap) version).get("id").toString().startsWith("1.8.9")) {
                v = ((LinkedTreeMap) version).get("id").toString();
                url = new URL(((String) ((LinkedTreeMap) version).get("url")));
            }
        }
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        response = new StringBuilder();
        line = "";
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        try {
            URLConnection connection1 = url.openConnection();
            InputStream inputStream = connection1.getInputStream();

            File file = new File(".minecraft/versions/" + v);
            file.mkdirs();

            // Open a FileOutputStream to save the file
            FileOutputStream outputStream = new FileOutputStream(".minecraft/versions/" + v + "/" + v + ".json");

            // Read bytes from the input stream and write them to the output stream
            byte[] buffer = new byte[1024];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // Close the input and output streams
            inputStream.close();
            outputStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        String clientJarUrl = ((LinkedTreeMap) ((LinkedTreeMap) gson.fromJson(response.toString(), LinkedTreeMap.class).get("downloads")).get("client")).get("url").toString();
        av = gson.fromJson(response.toString(), LinkedTreeMap.class).get("assets").toString();
        String assetsUrl = ((LinkedTreeMap) gson.fromJson(response.toString(), LinkedTreeMap.class).get("assetIndex")).get("url").toString();
        {
            URL a = new URL(assetsUrl);
            HttpURLConnection b = (HttpURLConnection) a.openConnection();
            b.setRequestMethod("GET");
            BufferedReader c = new BufferedReader(new InputStreamReader(b.getInputStream()));
            StringBuilder d = new StringBuilder();
            String e;
            while ((e = c.readLine()) != null) {
                d.append(e);
            }


            LinkedTreeMap<String, LinkedTreeMap> assets = (LinkedTreeMap) gson.fromJson(d.toString(), LinkedTreeMap.class).get("objects");
            for (Map.Entry<String, LinkedTreeMap> f : assets.entrySet()) {
                Thread thread = new Thread(() -> {
                    LinkedTreeMap asset = f.getValue();
                    File folder = new File(".minecraft/assets/objects/" + asset.get("hash").toString().substring(0, 2));
                    folder.mkdirs();

                    try {
                        URL url1 = new URL("https://resources.download.minecraft.net/" + asset.get("hash").toString().substring(0, 2) + "/" + asset.get("hash").toString());
                        URLConnection connection1 = url1.openConnection();
                        InputStream inputStream = connection1.getInputStream();

                        // Open a FileOutputStream to save the file
                        FileOutputStream outputStream = new FileOutputStream(".minecraft/assets/objects/" + asset.get("hash").toString().substring(0, 2) + "/" + asset.get("hash").toString());

                        // Read bytes from the input stream and write them to the output stream
                        byte[] buffer = new byte[1024];
                        int bytesRead = -1;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        // Close the input and output streams
                        inputStream.close();
                        outputStream.close();
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                });

                threads.add(thread);
                thread.start();

            }
        }


        ArrayList<LinkedTreeMap> libraries = (ArrayList<LinkedTreeMap>) gson.fromJson(response.toString(), LinkedTreeMap.class).get("libraries");
        for (LinkedTreeMap library : libraries) {
            Thread thread = new Thread(() -> {
                String u = "";
                String path = "";
                try {
                    path = ((LinkedTreeMap) ((LinkedTreeMap) ((LinkedTreeMap) ((LinkedTreeMap) library
                            .get("downloads"))
                            .get("classifiers"))
                            .get("natives-windows-64")))
                            .get("path").toString();
                    u = ((LinkedTreeMap) ((LinkedTreeMap) ((LinkedTreeMap) ((LinkedTreeMap) library
                            .get("downloads"))
                            .get("classifiers"))
                            .get("natives-windows-64")))
                            .get("url").toString();
                } catch (NullPointerException e1) {
                    try {
                        path = ((LinkedTreeMap) ((LinkedTreeMap) ((LinkedTreeMap) library
                                .get("downloads"))
                                .get("artifact")))
                                .get("path").toString();
                        u = ((LinkedTreeMap) ((LinkedTreeMap) ((LinkedTreeMap) library
                                .get("downloads"))
                                .get("artifact")))
                                .get("url").toString();
                    } catch (NullPointerException e2) {
                        path = ((LinkedTreeMap) ((LinkedTreeMap) ((LinkedTreeMap) ((LinkedTreeMap) library
                                .get("downloads"))
                                .get("classifiers"))
                                .get("natives-windows")))
                                .get("path").toString();
                        u = ((LinkedTreeMap) ((LinkedTreeMap) ((LinkedTreeMap) ((LinkedTreeMap) library
                                .get("downloads"))
                                .get("classifiers"))
                                .get("natives-windows")))
                                .get("url").toString();
                    }
                }
                String[] p = path.split("/");
                path = "";
                for (int i = 0; i < p.length - 1; i++) {
                    path = path + p[i] + "/";
                }
                File folder = new File(".minecraft/libraries/" + path);
                folder.mkdirs();
                try {
                    URL a = new URL(u);
                    URLConnection b = a.openConnection();
                    InputStream inputStream = b.getInputStream();

                    // Open a FileOutputStream to save the file
                    FileOutputStream outputStream = new FileOutputStream(".minecraft/libraries/" + path + p[p.length - 1]);

                    // Read bytes from the input stream and write them to the output stream
                    byte[] buffer = new byte[1024];
                    int bytesRead = -1;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    // Close the input and output streams
                    inputStream.close();
                    outputStream.close();

                    System.out.println(library.get("name") + " was successfully downloaded!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            threads.add(thread);
            thread.start();


        }
        File folder = new File(".minecraft/versions/" + v + "/");
        folder.mkdirs();
        try {
            URL a = new URL(clientJarUrl);
            URLConnection b = a.openConnection();
            InputStream inputStream = b.getInputStream();

            // Open a FileOutputStream to save the file
            FileOutputStream outputStream = new FileOutputStream(".minecraft/versions/" + v + "/" + v + ".jar");

            // Read bytes from the input stream and write them to the output stream
            byte[] buffer = new byte[1024];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // Close the input and output streams
            inputStream.close();
            outputStream.close();

            System.out.println("Client Jar Downloaded Sucessfully");
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file = new File(".minecraft/assets/indexes");
        file.mkdirs();
        try {
            URL a = new URL(assetsUrl);
            URLConnection b = a.openConnection();
            InputStream inputStream = b.getInputStream();

            // Open a FileOutputStream to save the file
            FileOutputStream outputStream = new FileOutputStream(".minecraft/assets/indexes/" + av + ".json");

            // Read bytes from the input stream and write them to the output stream
            byte[] buffer = new byte[1024];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // Close the input and output streams
            inputStream.close();
            outputStream.close();

            System.out.println("Asset Index Downloaded Successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String zipFile = Main.class.getResource("/natives.zip").getPath();
        String outputDir = ".minecraft/versions/" + v + "/natives";
        deleteDirectory(new File(outputDir));

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();
                File entryFile = new File(outputDir, entryName);
                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    entryFile.getParentFile().mkdirs();
                    try (OutputStream outputStream = new FileOutputStream(entryFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        }
        for (Thread thread : threads) {
            thread.join();
        }
        reader.close();
        connection.disconnect();
        String[] data = Utils.getSecureLoginData(CLIENT_ID, REDIRECT_URL, null);
        String login_url = data[0];
        String state = data[1];
        String codeVerifier = data[2];

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

        server.stop(0);
        List<String> launchCommand = Arrays.asList("java", "-Djava.library.path=$path/.minecraft/versions/1.8.9/natives", "-cp", "$path/.minecraft/libraries/com/mojang/netty/1.8.8/netty-1.8.8.jar;$path/.minecraft/libraries/oshi-project/oshi-core/1.1/oshi-core-1.1.jar;$path/.minecraft/libraries/net/java/dev/jna/jna/3.4.0/jna-3.4.0.jar;$path/.minecraft/libraries/net/java/dev/jna/platform/3.4.0/platform-3.4.0.jar;$path/.minecraft/libraries/com/ibm/icu/icu4j-core-mojang/51.2/icu4j-core-mojang-51.2.jar;$path/.minecraft/libraries/net/sf/jopt-simple/jopt-simple/4.6/jopt-simple-4.6.jar;$path/.minecraft/libraries/com/paulscode/codecjorbis/20101023/codecjorbis-20101023.jar;$path/.minecraft/libraries/com/paulscode/codecwav/20101023/codecwav-20101023.jar;$path/.minecraft/libraries/com/paulscode/libraryjavasound/20101123/libraryjavasound-20101123.jar;$path/.minecraft/libraries/com/paulscode/librarylwjglopenal/20100824/librarylwjglopenal-20100824.jar;$path/.minecraft/libraries/com/paulscode/soundsystem/20120107/soundsystem-20120107.jar;$path/.minecraft/libraries/io/netty/netty-all/4.0.23.Final/netty-all-4.0.23.Final.jar;$path/.minecraft/libraries/com/google/guava/guava/17.0/guava-17.0.jar;$path/.minecraft/libraries/org/apache/commons/commons-lang3/3.3.2/commons-lang3-3.3.2.jar;$path/.minecraft/libraries/commons-io/commons-io/2.4/commons-io-2.4.jar;$path/.minecraft/libraries/commons-codec/commons-codec/1.9/commons-codec-1.9.jar;$path/.minecraft/libraries/net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar;$path/.minecraft/libraries/net/java/jutils/jutils/1.0.0/jutils-1.0.0.jar;$path/.minecraft/libraries/com/google/code/gson/gson/2.2.4/gson-2.2.4.jar;$path/.minecraft/libraries/com/mojang/authlib/1.5.21/authlib-1.5.21.jar;$path/.minecraft/libraries/com/mojang/realms/1.7.59/realms-1.7.59.jar;$path/.minecraft/libraries/org/apache/commons/commons-compress/1.8.1/commons-compress-1.8.1.jar;$path/.minecraft/libraries/org/apache/httpcomponents/httpclient/4.3.3/httpclient-4.3.3.jar;$path/.minecraft/libraries/commons-logging/commons-logging/1.1.3/commons-logging-1.1.3.jar;$path/.minecraft/libraries/org/apache/httpcomponents/httpcore/4.3.2/httpcore-4.3.2.jar;$path/.minecraft/libraries/org/apache/logging/log4j/log4j-api/2.0-beta9/log4j-api-2.0-beta9.jar;$path/.minecraft/libraries/org/apache/logging/log4j/log4j-core/2.0-beta9/log4j-core-2.0-beta9.jar;$path/.minecraft/libraries/org/lwjgl/lwjgl/lwjgl/2.9.4-nightly-20150209/lwjgl-2.9.4-nightly-20150209.jar;$path/.minecraft/libraries/org/lwjgl/lwjgl/lwjgl_util/2.9.4-nightly-20150209/lwjgl_util-2.9.4-nightly-20150209.jar;$path/.minecraft/libraries/org/lwjgl/lwjgl/lwjgl-platform/2.9.4-nightly-20150209/lwjgl-platform-2.9.4-nightly-20150209.jar;$path/.minecraft/libraries/org/lwjgl/lwjgl/lwjgl-platform/2.9.4-nightly-20150209/lwjgl-platform-2.9.4-nightly-20150209-natives-windows.jar;$path/.minecraft/libraries/net/java/jinput/jinput-platform/2.0.5/jinput-platform-2.0.5.jar;$path/.minecraft/libraries/net/java/jinput/jinput-platform/2.0.5/jinput-platform-2.0.5-natives-windows.jar;$path/.minecraft/libraries/tv/twitch/twitch/6.5/twitch-6.5.jar;$path/.minecraft/libraries/tv/twitch/twitch-platform/6.5/twitch-platform-6.5.jar;$path/.minecraft/libraries/tv/twitch/twitch-platform/6.5/twitch-platform-6.5-natives-windows-64.jar;$path/.minecraft/libraries/tv/twitch/twitch-external-platform/4.5/twitch-external-platform-4.5.jar;$path/.minecraft/libraries/tv/twitch/twitch-external-platform/4.5/twitch-external-platform-4.5-natives-windows-64.jar;$path/.minecraft/versions/1.8.9/1.8.9.jar", "net.minecraft.client.main.Main", "--username", profile.get("name").toString(), "--version", "1.8.9", "--gameDir", "$path/.minecraft", "--assetsDir", "$path/.minecraft/assets", "--assetIndex", "1.8", "--uuid", profile.get("id").toString(), "--accessToken",accessToken, "--userProperties", "{}", "--userType", "mojang");
        for (int i = 0; i < launchCommand.size(); i++) {
            String cmd = launchCommand.get(i);
            launchCommand.set(i, cmd.replace("$path", System.getProperty("user.dir")));
        }

        String b = "";
        for (int i = 0; i < launchCommand.size(); i++) {
            b = b + launchCommand.get(i) + " ";
        }
        System.out.println(b);
        ProcessBuilder builder = new ProcessBuilder(launchCommand);
        Process process = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String l;
        System.out.println(System.getProperty("user.dir"));
        while ((l = r.readLine()) != null) {
            System.out.println(l);
        }
    }

    public static void deleteDirectory(File directory) throws IOException {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}