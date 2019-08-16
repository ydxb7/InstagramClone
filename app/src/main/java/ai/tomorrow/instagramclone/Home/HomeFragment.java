package ai.tomorrow.instagramclone.Home;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.Utils.MainfeedListAdapter;
import ai.tomorrow.instagramclone.models.Comment;
import ai.tomorrow.instagramclone.models.Like;
import ai.tomorrow.instagramclone.models.Photo;

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
    private static final int LOAD_COUNT = 5;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: started.");
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mContext = getActivity();
        mListView = (ListView) view.findViewById(R.id.listView);
        myRef = FirebaseDatabase.getInstance().getReference();

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
                        Photo photo = getPhoto(singleSnapshot);
                        mPhotos.add(photo);
                    }
                    if (count == mFollowingUserIDs.size() - 1) {
                        displayPhotos();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }

    }

    private void displayPhotos(){
        Log.d(TAG, "displayPhotos: sort photos and set listView");
        // sort photos
        if (mPhotos.size() > 0){
            Collections.sort(mPhotos, new Comparator<Photo>() {
                @Override
                public int compare(Photo o1, Photo o2) {
                    return o2.getDate_created().compareTo(o1.getDate_created());
                }
            });

            int iterations = LOAD_COUNT;
            if (mPhotos.size() < LOAD_COUNT){
                iterations = mPhotos.size();
            }

            for (int i = 0; i < iterations; i++){
                mPaginatedPhotos.add(mPhotos.get(i));
            }

            // set listView
            mAdapter = new MainfeedListAdapter(mContext, R.layout.layout_mainfeed_listitem, mPaginatedPhotos);
            mListView.setAdapter(mAdapter);
        }
    }

    public void loadMorePhotos(){
        Log.d(TAG, "loadMorePhotos: loading more photos.");

        if (mPaginatedPhotos.size() < mPhotos.size()){
            int iterations = Math.min(mPhotos.size() - mPaginatedPhotos.size(), LOAD_COUNT);

            int start = mPaginatedPhotos.size();
            for (int i = start; i < start + iterations; i++){
                mPaginatedPhotos.add(mPhotos.get(i));
            }
            mAdapter.notifyDataSetChanged();
        }

    }

    private Photo getPhoto(DataSnapshot singleSnapshot) {
        Photo photo = new Photo();
        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
        photo.setCaption(objectMap.get(mContext.getString(R.string.field_caption)).toString());
        photo.setDate_created(objectMap.get(mContext.getString(R.string.field_date_created)).toString());
        photo.setImage_path(objectMap.get(mContext.getString(R.string.field_image_path)).toString());
        photo.setTags(objectMap.get(mContext.getString(R.string.field_tags)).toString());
        photo.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
        photo.setPhoto_id(objectMap.get(mContext.getString(R.string.field_photo_id)).toString());

        List<Like> likesList = new ArrayList<>();
        for (DataSnapshot ds : singleSnapshot.child(mContext.getString(R.string.field_likes)).getChildren()) {
            Like like = new Like();
            like.setUser_id(ds.getValue(Like.class).getUser_id());
            likesList.add(like);
        }

        List<Comment> commentList = new ArrayList<>();
        for (DataSnapshot ds : singleSnapshot.child(mContext.getString(R.string.field_comments)).getChildren()) {
            Comment comment = new Comment();
            comment.setUser_id(ds.getValue(Comment.class).getUser_id());
            comment.setDate_created(ds.getValue(Comment.class).getDate_created());
            comment.setComment(ds.getValue(Comment.class).getComment());
            commentList.add(comment);
        }

        photo.setLikes(likesList);
        photo.setComments(commentList);
        return photo;
    }
}
