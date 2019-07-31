package in.cipherhub.notebox.models;

import android.graphics.Bitmap;

public class ItemPDFPage {
  private boolean title;
  private Bitmap imageUrl;

  public ItemPDFPage(boolean title, Bitmap imageUrl) {

    this.title = title;
    this.imageUrl = imageUrl;
  }

  public boolean getTitle() {
    return title;
  }

  public Bitmap getImage() {
    return imageUrl;
  }
}
