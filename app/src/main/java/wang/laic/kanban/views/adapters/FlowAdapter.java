package wang.laic.kanban.views.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import wang.laic.kanban.R;
import wang.laic.kanban.models.Flow;
import wang.laic.kanban.models.OpEnum;
import wang.laic.kanban.utils.KukuUtil;

/**
 * Created by duduba on 2017/4/10.
 */

public class FlowAdapter extends RecyclerView.Adapter<FlowAdapter.ViewHolder> {


    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        @BindView(R.id.tv_flow_date) TextView tvFlowDate;
        @BindView(R.id.tv_flow_type) TextView tvFlowType;
        @BindView(R.id.tv_flow_quantity) TextView tvFlowQuantity;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    List<Flow> items;
    private Context mContext;

    public FlowAdapter(Context context, List<Flow> myDataset) {
        mContext = context;
        items = myDataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flow, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Flow item = items.get(position);

        holder.tvFlowDate.setText(KukuUtil.getFormatDate(item.getOpDate()));
        holder.tvFlowType.setText(OpEnum.getName(item.getOpType()));
        holder.tvFlowQuantity.setText("" + item.getQuantity());

        if(position % 2 == 0) {
            holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
