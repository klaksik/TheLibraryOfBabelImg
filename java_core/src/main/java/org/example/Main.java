package org.example;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import javax.imageio.ImageIO;
import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.common.hash.Hashing;

import java.io.*;

import static org.apache.commons.io.IOUtils.length;

public class Main{
    static int width = 512;
    static int height = 512;
    static String[] bot_tokens = new String[]{};
    static String ChatID = "-1001966701084";
    static String post_link = "https://t.me/TheLibraryofBabelImg";
    static String temp_file_path = "img.png";

    public static void main(String[] args) {

        int current_bot_index = 0;
        while (true) {

            int[][] collors = new int[width][height];
            int nohash = 0;


            Random random = new Random();
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    collors[i][j] = random.nextInt(16777216) + 1;
                }
            }
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    int color = collors[h][w];
                    nohash = nohash + color;
                    int red = (color / 65536) % 256;
                    int green = (color / 256) % 256;
                    int blue = color % 256;
                    int rgb = (red << 16) | (green << 8) | blue;
                    image.setRGB(w, h, rgb);
                }
            }
            File outputFile = new File(temp_file_path);
            try {
                ImageIO.write(image, "png", outputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String current_bot_token = bot_tokens[current_bot_index];

            String hashed = Hashing.sha256().hashString(String.valueOf(nohash), StandardCharsets.UTF_8).toString();
            System.out.println("hash " + hashed);

            try {
                URL url = new URL("https://api.telegram.org/bot" + current_bot_token + "/getChatHistory");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                String parameters = "chat_id=" + ChatID + "&text=" + hashed;
                byte[] postData = parameters.getBytes("UTF-8");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length", String.valueOf(postData.length));
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(postData);
                int responseCode = connection.getResponseCode();
                if (responseCode != 404) {
                    break;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                URL url = new URL("https://api.telegram.org/bot" + current_bot_token + "/sendPhoto");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setChunkedStreamingMode(0);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
                String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
                String CRLF = "\r\n";
                String postData = "--" + boundary + CRLF
                        + "Content-Disposition: form-data; name=\"chat_id\"" + CRLF + CRLF
                        + ChatID + CRLF
                        + "--" + boundary + CRLF
                        + "Content-Disposition: form-data; name=\"photo\"; filename=\"" + temp_file_path + "\"" + CRLF
                        + "Content-Type: png" + CRLF + CRLF;
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(postData.getBytes());
                File imageFile = new File(temp_file_path);
                FileInputStream fileInputStream = new FileInputStream(imageFile);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.write((CRLF + "--" + boundary + CRLF).getBytes());
                outputStream.write(("Content-Disposition: form-data; name=\"caption\"" + CRLF + CRLF + hashed + " " + post_link + CRLF).getBytes());
                outputStream.write((CRLF + "--" + boundary + "--" + CRLF).getBytes());
                outputStream.flush();
                outputStream.close();
                int responseCode = connection.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                connection.disconnect();
                if (responseCode == 200){
                    System.out.println("Image sent successfully using Bot " + current_bot_index);
                }
                current_bot_index = (current_bot_index + 1) % length(bot_tokens);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}