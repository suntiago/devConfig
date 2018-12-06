package viroyal.com.devconfig;

import com.suntiago.baseui.utils.log.Slog;

import java.io.File;
import java.util.List;

import viroyal.com.dev.splash.SplashIpActivity;

/**
 * Created by zy on 2018/12/6.
 */

public class SplashIp extends SplashIpActivity {
    @Override
    protected void splashFinish() {
        List<File> logFiles = Slog.getFileSort();
        if (logFiles != null && logFiles.size() > 0) {
            for (File logFile : logFiles) {
                Slog.d(TAG, "splashFinish  []:" + logFile.getName());
            }
        }
    }
}
