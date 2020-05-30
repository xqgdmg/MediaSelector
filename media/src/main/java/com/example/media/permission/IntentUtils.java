package com.example.media.permission;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class IntentUtils {
    public static final int OPEN_APPLY_CENTER_CODE = 5501;

    /**
     * Activity打开应用中心
     *
     * @param activity 页面
     */
    public static void openActivityApplyCenter(@NonNull Activity activity) {
        Uri packageURI = Uri.parse("package:" + activity.getPackageName());
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
        activity.startActivityForResult(intent, OPEN_APPLY_CENTER_CODE);
    }

    /**
     * fragemnt打开应用中心
     *
     * @param fragment fragment
     * @param activity activity
     */
    public static void openFragmentApplyCenter(@NonNull Fragment fragment, @NonNull Activity activity) {
        Uri packageURI = Uri.parse("package:" + activity.getPackageName());
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
        fragment.startActivityForResult(intent, OPEN_APPLY_CENTER_CODE);
    }
}
