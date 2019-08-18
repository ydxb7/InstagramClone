package ai.tomorrow.instagramclone.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import ai.tomorrow.instagramclone.Share.ShareActivity;

public class Permissions {
    private static final String TAG = "Permissions";

    private static final int VERIFY_PERMISSION_REQUEST = 1;

    public static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    public static final String[] CAMERA_PERMISSION = {
            Manifest.permission.CAMERA
    };

    public static final String[] WRITE_STORAGE_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final String[] READ_STORAGE_PERMISSION = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    /**
     * verify all the permissions passed to the array
     * @param permissions
     */
    public static void verifyPermissions(Activity activity, String[] permissions){
        Log.d(TAG, "verifyPermissions: verifying permissions");

        ActivityCompat.requestPermissions(
                activity,
                permissions,
                VERIFY_PERMISSION_REQUEST
        );
    }

    /**
     * check an array of permissions are they have been granted
     * @param permissions
     * @return
     */
    public static boolean checkPermissionArray(Context context, String[] permissions){
        Log.d(TAG, "checkPermissionArray: checking permissions array.");

        for (int i = 0; i < permissions.length; i++){
            String check = permissions[i];
            if (!checkPermissions(context, check)){
                return false;
            }
        }
        return true;
    }

    /**
     * check a single permission is it has been granted
     * @param permission
     * @return
     */
    public static boolean checkPermissions(Context context, String permission){
        Log.d(TAG, "checkPermissions: checking permission: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(context, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermissions: Permission was not granted for: " + permission);
            return false;
        } else {
            Log.d(TAG, "checkPermissions: Permission was granted for: " + permission);
            return true;
        }
    }

}
