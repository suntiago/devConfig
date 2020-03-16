package viroyal.com.dev.boot;

import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface BootConfig {
  /**
   * 获取定时开关机策略
   *
   * @param bootRequest
   * @return
   */
  @POST("api/getTime")
  Observable<BootResponse> bootApi(@Body BootRequest bootRequest);
}
