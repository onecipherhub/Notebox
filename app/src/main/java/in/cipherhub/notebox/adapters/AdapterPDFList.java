package in.cipherhub.notebox.adapters;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.List;

import in.cipherhub.notebox.models.ItemPDFList;
import in.cipherhub.notebox.R;

public class AdapterPDFList extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private List<ItemPDFList> list;
  private OnItemClickListener mListener;

  private int adPosition;

  public AdapterPDFList(List<ItemPDFList> list) {
    if (list.size() > 3) {
      adPosition = 3;
    } else {
      adPosition = 0;
    }
    list.add(adPosition, null);
    this.list = list;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    if (i == 1) {
      View view = LayoutInflater
              .from(viewGroup.getContext())
              .inflate(R.layout.item_smart_ad, viewGroup, false);
      return new SmartAdViewHolder(view);
    } else {
      return new PdfListItemViewHolder(LayoutInflater
              .from(viewGroup.getContext())
              .inflate(R.layout.item_pdf_list, viewGroup, false), mListener);
    }
  }

  public class SmartAdViewHolder extends RecyclerView.ViewHolder {

    SmartAdViewHolder(@NonNull View itemView) {
      super(itemView);

      AdView adsView = itemView.findViewById(R.id.adView);

      AdRequest adRequest = new AdRequest.Builder().build();
//      AdSize adSize = new AdSize(400, 50);
//        adsView.setAdSize(adSize);
//        adsView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
      adsView.loadAd(adRequest);
    }
  }

  public interface OnItemClickListener {
    void onItemClick(int position);
  }


  public void setOnItemClickListener(OnItemClickListener listener) {
    this.mListener = listener;
  }


  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i) {

    if (i != adPosition) {
      PdfListItemViewHolder pdfListItemViewHolder = (PdfListItemViewHolder) holder;

      String pdfName = list.get(i).getName();
      pdfListItemViewHolder.pdfName_TV.setText(pdfName);
      pdfListItemViewHolder.byValue_TV.setText(list.get(i).getBy());
      pdfListItemViewHolder.date_TV.setText(list.get(i).getDate());
      pdfListItemViewHolder.rating_TV.setText(String.valueOf(list.get(i).getRating()));
      pdfListItemViewHolder.sharesCount_TV.setText(String.valueOf(list.get(i).getTotalShares()));
      pdfListItemViewHolder.downloadsCount_TV.setText(String.valueOf(list.get(i).getTotalDownloads()));
      pdfListItemViewHolder.authorValue_TV.setText(list.get(i).getAuthor());
    }
  }

  @Override
  public int getItemViewType(int position) {
    if (position == adPosition)
      return 1;
    return 0;
  }

  public void filterList(List<ItemPDFList> filteredList) {
    if (list.size() > 3) {
      adPosition = 3;
    } else {
      adPosition = 0;
    }
    filteredList.add(adPosition, null);
    this.list = filteredList;
    notifyDataSetChanged();
  }


  @Override
  public int getItemCount() {
    return list.size();
  }


  class PdfListItemViewHolder extends RecyclerView.ViewHolder {

    TextView pdfName_TV, date_TV, rating_TV, sharesCount_TV, downloadsCount_TV, authorValue_TV, byValue_TV;
    ConstraintLayout pdfList_CL;

    PdfListItemViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
      super(itemView);

      pdfList_CL = itemView.findViewById(R.id.pdfList_CL);
      pdfName_TV = itemView.findViewById(R.id.pdfName_TV);
      date_TV = itemView.findViewById(R.id.date_TV);
      rating_TV = itemView.findViewById(R.id.rating_TV);
      sharesCount_TV = itemView.findViewById(R.id.sharesCount_TV);
      downloadsCount_TV = itemView.findViewById(R.id.downloadsCount_TV);
      authorValue_TV = itemView.findViewById(R.id.authorValue_TV);
      byValue_TV = itemView.findViewById(R.id.byValue_TV);

      pdfList_CL.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          if (listener != null) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
              listener.onItemClick(position);
            }
          }
        }
      });
    }
  }
}
