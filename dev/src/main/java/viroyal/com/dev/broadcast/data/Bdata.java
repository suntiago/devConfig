package viroyal.com.dev.broadcast.data;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import org.kymjs.kjframe.database.annotate.Property;

import viroyal.com.dev.broadcast.BroadcastData;

/**
 * Created by Zaiyu on 2019/4/24.
 */
public class Bdata {

  public String id;

  public int duration = 15;

  //image or media
  public String type;

  //播放类型分组
  public int group;

  //目标url, 可能是视频地址，图片地址，H5连接地址
  public String des_url;

  //置顶播放当前的数据
  public int top = 0;

  public boolean isMieda() {
    if (isImage() || isTextImage() || isVideo()) {
      return true;
    }
    return false;
  }

  public boolean isImage() {
    return "image".equals(type);
  }

  public boolean isVideo() {
    return "video".equals(type);
  }

  public boolean isTextImage() {
    return "image-text".equals(type);
  }

  public boolean isText() {
    return "text".equals(type);
  }

  public boolean isHtml() {
    return "html".equals(type);
  }

  //获取本地缓存的媒体资源
  public Bmedia getLocalMedia() {
    return null;
  }

  @SerializedName("text")
  @Property(column = "title")
  public String title = "";


  public String tvtitle;
  public String tvcontent;
  public String tvtitleSize;
  public String tvtitleColor;
  public String tvcontSize;
  public String tvcontColor;

  public String start_date;// "2019-03-05",
  public String end_date;//"2019-03-05",
  public String start_time;// "02:02:00",
  public String end_time;// "09:19:00"

  public int maxwidthpercent;


  public void updata(BroadcastData media) {
    this.top = media.top;
    if (media.duration == 0) {
      //轮播间隔默认15秒
      this.duration = 15;
    } else {
      this.duration = media.duration;
    }
    this.title = media.title;
    this.type = media.type;
    this.des_url = media.image_url;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Bdata) {
      Bdata d = (Bdata) o;
      return com(id, d.id)
          && com(des_url, d.des_url)
          && com(duration, d.duration)
          && com(top, d.top)
          && com(type, d.type)
          && com(title, d.title)
          && com(tvtitle, d.tvtitle)
          && com(tvcontent, d.tvcontent)
          && com(tvtitleSize, d.tvtitleSize)
          && com(tvtitleColor, d.tvtitleColor)
          && com(tvcontSize, d.tvcontSize)
          && com(start_date, d.start_date)
          && com(end_date, d.end_date)
          && com(start_time, d.start_time)
          && com(end_time, d.end_time)
          && com(tvcontColor, d.tvcontColor);
    }
    return false;
  }

  private static boolean com(String a, String b) {
    if (TextUtils.isEmpty(a) && TextUtils.isEmpty(b)) {
      return true;
    }

    if (TextUtils.isEmpty(a) || TextUtils.isEmpty(b)) {
      return false;
    }

    if (a.equals(b)) {
      return true;
    }
    return false;
  }

  private static boolean com(int a, int b) {
    return a == b;
  }
}
