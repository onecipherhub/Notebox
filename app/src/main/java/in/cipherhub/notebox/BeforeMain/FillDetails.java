package in.cipherhub.notebox.BeforeMain;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.cipherhub.notebox.Adapters.AdapterBranchSelector;
import in.cipherhub.notebox.Models.ItemDataBranchSelector;
import in.cipherhub.notebox.Models.ItemDataHomeSubjects;
import in.cipherhub.notebox.R;

public class FillDetails extends Fragment {

    private String TAG = "FillDetailsOXET";

    FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_fill_details, container, false);

        final Button submit_B = rootView.findViewById(R.id.submit_B);
        final View fullName_V = rootView.findViewById(R.id.fullName_V);
        final EditText fullName_ET = rootView.findViewById(R.id.fullName_ET);
        final LinearLayout instituteAndCourseHolder_LL = rootView.findViewById(R.id.instituteAndCourseHolder_LL);
        final EditText branch_ET = rootView.findViewById(R.id.branch_ET);
        final View branch_V = rootView.findViewById(R.id.branch_V);
        final RecyclerView recyclerView = rootView.findViewById(R.id.branchSelectorList_RV);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        CollectionReference branches = db
                .collection("institutes").document("nmit_560064")
                .collection("courses").document("be")
                .collection("branches");

        branch_ET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (branch_ET.isFocused()) {
                    instituteAndCourseHolder_LL.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    instituteAndCourseHolder_LL.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });

        final List<ItemDataBranchSelector> list = new ArrayList<>();
        branches.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String branchName = String.valueOf(document.getData().get("name"));
                                String branchAbbColor = String.valueOf(document.getData().get("branch_abb_color"));
                                String totalUploads = String.valueOf(document.getData().get("total_uploads"));

                                list.add(new ItemDataBranchSelector(branchName
                                        , branchAbbColor, totalUploads
                                ));
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        final AdapterBranchSelector adapterBranchSelector = new AdapterBranchSelector(list);

        adapterBranchSelector.setOnItemClickListener(new AdapterBranchSelector.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                branch_ET.setText(list.get(position).getBranchName());
                instituteAndCourseHolder_LL.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                submit_B.setVisibility(View.VISIBLE);
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });

        recyclerView.setAdapter(adapterBranchSelector);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        branch_ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                submit_B.setVisibility(View.GONE);
                if (branch_ET.getText().toString().equals("")) {
                    branch_V.setBackgroundColor(getResources().getColor(R.color.colorGray_AAAAAA));
                } else {
                    branch_V.setBackgroundColor(getResources().getColor(R.color.colorAppTheme));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                List<ItemDataBranchSelector> filteredList = new ArrayList<>();

                for (ItemDataBranchSelector s : list) {
                    //new array list that will hold the filtered data
                    //if the existing elements contains the search input
                    if (s.getBranchName().toLowerCase().contains(branch_ET.getText().toString().toLowerCase())) {
                        filteredList.add(s);
                    }
                }
                adapterBranchSelector.filterList(filteredList);
            }
        });

        fullName_ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (fullName_ET.getText().toString().equals("")) {
                    fullName_V.setBackgroundColor(getResources().getColor(R.color.colorGray_AAAAAA));
                } else {
                    fullName_V.setBackgroundColor(getResources().getColor(R.color.colorAppTheme));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        submit_B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String filledFullName = fullName_ET.getText().toString();
                String filledBranchName = branch_ET.getText().toString();

                if (filledFullName.matches("[a-zA-z]+([ '-][a-zA-Z]+)*")) {
                    Map<String, Object> city = new HashMap<>();
                    city.put("name", filledFullName);
                    city.put("institute", "Nitte Meenakshi Institute of Technology");
                    city.put("course", "Bachelors in Engineering");
                    city.put("branch", filledBranchName);

                    db.collection("users").document(user.getUid())
                            .set(city)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully written!");

                                    SharedPreferences preferences = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error writing document", e);
                                }
                            });
                } else {
                    Toast.makeText(getActivity(), "Invalid Full Name!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }
}