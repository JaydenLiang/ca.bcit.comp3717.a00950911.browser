package ca.bcit.comp3717.a00950911.browser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;

import ca.bcit.comp3717.a00950911.browser.services.HTTPContentLoader;
import ca.bcit.comp3717.a00950911.browser.services.WebsiteObject;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    String srcURL = "http://max.bcit.ca/comp.json";
    ArrayAdapter<String> websiteListAdapter;
    ListView listView;
    HTTPContentLoader httpContentLoader;
    HTTPContentLoader.Loader loader;
    BroadcastReceiver broadcastReceiver;
    ArrayList<WebsiteObject> websiteObjects = new ArrayList<>();
    ArrayList<Exception> exceptions = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //add a broadcast receiver to listen to event from HTTPContentLoader
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onHTTPContentLoaded(intent);
            }
        };

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, new
                IntentFilter(HTTPContentLoader.LOCAL_EVENT_ON_HTTP_CONTENT_LOAD_COMPLETED));

        //initialize the list adapter and list view
        websiteListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView = (ListView) findViewById(R.id.website_list_view);
        listView.setOnItemClickListener(this);
        listView.setAdapter(websiteListAdapter);

        httpContentLoader = new HTTPContentLoader(getApplicationContext());
        load();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
        httpContentLoader.cleanup();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * load the content from source URL
     */
    private void load() {
        try {
            httpContentLoader.load(srcURL);
        } catch (MalformedURLException e) {
            exceptions.add(e);
            handleExceptions(exceptions);
        }
    }

    /**
     * call alert dialog to display exceptions messages
     *
     * @param exceptions arraylist of exception
     */
    private void handleExceptions(final ArrayList<Exception> exceptions) {
        if (exceptions.size() == 0)
            return;
        //no need to throw the exception
        Exception e = exceptions.remove(0);
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(e.getClass().getSimpleName()).setMessage(e.getMessage());
        //loop through all the exception array to display messages
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //reload the content
                if (loader != null)
                    httpContentLoader.load(loader);
                else
                    load();
            }
        });
        //if this not the last exception, display a "Next" button to continue, and hide the retry button;
        if (exceptions.size() > 0) {
            dialog = builder.setNegativeButton("Next", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handleExceptions(exceptions);
                }
            }).show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
        } else {
            dialog = builder.setNegativeButton("Dismiss", null).show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
        }
    }

    /**
     * get a String array for the ListView items
     *
     * @return a string array
     */
    private String[] getListViewItems() {
        //show an error message if there is no website to list
        if (websiteObjects.size() == 0) {
            exceptions.add(new Exception("Website list is empty. No website to list for now."));
            return null;
        }
        String[] items = new String[websiteObjects.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = websiteObjects.get(i).getName();
        }
        return items;
    }

    /**
     * create website objects from the given content (expected a plain text of JSON)
     *
     * @param content content to create website objects, expected a plain text of JSON
     */
    private void createWebsiteObjectsFromContent(String content) {
        try {
            JSONArray json = new JSONArray(content);
            int i = json.length();
            if (websiteObjects.size() > 0)
                websiteObjects.clear();
            while (i > 0) {
                JSONObject o = json.getJSONObject(--i);
                String websiteURL;
                if (!o.has("url"))
                    throw new JSONException("Unable to parse content from the source URL.");

                websiteObjects.add(new WebsiteObject(o.getString("name"), o.getString("url")));
            }
        } catch (JSONException e) {
            exceptions.add(e);

        }
    }

    /**
     * handle loader loaded event.
     *
     * @param intent generated by HTTPContentLoader that contains information about which loader is loaded.
     */
    private void onHTTPContentLoaded(Intent intent) {
        loader = httpContentLoader.getLoader(intent);
        //do the error handling
        if (loader.hasExceptions()) {
            handleExceptions(loader.getExceptions());
        } else {

            //create websiteObject for listing
            createWebsiteObjectsFromContent(loader.getContent());
            //update listview
            String[] items = getListViewItems();
            //handle exceptions caught in this process
            if (exceptions.size() > 0) {
                handleExceptions(exceptions);
            }
            if (items != null && websiteListAdapter != null) {
                websiteListAdapter.clear();
                websiteListAdapter.addAll(items);
            }
        }
        httpContentLoader.destroyLoader(loader);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, WebBrowserActivity.class);
        intent.putExtra("rawURL", websiteObjects.get(position).getRawURL());
        startActivity(intent);
    }
}
