package my.edu.utar.moneyforest.network;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
/*Done by Ng Jing Ying*/
/*This is a helper class that defines all the possible RESTful API calls in Money Forest application
* This class can be extended and defined according to different REST service calls in the future.
* */
public class MTRESTTask {
    private static final String GPT_URL = "https://api.openai.com/v1/chat/completions";
    public static String performGPTTask(final JSONArray messages, final String API) {
        String result = "";
        try {
            URL GPT_URL_endpt = new URL(GPT_URL);
            HttpsURLConnection myConnection;

            myConnection =
                    (HttpsURLConnection) GPT_URL_endpt.openConnection();
            myConnection.setRequestMethod("POST");
            myConnection.setDoOutput(true);
            myConnection.setChunkedStreamingMode(0);
            myConnection.setRequestProperty("Accept",
                    "application/json");
            myConnection.setRequestProperty("Content-Type",
                    "application/json");
            myConnection.setRequestProperty("Authorization", "Bearer " + API);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("model", "gpt-3.5-turbo");
            jsonObject.put("temperature", 0);
            jsonObject.put("messages", messages);
            String jsonPrompt = jsonObject.toString();
            try (OutputStream os = myConnection.getOutputStream()) {
                byte[] input = jsonPrompt.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            if (myConnection.getResponseCode() == 200) {
                InputStream responseBody = myConnection.getInputStream();
                result = readStream(responseBody);
            } else {
                result = "Error" + myConnection.getResponseMessage();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Exception: " + ex.getMessage();
        }
        return result;
    }

    public static String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while (i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (Exception ex) {
            return "";
        }

    }

}
