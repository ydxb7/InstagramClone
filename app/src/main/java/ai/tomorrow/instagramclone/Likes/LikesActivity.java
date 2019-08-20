package ai.tomorrow.instagramclone.Likes;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.util.List;

import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.Utils.BottomNavigationViewHelper;
import ai.tomorrow.instagramclone.Utils.FirebaseMethods;
import ai.tomorrow.instagramclone.models.Follow;
import ai.tomorrow.instagramclone.models.Like;
import ai.tomorrow.instagramclone.models.LikePhoto;
import de.hdodenhof.circleimageview.CircleImageView;

public class LikesActivity extends AppCompatActivity {

    private static final String TAG = "LikesActivity";
    private static final int ACTIVITY_NUM = 3;

    private Context mContext = LikesActivity.this;

    //widgets
    private TabLayout mTab;
    private FrameLayout mFrameLayout;

    //vars
    private List<LikePhoto> mLikes;
    private List<String> mFollowingsUserID;

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


    }


    private void getFollowingsLikedPosts(){
        Log.d(TAG, "getFollowingsLikedPosts: get all followings liked posts.");

        Query query = myRef.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mFollowingsUserID.clear();
                mLikes.clear();
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found following:" + singleSnapshot);

                    mFollowingsUserID.add(singleSnapshot.getValue(Follow.class).getUser_id());
                }

                for (String userID: mFollowingsUserID){
                    Query query = myRef.child(getString(R.string.dbname_user_likes))
                            .child(userID)
                            .child(getString(R.string.field_photo_likes));

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds: dataSnapshot.getChildren()){
                                Log.d(TAG, "onDataChange: found photo likes: " + ds);
                                String photoID = ds.getKey();

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
