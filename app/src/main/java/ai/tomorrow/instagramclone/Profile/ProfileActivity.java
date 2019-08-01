package ai.tomorrow.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.Utils.BottomNavigationViewHelper;
import ai.tomorrow.instagramclone.Utils.GridImageAdapter;
import ai.tomorrow.instagramclone.Utils.UniversalImageLoader;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final int ACTIVITY_NUM = 4;

    private Context mContext = ProfileActivity.this;
    private ProgressBar mProgressBar;
    private ImageView profilePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: starting");

        setupBottomNavigationView();
        setupToolbar();
        setupActivityWidgets();
        setProfileImage();

        tempGridSetup();
    }

    private void tempGridSetup(){
        ArrayList<String> imgURLs = new ArrayList<>();
        imgURLs.add("https://sjbz-fd.zol-img.com.cn/t_s208x312c5/g5/M00/01/06/ChMkJ1w3FnmIE9dUAADdYQl3C5IAAuTxAKv7x8AAN15869.jpg");
        imgURLs.add("http://www.baojiabao.com/bjbnews/pic/20180404/6405f2b36c6ae5a2799980b43156d90f.jpg");
        imgURLs.add("https://img.pc841.com/2018/0905/20180905031728569.jpg");
        imgURLs.add("https://img0.sc115.com/uploads3/sc/jpgs/1904/zzpic17543_sc115.com.jpg");
        imgURLs.add("https://www.feizl.com/upload2007/allimg/190624/19592H5L-5.jpg");
        imgURLs.add("http://p9.pstatp.com/large/pgc-image/5552f943362d475c88b3f5cc3ee74a49");
        imgURLs.add("http://p99.pstatp.com/large/pgc-image/15407759708280197df4b71");
        imgURLs.add("https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/1_jiuzhaigou_valley_national_park_wu_hua_hai.jpg/250px-1_jiuzhaigou_valley_national_park_wu_hua_hai.jpg");
        imgURLs.add("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSfbvMVzqmGmk7TXKxkBplNpybfQOYNGyLhyM4aDM_nIpjj729zGw");
        imgURLs.add("http://pic.9ht.com/up/2018-4/2018041315500518788.jpg");
        imgURLs.add("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRSpm6jttKtC2mbh9HPYYDSTmpALPz5ngbAF9teFwBG0-vVS7mX");
        imgURLs.add("https://cdn1-digiphoto.techbang.com/system/excerpt_images/9862/inpage/3487c5f3a19271d9fab1238faf518293.jpg?1548392543");

        setupImageGrid(imgURLs);
    }



    private void setupImageGrid(ArrayList<String> imgURLs){
        GridView gridView = (GridView) findViewById(R.id.gridView);

        GridImageAdapter adapter = new GridImageAdapter(mContext, R.layout.layout_grid_imageview, "", imgURLs);
        gridView.setAdapter(adapter);
    }


    private void setProfileImage(){
        String imgURL = "http://k2.jsqq.net/uploads/allimg/1711/17_171129092304_1.jpg";
        UniversalImageLoader.setImage(imgURL, profilePhoto, null, "");
    }

    private void setupActivityWidgets(){
        mProgressBar = (ProgressBar) findViewById(R.id.profileProgressBar);
        mProgressBar.setVisibility(View.GONE);

        profilePhoto = (ImageView) findViewById(R.id.profile_photo);
    }



    /**
     * Responsible for setting up the profile toolbar
     */
    private void setupToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.profileToolBar);
        setSupportActionBar(toolbar);

        ImageView profileMenu = (ImageView) findViewById(R.id.profileMenu);
        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(intent);
            }
        });

    }


    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }


}
