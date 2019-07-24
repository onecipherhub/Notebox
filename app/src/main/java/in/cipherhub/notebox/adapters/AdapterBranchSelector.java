package in.cipherhub.notebox.adapters;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import in.cipherhub.notebox.models.ItemDataBranchSelector;
import in.cipherhub.notebox.R;

public class AdapterBranchSelector extends RecyclerView.Adapter<AdapterBranchSelector.branchSelectorItemViewHolder>{

    private List<ItemDataBranchSelector> list;
    private OnItemClickListener mListener;

    public AdapterBranchSelector(List<ItemDataBranchSelector> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public branchSelectorItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        return new branchSelectorItemViewHolder(LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.item_branch_selector, viewGroup, false), mListener);
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }


    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }


    @Override
    public void onBindViewHolder(@NonNull branchSelectorItemViewHolder branchSelectorItemViewHolder, int i) {

        branchSelectorItemViewHolder.branchName_TV.setText(list.get(i).getBranchName());
        branchSelectorItemViewHolder.branchAbb_TV.setText(list.get(i).getBranchAbb());
        branchSelectorItemViewHolder.branchAbb_TV.setTextColor(Color.parseColor(list.get(i).getBranchAbbColor()));
        branchSelectorItemViewHolder.totalUploads_TV.setText(list.get(i).getTotalUploads());
    }


    public void filterList(List<ItemDataBranchSelector> filteredList) {
        this.list = filteredList;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return list.size();
    }


    class branchSelectorItemViewHolder extends RecyclerView.ViewHolder {

        TextView branchName_TV, branchAbb_TV, totalUploads_TV;
        ConstraintLayout itemBranchSelector_CL;

        branchSelectorItemViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            itemBranchSelector_CL = itemView.findViewById(R.id.itemBranchSelector_CL);
            branchName_TV = itemView.findViewById(R.id.branchName1_TV);
            branchAbb_TV = itemView.findViewById(R.id.branchAbb1_TV);
            totalUploads_TV = itemView.findViewById(R.id.totalUploads1_TV);

            itemBranchSelector_CL.setOnClickListener(new View.OnClickListener() {
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
