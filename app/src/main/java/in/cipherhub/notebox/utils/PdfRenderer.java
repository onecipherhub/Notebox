package in.cipherhub.notebox.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import in.cipherhub.notebox.R;

public class PdfRenderer extends Fragment implements View.OnClickListener {

    // Key string for saving the state of current page index.
    private static final String STATE_CURRENT_PAGE_INDEX = "current_page_index";
    private static final String TAG = "PDFRenderer";

    // The filename of the PDF.
    private String FILENAME = "";

    // File descriptor of the PDF.
    private ParcelFileDescriptor mFileDescriptor;

    // Renderer for pdf
    private android.graphics.pdf.PdfRenderer mPdfRenderer;

    // CurrentPage
    private android.graphics.pdf.PdfRenderer.Page mCurrentPage;

    // ImageView to display PDF page as a Bitmap
    private ImageView mImageView;

    private Button mButtonPrevious;
    private Button mButtonNext;

    private int mPageIndex;


    public PdfRenderer() {
        // Required Empty Constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pdf_viewer, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mImageView = view.findViewById(R.id.image);
        mButtonPrevious = view.findViewById(R.id.previous);
        mButtonNext = view.findViewById(R.id.next);

        mButtonPrevious.setOnClickListener(this);
        mButtonNext.setOnClickListener(this);

        mPageIndex = 0;

        // If there is a savedInstanceState (screen orientations, etc.), we restore the page index.
        if (null != savedInstanceState) {
            mPageIndex = savedInstanceState.getInt(STATE_CURRENT_PAGE_INDEX, 0);
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        if (getArguments() != null) {
            FILENAME = getArguments().getString("file_name");
            Log.i(TAG, "file_name_is: " + FILENAME);
        }

        try {
            openRenderer(getActivity());
            showPage(mPageIndex);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onStop() {
        try {
            closeRenderer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStop();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mCurrentPage) {
            outState.putInt(STATE_CURRENT_PAGE_INDEX, mCurrentPage.getIndex());
        }
    }


    private void openRenderer(Context context) throws IOException {

        // In this sample, we read a PDF from the assets directory.


//        File file = new File(Environment.getExternalStorageDirectory() + "/Notebox", "test.pdf");
        File file = new File(FILENAME);

        if (!file.exists()) {
            // Since PdfRenderer cannot handle the compressed asset file directly, we copy it into
            // the cache directory.
            InputStream asset = context.getAssets().open(FILENAME);
            FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[5120];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();
        }

        mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);

        // This is the PdfRenderer we use to render the PDF.
        if (mFileDescriptor != null) {
            mPdfRenderer = new android.graphics.pdf.PdfRenderer(mFileDescriptor);
        }
    }


    private void closeRenderer() throws IOException {
        if (null != mCurrentPage) {
            mCurrentPage.close();
        }
        mPdfRenderer.close();
        mFileDescriptor.close();
    }


    // Shows the specified page of PDF to the screen.
    private void showPage(int index) {
        if (mPdfRenderer.getPageCount() <= index) {
            return;
        }

        // Make sure to close the current page before opening another one.
        if (null != mCurrentPage) {
            mCurrentPage.close();
        }

        // Use `openPage` to open a specific page in PDF.
        mCurrentPage = mPdfRenderer.openPage(index);

        // Important: the destination bitmap must be ARGB (not RGB).
        Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(),
                Bitmap.Config.ARGB_8888);

        // Here, we render the page onto the Bitmap.
        // To render a portion of the page, use the second and third parameter. Pass nulls to get
        // the default result.
        // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
        mCurrentPage.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        // We are ready to show the Bitmap to user.
        mImageView.setImageBitmap(bitmap);
        updateUi();
    }


    private void updateUi() {
        int index = mCurrentPage.getIndex();
        int pageCount = mPdfRenderer.getPageCount();
        mButtonPrevious.setEnabled(0 != index);
        mButtonNext.setEnabled(index + 1 < pageCount);
        Objects.requireNonNull(getActivity()).setTitle(getString(R.string.pdf_index, index + 1, pageCount));
    }


    // Returns the number of pages.
    public int getPageCount() {
        return mPdfRenderer.getPageCount();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.previous: {
                // Move to the previous page
                showPage(mCurrentPage.getIndex() - 1);
                break;
            }
            case R.id.next: {
                // Move to the next page
                showPage(mCurrentPage.getIndex() + 1);
                break;
            }
        }
    }
}