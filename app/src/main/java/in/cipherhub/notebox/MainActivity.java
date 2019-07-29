package in.cipherhub.notebox;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.json.JSONObject;

import java.util.Objects;

import in.cipherhub.notebox.fragments.Home;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "MainActivityOXET";
    public boolean checkPermission = false;
    private static final int STORAGE_PERM = 123;
    String[] perms;

    Button home_B, explore_B, upload_B, profile_B, buttonClicked;
    FrameLayout signinTemplateContainer_FL;
    View bgBlurForBtmTemplate_V;

    SharedPreferences localDB;
    SharedPreferences.Editor editor;
    FirebaseFirestore db;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).hide();
        startActivity(new Intent(MainActivity.this, MainActivity2.class));

        askPermission();

        localDB = getSharedPreferences("localDB", MODE_PRIVATE);
        editor = localDB.edit();
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        home_B = findViewById(R.id.home_B);
        explore_B = findViewById(R.id.explore_B);
        upload_B = findViewById(R.id.upload_B);
        profile_B = findViewById(R.id.profile_B);
        bgBlurForBtmTemplate_V = findViewById(R.id.bgBlurForBtmTemplate_V);
        signinTemplateContainer_FL = findViewById(R.id.signinTemplateContainer_FL);

        home_B.setOnClickListener(this);
        explore_B.setOnClickListener(this);
        upload_B.setOnClickListener(this);
        profile_B.setOnClickListener(this);

        applyInitButState(new Button[]{home_B, explore_B, upload_B, profile_B});

        // Home Page is launched onCreate to make it default
        buttonClicked = home_B;
        customButtonRadioGroup(buttonClicked);

        setListener();
//        MobileAds.initialize(this, new OnInitializationCompleteListener() {
//            @Override
//            public void onInitializationComplete(InitializationStatus initializationStatus) {
//
//                Log.i(TAG, "Ads Loaded!!!");
//
//            }
//        });
    }


    public void setListener() {
        db.collection("users").document(user.getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {

                        if (snapshot != null && snapshot.exists()) {
                            editor.putString("user", String.valueOf(new JSONObject(snapshot.getData())))
                                    .apply();

                            db.collection("institutes")
                                    .document(String.valueOf(snapshot.getData().get("institute")))
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        editor.putString("institute"
                                                , String.valueOf(new JSONObject(task.getResult().getData())))
                                                .apply();
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, "data: null");
                        }
                    }
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @AfterPermissionGranted(STORAGE_PERM)
    public void askPermission() {

        perms = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(getApplicationContext(), perms)) {
//            Log.i(TAG, "Permission Granted");
            checkPermission = true;

        } else {
            // Do not have permissions, request them
            EasyPermissions.requestPermissions(this, "This app requires storage permission to function properly.",
                    STORAGE_PERM, perms);
        }
    }


    @Override
    public void onClick(View view) {
        buttonClicked = findViewById(view.getId());
        /* Below line should be changed to switch condition if more buttons are introduced
         * registered with setOnClickListener(this) in onCreate method of this activity. */
        customButtonRadioGroup(buttonClicked);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        getWindow().setStatusBarColor(getResources().getColor(R.color.colorWhite_FFFFFF));
        bgBlurForBtmTemplate_V.setVisibility(View.GONE);
        bgBlurForBtmTemplate_V.setClickable(false);
    }


    // Make all choices in Bottom Navigation Bar as unfocused
    public void applyInitButState(Button[] buttons) {
        for (Button button : buttons) {
            // below line gives the name of icon with respect to button name
            int buttonIconDrawableId = getResources().getIdentifier(
                    "icon_" + button.getText().toString().toLowerCase() + "_but_unfocused",
                    "drawable", getPackageName());

            // set icons only on the top // icons position is adjusted from XML
            button.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                    getResources().getDrawable(buttonIconDrawableId), null, null);
            button.setTextColor(getResources().getColor(R.color.colorGray_777777));
        }
    }


    // select one choice in Bottom Navigation Bar and commit fragment transaction
    public void customButtonRadioGroup(Button buttonClicked) {
        String buttonClickedTitle = buttonClicked.getText().toString();
        Fragment fragment;

        applyInitButState(new Button[]{home_B, explore_B, upload_B, profile_B});

        int buttonClickedIconDrawableId = getResources().getIdentifier(
                "icon_" + buttonClickedTitle.toLowerCase() + "_but_focused",
                "drawable", getPackageName());
        buttonClicked.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
                getResources().getDrawable(buttonClickedIconDrawableId), null, null);
        buttonClicked.setTextColor(getResources().getColor(R.color.colorAppTheme));

        try {
            fragment = (Fragment) (Class.forName(getPackageName() + ".fragments." + buttonClickedTitle).newInstance());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            fragment = new Home();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.mainPagesContainer_FL, fragment)
                .commit();
    }


    private void tintSystemBars(final int fromThisColor, final int toThisColor) {

        ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // Use animation position to blend colors.
                float position = animation.getAnimatedFraction();

                // Apply blended color to the status bar.
                int blended = blendColors(fromThisColor, toThisColor, position);
                getWindow().setStatusBarColor(blended);

                // Apply blended color to the ActionBar.
//                blended = blendColors(toolbarColor, toolbarToColor, position);
                ColorDrawable background = new ColorDrawable(blended);
                getSupportActionBar().setBackgroundDrawable(background);
            }
        });

        anim.setDuration(500).start();
    }


    private int blendColors(int from, int to, float ratio) {
        final float inverseRatio = 1f - ratio;

        final float r = Color.red(to) * ratio + Color.red(from) * inverseRatio;
        final float g = Color.green(to) * ratio + Color.green(from) * inverseRatio;
        final float b = Color.blue(to) * ratio + Color.blue(from) * inverseRatio;

        return Color.rgb((int) r, (int) g, (int) b);
    }
}
