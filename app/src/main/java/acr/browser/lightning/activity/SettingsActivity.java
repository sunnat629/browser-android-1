/*
 * Copyright 2014 A.C.R. Development
 */
package acr.browser.lightning.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.anthonycr.grant.PermissionsManager;
import com.cliqz.browser.BuildConfig;
import com.cliqz.browser.R;
import com.cliqz.browser.app.BrowserApp;
import com.cliqz.browser.main.MainActivity;
import com.cliqz.browser.offrz.OffrzConfig;
import com.cliqz.browser.telemetry.Telemetry;
import com.cliqz.browser.telemetry.TelemetryKeys;
import com.cliqz.utils.ActivityUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final boolean IS_GHOSTERY = "ghostery".equals(BuildConfig.FLAVOR_api);

    @Inject
    Telemetry telemetry;

    private static class HeaderInfo {
        final String name;
        final long id;

        HeaderInfo(String name, long id) {
            this.name = name;
            this.id = id;
        }
    }

    private static final List<HeaderInfo> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Task description
        ActivityUtils.setTaskDescription(this, R.string.app_name, R.color.primary_color_dark,
                R.mipmap.ic_launcher);

        BrowserApp.getAppComponent().inject(this);
        // this is a workaround for the Toolbar in PreferenceActitivty
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        LinearLayout content = (LinearLayout) root.getChildAt(0);
        LinearLayout toolbarContainer = (LinearLayout) View.inflate(this, R.layout.toolbar_settings, null);

        root.removeAllViews();
        toolbarContainer.addView(content);
        root.addView(toolbarContainer);

        // now we can set the Toolbar using AppCompatPreferenceActivity
        Toolbar toolbar = (Toolbar) toolbarContainer.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences_headers, target);
        fragments.clear();
        final Iterator<Header> iterator = target.iterator();
        final boolean isOffrzAvailable = OffrzConfig.isOffrzSupportedForLang();
        while (iterator.hasNext()) {
            final Header header = iterator.next();
            if (IS_GHOSTERY && (
                    header.id == R.id.rate_us ||
                    header.id == R.id.feedback ||
                    header.id == R.id.report_site ||
                    header.id == R.id.tips_and_tricks
            )) {
                iterator.remove();
            } else if (!isOffrzAvailable && header.id == R.id.myoffrz) {
                iterator.remove();
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
                    && header.id == R.id.ad_block) {
                iterator.remove();
            } else {
                fragments.add(new HeaderInfo(header.fragment, header.id));
            }
        }
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        for (HeaderInfo info: fragments) {
            if (fragmentName.equals(info.name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final HeaderInfo info = fragments.get(position);
        if (info.id == R.id.imprint) {
            telemetry.sendSettingsMenuSignal(TelemetryKeys.IMPRINT, TelemetryKeys.MAIN);
            openUrl(getString(R.string.imprint_url));
        } else if (info.id == R.id.feedback) {
            telemetry.sendSettingsMenuSignal(TelemetryKeys.CONTACT, TelemetryKeys.MAIN);
            openUrl(getString(R.string.support_url));
        } else if (info.id == R.id.rate_us) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.cliqz.browser")));
        } else if (info.id == R.id.tips_and_tricks) {
            telemetry.sendSettingsMenuSignal(TelemetryKeys.TIPS_AND_TRICKS, TelemetryKeys.MAIN);
            openUrl(getString(R.string.tips_and_tricks_url));
        } else if (info.id == R.id.report_site) {
            openUrl(getString(R.string.report_site_url));
        } else if (info.id == R.id.myoffrz) {
            telemetry.sendSettingsMenuSignal(TelemetryKeys.ABOUT_MYOFFRZ, TelemetryKeys.MAIN);
            openUrl(getString(R.string.myoffrz_url));
        } else {
            super.onListItemClick(l, v, position, id);
        }
    }

    private void openUrl(String url) {
        final Intent browserIntent = new Intent(this, MainActivity.class);
        browserIntent.setAction(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(url));
        startActivity(browserIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            finish();
        } else {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        telemetry.sendOrientationSignal(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ?
        TelemetryKeys.LANDSCAPE : TelemetryKeys.PORTRAIT, TelemetryKeys.SETTINGS);
    }
}
