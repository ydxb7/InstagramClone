package ai.tomorrow.instagramclone.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

import ai.tomorrow.instagramclone.Profile.ProfileActivity;
import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.models.Comment;
import ai.tomorrow.instagramclone.models.LikePhoto;
import ai.tomorrow.instagramclone.models.Photo;
import ai.tomorrow.instagramclone.models.User;
import ai.tomorrow.instagramclone.models.UserAccountSettings;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainfeedListAdapter extends ArrayAdapter<Photo> {
    private static final String TAG = "MainfeedListAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResource;
    private List<Photo> mPhotos;
    private String currentUsername;

    //firebase
    private FirebaseMethods mFirebaseMethods;
    private DatabaseReference myRef;

    public interface OnCommentThreadSelectedListener {
        void onCommentThreadSelectedListener(Photo photo);
    }

    MainfeedListAdapter.OnCommentThreadSelectedListener mOnCommentThreadSelectedListener;

    public interface OnLoadMoreItemsListener {
        void onLoadMoreItems();
    }

    OnLoadMoreItemsListener mOnLoadMoreItemsListener;

    public MainfeedListAdapter(@NonNull Context context, int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layoutResource = resource;
        mPhotos = objects;
        mFirebaseMethods = new FirebaseMethods(context);
        myRef = FirebaseDatabase.getInstance().getReference();
        UniversalImageLoader.initImageLoader(mContext);
        try {
            mOnCommentThreadSelectedListener = (OnCommentThreadSelectedListener) mContext;
            mOnLoadMoreItemsListener = (OnLoadMoreItemsListener) mContext;
        } catch (ClassCastException e) {
            Log.d(TAG, "MainfeedListAdapter: ClassCastException: " + e.getMessage());
        }
        getCurrentUsername();
    }

    private static class ViewHolder {
        CircleImageView mprofileImage;
        TextView username, mTimestamp, caption, likes, comments;
        SquareImageView image;
        ImageView heartRed, heartWhite, comment;

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        //        StringBuilder users;
        String mLikesString;
        boolean likeByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Photo photo;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = convertView.findViewById(R.id.username);
            holder.image = convertView.findViewById(R.id.post_image);
            holder.heartRed = convertView.findViewById(R.id.image_heart_red);
            holder.heartWhite = convertView.findViewById(R.id.image_heart);
            holder.comment = convertView.findViewById(R.id.speech_bubble);
            holder.likes = convertView.findViewById(R.id.image_likes);
            holder.comments = convertView.findViewById(R.id.image_comments_link);
            holder.caption = convertView.findViewById(R.id.image_caption);
            holder.mTimestamp = convertView.findViewById(R.id.image_time_posted);
            holder.mprofileImage = convertView.findViewById(R.id.profile_photo);
            holder.heart = new Heart(holder.heartWhite, holder.heartRed);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // get current photo
        holder.photo = mPhotos.get(position);
        UniversalImageLoader.setImage(holder.photo.getImage_path(), holder.image, null, "");
        holder.detector = new GestureDetector(mContext, new GestureListener(holder));

        // get user and userAccountSettings
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference();
        Query query1 = reference1.child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(holder.photo.getUser_id());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot.getValue(User.class).toString());
                    holder.user = singleSnapshot.getValue(User.class);
                }
                getLikesString(holder);

                holder.username.setText(holder.user.getUsername());
                // set onclicklistener to holder.username, navigate to the user's profile
                holder.username.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "onClick: navigate to ProfileActivity.");
                        navigateToProfileActivity(holder);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled.");
            }
        });

        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference();
        Query query2 = reference2.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(holder.photo.getUser_id());
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found userAccoutSettings: " + singleSnapshot.getValue());
                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);
                }
                UniversalImageLoader.setImage(holder.settings.getProfile_photo(), holder.mprofileImage, null, "");
                holder.mprofileImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "onClick: navigate to ProfileActivity.");
                        navigateToProfileActivity(holder);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled.");
            }
        });

        // set widgets
        holder.caption.setText(holder.photo.getCaption());

        // set comments
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Comment> commentList = new ArrayList<>();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Log.d(TAG, "onChildAdded: found comment: " + ds);
                            commentList.add(mFirebaseMethods.getComment(ds));
                        }
                        mPhotos.get(position).setComments(commentList);
                        if (holder.photo.getComments().size() > 0) {
                            holder.comments.setVisibility(View.VISIBLE);
                            holder.comments.setText("View all " + holder.photo.getComments().size() + " comments");
                        } else {
                            holder.comments.setVisibility(View.GONE);
                            holder.comments.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled.");
                    }
                });

        // if reach the end of the list, load more data
        if (position == getCount() - 1) {
            loadMoreData();
        }

        return convertView;
    }

    private void loadMoreData() {
        Log.d(TAG, "loadMoreData: loading more data.");
        mOnLoadMoreItemsListener.onLoadMoreItems();
    }

    private void navigateToProfileActivity(ViewHolder holder) {
        Log.d(TAG, "navigateToProfileActivity: navigating.");
        // navigate to profile activity
        Intent intent = new Intent(mContext, ProfileActivity.class);
        intent.putExtra(mContext.getString(R.string.calling_activity_number), mContext.getResources().getInteger(R.integer.home_activity_number));
        intent.putExtra(mContext.getString(R.string.selected_user), holder.user);
        mContext.startActivity(intent);
        ((Activity) mContext).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {

        ViewHolder mHolder;

        public GestureListener(ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap: double tap detected.");

            mHolder.heart.toggleLike();
            if (mHolder.likeByCurrentUser) {
                mFirebaseMethods.removePhotoLike(mHolder.photo.getPhoto_id(), mHolder.photo.getUser_id());
            } else {
                mFirebaseMethods.addPhotoNewLike(mHolder.photo.getPhoto_id(), mHolder.photo.getUser_id());
            }
            getLikesString(mHolder);

            return true;
        }
    }

    private void getLikesString(final ViewHolder holder) {
        Log.d(TAG, "getLikesString: getting likes string");

        holder.likeByCurrentUser = false;
        Query query = myRef.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes_photo));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: there is no likes node in photo: " + holder.photo.getPhoto_id());
                    holder.mLikesString = "";
                    setupWidgets(holder);
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
                            holder.likeByCurrentUser = true;
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
                        Log.d(TAG, "onDataChange: i = " + i);
                        LikePhoto eachLike = likes.get(i);
                        Query query = myRef.child(mContext.getString(R.string.dbname_users))
                                .orderByChild(mContext.getString(R.string.field_user_id))
                                .equalTo(eachLike.getLiked_by_user_id());

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    Log.d(TAG, "onDataChange: likesUsername.add: " + singleSnapshot.getValue(User.class).getUsername());
                                    likesUsername.add(singleSnapshot.getValue(User.class).getUsername());
                                }
                                if (likesUsername.size() == Math.min(likes.size(), 4)) {
                                    Log.d(TAG, "onDataChange: likes.size() = " + likes.size() + " likesUsername.size: " + likesUsername.size());
                                    holder.mLikesString = StringManipulation.getLikesString(likes.size(), likesUsername);
                                    setupWidgets(holder);
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


    private void setupWidgets(final ViewHolder holder) {
        String timestampDiff = StringManipulation.getTimeStampDifference(holder.photo.getDate_created());
        holder.mTimestamp.setText(timestampDiff);

        if (holder.mLikesString.equals("")) {
            holder.likes.setVisibility(View.GONE);
        } else {
            holder.likes.setVisibility(View.VISIBLE);
            holder.likes.setText(holder.mLikesString);
        }

        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to comments thread");

                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(holder.photo);
            }
        });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(holder.photo);
            }
        });

        if (holder.likeByCurrentUser) {
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: red heart touch detected.");
                    return holder.detector.onTouchEvent(event);
                }
            });
        } else {
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: white heart touch detected.");
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
    }

    private void getCurrentUsername() {
        Log.d(TAG, "getCurrentUsername: get current username.");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    currentUsername = singleSnapshot.getValue(User.class).getUsername();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled.");
            }
        });
    }
}
