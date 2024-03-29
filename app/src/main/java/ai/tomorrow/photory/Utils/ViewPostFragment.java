package ai.tomorrow.photory.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ai.tomorrow.photory.Likes.LikesActivity;
import ai.tomorrow.photory.Profile.ProfileActivity;
import ai.tomorrow.photory.R;
import ai.tomorrow.photory.models.LikePhoto;
import ai.tomorrow.photory.models.Photo;
import ai.tomorrow.photory.models.User;
import ai.tomorrow.photory.models.UserAccountSettings;

public class ViewPostFragment extends Fragment {

    private static final String TAG = "ViewPostFragment";

    public interface OnCommentThreadSelectedListener {
        void onCommentThreadSelectedListener(Photo photo);
    }

    OnCommentThreadSelectedListener mOnCommentThreadSelectedListener;

    public ViewPostFragment() {
        super();
        setArguments(new Bundle());
    }

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    //widgets
    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationView;
    private TextView mBackLabel, mCaption, mUsername, mTimestamp, mLikes, mComments;
    private ImageView mBackArrow, mEllipses, mHeartRed, mHeartWhite, mProfileImage, mComment;

    //vars
    private Context mContext;
    private Photo mPhoto;
    private int mActivityNumber = 4;
    private String photoUsername = "";
    private String profilePhotoUrl = "";
    private UserAccountSettings mUserAccountSettings;
    private User mUser;
    private GestureDetector mGestureDetector;
    private Heart mHeart;
    private Boolean mLikedByCurrentUser;
    private StringBuilder mUsers;
    private String mLikesString = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: started.");
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        mContext = getActivity();
        mPostImage = view.findViewById(R.id.post_image);
        bottomNavigationView = view.findViewById(R.id.bottomNavViewBar);
        mBackArrow = view.findViewById(R.id.imageBackArrow);
        mBackLabel = view.findViewById(R.id.tvBackLabel);
        mCaption = view.findViewById(R.id.image_caption);
        mUsername = view.findViewById(R.id.username);
        mTimestamp = view.findViewById(R.id.image_time_posted);
        mEllipses = view.findViewById(R.id.ivEllipses);
        mHeartRed = view.findViewById(R.id.image_heart_red);
        mHeartWhite = view.findViewById(R.id.image_heart);
        mProfileImage = view.findViewById(R.id.profile_photo);
        mLikes = view.findViewById(R.id.image_likes);
        mComment = view.findViewById(R.id.speech_bubble);
        mComments = view.findViewById(R.id.image_comments_link);
        UniversalImageLoader.initImageLoader(mContext);

        mHeart = new Heart(mHeartWhite, mHeartRed);
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());
        mFirebaseMethods = new FirebaseMethods(getActivity());

        setupFirebaseAuth();
        setupBottomNavigationView();

        return view;
    }

    private void init() {
        try {
            //mPhoto = getPhotoFromBundle();
            UniversalImageLoader.setImage(getPhotoFromBundle().getImage_path(), mPostImage, null, "");
            mActivityNumber = getActivityNumFromBundle();
            String photo_id = getPhotoFromBundle().getPhoto_id();

            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_photos))
                    .orderByChild(getString(R.string.field_photo_id))
                    .equalTo(photo_id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Photo newPhoto = mFirebaseMethods.getPhoto(singleSnapshot);

                        mPhoto = newPhoto;

                        setupWidgets();
                        getLikesString();
                        getPhotoDetails();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: query cancelled.");
                }
            });

        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }
    }

    /**
     * solve the problem: fragment not attached to activity
     */
    @Override
    public void onResume() {
        super.onResume();
        // check if the fragment is added
        if (isAdded()) {
            init();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnCommentThreadSelectedListener = (OnCommentThreadSelectedListener) getActivity();
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }

    private void getLikesString() {
        Log.d(TAG, "getLikesString: getting likes string");

        mLikedByCurrentUser = false;
        Query query = myRef.child(mContext.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_likes_photo));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: there is no likes node in photo: " + mPhoto.getPhoto_id());
                    mLikesString = "";
                    setupLikes();
                } else {
                    final List<LikePhoto> likes = new ArrayList<>();

                    // get all likes for this photo
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: found Like: " + singleSnapshot);
                        LikePhoto like = singleSnapshot.getValue(LikePhoto.class);
                        likes.add(like);
                        Log.d(TAG, "onDataChange: like.getUser_id(): " + like.getLiked_by_user_id());
                        Log.d(TAG, "onDataChange: current user id: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
                        if (like.getLiked_by_user_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            Log.d(TAG, "onDataChange: likeByCurrentUser");
                            mLikedByCurrentUser = true;
                        }
                    }

                    // sort likes by date created
                    Collections.sort(likes, new Comparator<LikePhoto>() {
                        @Override
                        public int compare(LikePhoto o1, LikePhoto o2) {
                            return o2.getDate_created().compareTo(o1.getDate_created());
                        }
                    });

                    // get likes usernames list
                    final ArrayList<String> likesUsername = new ArrayList<>();

                    for (int i = 0; i < Math.min(likes.size(), 4); i++) {
                        final int count = i;
                        LikePhoto eachLike = likes.get(i);
                        Query query = myRef.child(mContext.getString(R.string.dbname_users))
                                .orderByChild(mContext.getString(R.string.field_user_id))
                                .equalTo(eachLike.getLiked_by_user_id());

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot);
                                    likesUsername.add(singleSnapshot.getValue(User.class).getUsername());
                                }
                                if (likesUsername.size() == Math.min(likes.size(), 4)) {
                                    mLikesString = StringManipulation.getLikesString(likes.size(), likesUsername);
                                    setupLikes();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled.");
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled.");
            }
        });
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: double tap detected.");

            mHeart.toggleLike();
            if (mLikedByCurrentUser) {
                mFirebaseMethods.removePhotoLike(mPhoto.getPhoto_id(), mPhoto.getUser_id());
            } else {
                mFirebaseMethods.addPhotoNewLike(mPhoto.getPhoto_id(), mPhoto.getUser_id());
            }
            getLikesString();

            return true;
        }
    }

    private void getPhotoDetails() {
        Log.d(TAG, "getPhotoDetails: retrieving photo details.");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mPhoto.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    mUserAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                    UniversalImageLoader.setImage(mUserAccountSettings.getProfile_photo(), mProfileImage, null, "");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }

    private void setupWidgets() {
        String timestampDiff = StringManipulation.getTimeStampDifference(mPhoto.getDate_created());
        mTimestamp.setText(timestampDiff);

        Query query = myRef.child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(mPhoto.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot);
                    mUser = singleSnapshot.getValue(User.class);
                }
                mUsername.setText(mUser.getUsername());
                mProfileImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "onClick: navigate to view profile.");
                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.putExtra(mContext.getString(R.string.calling_activity_number),
                                getArguments().getInt(mContext.getString(R.string.calling_activity_number),
                                        getResources().getInteger(R.integer.home_activity_number)));
                        intent.putExtra(mContext.getString(R.string.selected_user), mUser);
                        mContext.startActivity(intent);
                        ((Activity) mContext).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled.");
            }
        });

        setupLikes();

        mComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to comments thread");

                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto);
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                getActivity().getSupportFragmentManager().popBackStack();

                Bundle bundle = getArguments();
                if (mContext instanceof LikesActivity
                        && bundle != null
                        && bundle.getInt(mContext.getString(R.string.calling_activity_number), -1) ==
                        mContext.getResources().getInteger(R.integer.likes_activity_number)) {
                    ((LikesActivity) mContext).showRelativeLayout();
                }
            }
        });

        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto);
            }
        });

        Query query2 = myRef.child(mContext.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_likes_photo))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mLikedByCurrentUser = true;
                    mHeartWhite.setVisibility(View.GONE);
                    mHeartRed.setVisibility(View.VISIBLE);
                } else {
                    mLikedByCurrentUser = false;
                    mHeartWhite.setVisibility(View.VISIBLE);
                    mHeartRed.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled.");
            }
        });

        mHeartRed.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch: red heart touch detected.");
                return mGestureDetector.onTouchEvent(event);
            }
        });

        mHeartWhite.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch: white heart touch detected.");
                return mGestureDetector.onTouchEvent(event);
            }
        });
    }

    private void setupLikes() {
        if (mLikesString.equals("")) {
            mLikes.setVisibility(View.GONE);
        } else {
            mLikes.setVisibility(View.VISIBLE);
            mLikes.setText(mLikesString);
        }
        mCaption.setText(mPhoto.getCaption());

        if (mPhoto.getComments().size() > 0) {
            mComments.setVisibility(View.VISIBLE);
            mComments.setText("View all " + mPhoto.getComments().size() + " comments");
        } else {
            mComments.setVisibility(View.GONE);
            mComments.setText("");
        }
    }

    /**
     * retrieve the activity number from the incoming bundle from profileActivity interface
     *
     * @return
     */
    private int getActivityNumFromBundle() {
        Log.d(TAG, "getActivityNumFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getInt(getString(R.string.activity_number));
        } else {
            return 0;
        }
    }

    /**
     * retrieve the photo from the incoming bundle from profileActivity interface
     *
     * @return
     */
    private Photo getPhotoFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.photo));
        } else {
            return null;
        }
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        Bundle bundle = this.getArguments();

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(getActivity(), getActivity(), bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        try {
            mActivityNumber = bundle.getInt(mContext.getString(R.string.calling_activity_number), mContext.getResources().getInteger(R.integer.profile_activity_number));
        } catch (NullPointerException e) {
            Log.d(TAG, "setupBottomNavigationView: NullPointerException" + e.getMessage());
        }
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
    }

       /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();


                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
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
