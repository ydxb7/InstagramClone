package ai.tomorrow.instagramclone.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ai.tomorrow.instagramclone.Profile.AccountSettingsActivity;
import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.Utils.Permissions;

public class PhotoFragment extends Fragment {
    private static final String TAG = "PhotoFragment";

    //constants
    private static final int PHOTO_FRAGMENT_NUM = 1;
    private static final int GALLERY_FRAGMENT_NUM = 0;
    private static final int CAMERA_REQUEST_CODE = 5;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        Log.d(TAG, "onCreateView: stated.");

        Button btnLaunchCamera = (Button) view.findViewById(R.id.btnLaunchCamera);
        btnLaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: launching camera.");
                if (((ShareActivity) getActivity()).getCurrentTabNumber() == PHOTO_FRAGMENT_NUM){
                    if (((ShareActivity) getActivity()).checkPermissions(Permissions.CAMERA_PERMISSION[0])){
                        Log.d(TAG, "onClick: starting camera");
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                        }
                    }
                } else {
                    Intent intent = new Intent(getActivity(), ShareActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });



        return view;
    }

    private boolean isRootTask(){
        if (((ShareActivity) getActivity()).getTask() == 0){
            return true;
        } else {
            // It's coming from EditProfileFragment
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE){
            Log.d(TAG, "onActivityResult: done taking a photo.");

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            // navigate to the final share screen to publish photo
            if (isRootTask()){
                Log.d(TAG, "onActivityResult: attempting to navigate to final share screen");
//                Intent intent = new Intent(getActivity(), NextActivity.class);
//                intent.putExtra(getString(R.string.selected_image), mSelectedImage);
//                startActivity(intent);
            } else {
                try {
                    Log.d(TAG, "onActivityResult: received new bitmap from camera: " + bitmap);
                    Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();
                }catch (NullPointerException e){
                    Log.e(TAG, "onActivityResult: NullPointerException: " + e.getMessage() );
                }
            }

        }


    }
}
