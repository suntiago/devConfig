package viroyal.com.devconfig;

import android.app.Application;

import com.suntiago.baseui.utils.log.CrashHandler;
import com.suntiago.baseui.utils.log.Slog;

/**
 * Created by zy on 2018/12/6.
 */

public class DevApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Slog.init(this, "suntiago", "com.suntiago.demo");
        Slog.enableSaveLog(true);
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext(), "suntiago", "com.suntiago.demo");
    }
}
