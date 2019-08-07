package ai.tomorrow.instagramclone;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import ai.tomorrow.instagramclone.Utils.BottomNavigationViewHelper;
import ai.tomorrow.instagramclone.Utils.SquareImageView;
import ai.tomorrow.instagramclone.Utils.UniversalImageLoader;
import ai.tomorrow.instagramclone.models.Photo;
import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPostFragment extends Fragment {
    private static final String TAG = "ViewPostFragment";

    public ViewPostFragment(){
        setArguments(new Bundle());
    }

    // vars
    private Photo mPhoto;
    private int mActivityNumber = 0;
    private Context mContext;

    //widgets
    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationView;
    private TextView mBackLabel, mCaption, mUsername, mTimestamp;
    private ImageView mBackArrow, mEllipses, mHeartRed, mHeartWhite, mProfileImage;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        Log.d(TAG, "onCreateView: started");
        bottomNavigationView = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
        mContext = getActivity();
        mPostImage = (SquareImageView) view.findViewById(R.id.post_image);
        mBackArrow = (ImageView) view.findViewById(R.id.backArrow);
        mBackLabel = (TextView) view.findViewById(R.id.tvBackLabel);
        mCaption = (TextView) view.findViewById(R.id.image_caption);
        mUsername = (TextView) view.findViewById(R.id.username);
        mTimestamp = (TextView) view.findViewById(R.id.image_time_posted);
        mEllipses = (ImageView) view.findViewById(R.id.ivEllipses);
        mHeartRed = (ImageView) view.findViewById(R.id.image_heart_red);
        mHeartWhite = (ImageView) view.findViewById(R.id.image_heart);
        mProfileImage = (CircleImageView) view.findViewById(R.id.profile_photo);


        try {
            mPhoto = getPhotoFromBundle();
            UniversalImageLoader.setImage(mPhoto.getImage_path(), mPostImage, null, "");
            mActivityNumber = getActivityNumberFromBundle();
        }catch (NullPointerException e){
            Log.d(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }

        setupBottomNavigationView();

        return view;
    }

    /**
     * return the activityNumber from the incoming bundle from profileActivity interface
     * @return
     */
    private int getActivityNumberFromBundle(){
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());

        Bundle bundle = getArguments();
        if (bundle != null){
            return bundle.getInt(getString(R.string.activity_number));
        }else {
            return 0;
        }
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

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext, getActivity(), bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
    }


}
