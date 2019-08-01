package in.cipherhub.notebox.utils;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import in.cipherhub.notebox.R;
import in.cipherhub.notebox.adapters.AdapterHomeSubjects;
import in.cipherhub.notebox.models.ItemDataHomeSubjects;

public class tempFragment extends Fragment {

    AdapterHomeSubjects homeSubjectAdapter;
    List<ItemDataHomeSubjects> homeSubjects;

    private String TAG = "homeOX";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}
