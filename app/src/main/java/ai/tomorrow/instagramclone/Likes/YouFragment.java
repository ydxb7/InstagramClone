package ai.tomorrow.instagramclone.Likes;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import ai.tomorrow.instagramclone.Utils.YouLikesListAdapter;
import ai.tomorrow.instagramclone.models.LikePhoto;
import ai.tomorrow.instagramclone.models.LikeYou;
import ai.tomorrow.instagramclone.models.Photo;

public class YouFragment extends Fragment {
    private static final String TAG = "YouFragment";

    //widgets
    private ListView mListView;

    //vars
    private Context mContext;
    private List<LikeYou> mLikeYous = new ArrayList<>();
    private YouLikesListAdapter mAdapter;

    //firebase
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: started.");
        View view = inflater.inflate(R.layout.fragment_you, container, false);

        mContext = getActivity();
        mListView = view.findViewById(R.id.listView);
        myRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseMethods = new FirebaseMethods(mContext);

        mAdapter = new YouLikesListAdapter(mContext, R.layout.layout_likes_you_post_listitem, mLikeYous);
        mListView.setAdapter(mAdapter);

        getLikes();

        return view;
    }

    private void getLikes(){
        Log.d(TAG, "getLikes: get likes.");
        mLikeYous.clear();

        Query query = myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Photo photo = mFirebaseMethods.getPhoto(singleSnapshot);

                    for (LikePhoto like: photo.getLikes_photo()){
                        if (!like.getLiked_by_user_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            LikeYou likeYou = new LikeYou();
                            likeYou.setDate_created(like.getDate_created());
                            likeYou.setLiked_by_user_id(like.getLiked_by_user_id());
                            likeYou.setPhoto_id(like.getPhoto_id());
                            mLikeYous.add(likeYou);
                        }
                    }
                }

                // sort likes by date created
                Collections.sort(mLikeYous, new Comparator<LikeYou>() {
                    @Override
                    public int compare(LikeYou o1, LikeYou o2) {
                        return o2.getDate_created().compareTo(o1.getDate_created());
                    }
                });

                mAdapter.setNewData(mLikeYous);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





    }


}
