package viroyal.com.dev.boot;

import com.google.gson.annotations.SerializedName;
import com.suntiago.network.network.rsp.BaseResponse;

public class BootResponse extends BaseResponse {
  @SerializedName("extra")
  public BootModel bootModel;
}
