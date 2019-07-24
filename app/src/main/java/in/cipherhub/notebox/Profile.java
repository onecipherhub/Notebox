package in.cipherhub.notebox;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import in.cipherhub.notebox.registration.SignIn;

public class Profile extends Fragment implements View.OnClickListener {

    String TAG = "ProfileOX";

    FirebaseAuth firebaseAuth;
    SharedPreferences localDB;
    JSONObject userObject;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        localDB = getActivity().getSharedPreferences("localDB", Context.MODE_PRIVATE);


        Button logOut_B = rootView.findViewById(R.id.logOut_B);
        TextView fullName_TV = rootView.findViewById(R.id.fullName_TV);
        TextView instituteName_TV = rootView.findViewById(R.id.instituteName_TV);
        TextView courseName_TV = rootView.findViewById(R.id.courseName_TV);
        TextView branchName_TV = rootView.findViewById(R.id.branchName_TV);
        ImageButton editUserDetails_IB = rootView.findViewById(R.id.editUserDetails_IB);
        RatingBar rating_RB = rootView.findViewById(R.id.rating_RB);
        TextView rating_TV = rootView.findViewById(R.id.rating_TV);
        TextView userUploadsCount_TV = rootView.findViewById(R.id.userUploadsCount_TV);
        TextView userDownloadsCount_TV = rootView.findViewById(R.id.userDownloadsCount_TV);


        try {
            userObject = new JSONObject(localDB.getString("user", "Error Fetching..."));

            rating_RB.setRating((float) userObject.getDouble("rating"));
            fullName_TV.setText(userObject.getString("full_name"));
            instituteName_TV.setText(userObject.getString("institute"));
            courseName_TV.setText(userObject.getString("course"));
            rating_TV.setText(String.valueOf(userObject.getInt("rating")));
            userDownloadsCount_TV.setText(String.valueOf(userObject.getInt("total_downloads")));
            branchName_TV.setText(userObject.getString("branch"));
            userUploadsCount_TV.setText(String.valueOf(userObject.getInt("total_uploads")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        logOut_B.setOnClickListener(this);
        editUserDetails_IB.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.logOut_B:
                firebaseAuth.signOut();
                getActivity().finish();
                startActivity(new Intent(getContext(), SignIn.class));
                getActivity().overridePendingTransition(R.anim.fade_in, 0);
                break;

            case R.id.editUserDetails_IB:
                Intent intent = new Intent(getActivity(), SignIn.class);
                intent.putExtra("isEmailVerified", true);
                intent.putExtra("isDetailsFilled", false);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, 0);
                break;
        }
    }
}
  