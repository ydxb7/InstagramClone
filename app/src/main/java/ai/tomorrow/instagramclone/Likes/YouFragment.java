package ai.tomorrow.instagramclone.Likes;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.Utils.FirebaseMethods;
import ai.tomorrow.instagramclone.models.LikePhoto;

public class YouFragment extends Fragment {
    private static final String TAG = "YouFragment";

    //widgets
    private ListView mListView;

    //vars
    private Context mContext;
//    private List<LikePhoto> mAllLikes = new ArrayList<>();
//    private List<List<LikePhoto>> mLikes = new ArrayList<>();
//    private List<String> mFollowingsUserID = new ArrayList<>();

    //firebase
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: started.");
        View view = inflater.inflate(R.layout.fragment_you, container, false);
        mContext = getActivity();
//        mListView = (ListView) view.findViewById(R.id.listView);
        myRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseMethods = new FirebaseMethods(mContext);

        return view;
    }
}
