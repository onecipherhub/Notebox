package in.cipherhub.notebox.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import in.cipherhub.notebox.R;

public class AdapterRecentViews extends RecyclerView.Adapter<AdapterRecentViews.recentViewsItemViewHolder>{

    private List<AdapterRecentViews.recentViewsItemData> list;

    public AdapterRecentViews(List<AdapterRecentViews.recentViewsItemData> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public recentViewsItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        return new recentViewsItemViewHolder(LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.item_recent_views, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull recentViewsItemViewHolder recentViewsItemViewHolder, int i) {

        recentViewsItemViewHolder.subAbb_TV.setText(list.get(i).subAbb);
        recentViewsItemViewHolder.branchAbb_TV.setText(list.get(i).branchAbb);
        recentViewsItemViewHolder.time_TV.setText(list.get(i).time);
        recentViewsItemViewHolder.subName_TV.setText(list.get(i).subName);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class recentViewsItemViewHolder extends RecyclerView.ViewHolder {
        TextView subAbb_TV, branchAbb_TV, time_TV, subName_TV;
        recentViewsItemViewHolder(@NonNull View itemView) {
            super(itemView);

            subAbb_TV = itemView.findViewById(R.id.subAbb_TV);
            branchAbb_TV = itemView.findViewById(R.id.branchAbb_TV);
            time_TV = itemView.findViewById(R.id.time_TV);
            subName_TV = itemView.findViewById(R.id.subName_TV);
        }
    }

    public static class recentViewsItemData {

        private String subAbb, branchAbb, time, subName;

        public recentViewsItemData(String subAbb, String branchAbb, String time, String subName) {
            this.subAbb = subAbb;
            this.branchAbb = branchAbb;
            this.time = time;
            this.subName = subName;
        }
    }
}
