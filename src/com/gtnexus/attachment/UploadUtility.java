package com.gtnexus.attachment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
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
        if (args.length != 5) {
            System.out.println("The program accepts five arguments: " +
                "authorization token, data key, global object type, " +
                "object folder uid, and attachment file name.");
            return;
        }

        // Retrieve the authorization token, data key, global object type,
        // object folder uid and attachment file name from arguments.
        // The attachment file should reside in the current working directory.
        authorization = args[0];
        dataKey = args[1];
        String globalObjectType = args[2];
        String folderUid = args[3];
        String filename = args[4];
        uploadAttachment(globalObjectType, folderUid, filename);
    }

    private static void uploadAttachment(
        String globalObjectType, String folderUid, String filename) {

        try {
            String urlString = baseURL + "/" + globalObjectType + "/" + folderUid
                + "/attach?dataKey=" + dataKey;
            URL url = new URL(urlString);

            // Create a unique boundary string based on time stamp.
            String boundary = "===" + System.currentTimeMillis() + "===";

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
            conn.setRequestProperty("Content-Type", "multipart/form-data; "
                + "boundary=" + boundary);

            // Create a multipart/form-data POST request.
            OutputStream outputStream = conn.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            File file = new File(filename);

            dataOutputStream.writeBytes("--");
            dataOutputStream.writeBytes(boundary);
            dataOutputStream.writeBytes("\r\n");

            // Because the API will accept any field name for the file,
            // so name={fieldName} is omitted from the Content-Disposition header.
            // Only the filename={filename} is specified.
            dataOutputStream.writeBytes("Content-Disposition: form-data; "
                + "filename=\"" + filename + "\"\r\n");

            // The Content-Type can be the generic application/octet-stream
            // or a more specific type dynamically determined by calling
            // URLConnection.guessContentTypeFromName(filename).
            dataOutputStream.writeBytes("Content-Type: "
                + URLConnection.guessContentTypeFromName(filename) + "; "
                + "charset=utf-8\r\n");

            dataOutputStream.writeBytes("\r\n");
            dataOutputStream.write(Files.readAllBytes(file.toPath()));
            dataOutputStream.writeBytes("\r\n");
            dataOutputStream.writeBytes("\r\n--" + boundary + "--\r\n");
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