package acr.browser.lightning.utils;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebStorage;
import android.webkit.WebView;

/**
 * Copyright 8/4/2015 Anthony Restaino
 */
public class WebUtils {

    public static void clearCookies(@NonNull Context context) {
        CookieManager c = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            c.removeAllCookies(null);
        } else {
            //noinspection deprecation
            CookieSyncManager.createInstance(context);
            //noinspection deprecation
            c.removeAllCookie();
        }
    }

    public static void clearWebStorage() {
        WebStorage.getInstance().deleteAllData();
    }

    public static void clearCache(Context context) {
        final WebView webView = new WebView(context);
        webView.clearCache(true);
    }

}
