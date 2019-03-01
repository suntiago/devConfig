package viroyal.com.dev;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.suntiago.baseui.utils.log.Slog;

import java.io.File;
import java.util.List;

public class CommandBroadcastReceiver extends BroadcastReceiver {
    protected final String TAG = getClass().getSimpleName();
    //使用时需要在AndroidManifest中声明
    public static final String GET_LOG_BROADCAST_ACTION = "com.viroyal.permission.getlog";
    public static final String LOG_BROADCAST_ACTION = "com.viroyal.permission.sendlog";
    public static final String RESTART_BROADCAST_ACTION = "com.viroyal.permission.restart";
    public static final String RESTART_COMPLETE_ACTION = "com.viroyal.permission.restart_complete";
    private String mLogId;
    private String mRestartId;

    @Override
    public void onReceive(Context context, Intent intent) {
        Slog.d(TAG, "LogBroadcastReceiver:pckName:" + context.getPackageName());
        if (context.getPackageName().equals(intent.getStringExtra("pkgName"))) {
            if (intent.getAction().equals(GET_LOG_BROADCAST_ACTION)) {
                Slog.d(TAG, GET_LOG_BROADCAST_ACTION);
                mLogId = intent.getStringExtra("id");
                updatelog(context);
            }
            if (intent.getAction().equals(RESTART_BROADCAST_ACTION)) {
                Slog.d(TAG, RESTART_BROADCAST_ACTION);
                mRestartId = intent.getStringExtra("id");
                restartApp(context);
            }
        }
    }

    private void updatelog(Context context) {
        Slog.d(TAG, "updatelog:path:"
                + " id:" + mLogId + " pkgName:" + context.getPackageName());
        List<File> logFiles = Slog.getFileSort();
        if (logFiles != null && logFiles.size() > 0) {
            Intent intent = new Intent();
            intent.setAction(LOG_BROADCAST_ACTION);
            intent.putExtra("path", logFiles.get(0).getAbsolutePath());
            intent.putExtra("pkgName", context.getPackageName());
            intent.putExtra("id", mLogId);
            context.sendBroadcast(intent);
        }
    }

    private void restartApp(Context context) {
        Slog.d(TAG, "restartApp:pkgName:" + context.getPackageName() + " status:" + String.valueOf(2) + " id:" + mRestartId);
        Intent intent = new Intent();
        intent.setAction(RESTART_COMPLETE_ACTION);
        intent.putExtra("pkgName", context.getPackageName());
        intent.putExtra("status", String.valueOf(1));
        intent.putExtra("id", mRestartId);
        context.sendBroadcast(intent);
        Intent i = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }
}

