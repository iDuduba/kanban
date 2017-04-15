package wang.laic.kanban;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wang.laic.kanban.network.HttpClient;
import wang.laic.kanban.network.message.FlowAnswer;
import wang.laic.kanban.network.message.Question;
import wang.laic.kanban.utils.KukuUtils;
import wang.laic.kanban.views.MyLinearItemDecoration;
import wang.laic.kanban.views.adapters.FlowAdapter;

public class FlowActivity extends BaseActivity {

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @BindView(R.id.rv_flow_list) RecyclerView rvFlowList;

    @BindView(R.id.et_item_code) EditText tvItemCode;

    @BindView(R.id.tv_begin_date) TextView tvBeginDate;
    @BindView(R.id.tv_end_date) TextView tvEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        setToolbarTitle(getString(R.string.title_activity_flow_query) + "(" + getCurrentCustomer().getName() + ")");

        rvFlowList.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        rvFlowList.setLayoutManager(mLayoutManager);

        rvFlowList.addItemDecoration(new MyLinearItemDecoration(this, MyLinearItemDecoration.VERTICAL_LIST));

        DateTime now = DateTime.now();
        setDateRange(now.minusDays(6), now);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.scannerButton)
    public void onScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(ScanActivity.class);
        integrator.setPrompt("请扫描单品编号");
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d(Constants.TAG, "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d(Constants.TAG, "Scanned");
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                tvItemCode.setText(result.getContents());
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @OnClick(R.id.search)
    public void onSarch() {
        okHttp_flow_query();
    }

    private void okHttp_flow_query() {
        Map<String, Object> body = new HashMap<>();
        body.put("customerCode", getCurrentCustomer().getCode());
        body.put("itemCode", tvItemCode.getText().toString());
        body.put("beginDate", tvBeginDate.getText().toString());
        body.put("endDate", tvEndDate.getText().toString());
        Question<Map<String, Object>> msg = new Question<>();
        msg.setBody(body);

        HttpClient.getInstance(this).doPost("/stockflow",msg, FlowAnswer.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOrderDetailEvent(FlowAnswer event) {
        if(event.getCode() == 0) {
            mAdapter = new FlowAdapter(this, event.getBody());
            rvFlowList.setAdapter(mAdapter);
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

    public void onDateRangeRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        DateTime now = DateTime.now();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_week:
                if (checked) {
                    setDateRange(now.minusDays(6), now);
                }
                break;
            case R.id.radio_month:
                if (checked) {
                    setDateRange(now.minusMonths(1), now);
                }
                break;
            case R.id.radio_3_month:
                if (checked) {
                    setDateRange(now.minusMonths(3), now);
                }
                break;
        }
    }

    @OnClick(R.id.tv_begin_date)
    public void onBeginDate() {
//        DateTime now = DateTime.now();
        DateTime initDate = KukuUtils.parse(tvBeginDate.getText().toString());

        // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
        new DatePickerDialog(FlowActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker dp, int year, int month, int dayOfMonth) {
                        tvBeginDate.setText(String.format("%d-%02d-%02d", year, month+1, dayOfMonth));
                    }
                },
                initDate.getYear(), initDate.getMonthOfYear()-1, initDate.getDayOfMonth())
                .show();
    }

    @OnClick(R.id.tv_end_date)
    public void onEndDate() {
        DateTime initDate = KukuUtils.parse(tvEndDate.getText().toString());

        new DatePickerDialog(FlowActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker dp, int year, int month, int dayOfMonth) {
                        tvEndDate.setText(String.format("%d-%02d-%02d", year, month+1, dayOfMonth));
                    }
                },
                initDate.getYear(), initDate.getMonthOfYear()-1, initDate.getDayOfMonth())
                .show();
    }

    private void setDateRange(DateTime beginDate, DateTime endDate) {
        tvBeginDate.setText(beginDate.toString("yyyy-MM-dd"));
        tvEndDate.setText(endDate.toString("yyyy-MM-dd"));
    }

}
