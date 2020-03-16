package viroyal.com.dev.boot;

import com.google.gson.annotations.SerializedName;

import org.kymjs.kjframe.database.annotate.Id;
import org.kymjs.kjframe.database.annotate.Property;
import org.kymjs.kjframe.database.annotate.Table;

@Table(name = "strategy")
public class Strategy {
  @Id(column = "id")
  public int id;

  /**
   * 是否按年周期执行
   */
  @SerializedName("in_cycle")
  @Property(column = "in_cycle")
  public String in_cycle;
  /**
   * 执行星期
   */
  @SerializedName("weeks")
  @Property(column = "weeks")
  public String weeks;
  /**
   * 策略执行开始日期
   */
  @SerializedName("start_date")
  @Property(column = "start_date")
  public String start_date;
  /**
   * 策略执行结束日期
   */
  @SerializedName("end_date")
  @Property(column = "end_date")
  public String end_date;
  /**
   * 策略类型 1常规 2临时
   */
  @SerializedName("type")
  @Property(column = "type")
  public String type;
  /**
   * 开机时间
   */
  @SerializedName("on_time")
  @Property(column = "on_time")
  public String on_time;
  /**
   * 关机时间
   */
  @SerializedName("off_time")
  @Property(column = "off_time")
  public String off_time;
}
