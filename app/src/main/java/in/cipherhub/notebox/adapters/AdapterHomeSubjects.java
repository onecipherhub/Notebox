package in.cipherhub.notebox.adapters;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.List;

import in.cipherhub.notebox.PDFList;
import in.cipherhub.notebox.R;
import in.cipherhub.notebox.models.ItemDataHomeSubjects;

public class AdapterHomeSubjects extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private List<ItemDataHomeSubjects> list;
  private OnItemClickListener mListener;

  private int adPosition = 0;
  boolean isHomePage;

  public AdapterHomeSubjects(List<ItemDataHomeSubjects> list, boolean isHomePage) {
    this.list = list;
    this.isHomePage = isHomePage;
  }

  public interface OnItemClickListener {
    void onItemClick(int position);
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    this.mListener = listener;
  }

  class HomeSubjectsItemViewHolder extends RecyclerView.ViewHolder {
    TextView subAbb_TV, subName_TV, lastUpdate_TV;
    ConstraintLayout itemHomeSubjects_CL;

    HomeSubjectsItemViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
      super(itemView);

      subAbb_TV = itemView.findViewById(R.id.subAbb_TV);
      subName_TV = itemView.findViewById(R.id.subName_TV);
      lastUpdate_TV = itemView.findViewById(R.id.lastUpdate_TV);
      itemHomeSubjects_CL = itemView.findViewById(R.id.itemHomeSubjects_CL);


      itemHomeSubjects_CL.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          if (isHomePage) {
            Intent intent = new Intent(view.getContext(), PDFList.class);
            intent.putExtra("subjectName", subName_TV.getText());
            intent.putExtra("subjectAbbreviation", subAbb_TV.getText());
            view.getContext().startActivity(intent);
          } else {
            // upload page subject selector
            if (listener != null) {
              int position = getAdapterPosition();
              if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position);
              }
            }
          }
        }
      });
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


  @Override
  public int getItemCount() {
    return list.size();
  }


  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    if (i == 1) {
      // show ad
      View view = LayoutInflater
              .from(viewGroup.getContext())
              .inflate(R.layout.item_smart_ad, viewGroup, false);
      return new SmartAdViewHolder(view);
    } else {
      View view = LayoutInflater
              .from(viewGroup.getContext())
              .inflate(R.layout.item_home_subjects, viewGroup, false);
      return new HomeSubjectsItemViewHolder(view, mListener);
    }
  }


  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i) {

    if (i != adPosition) {
      HomeSubjectsItemViewHolder homeSubjectsItemViewHolder = (HomeSubjectsItemViewHolder) holder;

      homeSubjectsItemViewHolder.subAbb_TV.setText(list.get(i).subAbb);
      homeSubjectsItemViewHolder.subName_TV.setText(list.get(i).subName);

      String lastUpdateDate = list.get(i).lastUpdate;
      if (lastUpdateDate.isEmpty()) {
        homeSubjectsItemViewHolder.lastUpdate_TV.setVisibility(View.GONE);
      } else {
        homeSubjectsItemViewHolder.lastUpdate_TV.setVisibility(View.VISIBLE);
        String last_update = "Subject Type: " + lastUpdateDate;
        homeSubjectsItemViewHolder.lastUpdate_TV.setText(lastUpdateDate);
      }
    }
  }


  @Override
  public int getItemViewType(int position) {
    if (position == adPosition && isHomePage)
      return 1;
    return 0;
  }


  public void filterList(List<ItemDataHomeSubjects> filteredList) {
    if (list.size() > 2) {
      adPosition = 2;
    } else {
      adPosition = filteredList.size();
    }
    filteredList.add(adPosition, null);
    this.list = filteredList;

    notifyDataSetChanged();
  }

}

