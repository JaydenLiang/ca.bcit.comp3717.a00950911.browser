package ca.bcit.comp3717.a00950911.browser;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class WebBrowserActivity extends AppCompatActivity {
    private String rawURL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_browser);
        this.rawURL = getIntent().getStringExtra("rawURL");
        WebView webView = (WebView) findViewById(R.id.webbrowser_webview);
        webView.loadUrl(this.rawURL);
    }
}
