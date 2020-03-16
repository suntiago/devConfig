package viroyal.com.dev.boot;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BootModel {
  @SerializedName("tomorrow")
  public Tomorrow tomorrow;

  @SerializedName("today")
  public Today today;

  @SerializedName("strategy")
  public List<Strategy> strategy;

  @SerializedName("device_type")
  public int device_type;
}
