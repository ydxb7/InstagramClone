package ai.tomorrow.photory.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import ai.tomorrow.photory.R;

public class ConfirmPasswordDialog extends DialogFragment {

    private static final String TAG = "ConfirmPasswordDialog";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password, container, false);
        mPassword = view.findViewById(R.id.confirm_password);

        Log.d(TAG, "onCreateView: started");


        TextView confirmDialog = view.findViewById(R.id.dialogConfirm);
        confirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: capture password and confirming");
                String password = mPassword.getText().toString();
                if (!password.equals("")) {
                    mOnConfirmPasswordListener.onComfirmPasswordListener(password);
                } else {
                    Toast.makeText(getActivity(), "You must enter a password", Toast.LENGTH_SHORT).show();
                }
                getDialog().dismiss();
            }
        });


        TextView cancelDialog = view.findViewById(R.id.dialogCancel);
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: cancel the dialog");
                getDialog().dismiss();
            }
        });

        return view;
    }

    OnConfirmPasswordListener mOnConfirmPasswordListener;

    //vars
    EditText mPassword;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mOnConfirmPasswordListener = (OnConfirmPasswordListener) getTargetFragment();
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }


    public interface OnConfirmPasswordListener {
        void onComfirmPasswordListener(String password);
    }
}
