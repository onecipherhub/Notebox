package in.cipherhub.notebox;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.MenuItem;

import org.json.JSONObject;

import in.cipherhub.notebox.adapters.AdapterBottomSlider;
import in.cipherhub.notebox.fragments.Explore;
import in.cipherhub.notebox.fragments.Home;
import in.cipherhub.notebox.fragments.Profile;
import in.cipherhub.notebox.fragments.Upload;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = "MainActivity";

	public boolean checkPermission = false;
	private static final int STORAGE_PERM = 123;
	String[] perms;

	BottomNavigationView bottomNavigationView;

	AdapterBottomSlider bottomSlideAdapter;
	ViewPager viewPager;

	// Fragments
	Home home_fragment = new Home();
	Explore explore_fragment = new Explore();
	Upload upload_fragment = new Upload();
	Profile profile_fragment = new Profile();

	SharedPreferences localDB;
	SharedPreferences.Editor editor;
	FirebaseFirestore db;
	FirebaseAuth firebaseAuth;
	FirebaseUser user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getSupportActionBar().hide();
		askPermission();

		localDB = getSharedPreferences("localDB", MODE_PRIVATE);
		editor = localDB.edit();
		db = FirebaseFirestore.getInstance();
		firebaseAuth = FirebaseAuth.getInstance();
		user = firebaseAuth.getCurrentUser();

		// Calling the view pager for fragments
		viewPager = findViewById(R.id.view_pager);
		bottomNavigationView = findViewById(R.id.navigation);

		bottomSlideAdapter = new AdapterBottomSlider(getSupportFragmentManager());

		// Add fragments to list and populate
		bottomSlideAdapter.addFragment(home_fragment);
		bottomSlideAdapter.addFragment(explore_fragment);
		bottomSlideAdapter.addFragment(upload_fragment);
		bottomSlideAdapter.addFragment(profile_fragment);

		bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
		bottomNavigationView.setSelectedItemId(R.id.home_B);

		viewPager.setAdapter(bottomSlideAdapter);
		viewPager.addOnPageChangeListener(pageChangeListener);
		viewPager.setCurrentItem(0);
		viewPager.setOffscreenPageLimit(4);

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

	private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

		@Override
		public void onPageSelected(int position) {
			switch (position) {
				case 0:
					bottomNavigationView.setSelectedItemId(R.id.home_B);
					break;

				case 1:
					bottomNavigationView.setSelectedItemId(R.id.explore_B);
					break;

				case 2:
					bottomNavigationView.setSelectedItemId(R.id.upload_B);
					break;

				case 3:
					bottomNavigationView.setSelectedItemId(R.id.profile_B);
					break;

			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {}
	};


	private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
			= new BottomNavigationView.OnNavigationItemSelectedListener() {

		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item) {
			switch (item.getItemId()) {

				case R.id.home_B:
					viewPager.setCurrentItem(0);
					return true;

				case R.id.explore_B:
					viewPager.setCurrentItem(1);
					return true;

				case R.id.upload_B:
					viewPager.setCurrentItem(2);
					return true;

				case R.id.profile_B:
					viewPager.setCurrentItem(3);
					return true;

			}
			return false;
		}
	};

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		// Forward results to EasyPermissions
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}


	@AfterPermissionGranted(STORAGE_PERM)
	public void askPermission() {

		perms = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		if (EasyPermissions.hasPermissions(this, perms)) {
            Log.i(TAG, "Permission Granted");
			checkPermission = true;
		} else {
			// Do not have permissions, request them
			EasyPermissions.requestPermissions(this, "This app requires storage permission to function properly.",
					STORAGE_PERM, perms);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
}