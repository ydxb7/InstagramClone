package ai.tomorrow.instagramclone.Share;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ai.tomorrow.instagramclone.R;

public class NextActivity extends AppCompatActivity {

    private static final String TAG = "NextActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        Log.d(TAG, "onCreate: get the chosen image: " + getIntent().getStringExtra(getString(R.string.selected_image)));

    }
}
