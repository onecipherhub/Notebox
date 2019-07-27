package in.cipherhub.notebox.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import in.cipherhub.notebox.BookmarkActivity;
import in.cipherhub.notebox.R;
import in.cipherhub.notebox.registration.SignIn;

public class Profile extends Fragment implements View.OnClickListener {

    String TAG = "ProfileOX";
    Button reportbutton, sharebutton, feedbackbutton, aboutbutton, bookmarks_B;

    FirebaseAuth firebaseAuth;
    SharedPreferences localDB;
    JSONObject userObject;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        localDB = getActivity().getSharedPreferences("localDB", Context.MODE_PRIVATE);

        sharebutton = rootView.findViewById(R.id.share_b);
        reportbutton = rootView.findViewById(R.id.report_b);
        feedbackbutton = rootView.findViewById(R.id.feedback_b);
        aboutbutton = rootView.findViewById(R.id.about_b);
        bookmarks_B = rootView.findViewById(R.id.bookmarks_B);

        bookmarks_B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), BookmarkActivity.class));
            }
        });

        sharebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Insert Subject here");
                String app_url = "cipherhub.ml";
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, app_url);
                startActivity(Intent.createChooser(shareIntent, "Share via"));

            }
        });

        reportbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");

                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"onecipherhub@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Report your issues.");
                i.putExtra(Intent.EXTRA_TEXT   , "We will contact you soon. Please write in details.");
                //startActivity(Intent.createChooser(i, "Choose an Email client :"));

                try {
                    startActivity(Intent.createChooser(i, "Report your issues."));
                } catch (android.content.ActivityNotFoundException ex) {
                    //Toast.makeText(MyActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();

                }
            }
        });

        feedbackbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");

                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"onecipherhub@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Share your valuable feedback.");
                i.putExtra(Intent.EXTRA_TEXT   , "Your feedback is highly appreciated and will help us to improve.");
                //startActivity(Intent.createChooser(i, "Choose an Email client :"));

                try {
                    startActivity(Intent.createChooser(i, "Feedback"));
                } catch (android.content.ActivityNotFoundException ex) {
                    //Toast.makeText(MyActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();

                }

            }
        });

        aboutbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAbout(rootView);

            }
        });


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


    //About button
    private void showAbout(View v) {
        ViewGroup viewGroup = v.findViewById(android.R.id.content);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_about_profile, viewGroup, false);

        Button button = view.findViewById(R.id.webLink_B);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Under Development", Toast.LENGTH_LONG).show();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
//            case R.id.download_pdf:
//
//                // Create our main directory for storing files
//                File directPath = new File(Environment.getExternalStorageDirectory() + "/Notebox");
//                if (!directPath.exists()) {
//                    boolean mkdir = directPath.mkdir();
//                    if (!mkdir) {
//                        Log.e(TAG, "Directory creation failed.");
//                    }
//                }
//
//                Uri uri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/notebox-1559903149503.appspot.com/o/CSE%2FPYTHON%2FNumpy%20Exercises%20(Unit-1).pdf?alt=media&token=daa8d108-24c4-482b-b4c0-d5a6fb9e15df");
//
//                DownloadManager mgr = (DownloadManager) Objects.requireNonNull(
//                        getActivity()).getSystemService(Context.DOWNLOAD_SERVICE);
//
//                DownloadManager.Request request = new DownloadManager.Request(uri);
//                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
//                        .setAllowedOverRoaming(false)
//                        // can be named dynamically once we have database ready
////                        .setTitle("test1.pdf")
////                        .setDescription("Something useful? Maybe.")
//                        .setDestinationInExternalPublicDir("/Notebox", "test.pdf")
//                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//
//                mgr.enqueue(request);

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


