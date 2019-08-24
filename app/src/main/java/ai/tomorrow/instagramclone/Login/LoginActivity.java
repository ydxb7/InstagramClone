package ai.tomorrow.instagramclone.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ai.tomorrow.instagramclone.Home.HomeActivity;
import ai.tomorrow.instagramclone.R;
import ai.tomorrow.instagramclone.Utils.FirebaseMethods;
import ai.tomorrow.instagramclone.Utils.Helpers;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods mFirebaseMethods;

    private Context mContext;
    private ProgressBar mProgressBar;
    private EditText mEmail, mPassword;
    private TextView mPleaseWait, mSendEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: started.");

        mContext = LoginActivity.this;
        mProgressBar = findViewById(R.id.progressBar);
        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mPleaseWait = findViewById(R.id.pleaseWait);
        mSendEmail = findViewById(R.id.link_send_email);
        mFirebaseMethods = new FirebaseMethods(mContext);

        mPleaseWait.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);

        setupFirebaseAuth();
        init();
    }

    private boolean isStringNull(String string) {
        return string.equals("");
    }

    /**
     * -------------------------------- firebase --------------------------
     */
    private void init() {
        // initialize the button to logging in
        Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helpers.hideSoftKeyboard(LoginActivity.this);
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if (isStringNull(email) || isStringNull(password)) {
                    Toast.makeText(mContext, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mPleaseWait.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        try {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            if (user.isEmailVerified()) {
                                                Intent intent = new Intent(mContext, HomeActivity.class);
                                                startActivity(intent);
                                                LoginActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                            } else {
                                                Toast.makeText(mContext, "Email is not verified \n Please check your email inbox", Toast.LENGTH_SHORT).show();
                                                mAuth.signOut();
                                            }
                                        } catch (NullPointerException e) {
                                            Log.d(TAG, "onComplete: NullPointerException: " + e.getMessage());
                                        }

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, getString(R.string.auth_fail), task.getException());
                                        Toast.makeText(mContext, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    mProgressBar.setVisibility(View.GONE);
                                    mPleaseWait.setVisibility(View.GONE);
                                }
                            });
                }
            }
        });

        TextView linkSignUp = findViewById(R.id.link_signup);
        linkSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to register screen");
                Intent intent = new Intent(mContext, RegisterActivity.class);
                startActivity(intent);
                LoginActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        /**
         * If the user is logged in then navigate to 'HomeActivity' and call 'finish()'
         */
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(mContext, HomeActivity.class);
            startActivity(intent);
            finish();
            LoginActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }

        mSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: send email again.");
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    try {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user.isEmailVerified()) {
                                            Intent intent = new Intent(mContext, HomeActivity.class);
                                            startActivity(intent);
                                            LoginActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        } else {
                                            mFirebaseMethods.sendVerificationEmail();
                                            mAuth.signOut();
                                        }
                                    } catch (NullPointerException e) {
                                        Log.d(TAG, "onComplete: NullPointerException: " + e.getMessage());
                                    }

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, getString(R.string.auth_fail), task.getException());
                                    Toast.makeText(mContext, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

        });
    }

    /**
     * setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();

                if (user != null) {
                    // User is logged in
                    Log.d(TAG, "onAuthStateChanged: signed_in: " + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
