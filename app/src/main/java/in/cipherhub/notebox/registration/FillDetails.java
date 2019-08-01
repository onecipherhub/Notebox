package in.cipherhub.notebox.registration;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import in.cipherhub.notebox.adapters.AdapterBranchSelector;
import in.cipherhub.notebox.models.ItemDataBranchSelector;
import in.cipherhub.notebox.R;
import in.cipherhub.notebox.utils.Internet;

import static android.content.Context.MODE_PRIVATE;

public class FillDetails extends Fragment {

    private String TAG = "FillDetailsOXET";
    private boolean isBranchNameValid = false;

    FirebaseUser user;

    ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_fill_details, container, false);

        final Button submit_B = rootView.findViewById(R.id.submit_B);
        final View fullName_V = rootView.findViewById(R.id.fullName_V);
        final EditText fullName_ET = rootView.findViewById(R.id.fullName_ET);
        final EditText institute_ET = rootView.findViewById(R.id.institute_ET);
        final EditText course_ET = rootView.findViewById(R.id.course_ET);
        final AutoCompleteTextView branch_ACTV = rootView.findViewById(R.id.branch_ACTV);
        final LinearLayout instituteAndCourseHolder_LL = rootView.findViewById(R.id.instituteAndCourseHolder_LL);
        final View branch_V = rootView.findViewById(R.id.branch_V);
        final RecyclerView recyclerView = rootView.findViewById(R.id.branchSelectorList_RV);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Filling Up Details...");
        progressDialog.setCancelable(false);

        user = FirebaseAuth.getInstance().getCurrentUser();


        branch_ACTV.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    branch_ACTV.showDropDown();
                }
            }
        });

        final List<String> branchList = new ArrayList<>();

        if (new Internet(getActivity()).isAvailable()) {
            //get the branchList of institutes, courses and branches
            FirebaseFirestore.getInstance().collection("institutes")
                    .whereEqualTo("name", institute_ET.getText().toString())
                    .whereEqualTo("pin_code", 560064)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            JSONObject userInstitute = new JSONObject(document.getData());
                            try {

                                JSONObject branches = userInstitute
                                        .getJSONObject("courses")
                                        .getJSONObject(course_ET.getText().toString())
                                        .getJSONObject("branches");

                                Iterator<String> iterator = branches.keys();
                                while (iterator.hasNext()) {
                                    String key = iterator.next();
                                    JSONObject branchName = branches.getJSONObject(key);
                                    branchList.add(key);
                                }

                            } catch (JSONException e) {
                                Log.d(TAG, String.valueOf(e));
                            }
                        }

                        // Populate the Subjects
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                                R.layout.subject_recycler_view, R.id.lrg_text_view, branchList);
                        branch_ACTV.setAdapter(adapter);

                        branch_ACTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                isBranchNameValid = true;
                                submit_B.setVisibility(View.VISIBLE);
                                Log.d(TAG, String.valueOf(adapterView.getItemAtPosition(i)));
                            }
                        });
                    } else {
                        Log.d(TAG, String.valueOf(task.getException()));
                    }
                }
            });
        } else {
            Toast.makeText(getActivity(), "Try to restart Activity", Toast.LENGTH_SHORT).show();
        }

        fullName_ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (fullName_ET.getText().toString().equals("")) {
                    fullName_V.setBackgroundColor(getResources().getColor(R.color.colorGray_777777));
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

                progressDialog.show();

                String filledFullName = fullName_ET.getText().toString();
                String filledInstitute = institute_ET.getText().toString();
                String filledCourse = course_ET.getText().toString();
                String filledBranch = branch_ACTV.getText().toString();

                if (filledFullName.matches("[a-zA-z]+([ '-][a-zA-Z]+)*")) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    final Map<String, Object> userDetails = new HashMap<>();

                    final String[] userDetailsKeys = new String[]{"full_name", "institute", "course", "branch"};
                    final String[] userDetailsValues = new String[]{
                            filledFullName, filledInstitute, filledCourse, filledBranch
                    };

                    for (int i = 0; i < userDetailsKeys.length; i++) {
                        userDetails.put(userDetailsKeys[i], userDetailsValues[i]);
                    }

                    DocumentReference documentReference = db.collection("users").document(user.getUid());

                    documentReference.update(userDetails)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Toast.makeText(getActivity(),
                                            "Your Details has been registered for you better experience with Notebox!",
                                            Toast.LENGTH_LONG).show();

                                    getActivity().getSharedPreferences("user", MODE_PRIVATE)
                                            .edit().putBoolean("isDetailsFilled", true).apply();

                                    ((SignIn) getActivity()).openHomePage();
                                    progressDialog.dismiss();
                                }
                            });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Invalid Full Name!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }
}
