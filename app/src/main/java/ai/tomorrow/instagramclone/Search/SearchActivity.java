package ai.tomorrow.instagramclone.Search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;

import ai.tomorrow.instagramclone.Profile.ProfileActivity;
import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.Utils.BottomNavigationViewHelper;
import ai.tomorrow.instagramclone.Utils.Helpers;
import ai.tomorrow.instagramclone.Utils.UserListAdapter;
import ai.tomorrow.instagramclone.models.User;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";

    private Context mContext = SearchActivity.this;

    //widegets
    private EditText mSearch;
    private ListView mListView;

    //vars
    private List<User> mUserList;
    private UserListAdapter adapter;
    private int mActivityNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Log.d(TAG, "onCreate: starting");
        mSearch = findViewById(R.id.search);
        mListView = findViewById(R.id.listView);

        Helpers.hideSoftKeyboard(this);

        setupBottomNavigationView();

        initTextListener();
    }

    private void initTextListener() {
        Log.d(TAG, "initTextListener: initializing");

        mUserList = new ArrayList<>();

        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged.");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged.");
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged.");
                String text = mSearch.getText().toString().trim();
                searchForMatch(text);
            }
        });
    }

    private void searchForMatch(String keyword) {
        Log.d(TAG, "searchForMatch: searching for: " + keyword);
        mUserList.clear();
        if (!keyword.equals("")) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            Query query = reference.child(getString(R.string.dbname_users))
                    .orderByChild(getString(R.string.field_username))
                    .equalTo(keyword);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleDatasnapshot : dataSnapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: getting user: " + singleDatasnapshot.getValue(User.class).toString());
                        mUserList.add(singleDatasnapshot.getValue(User.class));
                    }
                    // update the users list view
                    updateUsersList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled.");
                }
            });
        }
    }

    private void updateUsersList() {
        Log.d(TAG, "updateUsersList: updating users list");
        adapter = new UserListAdapter(mContext, R.layout.layout_user_listitem, mUserList);

        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: seleted user: " + mUserList.get(position).toString());

                // navigate to profile activity
                Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                intent.putExtra(getString(R.string.calling_activity_number), getString(R.string.search_activity));
//                intent.putExtra(getString(R.string.calling_activity), getString(R.string.search_activity));
                intent.putExtra(getString(R.string.selected_user), mUserList.get(position));
                startActivity(intent);
                SearchActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    /**
     * BottomNavigationView setup
     */
    private void setupBottomNavigationView() {
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        mActivityNumber = getIntent().getIntExtra(getString(R.string.calling_activity_number),
                getResources().getInteger(R.integer.search_activity_number));
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
    }
}
