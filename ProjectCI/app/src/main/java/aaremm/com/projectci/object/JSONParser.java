package aaremm.com.projectci.object;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by rahul on 02-09-2014.
 */
public class JSONParser {


    public JSONArray getJson(String url) {

        InputStream is = null;
        String result = "";
        JSONArray jsonArray = null;

        // HTTP
        try {
            HttpClient httpclient = new DefaultHttpClient(); // for port 80 requests!
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            return null;
        }

        // Read response to string
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
        } catch (Exception e) {
            return null;
        }

        // Convert string to object
        try {
            jsonArray = new JSONArray(result);
        } catch (JSONException e) {
            return null;
        }

        return jsonArray;

    }
}