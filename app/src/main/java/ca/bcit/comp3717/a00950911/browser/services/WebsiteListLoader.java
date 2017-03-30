package ca.bcit.comp3717.a00950911.browser.services;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jaydenliang on 2017-03-23.
 */

public class WebsiteListLoader {
    public static String LOCAL_EVENT_ON_LIST_LOADED_COMPLETE = "BrowserListLoader_LOCAL_EVENT_ON_LIST_LOADED_COMPLETE";
    private static String src = "http://cartoonapi.azurewebsites.net/api/cartoon/";
    private Context context;
    private URL url;
    private ArrayList<WebsiteObject> websiteObjects;
    private boolean hasError;
    private String errorMessage;

    public WebsiteListLoader(Context context) {
        this.context = context;
        try{
            this.url = new URL(src);
        } catch (MalformedURLException e){
            url = null;
        }
        websiteObjects = new ArrayList<>();
    }

    public boolean HasError() {
        return hasError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public WebsiteListLoader load(){
        if(url != null){
            URLLoadTask task = new URLLoadTask();
            task.execute(url);
        }
        return this;
    }

    public ArrayList<WebsiteObject> getWebsiteObjects(){
        return websiteObjects;
    }

    public HashMap<String, WebsiteObject> cloneWebsiteObjects(){
        return (HashMap<String, WebsiteObject>)(websiteObjects.clone());
    }

    private void parseWebsitesFromJSON(JSONArray json){
        int i = json.length();
        if(websiteObjects.size() > 0)
            websiteObjects.clear();
        try {
            while (i > 0) {
                JSONObject o = json.getJSONObject(--i);
                String websiteURL;
                if (o.has("url"))
                    websiteURL = o.getString("url");
                else if (o.has("pictureUrl"))
                    websiteURL = o.getString("pictureUrl");
                else
                    websiteURL = i % 2 == 0 ? "http://www.bcit.ca/" : "http://www.youtube.com/";

                WebsiteObject w = new WebsiteObject(o.getString("name"), websiteURL);
                websiteObjects.add(w);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void onURLLoaded(JSONArray json){
        parseWebsitesFromJSON(json);
        Intent intent = new Intent(LOCAL_EVENT_ON_LIST_LOADED_COMPLETE);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private class URLLoadTask extends AsyncTask<URL, String, String>{

        @Override
        protected String doInBackground(URL... params) {
            // Create a new HttpClient and Post Header
            HttpURLConnection urlConnection = null;
            String content = "";
            JSONArray json = null;
            int index = 0;
            while(index < params.length) {
                try {
                    hasError = false;
                    errorMessage = "";
                    urlConnection = (HttpURLConnection) params[index++].openConnection();
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    String line;

                    while ((line = reader.readLine()) != null) {
                        content += line;
                    }
                    json = new JSONArray(content);
                } catch (IOException e) {
                    hasError = true;
                    errorMessage = "No Internet connection.";
//                    e.printStackTrace();
                } catch (JSONException e){
                    hasError = true;
                    errorMessage = "Failed to load website list.";
                }finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (json != null) {
                        onURLLoaded(json);
                    }
                }
            }
            return null;
        }
    }

    public class WebsiteObject {
        private String name;
        private String rawURL;

        WebsiteObject(String name, String rawURL) {
            this.name = name.trim();
            this.rawURL = rawURL.trim();
        }

        public String getName() {
            return name;
        }

        public String getRawURL() {
            return rawURL;
        }

        public URL getURL(){
            URL url = null;
            try{
                url = new URL(rawURL);
            } catch (MalformedURLException e){
                e.printStackTrace();
            }
            return url;
        }
    }
}
