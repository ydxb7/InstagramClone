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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.Utils.CommentListAdapter;
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
    private ArrayList<Photo> mPhotos = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: started.");
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mContext = getActivity();
        mListView = (ListView) view.findViewById(R.id.listView);

        setupListView();
        return view;
    }

    private void setupListView() {
        Log.d(TAG, "setupListView: setting up comments.");

        Query query = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mPhotos.clear();

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found photo: " + singleSnapshot.getValue());
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

                    mPhotos.add(photo);
//                    // In the Photo class, we have a List<Like>, but Firebase thinks it has a HashMap
//                    // so we need to manually insert these into our photos
////                    photos.add(singleSnapshot.getValue(Photo.class));
//                    Comment comment = new Comment();
//                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
//
//                    comment.setComment(objectMap.get(mContext.getString(R.string.field_comment)).toString());
//                    comment.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
//                    comment.setDate_created(objectMap.get(mContext.getString(R.string.field_date_created)).toString());
//
//                    List<Like> likesList = new ArrayList<>();
//                    for (DataSnapshot ds : singleSnapshot.child(mContext.getString(R.string.field_likes)).getChildren()) {
//                        Like like = new Like();
//                        like.setUser_id(ds.getValue(Like.class).getUser_id());
//                        likesList.add(like);
//                    }
//                    comment.setLikes(likesList);
//                    mComments.add(comment);
                }

                MainfeedListAdapter adapter = new MainfeedListAdapter(mContext, R.layout.layout_mainfeed_listitem, mPhotos);
                mListView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });


    }
}
