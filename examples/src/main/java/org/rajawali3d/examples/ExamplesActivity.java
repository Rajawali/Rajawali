package org.rajawali3d.examples;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import org.rajawali3d.examples.data.Example;
import org.rajawali3d.examples.examples.AExampleFragment;

public class ExamplesActivity extends AppCompatActivity {

    public static final String EXTRA_EXAMPLE = "EXTRA_EXAMPLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examples);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    processExtras(extras);
                }
            }
        }
    }

    private void processExtras(Bundle extras) {
        Example example = extras.getParcelable(EXTRA_EXAMPLE);
        if (example == null) {
            throw new NullPointerException();
        }

        Class aClass = example.getType();

        try {
            Bundle bundle = new Bundle();
            bundle.putString(AExampleFragment.BUNDLE_EXAMPLE_URL, example.getPath());

            Fragment fragment = (Fragment) aClass.newInstance();
            fragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment, aClass.getName())
                    .commit();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
