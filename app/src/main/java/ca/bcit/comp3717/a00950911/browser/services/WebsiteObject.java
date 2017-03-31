package ca.bcit.comp3717.a00950911.browser.services;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Website Object
 * Created by jaydenliang on 2017-03-30.
 */

public class WebsiteObject {
    private String name;
    private String rawURL;

    public WebsiteObject(String name, String rawURL) {
        this.name = name.trim();
        this.rawURL = rawURL.trim();
    }

    public String getName() {
        return name;
    }

    public String getRawURL() {
        return rawURL;
    }

    public URL getURL() {
        URL url = null;
        try {
            url = new URL(rawURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
}