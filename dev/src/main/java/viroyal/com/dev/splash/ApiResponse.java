package viroyal.com.dev.splash;

import com.google.gson.annotations.SerializedName;
import com.suntiago.network.network.rsp.BaseResponse;

public class ApiResponse extends BaseResponse {
    @SerializedName("extra")
    public ApiModel apiModel;
}
