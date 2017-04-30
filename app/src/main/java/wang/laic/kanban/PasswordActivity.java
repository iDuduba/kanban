package wang.laic.kanban;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wang.laic.kanban.network.HttpClient;
import wang.laic.kanban.network.message.Failure;
import wang.laic.kanban.network.message.PasswordAnswer;
import wang.laic.kanban.network.message.Question;
import wang.laic.kanban.utils.KukuUtil;

public class PasswordActivity extends BaseActivity {

    @BindView(R.id.old_password) EditText oldPasswordView;
    @BindView(R.id.new_password) EditText newPasswordView;
    @BindView(R.id.retry_password) EditText retryPasswordView;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        setToolbarTitle(getString(R.string.pwd_change_password));

        preferences = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_APPEND);
    }

    @OnClick(R.id.confir_password)
    public void onConfirmPassword() {
        String oldPassword = oldPasswordView.getText().toString();
        String newPassword = newPasswordView.getText().toString();
        String retryPassword = retryPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if(!isPasswordValid(newPassword)) {
            newPasswordView.setError(getString(R.string.pwd_err_invalid_password));
            focusView = newPasswordView;
            cancel = true;
        }
        if(newPassword.compareTo(retryPassword) != 0) {
            retryPasswordView.setError(getString(R.string.pwd_err_invalid_retry_password));
            focusView = retryPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            showProgress(true);
            mProgress.show();
            okHttp_change_password(getCurrentUser(), oldPassword, newPassword);
        }

    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 6;
    }

    private void okHttp_change_password(String user, String oldPassword, String newPassword) {
        Map<String, Object> body = new HashMap<>();
        body.put("userCode", user);
        body.put("oldPassword", KukuUtil.md5Digest(oldPassword));
        body.put("newPassword", KukuUtil.md5Digest(newPassword));
        Question<Map<String, Object>> msg = new Question<>();
        msg.setBody(body);

        HttpClient.getInstance(this).doPost("/password", msg, PasswordAnswer.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPasswordEvent(PasswordAnswer event) {
        mProgress.dismiss();
        if(event.getCode() == 0) {
            showMessage("重置密码成功");

            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(Constants.PREFERENCE_LOGIN_TIME);
            editor.putBoolean(Constants.PREFERENCE_LOGIN_STATUS, false);
            editor.commit();

            KanbanApplication app = (KanbanApplication)getApplication();
            app.removeParameter(Constants.KEY_CURRENT_USER);

            Intent intent = new Intent(PasswordActivity.this, LoginActivity.class);
            startActivity(intent);

            finish();
        } else {
            String errorMessage = event.getMessage();
            if(event.getCode() == 9999) {
                errorMessage = getString(R.string.error_sever_exception);
            }
            showMessage(errorMessage);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFailureEvent(Failure failure) {
        if(failure.getBody().compareTo("/password") == 0) {
            mProgress.dismiss();
            showMessage(failure.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
//            case R.id.add:
//                Toast.makeText(PasswordActivity.this , "haha" , Toast.LENGTH_SHORT).show();
//                return(true);
//            case R.id.reset:
//                //add the function to perform here
//                return(true);
            case R.id.about:
                //add the function to perform here
                return(true);
//            case R.id.exit:
//                //add the function to perform here
//                return(true);
        }
        return super.onOptionsItemSelected(item);
    }


}
