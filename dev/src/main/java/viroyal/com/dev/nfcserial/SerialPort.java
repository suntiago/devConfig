package viroyal.com.dev.nfcserial;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class SerialPort {
    private static final String TAG = "SerialPort";

    /*
     * Do not remove or rename the field mFd: it is used by native method
     * close();
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {
        try {
            System.loadLibrary("native-lib");
        } catch (Exception ex) {
        }

        Log.i(TAG, "SerialPort: device exists"+ device.exists());
        Log.i(TAG, "SerialPort: baudrate"+ baudrate);
        Log.i(TAG, "SerialPort: flags"+ flags);
        mFd = open(device.getAbsolutePath(), baudrate, flags);

        if (mFd == null) {
            Log.d("Ginger", "mfd is null");
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    // JNI
    private native static FileDescriptor open(String path, int baudrate, int flags);

    public native void close();

    static {
        System.loadLibrary("native-lib");
    }
}