package ai.tomorrow.instagramclone.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.Utils.BottomNavigationViewHelper;
import ai.tomorrow.instagramclone.Utils.Permissions;
import ai.tomorrow.instagramclone.Utils.SectionsPagerAdapter;
import ai.tomorrow.instagramclone.Utils.SectionsStatePagerAdapter;

public class ShareActivity extends AppCompatActivity {

    private static final String TAG = "ShareActivity";

    // constants
    private static final int ACTIVITY_NUM = 2;
    private static final int VERIFY_PERMISSION_REQUEST = 1;

    private ViewPager mViewPager;

    private Context mContext = ShareActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiy_share);
        Log.d(TAG, "onCreate: starting");

        if (Permissions.checkPermissionArray(this, Permissions.PERMISSIONS)){
            // all permissions are granted
            setupViewPager();
        } else {
            // verify permissions
            Log.d(TAG, "verifyPermissions: verifying permissions");
            ActivityCompat.requestPermissions(
                    this,
                    Permissions.PERMISSIONS,
                    VERIFY_PERMISSION_REQUEST
            );
        }

//        setupBottomNavigationView();
    }

    /**
     * if it getFlags != 0: it's coming from EditProfileFragment, otherwise it's the root Task
     * @return
     */
    public int getTask(){
        Log.d(TAG, "getTask: getTask: TASK: " + getIntent().getFlags());
        return getIntent().getFlags();
    }



    /**
     * return the current tab number
     * 0 = GalleryFragment
     * 1 = PhotoFragment
     * @return
     */
    public int getCurrentTabNumber(){
        return mViewPager.getCurrentItem();
    }

    /**
     * setup viewpager for manager the tabs
     */
    private void setupViewPager(){
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new PhotoFragment());

        mViewPager = (ViewPager) findViewById(R.id.viewpager_container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText(getString(R.string.gallery));
        tabLayout.getTabAt(1).setText(getString(R.string.photo));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case VERIFY_PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                boolean isAllGranted = true;
                if (grantResults.length > 0){
                    for (int result: grantResults){
                        if (result != PackageManager.PERMISSION_GRANTED){
                            isAllGranted = false;
                        }
                    }
                }

                if (isAllGranted){
                    setupViewPager();
                } else {
                    finish();
                }
            }

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
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
