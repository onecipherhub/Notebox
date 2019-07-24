package in.cipherhub.notebox.registration;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import in.cipherhub.notebox.R;
import in.cipherhub.notebox.utils.Internet;

import static android.content.Context.MODE_PRIVATE;

public class LogIn extends Fragment implements View.OnClickListener {

    private String TAG = "LogIn";

    Button logIn_B;
    EditText email_ET, password_ET;
    TextView forgotPassword_TV, signUp_TV, show_Password;
    View email_V, password_V;

    FirebaseAuth firebaseAuth;

    ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        logIn_B = rootView.findViewById(R.id.logIn_B);
        email_ET = rootView.findViewById(R.id.email_ET);
        password_ET = rootView.findViewById(R.id.password_ET);
        show_Password = rootView.findViewById(R.id.show_Password);
        forgotPassword_TV = rootView.findViewById(R.id.forgotPassword_TV);
        signUp_TV = rootView.findViewById(R.id.signUp_TV);
        email_V = rootView.findViewById(R.id.email_V);
        password_V = rootView.findViewById(R.id.password_V);

        firebaseAuth = FirebaseAuth.getInstance();

        //Initialize Progress Dialog
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Logging In...");
        progressDialog.setCancelable(false);
        FirebaseFirestore.getInstance().collection("institutes").whereEqualTo("name", "Nitte Meenakshi Institute of Technology")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, String.valueOf(task.getResult()));
                } else {
                    Log.d(TAG, String.valueOf(task.getException()));
                }
            }
        });
        logIn_B.setOnClickListener(this);
        forgotPassword_TV.setOnClickListener(this);
        signUp_TV.setOnClickListener(this);

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
                if (!password_ET.getText().toString().equals("") && password_ET.getText().length() > 0) {

//                    Log.i(TAG,"typing");

                    password_V.setBackgroundColor(getResources().getColor(R.color.colorAppTheme));
                    show_Password.setVisibility(View.VISIBLE);
                }
                else {
                    password_V.setBackgroundColor(getResources().getColor(R.color.colorGray_AAAAAA));
                    show_Password.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        show_Password.setVisibility(View.GONE);

        show_Password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(show_Password.getText().toString().equals("SHOW")){
//                    Log.i(TAG,"showing password");
                    show_Password.setText("HIDE");
                    password_ET.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    password_ET.setSelection(password_ET.length());


                }
                else {

//                    Log.i(TAG,"hiding password");

                    show_Password.setText("SHOW");
                    password_ET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    password_ET.setSelection(password_ET.length());
                }
            }
        });



        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.forgotPassword_TV:
                ((SignIn) getActivity()).changeFragment(new ForgotPassword(), true, true);
                break;

            case R.id.signUp_TV:
                ((SignIn) getActivity()).changeFragment(new SignUp(), true, true);
                break;

            case R.id.logIn_B:
                // write to login using email and password

                progressDialog.show();

                String filledEmail = email_ET.getText().toString();
                String filledPassword = password_ET.getText().toString();

                if (new Internet(getActivity()).isAvailable())
                    // have internet to use signup service

                    if (!TextUtils.isEmpty(filledEmail)
                            && android.util.Patterns.EMAIL_ADDRESS.matcher(filledEmail).matches()
                            && filledPassword.length() >= 8)
                        // email address is valid and password is greater than 8-digits

                        firebaseAuth.signInWithEmailAndPassword(filledEmail, filledPassword)
                                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information

                                            Toast.makeText(getActivity(), "LogIn Success!", Toast.LENGTH_SHORT).show();

                                            getActivity().getSharedPreferences("user", MODE_PRIVATE)
                                                    .edit().putBoolean("isDetailsFilled", true).apply();

                                            ((SignIn) getActivity()).openHomePage();

                                            // will store the user details to shared preferences
                                            pullUserDetails();
                                        } else {
                                            // If sign in fails, display a message to the user.

                                            if (task.getException() != null)
                                                try {
                                                    if (task.getException().getMessage().contains("no user record")) {
                                                        Toast.makeText(getActivity(), "Login Failed!\nUser E-mail does not exists!", Toast.LENGTH_SHORT).show();
                                                    }
                                                } catch (Exception e) {
                                                    Log.e(TAG, String.valueOf(e));
                                                } finally {
                                                    // If sign in fails, display a message to the user.
                                                    Toast.makeText(getActivity(), "Authentication failed.",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                        }

                                        progressDialog.dismiss();
                                    }
                                });
                    else if (filledPassword.length() < 8) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Password should be greater than '8' characters", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Invalid E-mail or Password!!", Toast.LENGTH_SHORT).show();
                    }
                break;
        }
    }

    // will store the user details to shared preferences
    public void pullUserDetails() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        DocumentReference documentReference = db.collection("users").document(user.getUid());

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        editor.putString("full_name", String.valueOf(document.getData().get("full_name")));
                        editor.putString("institute", String.valueOf(document.getData().get("institute")));
                        editor.putString("course", String.valueOf(document.getData().get("course")));
                        editor.putString("branch", String.valueOf(document.getData().get("branch")));
                        editor.apply();
                    } else {
                        Log.d(TAG, "User was logged but does not has user details entry!");
                    }
                } else {
                    Log.d(TAG, "Failed to fetch user details with error: ", task.getException());
                }
            }
        });
    }
}
