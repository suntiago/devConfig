package viroyal.com.devconfig.splash;

import com.suntiago.baseui.activity.base.theMvp.databind.DataBinder;
import com.suntiago.baseui.utils.log.Slog;

import java.io.File;
import java.util.List;

import viroyal.com.dev.splash.SplashIpActivity;

/**
 * Created by zy on 2018/12/6.
 */

public class SplashIp extends SplashIpActivity<SplashAppDelegate, SplashModel> {
  @Override
  protected void splashFinish() {
    List<File> logFiles = Slog.getFileSort();
    if (logFiles != null && logFiles.size() > 0) {
      for (File logFile : logFiles) {
        Slog.d(TAG, "splashFinish  []:" + logFile.getName());
      }
    }
  }

  @Override
  protected String getHostApi() {
    return "https://mcpapi.iyuyun.net:18443/";
  }

  @Override
  protected String getDeviceType() {
    return "default";
  }

  @Override
  public DataBinder getDataBinder() {
    return super.getDataBinder();
  }

  @Override
  protected Class<SplashAppDelegate> getDelegateClass() {
    return SplashAppDelegate.class;
  }

  @Override
  protected Class<SplashModel> getModelClass() {
    return SplashModel.class;
  }
}
