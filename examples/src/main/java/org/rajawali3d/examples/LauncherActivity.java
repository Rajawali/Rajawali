package org.rajawali3d.examples;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.rajawali3d.examples.wallpaper.WallpaperPreferenceActivity;

public class LauncherActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new ExamplesFragment(), ExamplesFragment.TAG)
                .commit();
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

}
