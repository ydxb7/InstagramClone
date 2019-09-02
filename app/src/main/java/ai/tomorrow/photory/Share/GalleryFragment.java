package ai.tomorrow.photory.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

import ai.tomorrow.photory.Profile.AccountSettingsActivity;
import ai.tomorrow.photory.R;
import ai.tomorrow.photory.Utils.FilePaths;
import ai.tomorrow.photory.Utils.FileSearch;
import ai.tomorrow.photory.Utils.GridImageAdapter;

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";

    //constants
    private static final int NUM_GRID_COLUMNS = 4;
    private static final String mAppend = "file:/";

    //widgets
    private RecyclerView gridView;
    private ImageView galleryImage;
    private ProgressBar progressBar;
    private Spinner directorySpinner;

    //vars
    private ArrayList<String> directories;
    private String mSelectedImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        Log.d(TAG, "onCreateView: stated.");

        galleryImage = view.findViewById(R.id.galleryImageView);
        gridView = view.findViewById(R.id.gridView);
        directorySpinner = view.findViewById(R.id.spinnerDirectory);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        ImageView shareClose = view.findViewById(R.id.ivCloseShare);
        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the gallery fragment");
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        TextView nextScreen = view.findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the final share screen");

                if (isRootTask()) {
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } else {
                    Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_image), mSelectedImage);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();
                    getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            }
        });
        init();
        return view;
    }

    private boolean isRootTask() {
        // It's coming from EditProfileFragment
        return ((ShareActivity) getActivity()).getTask() == 0;
    }


    private void init() {
        FilePaths filePaths = new FilePaths();

        //check for other folders inside "/storage/emulated/0/pictures"
        if (FileSearch.getDirectoryPaths(filePaths.PICTURES) != null) {
            directories = FileSearch.getDirectoryPaths(filePaths.PICTURES);
        }
        directories.add(filePaths.CAMERA);

        // directories is too long, we only need the last part of the file path
        ArrayList<String> directoryName = new ArrayList<>();
        for (int i = 0; i < directories.size(); i++) {
            int index = directories.get(i).lastIndexOf("/");
            String string = directories.get(i).substring(index);
            directoryName.add(string);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, directoryName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(adapter);

        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: selected: " + directories.get(position));

                // setup our image grid for the directory chosen
                setupGridView(directories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "onNothingSelected.");
            }
        });
    }

    private void setupGridView(String selectedDirectory) {
        Log.d(TAG, "setupGridView: directory chosen: " + selectedDirectory);
        final ArrayList<String> imgURLs = FileSearch.getFilePaths(selectedDirectory);

        // user the grid adapter to adapter the images to gridview
        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview,
                mAppend, imgURLs, new GridImageAdapter.OnGridItemClickListener() {
            @Override
            public void OnGridItemClick(int position) {
                Log.d(TAG, "OnGridItemClick: selected an image + " + imgURLs.get(position));
                if (imgURLs != null && imgURLs.size() > 0) {
                    setImage(imgURLs.get(position), galleryImage, mAppend);
                    mSelectedImage = imgURLs.get(position);
                }
            }
        });

        gridView.setAdapter(adapter);

        // set the first image to be displayed when the activity fragment is inflated.
        try {
            if (imgURLs.size() > 0) {
                setImage(imgURLs.get(0), galleryImage, mAppend);
                mSelectedImage = imgURLs.get(0);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.d(TAG, "setupGridView: ArrayIndexOutOfBoundsException: " + e.getMessage());
        }
    }

    private void setImage(String imgURL, ImageView image, String append) {
        Log.d(TAG, "setImage: setting image");

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(append + imgURL, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
