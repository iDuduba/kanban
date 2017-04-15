package wang.laic.kanban.views.adapters;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import wang.laic.kanban.Constants;
import wang.laic.kanban.OrderActivity;
import wang.laic.kanban.R;
import wang.laic.kanban.models.Order;
import wang.laic.kanban.models.OrderStatusEnum;
import wang.laic.kanban.utils.KukuUtils;

/**
 * Created by duduba on 2017/4/10.
 */

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {


    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView tvOrderNo;
        public TextView tvOrderTimes;
        public TextView tvStatus;
        public TextView tvDeliveryDate;

        public ViewHolder(View itemView) {
            super(itemView);
            tvOrderNo = (TextView) itemView.findViewById(R.id.orderNo);
            tvOrderTimes = (TextView) itemView.findViewById(R.id.orderTimes);
            tvStatus = (TextView) itemView.findViewById(R.id.orderStatus);
            tvDeliveryDate = (TextView) itemView.findViewById(R.id.deliveryDate);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, OrderActivity.class);
                    intent.putExtra(Constants.KEY_ORDER_NO, tvOrderNo.getText().toString());
                    intent.putExtra(Constants.KEY_ORDER_TIMES, Integer.parseInt(tvOrderTimes.getText().toString()));
                    mContext.startActivity(intent);
                }
            });

        }
    }

    List<Order> items;
    private Context mContext;

    public OrderAdapter(Context context, List<Order> myDataset) {
        mContext = context;
        items = myDataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Order order = items.get(position);

        holder.tvOrderNo.setText(order.getOrderCompId().getOrderNo());
        holder.tvOrderTimes.setText("" + order.getOrderCompId().getDeliveryNumber());
        holder.tvStatus.setText(OrderStatusEnum.getBackName(order.getStatus()));
        holder.tvDeliveryDate.setText(KukuUtils.getFormatDate(order.getDeliveryDate()));

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
