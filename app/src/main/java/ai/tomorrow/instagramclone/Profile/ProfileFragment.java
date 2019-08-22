package ai.tomorrow.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

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

import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.Utils.BottomNavigationViewHelper;
import ai.tomorrow.instagramclone.Utils.FirebaseMethods;
import ai.tomorrow.instagramclone.Utils.GridImageAdapter;
import ai.tomorrow.instagramclone.Utils.UniversalImageLoader;
import ai.tomorrow.instagramclone.models.Photo;
import ai.tomorrow.instagramclone.models.UserAccountSettings;
import ai.tomorrow.instagramclone.models.UserSettings;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    public interface OnGridImageSelectedListener{
        void onGridImageSelected(Photo photo, int activityNumber);
    }
    OnGridImageSelectedListener mOnGridImageSelectedListener;

    //widgets
    private TextView mPosts, mFollowers, mFollowing, mDisplayName, mUsername, mWebsite, mDescription;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private RecyclerView gridView;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationView;
    private Context mContext;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    //vars
    private int mFollowingCount = 0;
    private int mFollowersCount = 0;
    private int mPostsCount = 0;
    private int mActivityNumber;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Log.d(TAG, "onCreateView: started");

        mDisplayName = (TextView) view.findViewById(R.id.display_name);
        mUsername = (TextView) view.findViewById(R.id.username);
        mWebsite = (TextView) view.findViewById(R.id.website);
        mDescription = (TextView) view.findViewById(R.id.description);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mPosts = (TextView) view.findViewById(R.id.tvPosts);
        mFollowers = (TextView) view.findViewById(R.id.tvFollowers);
        mFollowing = (TextView) view.findViewById(R.id.tvFollowing);
        mProgressBar = (ProgressBar) view.findViewById(R.id.profileProgressBar);
        gridView = (RecyclerView) view.findViewById(R.id.gridView);
        toolbar = (Toolbar) view.findViewById(R.id.profileToolBar);
        profileMenu = (ImageView) view.findViewById(R.id.profileMenu);
        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
        mContext = getActivity();
        mFirebaseMethods = new FirebaseMethods(getActivity());
        UniversalImageLoader.initImageLoader(mContext);
        mActivityNumber = this.getArguments().getInt(mContext.getString(R.string.calling_activity_number),
                mContext.getResources().getInteger(R.integer.profile_activity_number));

        setupBottomNavigationView();
        setupToolbar();
        setupFirebaseAuth();

        TextView editProfile = (TextView) view.findViewById(R.id.textEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to " + mContext.getString(R.string.edit_profile_fragment));
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                intent.putExtra(mContext.getString(R.string.calling_activity_number), mContext.getResources().getInteger(R.integer.profile_activity_number));
//                intent.putExtra(mContext.getString(R.string.calling_activity), mContext.getString(R.string.profile_activity));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


        return view;
    }

    @Override
    public void onAttach(Context context) {
        try {
            mOnGridImageSelectedListener = (OnGridImageSelectedListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }

        super.onAttach(context);
    }

    private void setFollowingCount(){
        Log.d(TAG, "setFollowingCount: set following count");
        mFollowingCount = 0;

        Query query = myRef.child(mContext.getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found following: " + singleSnapshot.getChildren());
                    mFollowingCount++;
                }
                mFollowing.setText(String.valueOf(mFollowingCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setFollowerCount(){
        Log.d(TAG, "setFollowingCount: set followers count");
        mFollowersCount = 0;

        Query query = myRef.child(mContext.getString(R.string.dbname_followers))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found follower: " + singleSnapshot.getChildren());
                    mFollowersCount++;
                }
                mFollowers.setText(String.valueOf(mFollowersCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setPostsCount(){
        Log.d(TAG, "setFollowingCount: set posts count");
        mPostsCount = 0;

        Query query = myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found post: " + singleSnapshot.getChildren());
                    mPostsCount++;
                }
                mPosts.setText(String.valueOf(mPostsCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setupGridView() {
        Log.d(TAG, "setupGridView: setting up image grid.");
        final ArrayList<Photo> photos = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    // In the Photo class, we have a List<Like>, but Firebase thinks it has a HashMap
                    // so we need to manually insert these into our photos
                    photos.add(mFirebaseMethods.getPhoto(singleSnapshot));
                }

                // sort photos by date created
                if (photos.size() > 0){
                    Collections.sort(photos, new Comparator<Photo>() {
                        @Override
                        public int compare(Photo o1, Photo o2) {
                            return o2.getDate_created().compareTo(o1.getDate_created());
                        }
                    });
                }

                ArrayList<String> imgURLs = new ArrayList<>();
                for (int i = 0; i < photos.size(); i++){
                    imgURLs.add(photos.get(i).getImage_path());
                }
                GridImageAdapter adapter = new GridImageAdapter(mContext, R.layout.layout_grid_imageview,
                        "", imgURLs, new GridImageAdapter.OnGridItemClickListener() {
                    @Override
                    public void OnGridItemClick(int clickedItemIndex) {
                        mOnGridImageSelectedListener.onGridImageSelected(photos.get(clickedItemIndex), mActivityNumber);
                    }
                });
                gridView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });

    }


    private void setProfileWidgets(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieved from firebase databse: " + userSettings.toString());
        Log.d(TAG, "setProfileWidgets: mUsername = " + mUsername);

//        User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());

        mProgressBar.setVisibility(View.GONE);
        setupGridView();
        setFollowerCount();
        setFollowingCount();
        setPostsCount();
    }


    /**
     * Responsible for setting up the profile toolbar
     */
    private void setupToolbar() {
        ((ProfileActivity) getActivity()).setSupportActionBar(toolbar);

        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AccountSettingsActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

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

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //retrieve user information from the database
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));


                //retrieve images for the user in question

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


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
