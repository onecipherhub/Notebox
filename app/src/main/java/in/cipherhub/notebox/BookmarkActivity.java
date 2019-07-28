package in.cipherhub.notebox;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

  SharedPreferences localBookmarkDB, localBookmarkDBBoolean;
  SharedPreferences.Editor localBookmarkDBEditor, localBookmarkDBBooleanEditor;
  List<ItemPDFList> itemPDFLists = new ArrayList<>();
  ItemPDFList openedPDFItem;
  AdapterPDFList adapterPDFList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_bookmark);

    getSupportActionBar().hide();

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
    final Dialog dialog = new Dialog(this, R.style.DialogBottomAnimation);
    dialog.getWindow().setGravity(Gravity.BOTTOM);
    dialog.setContentView(dialogView);
    dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogBottomAnimation;
    dialog.show();

    TextView pdfName_TV = dialogView.findViewById(R.id.pdfName_TV);
    TextView byValue_TV = dialogView.findViewById(R.id.byValue_TV);
    TextView authorValue_TV = dialogView.findViewById(R.id.authorValue_TV);
    TextView sharesCount_TV = dialogView.findViewById(R.id.sharesCount_TV);
    TextView downloadsCount_TV = dialogView.findViewById(R.id.downloadsCount_TV);
    final TextView rating_TV = dialogView.findViewById(R.id.rating_TV);
    TextView date_TV = dialogView.findViewById(R.id.date_TV);
    Button sharePDF_B = dialogView.findViewById(R.id.sharePDF_B);
    final Button bookmark_B = dialogView.findViewById(R.id.bookmark_B);
    final Button like_IB = dialogView.findViewById(R.id.like_IB);
    final Button dislike_IB = dialogView.findViewById(R.id.dislike_IB);

    like_IB.setVisibility(View.GONE);
    dislike_IB.setVisibility(View.GONE);

    pdfName_TV.setText(openedPDFItem.getName());
    byValue_TV.setText(openedPDFItem.getBy());
    authorValue_TV.setText(openedPDFItem.getAuthor());
    date_TV.setText(openedPDFItem.getDate());
    sharesCount_TV.setText(String.valueOf(openedPDFItem.getTotalShares()));
    downloadsCount_TV.setText(String.valueOf(openedPDFItem.getTotalDownloads()));
    rating_TV.setText(String.valueOf((openedPDFItem.getLikes() - openedPDFItem.getDislikes())));
    if (localBookmarkDBBoolean.getBoolean(openedPDFItem.getName(), false)) {
      bookmark_B.setCompoundDrawablesRelativeWithIntrinsicBounds(getDrawable(R.drawable.icon_bookmark_red_fill)
              , null, null, null);
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
        // if already bookmarked
        bookmark_B.setCompoundDrawablesRelativeWithIntrinsicBounds(getDrawable(R.drawable.icon_bookmark_gray_border)
                , null, null, null);

        localBookmarkDBEditor.remove(openedPDFItem.getName()).apply();
        localBookmarkDBBooleanEditor.remove(openedPDFItem.getName()).apply();
        dialog.dismiss();
        inflateBookmarksList();
        Toast.makeText(BookmarkActivity.this, openedPDFItem.getName() + " removed from bookmarks!", Toast.LENGTH_SHORT).show();
      }
    });

  }
}
