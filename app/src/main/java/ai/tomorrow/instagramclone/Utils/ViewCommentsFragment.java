package ai.tomorrow.instagramclone.Utils;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.models.Comment;
import ai.tomorrow.instagramclone.models.Photo;

public class ViewCommentsFragment extends Fragment {

    private static final String TAG = "ViewCommentsFragment";

    public ViewCommentsFragment(){
        setArguments(new Bundle());
    }

    //widgets
    private ImageView mBackArrow, mCheckMard;
    private EditText mComment;
    private ListView mListView;

    //vars
    private Photo mPhoto;
    private ArrayList<Comment> mComments;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);
        Log.d(TAG, "onCreateView: started.");

        mBackArrow = view.findViewById(R.id.backArrow);
        mCheckMard = view.findViewById(R.id.ivPostComment);
        mComment = view.findViewById(R.id.comment);
        mListView = view.findViewById(R.id.listView);


        try {
            mPhoto = getPhotoFromBundle();
        }catch (NullPointerException e){
            Log.d(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }

        mComments = new ArrayList<>();
        Comment firstComment = new Comment();
        firstComment.setUser_id(mPhoto.getUser_id());
        firstComment.setComment(mPhoto.getCaption());
        firstComment.setDate_created(mPhoto.getDate_created());

        mComments.add(firstComment);

        CommentListAdapter adapter = new CommentListAdapter(getActivity(), R.layout.layout_comment, mComments);
        mListView.setAdapter(adapter);

        return view;
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
}
