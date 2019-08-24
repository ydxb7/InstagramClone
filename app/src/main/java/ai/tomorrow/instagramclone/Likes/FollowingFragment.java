package ai.tomorrow.instagramclone.Likes;

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
import java.util.List;

import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.Utils.FirebaseMethods;
import ai.tomorrow.instagramclone.Utils.FollowingLikesListAdapter;
import ai.tomorrow.instagramclone.models.Follow;
import ai.tomorrow.instagramclone.models.LikePhoto;

public class FollowingFragment extends Fragment {
    private static final String TAG = "FollowingFragment";

    //widgets
    private ListView mListView;

    //vars
    private Context mContext;
    private List<LikePhoto> mAllLikes = new ArrayList<>();
    private List<List<LikePhoto>> mLikes = new ArrayList<>();
    private List<String> mFollowingsUserID = new ArrayList<>();
    private FollowingLikesListAdapter mAdapter;

    //firebase
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: started.");
        View view = inflater.inflate(R.layout.fragment_following, container, false);

        mContext = getActivity();
        mListView = view.findViewById(R.id.listView);
        myRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseMethods = new FirebaseMethods(mContext);

        getFollowingsLikedPosts();

        mAdapter = new FollowingLikesListAdapter(mContext, R.layout.layout_following_likes_post_listitem, mLikes);
        mListView.setAdapter(mAdapter);

        return view;
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
                                mAdapter.setNewData(mLikes);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d(TAG, "onCancelled.");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled.");
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
}
