package wang.laic.kanban;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wang.laic.kanban.models.Order;
import wang.laic.kanban.models.OrderItem;
import wang.laic.kanban.models.OrderKey;
import wang.laic.kanban.models.OrderStatusEnum;
import wang.laic.kanban.network.HttpClient;
import wang.laic.kanban.network.message.Failure;
import wang.laic.kanban.network.message.OrderDetailAnswer;
import wang.laic.kanban.network.message.OrderEntryCancelAnswer;
import wang.laic.kanban.network.message.Question;
import wang.laic.kanban.utils.KukuUtil;
import wang.laic.kanban.views.adapters.PartAdapter;

public class OrderActivity extends BaseActivity {

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @BindView(R.id.part_list_view) RecyclerView recyclerView;

    @BindView(R.id.orderNo) TextView orderNoView;
    @BindView(R.id.orderTimes) TextView orderTimesView;
    @BindView(R.id.totalAmount) TextView totalAmountView;
    @BindView(R.id.orderStatus) TextView orderStatusView;
    @BindView(R.id.deliveryDate) TextView deliveryDateView;
    @BindView(R.id.arrivalDate) TextView arrivalDateView;
    @BindView(R.id.confirm) Button confirmButton;

    private OrderStatusEnum mOrderStatus;
    private OrderKey orderKey = new OrderKey();

    private int mOrderFlag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        setToolbarTitle(getString(R.string.title_activity_order) + "(" + getCurrentCustomer().getName() + ")");

        Intent intent = getIntent();

        mOrderFlag = getIntent().getIntExtra(Constants.KEY_ORDER_FLAG, 0);

        orderKey.setOrderNo(intent.getStringExtra(Constants.KEY_ORDER_NO));
        orderKey.setDeliveryNumber(intent.getIntExtra(Constants.KEY_ORDER_TIMES, 1));

        String xxx = String.format(getString(R.string.prompt_order_no), orderKey.getOrderNo());
        orderNoView.setText(Html.fromHtml(xxx));

        xxx = String.format(getString(R.string.prompt_order_times), orderKey.getDeliveryNumber());
        orderTimesView.setText(Html.fromHtml(xxx));

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.addItemDecoration(new MyLinearItemDecoration(this, MyLinearItemDecoration.VERTICAL_LIST));

        mProgress.show();
        okHttp_order_detail(orderKey);
    }

    private void okHttp_order_detail(OrderKey orderKey) {
        Map<String, Object> body = new HashMap<>();
        body.put("customerCode", getCurrentCustomer().getCode());
        body.put("orderNo", orderKey.getOrderNo());
        body.put("deliveryNumber", orderKey.getDeliveryNumber());
        Question<Map<String, Object>> msg = new Question<>();
        msg.setBody(body);

        HttpClient.getInstance(this).doPost("/orderDetail", msg, OrderDetailAnswer.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOrderDetailEvent(OrderDetailAnswer event) {
        mProgress.dismiss();
        if(event.getCode() == 0) {
            if(event.getBody() != null && event.getBody().size() > 0) {
                fill(event.getBody().get(0));
            }
        } else {
            String errorMessage = event.getMessage();
            Log.i(Constants.TAG, "code = " + event.getCode() + " >" + errorMessage);
            if(event.getCode() == 9999) {
                errorMessage = getString(R.string.error_sever_exception);
            }
            showMessage(errorMessage);
            finish();
        }
    }

    private void fill(Order order) {
        mOrderStatus = OrderStatusEnum.valueOf(order.getStatus());
        String xxx = String.format(getString(R.string.prompt_order_status), mOrderStatus.getBackName());
        orderStatusView.setText(Html.fromHtml(xxx));
        if(order.getDeliveryDate() != null) {
            xxx = String.format(getString(R.string.prompt_order_delivery_date), KukuUtil.getFormatDate(order.getDeliveryDate()));
            deliveryDateView.setText(Html.fromHtml(xxx));
        }
        switch(mOrderStatus) {
            case WAY:
                confirmButton.setText(getString(R.string.lab_confirm_stock_in));
                if(mOrderFlag == 2) {
                    showMessage("订单尚未入库，不能撤销");
                    confirmButton.setVisibility(View.GONE);
                }
                arrivalDateView.setVisibility(View.GONE);
                break;
            case AOG:
                confirmButton.setText(getString(R.string.lab_revoke_stock_in));
                if(mOrderFlag == 1) {
                    showMessage("订单已经入库，不能重复入库");
                    confirmButton.setVisibility(View.GONE);
                }
                if(order.getArrivalDate() != null) {
                    xxx = String.format(getString(R.string.prompt_order_arrival_date), KukuUtil.getFormatDate(order.getArrivalDate()));
                    arrivalDateView.setText(Html.fromHtml(xxx));
                }
                break;
            default:
                arrivalDateView.setVisibility(View.GONE);
                confirmButton.setVisibility(View.GONE);
                break;
        }

        int sum = 0;
        for(OrderItem item : order.getItems()) {
            sum += item.getSendQuantity();
        }
        xxx = String.format(getString(R.string.prompt_order_total_amount), sum);
        totalAmountView.setText(Html.fromHtml(xxx));

        mAdapter = new PartAdapter(this, order.getItems());
        recyclerView.setAdapter(mAdapter);
    }

    @OnClick(R.id.confirm)
    public void confirm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("确认提交数据?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                okHttp_order_entry_or_cancel(orderKey);

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

    private void okHttp_order_entry_or_cancel(OrderKey orderKey) {
        Map<String, Object> body = new HashMap<>();
        body.put("customerCode", getCurrentCustomer().getCode());
        body.put("orderNo", orderKey.getOrderNo());
        body.put("deliveryNumber", orderKey.getDeliveryNumber());
        Question<Map<String, Object>> msg = new Question<>();
        msg.setBody(body);

        mProgress.show();
        switch(mOrderStatus) {
            case WAY:
                HttpClient.getInstance(this).doPost("/entry", msg, OrderEntryCancelAnswer.class);
                break;
            case AOG:
                HttpClient.getInstance(this).doPost("/entryCancel", msg, OrderEntryCancelAnswer.class);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOrderEntryCancelEvent(OrderEntryCancelAnswer event) {
        mProgress.dismiss();
        if(event.getCode() == 0) {
            switch(mOrderStatus) {
                case WAY:
                    showMessage("订单入库成功");
                    break;
                case AOG:
                    showMessage("订单撤销成功");
                    break;
            }
            finish();
        } else {
            String errorMessage = event.getMessage();
            Log.i(Constants.TAG, "code = " + event.getCode() + " >" + errorMessage);
            if(event.getCode() == 9999) {
                errorMessage = getString(R.string.error_sever_exception);
            }
            showMessage(errorMessage);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFailureEvent(Failure failure) {
        String url = failure.getBody();
        if(url.compareTo("/orderDetail") == 0 ||
                url.compareTo("/entry") == 0 ||
                url.compareTo("/entryCancel") == 0) {

            mProgress.dismiss();
            Toast.makeText(OrderActivity.this, failure.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
