package wang.laic.kanban;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import wang.laic.kanban.models.Customer;
import wang.laic.kanban.views.WebCallProgressDialog;


/**
 * Created by duduba on 2017/4/5.
 */

public class BaseActivity extends AppCompatActivity {
    private LinearLayout rootLayout;
    private Toolbar toolbar;
    private TextView toolbarTitle;

    protected WebCallProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 这句很关键，注意是调用父类的方法
        super.setContentView(R.layout.activity_base);
        // 经测试在代码里直接声明透明状态栏更有效
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        initToolbar();

        mProgress = new WebCallProgressDialog(this, R.style.WebCallProgressDialog);
    }

    private void initToolbar() {
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("");
            setSupportActionBar(toolbar);

            // Get a support ActionBar corresponding to this toolbar
            ActionBar actionBar = getSupportActionBar();
            // Enable the Up button
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void setContentView(int layoutId) {
        setContentView(View.inflate(this, layoutId, null));
    }

    @Override
    public void setContentView(View view) {
        rootLayout = (LinearLayout) findViewById(R.id.root_layout);
        if (rootLayout == null) return;
        rootLayout.addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        initToolbar();
    }

    protected void setToolbarTitle(String title) {
        toolbarTitle.setText(title);
    }
    protected String getCurrentUser() {
        KanbanApplication app = (KanbanApplication)getApplication();
        return (String)app.getParameter(Constants.KEY_CURRENT_USER);
    }
    protected Customer getCurrentCustomer() {
        KanbanApplication app = (KanbanApplication)getApplication();
        return (Customer) app.getParameter(Constants.KEY_CURRENT_CUSTOMER);
    }

    protected void showMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
//        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }

    protected void setAppParameter(String key, Object value) {
        KanbanApplication app = (KanbanApplication)getApplication();
        app.setParameter(key, value);
    }
    protected void removeAppParameter(String key) {
        KanbanApplication app = (KanbanApplication)getApplication();
        app.removeParameter(key);
    }
    protected Object getAppParameter(String key) {
        KanbanApplication app = (KanbanApplication)getApplication();
        return app.getParameter(key);
    }
}
