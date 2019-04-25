package viroyal.com.dev.broadcast.data;

import android.util.SparseArray;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Zaiyu on 2019/4/23.
 * 剥离数据更新处理
 */
public class BroadcastDataManager {

  SparseArray<HashMap<String, Bdata>> mSparseArray;

  HashMap<String, Bmedia> mStringBmediaHM;


  public BroadcastDataManager() {

  }

  public void refreshdata(List<Bdata> dataList) {
  }

  public void refreshDataPreLoad(List<Bdata> dataList) {
  }

}
