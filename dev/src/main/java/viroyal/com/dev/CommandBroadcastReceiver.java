package viroyal.com.dev;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.storage.StorageManager;

import com.suntiago.baseui.BuildConfig;
import com.suntiago.baseui.utils.log.Slog;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    private String sPath;
    private static String LOG_PATH = "/viroyal/" + BuildConfig.APPLICATION_ID + "/";// 日志文件在sdcard中的路径
    private static String LOG_FILE_FIRST_NAME = BuildConfig.APPLICATION_ID + "_";
    private final static String LOG_FILE_END_NAME = "_log.txt";

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

    public String getSavePath(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            sPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            if (context != null) {
                StorageList storageList = new StorageList(context);
                sPath = storageList.getVolumePaths()[1];
            }
        }
        return sPath + LOG_PATH;
    }

    public static class StorageList {
        private Context mContext;
        private StorageManager mStorageManager;
        private Method mMethodGetPaths;

        public StorageList(Context context) {
            mContext = context;
            if (mContext != null) {
                mStorageManager = (StorageManager) mContext
                        .getSystemService(Activity.STORAGE_SERVICE);
                try {
                    mMethodGetPaths = mStorageManager.getClass()
                            .getMethod("getVolumePaths");
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }

        public String[] getVolumePaths() {
            String[] paths = null;
            try {
                paths = (String[]) mMethodGetPaths.invoke(mStorageManager);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return paths;
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

