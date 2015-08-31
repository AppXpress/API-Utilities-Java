package com.gtnexus.attachment;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

// JSON library from https://github.com/douglascrockford/JSON-java
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DownloadUtility {
    // Version of API
    private final static String version = "310";
    // URL of Environment
    private final static String baseURL = "https://commerce-demo.gtnexus.com/rest/" + version;
    // Your user's authorization token
    private static String authorization;
    // Your organization's data key
    private static String dataKey;

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("The program accepts four arguments: " +
                "authorization token, data key, global object type, and object folder uid.");
            return;
        }

        // Retrieve the authorization token, data key, global object type, and object folder uid from arguments.
        authorization = args[0];
        dataKey = args[1];
        String globalObjectType = args[2];
        String folderUid = args[3];

        // Step 1 - Get the attachment list of the order.
        JSONArray attachmentList = getAttachmentList(globalObjectType, folderUid);

		try {
			// Step 2 - Download each attachment file.
			for (int i = 0; i < attachmentList.length(); i++) {
				JSONObject attachment = attachmentList.getJSONObject(i);
				String attachmentUid = attachment.getString("attachmentUid");
				String filename = attachment.getString("name");
				downloadAttachment(attachmentUid, filename);
			}
		} catch (JSONException e) {
			e.printStackTrace();
        }

    }

    public static JSONArray getAttachmentList(String globalObjectType, String folderUid) {
        try {
            String urlString = baseURL + "/" + globalObjectType + "/" + folderUid + "/attachment" +
                    "?dataKey=" + dataKey;
            URL url = new URL(urlString);

            // Set up the HTTP GET request.
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Connection", "Close");
            conn.setRequestProperty("Authorization", authorization);
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader responseBuffer =
                        new BufferedReader(new InputStreamReader((conn.getInputStream())));
                StringBuilder responseStrBuilder = new StringBuilder();
                String inputStr;
                while ((inputStr = responseBuffer.readLine()) != null) {
                    responseStrBuilder.append(inputStr);
                }

                // Convert the response body into a JSONObject.
                JSONObject response = new JSONObject(responseStrBuilder.toString());
                // Return the result[] array in the response body.
                return response.getJSONArray("result");

            } else {
                return new JSONArray();
            }

        } catch (Exception e) {
            System.out.println(e);
            return new JSONArray();
        }
    }


    public static void downloadAttachment(String attachmentUid, String filename) {
        try {
            String urlString =  baseURL + "/media/" + attachmentUid +
                    "?dataKey=" + dataKey;
            URL url = new URL(urlString);

            // Set up the HTTP GET request.
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Connection", "Close");
            conn.setRequestProperty("Authorization", authorization);

            // Expect a 200 success code, or else print out the error.
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {

                InputStream inputStream = conn.getInputStream();
                OutputStream outputStream = new FileOutputStream(filename);
                byte[] buffer = new byte[2048];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }

                inputStream.close();
                outputStream.close();

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