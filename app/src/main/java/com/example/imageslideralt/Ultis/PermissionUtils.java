package com.example.imageslideralt.Ultis;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

public class PermissionUtils {
    public interface PermissionAskListener {
        /*
         * Callback to ask permission
         * */
        void onPermissionAsk();
        /*
         * Callback on permission denied
         * */
        void onPermissionPreviouslyDenied();
        /*
         * Callback on permission "Never show again" checked and denied
         * */
        void onPermissionDisabled();
        /*
         * Callback on permission granted
         * */
        void onPermissionGranted();
    }
    public static final String CAMERA_PERMISSION = "android.permission.CAMERA";
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    /*
     * Check if version is marshmallow and above.
     * Used in deciding to ask runtime permission
     * */
    public static boolean shouldAskPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
    private static boolean shouldAskPermission(Context context, String permission){
        if (shouldAskPermission()) {
            int permissionResult = ActivityCompat.checkSelfPermission(context, permission);
            if (permissionResult != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }
    public static void checkPermission(Context context, String permission, PermissionAskListener listener){
        /*
         * If permission is not granted
         * */
        if (shouldAskPermission(context, permission)){
            /*
             * If permission denied previously
             * */
            if (((Activity) context).shouldShowRequestPermissionRationale(permission)) {
                listener.onPermissionPreviouslyDenied();
            } else {
                /*
                 * Permission denied or first time requested
                 * */
                if (PreferenceUtils.isFirstTimeAskingPermission(context, permission)) {
                    PreferenceUtils.firstTimeAskingPermission(context, permission, false);
                    listener.onPermissionAsk();
                } else {
                    /*
                     * Handle the feature without permission or ask user to manually allow permission
                     * */
                    listener.onPermissionDisabled();
                }
            }
        } else {
            listener.onPermissionGranted();
        }
    }

    public static void requestPermission(Activity activity, String permission, int requestCode){
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
    }

}