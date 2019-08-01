package in.cipherhub.notebox.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.cipherhub.notebox.R;
import in.cipherhub.notebox.models.ItemPDFPage;

public class AdapterPDFViewerList extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private List<ItemPDFPage> list;
  private AdapterBranchSelector.OnItemClickListener mListener;


  public AdapterPDFViewerList(List<ItemPDFPage> list) {
    this.list = list;
  }


  class HomeSubjectsItemViewHolder extends RecyclerView.ViewHolder {
    ImageView pdfPage_IV;
    CardView pdfPage_CV;

    HomeSubjectsItemViewHolder(@NonNull View itemView) {
      super(itemView);

      pdfPage_IV = itemView.findViewById(R.id.pdfPage_IV);
      pdfPage_CV = itemView.findViewById(R.id.pdfPage_CV);
    }
  }


  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    View view = LayoutInflater
            .from(viewGroup.getContext())
            .inflate(R.layout.item_pdf_page, viewGroup, false);
    return new HomeSubjectsItemViewHolder(view);
  }


  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i) {

    HomeSubjectsItemViewHolder homeSubjectsItemViewHolder = (HomeSubjectsItemViewHolder) holder;

      homeSubjectsItemViewHolder.pdfPage_IV.setImageBitmap(list.get(i).getImage());
  }


  @Override
  public int getItemCount() {
    return list.size();
  }
}

