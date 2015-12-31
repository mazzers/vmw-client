package com.example.mazzers.vmw_client;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by mazzers on 31. 12. 2015.
 */
public class Task extends AsyncTask<String,Void,String> {
    private final String USER_AGENT = "Mozilla/5.0";



    @Override
    protected String doInBackground(String... urls) {
        StringBuffer response = new StringBuffer();

        String url = "http://192.168.202.1:3456/customapi/images/?tag=hat&r=0&g=255&b=255";

        URL obj = null;
        try {
            obj = new URL(url);

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();


        //print result
        System.out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    @Override
    protected void onPostExecute(String string) {
        Log.d("DEBUGAA", "onPostExecute");
        try {
//            JSONObject jsonObject = new JSONObject(string);
            JSONArray jsonArray = new JSONArray(string);
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject object = jsonArray.getJSONObject(i);
                String addr = object.getString("photoUrl");
                String value = object.getString("colorShare");

                System.out.println("Parsed: addr - "+ addr + " value: "+ value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
