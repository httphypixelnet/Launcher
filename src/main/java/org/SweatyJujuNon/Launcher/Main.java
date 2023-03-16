package org.SweatyJujuNon.Launcher;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class Main {

    public static final Gson gson = new Gson();

    public static void main(String[] args) throws NullPointerException,IOException {
        URL url = new URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        ArrayList versions = ((ArrayList)gson.fromJson(response.toString(), LinkedTreeMap.class).get("versions"));
        url = null;
        String v = "";
        for (Object version : versions) {
            if (((LinkedTreeMap) version).get("id").toString().startsWith("1.8.9")) {
                v = ((LinkedTreeMap) version).get("id").toString();
                url = new URL((String) ((LinkedTreeMap) version).get("url"));
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
        String clientJarUrl = ((LinkedTreeMap)((LinkedTreeMap)gson.fromJson(response.toString(), LinkedTreeMap.class).get("downloads")).get("client")).get("url").toString();

        String assetsUrl = ((LinkedTreeMap)gson.fromJson(response.toString(), LinkedTreeMap.class).get("assetIndex")).get("url").toString();
        {
            URL a = new URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");
            HttpURLConnection b = (HttpURLConnection) a.openConnection();
            b.setRequestMethod("GET");
            BufferedReader c = new BufferedReader(new InputStreamReader(b.getInputStream()));
            StringBuilder d = new StringBuilder();
            String e;
            while ((e = c.readLine()) != null) {
                d.append(e);
            }

            LinkedTreeMap<String,LinkedTreeMap> assets = (LinkedTreeMap) gson.fromJson(d.toString(), LinkedTreeMap.class).get("objects");
            for (Map.Entry<String,LinkedTreeMap> f : assets.entrySet()) {
                LinkedTreeMap asset = f.getValue();
                File folder = new File(".minecraft/assets/objects/"+asset.get("hash").toString().substring(0,2));
                folder.mkdirs();

                try {
                    URL url1 = new URL("https://resources.download.minecraft.net/"+asset.get("hash").toString().substring(0,2)+"/"+asset.get("hash").toString());
                    URLConnection connection1 = url1.openConnection();
                    InputStream inputStream = connection1.getInputStream();

                    // Open a FileOutputStream to save the file
                    FileOutputStream outputStream = new FileOutputStream(".minecraft/assets/objects/"+asset.get("hash").toString().substring(0,2)+"/"+asset.get("hash").toString());

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
            }
        }




        ArrayList<LinkedTreeMap> libraries = (ArrayList<LinkedTreeMap>) gson.fromJson(response.toString(), LinkedTreeMap.class).get("libraries");
        for (LinkedTreeMap library: libraries) {
            String u = "";
            String path = "";
            try {
                path = ((LinkedTreeMap)((LinkedTreeMap)((LinkedTreeMap)((LinkedTreeMap)library
                        .get("downloads"))
                        .get("classifiers"))
                        .get("natives-windows-64")))
                        .get("path").toString();
                u = ((LinkedTreeMap)((LinkedTreeMap)((LinkedTreeMap)((LinkedTreeMap)library
                        .get("downloads"))
                        .get("classifiers"))
                        .get("natives-windows-64")))
                        .get("url").toString();
            } catch (NullPointerException e1) {
                try {
                    path = ((LinkedTreeMap)((LinkedTreeMap)((LinkedTreeMap)library
                            .get("downloads"))
                            .get("artifact")))
                            .get("path").toString();
                    u = ((LinkedTreeMap)((LinkedTreeMap)((LinkedTreeMap)library
                            .get("downloads"))
                            .get("artifact")))
                            .get("url").toString();
                } catch (NullPointerException e2) {
                    path = ((LinkedTreeMap)((LinkedTreeMap)((LinkedTreeMap)((LinkedTreeMap)library
                            .get("downloads"))
                            .get("classifiers"))
                            .get("natives-windows")))
                            .get("path").toString();
                    u = ((LinkedTreeMap)((LinkedTreeMap)((LinkedTreeMap)((LinkedTreeMap)library
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
            File file = new File(".minecraft/libraries/"+path);
            if (!file.exists()) {
                boolean success = file.mkdirs();
                if (!success) {
                    System.out.println("Failed to create directories");
                }
            }

            try {
                URL a = new URL(u);
                URLConnection b = a.openConnection();
                InputStream inputStream = b.getInputStream();

                // Open a FileOutputStream to save the file
                FileOutputStream outputStream = new FileOutputStream(".minecraft/libraries/"+path + p[p.length-1]);

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
        File folder = new File(".minecraft/version/"+v+"/");
        folder.mkdirs();
        try {
            URL a = new URL(clientJarUrl);
            URLConnection b = a.openConnection();
            InputStream inputStream = b.getInputStream();

            // Open a FileOutputStream to save the file
            FileOutputStream outputStream = new FileOutputStream(".minecraft/version/"+v+"/" + v + ".jar");

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
        reader.close();
        connection.disconnect();
    }
}