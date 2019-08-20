package ai.tomorrow.instagramclone.Utils;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
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

import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.models.Comment;
import ai.tomorrow.instagramclone.models.UserAccountSettings;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentListAdapter extends ArrayAdapter<Comment> {
    private static final String TAG = "CommentListAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResource;
    private List<Comment> mComments;
    private boolean mLikedByCurrentUser;
    private String photoID, photoOwnerID;
    private FirebaseMethods mFirebaseMethods;
    private DatabaseReference myRef;

    public CommentListAdapter(@NonNull Context context, int layoutResource, @NonNull List<Comment> objects,
                              String photoID, String photoOwnerID) {
        super(context, layoutResource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layoutResource = layoutResource;
        mComments = objects;
        this.photoID = photoID;
        this.photoOwnerID = photoOwnerID;
        mFirebaseMethods = new FirebaseMethods(context);
        myRef = FirebaseDatabase.getInstance().getReference();
    }

    private static class ViewHolder{

        CircleImageView profileImage;
        TextView username, comment, timePostes, likes_number, reply;
        ImageView like, heart_white, heart_red;

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null){
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();
            holder.profileImage = (CircleImageView) convertView.findViewById(R.id.comment_profile_image);
//            holder.username = (TextView) convertView.findViewById(R.id.comment_username);
            holder.comment = (TextView) convertView.findViewById(R.id.comment);
            holder.timePostes = (TextView) convertView.findViewById(R.id.comment_time_posted);
            holder.likes_number = (TextView) convertView.findViewById(R.id.likes_number);
            holder.reply = (TextView) convertView.findViewById(R.id.comment_reply);
            holder.heart_white = (ImageView) convertView.findViewById(R.id.heart_white);
            holder.heart_red = (ImageView) convertView.findViewById(R.id.heart_red);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        setupWidgets(position, holder);

        return convertView;
    }

    private void setupWidgets(final int position, final ViewHolder holder) {
        if (position == 0){
            holder.likes_number.setVisibility(View.GONE);
            holder.reply.setVisibility(View.GONE);
            holder.heart_white.setVisibility(View.GONE);
            holder.heart_red.setVisibility(View.GONE);
        } else {
            // set heart init
            Query query2 = myRef
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(photoID)
                    .child(mContext.getString(R.string.field_comments))
                    .child(getItem(position).getComment_id())
                    .child(mContext.getString(R.string.field_likes))
                    .orderByChild(mContext.getString(R.string.field_liked_by_user_id))
                    .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

            query2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange: comment's like state change.");
                    mLikedByCurrentUser = false;
                    for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: found comment like: " + singleSnapshot);
                        mLikedByCurrentUser = true;
                    }
                    Log.d(TAG, "onDataChange: comment mLikedByCurrentUser: " + mLikedByCurrentUser);

                    if (mLikedByCurrentUser){
                        holder.heart_red.setVisibility(View.VISIBLE);
                        holder.heart_white.setVisibility(View.GONE);
                    } else {
                        holder.heart_red.setVisibility(View.GONE);
                        holder.heart_white.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // set heart onclick
            holder.heart_white.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: liking comment.");

                    mFirebaseMethods.addCommentNewLike(photoID, photoOwnerID, getItem(position).getComment_id(), getItem(position).getUser_id());
                }
            });

            holder.heart_red.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: liking comment.");

                    mFirebaseMethods.removeCommentLike(photoID, photoOwnerID, getItem(position).getComment_id());
                }
            });
        }

        // set the time difference
        String timestampDifference = getTimeStampDifference(getItem(position));
        if (!timestampDifference.equals("0")){
            holder.timePostes.setText(timestampDifference + " d");
        } else {
            holder.timePostes.setText("today");
        }

        // set comment string
        Query query1 = myRef
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: getting user account settings");
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                    UserAccountSettings userAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                    // set comment
                    String commentString = String.format("<b>%s</b> %s",
                            userAccountSettings.getUsername(), getItem(position).getComment());
                    holder.comment.setText(Html.fromHtml(commentString));
                    UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(), holder.profileImage, null, "");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    /**
     * return a string presenting the number of days ago the post was made.
     * @return
     */
    private String getTimeStampDifference(Comment comment){
        Log.d(TAG, "getTimeStampDifference: getting timestamp difference");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimeStamp = comment.getDate_created();
        try {
            timestamp = sdf.parse(photoTimeStamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60/ 24)));
        }catch (ParseException e){
            Log.d(TAG, "getTimeStampDifference: ParseException: " + e.getMessage());
        }
        return difference;
    }
}
