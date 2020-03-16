package viroyal.com.dev.boot.util;

import android.text.TextUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtil {

  /**
   * 获取星期规律
   *
   * @param week
   * @return
   */
  public static String getWeek(String week) {
    StringBuilder stringBuilder = new StringBuilder();
    if (TextUtils.isEmpty(week)) {
      stringBuilder.append("0000000");
      return stringBuilder.toString();
    }
    if (week.contains("7")) {
      stringBuilder.append("1");
    } else {
      stringBuilder.append("0");
    }
    for (int i = 1; i < 7; i++) {
      if (week.contains("" + i)) {
        stringBuilder.append("1");
      } else {
        stringBuilder.append("0");
      }
    }
    return stringBuilder.toString();
  }

  public static int toInt(String time) {
    try {
      return Integer.parseInt(time);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }

  /**
   * 获取今天
   *
   * @return String
   */
  public static String getTodayDate() {
    return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
  }

  public static String getTime(int hour, int minute) {
    StringBuilder stringBuilder = new StringBuilder();
    if (hour >= 10) {
      stringBuilder.append(hour + "");
    } else {
      stringBuilder.append("0" + hour);
    }
    stringBuilder.append(":");
    if (minute >= 10) {
      stringBuilder.append(minute + "");
    } else {
      stringBuilder.append("0" + minute);
    }
    stringBuilder.append(":00");
    return stringBuilder.toString();
  }

  public static long getTimeStamp(String time) {
    try {
      DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      Date date = df.parse(time);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      return calendar.getTimeInMillis()/1000;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }

  /**
   * 获取明天的日期时间
   *
   * @return
   */
  public static String getNextDate() {
    Date date = new Date();
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    //把日期往后增加一天.整数往后推,负数往前移动
    calendar.add(calendar.DATE, 1);
    //这个时间就是日期往后推一天的结果
    date = calendar.getTime();

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    return formatter.format(date);
  }
}
