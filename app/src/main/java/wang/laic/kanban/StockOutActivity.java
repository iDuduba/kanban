package wang.laic.kanban;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
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
import wang.laic.kanban.models.OpEnum;
import wang.laic.kanban.models.Part;
import wang.laic.kanban.network.HttpClient;
import wang.laic.kanban.network.message.Failure;
import wang.laic.kanban.network.message.Question;
import wang.laic.kanban.network.message.StockOutAnswer;
import wang.laic.kanban.views.adapters.StockOutAdapter;

public class StockOutActivity extends BaseActivity {

    @BindView(R.id.part_list_view) RecyclerView recyclerView;
    @BindView(R.id.itemSize) TextView itemSizeView;
    @BindView(R.id.partQuantity) TextView partQuantityView;
    @BindView(R.id.tv_op_type) TextView tvOpType;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private int mOpType;
    private List<Part> items = null;

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

        mOpType = getIntent().getIntExtra(Constants.K_STOCK_OUT_OP_TYPE, OpEnum.OUT.getType());
        tvOpType.setText(OpEnum.getName(mOpType) + " >>> ");

        items = (List<Part>)getAppParameter(Constants.K_STOCK_OUT_PART_LIST);

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

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void okHttp_stock_out() {
        Map<String, Object> body = new HashMap<>();
        body.put("customerCode", getCurrentCustomer().getCode());
        body.put("opType", mOpType);
        body.put("items", items);
        Question<Map<String, Object>> msg = new Question<>();
        msg.setBody(body);

        HttpClient.getInstance(this).doPost("/stockout", msg, StockOutAnswer.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStockOutEvent(StockOutAnswer event) {
        mProgress.dismiss();
        if(event.getCode() == 0) {
            showMessage("出库成功!");
            items = null;
            removeAppParameter(Constants.K_STOCK_OUT_PART_LIST);
            finish();
        } else {
            String errorMessage = event.getMessage();
            Log.i(Constants.TAG, "code=" + event.getCode() + " >" + errorMessage);
            if(event.getCode() == 9999) {
                errorMessage = getString(R.string.error_sever_exception);
            }
            showMessage(errorMessage);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFailureEvent(Failure failure) {
        if(failure.getBody().compareTo("/stockout") == 0) {
            mProgress.dismiss();
            showMessage(failure.getMessage());
        }
    }

    @OnClick(R.id.btn_confirm)
    public void onBtnConfirm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("确认提交数据?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                mProgress.show();
                okHttp_stock_out();
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
    @OnClick(R.id.btn_cancel)
    public void onBtnCancel() {
        if(items != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("是否取消出库?");
            builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    items = null;
                    removeAppParameter(Constants.K_STOCK_OUT_PART_LIST);
                    finish();
                }
            });
            builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
