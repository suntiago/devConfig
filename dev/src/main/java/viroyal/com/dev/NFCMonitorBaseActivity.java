package viroyal.com.dev;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.CallSuper;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.suntiago.baseui.activity.base.AppDelegateBase;
import com.suntiago.baseui.activity.base.theMvp.model.IModel;
import com.suntiago.baseui.utils.log.Slog;
import com.suntiago.getpermission.rxpermissions.RxPermissions;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;

import rx.functions.Action1;
import viroyal.com.dev.nfc.NdefMessageParser;
import viroyal.com.dev.nfc.NfcParseUtil;
import viroyal.com.dev.nfc.record.DefaultRecord;
import viroyal.com.dev.nfc.record.ParsedNdefRecord;
import viroyal.com.dev.nfcserial.NfcService;

/**
 * Created by Zaiyu on 2019/3/22.
 * 在MonitorBaseActivity 的基础上加入nfc的支持
 * 页面需要NFC的时候
 * protected NFCSwitch NFCSwitch() {
 * return NFCSwitch.DEFAULT;
 * }
 * <p>
 * 需要刷卡的时候调用 enableNFC， 不需要用时调用disEnableNFC
 */

public abstract class NFCMonitorBaseActivity<T extends AppDelegateBase, D extends IModel> extends MonitorBaseActivity<T, D> {
  //是否支持NFC

  private NfcAdapter mNFCAdapter;
  private PendingIntent mPendingIntent;
  private NdefMessage mNdefPushMessage;

  boolean tagNFCOpen = false;

  /**
   * 默认胚子
   * DEFAULT,
   * 标准NFC
   * STANDARD,
   * 后添加的外置nfc
   * ADDED
   */
  protected enum NFCSwitch {
    DEFAULT,
    STANDARD,
    ADDED,
    SERIAL
  }

  protected NFCSwitch NFCSwitch() {
    return NFCSwitch.DEFAULT;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (NFCSwitch() == NFCSwitch.STANDARD) {
      resolveIntent(getIntent());
    } else if (NFCSwitch() == NFCSwitch.ADDED) {
      mStringBufferResult = new StringBuilder();
    } else if (NFCSwitch() == NFCSwitch.SERIAL) {
      startNfcService();
    }
  }

  @Override
  protected void initView(Bundle savedInstanceState) {
    super.initView(savedInstanceState);
    if (NFCSwitch() == NFCSwitch.STANDARD) {
      initNFC();
    }
  }

  /*-----------------------------------------NFC串口开始------------------------------------------------*/

  private Intent serviceIntent;
  private NfcService.MyBinder binder;

  private ServiceConnection conn = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      binder = (NfcService.MyBinder) service;
      Log.i(TAG, "onServiceConnected: binder" + binder);
      //打开串口，进行读卡
      if (binder != null) {
        binder.startLoop(handler);
      }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      binder = null;
    }
  };

  @SuppressLint("HandlerLeak")
  Handler handler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      Log.i(TAG, "读到NFC数据了");
      switch (msg.what) {
        case 0:
          //数据回调
          String data = (String) msg.obj;
          if (data != null && data.length() > 0) {
            Slog.d(TAG, "NFC数据 = " + data);
            String sixteenStr = data.substring(4, 12);
            Slog.d(TAG, "NFC数据 16进制= " + sixteenStr);
            String tenStr = new BigInteger(sixteenStr, 16).toString();
            if (!TextUtils.isEmpty(tenStr)) {
              try {
                String cardNo = String.format("%010d", Long.parseLong(tenStr));
                Slog.d(TAG, "NFC数据 10进制= " + cardNo);
                readSerialNfcId(cardNo);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
          break;
        default:
          break;
      }
    }
  };

  private void startNfcService() {
    serviceIntent = new Intent(this, NfcService.class);
    startService(serviceIntent);
  }

  /**
   * 后加的nfc,读取到数据后在此回调
   */
  protected void readSerialNfcId(String barcode) {
    Slog.d(TAG, "readSerialNfcId performScanSuccess  [barcode]:" + barcode);
  }

  /*-----------------------------------------NFC串口结束------------------------------------------------*/

  /**
   * 初始化nfc
   */

  private void initNFC() {
    Slog.d(TAG, "initNFC  []:");
    if (!RxPermissions.getInstance(this).isGranted(Manifest.permission.NFC)) {
      RxPermissions.getInstance(this).request(Manifest.permission.NFC).subscribe(new Action1<Boolean>() {
        @Override
        public void call(Boolean aBoolean) {
          if (aBoolean) {
            initNFC();
          } else {
            viewDelegate.showToast(
                    getResources().getString(R.string.please_turn_on_the_permission_of_nfc));
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + NFCMonitorBaseActivity.this.getPackageName()));
            startActivity(intent);
          }
        }
      });
    } else {
      // 获取默认的NFC控制器
      mNFCAdapter = NfcAdapter.getDefaultAdapter(this);
      if (mNFCAdapter == null) {
        viewDelegate.showToast("没有找到NFC设备");
        return;
      }
      mPendingIntent = PendingIntent.getActivity(this, 0,
              new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
      mNdefPushMessage = new NdefMessage(new NdefRecord[]{newTextRecord(
              "Message from NFC Reader :-)", Locale.ENGLISH, true)});
    }
  }


  public final void enableNFC() {
    Slog.d(TAG, "startNFC  []:");
    tagNFCOpen = true;
    resumeNFC();
  }

  public final void disEnableNFC() {
    Slog.d(TAG, "stopNfc  []:");
    pauseNFC();
    tagNFCOpen = false;
  }

  /**
   * 创建NdefRecord
   */
  private NdefRecord newTextRecord(String text, Locale locale, boolean encodeInUtf8) {
    byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
    Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
    byte[] textBytes = text.getBytes(utfEncoding);
    int utfBit = encodeInUtf8 ? 0 : (1 << 7);
    char status = (char) (utfBit + langBytes.length);
    byte[] data = new byte[1 + langBytes.length + textBytes.length];
    data[0] = (byte) status;
    System.arraycopy(langBytes, 0, data, 1, langBytes.length);
    System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
    return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
  }

  private void resumeNFC() {
    if (!tagNFCOpen) {
      return;
    }
    Slog.d(TAG, "resumeNFC  []:");
    if (mNFCAdapter == null) {
      Slog.e(TAG, "resumeNFC  []:" + "mNFCAdapter == null");
      return;
    }
    if (!mNFCAdapter.isEnabled()) {
      Slog.e(TAG, "resumeNFC  []:" + "!mNFCAdapter.isEnabled()");
      return;
    }
    if (mNFCAdapter != null && activityIsResume) {
      // 隐式启动
      mNFCAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
      mNFCAdapter.enableForegroundNdefPush(this, mNdefPushMessage);
    }
  }

  private void pauseNFC() {
    if (!tagNFCOpen) {
      return;
    }
    Slog.d(TAG, "pauseNFC  []:");
    if (mNFCAdapter != null) {
      // 隐式启动
      mNFCAdapter.disableForegroundDispatch(this);
      mNFCAdapter.disableForegroundNdefPush(this);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (NFCSwitch() == NFCSwitch.STANDARD) {
      resumeNFC();
    } else if(NFCSwitch() == NFCSwitch.SERIAL){
      bindService(new Intent(this, NfcService.class), conn, Context.BIND_AUTO_CREATE);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (NFCSwitch() == NFCSwitch.STANDARD) {
      pauseNFC();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    try {
      stopService(new Intent(NFCMonitorBaseActivity.this, NfcService.class));
      if(null != conn){
        unbindService(conn);
      }
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  // 获取系统隐式启动的
  @Override
  public void onNewIntent(Intent intent) {
    if (NFCSwitch() == NFCSwitch.STANDARD) {
      resolveIntent(intent);
    }
  }

  private void resolveIntent(Intent intent) {
    String action = intent.getAction();
    if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
            || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
            || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
      Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
      NdefMessage[] msgs;
      if (rawMsgs != null) {
        msgs = new NdefMessage[rawMsgs.length];
        for (int i = 0; i < rawMsgs.length; i++) {
          msgs[i] = (NdefMessage) rawMsgs[i];
        }
      } else {
        // Unknown tag type
        byte[] empty = new byte[0];
        byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        byte[] payload = NfcParseUtil.dumpTagData(tag).getBytes();
        NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
        NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
        msgs = new NdefMessage[]{msg};
      }
      // Setup the views
      // buildTagViews(msgs);
      nfcIDRead(msgs);
    }
  }

  private void buildTagViews(NdefMessage[] msgs) {
    if (msgs == null || msgs.length == 0) {
      return;
    }
    List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
    final int size = records.size();
    for (int i = 0; i < size; i++) {
      ParsedNdefRecord record = records.get(i);
      if (record instanceof DefaultRecord) {
        DefaultRecord dRecord = (DefaultRecord) record;
        Slog.d(TAG, "buildTagViews  [msgs]: \n" + dRecord.getReversedDec() + " \n" + dRecord.getText());
      }
    }
  }

  /**
   * 标准nfc,读取到数据后在此回调
   */
  @CallSuper
  protected void nfcIDRead(NdefMessage[] msgs) {
    if (msgs != null && msgs.length > 0) {
      NdefRecord[] records = msgs[0].getRecords();
      if (records != null && records.length > 0) {
        String id = NfcParseUtil.toReversedDec(records[0].getId()) + "";
        Slog.d(TAG, "nfcIDRead  [msgs]:" + id);
        readStandardNfcId(id);
      } else {
        Slog.e(TAG, "nfcIDRead:records == null || records.length == 0 ");
      }
    } else {
      Slog.e(TAG, "nfcIDRead:msgs == null || msgs.length == 0 ");
    }
  }

  /**
   * 标准nfc,读取到ID数据后在此回调
   */
  protected void readStandardNfcId(String nfcId) {
    Slog.d(TAG, "readStandardNfcId  [nfcId]:" + nfcId);
  }


  char[] code = new char[10];
  //记录刷卡字节的顺序
  int index = 0;
  private boolean mCaps;
  private StringBuilder mStringBufferResult;
  private final Handler mHandler = new Handler();
  boolean isScaning = false;

  //获取扫描内容
  private char getInputCode(KeyEvent event) {
    int keyCode = event.getKeyCode();
    char aChar;
    if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
      //字母
      aChar = (char) ((mCaps ? 'A' : 'a') + keyCode - KeyEvent.KEYCODE_A);
    } else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
      if (mCaps) {
        switch (keyCode) {
          case KeyEvent.KEYCODE_1:
            aChar = '!';
            break;
          case KeyEvent.KEYCODE_2:
            aChar = '@';
            break;
          case KeyEvent.KEYCODE_3:
            aChar = '#';
            break;
          case KeyEvent.KEYCODE_4:
            aChar = '$';
            break;
          case KeyEvent.KEYCODE_5:
            aChar = '%';
            break;
          case KeyEvent.KEYCODE_6:
            aChar = '^';
            break;
          case KeyEvent.KEYCODE_7:
            aChar = '&';
            break;
          case KeyEvent.KEYCODE_8:
            aChar = '*';
            break;
          case KeyEvent.KEYCODE_9:
            aChar = '(';
            break;
          case KeyEvent.KEYCODE_0:
            aChar = ')';
            break;
          default:
            aChar = 0;
            break;
        }
      } else {
        //数字
        aChar = (char) ('0' + keyCode - KeyEvent.KEYCODE_0);
      }
    } else {
      //其他符号
      switch (keyCode) {
        case KeyEvent.KEYCODE_PERIOD:
          aChar = '.';
          break;
        case KeyEvent.KEYCODE_MINUS:
          aChar = mCaps ? '_' : '-';
          break;
        case KeyEvent.KEYCODE_SLASH:
          aChar = '/';
          break;
        case KeyEvent.KEYCODE_BACKSLASH:
          aChar = mCaps ? '|' : '\\';
          break;
        default:
          aChar = 0;
          break;
      }
    }
    return aChar;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (NFCSwitch() == NFCSwitch.ADDED) {
      // 根据食堂后台配置刷卡类型的需求，在onCreate时若NFCSwitch不为added（缓存），mStringBufferResult将得不到初始化
      // 导致刷卡会一直获取空值（""）
      // 这里重新初始化是为了应对通过后台拿到数据重新设置为added的情况
      if (mStringBufferResult == null) {
        mStringBufferResult = new StringBuilder();
      }
      analysisKeyEvent(event);
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (NFCSwitch() == NFCSwitch.ADDED) {
      checkLetterStatus(event);
    }
    return super.onKeyUp(keyCode, event);
  }

  private final Runnable mScanningFishedRunnable = new Runnable() {
    @Override
    public void run() {
      Slog.d(TAG, "mScanningFishedRunnable run  []:");
      isScaning = true;
      String barcode = null;
      if (mStringBufferResult != null) {
        barcode = mStringBufferResult.toString();
      }
      if (barcode == null) {
        barcode = "";
      }
      if (mStringBufferResult != null) {
        mStringBufferResult.setLength(0);
      }
      readAddedNfcId(barcode);
    }
  };


  /**
   * 后加的nfc,读取到数据后在此回调
   */
  protected void readAddedNfcId(String barcode) {
    Slog.d(TAG, "performScanSuccess  [barcode]:" + barcode);
  }

  //检查shift键
  private void checkLetterStatus(KeyEvent event) {
    int keyCode = event.getKeyCode();
    if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT || keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) {
      if (event.getAction() == KeyEvent.ACTION_DOWN) {
        //按着shift键，表示大写
        mCaps = true;
      } else {
        //松开shift键，表示小写
        mCaps = false;
      }
    }
  }

  /**
   * 扫码枪事件解析
   */
  private void analysisKeyEvent(KeyEvent event) {
    int keyCode = event.getKeyCode();
    //字母大小写判断
    checkLetterStatus(event);
    if (event.getAction() == KeyEvent.ACTION_DOWN) {
      char aChar = getInputCode(event);
      analusisErrorEvent(aChar, keyCode);
    }
  }

  //扫码会有错误的情况，在此判断出错误的情况，筛选出来，不处理
  private synchronized void analusisErrorEvent(char c, int keyCode) {
    if (keyCode == KeyEvent.KEYCODE_ENTER) {
      //本次刷卡结束
      if (index == 10) {
        for (int j = 0; j < 10; j++) {
          if (mStringBufferResult != null) {
            mStringBufferResult.append(code[j]);
          }
        }
        index = 0;
        mHandler.removeCallbacks(mScanningFishedRunnable);
        mHandler.post(mScanningFishedRunnable);
      } else {
        Slog.e(TAG, "analusisErrorEvent: index is not 10");
      }
    } else {
      if (c == 0) {
        //排除非法字符
        return;
      }
      if (index < 10) {
        code[index] = c;
        index++;
      } else {
        for (int j = 0; j < 9; j++) {
          code[j] = code[j + 1];
        }
        code[9] = c;
      }
    }
  }
}
