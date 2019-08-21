package ai.tomorrow.instagramclone.Utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.models.Comment;
import ai.tomorrow.instagramclone.models.LikePhoto;
import ai.tomorrow.instagramclone.models.UserAccountSettings;
import de.hdodenhof.circleimageview.CircleImageView;

public class FollowingLikesListAdapter extends ArrayAdapter<List<LikePhoto>> {
    private static final String TAG = "FollowingLikesListAdapt";

    //constance
    private final int NUM_GRID_COLUMNS = 7;

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
    }

    public void setNewData(List<List<LikePhoto>> objects){
        mLikes = objects;
        notifyDataSetChanged();
    }

    private static class ViewHolder {

        CircleImageView profilePhoto;
        TextView tv_liked_post;
        ExpandableHeightGridView gridView;
        String userID;
        UserAccountSettings settings;

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();
            holder.profilePhoto = (CircleImageView) convertView.findViewById(R.id.profile_photo);
            holder.tv_liked_post = (TextView) convertView.findViewById(R.id.tv_liked_post);
            holder.gridView = (ExpandableHeightGridView) convertView.findViewById(R.id.gridView);
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
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found user account settings: " + singleSnapshot);

                    holder.settings = singleSnapshot.getValue(UserAccountSettings.class);

                    // set profile photo
                    UniversalImageLoader.setImage(holder.settings.getProfile_photo(), holder.profilePhoto, null, "");

                    // like string
                    SpannableStringBuilder spannableString = new SpannableStringBuilder();

                    // user's username
                    spannableString.append(holder.settings.getUsername());
                    StyleSpan boldStyleSpan = new StyleSpan(Typeface.BOLD);//粗体
                    int s1 = spannableString.length();
                    spannableString.setSpan(boldStyleSpan, 0, s1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // liked 3 posts
                    spannableString.append(" liked " + getItem(position).size() + " posts. " );

                    // time diff
                    int s2 = spannableString.length();
                    spannableString.append(StringManipulation.getTimeStampDifference(getItem(position).get(0).getDate_created()));
                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(mContext.getResources().getColor(R.color.dark_grey));
                    spannableString.setSpan(colorSpan, s2, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    holder.tv_liked_post.setText(spannableString);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupGridView(final int position, final ViewHolder holder){
        Log.d(TAG, "setupGridView. ");

        // set the grid column width
        int gridWidth = mContext.getResources().getDisplayMetrics().widthPixels - 40 - 8 - 16 - 16 - 6 * 2;
        int imageWidth = gridWidth / 4;
        holder.gridView.setColumnWidth(imageWidth);

        // get imgURLs and setup grid view
        final ArrayList<String> imgURLs = new ArrayList<>();

        for (int i = 0; i < getItem(position).size(); i++){
            final int count = i;

            String photoID = getItem(position).get(i).getPhoto_id();
            Query query = myRef.child(mContext.getString(R.string.dbname_photos))
                    .orderByChild(mContext.getString(R.string.field_photo_id))
                    .equalTo(photoID);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: found photo: " + singleSnapshot);
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                        imgURLs.add(objectMap.get(mContext.getString(R.string.field_image_path)).toString());
                    }
                    if (count == getItem(position).size() - 1){
                        // set grid view
                        GridImageAdapter adapter = new GridImageAdapter(mContext, R.layout.layout_grid_imageview, "", imgURLs);
                        holder.gridView.setAdapter(adapter);


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


}
