package in.cipherhub.notebox.fragments;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import in.cipherhub.notebox.R;

public class Explore extends Fragment {

    View rootView;

    private static final String TAG = "Explore";

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference httpsReference;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_explore, container, false);
//
//
//        /*=============================== DOWNLOADING AND VIEWING PDF CODE ====================================*/
//
//        httpsReference = storage.getReferenceFromUrl(
//                "https://firebasestorage.googleapis.com/v0/b/notebox-4f384.appspot.com/" +
//                        "o/D3INJYGQIHcJhX24SnrgJBXVaSH3%2FU1_CN2_CSE_BE_NMIT?alt=media&token=f808ef30-eb6b-40e9-ba1b-bc18a2ad5d11");
//
//        try {
//            // saved to cache directory
//            final File localFile = File.createTempFile("something", ".pdf", getActivity().getCacheDir());
//
//            Log.i(TAG, String.valueOf(localFile));
//            Log.i(TAG, String.valueOf(getActivity().getCacheDir()));
//
//<<<<<<< HEAD
//            httpsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//
//                    // Local temp file has been created
//                    Toast.makeText(getActivity(), "Downloaded!", Toast.LENGTH_SHORT).show();
//
//                    Intent intent = new Intent(getContext(), PDFViewer.class);
//                    intent.putExtra("file_name", String.valueOf(localFile));
//                    startActivity(intent);
//
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception exception) {
//                    Toast.makeText(getContext(), "Failed to download!", Toast.LENGTH_SHORT).show();
//                }
//            });
//=======
//            try {
//
//                httpsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//
//                        // Local temp file has been created
//                        try {
//                            Toast.makeText(getContext(), "Downloaded!", Toast.LENGTH_SHORT).show();
//                            Intent intent = new Intent(getContext(), PDFViewer.class);
//                            intent.putExtra("file_name", String.valueOf(localFile));
//                            startActivity(intent);
//                        }catch (NullPointerException e) {
//                            Log.i(TAG, "Sorry couldn't download it");
//                        }
//
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception exception) {
//                        Toast.makeText(getContext(), "Failed to download!", Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//
//            }catch (NullPointerException e) {
//                Toast.makeText(getContext(), "Sorry couldn't download it", Toast.LENGTH_SHORT).show();
//            }
//>>>>>>> 7efef57af7ad6e226cb38ceca333b9c572ccdb1d
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        /*============= END OF -> DOWNLOADING AND VIEWING PDF CODE ==================*/
        return rootView;
    }
}
