package viroyal.com.dev.boot;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.suntiago.baseui.activity.base.AppDelegateBase;
import com.suntiago.baseui.activity.base.theMvp.model.IModel;
import com.suntiago.dblib.DBUpdateHelper;
import com.suntiago.network.network.Api;
import com.suntiago.network.network.BaseRspObserver;

import org.kymjs.kjframe.KJDB;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import viroyal.com.dev.NFCMonitorBaseActivity;
import viroyal.com.dev.boot.util.DateUtil;
import viroyal.com.dev.boot.util.LztekUtil;
import viroyal.com.dev.util.DeviceInfoUtil;


/**
 * @author chenjunwei
 * @desc 开关机操作
 * @date 2020-03-10
 */
public abstract class SplashBootActivity<T extends AppDelegateBase, D extends IModel> extends NFCMonitorBaseActivity<T, D> {
  private String todayEndDate;
  private String tomorrowStartDate;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    KJDB.create(this, "testdb", true, 1, new DBUpdateHelper());
    syncOneSecond();
  }

  /**
   * 获取开关机策略
   */
  protected void getBootConfig() {
    BootRequest bootRequest = new BootRequest();
    bootRequest.IMEI = DeviceInfoUtil.getInstance(SplashBootActivity.this).getDeviceUuid();
    Subscription bootSubscription = Api.get().getApi(BootConfig.class, getBootHostApi())
            .bootApi(bootRequest)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new BaseRspObserver<>(BootResponse.class, new Action1<BootResponse>() {
              @Override
              public void call(BootResponse rsp) {
                if (rsp.error_code == 1001 || rsp.error_code < 0) {
                  //请求失败 无网络 数据解析失败等读取本地缓存数据
                  chooseOffLineStrategy(rsp);
                } else {
                  chooseStrategy(rsp);
                }
                splashFinish();
              }
            }));
    addRxSubscription(bootSubscription);
  }

  /**
   * 选择离线模式
   *
   * @param rsp
   */
  private void chooseOffLineStrategy(BootResponse rsp) {
    BootModel bootModel = rsp.bootModel;
    switch (bootModel.device_type) {
      case 4:
        //大屏
        setOffLineStrategyTwo();
        break;
      case 15:
        //教师考勤
        setOffLineStrategyOne();
        break;
    }
  }

  /**
   * 选择在线模式
   *
   * @param rsp
   */
  private void chooseStrategy(BootResponse rsp) {
    BootModel bootModel = rsp.bootModel;
    switch (bootModel.device_type) {
      case 4:
        //大屏
        setStrategyTwo(rsp);
        break;
      case 15:
        //教师考勤
        setStrategyOne(rsp);
        break;
    }
  }

  /*-----------------------------------------策略一开始------------------------------------------------*/

  /**
   * 时间检测 定时器 60s一次
   */
  private void syncOneSecond() {
    Subscription syncOneSecond = Observable.timer(60, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<Long>() {
              @Override
              public void call(Long aLong) {
                if (TextUtils.isEmpty(todayEndDate)) {
                  syncOneSecond();
                  return;
                }
                if (System.currentTimeMillis() / 1000 + 5 * 60 >= DateUtil.getTimeStamp(todayEndDate)) {
                  syncOneMin();
                } else {
                  //时间未到
                  syncOneSecond();
                }
              }
            });
    addRxSubscription(syncOneSecond);
  }

  /**
   * 时间检测 定时器 1分钟一次
   */
  private void syncOneMin() {
    getBootConfig();
    Subscription syncOneMin = Observable.timer(60, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<Long>() {
              @Override
              public void call(Long aLong) {
                syncOneMin();
              }
            });
    addRxSubscription(syncOneMin);
  }

  /**
   * 离线设置策略1
   * 临时>常规
   */
  private void setOffLineStrategyOne() {
    setTodayOffLineStrategy();

    setTomorrowOffLineStrategy();
  }

  /**
   * 获取今日关机时间
   */
  private void setTodayOffLineStrategy() {
    //获取今天的结束时间
    String todayNextDate = DateUtil.getTodayDate() + " 00:00:00";
    String todayTempWhereStr = "type='2' and '" + todayNextDate + "' between start_date and end_date";
    String todayWhereStr = "type='1' and '" + todayNextDate + "' between start_date and end_date";
    List<Strategy> todayTempStrategyList = KJDB.getDefaultInstance().findAllByWhere(Strategy.class, todayTempWhereStr);
    List<Strategy> todayNormalStrategyList = KJDB.getDefaultInstance().findAllByWhere(Strategy.class, todayWhereStr);
    if (null != todayTempStrategyList && todayTempStrategyList.size() > 0) {
      Strategy strategy = todayTempStrategyList.get(0);
      todayEndDate = DateUtil.getTodayDate() + " " + strategy.end_date.split(" ")[1];
    } else if (null != todayNormalStrategyList && todayNormalStrategyList.size() > 0) {
      //常规策略
      Strategy strategy = todayNormalStrategyList.get(0);
      todayEndDate = DateUtil.getTodayDate() + " " + strategy.end_date.split(" ")[1];
    } else {
      todayEndDate = DateUtil.getTodayDate() + " 18:00:00";
    }
  }

  private void setTomorrowOffLineStrategy() {
    String nextDate = DateUtil.getNextDate() + " 00:00:00";
    String tempWhereStr = "type='2' and '" + nextDate + "' between start_date and end_date";
    String whereStr = "type='1' and '" + nextDate + "' between start_date and end_date";
    List<Strategy> tempStrategyList = KJDB.getDefaultInstance().findAllByWhere(Strategy.class, tempWhereStr);
    List<Strategy> normalStrategyList = KJDB.getDefaultInstance().findAllByWhere(Strategy.class, whereStr);
    if (null != tempStrategyList && tempStrategyList.size() > 0) {
      //临时策略
      setTomorrowOffLineStrategyOne(tempStrategyList);
      return;
    }
    if (null != normalStrategyList && normalStrategyList.size() > 0) {
      //常规策略
      setTomorrowOffLineStrategyOne(normalStrategyList);
      return;
    }
    //无匹配策略
    resetStrategy();
  }

  private void setStrategyOne(BootResponse rsp) {
    BootModel bootModel = rsp.bootModel;
    setTodayStrategy(bootModel);
    //成功
    Tomorrow tomorrow = bootModel.tomorrow;
    if (null != tomorrow) {
      setTomorrowStrategy(tomorrow);
    } else {
      //说明明天无策略
      resetStrategy();
    }
    //保存开机策略
    if (null != bootModel.strategy) {
      KJDB.getDefaultInstance().deleteByWhere(Strategy.class, "1==1");
      KJDB.getDefaultInstance().save(rsp.bootModel.strategy);
    }
  }

  private void setTodayStrategy(BootModel bootModel) {
    if (null != bootModel.today) {
      todayEndDate = DateUtil.getTodayDate() + " " + DateUtil.getTime(bootModel.today.today_off_hour, bootModel.today.today_off_minute);
    } else {
      //今天无策略 18点更新一次
      todayEndDate = DateUtil.getTodayDate() + " 18:00:00";
    }
  }

  private void setTomorrowOffLineStrategyOne(List<Strategy> strategyList) {
    if (strategyList.size() > 0) {
      Strategy strategy = strategyList.get(0);
      String on_hour = "0";
      String on_minute = "0";
      String off_hour = "0";
      String off_minute = "0";
      String week = DateUtil.getWeek(strategy.weeks);
      String[] onTime = getTime(strategy.on_time);
      if (null != onTime) {
        on_hour = TextUtils.equals(onTime[0].substring(0, 1), "0") ? onTime[0].substring(1, 2) : onTime[0].substring(0, 2);
        on_minute = TextUtils.equals(onTime[1].substring(0, 1), "0") ? onTime[1].substring(1, 2) : onTime[1].substring(0, 2);
      }
      String[] offTime = getTime(strategy.off_time);
      if (null != offTime) {
        off_hour = TextUtils.equals(offTime[0].substring(0, 1), "0") ? offTime[0].substring(1, 2) : offTime[0].substring(0, 2);
        off_minute = TextUtils.equals(offTime[1].substring(0, 1), "0") ? offTime[1].substring(1, 2) : offTime[1].substring(0, 2);
      }
      Tomorrow tomorrow = new Tomorrow();
      tomorrow.on_hour = DateUtil.toInt(on_hour);
      tomorrow.on_minute = DateUtil.toInt(on_minute);
      tomorrow.off_hour = DateUtil.toInt(off_hour);
      tomorrow.off_minute = DateUtil.toInt(off_minute);
      tomorrow.week = week;

      setTomorrowStrategy(tomorrow);
    }
  }

  /**
   * 拆分时间
   *
   * @param time
   * @return
   */
  private String[] getTime(String time) {
    try {
      return time.split(" ")[1].split(":");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 在线设置策略1
   *
   * @param tomorrow
   */
  private void setTomorrowStrategy(Tomorrow tomorrow) {
    //false - 关闭自动开机 true - 开启自动开机
    Intent autoBootIntent = new Intent("com.hra.setAutoBoot");
    autoBootIntent.putExtra("key", true);
    sendBroadcast(autoBootIntent);
    //开机时间
    Intent bootIntent = new Intent("com.hra.setBootTime");
    bootIntent.putExtra("hourOfDay", tomorrow.on_hour);
    bootIntent.putExtra("minute", tomorrow.on_minute);
    sendBroadcast(bootIntent);

    //false - 关闭自动关机 true - 开启自动关机
    Intent autoShutdownIntent = new Intent("com.hra.setAutoShutdown");
    autoShutdownIntent.putExtra("key", true);
    sendBroadcast(autoShutdownIntent);

    //关机时间
    Intent shutdownIntent = new Intent("com.hra.setShutdownTime");
    shutdownIntent.putExtra("hourOfDay", tomorrow.off_hour);
    shutdownIntent.putExtra("minute", tomorrow.off_minute);
    sendBroadcast(shutdownIntent);

    //星期周期
    Intent bootWeekIntent = new Intent("com.hra.setBootWeek");
    bootWeekIntent.putExtra("key", DateUtil.getWeek(tomorrow.week));
    sendBroadcast(bootWeekIntent);

    Intent shutdownWeekIntent = new Intent("com.hra.setShutdownWeek");
    shutdownWeekIntent.putExtra("key", DateUtil.getWeek(tomorrow.week));
    sendBroadcast(shutdownWeekIntent);
  }

  /**
   * 重置策略
   */
  private void resetStrategy() {
    //false - 关闭自动开机 true - 开启自动开机
    Intent autoBootIntent = new Intent("com.hra.setAutoBoot");
    autoBootIntent.putExtra("key", false);
    sendBroadcast(autoBootIntent);

    //false - 关闭自动关机 true - 开启自动关机
    Intent autoShutdownIntent = new Intent("com.hra.setAutoShutdown");
    autoShutdownIntent.putExtra("key", false);
    sendBroadcast(autoShutdownIntent);

    Intent bootWeekIntent = new Intent("com.hra.setBootWeek");
    bootWeekIntent.putExtra("key", "0000000");
    sendBroadcast(bootWeekIntent);

    Intent intent = new Intent("com.hra.setShutdownWeek");
    intent.putExtra("key", "0000000");
    sendBroadcast(intent);
  }


  /*-----------------------------------------策略一结束------------------------------------------------*/



  /*-----------------------------------------策略二开始------------------------------------------------*/

  private void setStrategyTwo(BootResponse rsp) {
    BootModel bootModel = rsp.bootModel;
    setTodayStrategy(bootModel);
    setTomorrowStrategyTwo(bootModel);
  }

  private void setTomorrowStrategyTwo(BootModel bootModel) {
    //成功
    Tomorrow tomorrow = bootModel.tomorrow;
    if (null != tomorrow) {
      tomorrowStartDate = DateUtil.getNextDate() + " " + DateUtil.getTime(tomorrow.on_hour, tomorrow.on_minute);
    } else {
      tomorrowStartDate = "";
    }
    //保存开机策略
    if (null != bootModel.strategy) {
      KJDB.getDefaultInstance().deleteByWhere(Strategy.class, "1==1");
      KJDB.getDefaultInstance().save(bootModel.strategy);
    }

    //开启定时任务
    syncOneSecondTwo();
  }

  private void setOffLineStrategyTwo() {
    setTodayOffLineStrategy();
    setTomorrowOffLineStrategyTwo();
  }

  private void setTomorrowOffLineStrategyTwo() {
    String nextDate = DateUtil.getNextDate() + " 00:00:00";
    String tempWhereStr = "type='2' and '" + nextDate + "' between start_date and end_date";
    String whereStr = "type='1' and '" + nextDate + "' between start_date and end_date";
    List<Strategy> tempStrategyList = KJDB.getDefaultInstance().findAllByWhere(Strategy.class, tempWhereStr);
    List<Strategy> normalStrategyList = KJDB.getDefaultInstance().findAllByWhere(Strategy.class, whereStr);
    if (null != tempStrategyList && tempStrategyList.size() > 0) {
      Strategy strategy = tempStrategyList.get(0);
      tomorrowStartDate = DateUtil.getNextDate() + " " + strategy.end_date.split(" ")[1];
    } else if (null != normalStrategyList && normalStrategyList.size() > 0) {
      //常规策略
      Strategy strategy = normalStrategyList.get(0);
      todayEndDate = DateUtil.getTodayDate() + " " + strategy.end_date.split(" ")[1];
    } else {
      //无匹配策略
      tomorrowStartDate = "";
    }

    //开启定时任务
    syncOneSecondTwo();
  }

  /**
   * 时间检测 定时器 1s一次
   */
  private void syncOneSecondTwo() {
    Subscription syncOneSecondTwo = Observable.timer(1, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<Long>() {
              @Override
              public void call(Long aLong) {
                if (TextUtils.isEmpty(todayEndDate)) {
                  syncOneSecondTwo();
                  return;
                }
                long todayEndDateTimeStamp = DateUtil.getTimeStamp(todayEndDate);
                if (System.currentTimeMillis() / 1000 >= todayEndDateTimeStamp) {
                  if (TextUtils.isEmpty(tomorrowStartDate)) {
                    //明天无策略 直接关机
                    LztekUtil.getInstance().hardShutdown(SplashBootActivity.this);
                  } else {
                    long tomorrowStartDateTimeStamp = DateUtil.getTimeStamp(tomorrowStartDate);
                    LztekUtil.getInstance().alarmPoweron((int) (tomorrowStartDateTimeStamp - todayEndDateTimeStamp), SplashBootActivity.this);
                  }
                } else {
                  //时间未到
                  syncOneSecondTwo();
                }
              }
            });
    addRxSubscription(syncOneSecondTwo);
  }

  /*-----------------------------------------策略二结束------------------------------------------------*/

  /**
   * 加载完成的后续处理
   */
  protected abstract void splashFinish();

  /**
   * 获取请求主接口的api
   */
  protected abstract String getBootHostApi();
}
