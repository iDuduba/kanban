package wang.laic.kanban.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import wang.laic.kanban.R;
import wang.laic.kanban.models.OpEnum;
import wang.laic.kanban.models.Part;

/**
 * Created by duduba on 2017/4/1.
 */

public class StockOutAdapter extends RecyclerSwipeAdapter<StockOutAdapter.ViewHolder> {

    private Context mContext;
    private List<Part> mDataset;

    public StockOutAdapter(Context context, List<Part> myDataset) {
        mContext = context;
        mDataset = myDataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stockout, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Part item = mDataset.get(position);

        holder.swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        holder.swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {
//                YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.trash));
            }
        });
        holder.swipeLayout.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
            @Override
            public void onDoubleClick(SwipeLayout layout, boolean surface) {
                Toast.makeText(mContext, "DoubleClick", Toast.LENGTH_SHORT).show();
            }
        });
        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDataset.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mDataset.size());
            }
        });

        holder.txtIndex.setText(String.format("%02d", position + 1));
        holder.txtPartModel.setText(String.format("型号:%s", item.getModel()));
        holder.txtPartNo.setText(String.format("零件号:%s", item.getPartNo()));
//        if(item.getLocation() != null && item.getLocation().length() > 0) {
//            holder.txtPartLocation.setText(String.format("工位:%s", item.getLocation()));
//        }
        holder.txtPartQuantity.setText(String.valueOf(item.getQuantity()));

        if(position % 2 == 0) {
            holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        } else {
            holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.backgroundColor));
        }

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

    public void add(int position, Part item) {
        mDataset.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(Part item) {
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

        @BindView(R.id.swipe) SwipeLayout swipeLayout;
        @BindView(R.id.delete) Button buttonDelete;

        // each data item is just a string in this case
        @BindView(R.id.tv_index) TextView txtIndex;
        @BindView(R.id.tv_part_model) TextView txtPartModel;
        @BindView(R.id.tv_part_no) TextView txtPartNo;
        @BindView(R.id.tv_part_location) TextView txtPartLocation;
        @BindView(R.id.tv_part_quantity) TextView txtPartQuantity;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Log.d(getClass().getSimpleName(), "onItemSelected: " + txtPartModel.getText().toString());
//                    Toast.makeText(view.getContext(), "onItemSelected: " + txtPartModel.getText().toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
