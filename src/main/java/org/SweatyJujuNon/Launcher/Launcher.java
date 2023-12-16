package org.SweatyJujuNon.Launcher;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.sun.net.httpserver.HttpServer;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.SweatyJujuNon.Launcher.Utils.CLIENT_ID;
import static org.SweatyJujuNon.Launcher.Utils.REDIRECT_URL;

public class Launcher {

    public static Gson gson = new Gson();
    public static boolean mcRunning = false;

    public static LinkedTreeMap<String,LinkedTreeMap<String,LinkedTreeMap>> profiles;

    static {
        File profilesFile = new File("profiles.json");
        if (!profilesFile.exists()) {
            try {
                profilesFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            profiles = (LinkedTreeMap<String,LinkedTreeMap<String,LinkedTreeMap>>) gson.fromJson(new FileReader(profilesFile), LinkedTreeMap.class);
            if (profiles == null) {
                profiles = new LinkedTreeMap<>();
                profiles.put("profiles",new LinkedTreeMap<>());
                try (FileOutputStream fos = new FileOutputStream("profiles.json");
                     OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                    osw.write(gson.toJson(profiles));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void launch(String account) throws Exception {
        File profilesFile = new File("profiles.json");
        if (!profilesFile.exists()) {
            profilesFile.createNewFile();
        }
        try {
            profiles = (LinkedTreeMap<String, LinkedTreeMap<String, LinkedTreeMap>>) gson.fromJson(new FileReader(profilesFile), LinkedTreeMap.class);
            if (profiles == null) {
                profiles = new LinkedTreeMap<>();
                profiles.put("profiles",new LinkedTreeMap<>());
                try (FileOutputStream fos = new FileOutputStream("profiles.json");
                     OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                    osw.write(gson.toJson(profiles));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        mcRunning = true;
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
            if (((LinkedTreeMap) version).get("id").toString().equals("1.8.9")) {
                v = ((LinkedTreeMap) version).get("id").toString();
                url = new URL(((String) ((LinkedTreeMap) version).get("url")));
            }
            else if (((LinkedTreeMap) version).get("id").toString().equals("1.12.2")) {
                v = ((LinkedTreeMap) version).get("id").toString();
                url = new URL(((String) ((LinkedTreeMap) version).get("url")));
            }
            else if (((LinkedTreeMap) version).get("id").toString().equals("1.16.5")) {
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
            System.out.println("Checking assets:" + assets.size());
            for (Map.Entry<String, LinkedTreeMap> f : assets.entrySet()) {
                Thread thread = new Thread(() -> {
                    LinkedTreeMap asset = f.getValue();
                    if (!new File(".minecraft/assets/objects/" + asset.get("hash").toString().substring(0, 2) + "/" + asset.get("hash").toString()).exists()) {
                        File folder = new File(".minecraft/assets/objects/" + asset.get("hash").toString().substring(0, 2));
                        folder.mkdirs();

                        try {
                            URL url1 = new URL("https://resources.download.minecraft.net/" + asset.get("hash").toString().substring(0, 2) + "/" + asset.get("hash").toString());
                            URLConnection connection1 = url1.openConnection();
                            InputStream inputStream = connection1.getInputStream();


                            FileOutputStream outputStream = new FileOutputStream(".minecraft/assets/objects/" + asset.get("hash").toString().substring(0, 2) + "/" + asset.get("hash").toString());


                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }


                            inputStream.close();
                            outputStream.close();
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    }
                });

                threads.add(thread);
                thread.start();

            }
            for (Thread t : threads) {
                t.join();
            }
        }



        System.out.println("Downloading Libraries");
        StringBuilder cp = new StringBuilder();
        ArrayList<LinkedTreeMap> libraries = (ArrayList<LinkedTreeMap>) gson.fromJson(response.toString(), LinkedTreeMap.class).get("libraries");
        for (LinkedTreeMap library : libraries) {
            {
                String u = "";
                String path = "";
                try {
                    path = ((LinkedTreeMap) ((LinkedTreeMap) ((LinkedTreeMap) library
                            .get("downloads"))
                            .get("classifiers"))
                            .get("natives-windows-64"))
                            .get("path").toString();
                    u = ((LinkedTreeMap) ((LinkedTreeMap) ((LinkedTreeMap) library
                            .get("downloads"))
                            .get("classifiers"))
                            .get("natives-windows-64"))
                            .get("url").toString();
                } catch (NullPointerException e1) {
                    try {
                        path = ((LinkedTreeMap) ((LinkedTreeMap) library
                                .get("downloads"))
                                .get("artifact"))
                                .get("path").toString();
                        u = ((LinkedTreeMap) ((LinkedTreeMap) library
                                .get("downloads"))
                                .get("artifact"))
                                .get("url").toString();
                    } catch (NullPointerException e2) {
                        path = ((LinkedTreeMap) ((LinkedTreeMap) ((LinkedTreeMap) library
                                .get("downloads"))
                                .get("classifiers"))
                                .get("natives-windows"))
                                .get("path").toString();
                        u = ((LinkedTreeMap) ((LinkedTreeMap) ((LinkedTreeMap) library
                                .get("downloads"))
                                .get("classifiers"))
                                .get("natives-windows"))
                                .get("url").toString();
                    }
                }
                String[] p = path.split("/");
                path = "";
                for (int i = 0; i < p.length - 1; i++) {
                    path = path + p[i] + "/";
                }
                File lib = new File(".minecraft/libraries/" + path + p[p.length - 1]);
                cp.append(lib.getAbsolutePath()).append(";");
                if (!lib.exists()) {
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
                }
            }

            {
                String u = "";
                String path = "";
                try {
                    path = ((LinkedTreeMap) ((LinkedTreeMap) ((LinkedTreeMap) library
                            .get("downloads"))
                            .get("classifiers"))
                            .get("natives-windows"))
                            .get("path").toString();
                    u = ((LinkedTreeMap) ((LinkedTreeMap) ((LinkedTreeMap) library
                            .get("downloads"))
                            .get("classifiers"))
                            .get("natives-windows"))
                            .get("url").toString();
                } catch (NullPointerException ignored) {

                }

                String[] p = path.split("/");
                path = "";
                for (int i = 0; i < p.length - 1; i++) {
                    path = path + p[i] + "/";
                }
                File lib = new File(".minecraft/libraries/" + path + p[p.length - 1]);
                cp.append(lib.getAbsolutePath()).append(";");
                if (!lib.exists()) {
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
                }
            }
        }

        System.out.println("Downloading Client");
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
        System.out.println("Downloading Asset Index");
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
        String outputDir = ".minecraft/versions/" + v + "/natives";
        deleteDirectory(new File(outputDir));

        if (v != "1.16.5") {
            try (ZipInputStream zipInputStream = new ZipInputStream(new URL("https://github.com/Baller-Studios/LauncherFiles/raw/main/natives.zip").openConnection().getInputStream())) {
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
        }
        reader.close();
        connection.disconnect();

        String name;
        String uuid;
        String token;

        System.out.println("Signing In");
        if (!profiles.get("profiles").containsKey(account)) {
            String[] data = Utils.getSecureLoginData(CLIENT_ID, REDIRECT_URL, null);
            String login_url = data[0];
            String state = data[1];
            String codeVerifier = data[2];

            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/", new AuthCodeHandler(codeVerifier));
            server.start();

            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(login_url));

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

            name = profile.get("name").toString();
            uuid = profile.get("id").toString();
            token = accessToken;

            profiles.get("profiles").putIfAbsent(profile.get("name").toString(), profileMap);

            try (FileOutputStream fos = new FileOutputStream("profiles.json");
                 OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                osw.write(gson.toJson(profiles));
            } catch (IOException e) {
                e.printStackTrace();
            }

            server.stop(0);
        } else {
            name = profiles.get("profiles").get(account).get("name").toString();
            uuid = profiles.get("profiles").get(account).get("uuid").toString();
            token = profiles.get("profiles").get(account).get("token").toString();
        }

        System.out.println("Launching Game");
        List<String> launchCommand = new ArrayList<>();
        if (Objects.equals(v, "1.8.9")) {
            launchCommand = Arrays.asList("java", "-Djava.library.path=$path/.minecraft/versions/$version/natives", "-cp", cp +"$path/.minecraft/versions/$version/$version.jar", "net.minecraft.client.main.Main", "--username", name, "--version", "$version", "--gameDir", "$path/.minecraft", "--assetsDir", "$path/.minecraft/assets", "--assetIndex", "1.8", "--uuid", uuid, "--accessToken",token, "--userProperties", "{}", "--userType", "mojang");
            for (int i = 0; i < launchCommand.size(); i++) {
                String cmd = launchCommand.get(i);
                launchCommand.set(i, cmd.replace("$path", System.getProperty("user.dir")).replace("$version",v));
            }
        } else if (Objects.equals(v,"1.16.5")) {
            launchCommand = Arrays.asList("java","-Dminecraft.launcher.brand=MCLauncher","-Dos.name=Windows 10","-Dos.version=10.0","-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump","-Dminecraft.launcher.version=0.1",  "-cp", cp +"$path/.minecraft/versions/$version/$version.jar", "net.minecraft.client.main.Main", "--username", name, "--version", "$version", "--gameDir", "$path/.minecraft", "--assetsDir", "$path/.minecraft/assets", "--assetIndex", "1.16.5", "--uuid", uuid, "--accessToken",token, "--userProperties", "{}", "--userType", "mojang");
            for (int i = 0; i < launchCommand.size(); i++) {
                String cmd = launchCommand.get(i);
                launchCommand.set(i, cmd.replace("$path", System.getProperty("user.dir")).replace("$version",v));
            }
        } else if (Objects.equals(v,"1.12.2")) {
            launchCommand = Arrays.asList("java","-Dminecraft.launcher.brand=MCLauncher","-Dos.name=Windows 10","-Dos.version=10.0","-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump","-Dminecraft.launcher.version=0.1",  "-cp", cp +"$path/.minecraft/versions/$version/$version.jar", "net.minecraft.client.main.Main", "--username", name, "--version", "$version", "--gameDir", "$path/.minecraft", "--assetsDir", "$path/.minecraft/assets", "--assetIndex", "1.12.2", "--uuid", uuid, "--accessToken",token, "--userProperties", "{}", "--userType", "mojang");
            for (int i = 0; i < launchCommand.size(); i++) {
                String cmd = launchCommand.get(i);
                launchCommand.set(i, cmd.replace("$path", System.getProperty("user.dir")).replace("$version",v));
            }
        }


        String b = "";
        for (String s : launchCommand) {
            b = b + s + " ";
        }
        System.out.println(b);

        ProcessBuilder builder = new ProcessBuilder(launchCommand);
        Process process = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String l;
        while ((l = r.readLine()) != null) {
            System.out.println(l);
        }
        mcRunning = false;
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
