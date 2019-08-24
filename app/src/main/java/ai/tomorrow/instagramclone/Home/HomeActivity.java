package ai.tomorrow.instagramclone.Home;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import ai.tomorrow.instagramclone.Login.LoginActivity;
import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.Share.NextActivity;
import ai.tomorrow.instagramclone.Utils.BottomNavigationViewHelper;
import ai.tomorrow.instagramclone.Utils.MainfeedListAdapter;
import ai.tomorrow.instagramclone.Utils.Permissions;
import ai.tomorrow.instagramclone.Utils.SectionsStatePagerAdapter;
import ai.tomorrow.instagramclone.Utils.UniversalImageLoader;
import ai.tomorrow.instagramclone.Utils.ViewCommentsFragment;
import ai.tomorrow.instagramclone.models.Photo;

public class HomeActivity extends AppCompatActivity implements
        MainfeedListAdapter.OnCommentThreadSelectedListener, MainfeedListAdapter.OnLoadMoreItemsListener {

    //constants
    private static final String TAG = "HomeActivity";
    private static final int CAMERA_REQUEST_CODE = 5;
    private static final int VERIFY_PERMISSION_REQUEST = 1;

    private Context mContext = HomeActivity.this;
    // FirebaseAuth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //widgets
    private RelativeLayout mRelativeLayout;
    private FrameLayout mFrameLayout;
    private ViewPager mViewPager;
    private ImageView mCamera, mSend;

    //vars
    private SectionsStatePagerAdapter pagerAdapter;
    private int mActivityNumber = 0;

    @Override
    public void onCommentThreadSelectedListener(Photo photo) {
        Log.d(TAG, "onCommentThreadSelectedListener: selected an comment thread");
        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putInt(getString(R.string.calling_activity_number),
                getIntent().getIntExtra(getString(R.string.calling_activity_number), getResources().getInteger(R.integer.home_activity_number)));
        fragment.setArguments(args);

        FragmentTransaction transaction = HomeActivity.this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
        hideRelativeLayout();
    }

    @Override
    public void onLoadMoreItems() {
        Log.d(TAG, "onLoadMoreItems: displaying more photos");
        HomeFragment fragment = (HomeFragment) pagerAdapter.getFragmentFromName(getString(R.string.home_fragment));
        if (fragment != null) {
            fragment.loadMorePhotos();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: starting");

        mRelativeLayout = findViewById(R.id.relLayoutParent);
        mFrameLayout = findViewById(R.id.container);
        mViewPager = findViewById(R.id.viewpager_container);
        mCamera = findViewById(R.id.ivCamera);
        mSend = findViewById(R.id.ivSend);

        setupFirebaseAuth();

        // check if user is logged in
        if (isLoggedin()) {
            // logged in
            Log.d(TAG, "onCreate: user logged in.");
            init();
        } else {
            // sign out
            Log.d(TAG, "onCreate: user signed out.");
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
            this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private void init() {
        UniversalImageLoader.initImageLoader(HomeActivity.this);
        setupBottomNavigationView();
        setupFragments();
        Log.d(TAG, "onCreate: getFragmentNumber: " + pagerAdapter.getFragmentNumber(getString(R.string.home_fragment)));
        setViewPager(pagerAdapter.getFragmentNumber(getString(R.string.home_fragment)));

        mCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigate to camera.");
                if (Permissions.checkPermissions(mContext, Permissions.CAMERA_PERMISSION[0])) {
                    // camera permission is granted
                    startTakePicture();
                } else {
                    // verify permission
                    ActivityCompat.requestPermissions(
                            HomeActivity.this, Permissions.CAMERA_PERMISSION, VERIFY_PERMISSION_REQUEST
                    );
                }
            }
        });
    }

    private void startTakePicture() {
        Log.d(TAG, "onClick: starting camera");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case VERIFY_PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    startTakePicture();
                }
            }
        }
    }

    private void setupFragments() {
        pagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new HomeFragment(), getString(R.string.home_fragment)); // fragment 0
        pagerAdapter.addFragment(new MessagesFragment(), getString(R.string.messages_fragment)); // fragment 1
    }

    private void setViewPager(int fragmentNumber) {
        mRelativeLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
        Log.d(TAG, "setViewPager: navigating to fragment #: " + fragmentNumber);

        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(fragmentNumber);
    }

    public void hideRelativeLayout() {
        Log.d(TAG, "hideLayout: hiding relativelayout");
        mRelativeLayout.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
    }

    public void showRelativeLayout() {
        Log.d(TAG, "hideLayout: showing relativelayout");
        mRelativeLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mFrameLayout.getVisibility() == View.VISIBLE) {
            showRelativeLayout();
        }
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();

        mActivityNumber = getIntent().getIntExtra(getString(R.string.calling_activity_number),
                getResources().getInteger(R.integer.home_activity_number));

        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: done taking a photo.");

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            // navigate to the final share screen to publish photo
            Log.d(TAG, "onActivityResult: attempting to navigate to final share screen");
            Intent intent = new Intent(this, NextActivity.class);
            intent.putExtra(getString(R.string.selected_bitmap), bitmap);
            startActivity(intent);
        }
    }

    /**
     * -------------------------------- firebase --------------------------
     */

    private boolean isLoggedin() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    /**
     * check to see if the @param 'user' is logged in
     *
     * @param user
     */
    private void checkCurrentUser(FirebaseUser user) {
        Log.d(TAG, "checkCurrentUser: checking if user is logged in " + user);
        if (user == null) {
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
            this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    /**
     * setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();

                if (user != null) {
                    // User is logged in
                    Log.d(TAG, "onAuthStateChanged: signed_in: " + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart.");
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        // check if user is logged in
        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
