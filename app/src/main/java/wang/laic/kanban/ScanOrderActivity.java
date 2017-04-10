package wang.laic.kanban;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScanOrderActivity extends BaseActivity {

    @BindView(R.id.orderNo) EditText orderNoView;
    @BindView(R.id.orderTimes) EditText orderTimesView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_order);

        ButterKnife.bind(this);

        boolean isRevoked = getIntent().getBooleanExtra(Constants.KEY_ORDER_REVOKED_FLAG, false);
        if(isRevoked) {
            setToolbarTitle(getString(R.string.title_activity_order_revoked) + "(" + getCurrentCustomer().getName() + ")");
        } else {
            setToolbarTitle(getString(R.string.title_activity_order_in) + "(" + getCurrentCustomer().getName() + ")");
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
        Intent intent = new Intent(this, OrderActivity.class);
        intent.putExtra(Constants.KEY_ORDER_NO, orderNoView.getText().toString());
        intent.putExtra(Constants.KEY_ORDER_TIMES, Integer.parseInt(orderTimesView.getText().toString()));
        startActivity(intent);
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
                orderNoView.setText(result.getContents());
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
