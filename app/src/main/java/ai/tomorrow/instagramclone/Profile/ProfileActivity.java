package ai.tomorrow.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.Utils.ViewCommentsFragment;
import ai.tomorrow.instagramclone.Utils.ViewPostFragment;
import ai.tomorrow.instagramclone.Utils.ViewProfileFragment;
import ai.tomorrow.instagramclone.models.Photo;
import ai.tomorrow.instagramclone.models.User;

public class ProfileActivity extends AppCompatActivity implements
        ProfileFragment.OnGridImageSelectedListener,
        ViewPostFragment.OnCommentThreadSelectedListener {

    private static final String TAG = "ProfileActivity";
    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;

    private Context mContext = ProfileActivity.this;
    private ProgressBar mProgressBar;
    private ImageView profilePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: starting");

        init();

    }

    @Override
    public void onCommentThreadSelectedListener(Photo photo) {
        Log.d(TAG, "onCommentThreadSelectedListener: selected an comment thread");
        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
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
        args.putInt(getString(R.string.activity_number), activityNumber);
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
        if (intent.hasExtra(mContext.getString(R.string.calling_activity))){
            Log.d(TAG, "init: searching for user object attached as intent extra");
            if (intent.hasExtra(mContext.getString(R.string.selected_user))){
                ViewProfileFragment fragment = new ViewProfileFragment();
                Bundle args = new Bundle();
                args.putParcelable(mContext.getString(R.string.selected_user),
                        intent.getParcelableExtra(mContext.getString(R.string.selected_user)));
                fragment.setArguments(args);

                FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, fragment);
                transaction.addToBackStack(getString(R.string.view_profile_fragment)); // add fragment into backstack
                transaction.commit();
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
}
