package ai.tomorrow.instagramclone.Utils;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ai.tomorrow.instagramclone.Home.HomeActivity;
import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.models.Comment;
import ai.tomorrow.instagramclone.models.Photo;

public class ViewCommentsFragment extends Fragment implements CommentListAdapter.OnReplyClickedListener {

    private static final String TAG = "ViewCommentsFragment";

    @Override
    public void OnReplyClick(String replyToUsername) {
        Log.d(TAG, "OnReplyClick.");
        String usernameString = String.format("<font color=#2e5cb8>@%s</font><font color=#000000/> ", replyToUsername);
        mComment.setText(Html.fromHtml(usernameString));
        mComment.requestFocus();
        mComment.setSelection(mComment.getText().length());
        Helpers.showSoftKeyboard(mContext);
    }

    public ViewCommentsFragment() {
        setArguments(new Bundle());
    }

    //widgets
    private ImageView mBackArrow, mCheckMark;
    private EditText mComment;
    private ListView mListView;

    //vars
    private Context mContext;
    private Photo mPhoto;
    private ArrayList<Comment> mComments = new ArrayList<>();
    private CommentListAdapter mAdapter;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);
        Log.d(TAG, "onCreateView: started.");

        mContext = getActivity();
        mBackArrow = view.findViewById(R.id.backArrow);
        mCheckMark = view.findViewById(R.id.ivPostComment);
        mComment = view.findViewById(R.id.comment);
        mListView = view.findViewById(R.id.listView);
        mFirebaseMethods = new FirebaseMethods(mContext);

        try {
            mPhoto = getPhotoFromBundle();
        } catch (NullPointerException e) {
            Log.d(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }

        mAdapter = new CommentListAdapter(mContext, R.layout.layout_comment,
                mComments, mPhoto.getPhoto_id(), mPhoto.getUser_id(), ViewCommentsFragment.this);
        mListView.setAdapter(mAdapter);
        setupFirebaseAuth();

        refreshListView();

        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> commentResults = StringManipulation.splitCommentString(mComment.getText().toString());
                Log.d(TAG, "onClick: commentResults: " + commentResults);

                if (commentResults != null) {
                    Log.d(TAG, "onClick: post a comment.");

                    String replyToUername = commentResults.get(0);
                    String comment = commentResults.get(1);

                    mFirebaseMethods.addNewComment(comment, mPhoto.getPhoto_id(), mPhoto.getUser_id(), replyToUername);
                    refreshListView();

                    mComment.setText("");
                    Helpers.hideSoftKeyboard(getActivity());
                    mListView.smoothScrollToPosition(mListView.getCount());
                } else {
                    Toast.makeText(getActivity(), "you can't post an empty comment.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back.");
                getActivity().getSupportFragmentManager().popBackStack();
                Bundle bundle = getArguments();
                Log.d(TAG, "onClick: bundle.getString(mContext.getString(R.string.calling_activity)):" + bundle.getString(mContext.getString(R.string.calling_activity)));
                if (bundle != null
                        && bundle.getString(mContext.getString(R.string.calling_activity)) != null
                        && bundle.getString(mContext.getString(R.string.calling_activity)).equals(mContext.getString(R.string.home_activity))){
                    ((HomeActivity)getActivity()).showRelativeLayout();
                }
            }
        });

        return view;
    }

    private void refreshListView() {
        Log.d(TAG, "refreshListView: setting up comments.");

        Query query = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mComments.clear();
                Comment firstComment = new Comment();
                firstComment.setUser_id(mPhoto.getUser_id());
                firstComment.setReply_to_username("");
                firstComment.setComment(mPhoto.getCaption());
                firstComment.setDate_created(mPhoto.getDate_created());

                mComments.add(firstComment);

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    mComments.add(mFirebaseMethods.getComment(singleSnapshot));
                }

                mAdapter.setNewData(mComments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    /**
     * return the photo from the incoming bundle from profileActivity interface
     *
     * @return
     */
    private Photo getPhotoFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());

        Bundle bundle = getArguments();
        if (bundle != null) {
            return bundle.getParcelable(mContext.getString(R.string.photo));
        } else {
            return null;
        }
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
