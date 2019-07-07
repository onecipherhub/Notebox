package in.cipherhub.notebox;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Objects;

public class Profile extends Fragment implements View.OnClickListener {

    String TAG = "ProfileOXET";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        Button download_pdf = rootView.findViewById(R.id.download_pdf);
        download_pdf.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.download_pdf:

                // Create our main directory for storing files
                File directPath = new File(Environment.getExternalStorageDirectory() + "/Notebox");
                if (!directPath.exists()) {
                    boolean mkdir = directPath.mkdir();
                    if (!mkdir) {
                        Log.e(TAG, "Directory creation failed.");
                    }
                }

                Uri uri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/notebox-1559903149503.appspot.com/o/CSE%2FPYTHON%2FNumpy%20Exercises%20(Unit-1).pdf?alt=media&token=daa8d108-24c4-482b-b4c0-d5a6fb9e15df");

                DownloadManager mgr = (DownloadManager) Objects.requireNonNull(
                        getActivity()).getSystemService(Context.DOWNLOAD_SERVICE);

                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                        .setAllowedOverRoaming(false)
                        // can be named dynamically once we have database ready
//                        .setTitle("test1.pdf")
//                        .setDescription("Something useful? Maybe.")
                        .setDestinationInExternalPublicDir("/Notebox", "test.pdf")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                mgr.enqueue(request);

        }
    }
}
  