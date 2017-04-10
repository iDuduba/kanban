package wang.laic.kanban;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.UUID;

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

    @BindView(R.id.barcode_scanner) DecoratedBarcodeView barcodeView;
    @BindView(R.id.part_model) TextView modelView;
    @BindView(R.id.part_no) TextView partNoView;
    @BindView(R.id.part_category) TextView categoryView;
    @BindView(R.id.part_stock) TextView stockView;
    @BindView(R.id.select_out_type) Spinner stockTypeView;
    @BindView(R.id.quantity) EditText quantityView;

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

        barcodeView.decodeContinuous(callback);
        barcodeView.setStatusText(getString(R.string.scan_status_text));

        beepManager = new BeepManager(this);
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if(result.getText() == null || result.getText().equals(lastText)) {
                return;
            }

            lastText = result.getText();
            modelView.setText(lastText);

            beepManager.playBeepSoundAndVibrate();

            okHttp_get_part(lastText);

            barcodeView.pause();
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
        KanbanApplication app = (KanbanApplication)getApplication();
        Customer customer = (Customer)app.getParameter(Constants.KEY_CURRENT_CUSTOMER);
        body.put("customerCode", customer.getCode());
        body.put("model", model);
        Question<Map<String, Object>> msg = new Question<>();
        msg.setBody(body);

        HttpClient.getInstance(this).doPost("/part", msg, PartAnswer.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onQueryPartEvent(PartAnswer event) {
        if(event.getCode() == 0) {
            modelView.setText(event.getBody().getModel());
            partNoView.setText(event.getBody().getPartNo());
            categoryView.setText(event.getBody().getCategory());
            stockView.setText("" + event.getBody().getQuantity());
        }
    }

    public void triggerScan(View view) {
        barcodeView.decodeSingle(callback);
    }

    @OnClick(R.id.resume)
    public void resume() {
        mPart.setModel(modelView.getText().toString());
        mPart.setPartNo(partNoView.getText().toString());
        mPart.setCategory(categoryView.getText().toString());
        mPart.setQuantity(Integer.parseInt(quantityView.getText().toString()));

        mOuts.add(mPart);
        mPart = new Part();

        modelView.setText(null);
        partNoView.setText(null);
        categoryView.setText(null);
        stockView.setText(null);

        barcodeView.resume();
    }

    @OnClick(R.id.commit)
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
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }


}
