package ai.tomorrow.instagramclone.Utils;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ai.tomorrow.instagramclone.Profile.ProfileActivity;
import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.Search.SearchActivity;
import ai.tomorrow.instagramclone.models.Like;
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

    public interface OnCommentThreadSelectedListener{
        void onCommentThreadSelectedListener(Photo photo);
    }
    MainfeedListAdapter.OnCommentThreadSelectedListener mOnCommentThreadSelectedListener;

    public MainfeedListAdapter(@NonNull Context context, int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layoutResource = resource;
        mPhotos = objects;
        mOnCommentThreadSelectedListener = (OnCommentThreadSelectedListener) mContext;
    }

    private static class ViewHolder{
        CircleImageView mprofileImage;
        TextView username, mTimestamp, caption, likes, comments;
        SquareImageView image;
        ImageView heartRed, heartWhite, comment;

        UserAccountSettings settings = new UserAccountSettings();
        User user  = new User();
        StringBuilder users;
        String mLikesString;
        boolean likeByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Photo photo;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null){
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.image = (SquareImageView) convertView.findViewById(R.id.post_image);
            holder.heartRed = (ImageView) convertView.findViewById(R.id.image_heart_red);
            holder.heartWhite = (ImageView) convertView.findViewById(R.id.image_heart);
            holder.comment = (ImageView) convertView.findViewById(R.id.speech_bubble);
            holder.likes = (TextView) convertView.findViewById(R.id.image_likes);
            holder.comments = (TextView) convertView.findViewById(R.id.image_comments_link);
            holder.caption = (TextView) convertView.findViewById(R.id.image_caption);
            holder.mTimestamp = (TextView) convertView.findViewById(R.id.image_time_posted);
            holder.mprofileImage = (CircleImageView) convertView.findViewById(R.id.profile_photo);
            holder.heart = new Heart(holder.heartWhite, holder.heartRed);

            convertView.setTag(holder);
        }else {
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
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
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

            }
        });

        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference();
        Query query2 = reference2.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(holder.photo.getUser_id());
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
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

            }
        });

        // set widgets
        holder.caption.setText(holder.photo.getCaption());

        return convertView;
    }

    private void navigateToProfileActivity(ViewHolder holder) {
        Log.d(TAG, "navigateToProfileActivity: navigating.");
        // navigate to profile activity
        Intent intent = new Intent(mContext, ProfileActivity.class);
        intent.putExtra(mContext.getString(R.string.calling_activity), mContext.getString(R.string.home_activity));
        intent.putExtra(mContext.getString(R.string.selected_user), holder.user);
        mContext.startActivity(intent);
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener{

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

            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(mHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                        String keyID = singleSnapshot.getKey();

                        //case1: Then user already liked the photo
                        if(mHolder.likeByCurrentUser &&
                                singleSnapshot.getValue(Like.class).getUser_id()
                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                            reference.child(mContext.getString(R.string.dbname_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();
///
                            reference.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            mHolder.heart.toggleLike();
                            getLikesString(mHolder);
                        }
                        //case2: The user has not liked the photo
                        else if(!mHolder.likeByCurrentUser){
                            //add new like
                            addNewLike(mHolder);
                            break;
                        }
                    }
                    if(!dataSnapshot.exists()){
                        //add new like
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return true;
        }
    }

    private void addNewLike(ViewHolder holder){
        Log.d(TAG, "addNewLike: adding new like");

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        String newLikeID = myRef.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        holder.heart.toggleLike();
        getLikesString(holder);
    }


    private void getLikesString(final ViewHolder holder){
        Log.d(TAG, "getLikesString: getting likes string");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.users = new StringBuilder();
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference
                            .child(mContext.getString(R.string.dbname_users))
                            .orderByChild(mContext.getString(R.string.field_user_id))
                            .equalTo(singleSnapshot.getValue(Like.class).getUser_id());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                                Log.d(TAG, "onDataChange: found like: " +
                                        singleSnapshot.getValue(User.class).getUsername());

                                holder.users.append(singleSnapshot.getValue(User.class).getUsername());
                                holder.users.append(",");
                            }

                            String[] splitUsers = holder.users.toString().split(",");

                            if(holder.users.toString().contains(holder.user.getUsername() + ",")){
                                holder.likeByCurrentUser = true;
                            }else{
                                holder.likeByCurrentUser = false;
                            }

                            int length = splitUsers.length;
                            if(length == 1){
                                holder.mLikesString = "Liked by " + splitUsers[0];
                            }
                            else if(length == 2){
                                holder.mLikesString = "Liked by " + splitUsers[0]
                                        + " and " + splitUsers[1];
                            }
                            else if(length == 3){
                                holder.mLikesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + " and " + splitUsers[2];

                            }
                            else if(length == 4){
                                holder.mLikesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + ", " + splitUsers[2]
                                        + " and " + splitUsers[3];
                            }
                            else if(length > 4){
                                holder.mLikesString = "Liked by " + splitUsers[0]
                                        + ", " + splitUsers[1]
                                        + ", " + splitUsers[2]
                                        + " and " + (splitUsers.length - 3) + " others";
                            }
                            Log.d(TAG, "onDataChange: likes string: " + holder.mLikesString);
                            setupWidgets(holder);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                if(!dataSnapshot.exists()){
                    holder.mLikesString = "";
                    holder.likeByCurrentUser = false;
                    setupWidgets(holder);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setupWidgets(final ViewHolder holder){
        String timestampDiff = getTimestampDifference(holder.photo.getDate_created());
        if(!timestampDiff.equals("0")){
            holder.mTimestamp.setText(timestampDiff + " DAYS AGO");
        }else{
            holder.mTimestamp.setText("TODAY");
        }

        holder.likes.setText(holder.mLikesString);

        if(holder.photo.getComments().size() > 0){
            holder.comments.setText("View all " + holder.photo.getComments().size() + " comments");
        }else{
            holder.comments.setText("");
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

        if(holder.likeByCurrentUser){
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: red heart touch detected.");
                    return holder.detector.onTouchEvent(event);
                }
            });
        }
        else{
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

    /**
     * Returns a string representing the number of days ago the post was made
     * @return
     */
    private String getTimestampDifference(String photoTimestamp){
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));//google 'android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch (ParseException e){
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }
        return difference;
    }

}
