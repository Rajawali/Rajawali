package org.rajawali3d.examples;

import android.Manifest.permission;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import org.rajawali3d.examples.wallpaper.WallpaperPreferenceActivity;

public class LauncherActivity extends AppCompatActivity {

    private static final String TAG = "LauncherActivity";

    private static final int REQUEST_PERMISSIONS = 1;
    private static String[] PERMISSIONS = {
            permission.CAMERA,
            permission.READ_PHONE_STATE,
            permission.INTERNET,
            permission.ACCESS_NETWORK_STATE,
            permission.ACCESS_WIFI_STATE,
            permission.WAKE_LOCK,
            permission.NFC,
            permission.VIBRATE,
            permission.READ_EXTERNAL_STORAGE,
            permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new ExamplesFragment(), ExamplesFragment.TAG)
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                if (hasAllPermissions(PERMISSIONS)) {
                    Log.d(TAG, "All permissions granted!");
                } else {
                    Toast.makeText(
                            this,
                            "Cannot continue running Rajawali Examples without all required permissions.",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, WallpaperPreferenceActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Determine if the application has the necessary permissions to perform an action.
     *
     * @param permissions permissions to check
     * @return true if application has all permissions
     */
    boolean hasAllPermissions(String[] permissions) {
        // Only Marshmallow and higher needs to check permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        for (String permission : permissions) {
            final int result = checkSelfPermission(permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }
}
