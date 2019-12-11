package viroyal.com.dev.nfcserial;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

public class NfcService extends Service {

    private SerialPort serialPort;
    private OutputStream ops;
    private InputStream ips;
    private boolean isLoop = false;
    private Handler handler;

    private LinkedList<Byte> link;

    public NfcService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        link = new LinkedList<>();
        return new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 解绑服务后调用
     *
     * @param intent
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public class MyBinder extends Binder {
        /**
         * 开始循环遍历NFC
         */
        public void startLoop(Handler handler) {

            try {
                Log.d("ginger", "start access /dev/ttyS4");
                int flag = 1;
                serialPort = new SerialPort(new File("/dev/ttyS4"), 9600, flag);
                ops = serialPort.getOutputStream();
                ips = serialPort.getInputStream();
                if(ops == null | ips == null) {
                    Log.d("Ginger", "ops and ips = null");
                }else {
                    Log.d("Ginger", "has get ips and ops");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

                NfcService.this.handler = handler;
                Log.d("ginger", "开启串口");
                isLoop = true;

                new ReadThread().start();
        }

        /**
         * 停止loop
         */
        public void endLoop() {
            Log.d("ginger", "关闭串口");
            isLoop = false;
            updateData("", 1);
        }
    }

    /**
     * 循环等待读卡
     */
    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();

            int size = 0;

            while (isLoop) {

                byte[] bytes = new byte[7];

                try {
                    if (ips != null) {
                        size = ips.read(bytes);
                    }
                    //55000000000000 + ffffffffffff00 | 55ff + ffffffffff0000 | ..
                    if(size > 0 && size < 7) {
                        for (int i = 0; i < size; i++) {
                            link.add(bytes[i]);
                        }

                        int len = link.size();

                        if (len > 7) {
                            for (int i = 0; i < len - 7; i++) {
                                link.removeFirst();
                            }
                        }

                        if(isLoop && link.size() == 7 && checkFist(link)){
                            Log.d("Ginger", "add link data : " + link.toString());
                            String str = bytesToHex(toArray(link));
                            updateData(str, 0);
                        }
                    }else if (size == 7) {//55ffffffffffff
                        String str = bytesToHex(bytes);
                        Log.d("Ginger", str);
                        if(isLoop && checkRight(bytes)) {
                            updateData(str, 0);
                            bytes = null;
                        }

                    }
                    Thread.sleep(200);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 更新UI中的读卡数据
     *
     * @param hex
     */
    private void updateData(String hex, int what) {
        if (handler != null) {
            Log.d("ginger", "prepare update UI Data");
            Message message = Message.obtain();
            message.what = what;
            message.obj = hex;
            handler.sendMessage(message);
        }
    }


    /**
     * 字节数组转16进制
     *
     * @param bytes 需要转换的byte数组
     * @return 转换后的Hex字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 判断是否为有效数据
     * @param bytes
     * @return
     */
    private boolean checkRight(byte[] bytes){

        String data = bytesToHex(bytes);

        Log.d("Ginger", data);

        if (!data.substring(0, 2).equals("55") || !data.substring(2, 4).equals("aa")) {
            return false;
        }

        if(bytes[6] != (byte) (bytes[2] ^ bytes[3] ^ bytes[4] ^ bytes[5])){
            return false;
        }

        if (data.substring(12, 14).equals("00")) {
            return false;
        }

        return true;
    }

    /**
     * 判断linkedList是否为有效数据
     * @param link
     * @return
     */
    private boolean checkFist(LinkedList<Byte> link){

        if (link.size() != 7) {
            return false;
        }

        byte[] bytes = new byte[7];
        for (int i = 0; i < 7; i++) {
            bytes[i] = link.get(i);
        }

        return checkRight(bytes);
    }

    /**
     * toArray
     * @param link
     * @return
     */
    private byte[] toArray(LinkedList<Byte> link){

        byte[] bytes = new byte[7];

        for (int i = 0; i < link.size(); i++) {
            bytes[i] = link.get(i);
        }

        return bytes;
    }
}
