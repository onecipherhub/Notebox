package in.cipherhub.notebox;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.ParcelFileDescriptor;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

public class PDFViewer extends AppCompatActivity {

  private String TAG = "PDFViewerOXET";
  List<ItemPDFPage> bitmapList = new ArrayList<>();

  RecyclerView pdfViewer_RV;
  TextView pdfName_TV, pageCount_TV;
  ConstraintLayout thisIsIt;
  LinearLayout ll;
  ImageButton zoomIn_IB, zoomOut_IB;

  LinearLayoutManager layoutManagerPDFViewer;

  // Used to detect pinch zoom gesture.
  private ScaleGestureDetector scaleGestureDetector = null;

  float getY, getRawY, getX, getRawX;
  ConstraintLayout.LayoutParams layoutParams;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pdfviewer);

    getSupportActionBar().hide();

    pdfViewer_RV = findViewById(R.id.pdfViewer_RV);
    pdfName_TV = findViewById(R.id.pdfName_TV);
    pageCount_TV = findViewById(R.id.pageCount_TV);
    thisIsIt = findViewById(R.id.thisIsIt);
    zoomOut_IB = findViewById(R.id.zoomOut_IB);
    ll = findViewById(R.id.ll);

    zoomOut_IB.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        layoutParams = new ConstraintLayout.LayoutParams(ll.getWidth(), ll.getHeight());
        pdfViewer_RV.setLayoutParams(layoutParams);
        zoomOut_IB.setVisibility(View.GONE);
        pdfViewer_RV.animate()
                .x(0)
                .setDuration(0)
                .start();
      }
    });

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
      PdfRenderer.Page page = null;
      for (int i = 0; i < pdfRenderer.getPageCount(); i++) {
        page = pdfRenderer.openPage(i);

        Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(),
                Bitmap.Config.ARGB_8888);

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        bitmapList.add(new ItemPDFPage(true, bitmap));

        page.close();
      }
      bitmapList.add(new ItemPDFPage(false, Bitmap.createBitmap(page.getWidth(), page.getHeight(),
              Bitmap.Config.ARGB_8888)));
    }

    inflatePDFPages();
  }


  public void inflatePDFPages() {
    final AdapterPDFViewerList adapterPDFViewerList = new AdapterPDFViewerList(bitmapList);
    pdfViewer_RV.setAdapter(adapterPDFViewerList);
    layoutManagerPDFViewer = new LinearLayoutManager(this);
    pdfViewer_RV.setLayoutManager(layoutManagerPDFViewer);

    setPDFListeners();
    createPDFInterface();
  }


  @SuppressLint("ClickableViewAccessibility")
  public void setPDFListeners() {

    pdfViewer_RV.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent event) {
        // Dispatch activity on touch event to the scale gesture detector.
        scaleGestureDetector.onTouchEvent(event);

        pdfViewer_RV.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
          @Override
          public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
            zoomOut_IB.setVisibility(View.VISIBLE);
          }
        });

        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            getX = view.getX();
            getRawX = event.getRawX();
            getY = view.getY();
            getRawY = event.getRawY();
            break;
          case MotionEvent.ACTION_MOVE:
            if (pdfViewer_RV.getWidth() - Math.abs(view.getX()) - Resources.getSystem().getDisplayMetrics().widthPixels > 0
                    || view.getX() < 0)
              view.animate()
                      .x(event.getRawX() + (getX - getRawX))
                      .setDuration(0)
                      .start();

            if (view.getX() > 0) {
              // RV has visible start margin
              view.animate()
                      .x(0)
                      .setDuration(100)
                      .start();
            } else if (pdfViewer_RV.getWidth() - Math.abs(view.getX()) - Resources.getSystem().getDisplayMetrics().widthPixels < 0) {
              view.animate()
                      .x(-(pdfViewer_RV.getWidth() - Resources.getSystem().getDisplayMetrics().widthPixels))
                      .setDuration(100)
                      .start();
            }
            break;
          default:
            return false;
        }
        return false;
      }
    });

    if (scaleGestureDetector == null) {
      // Create an instance of OnPinchListener custom class.
      OnPinchListener onPinchListener = new OnPinchListener(this, pdfViewer_RV);

      // Create the new scale gesture detector object use above pinch zoom gesture listener.
      scaleGestureDetector = new ScaleGestureDetector(getApplicationContext(), onPinchListener);
    }

    pdfViewer_RV.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        String pageCount = (layoutManagerPDFViewer.findFirstVisibleItemPosition() + 1)
                + " / " + (layoutManagerPDFViewer.getItemCount());

        pageCount_TV.setText(pageCount);
      }
    });
  }


  public void createPDFInterface() {
    String pageCount = "1 / " + (layoutManagerPDFViewer.getItemCount());

    pdfName_TV.setText(getIntent().getStringExtra("pdf_name"));
    pageCount_TV.setText(pageCount);
  }
}
