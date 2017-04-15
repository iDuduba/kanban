package wang.laic.kanban;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
import wang.laic.kanban.network.HttpClient;
import wang.laic.kanban.network.message.CustomersAnswer;
import wang.laic.kanban.network.message.Question;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.spinner_customers) Spinner mCustomersView;

    List<Customer> customers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        mCustomersView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "你的选择是：" + customers.get(position).getName(), Toast.LENGTH_SHORT).show();
                KanbanApplication app = (KanbanApplication)getApplication();
                app.setParameter(Constants.KEY_CURRENT_CUSTOMER, customers.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        KanbanApplication app = (KanbanApplication)getApplication();
        String user = (String)app.getParameter(Constants.KEY_CURRENT_USER);
        okHttp_getCustomers(user);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void okHttp_getCustomers(String user) {
        Map<String, Object> body = new HashMap<>();
        body.put("userCode", user);
        Question<Map<String, Object>> msg = new Question<>();
        msg.setBody(body);

        HttpClient.getInstance(this).doPost("/customers", msg, CustomersAnswer.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CustomersAnswer event) {
        Log.i(Constants.TAG, "code = " + event.getCode());
        for(Customer customer : event.getBody()) {
            Log.i(Constants.TAG, customer.toString());
        }
        customers = event.getBody();
        ArrayAdapter<Customer> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, customers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCustomersView.setAdapter(adapter);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = cm.getActiveNetworkInfo();
        if(networkinfo != null){
            return true;
        }
        else {
            return false;
        }
    }


    @OnClick(R.id.btn_change_password)
    public void onChangePassword() {
        Intent intent = new Intent(this, PasswordActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_query_order)
    public void onQueryOrder() {
        Intent intent = new Intent(this, OrderQueryActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_in_order)
    public void onStockIn() {
        Intent intent = new Intent(this, ScanOrderActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_revoked_order)
    public void onRevokedOrder() {
        Intent intent = new Intent(this, ScanOrderActivity.class);
        intent.putExtra(Constants.KEY_ORDER_REVOKED_FLAG, true);
        startActivity(intent);
    }

    @OnClick(R.id.btn_out_goods)
    public void onStockOut() {
        Intent intent = new Intent(this, ScanPartActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_query_flow)
    public void onQueryFlow() {
        Intent intent = new Intent(this, FlowActivity.class);
        startActivity(intent);
    }

}
