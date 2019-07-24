package in.cipherhub.notebox;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import in.cipherhub.notebox.models.ItemDataHomeSubjects;

import static android.content.Context.MODE_PRIVATE;


public class Upload extends Fragment implements View.OnClickListener {

    private int REQUEST_PDF_PATH = 1000;
    private String TAG = "Upload";

    Button selectPDF_B, upload_button, unitOne_B, unitTwo_B, unitThree_B, unitFour_B, unitFive_B;
    Button[] allButtons;

    AutoCompleteTextView subjectName_ACTV;
    ConstraintLayout signin_CL;
    TextView pdfName_TV, pdfSize_TV, lrg_text_view;

    private StorageReference mStorageRef;
    ProgressDialog progressDialog;

    View rootView;
    Uri fileToUpload;

    String downloadUrl;
    String userInstitute = "Nitte Meenakshi Institute of Technology";
    String userCourse = "Bachelors in Engineering";
    String userBranch = "Computer Science and Engineering";
    String[] subjectListsShort;
    List<String> subjectLists;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_upload, container, false);

        // Firebase Storage
        mStorageRef = FirebaseStorage.getInstance().getReference();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Uploading File");
        progressDialog.setCancelable(false);

        // Instantiate Views
        selectPDF_B = rootView.findViewById(R.id.selectPDF_B);
        upload_button = rootView.findViewById(R.id.upload_button);

        unitOne_B = rootView.findViewById(R.id.unitOne_B);
        unitTwo_B = rootView.findViewById(R.id.unitTwo_B);
        unitThree_B = rootView.findViewById(R.id.unitThree_B);
        unitFour_B = rootView.findViewById(R.id.unitFour_B);
        unitFive_B = rootView.findViewById(R.id.unitFive_B);

        signin_CL = rootView.findViewById(R.id.signin_CL);
        pdfName_TV = rootView.findViewById(R.id.pdfName_TV);
        pdfSize_TV = rootView.findViewById(R.id.pdfSize_TV);
        subjectName_ACTV = rootView.findViewById(R.id.subjectName_ACTV);

        lrg_text_view = rootView.findViewById(R.id.lrg_text_view);

        subjectLists = new ArrayList<>();
        subjectListsShort = getResources().getStringArray(R.array.cse_subject);

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
                subjectLists.add(subjectName);
            }
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }

        // Populate the Subjects
        subjectName_ACTV.setAdapter(new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                R.layout.subject_recycler_view, R.id.lrg_text_view, subjectLists));

        // Initialize button activities
        selectPDF_B.setOnClickListener(this);
        upload_button.setOnClickListener(this);
        unitOne_B.setOnClickListener(this);
        unitTwo_B.setOnClickListener(this);
        unitThree_B.setOnClickListener(this);
        unitFour_B.setOnClickListener(this);
        unitFive_B.setOnClickListener(this);

        allButtons = new Button[]{unitOne_B, unitTwo_B, unitThree_B, unitFour_B, unitFive_B};
        setFileName();

        return rootView;
    }


    public void onClick(View v) {

        Button buttonClicked = rootView.findViewById(v.getId());

        if (buttonClicked == selectPDF_B) {

            if (((MainActivity) Objects.requireNonNull(this.getActivity())).checkPermission) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(Intent.createChooser(intent, "Select PDF"), REQUEST_PDF_PATH);
            } else {
                Toast.makeText(getContext(), "Permission not Granted", Toast.LENGTH_SHORT).show();
                ((MainActivity) Objects.requireNonNull(this.getActivity())).askPermission();
            }

        } else if (buttonClicked == upload_button) {

            if (subjectName_ACTV.getText().toString().equals("")) {

                Toast.makeText(getContext(), "Please Provide Full Details!", Toast.LENGTH_LONG).show();

            } else {

                String branch = getActivity().getSharedPreferences("users", Context.MODE_PRIVATE).getString("branch", "_");

                String subject = generateAbbreviation(subjectName_ACTV.getText().toString()).toLowerCase();

                mStorageRef = mStorageRef.child("notes/" + "nmit_560064/" + "be/" + branch + "/" + subject + "/" +
                        pdfName_TV.getText().toString());

                uploadFile();
            }


        } else {

            for (Button button : allButtons) {
                button.setBackground(getResources().getDrawable(R.drawable.bg_br_radius_gray_smaller));
                button.setTextColor(getResources().getColor(R.color.colorGray_777777));
            }

            buttonClicked.setBackground(getResources().getDrawable(R.drawable.bg_apptheme_pill_5));
            buttonClicked.setTextColor(getResources().getColor(R.color.colorWhite_FFFFFF));
            setFileName(buttonClicked);

        }

    }


    private void uploadFile() {

        progressDialog.show();

        mStorageRef.putFile(fileToUpload).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                        mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                downloadUrl = uri.toString();
                                Log.i(TAG, downloadUrl);

                                // Write into Firestore DB
                                writeNewInfoToDB(pdfName_TV.getText().toString(), downloadUrl);

                                Toast.makeText(getContext(), "File " + pdfName_TV.getText().toString() + " uploaded.",
                                        Toast.LENGTH_SHORT).show();

                                // Reset the URI and Reference to null so that we can initialize again when needed
                                fileToUpload = null;
                                mStorageRef = null;
                                progressDialog.dismiss();
                                pdfName_TV.setText("");

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                progressDialog.dismiss();

                                Log.i(TAG, "Failed to download the URL.");
                                Toast.makeText(getContext(), "Failed to Upload " + pdfName_TV.getText().toString() + " file.",
                                        Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d(TAG, "Failed to Upload the File");

                progressDialog.dismiss();
                Toast.makeText(getContext(), "Failed to upload " + pdfName_TV.getText().toString() + " file.",
                        Toast.LENGTH_SHORT).show();


            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                // percentage in progress dialog
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressDialog.setMessage("File: " + pdfName_TV.getText().toString() + "\n" + "Uploaded " + ((int) progress) + "%");
            }
        });

    }


    private void writeNewInfoToDB(String noteName, String url) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> pdf_details = new HashMap<>();
        pdf_details.put("rating", "4.2");
        pdf_details.put("no_of_downloads", "44");
        pdf_details.put("url", url);

        db.collection("institutes").document("nmit_560064")
                .collection("courses").document("be")
                .collection("branches").document("cse")
                .collection("notes")
                .document(noteName).set(pdf_details)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Log.d(TAG, "DocumentSnapshot successfully written!");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.w(TAG, "Error writing document", e);

                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PDF_PATH && resultCode == -1 && data != null && data.getData() != null) {

            Uri fileUri = data.getData();

            fileToUpload = Uri.parse(String.valueOf(fileUri));

            pdfName_TV.setText(getFileDetails(fileUri)[0]);
            pdfSize_TV.setText(getFileDetails(fileUri)[1]);

            signin_CL.animate().alpha(1).setDuration(500);
        }
    }


    public void setFileName() {

        subjectName_ACTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i(TAG,"On text change");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.i(TAG,"After text change");

                pdfName_TV.setText(String.format("%s_%s.pdf", generateAbbreviation(subjectName_ACTV.getText().toString()),
                        generateAbbreviation(userInstitute)));
            }
        });
    }


    public void setFileName(final Button btn) {
        pdfName_TV.setText(String.format("U%s_%s_%s.pdf", btn.getText().toString(),
                generateAbbreviation(subjectName_ACTV.getText().toString()), generateAbbreviation(userInstitute)));
    }


    public String[] getFileDetails(Uri uri) {
        String[] result = new String[]{null, null};
        if (Objects.equals(uri.getScheme(), "content")) {
            try (Cursor cursor = Objects.requireNonNull(getActivity()).getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result[0] = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    double fileSize = Double.parseDouble(cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE)));
                    result[1] = String.valueOf(fileSize / (1024 * 1024)).substring(0, 5) + " MB";
                }
            }
        }
        if (result[0] == null) {
            result[0] = uri.getPath();
            if (result[0] != null && result[0].contains("/")) {
                int cut = result[0].lastIndexOf('/');
                if (cut != -1) {
                    result[0] = result[0].substring(cut + 1);
                }
            }
        }
        return result;
    }


    public String generateAbbreviation(String fullForm) {
        StringBuilder abbreviation = new StringBuilder();

        for (int i = 0; i < fullForm.length(); i++) {
            char temp = fullForm.charAt(i);
            abbreviation.append(Character.isUpperCase(temp) ? temp : "");
        }
        return abbreviation.toString();
    }
}
