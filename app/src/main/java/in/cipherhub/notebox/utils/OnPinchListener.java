package in.cipherhub.notebox.utils;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import static java.security.AccessController.getContext;

/**
 * Created by zhaosong on 2018/5/6.
 */


/* This listener is used to listen pinch zoom gesture. ß*/
public class OnPinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

  private final static String TAG_PINCH_LISTENER = "PINCH_LISTENER";

  // Pinch zoon occurred on this image view object.
  private RecyclerView srcImageView = null;

  // Related appication context.
  private Context context = null;

  // The default constructor pass context and imageView object.
  public OnPinchListener(Context context, RecyclerView srcImageView) {
    this.context = context;
    this.srcImageView = srcImageView;
  }

  // When pinch zoom gesture occurred.
  @Override
  public boolean onScale(ScaleGestureDetector detector) {

    if (detector != null) {

//      float scaleFactor = detector.getScaleFactor();
      float something = detector.getFocusX();
      Log.d("OXET", "detector.getFocusX() => " + something);

      if (srcImageView != null) {

        // Scale the image with pinch zoom value.
        scaleImage(detector);
      } else {

        if (context != null) {
          Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
        } else {
          Log.e(TAG_PINCH_LISTENER, "Both context and srcImageView is null.");
        }
      }
    } else {
      Log.e(TAG_PINCH_LISTENER, "Pinch listener onScale detector parameter is null.");
    }

    return true;
  }

  /* This method is used to scale the image when user pinch zoom it. */
  private void scaleImage(ScaleGestureDetector detector) {
    float scale = detector.getScaleFactor();

    int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
    //    int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

    int desiringWidth = (int) (srcImageView.getWidth() * scale);
    int desiringHeight = (int) (srcImageView.getHeight() * scale);

    if (desiringWidth > screenWidth) {
      ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(desiringWidth, desiringHeight);
      srcImageView.setLayoutParams(layoutParams);

      // Log.d("OXET xScale = yScale = ", "" + scale);
      // Log.d("OXET Width | Height", layoutParams.width + " | " + layoutParams.height);
      // Log.d("OXET screen", screenWidth + " | " + screenHeight);
    }
  }
}

/* useful methods
 * detector.getCurrentSpan() will give the distance between two finger will pinching
 * detector.getCurrentSpanX() will give the distance between two fingers in X direction while pinching
 * */
