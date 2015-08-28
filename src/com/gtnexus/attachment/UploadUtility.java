package com.gtnexus.attachment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UploadUtility {
    // Version of API
    final static String version = "310";
    // URL of Environment
    final static String baseURL = "https://commerce-demo.gtnexus.com/rest/" + version;
    // Your user's authorization token
    private static String authorization;
    // Your organization's data key
    private static String dataKey;

    public static void main(String[] args) {
        // Retrieve the authorization token, data key, order folder's uid
        // and attachment file name from arguments.
        authorization = args[0];
        dataKey = args[1];
        String orderUid = args[2];
        String filename = args[3];
        uploadAttachment(orderUid, filename);
    }

    private static void uploadAttachment(String orderUid, String filename)
    {
        try {
            String urlString = baseURL + "/OrderDetail/" + orderUid + "/attach?dataKey=" + dataKey;
            URL url = new URL(urlString);

            // Open a HTTP connection to the URL
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Allow Inputs
            conn.setDoInput(true);
            // Allow Outputs
            conn.setDoOutput(true);
            // Don't use a cached copy.
            conn.setUseCaches(false);
            // Use a post method.
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Authorization", authorization);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Disposition",
                    "attachment;filename=\"" + filename + "\"");

            // Send the file content to the output stream.
            DataOutputStream dataOutputStream = new DataOutputStream(conn.getOutputStream());
            File file = new File(filename);
            Files.copy(file.toPath(), dataOutputStream);
            dataOutputStream.flush();
            dataOutputStream.close();

            // Expect a 201 success code, or else print out the error.
            int responseCode = conn.getResponseCode();
            if (responseCode == 201) {
                System.out.print("File was uploaded successfully.");

            } else {
                InputStream inputStream = conn.getInputStream();
                byte[] buffer = new byte[2048];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    System.out.println(new String(buffer, 0, length));
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}