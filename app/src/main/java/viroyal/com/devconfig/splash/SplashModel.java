package viroyal.com.devconfig.splash;

import android.annotation.SuppressLint;
import android.os.Parcel;

import com.suntiago.baseui.activity.base.theMvp.model.BaseModel;
import com.suntiago.baseui.activity.base.theMvp.model.IModel;

/**
 *
 * @author zy
 * @date 2019/1/25
 */


@SuppressLint("ParcelCreator")
public class SplashModel extends BaseModel {

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {

  }
}
