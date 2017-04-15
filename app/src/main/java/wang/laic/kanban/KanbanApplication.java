package wang.laic.kanban;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by duduba on 2017/3/30.
 */

public class KanbanApplication extends Application {

    private Map<String, Object> cache = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);

        EventBus.builder()
                .throwSubscriberException(BuildConfig.DEBUG)
                .installDefaultEventBus();
    }

    public void setParameter(String key, Object value) {
        cache.put(key, value);
    }
    public Object getParameter(String key) {
        return cache.get(key);
    }
}
