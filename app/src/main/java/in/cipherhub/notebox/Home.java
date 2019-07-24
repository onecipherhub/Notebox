package in.cipherhub.notebox;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    private String TAG = "homeOXET";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        // TODO: remove below line after testing
//         mAuth.signOut();

//        user.reload();
//        Log.d(TAG, user.getEmail() + " " + user.isEmailVerified());

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
                Toast.makeText(getActivity(), "Bookmarks feature is coming soon...", Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(getActivity(), BookmarkActivity.class));
            }
        });

        subjectsSearch_ET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (subjectsSearch_ET.isFocused()) {
                    subjectsLayout_CL.animate().translationY(-recentViewsLayout_CL.getHeight()).setDuration(500);
                    searchIconInSearchBar_IB.setImageDrawable(getResources().getDrawable(R.drawable.icon_down_arrow));
                } else {    // when click on background root Constraint Layout
                    subjectsLayout_CL.animate().translationY(0).setDuration(500);
                    searchIconInSearchBar_IB.setImageDrawable(getResources().getDrawable(R.drawable.icon_search));

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
                        , subjectName, subjectObject.getString("last_update")));
            }
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }

        homeSubjectAdapter = new AdapterHomeSubjects(homeSubjects);

        homeSubjectAdapter.setOnItemClickListener(new AdapterBranchSelector.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
//                startActivity(new Intent(getActivity(), PDFList.class));
            }
        });

        homeSubjects_RV.setAdapter(homeSubjectAdapter);
        homeSubjects_RV.setLayoutManager(new LinearLayoutManager(getActivity()));

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
            if (s.subName.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(s);
            }
        }
        homeSubjectAdapter.filterList(filteredList);
    }
}

// TODO: recycler view heights stays short after keyboard is hidden... solve it :)
