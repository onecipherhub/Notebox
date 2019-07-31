package in.cipherhub.notebox.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import in.cipherhub.notebox.BookmarkActivity;
import in.cipherhub.notebox.R;
import in.cipherhub.notebox.adapters.AdapterBranchSelector;
import in.cipherhub.notebox.adapters.AdapterHomeSubjects;
import in.cipherhub.notebox.adapters.AdapterRecentViews;
import in.cipherhub.notebox.models.ItemDataHomeSubjects;

import static android.content.Context.MODE_PRIVATE;

public class Home extends Fragment {

	private FirebaseAuth mAuth;
	private FirebaseUser user;
	private FirebaseFirestore db;

	AdapterHomeSubjects homeSubjectAdapter;
	List<ItemDataHomeSubjects> homeSubjects;

	// for ads
	private static final String BANNER_APP_ID = "ca-app-pub-3940256099942544/6300978111";
	private static int ITEM_PER_AD = 1;




	private static final String TAG = "HomePage";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_home, container, false);

		// Initialize Firebase Auth
		db = FirebaseFirestore.getInstance();
		mAuth = FirebaseAuth.getInstance();
		user = mAuth.getCurrentUser();

		final ConstraintLayout subjectsLayout_CL = rootView.findViewById(R.id.subjectsLayout_CL);
		final ConstraintLayout recentViewsLayout_CL = rootView.findViewById(R.id.recentViewsLayout_CL);
		final EditText subjectsSearch_ET = rootView.findViewById(R.id.pdfSearch_ET);
		TextView noRecentViews_TV = rootView.findViewById(R.id.noRecentViews_TV);
		final ImageButton searchIconInSearchBar_IB = rootView.findViewById(R.id.searchIconInSearchBar_IB);

		RecyclerView recentViews_RV = rootView.findViewById(R.id.recentViews_RV);
		RecyclerView homeSubjects_RV = rootView.findViewById(R.id.homeSubjects_RV);
		ImageButton bookmark_IB = rootView.findViewById(R.id.bookmarks_IB);

		bookmark_IB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getActivity(), BookmarkActivity.class);
				startActivity(intent);
			}
		});

		subjectsSearch_ET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean b) {
				if (subjectsSearch_ET.isFocused()) {
//					subjectsLayout_CL.animate().translationY(-recentViewsLayout_CL.getHeight()).setDuration(500);
//					searchIconInSearchBar_IB.setImageDrawable(getResources().getDrawable(R.drawable.icon_down_arrow));
				} else {    // when click on background root Constraint Layout
//					subjectsLayout_CL.animate().translationY(0).setDuration(500);
//					searchIconInSearchBar_IB.setImageDrawable(getResources().getDrawable(R.drawable.icon_search));

					// to hide the keyboard
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(subjectsSearch_ET.getWindowToken(), 0);
				}
			}
		});

		List<AdapterRecentViews.recentViewsItemData> recentViews = new ArrayList<>();

		AdapterRecentViews recentViewsAdapter = new AdapterRecentViews(recentViews);
		recentViews_RV.setAdapter(recentViewsAdapter);
		recentViews_RV.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

		if (recentViews.isEmpty()) {
			noRecentViews_TV.setVisibility(View.VISIBLE);
		}

		homeSubjects = new ArrayList<>();

		homeSubjectAdapter = new AdapterHomeSubjects(homeSubjects, true);

		homeSubjects_RV.setAdapter(homeSubjectAdapter);
		homeSubjects_RV.setLayoutManager(new LinearLayoutManager(getActivity()));
		try {
			SharedPreferences localDB = getActivity().getSharedPreferences("localDB", MODE_PRIVATE);

			JSONObject userObject = new JSONObject(localDB.getString("user", "Error Fetching..."));

			JSONObject institute = new JSONObject(localDB.getString("institute", "Error Fetching..."));

			JSONObject subjects = institute.getJSONObject("courses").getJSONObject(userObject.getString("course"))
					.getJSONObject("branches").getJSONObject(userObject.getString("branch"))
					.getJSONObject("subjects");

			Iterator<String> iterator = subjects.keys();
			while (iterator.hasNext()) {
				String subjectName = iterator.next();
				JSONObject subjectObject = subjects.getJSONObject(subjectName);
				homeSubjects.add(new ItemDataHomeSubjects(subjectObject.getString("abbreviation")
						,subjectName, subjectObject.getString("last_update")));
			}

			homeSubjectAdapter.filterList(homeSubjects);

		} catch (JSONException e) {
			Log.d(TAG, e.getMessage());
		}

		subjectsSearch_ET.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

			@Override
			public void afterTextChanged(Editable editable) {
				filter(editable.toString());
			}
		});

		return rootView;
	}


	private void filter(String text) {
		List<ItemDataHomeSubjects> filteredList = new ArrayList<>();

		for (ItemDataHomeSubjects s : homeSubjects) {
			//new array list that will hold the filtered data
			//if the existing elements contains the search input
			if(s != null)
			if (s.subName.toLowerCase().contains(text.toLowerCase())
							|| s.subAbb.toLowerCase().contains(text.toLowerCase())
							|| s.lastUpdate.toLowerCase().contains(text.toLowerCase())) {
				filteredList.add(s);
			}
		}
		homeSubjectAdapter.filterList(filteredList);
	}
}
