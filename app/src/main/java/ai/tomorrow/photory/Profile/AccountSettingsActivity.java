package ai.tomorrow.photory.Profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

import ai.tomorrow.photory.R;
import ai.tomorrow.photory.Utils.BottomNavigationViewHelper;
import ai.tomorrow.photory.Utils.FirebaseMethods;
import ai.tomorrow.photory.Utils.SectionsStatePagerAdapter;

public class AccountSettingsActivity extends AppCompatActivity {

    private static final String TAG = "AccountSettingsActivity";

    //vars
    private int mActivityNumber;
    private Context mContext;

    //widgets
    private SectionsStatePagerAdapter pagerAdapter;
    private ViewPager mViewPager;
    private RelativeLayout mRelativeLayout;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsettings);
        mContext = AccountSettingsActivity.this;
        Log.d(TAG, "onCreate: started.");
        mViewPager = findViewById(R.id.viewpager_container);
        mRelativeLayout = findViewById(R.id.relLayout1);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);

        setupSettingList();
        setupBottomNavigationView();
        setupFragments();
        getIncomingIntent();

        // setup the backarrow for navigating back to 'ProfileActivity'
        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to 'ProfileActivity'. ");
                finish();
                AccountSettingsActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    private void getIncomingIntent() {
        Intent intent = getIntent();

        // if there is an imageUrl attached as an extra, then it was chosen from the gallery fragment
        if (intent.hasExtra(getString(R.string.selected_image))) {
            Log.d(TAG, "getIncomingIntent: navigating to EditProfileFragment");
            setViewPager(pagerAdapter.getFragmentNumber(getString(R.string.edit_profile_fragment)));
            // set the new profile picture
            FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
            firebaseMethods.uploadNewPhotos(getString(R.string.profile_photo), mProgressBar, null, 0,
                    getIntent().getStringExtra(getString(R.string.selected_image)), null);
        }

        // if there is an bitmap attached as an extra, then it was chosen from the photo fragment
        if (intent.hasExtra(getString(R.string.selected_bitmap))) {
            Log.d(TAG, "getIncomingIntent: navigating to EditProfileFragment");
            setViewPager(pagerAdapter.getFragmentNumber(getString(R.string.edit_profile_fragment)));
            // set the new profile picture
            FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
            firebaseMethods.uploadNewPhotos(getString(R.string.profile_photo), mProgressBar, null, 0,
                    null, (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap)));
        }

        if (intent.hasExtra(getString(R.string.calling_activity_number))) {
            Log.d(TAG, "getIncomingIntent: received incoming intent from " + getString(R.string.profile_activity));
            setViewPager(pagerAdapter.getFragmentNumber(getString(R.string.edit_profile_fragment)));
        }
    }

    private void setupFragments() {
        pagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new EditProfileFragment(), getString(R.string.edit_profile_fragment)); // fragment 0
        pagerAdapter.addFragment(new SignOutFragment(), getString(R.string.sign_out_fragment)); // fragment 1
    }

    private void setViewPager(int fragmentNumber) {
        mRelativeLayout.setVisibility(View.GONE);
        Log.d(TAG, "setViewPager: navigating to fragment #: " + fragmentNumber);

        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(fragmentNumber);
    }

    private void setupSettingList() {
        Log.d(TAG, "setupSettingList: initializing 'Account Settings' list. ");
        ListView listView = findViewById(R.id.lvAccountSettings);

        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.edit_profile_fragment));
        options.add(getString(R.string.sign_out_fragment));

        ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: navigating to fragment# :" + position);
                setViewPager(position);
            }
        });
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
                getResources().getInteger(R.integer.profile_activity_number));

        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
    }
}
