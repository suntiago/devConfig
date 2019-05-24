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

  @SerializedName("api_neiwang")
  public String api_neiwang;
  @SerializedName("netty_host_neiwang")
  public String netty_host_neiwang;
  @SerializedName("netty_port_neiwang")
  public int netty_port_neiwang;
}
