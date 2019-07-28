package in.cipherhub.notebox.utils;


import android.content.Context;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

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

  // The default constructor pass context and imageview object.
  public OnPinchListener(Context context, RecyclerView srcImageView) {
    this.context = context;
    this.srcImageView = srcImageView;
  }

  // When pinch zoom gesture occurred.
  @Override
  public boolean onScale(ScaleGestureDetector detector) {

    if(detector!=null) {

      float scaleFactor = detector.getScaleFactor();

      if (srcImageView != null) {

        // Scale the image with pinch zoom value.
        scaleImage(scaleFactor, scaleFactor);

      } else {
        if (context != null) {
          Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
        } else {
          Log.e(TAG_PINCH_LISTENER, "Both context and srcImageView is null.");
        }
      }
    }else
    {
      Log.e(TAG_PINCH_LISTENER, "Pinch listener onScale detector parameter is null.");
    }

    return true;
  }

  /* This method is used to scale the image when user pinch zoom it. */
  private void scaleImage(float xScale, float yScale)
  {
    Log.d("OXET", xScale + " | " + yScale);
    ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
            (int) (srcImageView.getWidth() * xScale), (int) (srcImageView.getHeight() * yScale)
    );
    Log.d("OXET", layoutParams.width +" | " + layoutParams.height);
    srcImageView.setLayoutParams(layoutParams);
  }
}

