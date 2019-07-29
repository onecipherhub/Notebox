package in.cipherhub.notebox.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.transition.Transition;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import in.cipherhub.notebox.MainActivity;
import in.cipherhub.notebox.MainActivity2;
import in.cipherhub.notebox.R;
import in.cipherhub.notebox.utils.Internet;

import static android.content.Context.MODE_PRIVATE;


public class Upload extends Fragment implements View.OnClickListener {

  private int REQUEST_PDF_PATH = 1000;
  private String TAG = "UploadOXET";

  Button selectPDF_B, upload_button, unitOne_B, unitTwo_B, unitThree_B, unitFour_B, unitFive_B;
  Button[] allButtons;

  AutoCompleteTextView subjectName_ACTV;
  ConstraintLayout signin_CL;
  TextView pdfName_TV, pdfSize_TV, lrg_text_view;
  EditText teacherName_ET;

  private StorageReference mStorageRef;
  ProgressDialog progressDialog;

  Boolean isSubjectsNameValid = false;

  View rootView;
  Uri fileToUpload;

  String downloadUrl;
  List<String> subjectList, subjectAbbList;
  List<String> clickedSubjPDFNames = new ArrayList<>();

  String userFullName = "";
  String subjectName = "";
  String unit = "";
  String teacher = "";
  String fixedSuffix = "";
  String PDFName = "";

  SharedPreferences localDB;
  JSONObject userObject;
  DocumentReference pdfDocRef, instituteDocRef;
  FirebaseFirestore db;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    // Inflate the layout for this fragment
    rootView = inflater.inflate(R.layout.fragment_upload, container, false);

    // Firebase Storage
    mStorageRef = FirebaseStorage.getInstance().getReference();
    localDB = getActivity().getSharedPreferences("localDB", Context.MODE_PRIVATE);

    progressDialog = new ProgressDialog(getActivity());
    progressDialog.setTitle("Uploading File");
    progressDialog.setCancelable(false);

    // Instantiate Views
    selectPDF_B = rootView.findViewById(R.id.selectPDF_B);
    upload_button = rootView.findViewById(R.id.upload_button);

    teacherName_ET = rootView.findViewById(R.id.teacherName_ET);
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

    subjectList = new ArrayList<>();
    subjectAbbList = new ArrayList<>();
    for (Map.Entry<String, String> entry : getSubjectList().entrySet()) {

      subjectList.add("(" + entry.getValue() + ") " + entry.getKey());
      subjectAbbList.add(entry.getValue());
    }


    // Populate the Subjects
    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
            R.layout.subject_recycler_view, R.id.lrg_text_view, subjectList);
    subjectName_ACTV.setAdapter(adapter);

    subjectName_ACTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                              long arg3) {
        isSubjectsNameValid = true;
        subjectName = arg0.getItemAtPosition(arg2).toString();
        subjectName = subjectName.substring(1, subjectName.indexOf(")"));
        setPDFName();
        getClickedSubjPDFNames(subjectName);
      }
    });

    subjectName_ACTV.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
          subjectName_ACTV.showDropDown();
        }
      }
    });

    subjectName_ACTV.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void afterTextChanged(Editable editable) {
        isSubjectsNameValid = false;
        subjectName = "";
        setPDFName();
      }
    });

    teacherName_ET.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void afterTextChanged(Editable editable) {
        teacher = editable.toString().replace(" ", "_");
        setPDFName();
      }
    });

    // Initialize button activities
    selectPDF_B.setOnClickListener(this);
    upload_button.setOnClickListener(this);
    unitOne_B.setOnClickListener(this);
    unitTwo_B.setOnClickListener(this);
    unitThree_B.setOnClickListener(this);
    unitFour_B.setOnClickListener(this);
    unitFive_B.setOnClickListener(this);

    allButtons = new Button[]{unitOne_B, unitTwo_B, unitThree_B, unitFour_B, unitFive_B};

    try {
      userObject = new JSONObject(localDB.getString("user", "Error Fetching..."));

      userFullName = userObject.getString("full_name");
      fixedSuffix = generateAbbreviation(userObject.getString("branch"))
              + "_" + generateAbbreviation(userObject.getString("course"))
              + "_" + generateAbbreviation(userObject.getString("institute"));
    } catch (JSONException e) {
      e.printStackTrace();
    }

    unitOne_B.callOnClick();

//        setFileName();

    return rootView;
  }


  public void onClick(View v) {

    Button buttonClicked = rootView.findViewById(v.getId());

    if (buttonClicked == selectPDF_B) {

      if (((MainActivity2) Objects.requireNonNull(this.getActivity())).checkPermission) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), REQUEST_PDF_PATH);
      } else {
        Toast.makeText(getContext(), "Permission not Granted", Toast.LENGTH_SHORT).show();
        ((MainActivity) Objects.requireNonNull(this.getActivity())).askPermission();
      }

    } else if (buttonClicked == upload_button) {


      if (!isSubjectsNameValid) {
        Toast.makeText(getContext(), "Subject name must be selected from list(auto-fill)", Toast.LENGTH_LONG).show();
      } else {
        if (new Internet(getActivity()).isAvailable())
          uploadFile();
      }
    } else {
      for (Button button : allButtons) {
        button.setBackground(getResources().getDrawable(R.drawable.bg_br_radius_gray_smaller));
        button.setTextColor(getResources().getColor(R.color.colorGray_777777));
      }
      buttonClicked.setBackground(getResources().getDrawable(R.drawable.bg_apptheme_pill_5));
      buttonClicked.setTextColor(getResources().getColor(R.color.colorWhite_FFFFFF));
//            setFileName(buttonClicked);
      unit = "U" + buttonClicked.getText().toString();
      setPDFName();
    }
  }


  private void uploadFile() {
    progressDialog.show();

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    mStorageRef = mStorageRef.child(user.getUid() + "/" + pdfName_TV.getText().toString());

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
                    int sameNamePDFCount = 1;
                    PDFName = PDFName + "_" + sameNamePDFCount;
                    while (clickedSubjPDFNames.contains(PDFName)) {
                      sameNamePDFCount++;
                      PDFName = PDFName
                              .substring(0, PDFName.lastIndexOf("_"))
                              + "_" + sameNamePDFCount;
                    }

                    fixedSuffix = fixedSuffix.substring(0, fixedSuffix.lastIndexOf("_")) + "_" + sameNamePDFCount;

                    writeNewInfoToDB();
                    Toast.makeText(getContext(), "File " + PDFName + " uploaded.",
                            Toast.LENGTH_SHORT).show();

                    // Reset the URI and Reference to null so that we can initialize again when needed
//                                fileToUpload = null;
//                                mStorageRef = null;
                    progressDialog.dismiss();

                    subjectName_ACTV.setText("");
                    if (getFragmentManager() != null) {
                      getFragmentManager().beginTransaction().detach(Upload.this).attach(Upload.this).commit();
                    }

                  }
                }).addOnFailureListener(new OnFailureListener() {
                  @Override
                  public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Log.i(TAG, "Failed to download the URL.");
                    Toast.makeText(getContext(), "Failed to Upload " + PDFName + " file.",
                            Toast.LENGTH_SHORT).show();

                  }
                });
              }
            }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        Log.d(TAG, "Failed to Upload the File");
        progressDialog.dismiss();
        Toast.makeText(getContext(), "Failed to upload " + PDFName + " file.",
                Toast.LENGTH_SHORT).show();


      }
    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
      @Override
      public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

        // percentage in progress dialog
        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
        progressDialog.setMessage("File: " + PDFName + "\n" + "Uploaded " + ((int) progress) + "%");
      }
    });

  }


  private void writeNewInfoToDB() {
    Map<String, Object> nestedData = new HashMap<>();

    String authorName = teacherName_ET.getText().toString();
    if (authorName.isEmpty()) {
      authorName = "NA";
    }

    nestedData.put("author", authorName);
    nestedData.put("by", userFullName);
    nestedData.put("date", new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(new Date()));
    nestedData.put("downloads", 0);
    nestedData.put("likes", 0);
    nestedData.put("dislikes", 0);
    nestedData.put("shares", 0);
    nestedData.put("url", downloadUrl);

    Map<String, Object> pdf = new HashMap<>();
    pdf.put(PDFName, nestedData);

    pdfDocRef.set(pdf, SetOptions.merge())
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
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    DocumentReference userDocRef = db.collection("users").document(user.getUid());

    Map<String, Object> userUploads = new HashMap<>();
    userUploads.put("uploads", pdf);

    pdfDocRef.set(userUploads, SetOptions.merge())
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
      signin_CL.setVisibility(View.VISIBLE);
    }
  }


  private void setPDFName() {
    String pdfName;
    if (!teacher.isEmpty())
      pdfName = unit + "_" + subjectName + "_" + teacher + "_" + fixedSuffix;
    else
      pdfName = unit + "_" + subjectName + "_" + fixedSuffix;
    pdfName_TV.setText(pdfName);
    PDFName = pdfName;
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


  public Map<String, String> getSubjectList() {
    Map<String, String> subAndAbbList = new HashMap<>();
    // to load the subjects for subjects list
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

        subAndAbbList.put(subjectName
                , subjects.getJSONObject(subjectName).getString("abbreviation"));
      }
    } catch (JSONException e) {
      Log.d(TAG, e.getMessage());
    }

    return subAndAbbList;
  }


  private void getClickedSubjPDFNames(String subjectAbbreviation) {
    db = FirebaseFirestore.getInstance();

    try {
      userObject = new JSONObject(localDB.getString("user", "Error Fetching..."));

      final String subject_doc_name = subjectAbbreviation.toLowerCase()
              + "_" + generateAbbreviation(userObject.getString("branch")).toLowerCase()
              + "_" + generateAbbreviation(userObject.getString("course")).toLowerCase();

      Log.d(TAG, "subject_doc_name: " + subject_doc_name);

      pdfDocRef = db.collection("institutes")
              .document(userObject.getString("institute"))
              .collection("subject_notes")
              .document(subject_doc_name);

      Log.d(TAG, "pdfDocRef: " + pdfDocRef);

      pdfDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
          if (task.isSuccessful() && task.getResult() != null && task.getResult().getData() != null) {
            JSONObject subject = new JSONObject(task.getResult().getData());
            Log.d(TAG, "task.getResult(): " + task.getResult());
            Iterator<String> iter = subject.keys();
            while (iter.hasNext()) {
              clickedSubjPDFNames.add(iter.next());
            }
            Log.d(TAG, String.valueOf(clickedSubjPDFNames));
          } else {
            Log.d(TAG, "Error fetching PDF list: " + task.getException());
          }
        }
      });
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
}
