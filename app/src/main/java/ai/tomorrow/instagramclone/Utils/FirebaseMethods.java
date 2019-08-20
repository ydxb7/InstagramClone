package ai.tomorrow.instagramclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import ai.tomorrow.instagramclone.Home.HomeActivity;
import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.models.Comment;
import ai.tomorrow.instagramclone.models.Like;
import ai.tomorrow.instagramclone.models.Photo;
import ai.tomorrow.instagramclone.models.User;
import ai.tomorrow.instagramclone.models.UserAccountSettings;
import ai.tomorrow.instagramclone.models.UserSettings;

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userID;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;

    //vars
    private Context mContext;
    private double mPhotoUploadProgress = 0;

    public FirebaseMethods(Context mContext) {
        this.mContext = mContext;
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        if (mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public Photo getPhoto(DataSnapshot singleSnapshot) {
        Photo photo = new Photo();
        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
        photo.setCaption(objectMap.get(mContext.getString(R.string.field_caption)).toString());
        photo.setDate_created(objectMap.get(mContext.getString(R.string.field_date_created)).toString());
        photo.setImage_path(objectMap.get(mContext.getString(R.string.field_image_path)).toString());
        photo.setTags(objectMap.get(mContext.getString(R.string.field_tags)).toString());
        photo.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
        photo.setPhoto_id(objectMap.get(mContext.getString(R.string.field_photo_id)).toString());

        List<Like> likesList = new ArrayList<>();
        for (DataSnapshot ds : singleSnapshot.child(mContext.getString(R.string.field_likes)).getChildren()) {
            Like like = new Like();
            like.setLiked_by_user_id(ds.getValue(Like.class).getLiked_by_user_id());
            like.setLiked_to_user_id(ds.getValue(Like.class).getLiked_to_user_id());
            like.setDate_created(ds.getValue(Like.class).getDate_created());
            likesList.add(like);
        }

        List<Comment> commentList = new ArrayList<>();
        for (DataSnapshot ds : singleSnapshot.child(mContext.getString(R.string.field_comments)).getChildren()) {
            Comment comment = getComment(ds);
            commentList.add(comment);
        }

        photo.setLikes(likesList);
        photo.setComments(commentList);
        return photo;
    }

    //add new comment into firebase database
    public void addNewComment(String newComment, String photoID, String photoOwnerID, String commentToUserName) {
        Log.d(TAG, "addNewComment: adding new comment: " + newComment);

        String commentID = myRef.push().getKey();

        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        comment.setPhoto_id(photoID);
        comment.setReply_to_username(commentToUserName);
        comment.setDate_created(getTimeStamp());
        comment.setComment_id(commentID);


        //insert into photos node
        FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_photos))
                .child(photoID)
                .child(mContext.getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);

        //insert into user_photos node
        FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(photoOwnerID)
                .child(photoID)
                .child(mContext.getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);

    }

    public Comment getComment(DataSnapshot ds) {
        Comment comment = new Comment();

        Map<String, Object> objectMap = (HashMap<String, Object>) ds.getValue();
        comment.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
        comment.setPhoto_id(objectMap.get(mContext.getString(R.string.field_photo_id)).toString());
        comment.setComment_id(objectMap.get(mContext.getString(R.string.field_comment_id)).toString());
        comment.setReply_to_username(objectMap.get(mContext.getString(R.string.field_reply_to_username)).toString());
        comment.setComment(objectMap.get(mContext.getString(R.string.field_comment)).toString());
        comment.setDate_created(objectMap.get(mContext.getString(R.string.field_date_created)).toString());

        List<Like> likesList = new ArrayList<>();
        for (DataSnapshot likeSnapshot : ds.child(mContext.getString(R.string.field_likes)).getChildren()) {
            Like like = new Like();
            like.setLiked_by_user_id(likeSnapshot.getValue(Like.class).getLiked_by_user_id());
            like.setLiked_to_user_id(likeSnapshot.getValue(Like.class).getLiked_to_user_id());
            like.setDate_created(likeSnapshot.getValue(Like.class).getDate_created());
            likesList.add(like);
        }

        comment.setLikes(likesList);
        return comment;
    }

    public void removeCommentLike(String photoID, String photoUserID, String commentID){
        Log.d(TAG, "removeCommentLike: remove comment like: " + commentID);

        String currentUserID = mAuth.getCurrentUser().getUid();
        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(photoID)
                .child(mContext.getString(R.string.field_comments))
                .child(commentID)
                .child(mContext.getString(R.string.field_likes))
                .child(currentUserID)
                .removeValue();

        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(photoUserID)
                .child(photoID)
                .child(mContext.getString(R.string.field_comments))
                .child(commentID)
                .child(mContext.getString(R.string.field_likes))
                .child(currentUserID)
                .removeValue();

        myRef.child(mContext.getString(R.string.dbname_user_likes))
                .child(currentUserID)
                .child(mContext.getString(R.string.field_comment_likes))
                .child(commentID)
                .removeValue();
    }

    public void addCommentNewLike(String photoID, String photoUserID, String commentID, String commentOwnerID){
        Log.d(TAG, "addCommentNewLike: adding new like to comment: " + commentID);

        String currentUserID = mAuth.getCurrentUser().getUid();
        Like like = new Like();
        like.setLiked_by_user_id(mAuth.getCurrentUser().getUid());
        like.setLiked_to_user_id(commentOwnerID);

        like.setDate_created(getTimeStamp());

        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(photoID)
                .child(mContext.getString(R.string.field_comments))
                .child(commentID)
                .child(mContext.getString(R.string.field_likes))
                .child(currentUserID)
                .setValue(like);

        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(photoUserID)
                .child(photoID)
                .child(mContext.getString(R.string.field_comments))
                .child(commentID)
                .child(mContext.getString(R.string.field_likes))
                .child(currentUserID)
                .setValue(like);

        myRef.child(mContext.getString(R.string.dbname_user_likes))
                .child(currentUserID)
                .child(mContext.getString(R.string.field_comment_likes))
                .child(commentID)
                .setValue(like);
    }

    public void removePhotoLike(String photoID, String photoUserID){
        Log.d(TAG, "removePhotoLike: remove like.");
        String currentUserID = mAuth.getCurrentUser().getUid();

        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(photoID)
                .child(mContext.getString(R.string.field_likes))
                .child(mAuth.getCurrentUser().getUid())
                .removeValue();

        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(photoUserID)
                .child(photoID)
                .child(mContext.getString(R.string.field_likes))
                .child(mAuth.getCurrentUser().getUid())
                .removeValue();

        myRef.child(mContext.getString(R.string.dbname_user_likes))
                .child(currentUserID)
                .child(mContext.getString(R.string.field_photo_likes))
                .child(photoID)
                .removeValue();
    }

    public void addPhotoNewLike(String photoID, String photoUserID){
        Log.d(TAG, "addPhotoNewLike: adding new like to photo: " + photoID);

        String currentUserID = mAuth.getCurrentUser().getUid();
        Like like = new Like();
        like.setLiked_by_user_id(mAuth.getCurrentUser().getUid());
        like.setLiked_to_user_id(photoUserID);
        like.setDate_created(getTimeStamp());

        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(photoID)
                .child(mContext.getString(R.string.field_likes))
                .child(currentUserID)
                .setValue(like);

        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(photoUserID)
                .child(photoID)
                .child(mContext.getString(R.string.field_likes))
                .child(currentUserID)
                .setValue(like);

        myRef.child(mContext.getString(R.string.dbname_user_likes))
                .child(currentUserID)
                .child(mContext.getString(R.string.field_photo_likes))
                .child(photoID)
                .setValue(like);
    }

    public void uploadNewPhotos(String photoType, final String caption, int count, String imgUrl, Bitmap bm) {
        Log.d(TAG, "uploadNewPhotos: Attempting to upload new photo");
        final FilePaths filePaths = new FilePaths();
        mPhotoUploadProgress = 0;

        // case1) new photo
        if (photoType.equals(mContext.getString(R.string.new_photo))){
            Log.d(TAG, "uploadNewPhotos: Uploading NEW photo.");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference
                    .child(filePaths.FIRENASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (count + 1));

            // convert image url to bitmap
            if (bm == null){
                bm = ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            // upload the photo
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed");
                    Toast.makeText(mContext, "Photo upload failed.", Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = 100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();

                    if (progress - 15 > mPhotoUploadProgress){
                        Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }
                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });

            // After uploading a file, get the download Firebase URL to insert into 'photos' node and
            // 'user_photos' node
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        // after successfully upload the image, save the download url(firebase url) for
                        // that image in the database
                        Uri firebaseUrl = task.getResult();
                        Log.d(TAG, "onComplete: firebase url: " + firebaseUrl);
//                        Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show();

                        // add the new photo to 'photos' node and 'user_photos' node
                        addPhotoToDatabase(caption, firebaseUrl.toString());

                        // navigate to the main feed so the user can see their photo.
                        Intent intent = new Intent(mContext, HomeActivity.class);
                        mContext.startActivity(intent);

                    } else {
                        // Handle failures
                        Log.d(TAG, "onComplete: get firebase download url failed.");
                    }
                }
            });
        }
        // case2) profile photo
        else if (photoType.equals(mContext.getString(R.string.profile_photo))){
            Log.d(TAG, "uploadNewPhotos: Uploading new PROFILE photo.");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference
                    .child(filePaths.FIRENASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

            // convert image url to bitmap
            if (bm == null){
                bm = ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            // upload the photo
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed");
                    Toast.makeText(mContext, "Photo upload failed.", Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = 100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();

                    if (progress - 15 > mPhotoUploadProgress){
                        Toast.makeText(mContext, "photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }
                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });

            // After uploading a file, get the download Firebase URL to insert into 'photos' node and
            // 'user_photos' node
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        // after successfully upload the image, save the download url(firebase url) for
                        // that image in the database
                        Uri firebaseUrl = task.getResult();
                        Log.d(TAG, "onComplete: firebase url: " + firebaseUrl);
//                        Toast.makeText(mContext, "photo upload success", Toast.LENGTH_SHORT).show();

                        // insert into 'user_account_settings' node
                        setProfilePhoto(firebaseUrl);


                    } else {
                        // Handle failures
                        Log.d(TAG, "onComplete: get firebase download url failed.");
                    }
                }
            });
        }


    }

    private void setProfilePhoto(Uri firebaseUrl) {
        Log.d(TAG, "setProfilePhoto: setting new profile photo: " + firebaseUrl);
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(firebaseUrl.toString());
    }

    private String getTimeStamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        return sdf.format(new Date());
    }

    private void addPhotoToDatabase(String caption, String url){
        Log.d(TAG, "addPhotoToDatabase: add photo to database");

        String tags = StringManipulation.getTags(caption);
        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimeStamp());
        photo.setImage_path(url);
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);

        //insert into database
        myRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);
        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(newPhotoKey).setValue(photo);

    }


    public int getImageCount(DataSnapshot dataSnapshot) {
        int count = 0;
        for (DataSnapshot ds: dataSnapshot
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()){
            count++;
        }
        return count;
    }


    /**
     * update 'user_account_settings' node and 'users' node for the current user
     * @param displayName
     * @param website
     * @param description
     * @param phoneNumber
     */
    public void updateUserAccountSettings(String displayName, String website, String description, long phoneNumber){

        Log.d(TAG, "updateUserAccountSettings: updating user account settings");
        String currentUserID = mAuth.getCurrentUser().getUid();

        if (displayName != null){
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(currentUserID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName);
        }

        if (website != null){
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(currentUserID)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website);
        }

        if (description != null){
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(currentUserID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }

        if (phoneNumber != 0){
            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(currentUserID)
                    .child(mContext.getString(R.string.field_phone_number))
                    .setValue(phoneNumber);
        }

    }




    /**
     * update the username in 'users' node and 'user_acccount_settings' node
     * @param username
     */
    public void updateUsername(String username){
        Log.d(TAG, "updateUsername: updating username to: " + username);
        String currentUserID = mAuth.getCurrentUser().getUid();
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(currentUserID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(currentUserID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

    /**
     * update the email in the 'users' node
     * @param email
     */
    public void updateEmail(String email){
        Log.d(TAG, "updateUsername: updating emial to: " + email);
        String currentUserID = mAuth.getCurrentUser().getUid();
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(currentUserID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);
    }


    /**
     * Register a new email and password to Firebase Authentication
     * @param email
     * @param password
     */
    public void registerNewEmail(String email, String password){

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            // send verification email
                            sendVerificationEmail();

                            FirebaseUser user = mAuth.getCurrentUser();
                            userID = user.getUid();
                            Log.d(TAG, "createUserWithEmail:success, userID = " + userID);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void sendVerificationEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "sendVerificationEmail: user: " + user);
        if (user != null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(mContext, "Verification email has been sent to your email, " +
                                        "please check you email inbox.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, "Couldn't send verification email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }



    }



    /**
     * Add information to the users node
     * Add information to the user_account_settings node
     * @param email
     * @param username
     * @param description
     * @param website
     * @param profile_photo
     */
    public void addNewUser(String email, String username, String description, String website, String profile_photo){
        String currentUserID = mAuth.getCurrentUser().getUid();
        User user = new User(currentUserID, email, 0L, username);
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(currentUserID)
                .setValue(user);

        UserAccountSettings settings = new UserAccountSettings(
                description,
                username,
                profile_photo,
                StringManipulation.condenseUsername(username),
                website,
                currentUserID
        );
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(currentUserID)
                .setValue(settings);

    }


    /**
     * Retrieves the account settings for teh user currently logged in
     * Database: user_acount_Settings node
     * @param dataSnapshot
     * @return
     */
    public UserSettings getUserSettings(DataSnapshot dataSnapshot){
        Log.d(TAG, "getUserSettings: retrieving user account settings from firebase.");

        String currentUserID = mAuth.getCurrentUser().getUid();
        UserAccountSettings settings  = new UserAccountSettings();
        User user = new User();

        for(DataSnapshot ds: dataSnapshot.getChildren()){

            // user_account_settings node
            if(ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings))) {
                Log.d(TAG, "getUserSettings: user account settings node datasnapshot: " + ds);

                try {

                    settings.setDisplay_name(
                            ds.child(currentUserID)
                                    .getValue(UserAccountSettings.class)
                                    .getDisplay_name()
                    );
                    settings.setUsername(
                            ds.child(currentUserID)
                                    .getValue(UserAccountSettings.class)
                                    .getUsername()
                    );
                    settings.setWebsite(
                            ds.child(currentUserID)
                                    .getValue(UserAccountSettings.class)
                                    .getWebsite()
                    );
                    settings.setDescription(
                            ds.child(currentUserID)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription()
                    );
                    settings.setProfile_photo(
                            ds.child(currentUserID)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_photo()
                    );

                    Log.d(TAG, "getUserAccountSettings: retrieved user_account_settings information: " + settings.toString());
                } catch (NullPointerException e) {
                    Log.e(TAG, "getUserAccountSettings: NullPointerException: " + e.getMessage());
                }
            }


            // users node
            Log.d(TAG, "getUserSettings: snapshot key: " + ds.getKey());
            if(ds.getKey().equals(mContext.getString(R.string.dbname_users))) {
                Log.d(TAG, "getUserAccountSettings: users node datasnapshot: " + ds);

                user.setUsername(
                        ds.child(currentUserID)
                                .getValue(User.class)
                                .getUsername()
                );
                user.setEmail(
                        ds.child(currentUserID)
                                .getValue(User.class)
                                .getEmail()
                );
                user.setPhone_number(
                        ds.child(currentUserID)
                                .getValue(User.class)
                                .getPhone_number()
                );
                user.setUser_id(
                        ds.child(currentUserID)
                                .getValue(User.class)
                                .getUser_id()
                );

                Log.d(TAG, "getUserAccountSettings: retrieved users information: " + user.toString());
            }
        }
        return new UserSettings(user, settings);

    }

}
