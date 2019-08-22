package ai.tomorrow.instagramclone.Likes;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ai.tomorrow.instagramclone.Home.HomeFragment;
import ai.tomorrow.instagramclone.Home.MessagesFragment;
import ai.tomorrow.instagramclone.Profile.ProfileActivity;
import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.Utils.BottomNavigationViewHelper;
import ai.tomorrow.instagramclone.Utils.FirebaseMethods;
import ai.tomorrow.instagramclone.Utils.FollowingLikesListAdapter;
import ai.tomorrow.instagramclone.Utils.SectionsPagerAdapter;
import ai.tomorrow.instagramclone.Utils.ViewPostFragment;
import ai.tomorrow.instagramclone.models.Follow;
import ai.tomorrow.instagramclone.models.LikePhoto;
import ai.tomorrow.instagramclone.models.Photo;

public class LikesActivity extends AppCompatActivity implements
        FollowingLikesListAdapter.OnGridImageSelectedListener {

    private static final String TAG = "LikesActivity";

    //vars
    private int mActivityNumber;
    private Context mContext = LikesActivity.this;

    //widgets
    private TabLayout mTab;
    private FrameLayout mFrameLayout;
    private RelativeLayout mRelativeLayout;
    private ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiy_likes);
        Log.d(TAG, "onCreate: starting");
        setupBottomNavigationView();

        mTab = findViewById(R.id.tabs);
        mViewPager = findViewById(R.id.viewpager_container);
        mFrameLayout = findViewById(R.id.container);
        mRelativeLayout = findViewById(R.id.relLayoutParent);

        setupViewPager();
    }

    /**
     * Responsible for adding the 3 tabs: Camera, Home, Messages
     */
    private void setupViewPager(){
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FollowingFragment()); //index 0
        adapter.addFragment(new YouFragment()); //index 1
        mViewPager.setAdapter(adapter);

        mTab.setupWithViewPager(mViewPager);

        mTab.getTabAt(0).setText(getString(R.string.following));
        mTab.getTabAt(1).setText(getString(R.string.you));
    }

    @Override
    public void onGridImageSelected(Photo photo, int activityNumber) {
        Log.d(TAG, "onGridImageSelected: selected an image gridview: " + photo.toString());
        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        Log.d(TAG, "onGridImageSelected: put R.integer.likes_activity_number.");
        args.putInt(getString(R.string.calling_activity_number),
                getIntent().getIntExtra(getString(R.string.calling_activity_number), getResources().getInteger(R.integer.likes_activity_number)));
        fragment.setArguments(args);

        FragmentTransaction transaction = LikesActivity.this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
        hideRelativeLayout();
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
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();

        mActivityNumber = getIntent().getIntExtra(getString(R.string.calling_activity_number),
                getResources().getInteger(R.integer.likes_activity_number));

        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
    }
}
