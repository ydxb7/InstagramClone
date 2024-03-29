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
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
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
import ai.tomorrow.photory.models.Comment;
import ai.tomorrow.photory.models.LikeComment;
import ai.tomorrow.photory.models.User;
import ai.tomorrow.photory.models.UserAccountSettings;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentListAdapter extends ArrayAdapter<Comment> {
    private static final String TAG = "CommentListAdapter";

    public interface OnReplyClickedListener {
        void OnReplyClick(String replyToUsername);
    }

    private Context mContext;
    private LayoutInflater mInflater;
    private int layoutResource;
    private List<Comment> mComments;
    private boolean mLikedByCurrentUser;
    private String photoID, photoOwnerID;
    private FirebaseMethods mFirebaseMethods;
    private DatabaseReference myRef;
    private Fragment mFragment;

    public CommentListAdapter(@NonNull Context context, int layoutResource, @NonNull List<Comment> objects,
                              String photoID, String photoOwnerID, Fragment fragment) {
        super(context, layoutResource, objects);
        mFragment = fragment;
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layoutResource = layoutResource;
        mComments = objects;
        this.photoID = photoID;
        this.photoOwnerID = photoOwnerID;
        mFirebaseMethods = new FirebaseMethods(context);
        myRef = FirebaseDatabase.getInstance().getReference();
        UniversalImageLoader.initImageLoader(mContext);
    }

    private static class ViewHolder {
        CircleImageView profileImage;
        TextView username, comment, timePostes, likes_number, reply;
        ImageView like, heart_white, heart_red;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();
            holder.profileImage = convertView.findViewById(R.id.comment_profile_image);
            holder.comment = convertView.findViewById(R.id.comment);
            holder.timePostes = convertView.findViewById(R.id.comment_time_posted);
            holder.likes_number = convertView.findViewById(R.id.likes_number);
            holder.reply = convertView.findViewById(R.id.comment_reply);
            holder.heart_white = convertView.findViewById(R.id.heart_white);
            holder.heart_red = convertView.findViewById(R.id.heart_red);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        setupWidgets(position, holder);

        return convertView;
    }

    public void setNewData(ArrayList<Comment> comments) {
        mComments = comments;
        notifyDataSetChanged();
    }

    private void setupWidgets(final int position, final ViewHolder holder) {
        if (position == 0) {
            holder.likes_number.setVisibility(View.GONE);
            holder.reply.setVisibility(View.GONE);
            holder.heart_white.setVisibility(View.GONE);
            holder.heart_red.setVisibility(View.GONE);
        } else {
            // set heart init
            setCommentLike(position, holder);

            // set heart onclick
            holder.heart_white.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: liking comment.");

                    mFirebaseMethods.addCommentNewLike(photoID, photoOwnerID, getItem(position).getComment_id(), getItem(position).getUser_id());
                    setCommentLike(position, holder);
                }
            });

            holder.heart_red.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: liking comment.");

                    mFirebaseMethods.removeCommentLike(photoID, photoOwnerID, getItem(position).getComment_id());
                    setCommentLike(position, holder);
                }
            });
        }

        // set the time difference
        String timestampDifference = StringManipulation.getTimeStampDifference(getItem(position).getDate_created());
        holder.timePostes.setText(timestampDifference);

        // set comment string
        Query query1 = myRef
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: getting user account settings");
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    final UserAccountSettings userAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                    UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(), holder.profileImage, null, "");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled.");
            }
        });

        Query query2 = myRef
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user: " + singleSnapshot);
                    final User commentUser = singleSnapshot.getValue(User.class);

                    // click reply, set the username in edit textView
                    holder.reply.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "onClick: reply comment.");
                            ((ViewCommentsFragment) mFragment).OnReplyClick(commentUser.getUsername());
                        }
                    });

                    // set profile image onclick event
                    holder.profileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(TAG, "onClick: navigating to user's profile.");
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_activity_number), mContext.getResources().getInteger(R.integer.home_activity_number));
                            intent.putExtra(mContext.getString(R.string.selected_user), commentUser);
                            mContext.startActivity(intent);
                            ((Activity) mContext).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        }
                    });

                    if (!getItem(position).getReply_to_username().equals("")) {
                        // reply to comment, this comment has @username
                        Query query = myRef.child(mContext.getString(R.string.dbname_users))
                                .orderByChild(mContext.getString(R.string.field_username))
                                .equalTo(getItem(position).getReply_to_username());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    final User replyToUser = singleSnapshot.getValue(User.class);

                                    // set comment
                                    SpannableStringBuilder spannableString = new SpannableStringBuilder();

                                    // Current user's username
                                    spannableString.append(commentUser.getUsername());
                                    StyleSpan boldStyleSpan = new StyleSpan(Typeface.BOLD);//粗体
                                    int s1 = spannableString.length();
                                    spannableString.setSpan(boldStyleSpan, 0, s1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                    Log.d(TAG, "onDataChange: getItem(position).getReply_to_username(): " + getItem(position).getReply_to_username());

                                    // reply to user's username link
                                    spannableString.append(" @" + getItem(position).getReply_to_username());
                                    int s2 = spannableString.length();

                                    ClickableSpan clickSpan = new ClickableSpan() {
                                        @Override
                                        public void onClick(View widget) {
                                            //put whatever you like here, below is an example
                                            Intent intent = new Intent(mContext, ProfileActivity.class);
                                            intent.putExtra(mContext.getString(R.string.calling_activity_number), mContext.getResources().getInteger(R.integer.home_activity_number));
                                            intent.putExtra(mContext.getString(R.string.selected_user), replyToUser);
                                            mContext.startActivity(intent);
                                            ((Activity) mContext).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        }

                                        @Override
                                        public void updateDrawState(TextPaint ds) {// override updateDrawState
                                            ds.setColor(mContext.getResources().getColor(R.color.link_blue));
                                            ds.setUnderlineText(false); // set to false to remove underline
                                        }
                                    };
                                    spannableString.setSpan(clickSpan, s1 + 1, s2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    holder.comment.setMovementMethod(LinkMovementMethod.getInstance());
                                    // comment body
                                    spannableString.append(" " + getItem(position).getComment());
                                    holder.comment.setText(spannableString);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled.");
                            }
                        });
                    } else {
                        // reply to photo
                        SpannableStringBuilder spannableString = new SpannableStringBuilder();

                        // Current user's username
                        spannableString.append(commentUser.getUsername());
                        StyleSpan boldStyleSpan = new StyleSpan(Typeface.BOLD);//粗体
                        int s1 = spannableString.length();
                        spannableString.setSpan(boldStyleSpan, 0, s1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        // comment body
                        spannableString.append(" " + getItem(position).getComment());
                        holder.comment.setText(spannableString);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled.");
            }
        });
    }

    private void setCommentLike(int position, final ViewHolder holder) {
        Query query = myRef
                .child(mContext.getString(R.string.dbname_photos))
                .child(photoID)
                .child(mContext.getString(R.string.field_comments))
                .child(getItem(position).getComment_id())
                .child(mContext.getString(R.string.field_likes_comment));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: comment's like state change.");
                mLikedByCurrentUser = false;
                int likeCount = 0;
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found comment like: " + singleSnapshot);
                    if (singleSnapshot.getValue(LikeComment.class).getLiked_by_user_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        mLikedByCurrentUser = true;
                    likeCount++;
                }
                Log.d(TAG, "onDataChange: comment mLikedByCurrentUser: " + mLikedByCurrentUser);

                if (mLikedByCurrentUser) {
                    holder.heart_red.setVisibility(View.VISIBLE);
                    holder.heart_white.setVisibility(View.GONE);
                } else {
                    holder.heart_red.setVisibility(View.GONE);
                    holder.heart_white.setVisibility(View.VISIBLE);
                }

                if (likeCount != 0) {
                    holder.likes_number.setVisibility(View.VISIBLE);
                    holder.likes_number.setText(likeCount + " likes");
                } else {
                    holder.likes_number.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled.");
            }
        });
    }
}
