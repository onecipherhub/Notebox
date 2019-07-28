package in.cipherhub.notebox.models;

import android.graphics.Bitmap;

public class ItemPDFPage {
  private String title;
  private Bitmap imageUrl;

  public ItemPDFPage(String title, Bitmap imageUrl) {

    this.title = title;
    this.imageUrl = imageUrl;
  }

  public String getTitle() {
    return title;
  }

  public Bitmap getImage() {
    return imageUrl;
  }
}
