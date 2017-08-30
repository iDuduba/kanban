package wang.laic.kanban;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wang.laic.kanban.models.Customer;
import wang.laic.kanban.models.OpEnum;
import wang.laic.kanban.models.OrderStatusEnum;
import wang.laic.kanban.models.Part;
import wang.laic.kanban.network.HttpClient;
import wang.laic.kanban.network.message.Failure;
import wang.laic.kanban.network.message.PartAnswer;
import wang.laic.kanban.network.message.Question;

public class ScanPartActivity extends BaseActivity {

    @BindView(R.id.tv_stock_out_info) TextView tvOutInfo;

    @BindView(R.id.barcode_scanner) DecoratedBarcodeView viewBarcodeScanner;
    @BindView(R.id.tv_part_model) TextView tvModel;
    @BindView(R.id.tv_part_no) TextView tvPartNo;
    @BindView(R.id.tv_part_shelf) TextView tvShelf;
    @BindView(R.id.tv_part_safe_stock) TextView tvSafeStock;
    @BindView(R.id.tv_part_stock) TextView tvStock;
    @BindView(R.id.et_quantity) EditText etQuantity;
    @BindView(R.id.btn_commit) Button btnCommit;

    BeepManager beepManager;

    private String lastText;

    private OpEnum[] mItems = new OpEnum[] {OpEnum.OUT, OpEnum.LOSS, OpEnum.EXOUT};

//    private Part mPart = new Part();
    private List<Part> mOuts = new ArrayList<>();

    private int mOpType = OpEnum.OUT.getType();
    private String mLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_part);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        setToolbarTitle(getString(R.string.title_activity_scan_part) + "(" + getCurrentCustomer().getName() + ")");

        mLocation = getIntent().getStringExtra(Constants.KEY_LOCATION);
        mOpType = getIntent().getIntExtra(Constants.K_STOCK_OUT_OP_TYPE, OpEnum.OUT.getType());

        String xxx = String.format(getString(R.string.lab_out_info), OpEnum.getName(mOpType), mLocation);
        tvOutInfo.setText(Html.fromHtml(xxx));

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        resetProdInfo();

        viewBarcodeScanner.decodeContinuous(callback);
        viewBarcodeScanner.setStatusText(getString(R.string.scan_status_text));

        beepManager = new BeepManager(this);
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
//            if(result.getText() == null || result.getText().equals(lastText)) {
//                return;
//            }
            String scanText = result.getText();
            if(scanText == null || scanText.isEmpty()) {
                return;
            }
            viewBarcodeScanner.pause();

            lastText = scanText;
            tvModel.setText(lastText);

            beepManager.playBeepSoundAndVibrate();

            mProgress.show();
            okHttp_get_part(lastText);

            //Added preview of scanned barcode
//            ImageView imageView = (ImageView) findViewById(R.id.barcodePreview);
//            imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    private void okHttp_get_part(String model) {
        Map<String, Object> body = new HashMap<>();
        body.put("customerCode", getCurrentCustomer().getCode());
        body.put("itemCode", model);
        body.put("location", mLocation);
        Question<Map<String, Object>> msg = new Question<>();
        msg.setBody(body);

        HttpClient.getInstance(this).doPost("/part", msg, PartAnswer.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onQueryPartEvent(PartAnswer event) {
        mProgress.dismiss();

        if(event.getCode() == 0) {
            List<Part> items = event.getBody();
            if(items != null && items.size() > 0) {

                tvModel.setText(items.get(0).getModel());
                tvPartNo.setText(items.get(0).getPartNo());
                tvShelf.setText(items.get(0).getShelfNo());
                tvSafeStock.setText("" + items.get(0).getSafeStock());
                tvStock.setText("" + items.get(0).getInvQuantity());

                etQuantity.setSelectAllOnFocus(true);
                etQuantity.requestFocus();
                etQuantity.setSelected(true);
            } else {
                showMessage("单品不存在");
                resetProdInfo();
                viewBarcodeScanner.resume();
            }
        } else {
            String errorMessage = event.getMessage();
            Log.i(Constants.TAG, "code=" + event.getCode() + " >" + errorMessage);
            if(event.getCode() == 9999) {
                errorMessage = getString(R.string.error_sever_exception);
            }
            showMessage(errorMessage);
            viewBarcodeScanner.resume();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFailureEvent(Failure failure) {
        if(failure.getBody().compareTo("/part") == 0) {
            mProgress.dismiss();
            showMessage(failure.getMessage());
        }
    }

    public void triggerScan(View view) {
        viewBarcodeScanner.decodeSingle(callback);
    }

    private boolean add() {
        String sStock = tvStock.getText().toString();
        if(sStock == null || sStock.isEmpty()) {
            showMessage("没有单品信息");
            return true;
        }
        String sQuantity = etQuantity.getText().toString();
        if(sQuantity == null || sQuantity.isEmpty()) {
            etQuantity.setError("出库数量不能为空");
            return false;
        }
        double invQuantity = Double.parseDouble(sStock);
        double quantity = Double.parseDouble(sQuantity);
        if(quantity > invQuantity) {
            etQuantity.setError("出库数量(" + quantity + ")不能超过库存量(" + invQuantity + ")");
            return false;
        }

        Part part = new Part();
        part.setModel(tvModel.getText().toString());
        part.setPartNo(tvPartNo.getText().toString());
        part.setQuantity(quantity);

        if(mOuts.contains(part)) {
            mOuts.remove(part);
        }
        mOuts.add(part);
        btnCommit.setText("提交(" + mOuts.size() + ")");

        return true;
    }

    @OnClick(R.id.btn_resume)
    public void resume() {
        boolean resumeScanner = add();
        if(resumeScanner) {
            viewBarcodeScanner.resume();
            resetProdInfo();
        }
    }

    @OnClick(R.id.btn_commit)
    public void commit() {
        boolean resumeScanner = add();
        if(resumeScanner) {
//            viewBarcodeScanner.resume();
            resetProdInfo();
        } else {
            return;
        }

        if(mOuts == null || mOuts.size() == 0) {
            showMessage("没有待出库的单品数据");
            return;
        }

        setAppParameter(Constants.K_STOCK_OUT_PART_LIST, mOuts);

        Intent intent = new Intent(this, StockOutActivity.class);
        intent.putExtra(Constants.KEY_LOCATION, mLocation);
        intent.putExtra(Constants.K_STOCK_OUT_OP_TYPE, mOpType);
        startActivity(intent);

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        viewBarcodeScanner.resume();

        mOuts = (List<Part>)getAppParameter(Constants.K_STOCK_OUT_PART_LIST);
        if(mOuts == null) {
            lastText = null;
            mOuts = new ArrayList<>();
        }
        btnCommit.setText("提交(" + mOuts.size() + ")");
    }


    @Override
    protected void onPause() {
        super.onPause();
        viewBarcodeScanner.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        setAppParameter(Constants.K_STOCK_OUT_PART_LIST, null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return viewBarcodeScanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }


    private void resetProdInfo() {
        tvModel.setText(null);
        tvPartNo.setText(null);
        tvShelf.setText(null);
        tvSafeStock.setText(null);
        tvStock.setText(null);
        etQuantity.setText(null);
    }

}
