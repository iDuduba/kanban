package wang.laic.kanban.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.List;

import wang.laic.kanban.R;
import wang.laic.kanban.models.OrderItem;

/**
 * Created by duduba on 2017/4/1.
 */

public class PartAdapter extends RecyclerSwipeAdapter<PartAdapter.ViewHolder> {

    private Context mContext;
    private List<OrderItem> mDataset;

    public PartAdapter(Context context, List<OrderItem> myDataset) {
        mContext = context;
        mDataset = myDataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_part, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final OrderItem item = mDataset.get(position);

        holder.txtPartModel.setText(item.getItemCode());
        holder.txtPartNo.setText(item.getExtpn());
        holder.txtPartCategory.setText(item.getDescription());
        holder.txtPartQuantity.setText(String.valueOf(item.getSendQuantity()));

//        holder.txtPartModel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                remove(item);
//            }
//        });
//
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void add(int position, OrderItem item) {
        mDataset.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(OrderItem item) {
        int position = mDataset.indexOf(item);
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return position;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {

        // each data item is just a string in this case
        public TextView txtPartModel;
        public TextView txtPartNo;
        public TextView txtPartCategory;
        public TextView txtPartQuantity;

        public ViewHolder(View itemView) {
            super(itemView);

            txtPartModel = (TextView) itemView.findViewById(R.id.part_model);
            txtPartNo = (TextView) itemView.findViewById(R.id.part_no);
            txtPartCategory = (TextView) itemView.findViewById(R.id.part_category);
            txtPartQuantity = (TextView) itemView.findViewById(R.id.part_quantity);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(getClass().getSimpleName(), "onItemSelected: " + txtPartModel.getText().toString());
                    Toast.makeText(view.getContext(), "onItemSelected: " + txtPartModel.getText().toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
