package ca.bcit.comp3717.a00950911.browser.services;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * A Http content loader class
 * Created by jaydenliang on 2017-03-25.
 */

public class HTTPContentLoader {
    public static String LOCAL_EVENT_ON_HTTP_CONTENT_LOAD_COMPLETED =
            "HTTPContentLoader_LOCAL_EVENT_ON_HTTP_CONTENT_LOAD_COMPLETED";
    private static int id_inc = 0;
    private static int LOADER_INVALID_ID = -1;
    private static String LOADER_VAR_ID = "loader_id";
    private Context context;
    private SparseArray<Loader> loaderSparseArray;

    public HTTPContentLoader(Context context) {
        this.context = context;
        loaderSparseArray = new SparseArray<>();
    }

    /**
     * create a content loader by a given url
     *
     * @param url url to load the content
     * @return a Loader that can be used by this class
     * @throws MalformedURLException thrown if the given url string is invalid
     */
    public Loader createLoader(@NonNull String url) throws MalformedURLException {
        URL u = new URL(url);
        Loader loader = new Loader(u);
        loaderSparseArray.setValueAt(loader.getId(), loader);
        return loader;
    }

    /**
     * get a loader by a given id
     *
     * @param id loader id
     * @return a Loader that is created and can be used by this class
     */
    public Loader getLoader(int id) {
        return loaderSparseArray.get(id);
    }

    /**
     * get a loader by an intent
     *
     * @param intent an intent that's passed to a listener registered in LocalBroadcastManager
     * @return a Loader that is created and can be used by this class
     */
    public Loader getLoader(@NonNull Intent intent) {
        int id = intent.getIntExtra(LOADER_VAR_ID, LOADER_INVALID_ID);
        if (id != LOADER_INVALID_ID) {
            return getLoader(id);
        }
        return null;
    }

    /**
     * load content by a given url
     *
     * @param url url to load the content
     * @return the HTTPContentLoader instance itself for chaining
     * @throws MalformedURLException thrown if the given url string is invalid
     */
    public HTTPContentLoader load(@NonNull String url) throws MalformedURLException {
        Loader loader = createLoader(url);
        return load(loader);
    }

    /**
     * load content by a given loader created by this class
     *
     * @param loader a loader created by any instance of this class.
     * @return the HTTPContentLoader instance itself for chaining
     */
    public HTTPContentLoader load(@NonNull Loader loader) {
        (new LoaderTask()).execute(loader);
        return this;
    }

    /**
     * internal method to broadcast a local message of a load-completed event
     *
     * @param id the loader id
     */
    private void onLoaderTaskComplete(int id) {
        Intent intent = new Intent(LOCAL_EVENT_ON_HTTP_CONTENT_LOAD_COMPLETED);
        intent.putExtra(LOADER_VAR_ID, id);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Loader class associated with the HTTPContentLoader class
     */
    public class Loader {
        private int id;
        private ArrayList<Exception> exceptions;
        private String content;
        private URL url;

        public Loader(@NonNull URL url) {
            id = ++id_inc;
            this.url = url;
            exceptions = new ArrayList<>();
            content = "";
        }

        public int getId() {
            return id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        /**
         * check if any exception occurred while this loader is running
         *
         * @return true or false
         */
        public boolean hasExceptions() {
            return exceptions.size() > 0;
        }

        public ArrayList<Exception> getExceptions() {
            return exceptions;
        }
    }

    /**
     * internal AsyncTask for loading http content in a background thread.
     */
    private class LoaderTask extends AsyncTask<Loader, String, Integer> {
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            onLoaderTaskComplete(integer);
        }

        @Override
        protected Integer doInBackground(Loader... params) {
            if (params.length == 0)
                return LOADER_INVALID_ID;
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) params[0].url.openConnection();
                InputStream in = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line, content = "";
                while ((line = reader.readLine()) != null) {
                    content += line;
                }
                params[0].setContent(content);
            } catch (FileNotFoundException e) {
                params[0].exceptions.add(new FileNotFoundException("File not found: " + e.getMessage()));
            } catch (IOException e) {
                params[0].exceptions.add(new IOException("Error in loading from url: " + e.getMessage()));
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return params[0].getId();
        }
    }
}
