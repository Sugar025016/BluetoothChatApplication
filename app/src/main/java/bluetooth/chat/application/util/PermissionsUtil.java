package bluetooth.chat.application.util;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

public class PermissionsUtil {

    private Activity activity;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };



    public PermissionsUtil(Activity activity) {
        this.activity=activity;
    }

    public boolean openPermissions() {
        int permission1 = ActivityCompat.checkSelfPermission( activity, Manifest.permission.BLUETOOTH_SCAN);
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            boolean b = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.BLUETOOTH_SCAN);
            if (b) {
                new AlertDialog.Builder(activity)
                        .setCancelable(false)
                        .setTitle("需要藍芽權限")
                        .setMessage("藍芽需要藍芽權限才能連線")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
                            }
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions(
                        activity,
                        PERMISSIONS_STORAGE,
                        1
                );
            }
            return false;
        }
        return true;
    }
    public boolean checkPermissions() {
        int permission1 = ActivityCompat.checkSelfPermission( activity, Manifest.permission.BLUETOOTH_SCAN);
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

}
