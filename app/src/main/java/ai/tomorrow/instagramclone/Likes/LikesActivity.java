package ai.tomorrow.instagramclone.Likes;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.Utils.BottomNavigationViewHelper;
import ai.tomorrow.instagramclone.Utils.FirebaseMethods;
import ai.tomorrow.instagramclone.models.Follow;
import ai.tomorrow.instagramclone.models.LikePhoto;

public class LikesActivity extends AppCompatActivity {

    private static final String TAG = "LikesActivity";
    private static final int ACTIVITY_NUM = 3;

    private Context mContext = LikesActivity.this;

    //widgets
    private TabLayout mTab;
    private FrameLayout mFrameLayout;

    //vars
    private List<LikePhoto> mAllLikes = new ArrayList<>();
    private List<List<LikePhoto>> mLikes = new ArrayList<>();
    private List<String> mFollowingsUserID = new ArrayList<>();

    //firebase
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiy_likes);
        Log.d(TAG, "onCreate: starting");
        setupBottomNavigationView();

        mTab = findViewById(R.id.tabs);
        mFrameLayout = findViewById(R.id.container);

        myRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseMethods = new FirebaseMethods(mContext);

        getFollowingsLikedPosts();
    }


    private void getFollowingsLikedPosts(){
        Log.d(TAG, "getFollowingsLikedPosts: get all followings liked posts.");

        Query query = myRef.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mFollowingsUserID.clear();
                mAllLikes.clear();
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found following:" + singleSnapshot);

                    mFollowingsUserID.add(singleSnapshot.getValue(Follow.class).getUser_id());
                }
                Log.d(TAG, "onDataChange: mFollowingsUserID: " + mFollowingsUserID);

                for (int i = 0; i < mFollowingsUserID.size(); i++){
                    final int count = i;

                    Query query = myRef.child(getString(R.string.dbname_user_likes))
                            .child(mFollowingsUserID.get(i))
                            .child(getString(R.string.field_photo_likes));

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds: dataSnapshot.getChildren()){
                                Log.d(TAG, "onDataChange: found photo likes: " + ds);
                                LikePhoto likePhoto = ds.getValue(LikePhoto.class);
                                mAllLikes.add(likePhoto);
                            }
                            if (count == mFollowingsUserID.size() - 1){

                                getLikesForFollowingUsers();
                                Log.d(TAG, "onDataChange: mLikes: " + mLikes);
//                                updateListView();


                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    public void getLikesForFollowingUsers() {
        Log.d(TAG, "getLikesForFollowingUsers: sort likes and seperate them into groups according userID]");
        // sort likes by date created
        Collections.sort(mAllLikes, new Comparator<LikePhoto>() {
            @Override
            public int compare(LikePhoto o1, LikePhoto o2) {
                return o2.getDate_created().compareTo(o1.getDate_created());
            }
        });

        mLikes.clear();
        for (LikePhoto like: mAllLikes){
            if (mLikes.size() == 0 ||
                    !mLikes.get(mLikes.size() - 1).get(0).getLiked_by_user_id().equals(like.getLiked_by_user_id())){
                List<LikePhoto> likePhotos = new ArrayList<>();
                likePhotos.add(like);
                mLikes.add(likePhotos);
            } else {
                List<LikePhoto> temp = mLikes.get(mLikes.size() - 1);
                temp.add(like);
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
