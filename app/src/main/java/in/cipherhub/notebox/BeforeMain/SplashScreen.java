package in.cipherhub.notebox.BeforeMain;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import in.cipherhub.notebox.MainActivity;
import in.cipherhub.notebox.R;
import in.cipherhub.notebox.Utils.Internet;

public class SplashScreen extends AppCompatActivity {

    private String TAG = "SplashScreenOXET";
    private int RC_SIGN_IN = 1234;

    FirebaseUser user = null;
    FirebaseAuth firebaseAuth;
    GoogleSignInClient mGoogleSignInClient;

    Button googleSignin_B;
    FrameLayout signInContainer_FL;
    ImageView finalLogo_IV;
    TextView cipherHub_TV, by_TV;
    View blueBg_v, whiteBg_V;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Hide the actionbar and set FULLSCREEN flag - for design
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // instantiate views other then the one which are inside fragments
        // those cannot be instantiated here
        googleSignin_B = findViewById(R.id.googleSignin_B);
        signInContainer_FL = findViewById(R.id.signInContainer_FL);
        finalLogo_IV = findViewById(R.id.finalLogo_IV);
        cipherHub_TV = findViewById(R.id.cipherHub_TV);
        by_TV = findViewById(R.id.by_TV);
        blueBg_v = findViewById(R.id.blueBg_V);
        whiteBg_V = findViewById(R.id.whiteBg_V);

        // Get the last user which signed in
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        // GoogleSignInOptions will mention what and all we need
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // Specifies that an ID token for authenticated users is requested.
                .requestIdToken(getString(R.string.default_web_client_id))
                // Specifies that email info is requested by your application.
                .requestEmail()
                .build();

        // GoogleSignInClient to return the user details requested
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // if the user does not exist then fade out the white screen which is hiding login template
        if (user == null) {
            splashScreenCloseAnim(false);
        }
        // is user has registered but the email verification is pending
        else if (user.isEmailVerified()) {
            splashScreenCloseAnim(false);
        }
        // if user exists then don't fade out the white screen which is hiding the login template
        // and by pass straight to Home Page
        else {
            splashScreenCloseAnim(true);
        }

        // set default fragment as Login fragment which will open if the user has not registered yet
        changeFragment(new LogIn(), false);

        // Google button which is lying outside every fragment
        googleSignin_B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (new Internet(getBaseContext()).isAvailable())
                    startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
            }
        });
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
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // authenticate with Firebase
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
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
                            // Sign in success!
                            Toast.makeText(SplashScreen.this, "Google Signin Success!", Toast.LENGTH_SHORT).show();
                            doneWithSignIn();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SplashScreen.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void splashScreenCloseAnim(final Boolean completeClose) {

        finalLogo_IV.animate().scaleX(0).setDuration(1000);
        finalLogo_IV.animate().scaleY(0).setDuration(1000);
        by_TV.animate().alpha(0).setDuration(1000);
        cipherHub_TV.animate().alpha(0).setDuration(1000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                // slide blue background to complete screen width
                blueBg_v.animate().translationX(getWindow().getDecorView().getWidth() >> 1).setDuration(700);

                // wait for above animation to end and little extra time for smooth feel
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // if the complete close boolean is true then get directly to Home Page
                        if (completeClose) {
                            doneWithSignIn();
                        } else {
                            // else show the status bar & fade out white bg which is used to cover the login page
                            // while Login page is appearing
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                            whiteBg_V.animate().alpha(0).setDuration(500);
                        }
                    }
                }, 1000);
            }
        }, 1200);
    }


    // when the signin is complete and we have the required information of the user
    public void doneWithSignIn() {

        startActivity(new Intent(SplashScreen.this, MainActivity.class));
        overridePendingTransition(R.anim.fade_in, 0);
    }


    // change fragment at every step of signin process
    public void changeFragment(Fragment fragment, Boolean addBackStack) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.signInContainer_FL, new FillDetails());

        // other than login fragment all other template should have back button compatibility
        if (addBackStack)
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


}

