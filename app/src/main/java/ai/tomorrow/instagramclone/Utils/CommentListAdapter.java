package ai.tomorrow.instagramclone.Utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    public CommentListAdapter(@NonNull Context context, int layoutResource, @NonNull List<Comment> objects) {
        super(context, layoutResource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = layoutResource;
        mComments = objects;
    }

    private static class ViewHolder{

        CircleImageView profileImage;
        TextView username, comment, timePostes, likes, reply;
        ImageView mHeart;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null){
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();
            holder.profileImage = (CircleImageView) convertView.findViewById(R.id.comment_profile_image);
            holder.username = (TextView) convertView.findViewById(R.id.comment_username);
            holder.comment = (TextView) convertView.findViewById(R.id.comment);
            holder.timePostes = (TextView) convertView.findViewById(R.id.comment_time_posted);
            holder.likes = (TextView) convertView.findViewById(R.id.comment_likes);
            holder.reply = (TextView) convertView.findViewById(R.id.comment_reply);
            holder.mHeart = (ImageView) convertView.findViewById(R.id.comment_like);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // set comment
        holder.comment.setText(getItem(position).getComment());

        // set the time difference
        String timestampDifference = getTimeStampDifference(getItem(position));
        if (!timestampDifference.equals("0")){
            holder.timePostes.setText(timestampDifference + " d");
        } else {
            holder.timePostes.setText("today");
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: getting user account settings");
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                    UserAccountSettings userAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                    holder.username.setText(userAccountSettings.getUsername());
                    UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(), holder.profileImage, null, "");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return convertView;


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
