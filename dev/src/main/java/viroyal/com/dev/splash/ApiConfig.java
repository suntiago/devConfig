package viroyal.com.dev.splash;

import com.google.gson.annotations.SerializedName;

public class ApiConfig {
    @SerializedName("api")
    public String api;
    @SerializedName("netty_host")
    public String netty_host;
    @SerializedName("netty_port")
    public int netty_port;
    @SerializedName("school_id")
    public String school_id;
}
