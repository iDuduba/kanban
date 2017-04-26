package wang.laic.kanban;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScanOrderActivity extends BaseActivity {

    @BindView(R.id.orderNo) EditText orderNoView;
    @BindView(R.id.orderTimes) EditText orderTimesView;

    int mOrderFlag = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_order);

        ButterKnife.bind(this);

        mOrderFlag = getIntent().getIntExtra(Constants.KEY_ORDER_FLAG, 0);
        switch(mOrderFlag) {
            case 1:
                setToolbarTitle(getString(R.string.title_activity_order_revoked) + "(" + getCurrentCustomer().getName() + ")");
                break;
            case 2:
                setToolbarTitle(getString(R.string.title_activity_order_in) + "(" + getCurrentCustomer().getName() + ")");
                break;
        }
    }

    @OnClick(R.id.scannerButton)
    public void onScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(ScanActivity.class);
        integrator.setPrompt("请扫描订单号");
        integrator.setBeepEnabled(true); //扫描成功的「哔哔」声，默认开启
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @OnClick(R.id.confirm)
    public void onConfirm() {
        String order = orderNoView.getText().toString();
        String times = orderTimesView.getText().toString();
        if(order.isEmpty()) {
            orderNoView.setError("无效的订单号");
            return;
        }
        if(times.isEmpty()) {
            orderTimesView.setError("无效的次数");
            return;
        }
        Intent intent = new Intent(this, OrderActivity.class);
        intent.putExtra(Constants.KEY_ORDER_FLAG, mOrderFlag);
        intent.putExtra(Constants.KEY_ORDER_NO, orderNoView.getText().toString());
        intent.putExtra(Constants.KEY_ORDER_TIMES, Integer.parseInt(orderTimesView.getText().toString()));
        startActivity(intent);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "无效的二维码", Toast.LENGTH_LONG).show();
            } else {
                String content = result.getContents();
                String[] c = content.split("-");
                orderNoView.setText(c[0]);
                if(c.length > 1) {
                    orderTimesView.setText(c[1]);
                    onConfirm();
                }
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
