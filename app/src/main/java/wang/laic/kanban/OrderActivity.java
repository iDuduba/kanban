package wang.laic.kanban;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
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
import wang.laic.kanban.models.Order;
import wang.laic.kanban.models.OrderItem;
import wang.laic.kanban.models.OrderStatusEnum;
import wang.laic.kanban.network.HttpClient;
import wang.laic.kanban.network.message.OrderAnswer;
import wang.laic.kanban.network.message.OrderDetailAnswer;
import wang.laic.kanban.network.message.Question;
import wang.laic.kanban.utils.KukuUtils;
import wang.laic.kanban.views.MyLinearItemDecoration;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        setToolbarTitle(getString(R.string.title_activity_order) + "(" + getCurrentCustomer().getName() + ")");

        Intent intent = getIntent();
        String orderNo = intent.getStringExtra(Constants.KEY_ORDER_NO);
        int times = intent.getIntExtra(Constants.KEY_ORDER_TIMES, 1);

        orderNoView.setText(String.format(getString(R.string.prompt_order_no), orderNo));
        orderTimesView.setText(String.format(getString(R.string.prompt_order_times), times));

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.addItemDecoration(new MyLinearItemDecoration(this, MyLinearItemDecoration.VERTICAL_LIST));

        okHttp_order_detail(orderNo, times);
    }

    private void okHttp_order_detail(String orderNo, int times) {
        Map<String, Object> body = new HashMap<>();
        body.put("customerCode", getCurrentCustomer().getCode());
        body.put("orderNo", orderNo);
        body.put("deliveryNumber", times);
        Question<Map<String, Object>> msg = new Question<>();
        msg.setBody(body);

        HttpClient.getInstance(this).doPost("/orderDetail", msg, OrderDetailAnswer.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOrderDetailEvent(OrderDetailAnswer event) {
        if(event.getCode() == 0) {
            if(event.getBody() != null && event.getBody().size() > 0) {
                fill(event.getBody().get(0));
            }
        } else {
            Log.i(Constants.TAG, "code = " + event.getCode());
            String errorMessage = event.getMessage();
            if(event.getCode() == 9999) {
                errorMessage = getString(R.string.error_sever_exception);
            }
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    private void fill(Order order) {
        OrderStatusEnum os = OrderStatusEnum.valueOf(order.getStatus());
        orderStatusView.setText(String.format(getString(R.string.prompt_order_status), os.getBackName()));
        if(order.getDeliveryDate() != null) {
            deliveryDateView.setText(String.format(getString(R.string.prompt_order_delivery_date), KukuUtils.getFormatDate(order.getDeliveryDate())));
        }
        switch(os) {
            case WAY:
                confirmButton.setText(getString(R.string.lab_confirm_stock_in));
                arrivalDateView.setVisibility(View.GONE);
                break;
            case AOG:
                confirmButton.setText(getString(R.string.lab_revoke_stock_in));
                if(order.getArrivalDate() != null) {
                    arrivalDateView.setText(String.format(getString(R.string.prompt_order_arrival_date), KukuUtils.getFormatDate(order.getArrivalDate())));
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
        totalAmountView.setText(String.format(getString(R.string.prompt_order_total_amount), sum));

        mAdapter = new PartAdapter(this, order.getItems());
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
