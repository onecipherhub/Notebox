package in.cipherhub.notebox;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

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

        bgBlurForBtmTemplate_V.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeBottomTemplate();
            }
        });

        applyInitButState(new Button[]{home_B, explore_B, upload_B, profile_B});

        // Home Page is launched onCreate to make it default
        buttonClicked = home_B;
        customButtonRadioGroup(buttonClicked);

        setListener();
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
                                    .whereEqualTo("name", snapshot.getData().get("institute"))
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots
                                                , @Nullable FirebaseFirestoreException e) {
                                            if (queryDocumentSnapshots != null) {
                                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                    editor.putString("institute"
                                                            , String.valueOf(new JSONObject(document.getData()))
                                                    ).apply();
                                                }
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
//
//        final View view = this.getCurrentFocus();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                signinTemplateContainer_FL.animate().translationY(0).setDuration(0);
//                signinTemplateContainer_FL.setVisibility(View.GONE);
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        getSupportFragmentManager().popBackStack();
//                    }
//                }, 100);
//
//                if (view != null) {
//                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                }
//            }
//        }, 500);
//
//        // to refresh the selected page // to remove current signin button
//        customButtonRadioGroup(buttonClicked);

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
            fragment = (Fragment) (Class.forName(getPackageName() + "." + buttonClickedTitle).newInstance());
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            fragment = new Home();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.mainPagesContainer_FL, fragment)
                .commit();
    }


    public void openBottomTemplate() {
        signinTemplateContainer_FL.setVisibility(View.VISIBLE);
        bgBlurForBtmTemplate_V.setClickable(true);

//        getSupportFragmentManager().beginTransaction()
//                .setCustomAnimations(R.anim.slide_btm_entry, R.anim.slide_top_exit, R.anim.slide_btm_entry, R.anim.slide_top_exit)
//                .replace(R.id.signinTemplateContainer_FL, new Signin())
//                .addToBackStack(null)
//                .commit();

        tintSystemBars(getWindow().getStatusBarColor(), getResources().getColor(R.color.colorGray_E0E0E0));

        bgBlurForBtmTemplate_V.setVisibility(View.VISIBLE);
        bgBlurForBtmTemplate_V.animate().alpha(0.12f).setDuration(500);
    }


    public void removeBottomTemplate() {
        tintSystemBars(getWindow().getStatusBarColor(), getResources().getColor(R.color.colorWhite_FFFFFF));

        signinTemplateContainer_FL.animate().translationY(signinTemplateContainer_FL.getHeight()).setDuration(500);

        bgBlurForBtmTemplate_V.animate().alpha(0).setDuration(500);

        final View view = this.getCurrentFocus();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                bgBlurForBtmTemplate_V.setVisibility(View.GONE);
                bgBlurForBtmTemplate_V.setClickable(false);
                signinTemplateContainer_FL.animate().translationY(0).setDuration(0);
                signinTemplateContainer_FL.setVisibility(View.GONE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getSupportFragmentManager().popBackStack();
                    }
                }, 100);

                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }, 500);

        // to refresh the selected page // to remove current signin button
        customButtonRadioGroup(buttonClicked);
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
