package in.cipherhub.notebox;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.cipherhub.notebox.adapters.AdapterPDFList;
import in.cipherhub.notebox.models.ItemPDFList;

public class BookmarkActivity extends AppCompatActivity {

  private String TAG = "BookmarkActivityOXET";

  RecyclerView bookmarkSubjects_RV;
  EditText bookmarkSearch_ET;
  TextView noBookmarkSaved_TV;
  ImageButton searchIconInSearchBar_IB, closeBookmark_IB;

  SharedPreferences localDownloadDB, localDownloadDBBoolean;
  SharedPreferences.Editor localDownloadDBEditor, localDownloadDBBooleanEditor;
  SharedPreferences localBookmarkDB, localBookmarkDBBoolean;
  SharedPreferences.Editor localBookmarkDBEditor, localBookmarkDBBooleanEditor;
  List<ItemPDFList> itemPDFLists = new ArrayList<>();
  ItemPDFList openedPDFItem;
  AdapterPDFList adapterPDFList;

  FirebaseStorage storage = FirebaseStorage.getInstance();
  StorageReference httpsReference;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_bookmark);

    getSupportActionBar().hide();

    localDownloadDB = getSharedPreferences("localDownloadDB", MODE_PRIVATE);
    localDownloadDBBoolean = getSharedPreferences("localDownloadDBBoolean", MODE_PRIVATE);
    localDownloadDBEditor = localDownloadDB.edit();
    localDownloadDBBooleanEditor = localDownloadDBBoolean.edit();
    localBookmarkDB = getSharedPreferences("localBookmarkDB", MODE_PRIVATE);
    localBookmarkDBBoolean = getSharedPreferences("localBookmarkDBBoolean", MODE_PRIVATE);
    localBookmarkDBEditor = localBookmarkDB.edit();
    localBookmarkDBBooleanEditor = localBookmarkDBBoolean.edit();

    bookmarkSubjects_RV = findViewById(R.id.bookmarkSubjects_RV);
    bookmarkSearch_ET = findViewById(R.id.bookmarkSearch_ET);
    noBookmarkSaved_TV = findViewById(R.id.noBookmarkSaved_TV);
    searchIconInSearchBar_IB = findViewById(R.id.searchIconInSearchBar_IB);
    closeBookmark_IB = findViewById(R.id.closeBookmark_IB);
    closeBookmark_IB.animate().rotation(90).setDuration(500);
    closeBookmark_IB.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onBackPressed();
      }
    });

    bookmarkSearch_ET.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void afterTextChanged(Editable editable) {
        List<ItemPDFList> filteredList = new ArrayList<>();

        for (ItemPDFList s : itemPDFLists) {
          //new array list that will hold the filtered data
          //if the existing elements contains the search input
          if (s != null)
            if (s.getName().toLowerCase().contains(editable.toString().toLowerCase())
                    || s.getBy().toLowerCase().contains(editable.toString().toLowerCase())
                    || s.getAuthor().toLowerCase().contains(editable.toString().toLowerCase())) {
              filteredList.add(s);
            }
        }
        adapterPDFList.filterList(filteredList);
      }
    });

    adapterPDFList = new AdapterPDFList(itemPDFLists);
    adapterPDFList.setOnItemClickListener(new AdapterPDFList.OnItemClickListener() {
      @Override
      public void onItemClick(int position) {
        openedPDFItem = itemPDFLists.get(position);
        buildDialog();
      }
    });
    bookmarkSubjects_RV.setAdapter(adapterPDFList);
    bookmarkSubjects_RV.setLayoutManager(new LinearLayoutManager(BookmarkActivity.this));

    inflateBookmarksList();
  }


  private void inflateBookmarksList() {
    itemPDFLists.clear();

    // loop to get all the keys and values in shared preferences
    Map<String, ?> allEntries = localBookmarkDB.getAll();
    for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
      try {
        JSONObject pdf = new JSONObject(String.valueOf(entry.getValue()));
        itemPDFLists.add(new ItemPDFList(
                pdf.getString("name")
                , pdf.getString("by")
                , pdf.getString("author")
                , pdf.getString("date")
                , pdf.getInt("shares")
                , pdf.getInt("downloads")
                , pdf.getInt("likes")
                , pdf.getInt("dislikes")
        ));
      } catch (JSONException e) {
        Log.d(TAG, String.valueOf(e));
      }
    }
    if (itemPDFLists.isEmpty()) {
      bookmarkSearch_ET.setVisibility(View.GONE);
      searchIconInSearchBar_IB.setVisibility(View.GONE);
      noBookmarkSaved_TV.setVisibility(View.VISIBLE);
    }
    adapterPDFList.filterList(itemPDFLists);
  }


  private void buildDialog() {
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_pdf, null);
    final BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.DialogBottomAnimation);
    dialog.setContentView(dialogView);
    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogBottomAnimation;
    dialog.show();

    TextView pdfName_TV = dialogView.findViewById(R.id.pdfName_TV);
    TextView byValue_TV = dialogView.findViewById(R.id.byValue_TV);
    TextView authorValue_TV = dialogView.findViewById(R.id.authorValue_TV);
    TextView sharesCount_TV = dialogView.findViewById(R.id.sharesCount_TV);
    TextView downloadsCount_TV = dialogView.findViewById(R.id.downloadsCount_TV);
    TextView date_TV = dialogView.findViewById(R.id.date_TV);
    final TextView rating_TV = dialogView.findViewById(R.id.rating_TV);
    TextView reportAnIssue_TV = dialogView.findViewById(R.id.reportAnIssue_TV);
    reportAnIssue_TV.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");

        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"onecipherhub@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Report your issues.");
        i.putExtra(Intent.EXTRA_TEXT, "We will contact you soon. Please write in details.");

        try {
          startActivity(Intent.createChooser(i, "Report your issues."));
        } catch (android.content.ActivityNotFoundException ex) {
          Toast.makeText(BookmarkActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
      }
    });
    final Button download_B = dialogView.findViewById(R.id.download_B);

    Button sharePDF_B = dialogView.findViewById(R.id.sharePDF_B);
    final Button bookmark_B = dialogView.findViewById(R.id.bookmark_B);
    final Button like_IB = dialogView.findViewById(R.id.like_IB);
    final Button dislike_IB = dialogView.findViewById(R.id.dislike_IB);
    ImageButton closeTemplateArrow_IB = dialogView.findViewById(R.id.closeTemplateArrow_IB);

    closeTemplateArrow_IB.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        dialog.dismiss();
      }
    });

    like_IB.setVisibility(View.GONE);
    dislike_IB.setVisibility(View.GONE);

    pdfName_TV.setText(openedPDFItem.getName());
    byValue_TV.setText(openedPDFItem.getBy());
    authorValue_TV.setText(openedPDFItem.getAuthor());
    date_TV.setText(openedPDFItem.getDate());
    sharesCount_TV.setText(String.valueOf(openedPDFItem.getTotalShares()));
    downloadsCount_TV.setText(String.valueOf(openedPDFItem.getTotalDownloads()));
    rating_TV.setText(String.valueOf(openedPDFItem.getLikes() - openedPDFItem.getDislikes()));

    if (localBookmarkDBBoolean.getBoolean(openedPDFItem.getName(), false)) {
      bookmark_B.setCompoundDrawablesRelativeWithIntrinsicBounds(getDrawable(R.drawable.icon_bookmark_red_fill)
              , null, null, null);
    }
    if (localDownloadDBBoolean.getBoolean(openedPDFItem.getName(), false)) {
      download_B.setCompoundDrawablesRelativeWithIntrinsicBounds(getDrawable(R.drawable.ic_offline_pin_black_24dp)
              , null, null, null);
      download_B.setText(getResources().getString(R.string.open_pdf));
    }

    sharePDF_B.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Toast.makeText(BookmarkActivity.this, "Feature coming soon...", Toast.LENGTH_SHORT).show();
      }
    });

    bookmark_B.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (localBookmarkDBBoolean.getBoolean(openedPDFItem.getName(), false)) {
          // if already bookmarked
          bookmark_B.setCompoundDrawablesRelativeWithIntrinsicBounds(getDrawable(R.drawable.icon_bookmark_gray_border)
                  , null, null, null);

          localBookmarkDBEditor.remove(openedPDFItem.getName()).apply();
          localBookmarkDBBooleanEditor.remove(openedPDFItem.getName()).apply();
          Toast.makeText(BookmarkActivity.this, openedPDFItem.getName() + " removed from bookmarks!", Toast.LENGTH_SHORT).show();
        } else {
          // if not bookmarked
          bookmark_B.setCompoundDrawablesRelativeWithIntrinsicBounds(getDrawable(R.drawable.icon_bookmark_red_fill)
                  , null, null, null);
          Map<String, Object> pdfDetails = new HashMap<>();
          pdfDetails.put("name", "\"" + openedPDFItem.getName() + "\"");
          pdfDetails.put("by", "\"" + openedPDFItem.getBy() + "\"");
          pdfDetails.put("author", "\"" + openedPDFItem.getAuthor() + "\"");
          pdfDetails.put("date", "\"" + openedPDFItem.getDate() + "\"");
          pdfDetails.put("shares", "\"" + openedPDFItem.getTotalShares() + "\"");
          pdfDetails.put("downloads", "\"" + openedPDFItem.getTotalDownloads() + "\"");
          pdfDetails.put("likes", "\"" + openedPDFItem.getLikes() + "\"");
          pdfDetails.put("dislikes", "\"" + openedPDFItem.getDislikes() + "\"");
          pdfDetails.put("url", "\"" + openedPDFItem.getUrl() + "\"");
          localBookmarkDBEditor.putString(openedPDFItem.getName(), String.valueOf(pdfDetails)).apply();
          localBookmarkDBBooleanEditor.putBoolean(openedPDFItem.getName(), true).apply();
          Toast.makeText(BookmarkActivity.this, openedPDFItem.getName() + " added to your bookmarks!", Toast.LENGTH_SHORT).show();
        }
      }
    });

    download_B.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (localDownloadDBBoolean.getBoolean(openedPDFItem.getName(), false)) {
          // PDF is downloaded and can be opened in viewer

          String localFileName = "";
          try {
            localFileName = new JSONObject(localDownloadDB.getString(openedPDFItem.getName(), "")).getString("localFileName");
          } catch (JSONException e) {
            e.printStackTrace();
          }

          Intent intent = new Intent(BookmarkActivity.this, PDFViewer.class);
          intent.putExtra("file_name", localFileName);
          intent.putExtra("pdf_name", openedPDFItem.getName());
          startActivity(intent);
        } else {
          /*=============================== DOWNLOADING AND VIEWING PDF CODE ====================================*/
          final ProgressDialog progressDialog;
          progressDialog = new ProgressDialog(BookmarkActivity.this);
          progressDialog.setTitle("Downloading File");
          progressDialog.setCancelable(false);
          progressDialog.show();

          httpsReference = storage.getReferenceFromUrl(openedPDFItem.getUrl());
          try {
            // saved to cache directory
            final File localFile = File.createTempFile(openedPDFItem.getName(), ".pdf", getCacheDir());

            Log.i(TAG, String.valueOf(localFile));
            Log.i(TAG, String.valueOf(getCacheDir()));

            httpsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
              @Override
              public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                // Local temp file has been created
                Toast.makeText(BookmarkActivity.this, openedPDFItem.getName() + " download complete!"
                        , Toast.LENGTH_SHORT).show();

                progressDialog.dismiss();

                download_B.setCompoundDrawablesRelativeWithIntrinsicBounds(getDrawable(R.drawable.ic_offline_pin_black_24dp)
                        , null, null, null);
                download_B.setText(getResources().getString(R.string.open_pdf));
                Map<String, Object> pdfDetails = new HashMap<>();
                pdfDetails.put("name", "\"" + openedPDFItem.getName() + "\"");
                pdfDetails.put("by", "\"" + openedPDFItem.getBy() + "\"");
                pdfDetails.put("author", "\"" + openedPDFItem.getAuthor() + "\"");
                pdfDetails.put("date", "\"" + openedPDFItem.getDate() + "\"");
                pdfDetails.put("shares", "\"" + openedPDFItem.getTotalShares() + "\"");
                pdfDetails.put("downloads", "\"" + openedPDFItem.getTotalDownloads() + "\"");
                pdfDetails.put("likes", "\"" + openedPDFItem.getLikes() + "\"");
                pdfDetails.put("dislikes", "\"" + openedPDFItem.getDislikes() + "\"");
                pdfDetails.put("localFileName", "\"" + localFile + "\"");
                pdfDetails.put("url", "\"" + openedPDFItem.getUrl() + "\"");
                localDownloadDBEditor.putString(openedPDFItem.getName(), String.valueOf(pdfDetails)).apply();
                localDownloadDBBooleanEditor.putBoolean(openedPDFItem.getName(), true).apply();

              }
            }).addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception exception) {
                progressDialog.dismiss();
                Toast.makeText(BookmarkActivity.this, "Failed to download!", Toast.LENGTH_SHORT).show();
              }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
              @Override
              public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // percentage in progress dialog
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressDialog.setMessage("File: " + openedPDFItem.getName() + "\n" + "Downloaded " + ((int) progress) + "%");
              }
            });
          } catch (IOException e) {
            Log.e(TAG, String.valueOf(e));
          }

          /*============= END OF -> DOWNLOADING AND VIEWING PDF CODE ==================*/
        }
      }
    });


  }
}
