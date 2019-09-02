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
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ai.tomorrow.photory.Profile.ProfileActivity;
import ai.tomorrow.photory.R;
import ai.tomorrow.photory.models.LikePhoto;
import ai.tomorrow.photory.models.Photo;
import ai.tomorrow.photory.models.User;
import ai.tomorrow.photory.models.UserAccountSettings;
import de.hdodenhof.circleimageview.CircleImageView;

public class FollowingLikesListAdapter extends ArrayAdapter<List<LikePhoto>> {
    private static final String TAG = "FollowingLikesListAdapt";

    public interface OnGridImageSelectedListener {
        void onGridImageSelected(Photo photo, int activityNumber);
    }

    OnGridImageSelectedListener mOnGridImageSelectedListener;

    //vars
    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResource;
    private List<List<LikePhoto>> mLikes;

    //firebase
    private FirebaseMethods mFirebaseMethods;
    private DatabaseReference myRef;

    public FollowingLikesListAdapter(Context context, int resource, List<List<LikePhoto>> objects) {
        super(context, resource, objects);

        mContext = context;
        mLikes = objects;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        mFirebaseMethods = new FirebaseMethods(context);
        myRef = FirebaseDatabase.getInstance().getReference();
        mOnGridImageSelectedListener = (OnGridImageSelectedListener) mContext;
        UniversalImageLoader.initImageLoader(mContext);
    }

    public void setNewData(List<List<LikePhoto>> objects) {
        mLikes = objects;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        CircleImageView profilePhoto;
        TextView tv_liked_post;
        RecyclerView gridView;
        String userID;
        UserAccountSettings settings;
        List<Photo> mPhotos = new ArrayList<>();
        User mUser;
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
            holder.gridView = convertView.findViewById(R.id.gridView);
            holder.userID = getItem(position).get(0).getLiked_by_user_id();

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        setupWidgets(position, holder);
        setupGridView(position, holder);

        return convertView;
    }

    public void setupWidgets(final int position, final ViewHolder holder) {
        Query query = myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(holder.userID);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user account settings: " + singleSnapshot);

                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);

                    // set profile photo
                    UniversalImageLoader.setImage(holder.settings.getProfile_photo(), holder.profilePhoto, null, "");

                    Query query = myRef.child(mContext.getString(R.string.dbname_users))
                            .orderByChild(mContext.getString(R.string.field_user_id))
                            .equalTo(holder.userID);

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                holder.mUser = singleSnapshot.getValue(User.class);

                                holder.profilePhoto.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Log.d(TAG, "onClick: navigate to viewProfile.");
                                        navigateToProfileActivity(holder.mUser);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d(TAG, "onCancelled.");
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

                    // liked 3 posts
                    spannableString.append(" liked " + getItem(position).size() + " posts. ");

                    // time diff
                    int s2 = spannableString.length();
                    spannableString.append(StringManipulation.getTimeStampDifference(getItem(position).get(0).getDate_created()));
                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(mContext.getResources().getColor(R.color.dark_grey));
                    spannableString.setSpan(colorSpan, s2, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.tv_liked_post.setMovementMethod(LinkMovementMethod.getInstance());
                    holder.tv_liked_post.setText(spannableString);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled.");
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

    private void setupGridView(final int position, final ViewHolder holder) {
        Log.d(TAG, "setupGridView. ");

        // get imgURLs and setup grid view
        holder.mPhotos.clear();

        for (int i = 0; i < getItem(position).size(); i++) {

            final String photoID = getItem(position).get(i).getPhoto_id();
            Query query = myRef.child(mContext.getString(R.string.dbname_photos))
                    .orderByChild(mContext.getString(R.string.field_photo_id))
                    .equalTo(photoID);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: found photo: " + singleSnapshot);
                        holder.mPhotos.add(mFirebaseMethods.getPhoto(singleSnapshot));
                        Log.d(TAG, "onDataChange: mPhotos.size() = " + holder.mPhotos.size() + " getItem(position).size() = " + getItem(position).size());
                    }
                    if (holder.mPhotos.size() == getItem(position).size()) {
                        final ArrayList<String> imgURLs = new ArrayList<>();
                        for (Photo photo : holder.mPhotos) {
                            imgURLs.add(photo.getImage_path());
                        }
                        // set grid view
                        // user the grid adapter to adapter the images to gridview
                        GridImageAdapter adapter = new GridImageAdapter(mContext, R.layout.layout_grid_imageview,
                                "", imgURLs, new GridImageAdapter.OnGridItemClickListener() {
                            @Override
                            public void OnGridItemClick(int position) {
                                Log.d(TAG, "onItemClick: selected an image + " + imgURLs.get(position));
                                mOnGridImageSelectedListener.onGridImageSelected(holder.mPhotos.get(position),
                                        mContext.getResources().getInteger(R.integer.likes_activity_number));
                            }
                        });
                        holder.gridView.setAdapter(adapter);
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
