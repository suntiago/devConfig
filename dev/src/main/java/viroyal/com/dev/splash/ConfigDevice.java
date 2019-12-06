package viroyal.com.dev.splash;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.suntiago.baseui.utils.log.Slog;
import com.suntiago.network.network.Api;
import com.suntiago.network.network.BaseRspObserver;
import com.suntiago.network.network.rsp.BaseResponse;
import com.suntiago.network.network.utils.MacUtil;
import com.suntiago.network.network.utils.SPUtils;

import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Jeremy on 2018/8/2.
 */

public class ConfigDevice {
  private final static String TAG = "ConfigDevice";
  private static String GETAPI_URL_MCPAPI = "";
  public static String school_id = "";
  public static String serial_number = "";
  public static String operator = "";
  //是否开启演示模式
  public static boolean DEMO_MODE = false;

  //获取设备ip地址
  public static String getDeviceId(Context context) {
    return MacUtil.getLocalMacAddressFromIp();
  }

  //获取设备ip地址
  public static String getDeviceId() {
    return MacUtil.getLocalMacAddressFromIp();
  }

  private static String api_neiwang;
  private static String netty_host_neiwang;
  private static String netty_port_neiwang;

  protected static void configDemoMode() {
    DEMO_MODE = true;
    api_neiwang = "https://192.168.1.208";
    netty_host_neiwang = "192.168.1.208";
    netty_port_neiwang = "8000";
    school_id = "1001";
    Api.get().setApiConfig("https://192.168.1.208" + "/", "192.168.1.208", 8000);

  }

  public static String getNeiwangIP() {
    return api_neiwang;
  }

  public static String getNeiwangSocketHost() {
    return netty_host_neiwang;
  }

  public static String getNeiwangSocketPort() {
    return netty_port_neiwang;
  }

  @Deprecated
  public static void setGetapiUrl(String url) {
    GETAPI_URL_MCPAPI = url;
  }

  @Deprecated
  //配置ip地址
  public static Subscription configIp(final Context context, String deviceType, final Action1<BaseResponse> action) {
    return configIp(GETAPI_URL_MCPAPI, context, deviceType, action);
  }

  //配置ip地址
  public static Subscription configIp(String hostApi, final Context context, String deviceType, final Action1<BaseResponse> action) {

    if (TextUtils.isEmpty(hostApi)) {
      hostApi = GETAPI_URL_MCPAPI;
    }

    if (TextUtils.isEmpty(deviceType)) {
      deviceType = "default";
    }

    api_neiwang = SPUtils.getInstance(context).get("api_neiwang");
    netty_host_neiwang = SPUtils.getInstance(context).get("netty_host_neiwang");
    netty_port_neiwang = String.valueOf(SPUtils.getInstance(context).get("netty_port_neiwang", 0));

    return Api.get().getApi(IpConfig.class, hostApi)
            .api(ConfigDevice.getDeviceId(context), deviceType)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new BaseRspObserver<ApiResponse>(ApiResponse.class, new Action1<ApiResponse>() {
              @Override
              public void call(ApiResponse rsp) {
                if (rsp.error_code == 1000) {
                  if (!SPUtils.getInstance(context).get("serial_number").equals(rsp.apiModel.serial_number)) {
                    //百度语音离线序列号
                    if (!TextUtils.isEmpty(rsp.apiModel.serial_number)) {
                      SPUtils.getInstance(context).put("serial_number", rsp.apiModel.serial_number);
                      serial_number = rsp.apiModel.serial_number;
                      Slog.d(TAG, "call [rsp]:serial_number:" + serial_number);
                    }
                  }

                  Slog.d(TAG, "call [rsp]:" + rsp.apiModel.config);
                  if (!SPUtils.getInstance(context).get("api_config").equals(rsp.apiModel.config)) {
                    Gson gson = new Gson();
                    ApiConfig ac = gson.fromJson(rsp.apiModel.config, ApiConfig.class);

                    SPUtils.getInstance(context).put("api_config", rsp.apiModel.config);
                    if (!TextUtils.isEmpty(rsp.apiModel.school_id)) {
                      SPUtils.getInstance(context).put("school_id", rsp.apiModel.school_id);
                      school_id = rsp.apiModel.school_id;
                    }
                    if (!TextUtils.isEmpty(ac.school_id)) {
                      SPUtils.getInstance(context).put("school_id", ac.school_id);
                      school_id = ac.school_id;
                    }
                    Slog.d(TAG, "call [rsp]:school_id:" + ac.school_id);
                    Api.get().setApiConfig(ac.api + "/", ac.netty_host, ac.netty_port);

                    SPUtils.getInstance(context).put("api_neiwang", ac.api_neiwang);
                    SPUtils.getInstance(context).put("netty_host_neiwang", ac.netty_host_neiwang);
                    SPUtils.getInstance(context).put("netty_port_neiwang", ac.netty_port_neiwang);

                    api_neiwang = SPUtils.getInstance(context).get("api_neiwang");
                    netty_host_neiwang = SPUtils.getInstance(context).get("netty_host_neiwang");
                    netty_port_neiwang = String.valueOf(SPUtils.getInstance(context).get("netty_port_neiwang", 0));
                  }
                }
                if (action != null) {
                  action.call(rsp);
                }
              }
            }));
  }

  interface IpConfig {
    /**
     * 获取api
     *
     * @param mac
     * @param app_name
     * @return
     */
    @GET("device/devmonitor/dev/api-config")
    Observable<ApiResponse> api(@Header("dev_mac") String mac, @Query("app_name") String app_name);
  }
}
