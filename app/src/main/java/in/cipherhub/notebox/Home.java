package in.cipherhub.notebox;

import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import in.cipherhub.notebox.Adapters.AdapterHomeSubjects;
import in.cipherhub.notebox.Adapters.AdapterRecentViews;
import in.cipherhub.notebox.Models.DataHomeSubjectsItem;

public class Home extends Fragment implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    AdapterHomeSubjects homeSubjectAdapter;
    List<DataHomeSubjectsItem> homeSubjects;

    private String TAG = "homeOX";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

//        mAuth.signOut();

        final ConstraintLayout subjectsLayout_CL = rootView.findViewById(R.id.subjectsLayout_CL);
        final ConstraintLayout recentViewsLayout_CL = rootView.findViewById(R.id.recentViewsLayout_CL);
        final EditText subjectsSearch_ET = rootView.findViewById(R.id.subjectsSearch_ET);
        final ImageButton searchIconInSearchBar_IB = rootView.findViewById(R.id.searchIconInSearchBar_IB);
        Button signin_B = rootView.findViewById(R.id.signin_B);
        LinearLayout notSignedInTemplate_LL = rootView.findViewById(R.id.notSignedInTemplate_LL);
        RecyclerView recentViews_RV = rootView.findViewById(R.id.recentViews_RV);
        RecyclerView homeSubjects_RV = rootView.findViewById(R.id.homeSubjects_RV);
        ImageButton bookmark_IB = rootView.findViewById(R.id.bookmark_IB);

        if (user == null) {
            homeSubjects_RV.setVisibility(View.GONE);
            subjectsSearch_ET.setFocusable(false);
            notSignedInTemplate_LL.setVisibility(View.VISIBLE);
        } else {
            homeSubjects_RV.setVisibility(View.VISIBLE);
            subjectsSearch_ET.setFocusable(true);
            notSignedInTemplate_LL.setVisibility(View.GONE);
        }

        signin_B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).openBottomTemplate();
            }
        });

        bookmark_IB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(getActivity(), BookmarkActivity.class));
            }
        });

        subjectsSearch_ET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(subjectsSearch_ET.isFocused()) {
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

        homeSubjects = new ArrayList<>();

        homeSubjectAdapter = new AdapterHomeSubjects(homeSubjects);
        homeSubjects_RV.setAdapter(homeSubjectAdapter);
        homeSubjects_RV.setLayoutManager(new LinearLayoutManager(getActivity()));

        subjectsSearch_ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });

        return rootView;
    }

    private void filter(String text) {
        List<DataHomeSubjectsItem> filteredList = new ArrayList<>();

        for (DataHomeSubjectsItem s : homeSubjects) {
        //new array list that will hold the filtered data
            //if the existing elements contains the search input
            if (s.subName.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(s);
            }
        }
        homeSubjectAdapter.filterList(filteredList);
    }

    @Override
    public void onClick(View view) {

    }
}

// TODO: recycler view heights stays short after keyboard is hidden... solve it :)
// TODO: when letter length exceeds, use dots to end the sentence...