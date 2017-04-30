package wang.laic.kanban;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import wang.laic.kanban.network.HttpClient;
import wang.laic.kanban.network.message.LoginAnswer;
import wang.laic.kanban.network.message.Question;
import wang.laic.kanban.network.message.RegisterAnswer;
import wang.laic.kanban.utils.KukuUtil;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    static final String TAG = LoginActivity.class.getSimpleName();

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int REQUEST_READ_PHONE_STATE = 1;

    private String deviceId = "00000000";

    // UI references.
    @BindView(R.id.user) AutoCompleteTextView mUserView;
    @BindView(R.id.password) EditText mPasswordView;
    @BindView(R.id.sign_in_button) Button mSignInButton;
    @BindView(R.id.login_progress) View mProgressView;
    @BindView(R.id.login_form) View mLoginFormView;
    @BindView(R.id.sign_up) TextView mSignUpView;


    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        preferences = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_APPEND);
        String user = preferences.getString(Constants.PREFERENCE_USER, "");

        mUserView.setText(user);

        if(mayRequestPhoneState() == true) {
            TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = tm.getDeviceId().toString();
        }

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mSignUpView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpDevice();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private boolean mayRequestPhoneState() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        requestPermissions(new String[]{READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);

        return false;
    }
        /**
         * Callback received when a permissions request has been completed.
         */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch(requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                    deviceId = tm.getDeviceId().toString();
                    Log.i(TAG, "Device ID = " + deviceId);
                } else {
                    // permission deniedepends on this permission.
                    Log.i(TAG, "wuwu...");
                }
                break;
            default:
                break;
        }
    }

    private void signUpDevice() {
        Log.i(TAG, "sing up device...");

        mUserView.setError(null);

        String user = mUserView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(user)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            okHttp_register(deviceId, user);
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String user = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.pwd_err_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid user name.
        if (TextUtils.isEmpty(user)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            okHttp_signin(deviceId, user, password);
        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 6;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void okHttp_signin(String imei, String user, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("userCode", user);
        body.put("IMEI", imei);
        body.put("password", KukuUtil.md5Digest(password));
        Question<Map<String, Object>> msg = new Question<>();
        msg.setBody(body);

        HttpClient.getInstance(this).doPost("/login", msg, LoginAnswer.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(LoginAnswer event) {
        Log.i(TAG, "code = " + event.getCode());
        showProgress(false);

        if(event.getCode() == 0) {
//            Log.i(TAG, "session = " + event.getBody().get("session"));

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.PREFERENCE_USER, mUserView.getText().toString());
            editor.putString(Constants.PREFERENCE_LOGIN_TIME, DateTime.now().toString("HH:mm"));
            editor.putBoolean(Constants.PREFERENCE_LOGIN_STATUS, true);
            editor.commit();

            KanbanApplication app = (KanbanApplication)getApplication();
            app.setParameter(Constants.KEY_CURRENT_USER, mUserView.getText().toString());
//            app.setParameter(Constants.KEY_SESSION, event.getBody().get("session"));

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.i(Constants.TAG, "code = " + event.getCode());
            String errorMessage = event.getMessage();
            if(event.getCode() == 9999) {
                errorMessage = getString(R.string.error_sever_exception);
            }
            showMessage(errorMessage);
        }
    }

    private void okHttp_register(String imei, String user) {
        Map<String, Object> body = new HashMap<>();
        body.put("IMEI", imei);
        body.put("userCode", user);
        Question<Map<String, Object>> msg = new Question<>();
        msg.setBody(body);

        HttpClient.getInstance(this).doPost("/register", msg, RegisterAnswer.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRegisterEvent(RegisterAnswer event) {
        Log.i(TAG, "code = " + event.getCode());
        showProgress(false);

        if(event.getCode() == 0) {
            showMessage(getString(R.string.device_register_success));
            mPasswordView.requestFocus();
        } else {
            Log.i(Constants.TAG, "code = " + event.getCode());
            String errorMessage = event.getMessage();
            if(event.getCode() == 9999) {
                errorMessage = getString(R.string.error_sever_exception);
            }
            showMessage(errorMessage);
        }
    }

    @OnClick(R.id.cancel_sign_in)
    public void onCancelSignIn() {
        finish();
    }

    private void showMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
//        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}

