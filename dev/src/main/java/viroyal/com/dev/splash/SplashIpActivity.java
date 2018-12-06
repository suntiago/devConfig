package viroyal.com.dev.splash;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.suntiago.baseui.utils.log.Slog;
import com.suntiago.getpermission.rxpermissions.RxPermissions;
import com.suntiago.network.network.rsp.BaseResponse;
import com.suntiago.network.network.utils.SPUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;
import viroyal.com.dev.MonitorActivity;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Created by Jeremy on 2018/8/2.
 */

public abstract class SplashIpActivity extends MonitorActivity {

    //正在加载弹出框
    private AlertDialog alertDialog;
    int mAPITime = 1000 * 10;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigDevice.school_id = SPUtils.getInstance(this).get("school_id","");
        ConfigDevice.operator = SPUtils.getInstance(this).get("operator","");
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("配置加载")
                .setCancelable(false)
                .setMessage("正在加载配置信息...")
                .create();
        alertDialog.show();
        RxPermissions.getInstance(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean granted) {
                        if (granted) {
                            getMacApi(SplashIpActivity.this);
                        } else {
                            Slog.d(TAG, "uploadFile  [id, filepath, pkgName, action]:");
                        }
                    }
                });
    }

    public void getMacApi(final Context context) {
        if (TextUtils.isEmpty(ConfigDevice.getDeviceId())) {
            Slog.d(TAG, "getMacApi [context]:");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Slog.d(TAG, "run []:");
                    alertDialog.setMessage("Mac地址获取失败，请手动写入！");
                }
            });
            Observable.timer(mAPITime, TimeUnit.MILLISECONDS).subscribe(new Action1<Long>() {
                @Override
                public void call(Long aLong) {
                    Slog.d(TAG, "Observable.timer:regetMac");
                    getMacApi(context);
                }
            });
        } else {
            getApi(SplashIpActivity.this);
        }
    }

    public void getApi(final Context context) {
        Slog.d(TAG, "getApi  [context]:");
        ConfigDevice.configIp(context, "", new Action1<BaseResponse>() {
            @Override
            public void call(final BaseResponse r) {
                if (r.error_code == 1000) {
                    handleSplash();
                } else {
                    if (TextUtils.isEmpty(SPUtils.getInstance(context).get("api_config"))) {
                        Slog.d(TAG, "getApi [context]:");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Slog.d(TAG, "run []:");
                                alertDialog.setMessage(r.error_msg + ",mac:" + ConfigDevice.getDeviceId(context));
                            }
                        });
                        Observable.timer(mAPITime, TimeUnit.MILLISECONDS).subscribe(new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                Slog.d(TAG, "Observable.timer:regetApi");
                                getApi(context);
                            }
                        });
                    } else {
                        handleSplash();
                    }
                }
            }
        });
    }

    public void handleSplash() {
        String path = Environment.getExternalStorageDirectory() + File.separator + "viroyal_mac.txt";
        File file = new File(path);
        if (!file.exists()) {
            try {
                FileWriter filerWriter = new FileWriter(file, false);//后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
                BufferedWriter bufWriter = new BufferedWriter(filerWriter);
                bufWriter.write(ConfigDevice.getDeviceId());
                bufWriter.close();
                filerWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        alertDialog.dismiss();
        splashFinish();
    }

    protected abstract void splashFinish();

}
