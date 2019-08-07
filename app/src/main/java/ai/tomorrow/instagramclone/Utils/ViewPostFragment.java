package ai.tomorrow.instagramclone.Utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.Utils.BottomNavigationViewHelper;
import ai.tomorrow.instagramclone.Utils.GridImageAdapter;
import ai.tomorrow.instagramclone.Utils.SquareImageView;
import ai.tomorrow.instagramclone.Utils.StringManipulation;
import ai.tomorrow.instagramclone.Utils.UniversalImageLoader;
import ai.tomorrow.instagramclone.models.Like;
import ai.tomorrow.instagramclone.models.Photo;
import ai.tomorrow.instagramclone.models.User;
import ai.tomorrow.instagramclone.models.UserAccountSettings;
import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPostFragment extends Fragment {
    private static final String TAG = "ViewPostFragment";

    public ViewPostFragment(){
        setArguments(new Bundle());
    }

    //widgets
    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationView;
    private TextView mBackLabel, mCaption, mUsername, mTimestamp, mLikes;
    private ImageView mBackArrow, mEllipses, mHeartRed, mHeartWhite, mProfileImage;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userID;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;

    // vars
    private Photo mPhoto;
    private int mActivityNumber = 0;
    private Context mContext;
    private String photoUsername;
    private String mProfileUrl;
    private UserAccountSettings mUserAccountSettings;
    private GestureDetector mGestureDetector;
    private Heart mHeart;
    private boolean mLikedByCurrentUser;
    private StringBuilder mUser;
    private String mLikesString = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        Log.d(TAG, "onCreateView: started");
        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
        mContext = getActivity();
        mPostImage = (SquareImageView) view.findViewById(R.id.post_image);
        mBackArrow = (ImageView) view.findViewById(R.id.backArrow);
        mBackLabel = (TextView) view.findViewById(R.id.tvBackLabel);
        mCaption = (TextView) view.findViewById(R.id.image_caption);
        mUsername = (TextView) view.findViewById(R.id.username);
        mTimestamp = (TextView) view.findViewById(R.id.image_time_posted);
        mEllipses = (ImageView) view.findViewById(R.id.ivEllipses);
        mHeartRed = (ImageView) view.findViewById(R.id.image_heart_red);
        mHeartWhite = (ImageView) view.findViewById(R.id.image_heart);
        mLikes = (TextView) view.findViewById(R.id.image_likes);
        mProfileImage = (CircleImageView) view.findViewById(R.id.profile_photo);

        mHeartWhite.setVisibility(View.VISIBLE);
        mHeartRed.setVisibility(View.GONE);
        mHeart = new Heart(mHeartWhite, mHeartRed);
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());

        try {
            mPhoto = getPhotoFromBundle();
            UniversalImageLoader.setImage(mPhoto.getImage_path(), mPostImage, null, "");
            mActivityNumber = getActivityNumberFromBundle();
            getPhotoDetails();
            getLikesString();
        }catch (NullPointerException e){
            Log.d(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }

        setupBottomNavigationView();
        setupFirebaseAuth();

        return view;
    }



    private void getLikesString(){
        Log.d(TAG, "getLikesString: getting likes string");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUser = new StringBuilder();
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference
                            .child(getString(R.string.dbname_users))
                            .orderByChild(getString(R.string.field_user_id))
                            .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mUser = new StringBuilder();
                            for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                                Log.d(TAG, "onDataChange: found like: " + singleSnapshot.getValue(User.class).getUsername());
                                mUser.append(singleSnapshot.getValue(User.class).getUsername());
                                mUser.append(",");
                            }

                            String[] splitUsers = mUser.toString().split(",");
                            if (mUser.toString().contains(mUserAccountSettings.getUsername())){
                                mLikedByCurrentUser = true;
                            }else {
                                mLikedByCurrentUser = false;
                            }

                            int length = splitUsers.length;
                            if (length == 1){
                                mLikesString = "Liked by " + splitUsers[0];
                            }else if (length == 2){
                                mLikesString = "Liked by " + splitUsers[0]
                                        + " and " + splitUsers[1];
                            }else if (length == 3){
                                mLikesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + " and " + splitUsers[2];
                            }else if (length == 4){
                                mLikesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + ", " + splitUsers[2]
                                        + " and " + splitUsers[3];
                            }else if (length > 4){
                                mLikesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + ", " + splitUsers[2]
                                        + " and " + (length - 3) + " others";;
                            }
                            setupWidgets();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                if (!dataSnapshot.exists()){
                    mLikesString = "";
                    mLikedByCurrentUser = false;
                    setupWidgets();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: double tap detected.");

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(getString(R.string.dbname_user_photos))
                    .child(mPhoto.getPhoto_id())
                    .child(getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){

                        String keyId = singleSnapshot.getKey();
                        //case1: The user already liked the photo, remove like
                        if (mLikedByCurrentUser &&
                                singleSnapshot.getValue(Like.class).getUser_id()
                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            myRef.child(getString(R.string.dbname_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyId)
                                    .removeValue();

                            myRef.child(getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyId)
                                    .removeValue();

                            mHeart.toggleLike();
                            getLikesString();
                        }
                        //case2: The user has not liked the photo
                        else if (!mLikedByCurrentUser){
                           //add new like
                            addNewLike();
                        }
                    }
                    if (!dataSnapshot.exists()){
                        //add new like
                        addNewLike();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            return true;
        }
    }

    private void addNewLike(){
        Log.d(TAG, "addNewLike: adding new like");
        String newLikeId = myRef.push().getKey();

        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);

        myRef.child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(like);

        mHeart.toggleLike();
        getLikesString();
    }


    private void getPhotoDetails(){
        Log.d(TAG, "getPhotoDetails: getting user account settings");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mPhoto.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    mUserAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                }
//                setupWidgets();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }


    private void setupWidgets(){
        Log.d(TAG, "setupWidgets: setting up widgets: " + mUserAccountSettings.toString());
        String timestampDiff = getTimeStampDifference();
        if (!timestampDiff.equals("0")){
            mTimestamp.setText(timestampDiff + " DAYS AGO");
        }else {
            mTimestamp.setText("TODAY");
        }
        UniversalImageLoader.setImage(mUserAccountSettings.getProfile_photo(), mProfileImage, null, "");
        mUsername.setText(mUserAccountSettings.getUsername());
        mLikes.setText(mLikesString);

        if (mLikedByCurrentUser){
            mHeartRed.setVisibility(View.VISIBLE);
            mHeartWhite.setVisibility(View.GONE);
        } else {
            mHeartRed.setVisibility(View.GONE);
            mHeartWhite.setVisibility(View.VISIBLE);
        }

        mHeartRed.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });

        mHeartWhite.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });
    }

    /**
     * return a string presenting the number of days ago the post was made.
     * @return
     */
    private String getTimeStampDifference(){
        Log.d(TAG, "getTimeStampDifference: getting timestamp difference");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimeStamp = mPhoto.getDate_created();
        try {
            timestamp = sdf.parse(photoTimeStamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60/ 24)));
        }catch (ParseException e){
            Log.d(TAG, "getTimeStampDifference: ParseException: " + e.getMessage());
        }
        return difference;
    }


    /**
     * return the activityNumber from the incoming bundle from profileActivity interface
     * @return
     */
    private int getActivityNumberFromBundle(){
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());

        Bundle bundle = getArguments();
        if (bundle != null){
            return bundle.getInt(getString(R.string.activity_number));
        }else {
            return 0;
        }
    }

    /**
     * return the photo from the incoming bundle from profileActivity interface
     * @return
     */
    private Photo getPhotoFromBundle(){
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());

        Bundle bundle = getArguments();
        if (bundle != null){
            return bundle.getParcelable(getString(R.string.photo));
        }else {
            return null;
        }
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext, getActivity(), bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
    }

    /**
     * -------------------------------- firebase --------------------------
     */

    /**
     * setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

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
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
