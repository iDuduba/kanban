package wang.laic.kanban;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wang.laic.kanban.models.Customer;
import wang.laic.kanban.models.Part;
import wang.laic.kanban.network.HttpClient;
import wang.laic.kanban.network.message.Question;
import wang.laic.kanban.network.message.StockOutAnswer;
import wang.laic.kanban.views.adapters.StockOutAdapter;

public class StockOutActivity extends BaseActivity {

    @BindView(R.id.part_list_view) RecyclerView recyclerView;
    @BindView(R.id.itemSize) TextView itemSizeView;
    @BindView(R.id.partQuantity) TextView partQuantityView;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<Part> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_out);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        setToolbarTitle(getString(R.string.title_activity_stock_out) + "(" + getCurrentCustomer().getName() + ")");


        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        KanbanApplication app = (KanbanApplication)getApplication();
        items = (List<Part>)app.getParameter(Constants.K_STOCK_OUT_PART_LIST);

        String fm = getString(R.string.prompt_stock_out_1);
        itemSizeView.setText(String.format(fm, items.size()));
        fm = getString(R.string.prompt_stock_out_2);
        int sum = 0;
        for(Part part : items) {
            sum += part.getQuantity();
        }
        partQuantityView.setText(String.format(fm, sum));

        // specify an adapter (see also next example)
        mAdapter = new StockOutAdapter(this, items);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void okHttp_stock_out(String customerCode) {
        Map<String, Object> body = new HashMap<>();
        body.put("customerCode", getCurrentCustomer().getCode());
        body.put("items", items);
        Question<Map<String, Object>> msg = new Question<>();
        msg.setBody(body);

        HttpClient.getInstance(this).doPost("/stockout", msg, StockOutAnswer.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStockOutEvent(StockOutAnswer event) {
        Log.i(Constants.TAG, "code = " + event.getCode());
        if(event.getCode() == 0) {
            Toast.makeText(this, "OK", Toast.LENGTH_LONG).show();
        } else {
        }
    }

    @OnClick(R.id.confirm)
    public void confirm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("确认提交数据?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                okHttp_stock_out("KISS");

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
