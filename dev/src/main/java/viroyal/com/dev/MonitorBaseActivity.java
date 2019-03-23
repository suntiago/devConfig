package viroyal.com.dev;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import com.suntiago.baseui.activity.base.ActivityBase;
import com.suntiago.baseui.activity.base.AppDelegateBase;
import com.suntiago.baseui.activity.base.theMvp.model.IModel;
import com.suntiago.baseui.utils.date.DateUtils;
import com.suntiago.baseui.utils.file.StorageHelper;
import com.suntiago.baseui.utils.file.StorageManagerHelper;
import com.suntiago.baseui.utils.log.Slog;

import java.io.FileOutputStream;
import java.util.Date;

/**
 * Created by zy on 2019/1/24.
 */

public abstract class MonitorBaseActivity<T extends AppDelegateBase, D extends IModel> extends ActivityBase<T, D> {
  public static final String BROADCAST_ACTION = "com.viroyal.permission.capture_complete";
  public static final String REGISTER_BROADCAST_ACTION = "com.viroyal.permission.capture";
  public static final String CRASH_BROADCAST_ACTION = "com.viroyal.permission.crash";

  private String mCaptureId;
  MyBroadcastReceiver mMyBroadcastReceiver = new MyBroadcastReceiver();
  CrashBroadcastReceiver mCrashBroadcastReceiver = new CrashBroadcastReceiver();

  protected boolean activityIsResume = false;

  //截图
  private void capture() {
    viewSaveToImage(getRootView());
  }

  //截图并保存
  private void viewSaveToImage(View view) {
    view.setDrawingCacheEnabled(true);
    view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
    view.setDrawingCacheBackgroundColor(Color.WHITE);
    // 把一个View转换成图片
    Bitmap cachebmp = loadBitmapFromView(view);
    // 添加水印
    Bitmap bitmap = Bitmap.createBitmap(cachebmp);
    FileOutputStream fos;
    StorageHelper storageHelper = StorageManagerHelper.getStorageHelper();

    String fileName = "capture-" + DateUtils.format(new Date(), "yyyy-MM-dd-HH-mm-ss") + "-" + System.currentTimeMillis() + ".png";

    String filePath = storageHelper.getFilePath("capture", fileName);

    String ret = "";
    String msg = "";
    try {
      // 判断手机设备是否有SD卡
      boolean isHasSDCard = storageHelper.isSDCardEnable();
      if (isHasSDCard) {
        fos = new FileOutputStream(filePath);
      } else {
        msg = "创建截图文件失败!";
        throw new Exception("创建截图文件失败!");
      }
      bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
      fos.flush();
      fos.close();
    } catch (Exception e) {
      ret = "0";
      e.printStackTrace();
    }

    Slog.d(TAG, BROADCAST_ACTION);
    Intent intent = new Intent();
    intent.setAction(BROADCAST_ACTION);
    intent.putExtra("path", filePath);
    intent.putExtra("pkgName", getPackageName());
    intent.putExtra("ret", ret);
    intent.putExtra("msg", msg);
    intent.putExtra("id", mCaptureId);
    sendBroadcast(intent);
    view.destroyDrawingCache();
  }

  //获取根view
  private View getRootView() {
    return ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
  }

  private Bitmap loadBitmapFromView(View v) {
    int w = v.getWidth();
    int h = v.getHeight();
    Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    Canvas c = new Canvas(bmp);
    c.drawColor(Color.WHITE);
    /** 如果不设置canvas画布为白色，则生成透明 */
    v.layout(0, 0, w, h);
    v.draw(c);
    return bmp;
  }

  @Override
  protected void onResume() {
    super.onResume();
    activityIsResume = true;

    mMyBroadcastReceiver.register(this);
    mCrashBroadcastReceiver.register(this);
  }

  @Override
  protected void onPause() {
    mMyBroadcastReceiver.unRegister(this);
    mCrashBroadcastReceiver.unRegister(this);
    super.onPause();
    activityIsResume = false;
  }

  class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      Slog.d(TAG, REGISTER_BROADCAST_ACTION);
      mCaptureId = intent.getStringExtra("id");
      capture();
    }

    public void register(Context context) {
      IntentFilter filter = new IntentFilter();
      filter.addAction(REGISTER_BROADCAST_ACTION);
      registerReceiver(this, filter);
    }

    public void unRegister(Context context) {
      unregisterReceiver(this);
    }
  }

  class CrashBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      Slog.d(TAG, CRASH_BROADCAST_ACTION);
      String str = null;
      if (str.equals("1")) {

      }
    }

    public void register(Context context) {
      IntentFilter filter = new IntentFilter();
      filter.addAction(CRASH_BROADCAST_ACTION);
      registerReceiver(this, filter);

    }

    public void unRegister(Context context) {
      unregisterReceiver(this);
    }
  }
}
