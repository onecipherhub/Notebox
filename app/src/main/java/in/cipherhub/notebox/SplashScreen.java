package in.cipherhub.notebox;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import in.cipherhub.notebox.registration.SignIn;

public class SplashScreen extends AppCompatActivity {

    private String TAG = "SplashScreenOXET";

    FirebaseUser user = null;
    FirebaseAuth firebaseAuth;

    CardView appIcon_CV;
    ConstraintLayout appTitle_CL, company_CL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Hide the actionbar and set FULLSCREEN flag - for design
        getSupportActionBar().hide();
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // instantiate views other then the one which are inside fragments
        // those cannot be instantiated here
        appIcon_CV = findViewById(R.id.appIcon_CV);
        appTitle_CL = findViewById(R.id.appTitle_CL);
        company_CL = findViewById(R.id.companyTitle_CL);
        //cipherHub_TV = findViewById(R.id.cipherHub_TV);
        //by_TV = findViewById(R.id.by_TV);
        //blueBg_v = findViewById(R.id.blueBg_V);
        //whiteBg_V = findViewById(R.id.whiteBg_V);

        // Get the last user which signed in
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        // user has not logged in open registration page
        if (user == null)
            splashScreenCloseAnim(false);
        else if (!user.isEmailVerified() || !isDetailsFilled())
            splashScreenCloseAnim(false);
        else
            splashScreenCloseAnim(true);
    }


    private void splashScreenCloseAnim(final Boolean byPassRegistration) {

        //appTitle_CL.setVisibility(View.GONE);
        appIcon_CV.animate().scaleX(1).setDuration(1000);
        appIcon_CV.animate().scaleY(1).setDuration(1000);
        company_CL.animate().alpha(1).setDuration(1000);

        // wait for above animation to end and little extra time for smooth feel
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                appIcon_CV.animate().translationX(-200).setDuration(800);
                appTitle_CL.animate().alpha(1).setDuration(800);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent;
                        if (byPassRegistration) {
                            // open Main Home page
                            intent = new Intent(SplashScreen.this, MainActivity.class);

                        } else {
                            // open registration page
                            intent = new Intent(SplashScreen.this, SignIn.class);
                            if (user != null) {
                                intent.putExtra("isEmailVerified", user.isEmailVerified());
                                intent.putExtra("isDetailsFilled", isDetailsFilled());
                            }
                        }
                        startActivity(intent);
                        overridePendingTransition(0, R.anim.fade_out);
                    }
                },500);
            }
        }, 1200);


    }


    public boolean isDetailsFilled() {
        return getSharedPreferences("user", MODE_PRIVATE).getBoolean("isDetailsFilled", false);
    }
}
