package ai.tomorrow.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;

import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.Utils.ViewCommentsFragment;
import ai.tomorrow.instagramclone.Utils.ViewPostFragment;
import ai.tomorrow.instagramclone.Utils.ViewProfileFragment;
import ai.tomorrow.instagramclone.models.Photo;
import ai.tomorrow.instagramclone.models.User;

public class ProfileActivity extends AppCompatActivity implements
        ProfileFragment.OnGridImageSelectedListener,
        ViewProfileFragment.OnGridImageSelectedListener,
        ViewPostFragment.OnCommentThreadSelectedListener {

    private static final String TAG = "ProfileActivity";

    private Context mContext = ProfileActivity.this;
    private ProgressBar mProgressBar;
    private ImageView profilePhoto;
    private int mActivityNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: starting");
        mActivityNumber = getIntent().getIntExtra(getString(R.string.calling_activity_number),
                getResources().getInteger(R.integer.profile_activity_number));

        init();

    }

    @Override
    public void onCommentThreadSelectedListener(Photo photo) {
        Log.d(TAG, "onCommentThreadSelectedListener: selected an comment thread");
        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putInt(getString(R.string.calling_activity_number),
                getIntent().getIntExtra(getString(R.string.calling_activity_number), getResources().getInteger(R.integer.profile_activity_number)));
        fragment.setArguments(args);

        FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }

    @Override
    public void onGridImageSelected(Photo photo, int activityNumber) {
        Log.d(TAG, "onGridImageSelected: selected an image gridview: " + photo.toString());
        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putInt(getString(R.string.calling_activity_number), getIntent().getIntExtra(getString(R.string.calling_activity_number),
                getResources().getInteger(R.integer.profile_activity_number)));

        args.putParcelable(getString(R.string.photo), photo);
        fragment.setArguments(args);

        FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }

    private void init(){
        Log.d(TAG, "init: inflating" + getString(R.string.profile_fragment));

        Intent intent = getIntent();
        if (intent.hasExtra(mContext.getString(R.string.calling_activity_number))){
            Log.d(TAG, "init: searching for user object attached as intent extra");
            if (intent.hasExtra(mContext.getString(R.string.selected_user))){
                User user = intent.getParcelableExtra(getString(R.string.selected_user));
                if (user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    Log.d(TAG, "init: view user is our own user.");
                    Log.d(TAG, "init: inflating Profile");

                    ProfileFragment fragment = new ProfileFragment();

                    Bundle args = new Bundle();
                    args.putInt(getString(R.string.calling_activity_number),
                            getIntent().getIntExtra(getString(R.string.calling_activity_number),
                                    getResources().getInteger(R.integer.profile_activity_number)));
                    fragment.setArguments(args);

                    FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    transaction.addToBackStack(getString(R.string.profile_fragment)); // add fragment into backstack
                    transaction.commit();
                } else {
                    Log.d(TAG, "init: view profile for other user.");
                    ViewProfileFragment fragment = new ViewProfileFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(mContext.getString(R.string.selected_user),
                            intent.getParcelableExtra(mContext.getString(R.string.selected_user)));
                    args.putInt(getString(R.string.calling_activity_number),
                            intent.getIntExtra(getString(R.string.calling_activity_number),
                            getResources().getInteger(R.integer.profile_activity_number)));
//                    args.putString(mContext.getString(R.string.calling_activity), intent.getStringExtra(mContext.getString(R.string.calling_activity)));
                    fragment.setArguments(args);

                    FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragment);
                    transaction.addToBackStack(getString(R.string.view_profile_fragment)); // add fragment into backstack
                    transaction.commit();
                }
            }else {
                Toast.makeText(mContext, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        }else {
            Log.d(TAG, "init: inflating Profile");

            ProfileFragment fragment = new ProfileFragment();
            FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment);
            transaction.addToBackStack(getString(R.string.profile_fragment)); // add fragment into backstack
            transaction.commit();
        }

    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed.");
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (currentFragment instanceof ProfileFragment || currentFragment instanceof ViewProfileFragment){
            finish();
        }
        super.onBackPressed();
    }
}
