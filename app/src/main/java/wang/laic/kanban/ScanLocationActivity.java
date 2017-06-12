package wang.laic.kanban;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wang.laic.kanban.R;
import wang.laic.kanban.models.OpEnum;

public class ScanLocationActivity extends BaseActivity {

    @BindView(R.id.et_location) EditText locationView;

    private OpEnum mOpType = OpEnum.OUT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_location);

        ButterKnife.bind(this);

        setToolbarTitle(getString(R.string.title_activity_scan_part) + "(" + getCurrentCustomer().getName() + ")");

    }


    @OnClick(R.id.scannerButton)
    public void onScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(ScanActivity.class);
        integrator.setPrompt("扫描工位");
        integrator.setBeepEnabled(true); //扫描成功的「哔哔」声，默认开启
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }


    @OnClick(R.id.confirm)
    public void onConfirm() {
        String location = locationView.getText().toString();
        if(location.isEmpty()) {
            locationView.setError("无效的工位");
            return;
        }
        Intent intent = new Intent(this, ScanPartActivity.class);
        intent.putExtra(Constants.KEY_LOCATION, location);
        intent.putExtra(Constants.K_STOCK_OUT_OP_TYPE, mOpType.getType());
        startActivity(intent);
    }

    public void onOpRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_op_out:
                if (checked) {
                    mOpType = OpEnum.OUT;
                }
                break;
            case R.id.radio_op_loss:
                if (checked) {
                    mOpType = OpEnum.LOSS;
                }
                break;
            case R.id.radio_op_exout:
                if (checked) {
                    mOpType = OpEnum.EXOUT;
                }
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "无效的二维码", Toast.LENGTH_LONG).show();
            } else {
                String content = result.getContents();
                locationView.setText(content);
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
