package ai.tomorrow.photory.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import ai.tomorrow.photory.Profile.ProfileActivity;
import ai.tomorrow.photory.R;
import ai.tomorrow.photory.models.LikeYou;
import ai.tomorrow.photory.models.Photo;
import ai.tomorrow.photory.models.User;
import ai.tomorrow.photory.models.UserAccountSettings;
import de.hdodenhof.circleimageview.CircleImageView;

public class YouLikesListAdapter extends ArrayAdapter<LikeYou> {
    private static final String TAG = "YouLikesListAdapter";

    public interface OnPostImageSelectedListener {
        void onPostImageSelected(Photo photo, int activityNumber);
    }

    OnPostImageSelectedListener mOnPostImageSelectedListener;

    //vars
    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResource;
    private List<LikeYou> mLikes;
    private ImageLoader mImageLoader;

    //firebase
    private FirebaseMethods mFirebaseMethods;
    private DatabaseReference myRef;

    public YouLikesListAdapter(Context context, int resource, List<LikeYou> objects) {
        super(context, resource, objects);

        mContext = context;
        mLikes = objects;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        mFirebaseMethods = new FirebaseMethods(context);
        myRef = FirebaseDatabase.getInstance().getReference();
        UniversalImageLoader.initImageLoader(mContext);
        mImageLoader = ImageLoader.getInstance();
        mOnPostImageSelectedListener = (OnPostImageSelectedListener) mContext;
    }

    public void setNewData(List<LikeYou> objects) {
        mLikes = objects;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        CircleImageView profilePhoto;
        TextView tv_liked_post;
        SquareImageView postImage;
        UserAccountSettings settings;
        User mUser;
        String user_id;
        String photo_id;
        Photo mPhoto;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();
            holder.profilePhoto = convertView.findViewById(R.id.profile_photo);
            holder.tv_liked_post = convertView.findViewById(R.id.tv_liked_post);
            holder.postImage = convertView.findViewById(R.id.post_image);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        getUserSettingAndPhoto(position, holder);

        return convertView;
    }

    public void getUserSettingAndPhoto(final int position, final ViewHolder holder) {

        holder.user_id = getItem(position).getLiked_by_user_id();
        holder.photo_id = getItem(position).getPhoto_id();

        Query query = myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(holder.user_id);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);
                }

                Query query = myRef.child(mContext.getString(R.string.dbname_users))
                        .orderByChild(mContext.getString(R.string.field_user_id))
                        .equalTo(holder.user_id);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                            holder.mUser = singleSnapshot.getValue(User.class);

                            Query query = myRef.child(mContext.getString(R.string.dbname_photos))
                                    .orderByChild(mContext.getString(R.string.field_photo_id))
                                    .equalTo(holder.photo_id);

                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                        holder.mPhoto = mFirebaseMethods.getPhoto(singleSnapshot);

                                        setupWidgets(holder, position);
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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled.");
            }
        });
    }

    public void setupWidgets(final ViewHolder holder, int position) {
        Log.d(TAG, "setupWidgets: setting up widgets.");
        mImageLoader.displayImage(holder.settings.getProfile_photo(), holder.profilePhoto);
        holder.profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigate to profile.");
                navigateToProfileActivity(holder.mUser);
            }
        });

        // like string
        SpannableStringBuilder spannableString = new SpannableStringBuilder();

        // user's username
        ClickableSpan clickSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Log.d(TAG, "onClick: navigate to view profile.");
                navigateToProfileActivity(holder.mUser);
            }

            @Override
            public void updateDrawState(TextPaint ds) {// override updateDrawState
                ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                ds.setUnderlineText(false); // set to false to remove underline
            }
        };

        spannableString.append(holder.settings.getUsername());
        int s1 = spannableString.length();
        spannableString.setSpan(clickSpan, 0, s1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // liked your post
        spannableString.append(" liked your post. ");

        // time diff
        int s2 = spannableString.length();
        spannableString.append(StringManipulation.getTimeStampDifference(getItem(position).getDate_created()));
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(mContext.getResources().getColor(R.color.dark_grey));
        spannableString.setSpan(colorSpan, s2, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.tv_liked_post.setMovementMethod(LinkMovementMethod.getInstance());
        holder.tv_liked_post.setText(spannableString);

        mImageLoader.displayImage(holder.mPhoto.getImage_path(), holder.postImage);

        holder.postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnPostImageSelectedListener.onPostImageSelected(holder.mPhoto,
                        mContext.getResources().getInteger(R.integer.likes_activity_number));
            }
        });

    }

    private void navigateToProfileActivity(User user) {
        Log.d(TAG, "navigateToProfileActivity: navigating.");
        // navigate to profile activity
        Intent intent = new Intent(mContext, ProfileActivity.class);
        intent.putExtra(mContext.getString(R.string.calling_activity_number), mContext.getResources().getInteger(R.integer.likes_activity_number));
        intent.putExtra(mContext.getString(R.string.selected_user), user);
        mContext.startActivity(intent);
        ((Activity) mContext).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
