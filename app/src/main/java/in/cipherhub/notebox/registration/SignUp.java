package in.cipherhub.notebox.registration;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import in.cipherhub.notebox.R;
import in.cipherhub.notebox.utils.Internet;

import static android.content.Context.MODE_PRIVATE;

public class SignUp extends Fragment {

    private String TAG = "SignUpOXET";

    Button signUp_B;
    EditText email_ET, password_ET, repeatPassword_ET;
    TextView logIn_TV;
    View email_V, password_V, repeatPassword_V;

    FirebaseAuth firebaseAuth;

    ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);

        signUp_B = rootView.findViewById(R.id.signUp_B);
        email_ET = rootView.findViewById(R.id.email_ET);
        password_ET = rootView.findViewById(R.id.password_ET);
        repeatPassword_ET = rootView.findViewById(R.id.repeatPassword_ET);
        logIn_TV = rootView.findViewById(R.id.logIn_TV);
        email_V = rootView.findViewById(R.id.email_V);
        password_V = rootView.findViewById(R.id.password_V);
        repeatPassword_V = rootView.findViewById(R.id.repeatPassword_V);

        firebaseAuth = FirebaseAuth.getInstance();

        //Initialize Progress Dialog
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Signing Up...");
        progressDialog.setCancelable(false);

        signUp_B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.show();

                String filledEmail = email_ET.getText().toString();
                String filledPassword = password_ET.getText().toString();
                String filledRepeatPassword = repeatPassword_ET.getText().toString();

                if (new Internet(getActivity()).isAvailable()) {
                    // have internet to use signup service

                    if (!TextUtils.isEmpty(filledEmail)
                            && android.util.Patterns.EMAIL_ADDRESS.matcher(filledEmail).matches()
                            && filledPassword.length() >= 8
                            && filledRepeatPassword.equals(filledPassword)) {
                        // email address is valid and password is greater than 8-digits
                        firebaseAuth.createUserWithEmailAndPassword(filledEmail, filledPassword)
                                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {

                                            progressDialog.dismiss();

                                            // Sign in success, update UI with the signed-in user's information
                                            Toast.makeText(getActivity(), "Signup Success!", Toast.LENGTH_SHORT).show();

                                            ((SignIn) getActivity()).changeFragment(new EmailVerification(),
                                                    true, true);

                                            FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Log.d(TAG, "sent email");
                                                            }
                                                        }
                                                    });
                                        } else {
                                            if (task.getException() != null)
                                                try {
                                                    if (task.getException().getMessage().contains("email address is already in use"))
                                                        Toast.makeText(getActivity(), "User E-mail already exists!", Toast.LENGTH_SHORT).show();
                                                } catch (Exception e) {
                                                    Log.e(TAG, String.valueOf(e));
                                                }
                                            else
                                                // If sign in fails, display a message to the user.
                                                Toast.makeText(getActivity(), "Authentication failed.",
                                                        Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    }
                                });
                    } else if (filledPassword.length() < 8) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Password should be greater than '8' characters", Toast.LENGTH_SHORT).show();
                    } else if (!filledRepeatPassword.equals(filledPassword)) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Password field and Repeat password field is different", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Invalid E-mail or Password!!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressDialog.dismiss();
                }
            }
        });

        logIn_TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        email_ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!email_ET.getText().toString().equals(""))
                    email_V.setBackgroundColor(getResources().getColor(R.color.colorAppTheme));
                else
                    email_V.setBackgroundColor(getResources().getColor(R.color.colorGray_777777));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        password_ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (password_ET.getText().toString().equals("")) {
                    password_V.setBackgroundColor(getResources().getColor(R.color.colorGray_777777));
                } else {
                    if (password_ET.getText().toString().equals(repeatPassword_ET.getText().toString())) {
                        repeatPassword_V.setBackgroundColor(getResources().getColor(R.color.google_green));
                    } else {
                        password_V.setBackgroundColor(getResources().getColor(R.color.colorAppTheme));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        repeatPassword_ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (repeatPassword_ET.getText().toString().equals("")) {
                    repeatPassword_V.setBackgroundColor(getResources().getColor(R.color.colorGray_777777));
                } else {
                    if (repeatPassword_ET.getText().toString().equals(password_ET.getText().toString()))
                        repeatPassword_V.setBackgroundColor(getResources().getColor(R.color.google_green));
                    else
                        repeatPassword_V.setBackgroundColor(getResources().getColor(R.color.colorAppTheme));
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return rootView;
    }
}
