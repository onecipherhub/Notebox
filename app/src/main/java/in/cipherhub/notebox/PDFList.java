package in.cipherhub.notebox;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import in.cipherhub.notebox.adapters.AdapterPDFList;
import in.cipherhub.notebox.models.ItemPDFList;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class PDFList extends AppCompatActivity implements View.OnClickListener {
	boolean isAlreadyLiked = false, isAlreadyDisliked = false;
	private String TAG = "PDFListOXET", userLikedPDFs = "", userDislikedPDFs = "";
	int likedCount = 0, dislikedCount = 0, totalRating = 0;

	Button unitOne_B, unitTwo_B, unitThree_B, unitFour_B, unitFive_B;
	Button[] allButtons;
	List<ItemPDFList> pdfList = new ArrayList<>();
	List<ItemPDFList> filteredPDFList = new ArrayList<>();
	ItemPDFList openedPDFItem;
	Map<String, Object> pdfDetails;

	private static final int STORAGE_PERM = 123;
	public boolean checkPermission = false;
	String[] perms;

	FirebaseFirestore db;
	FirebaseAuth firebaseAuth;
	FirebaseUser user;
	FirebaseStorage storage = FirebaseStorage.getInstance();
	StorageReference httpsReference;

	SharedPreferences localDB, localBookmarkDB, localBookmarkDBBoolean, localLikeDBBoolean, localDislikeDBBoolean, localDownloadDBBoolean, localDownloadDB;
	SharedPreferences.Editor localBookmarkDBEditor, localBookmarkDBBooleanEditor, localLikeDBBooleanEditor, localDislikeDBBooleanEditor, localDownloadDBBooleanEditor, localDownloadDBEditor;
	JSONObject userObject, subject, pdf;

	RecyclerView PDFList_RV;
	AdapterPDFList adapterPDFList;
	DocumentReference pdfDocRef, userDocRef;
	Bundle extras;

	Bitmap pdf_first_page = null;

	String localFileName = "";

	ImageButton pdfPreview_IB;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pdflist);

		getSupportActionBar().hide();

		askPermission();

		db = FirebaseFirestore.getInstance();
		firebaseAuth = FirebaseAuth.getInstance();
		user = firebaseAuth.getCurrentUser();
		localDB = getSharedPreferences("localDB", MODE_PRIVATE);
		localBookmarkDB = getSharedPreferences("localBookmarkDB", MODE_PRIVATE);
		localBookmarkDBEditor = localBookmarkDB.edit();
		localBookmarkDBBoolean = getSharedPreferences("localBookmarkDBBoolean", MODE_PRIVATE);
		localBookmarkDBBooleanEditor = localBookmarkDBBoolean.edit();
		localLikeDBBoolean = getSharedPreferences("localLikeDBBoolean", MODE_PRIVATE);
		localLikeDBBooleanEditor = localLikeDBBoolean.edit();
		localDislikeDBBoolean = getSharedPreferences("localDislikeDBBoolean", MODE_PRIVATE);
		localDislikeDBBooleanEditor = localDislikeDBBoolean.edit();
		localDownloadDB = getSharedPreferences("localDownloadDB", MODE_PRIVATE);
		localDownloadDBEditor = localDownloadDB.edit();
		localDownloadDBBoolean = getSharedPreferences("localDownloadDBBoolean", MODE_PRIVATE);
		localDownloadDBBooleanEditor = localDownloadDBBoolean.edit();

		extras = getIntent().getExtras();

		TextView subjectName_TV = findViewById(R.id.subjectName_TV);
		final TextView subjectAbbreviation_TV = findViewById(R.id.subjectAbbreviation_TV);
		unitOne_B = findViewById(R.id.unitOne_B);
		unitTwo_B = findViewById(R.id.unitTwo_B);
		unitThree_B = findViewById(R.id.unitThree_B);
		unitFour_B = findViewById(R.id.unitFour_B);
		unitFive_B = findViewById(R.id.unitFive_B);
		PDFList_RV = findViewById(R.id.PDFList_RV);

		EditText pdfSearch_ET = findViewById(R.id.pdfSearch_ET);

		pdfSearch_ET.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				List<ItemPDFList> filteredList = new ArrayList<>();

				for (ItemPDFList s : pdfList) {
					//new array list that will hold the filtered data
					//if the existing elements contains the search input
					if (s != null)
						if (s.getName().toLowerCase().contains(editable.toString().toLowerCase())
								|| s.getBy().toLowerCase().contains(editable.toString().toLowerCase())
								|| s.getAuthor().toLowerCase().contains(editable.toString().toLowerCase())) {
							filteredList.add(s);
						}
				}
				filteredPDFList = filteredList;
				adapterPDFList.filterList(filteredList);
			}
		});

		allButtons = new Button[]{unitOne_B, unitTwo_B, unitThree_B, unitFour_B, unitFive_B};

		for (Button button : allButtons) {
			button.setOnClickListener(this);
		}

		subjectName_TV.setText(extras.getString("subjectName"));
		subjectAbbreviation_TV.setText(extras.getString("subjectAbbreviation"));

		adapterPDFList = new AdapterPDFList(pdfList);

		adapterPDFList.setOnItemClickListener(new AdapterPDFList.OnItemClickListener() {
			@Override
			public void onItemClick(int position) {
				if (!filteredPDFList.isEmpty()) {
					openedPDFItem = filteredPDFList.get(position);
				} else if (!pdfList.isEmpty()) {
					openedPDFItem = pdfList.get(position);
				}
				buildDialog();
			}
		});

		PDFList_RV.setAdapter(adapterPDFList);
		PDFList_RV.setLayoutManager(new LinearLayoutManager(PDFList.this));

		// set PDF document reference for whole page
		try {
			userObject = new JSONObject(localDB.getString("user", "Error Fetching..."));

			final String subject_doc_name = extras.getString("subjectAbbreviation").toLowerCase()
					+ "_" + generateAbbreviation(userObject.getString("branch")).toLowerCase()
					+ "_" + generateAbbreviation(userObject.getString("course")).toLowerCase();

			pdfDocRef = db.collection("institutes")
					.document(userObject.getString("institute"))
					.collection("subject_notes")
					.document(subject_doc_name);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// it will update all the PDFs if any one of them changes
		// then filters complete list
		pdfDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
			@Override
			public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
				if (documentSnapshot != null && documentSnapshot.getData() != null) {
					pdfList = new ArrayList<>();

					subject = new JSONObject(documentSnapshot.getData());

					Iterator<String> pdfs = subject.keys();
					while (pdfs.hasNext()) {
						String pdfName = pdfs.next();
						try {
							pdf = subject.getJSONObject(pdfName);
							totalRating = pdf.getInt("likes") - pdf.getInt("dislikes");
							pdfList.add(new ItemPDFList(
									pdfName
									, pdf.getString("by")
									, pdf.getString("author")
									, pdf.getString("date")
									, pdf.getInt("shares")
									, pdf.getInt("downloads")
									, pdf.getInt("likes")
									, pdf.getInt("dislikes")
							));
						} catch (JSONException e1) {
							Log.d(TAG, "error: " + e1);
						}
					}
					adapterPDFList.filterList(pdfList);
				}
			}
		});
	}


	@Override
	public void onClick(View view) {
		Button buttonClicked = findViewById(view.getId());

		for (Button button : allButtons) {
			button.setBackground(getResources().getDrawable(R.drawable.bg_br_radius_gray_smaller));
			button.setTextColor(getResources().getColor(R.color.colorGray_777777));
		}

		buttonClicked.setBackground(getResources().getDrawable(R.drawable.bg_apptheme_pill_5));
		buttonClicked.setTextColor(getResources().getColor(R.color.colorWhite_FFFFFF));

		pdfList.clear();

		if (subject != null) {

			Iterator<String> pdfs = subject.keys();
			while (pdfs.hasNext()) {
				String pdfName = pdfs.next();
				if (pdfName.startsWith("U" + buttonClicked.getText().toString())) {
					try {
						JSONObject pdf = subject.getJSONObject(pdfName);
						totalRating = pdf.getInt("likes") - pdf.getInt("dislikes");
						pdfList.add(new ItemPDFList(
								pdfName
								, pdf.getString("by")
								, pdf.getString("author")
								, pdf.getString("date")
								, pdf.getInt("shares")
								, pdf.getInt("downloads")
								, pdf.getInt("likes")
								, pdf.getInt("dislikes")
						));
						Log.d(TAG, "pdfList: " + pdfList);
					} catch (JSONException e1) {
						Log.d(TAG, "error: " + e1);
					}
				}
			}
			adapterPDFList.filterList(pdfList);
		}
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
		final Button download_B = dialogView.findViewById(R.id.download_B);

		pdfPreview_IB = dialogView.findViewById(R.id.pdfPreview_IB);
		pdfPreview_IB.setImageDrawable(getResources().getDrawable(R.drawable.ic_gray_report_an_issue));

		LoadImage image = new LoadImage();
		image.execute();


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
		reportAnIssue_TV.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");

				i.putExtra(Intent.EXTRA_EMAIL, new String[]{"onecipherhub@gmail.com"});
				i.putExtra(Intent.EXTRA_SUBJECT, "Report your issues.");
				i.putExtra(Intent.EXTRA_TEXT, "We will contact you soon. Please write in details.");
				//startActivity(Intent.createChooser(i, "Choose an Email client :"));

				try {
					startActivity(Intent.createChooser(i, "Report your issues."));
				} catch (android.content.ActivityNotFoundException ex) {
					Toast.makeText(PDFList.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
				}
			}
		});

		userLikedPDFs = "";
		userDislikedPDFs = "";

		userDocRef = db.collection("users").document(user.getUid());

		userDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
			@Override
			public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
				if (documentSnapshot != null) {
					try {
						userLikedPDFs = String.valueOf(new JSONObject(documentSnapshot.getData()).getJSONArray("liked"));
						userDislikedPDFs = String.valueOf(new JSONObject(documentSnapshot.getData()).getJSONArray("disliked"));

						isAlreadyLiked = userLikedPDFs.contains(openedPDFItem.getName());
						isAlreadyDisliked = userDislikedPDFs.contains(openedPDFItem.getName());

						if (isAlreadyLiked) {
							like_IB.setCompoundDrawablesRelativeWithIntrinsicBounds(null
									, getDrawable(R.drawable.ic_thumb_up_black_24dp), null, null);
						} else {
							like_IB.setCompoundDrawablesRelativeWithIntrinsicBounds(null
									, getDrawable(R.drawable.ic_thumb_up_fill_aaaaaa), null, null);
						}
						like_IB.setText(String.valueOf(likedCount));

						if (isAlreadyDisliked) {
							dislike_IB.setCompoundDrawablesRelativeWithIntrinsicBounds(null
									, getDrawable(R.drawable.ic_thumb_down_black_24dp), null, null);
						} else {
							dislike_IB.setCompoundDrawablesRelativeWithIntrinsicBounds(null
									, getDrawable(R.drawable.ic_thumb_down_fill_aaaaaa), null, null);
						}
						dislike_IB.setText(String.valueOf(dislikedCount));
						totalRating = likedCount - dislikedCount;
						rating_TV.setText(String.valueOf(totalRating));

					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				} else {
					Log.e(TAG, "document snapshot is null!");
				}
			}
		});

		likedCount = openedPDFItem.getLikes();
		dislikedCount = openedPDFItem.getDislikes();

		pdfName_TV.setText(openedPDFItem.getName());
		byValue_TV.setText(openedPDFItem.getBy());
		authorValue_TV.setText(openedPDFItem.getAuthor());
		date_TV.setText(openedPDFItem.getDate());
		sharesCount_TV.setText(String.valueOf(openedPDFItem.getTotalShares()));
		downloadsCount_TV.setText(String.valueOf(openedPDFItem.getTotalDownloads()));
		rating_TV.setText(String.valueOf(totalRating));

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
				Toast.makeText(PDFList.this, "Feature coming soon...", Toast.LENGTH_SHORT).show();
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
					Toast.makeText(PDFList.this, openedPDFItem.getName() + " removed from bookmarks!", Toast.LENGTH_SHORT).show();
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
					try {
						pdfDetails.put("url", "\"" + subject.getJSONObject(openedPDFItem.getName()).getString("url") + "\"");
					} catch (JSONException e) {
						Log.e(TAG, String.valueOf(e));
					}
					localBookmarkDBEditor.putString(openedPDFItem.getName(), String.valueOf(pdfDetails)).apply();
					localBookmarkDBBooleanEditor.putBoolean(openedPDFItem.getName(), true).apply();
					Toast.makeText(PDFList.this, openedPDFItem.getName() + " added to your bookmarks!", Toast.LENGTH_SHORT).show();
				}
			}
		});

		download_B.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (localDownloadDBBoolean.getBoolean(openedPDFItem.getName(), false)) {
					// PDF is downloaded and can be opened in viewer

					try {
						localFileName = new JSONObject(localDownloadDB.getString(openedPDFItem.getName(), "")).getString("localFileName");
					} catch (JSONException e) {
						e.printStackTrace();
					}

					Intent intent = new Intent(PDFList.this, PDFViewer.class);
					intent.putExtra("file_name", localFileName);
					intent.putExtra("pdf_name", openedPDFItem.getName());
					startActivity(intent);
				} else {

					if (checkPermission) {

						/*=============================== DOWNLOADING AND VIEWING PDF CODE ====================================*/
						final ProgressDialog progressDialog;
						progressDialog = new ProgressDialog(PDFList.this);
						progressDialog.setTitle("Downloading File");
						progressDialog.setCancelable(false);
						progressDialog.show();

						try {
							httpsReference = storage.getReferenceFromUrl(subject.getJSONObject(openedPDFItem.getName()).getString("url"));
						} catch (JSONException e) {
							Log.e(TAG, String.valueOf(e));
						}


						/*NOT CACHE CODE*/

						final File directPath = new File(getFilesDir(), "PDF");

						Log.i(TAG, "" + directPath);

						if (!directPath.exists()) {
							boolean mkdir = directPath.mkdir();
							if (!mkdir) {
								Log.i(TAG, "Directory creation failed.");
							} else {
								Log.i(TAG, "Directory created!!");

							}
						}

						final File pdf_file_download = new File(getApplicationContext().getFilesDir() + "/PDF", openedPDFItem.getName() + ".pdf");

						Log.i(TAG, "" + pdf_file_download);

						/*=============*/

						// saved to cache directory
//							final File localFile = File.createTempFile(openedPDFItem.getName(), ".pdf", getCacheDir());
//							Log.i(TAG, String.valueOf(localFile));
//							Log.i(TAG, String.valueOf(getCacheDir()));


						httpsReference.getFile(pdf_file_download).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
							@Override
							public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

								// Local temp file has been created
								Toast.makeText(PDFList.this, openedPDFItem.getName() + " download complete!"
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
								pdfDetails.put("localFileName", "\"" + pdf_file_download + "\"");
								try {
									pdfDetails.put("url", "\"" + subject.getJSONObject(openedPDFItem.getName()).getString("url") + "\"");
								} catch (JSONException e) {
									Log.e(TAG, String.valueOf(e));
								}
								localDownloadDBEditor.putString(openedPDFItem.getName(), String.valueOf(pdfDetails)).apply();
								localDownloadDBBooleanEditor.putBoolean(openedPDFItem.getName(), true).apply();

							}
						}).addOnFailureListener(new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception exception) {
								progressDialog.dismiss();
								Toast.makeText(PDFList.this, "Failed to download!", Toast.LENGTH_SHORT).show();
							}
						}).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
							@Override
							public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
								// percentage in progress dialog
								double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
								progressDialog.setMessage("File: " + openedPDFItem.getName() + "\n" + "Downloaded " + ((int) progress) + "%");
							}
						});

						/*============= END OF -> DOWNLOADING AND VIEWING PDF CODE ==================*/
					} else {
						Toast.makeText(getApplicationContext(), "Permission Not Granted", Toast.LENGTH_SHORT).show();

					}
				}
			}
		});

		// pdfDetails is the details of this PDF to update the firebase
		pdfDetails = new HashMap<>();
		pdfDetails.put("author", openedPDFItem.getAuthor());
		pdfDetails.put("by", openedPDFItem.getBy());
		pdfDetails.put("date", openedPDFItem.getDate());
		pdfDetails.put("downloads", openedPDFItem.getTotalDownloads());
		pdfDetails.put("name", openedPDFItem.getName());
		pdfDetails.put("shares", openedPDFItem.getTotalShares());
		pdfDetails.put("likes", likedCount);
		pdfDetails.put("dislikes", dislikedCount);
		try {
			pdfDetails.put("url", subject.getJSONObject(openedPDFItem.getName()).getString("url"));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		like_IB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (isAlreadyLiked) {
					// remove like
					likedCount--;
					addPDFToUserPDFs("liked", FieldValue.arrayRemove(openedPDFItem.getName()));
				} else {
					// add like
					likedCount++;
					addPDFToUserPDFs("liked", FieldValue.arrayUnion(openedPDFItem.getName()));
					if (isAlreadyDisliked) {
						// remove dislike
						dislikedCount--;
						addPDFToUserPDFs("disliked", FieldValue.arrayRemove(openedPDFItem.getName()));
					}
				}
				updateLikesPDFDocRef();
			}
		});

		dislike_IB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (isAlreadyDisliked) {
					dislikedCount--;
					addPDFToUserPDFs("disliked", FieldValue.arrayRemove(openedPDFItem.getName()));
				} else {
					dislikedCount++;
					addPDFToUserPDFs("disliked", FieldValue.arrayUnion(openedPDFItem.getName()));
					if (isAlreadyLiked) {
						likedCount--;
						addPDFToUserPDFs("liked", FieldValue.arrayRemove(openedPDFItem.getName()));
					}
				}
				updateLikesPDFDocRef();
			}
		});
	}


	public void updateLikesPDFDocRef() {
		pdfDetails.put("likes", likedCount);
		pdfDetails.put("dislikes", dislikedCount);

		pdfDocRef.update(openedPDFItem.getName(), pdfDetails)
				.addOnSuccessListener(new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
						Log.d(TAG, "Like updated in notes section");
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Log.d(TAG, "Failed to update likes in notes section");
					}
				});
	}


	public void addPDFToUserPDFs(String arrayName, Object fieldValue) {

		// update the user liked array
		userDocRef.update(arrayName, fieldValue)
				.addOnSuccessListener(new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
						Log.d(TAG, "Liked & updated");
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Log.d(TAG, "Failed to Liked & updated");
					}
				});
	}


	public String generateAbbreviation(String fullForm) {
		StringBuilder abbreviation = new StringBuilder();

		for (int i = 0; i < fullForm.length(); i++) {
			char temp = fullForm.charAt(i);
			abbreviation.append(Character.isUpperCase(temp) ? temp : "");
		}
		return abbreviation.toString();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		// Forward results to EasyPermissions
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}

	@AfterPermissionGranted(STORAGE_PERM)
	public void askPermission() {

		perms = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
		if (EasyPermissions.hasPermissions(this, perms)) {
			Log.i(TAG, "Permission Granted");
			checkPermission = true;

		} else {
			// Do not have permissions, request them
			EasyPermissions.requestPermissions(this, "This app requires storage permission to function properly.",
					STORAGE_PERM, perms);
		}
	}

	public Bitmap getFirstPage(String filename) throws IOException {

		File file = new File(filename);
		Bitmap bp = null;

		if (!file.exists()) {
			InputStream asset;
			asset = getAssets().open(filename);
			FileOutputStream output = null;
			try {
				output = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			final byte[] buffer = new byte[5120];
			int size;
			while ((size = asset.read(buffer)) != -1) {
				if (output != null) {
					output.write(buffer, 0, size);
					asset.close();
					output.close();
				}
			}
		}

		ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);

		android.graphics.pdf.PdfRenderer pdfRenderer = null;
		if (parcelFileDescriptor != null) {
			pdfRenderer = new android.graphics.pdf.PdfRenderer(parcelFileDescriptor);
		}

		if (pdfRenderer != null) {
			for (int i = 0; i < pdfRenderer.getPageCount(); i++) {
				android.graphics.pdf.PdfRenderer.Page page = pdfRenderer.openPage(i);

				pdf_first_page = Bitmap.createBitmap(page.getWidth(), page.getHeight(),
						Bitmap.Config.ARGB_8888);

				page.render(pdf_first_page, null, null,
						android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

				if (i == 0) {
					Log.i(TAG, String.valueOf(pdf_first_page));
					bp = pdf_first_page;
					Log.i(TAG, "2: " + bp);
				}
				page.close();
			}
		}

		return bp;
	}

	@SuppressLint("StaticFieldLeak")
	private class LoadImage extends AsyncTask<String, String, String> {

		Bitmap bitmap;

		@Override
		protected String doInBackground(String... strings) {
			try {

				localFileName = new JSONObject(localDownloadDB.getString(openedPDFItem.getName(), ""))
						.getString("localFileName");

				bitmap = getFirstPage(localFileName);

			} catch (JSONException e) {
				e.printStackTrace();
				bitmap = null;
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}


		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);

			if (bitmap != null) {
				pdfPreview_IB.setImageBitmap(bitmap);
			}
		}

	}
}
