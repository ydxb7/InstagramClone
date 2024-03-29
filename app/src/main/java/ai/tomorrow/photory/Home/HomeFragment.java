package ai.tomorrow.photory.Home;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ai.tomorrow.photory.R;
import ai.tomorrow.photory.Utils.FirebaseMethods;
import ai.tomorrow.photory.Utils.MainfeedListAdapter;
import ai.tomorrow.photory.models.Photo;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    //widgets
    private ListView mListView;

    //vars
    private Context mContext;
    private MainfeedListAdapter mAdapter;
    private ArrayList<Photo> mPhotos = new ArrayList<>();
    private ArrayList<Photo> mPaginatedPhotos = new ArrayList<>();
    private ArrayList<String> mFollowingUserIDs = new ArrayList<>();
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private static final int LOAD_COUNT = 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: started.");
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mContext = getActivity();
        mListView = view.findViewById(R.id.listView);
        myRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseMethods = new FirebaseMethods(mContext);

        getFollowing();

        return view;
    }

    private void getFollowing() {
        Log.d(TAG, "getFollowing: get all following user id");
        Query query = myRef.child(mContext.getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mFollowingUserIDs.clear();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found following: " + singleSnapshot.getValue());
                    mFollowingUserIDs.add(singleSnapshot.child(mContext.getString(R.string.field_user_id)).getValue().toString());
                }
                mFollowingUserIDs.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                // get photos
                getPhotos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled.");
            }
        });
    }

    private void getPhotos() {
        Log.d(TAG, "getPhotos: get photos.");
        mPhotos.clear();
        for (int i = 0; i < mFollowingUserIDs.size(); i++) {
            Log.d(TAG, "getPhotos: get photos for: " + mFollowingUserIDs.get(i));

            final int count = i;
            String userID = mFollowingUserIDs.get(i);

            Query query = myRef.child(mContext.getString(R.string.dbname_user_photos))
                    .child(userID);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: found photo: " + singleSnapshot.getValue());
                        Photo photo = mFirebaseMethods.getPhoto(singleSnapshot);
                        mPhotos.add(photo);
                    }
                    if (count == mFollowingUserIDs.size() - 1) {
                        displayPhotos();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled.");
                }
            });
        }
    }

    private void displayPhotos() {
        Log.d(TAG, "displayPhotos: sort photos and set listView");
        // sort photos
        if (mPhotos.size() > 0) {
            Collections.sort(mPhotos, new Comparator<Photo>() {
                @Override
                public int compare(Photo o1, Photo o2) {
                    return o2.getDate_created().compareTo(o1.getDate_created());
                }
            });

            int iterations = LOAD_COUNT;
            if (mPhotos.size() < LOAD_COUNT) {
                iterations = mPhotos.size();
            }

            for (int i = 0; i < iterations; i++) {
                mPaginatedPhotos.add(mPhotos.get(i));
            }

            // set listView
            mAdapter = new MainfeedListAdapter(mContext, R.layout.layout_mainfeed_listitem, mPaginatedPhotos);
            mListView.setAdapter(mAdapter);
        }
    }

    public void loadMorePhotos() {
        Log.d(TAG, "loadMorePhotos: loading more photos.");

        if (mPaginatedPhotos.size() < mPhotos.size()) {
            int iterations = Math.min(mPhotos.size() - mPaginatedPhotos.size(), LOAD_COUNT);

            int start = mPaginatedPhotos.size();
            for (int i = start; i < start + iterations; i++) {
                mPaginatedPhotos.add(mPhotos.get(i));
            }
            mAdapter.notifyDataSetChanged();
        }
    }
}
