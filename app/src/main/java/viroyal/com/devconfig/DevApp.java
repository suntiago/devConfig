package viroyal.com.devconfig;

import android.app.Application;

import com.suntiago.baseui.utils.file.StorageManagerHelper;
import com.suntiago.baseui.utils.log.CrashHandler;
import com.suntiago.baseui.utils.log.Slog;
import com.suntiago.dblib.DBUpdateHelper;

import org.kymjs.kjframe.KJDB;

/**
 * Created by zy on 2018/12/6.
 */

public class DevApp extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    Slog.setDebug(true, true);
    Slog.enableSaveLog(true);
    Slog.init(this);
    StorageManagerHelper.getStorageHelper().initPath("suntiago", "devconfig");
    KJDB.create(this,  "dev_db", true, 1, new DBUpdateHelper());
    CrashHandler crashHandler = CrashHandler.getInstance();
    crashHandler.init(getApplicationContext());
  }
}
