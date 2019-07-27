package in.cipherhub.notebox.adapters;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

import in.cipherhub.notebox.models.ItemPDFList;
import in.cipherhub.notebox.R;

public class AdapterPDFList extends RecyclerView.Adapter<AdapterPDFList.pdfListItemViewHolder>{

    private List<ItemPDFList> list;
    private OnItemClickListener mListener;

    public AdapterPDFList(List<ItemPDFList> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public pdfListItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        return new pdfListItemViewHolder(LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.item_pdf_list, viewGroup, false), mListener);
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }


    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }


    @Override
    public void onBindViewHolder(@NonNull pdfListItemViewHolder pdfListItemViewHolder, int i) {
        String pdfName = list.get(i).getName();
        pdfListItemViewHolder.pdfName_TV.setText(pdfName);
        pdfListItemViewHolder.byValue_TV.setText(list.get(i).getBy());
        pdfListItemViewHolder.date_TV.setText(list.get(i).getDate());
        pdfListItemViewHolder.rating_TV.setText(String.valueOf(list.get(i).getRating()));
        pdfListItemViewHolder.sharesCount_TV.setText(String.valueOf(list.get(i).getTotalShares()));
        pdfListItemViewHolder.downloadsCount_TV.setText(String.valueOf(list.get(i).getTotalDownloads()));
        pdfListItemViewHolder.authorValue_TV.setText(list.get(i).getAuthor());
    }


    public void filterList(List<ItemPDFList> filteredList) {
        this.list = filteredList;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return list.size();
    }


    class pdfListItemViewHolder extends RecyclerView.ViewHolder {

        TextView pdfName_TV, date_TV, rating_TV, sharesCount_TV, downloadsCount_TV, authorValue_TV, byValue_TV;
        ConstraintLayout pdfList_CL;

        pdfListItemViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
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
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
