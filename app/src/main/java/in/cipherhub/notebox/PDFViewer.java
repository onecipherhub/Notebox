package in.cipherhub.notebox;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.ParcelFileDescriptor;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import in.cipherhub.notebox.adapters.AdapterPDFViewerList;
import in.cipherhub.notebox.models.ItemPDFPage;
import in.cipherhub.notebox.utils.OnPinchListener;
import in.cipherhub.notebox.utils.PinchZoomItemTouchListener;

public class PDFViewer extends AppCompatActivity {


  private String TAG = "PDFViewerOXET";
  List<ItemPDFPage> bitmapList = new ArrayList<>();

  RecyclerView pdfViewer_RV;
  TextView pdfName_TV, pageCount_TV;

  LinearLayoutManager layoutManagerPDFViewer;

  // Used to detect pinch zoom gesture.
  private ScaleGestureDetector scaleGestureDetector = null;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pdfviewer);

    getSupportActionBar().hide();

    pdfViewer_RV = findViewById(R.id.pdfViewer_RV);
    pdfName_TV = findViewById(R.id.pdfName_TV);
    pageCount_TV = findViewById(R.id.pageCount_TV);

    try {
      // createBitmapList(fileName) will inflate the list with PDFs once the list is created
      createBitmapList(getIntent().getStringExtra("file_name"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  public void createBitmapList(String fileName) throws IOException {
    File file = new File(fileName);

    if (!file.exists()) {
      InputStream asset;
      asset = getAssets().open(fileName);
      FileOutputStream output = null;
      try {
        output = new FileOutputStream(file);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      final byte[] buffer = new byte[5120];
      int size = 0;
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

        Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(),
                Bitmap.Config.ARGB_8888);

        page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        bitmapList.add(new ItemPDFPage("", bitmap));

        page.close();
      }
    }

    inflatePDFPages();
  }


  @SuppressLint("ClickableViewAccessibility")
  public void inflatePDFPages() {

    AdapterPDFViewerList adapterPDFViewerList = new AdapterPDFViewerList(bitmapList);
    pdfViewer_RV.setAdapter(adapterPDFViewerList);
    layoutManagerPDFViewer = new LinearLayoutManager(this);
    pdfViewer_RV.setLayoutManager(layoutManagerPDFViewer);

    pdfViewer_RV.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        // Dispatch activity on touch event to the scale gesture detector.
        scaleGestureDetector.onTouchEvent(motionEvent);
        return false;
      }
    });

    if (scaleGestureDetector == null) {
      // Create an instance of OnPinchListener custom class.
      OnPinchListener onPinchListener = new OnPinchListener(this, pdfViewer_RV);

      // Create the new scale gesture detector object use above pinch zoom gesture listener.
      scaleGestureDetector = new ScaleGestureDetector(getApplicationContext(), onPinchListener);
    }
    createPDFInterface();
  }


  public void createPDFInterface(){
    String pageCount = "1 / " + layoutManagerPDFViewer.getItemCount();

    pdfName_TV.setText(getIntent().getStringExtra("pdf_name"));
    pageCount_TV.setText(pageCount);

    pdfViewer_RV.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        String pageCount = (layoutManagerPDFViewer.findFirstVisibleItemPosition() + 1)
                + " / " + (layoutManagerPDFViewer.getItemCount());

        pageCount_TV.setText(pageCount);
      }
    });
  }
}
