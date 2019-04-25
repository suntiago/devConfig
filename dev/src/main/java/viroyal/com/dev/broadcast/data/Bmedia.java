package viroyal.com.dev.broadcast.data;

/**
 * Created by Zaiyu on 2019/4/24.
 */
public class Bmedia {
  public String image_url;
  public String image_url_local;
  //视频缩略图
  public String thumb_url;

  /*轮播数据的类型，应对不同的使用情况， 暂时使用这个字段*/
  //视频缩略图本地下载地址
  public String thumb_url_path;
  public long lastReachTimes;
}
