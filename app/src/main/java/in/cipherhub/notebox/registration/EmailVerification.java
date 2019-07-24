package in.cipherhub.notebox.registration;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import in.cipherhub.notebox.R;

public class EmailVerification extends Fragment {

    private String TAG = "EmailVerificationOXET";

    Handler mHandler;
    FirebaseUser user;
    FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_email_verification, container, false);

        mHandler = new Handler();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        TextView emailVerificationEmail_TV = rootView.findViewById(R.id.emailVerificationEmail_TV);
        TextView resetEmail_TV = rootView.findViewById(R.id.resetEmail_TV);

        emailVerificationEmail_TV.setText(user.getEmail());
        resetEmail_TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            stopRepeatingTask();
                            ((SignIn) getActivity())
                                    .changeFragment(
                                            new SignUp()
                                            , false
                                            , true
                                    );
                        } else {
                            Toast.makeText(getActivity()
                                    , "Try again and check your internet connection..."
                                    , Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        startRepeatingTask();

        return rootView;

    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                user.reload();
                if (user.isEmailVerified()) {

                    getActivity().getSharedPreferences("user", Context.MODE_PRIVATE)
                            .edit().putBoolean("isDetailsFilled", true).apply();
                    Toast.makeText(getActivity(), "Your email has been verified!", Toast.LENGTH_SHORT).show();
                    ((SignIn) getActivity()).changeFragment(new FillDetails(), false, false);
                }
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, 1000);
            }
        }
    };


    public void startRepeatingTask() {
        mStatusChecker.run();
    }


    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopRepeatingTask();
    }
}
