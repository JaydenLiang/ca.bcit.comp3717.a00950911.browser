package ca.bcit.comp3717.a00950911.browser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Iterator;

import ca.bcit.comp3717.a00950911.browser.services.WebsiteListLoader;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    ArrayAdapter<String> websiteListAdapter;
    ListView listView;
    WebsiteListLoader websiteListLoader;
    BroadcastReceiver broadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onWebsiteListLoaded();
            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, new
                IntentFilter(WebsiteListLoader.LOCAL_EVENT_ON_LIST_LOADED_COMPLETE));

        websiteListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView = (ListView) findViewById(R.id.website_list_view);
        listView.setOnItemClickListener(this);
        listView.setAdapter(websiteListAdapter);
        websiteListLoader = new WebsiteListLoader(getApplicationContext());
        websiteListLoader.load();
    }

    private String[] getListViewItems(){
        ArrayList<WebsiteListLoader.WebsiteObject> arrayList = websiteListLoader.getWebsiteObjects();
        if(arrayList.size() == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Website list is empty. No website to list for now.")
                    .setNegativeButton("Dismiss", null)
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            websiteListLoader.load();
                        }
                    }).show();
            return null;
        }
        String[] items = new String[arrayList.size()];
        for(int i = 0; i < items.length; i ++){
            items[i] = arrayList.get(i).getName();
        }
        return items;
    }

    public void onReloadButonClicked(View view){
        websiteListLoader.load();
    }

    private void onWebsiteListLoaded(){
        if(websiteListLoader == null)
            return;
        if(websiteListLoader.HasError()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(websiteListLoader.getErrorMessage())
                    .setNegativeButton("Dismiss", null)
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            websiteListLoader.load();
                        }
                    }).show();
            return;
        }
        String[] items = getListViewItems();
        if(items != null && websiteListAdapter != null) {
            websiteListAdapter.clear();
            websiteListAdapter.addAll(items);

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        WebsiteListLoader.WebsiteObject websiteObject = websiteListLoader.getWebsiteObjects().get(position);
        Intent intent = new Intent(this, WebBrowserActivity.class);
        intent.putExtra("rawURL", websiteObject.getRawURL());
        startActivity(intent);
    }
}
