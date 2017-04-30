package wang.laic.kanban.views.adapters;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import wang.laic.kanban.Constants;
import wang.laic.kanban.OrderActivity;
import wang.laic.kanban.R;
import wang.laic.kanban.models.Order;
import wang.laic.kanban.models.OrderStatusEnum;
import wang.laic.kanban.utils.KukuUtil;

/**
 * Created by duduba on 2017/4/10.
 */

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {


    public class ViewHolder extends RecyclerView.ViewHolder {
//        @BindView(R.id.tv_index) TextView tvIndex;
        @BindView(R.id.tv_order_no) TextView tvOrderNo;
        @BindView(R.id.tv_order_times) TextView tvOrderTimes;
        @BindView(R.id.tv_delivery_date) TextView tvDeliveryDate;
        @BindView(R.id.tv_arrival_date) TextView tvArrivalDate;
        @BindView(R.id.tv_order_status) TextView tvOrderStatus;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Order order = (Order)view.getTag();
                    if(order != null) {
                        Intent intent = new Intent(mContext, OrderActivity.class);
                        intent.putExtra(Constants.KEY_ORDER_FLAG, 0);
                        intent.putExtra(Constants.KEY_ORDER_NO, order.getOrderCompId().getOrderNo());
                        intent.putExtra(Constants.KEY_ORDER_TIMES,order.getOrderCompId().getDeliveryNumber());
                        mContext.startActivity(intent);
                    }
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
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);

//        Log.i(Constants.TAG, "---> " + holder.getAdapterPosition() + " -> " + holder.itemView.getTag());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Order order = items.get(position);
        holder.itemView.setTag(order);

//        Log.i(Constants.TAG, "===> pos=" + position + " -> " + order);

        OrderStatusEnum orderStatus = OrderStatusEnum.valueOf(order.getStatus());

//        holder.tvIndex.setText(String.format("% 2d", position + 1));
//        String orderNo = String.format(mContext.getString(R.string.prompt_order_no), order.getOrderCompId().getOrderNo());
        holder.tvOrderNo.setText(order.getOrderCompId().getOrderNo());

//        String orderTimes = String.format(mContext.getString(R.string.prompt_order_times), order.getOrderCompId().getDeliveryNumber());
        holder.tvOrderTimes.setText(""+order.getOrderCompId().getDeliveryNumber());

//        String deliveryDate = String.format(mContext.getString(R.string.prompt_order_delivery_date), KukuUtil.getFormatDate(order.getDeliveryDate()));
        holder.tvDeliveryDate.setText(KukuUtil.getFormatDate(order.getDeliveryDate()));

        if(orderStatus == OrderStatusEnum.AOG) {
//            String arrivalDate = String.format(mContext.getString(R.string.prompt_order_arrival_date), KukuUtil.getFormatDate(order.getArrivalDate()));
            holder.tvArrivalDate.setText(KukuUtil.getFormatDate(order.getArrivalDate()));
            holder.tvArrivalDate.setVisibility(View.VISIBLE);
            holder.tvOrderStatus.setTextColor(mContext.getResources().getColor(R.color.darkRed));
        } else {
            holder.tvArrivalDate.setVisibility(View.GONE);
            holder.tvOrderStatus.setTextColor(mContext.getResources().getColor(R.color.dark_gray));
        }
        holder.tvOrderStatus.setText(orderStatus.getName());

        if(position % 2 == 0) {
            holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        } else {
            holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.backgroundColor));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
