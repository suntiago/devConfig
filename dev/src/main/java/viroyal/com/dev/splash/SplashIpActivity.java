package viroyal.com.dev.splash;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.suntiago.baseui.activity.base.AppDelegateBase;
import com.suntiago.baseui.activity.base.theMvp.model.IModel;
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
import viroyal.com.dev.NFCMonitorBaseActivity;

/**
 * Created by Jeremy on 2018/8/2.
 */

public abstract class SplashIpActivity<T extends AppDelegateBase, D extends IModel> extends NFCMonitorBaseActivity<T, D> {

  //正在加载弹出框
  private AlertDialog alertDialog;
  int mAPITime = 1000 * 10;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ConfigDevice.school_id = SPUtils.getInstance(this).get("school_id", "");
    ConfigDevice.operator = SPUtils.getInstance(this).get("operator", "");
    alertDialog = new AlertDialog.Builder(this)
        .setTitle("配置加载")
        .setCancelable(false)
        .setMessage("正在加载配置信息...")
        .setPositiveButton(" ", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Slog.d(TAG, "onClick  [dialog, which]:" + which);
            openDemoMode();
            handleSplash();
          }
        })
        .create();
    alertDialog.show();
    RxPermissions.getInstance(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE)
        .subscribe(new Action1<Boolean>() {
          @Override
          public void call(Boolean granted) {
            if (granted) {
              getMacApi(SplashIpActivity.this);
            } else {
              Slog.d(TAG, "call  [granted]:" + granted);
            }
          }
        });
  }

  //开启演示模式
  @CallSuper
  public boolean openDemoMode() {
    Slog.d(TAG, "openDemoMode  []:");
    ConfigDevice.DEMO_MODE = true;
    return false;
  }

  public void getMacApi(final Context context) {
    if (TextUtils.isEmpty(ConfigDevice.getDeviceId())) {
      Slog.d(TAG, "getMacApi [context]:");
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Slog.d(TAG, "run []:");
          alertDialog.getButton(-1).setText("开启演示模式");
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
    ConfigDevice.configIp(getHostApi(), context, getDeviceType(), new Action1<BaseResponse>() {
      @Override
      public void call(final BaseResponse r) {
        if (r.error_code == 1000) {
          handleSplash();
        } else {
          alertDialog.getButton(-1).setText("开启演示模式");
          if (TextUtils.isEmpty(SPUtils.getInstance(context).get("api_config"))) {
            Slog.d(TAG, "getApi [context]:");
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Slog.d(TAG, "run []:");
                alertDialog.setMessage("" + r.error_code + ":" + r.error_msg + "\nmac:" + ConfigDevice.getDeviceId(context));
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
    if (!ConfigDevice.DEMO_MODE) {
      String path = Environment.getExternalStorageDirectory() + File.separator + "viroyal_mac.txt";
      File file = new File(path);
      if (!file.exists()) {
        try {
          //后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
          FileWriter filerWriter = new FileWriter(file, false);
          BufferedWriter bufWriter = new BufferedWriter(filerWriter);
          bufWriter.write(ConfigDevice.getDeviceId());
          bufWriter.close();
          filerWriter.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    alertDialog.dismiss();
    splashFinish();
  }

  /**
   * 加载完成的后续处理
   */
  protected abstract void splashFinish();

  /**
   * 获取请求主接口的api
   */
  protected abstract String getHostApi();

  /**
   * 获取APP类型字段， 默认default， 监控：JianKong
   */
  protected abstract String getDeviceType();

}
