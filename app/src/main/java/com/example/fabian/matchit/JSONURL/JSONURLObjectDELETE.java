package com.example.fabian.matchit.JSONURL;

import android.util.Log;

import com.example.fabian.matchit.GlobalVariables;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JSONURLObjectDELETE {

    public static String getJSONfromURL(String url) {
        InputStream is = null;
        String result = "";


        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpDelete httpdelete = new HttpDelete(url);

            httpdelete.setHeader("Authorization", "Bearer " + GlobalVariables.BearerMatch);
            httpdelete.setHeader("Content-type", "application/json");
            httpdelete.setHeader("Accept-Language", GlobalVariables.LANGUAGE);

            HttpResponse response = httpclient.execute(httpdelete);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();

        } catch (Exception e) {
            Log.e("log_tag", "Error in http connection " + e.toString());
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();

        } catch (Exception e) {
            Log.e("log_tag", "Error converting result " + e.toString());
        }



        return result.substring(0, 1);
    }
}