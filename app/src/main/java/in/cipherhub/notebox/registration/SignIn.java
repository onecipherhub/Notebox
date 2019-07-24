package in.cipherhub.notebox.registration;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import in.cipherhub.notebox.MainActivity;
import in.cipherhub.notebox.R;
import in.cipherhub.notebox.utils.Internet;

public class SignIn extends AppCompatActivity {


    private String TAG = "SplashScreenOXET";
    private int RC_SIGN_IN = 1000;

    FirebaseFirestore db;
    FirebaseAuth firebaseAuth;
    FirebaseUser user = null;
    GoogleSignInClient mGoogleSignInClient;

    Button googleSignin_B;
    ConstraintLayout googleSignIn_CL;
    FrameLayout signInContainer_FL;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait while we authenticate...");
        progressDialog.setCancelable(false);

        // Hide the actionbar and set FULLSCREEN flag - for design
        getSupportActionBar().hide();

        // instantiate views other then the one which are inside fragments
        // those cannot be instantiated here
        googleSignin_B = findViewById(R.id.googleSignin_B);
        googleSignIn_CL = findViewById(R.id.googleSignIn_CL);
        signInContainer_FL = findViewById(R.id.signInContainer_FL);

        Bundle extras = getIntent().getExtras();
        boolean isEmailVerified = true, isDetailsFilled = true;
        if (extras != null) {
            isEmailVerified = extras.getBoolean("isEmailVerified");
            isDetailsFilled = extras.getBoolean("isDetailsFilled");
        }

        // Get the last user which signed in
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        // GoogleSignInOptions will mention what and all we need
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // Specifies that an ID token for authenticated users is requested.
//                .requestIdToken("728484278414-frop7tokkhf422tm7ru3kjtlbqljbk4t.apps.googleusercontent.com")
                // Specifies that email info is requested by your application.
                .requestEmail()
                .build();

        // GoogleSignInClient to return the user details requested
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        changeFragment(new LogIn(), false, true);
        if (user != null)
            // if user has sent an email to receive an e-mail Id
            if (!isEmailVerified) {
                // if e-mail is verified
                changeFragment(new EmailVerification(), true, true);
            } else if (!isDetailsFilled) {
                changeFragment(new FillDetails(), false, false);
            }

        // Google button which is lying outside every fragment
        googleSignin_B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (new Internet(getBaseContext()).isAvailable()) {
                    startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "back");

    }

    // for google signin, when the the user chooses which email he/she wants to login from
    // this function will have the desired result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // we can use onActivityResult for multiple purposes simultaneously so we mention REQUEST_CODE
        // with every launch of an intent to flag the launch of an intent and get result of every
        // intent here then write if condition with request code and use the result

        // Result returned from launching the Intent for Google Signin
        if (requestCode == RC_SIGN_IN) {

            progressDialog.show();

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // authenticate with Firebase
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {

                progressDialog.dismiss();
                // Google Sign In failed, update UI appropriately
                Log.d(TAG, "Google sign in failed", e);
            }
        }
    }


    // use Google credentials to signin with firebase authentication service
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {

        // Every user has IdToken in Google signin and we can put that token into GoogleAuthProvider
        // to get credentials necessary for firebase authentication
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        // not much info on how credential system works
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Google Sign in success!

                        }
                    }
                });
    }


    public void openHomePage() {
        user = firebaseAuth.getCurrentUser();
        db.collection("users").document(user.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    SharedPreferences localDB = getSharedPreferences("localDB", MODE_PRIVATE);
                    final SharedPreferences.Editor editor = localDB.edit();
                    editor.putString("user", String.valueOf(new JSONObject(task.getResult().getData())))
                            .apply();

                    db.collection("institutes")
                            .whereEqualTo("name", task.getResult().getData().get("institute"))
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    editor.putString("institute"
                                            , String.valueOf(new JSONObject(document.getData()))
                                    ).apply();

                                    // after loading data from firebase
                                    startActivity(new Intent(SignIn.this, MainActivity.class));
                                    overridePendingTransition(android.R.anim.fade_in, 0);
                                }
                            } else {
                                Log.w(TAG, "Error getting documents.", task.getException());
                            }
                        }
                    });
                }
            }
        });
    }


    // change fragment at every step of signin process
    public void changeFragment(Fragment fragment, Boolean addBackStack, Boolean keepGoogleSignInButton) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.signInContainer_FL, fragment);

        // other than login fragment all other template should have back button compatibility
        if (addBackStack) {
            fragmentTransaction.addToBackStack(null);
        } else {
            fragmentManager.popBackStack();
        }
        fragmentTransaction.commit();

        if (keepGoogleSignInButton) {
            googleSignIn_CL.setVisibility(View.VISIBLE);
        } else {
            googleSignIn_CL.setVisibility(View.GONE);
        }
    }
}
