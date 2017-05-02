package wang.laic.kanban;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
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
    @BindView(R.id.tv_login_info) TextView tvLoginInfo;

    List<Customer> customers;

    private long clickTime = 0; // 第一次点击的时间

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        preferences = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_APPEND);

        mCustomersView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Customer customer = customers.get(position);

                Toast.makeText(MainActivity.this, "你的选择是：" + customer.getName(), Toast.LENGTH_SHORT).show();

                KanbanApplication app = (KanbanApplication)getApplication();
                app.setParameter(Constants.KEY_CURRENT_CUSTOMER, customer);

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(Constants.PREFERENCE_CURRENT_CUSTOMER, customer.getCode());
                editor.commit();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        KanbanApplication app = (KanbanApplication)getApplication();
//        String user = (String)app.getParameter(Constants.KEY_CURRENT_USER);

        String user = preferences.getString(Constants.PREFERENCE_USER, "");
        String loginTime = preferences.getString(Constants.PREFERENCE_LOGIN_TIME, "");

        String loginInfoTemplate = getString(R.string.lab_login_info);
        String loginInfo = String.format(loginInfoTemplate, user, loginTime);
        tvLoginInfo.setText(loginInfo);

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
    public void onCustomersEvent(CustomersAnswer event) {
        if(event.getCode() == 0) {
            customers = event.getBody();
            ArrayAdapter<Customer> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, customers);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mCustomersView.setAdapter(adapter);

            String customerCode = preferences.getString(Constants.PREFERENCE_CURRENT_CUSTOMER, "");
            if(!customerCode.isEmpty()) {
                for(int i = 0; i < customers.size(); i++) {
                    Customer customer = customers.get(i);
                    if(customer.getCode().equalsIgnoreCase(customerCode)) {
                        mCustomersView.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            Log.i(Constants.TAG, "code = " + event.getCode());
            String errorMessage = event.getMessage();
            if(event.getCode() == 9999) {
                errorMessage = getString(R.string.error_sever_exception);
            }
            showMessage(errorMessage);
        }
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
        intent.putExtra(Constants.KEY_ORDER_FLAG, 1);
        startActivity(intent);
    }

    @OnClick(R.id.btn_revoked_order)
    public void onRevokedOrder() {
        Intent intent = new Intent(this, ScanOrderActivity.class);
        intent.putExtra(Constants.KEY_ORDER_FLAG, 2);
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

    @OnClick(R.id.ib_exit)
    public void onLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("是否退出当前用户?");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                SharedPreferences.Editor editor = preferences.edit();
//                editor.remove(Constants.PREFERENCE_USER);
                editor.remove(Constants.PREFERENCE_LOGIN_TIME);
                editor.putBoolean(Constants.PREFERENCE_LOGIN_STATUS, false);
                editor.commit();

                KanbanApplication app = (KanbanApplication)getApplication();
                app.removeParameter(Constants.KEY_CURRENT_USER);

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);

                finish();
            }
        });
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        } else { // 如果不是back键正常响应
            return super.onKeyDown(keyCode, event);
        }
    }

    private void exit() {
        if ((System.currentTimeMillis() - clickTime) > 2000) {
            Toast.makeText(this, "再按一次后退键退出程序", Toast.LENGTH_SHORT).show();
            clickTime = System.currentTimeMillis();
        } else {
            this.finish();
        }
    }

    private void showMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
    }

}
