package wang.laic.kanban.network;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import wang.laic.kanban.Constants;
import wang.laic.kanban.R;
import wang.laic.kanban.network.message.Failure;
import wang.laic.kanban.network.message.Question;

/**
 * Created by duduba on 2017/3/31.
 */

public class HttpClient {

    private volatile static HttpClient mInstance;

    private OkHttpClient client;
    private Gson gson;

    private Context mContext;
    private String apiBaseUrl;
    private String apiAppId;
    private String apiSecretKey;

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/plain; charset=utf-8");
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");


    private HttpClient(Context context) {
        super();

        OkHttpClient.Builder clientBuilder = new OkHttpClient().newBuilder();
        clientBuilder.connectTimeout(10, TimeUnit.SECONDS);
        clientBuilder.readTimeout(15, TimeUnit.SECONDS);
        clientBuilder.writeTimeout(30, TimeUnit.SECONDS);
        client = clientBuilder.build();

        gson = new GsonBuilder()
                .serializeNulls()
                .excludeFieldsWithoutExposeAnnotation()
                .setDateFormat("yyyy-MM-dd")
                .setPrettyPrinting()
                .create();

        this.mContext = context.getApplicationContext();

        Resources resources = mContext.getResources();
        apiBaseUrl = resources.getString(R.string.api_base_url);
        apiAppId = resources.getString(R.string.api_app_id);
        apiSecretKey = resources.getString(R.string.api_secret_key);

//        mHandler = new Handler(mContext.getMainLooper());
    }

    public static HttpClient getInstance(Context context) {
        HttpClient temp = mInstance;
        if (temp == null) {
            synchronized (HttpClient.class) {
                temp = mInstance;
                if (temp == null) {
                    temp = new HttpClient(context);
                    mInstance = temp;
                }
            }
        }
        return temp;
    }

    /**
     * 设置请求头
     *
     * @param headersParams
     * @return
     */
    private Headers setHeaders(Map<String, String> headersParams) {
        Headers.Builder headersBuilder = new Headers.Builder();
        if (headersParams != null) {
            for(String key : headersParams.keySet()) {
                headersBuilder.add(key, headersParams.get(key));
            }
        }
        return headersBuilder.build();
    }


    /**
     * 实现post请求,发送JSON数据
     *
     * @param reqUrl
     * @param question
     */
    public void doPost(final String reqUrl, Question question, final Class<?> answer) {

        String decodedBody = null;
        try {
            decodedBody = URLDecoder.decode(gson.toJson(question.getBody()), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder x = new StringBuilder();
        x.append(apiAppId).append(decodedBody).append(apiSecretKey);

        String signString = passwordDigest(x.toString());
        question.setSign(signString);
        question.setAppId(apiAppId);

        String jsonBody = gson.toJson(question);
        Log.i(Constants.TAG, jsonBody);

        RequestBody body = RequestBody.create(MEDIA_TYPE_TEXT, jsonBody);

        Request request = new Request.Builder()
                .url(apiBaseUrl + reqUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(Constants.TAG, e.getLocalizedMessage());
                Failure failure = new Failure();
                failure.setMessage(e.getLocalizedMessage());
                failure.setBody(reqUrl);
                EventBus.getDefault().post(failure);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()) {
                    EventBus.getDefault().post(gson.fromJson(response.body().charStream(), answer));
                } else {
                    Log.e(Constants.TAG, "Unexpected code " + response);
                    Failure failure = new Failure();
                    failure.setBody(reqUrl);
                    failure.setMessage("Unexpected code " + response);
                    EventBus.getDefault().post(failure);
                }
            }
        });
    }

    public String passwordDigest(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] digest = md.digest();
            BigInteger number = new BigInteger(1, digest);
            String hashText = number.toString(16);
            while (hashText.length() < 32) {
                hashText = "0" + hashText;
            }
            return hashText;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
