package wang.laic.kanban;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import wang.laic.kanban.models.Part;
import wang.laic.kanban.network.HttpClient;
import wang.laic.kanban.network.message.PartAnswer;
import wang.laic.kanban.network.message.Question;

public class ScanPartActivity extends BaseActivity {

    @BindView(R.id.barcode_scanner) DecoratedBarcodeView viewBarcodeScanner;
    @BindView(R.id.tv_part_model) TextView tvModel;
    @BindView(R.id.tv_part_no) TextView tvPartNo;
    @BindView(R.id.tv_part_category) TextView tvCategory;
    @BindView(R.id.tv_part_stock) TextView tvStock;
    @BindView(R.id.spinner_out_type) Spinner stockTypeView;
    @BindView(R.id.et_quantity) EditText etQuantity;

    BeepManager beepManager;

    private String lastText;

    private OpEnum[] mItems = new OpEnum[] {OpEnum.OUT, OpEnum.LOSS, OpEnum.EXOUT, OpEnum.TRANSOUT, OpEnum.RETOUT};

    private Part mPart = new Part();
    private List<Part> mOuts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_part);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        setToolbarTitle(getString(R.string.title_activity_scan_part) + "(" + getCurrentCustomer().getName() + ")");

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

        ArrayAdapter<OpEnum> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, mItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stockTypeView.setAdapter(adapter);

        stockTypeView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String[] types = getResources().getStringArray(R.array.stock_out_type);
//                Toast.makeText(ScanPartActivity.this, "你的选择是：" + mItems[position], Toast.LENGTH_SHORT).show();
                mPart.setOpType(mItems[position].getType());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        viewBarcodeScanner.decodeContinuous(callback);
        viewBarcodeScanner.setStatusText(getString(R.string.scan_status_text));

        beepManager = new BeepManager(this);
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if(result.getText() == null || result.getText().equals(lastText)) {
                return;
            }

            lastText = result.getText();
            tvModel.setText(lastText);

            beepManager.playBeepSoundAndVibrate();

            okHttp_get_part(lastText);

            viewBarcodeScanner.pause();
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
        Question<Map<String, Object>> msg = new Question<>();
        msg.setBody(body);

        HttpClient.getInstance(this).doPost("/part", msg, PartAnswer.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onQueryPartEvent(PartAnswer event) {
        if(event.getCode() == 0) {
            List<Part> items = event.getBody();
            if(items != null && items.size() > 0) {
                tvModel.setText(items.get(0).getModel());
                tvPartNo.setText(items.get(0).getPartNo());
                tvCategory.setText(items.get(0).getCategory());
                tvStock.setText("" + items.get(0).getQuantity());
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

    public void triggerScan(View view) {
        viewBarcodeScanner.decodeSingle(callback);
    }

    @OnClick(R.id.btn_resume)
    public void resume() {
        mPart.setModel(tvModel.getText().toString());
        mPart.setPartNo(tvPartNo.getText().toString());
        mPart.setCategory(tvCategory.getText().toString());
        mPart.setQuantity(Integer.parseInt(etQuantity.getText().toString()));

        mOuts.add(mPart);
        mPart = new Part();

        tvModel.setText(null);
        tvPartNo.setText(null);
        tvCategory.setText(null);
        tvStock.setText(null);

        viewBarcodeScanner.resume();
    }

    @OnClick(R.id.btn_commit)
    public void commit() {
        KanbanApplication app = (KanbanApplication)getApplication();
        app.setParameter(Constants.K_STOCK_OUT_PART_LIST, mOuts);

        Intent intent = new Intent(this, StockOutActivity.class);
        intent.putExtra(Constants.K_STOCK_OUT_PART_LIST, Constants.K_STOCK_OUT_PART_LIST);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewBarcodeScanner.resume();
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
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return viewBarcodeScanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }


}
