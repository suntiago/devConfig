package viroyal.com.dev.boot.util;

import android.content.Context;

import com.lztek.toolkit.Lztek;

/**
 * 大屏开关机方法
 */
public class LztekUtil {
  private volatile static LztekUtil instance = null;

  public static LztekUtil getInstance() {
    if (instance == null) {
      synchronized (LztekUtil.class) {
        if (instance == null) {
          instance = new LztekUtil();
        }
      }

    }
    return instance;
  }

  /**
   * 基于开关机电路的硬件关机操作。关机后只能通过遥控器、重新插拔电源、开机按键等
   * 方式开机
   */
  public void hardShutdown(Context context) {
    Lztek lztek = Lztek.create(context);
    lztek.hardShutdown();
  }

  /**
   * 此接口调用后会立即关机并在指定的时间秒数后自动开机。参数秒数最小为 60 秒即 1 分
   * 钟以后自动开机。比如现在是 18:00，希望主板现在关机并于第二天 08:00 开机，则参数应该设置为
   * 50400
   *
   * @param onSeconds
   */
  public void alarmPoweron(int onSeconds, Context context) {
    Lztek lztek = Lztek.create(context);
    lztek.alarmPoweron(onSeconds);
  }
}
