package viroyal.com.dev.broadcast;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.suntiago.baseui.utils.log.Slog;

import java.io.IOException;

import viroyal.com.dev.R;


/**
 * Created by zy on 2019/1/29.
 * <p>
 * 视频轮播
 */

public class BroadcastVideo implements BroadcastViewItem<BroadcastData> {
  private final String TAG = getClass().getSimpleName();
  Context mContext = null;
  SurfaceView mSurfaceView;

  private MediaPlayer mediaPlayer;
  private SurfaceHolder surfaceHolder;
  BroadcastProgress mBroadcastProgress;
  BroadcastData mcBroadcastData = null;

  //播放状态，默认开启
  boolean playingStatus = true;

  public BroadcastVideo(Context context) {
    Slog.d(TAG, "BroadcastVideo  [context]:");
    mContext = context;
    createView();
  }

  @Override
  public View bindView(BroadcastData data) {
    Slog.d(TAG, "bindView  [data]:");
    // 设置显示视频显示在SurfaceView上
    mcBroadcastData = data;
    return mSurfaceView;
  }

  private int currentPosition = 0;

  boolean tagSurfaceHolderCreated = false;

  @Override
  public View createView() {

    mSurfaceView = new SurfaceView(mContext);
    ViewGroup.LayoutParams l = new ViewGroup.LayoutParams(-1, -1);
    mSurfaceView.setLayoutParams(l);
    mSurfaceView.setTag(R.id.tag_first, this);
    //SurfaceHolder是SurfaceView的控制接口
    surfaceHolder = mSurfaceView.getHolder();
    //Surface类型
    surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    surfaceHolder.addCallback(
        new SurfaceHolder.Callback() {

          @Override
          public void surfaceCreated(SurfaceHolder holder) {
            Slog.d(TAG, "surfaceCreated  [holder]:");
            tagSurfaceHolderCreated = true;
            start();
            resume();
          }

          @Override
          public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            Slog.d(TAG, "surfaceChanged  [holder, format, width, height]:");
          }

          @Override
          public void surfaceDestroyed(SurfaceHolder holder) {
            Slog.d(TAG, "surfaceDestroyed  [holder]:");
            pause();
            stop();
            tagSurfaceHolderCreated = false;
          }
        });
    return mSurfaceView;
  }

  @Override
  public View getView() {
    return mSurfaceView;
  }

  @Override
  public void start() {
    Slog.d(TAG, "start  []:");

    mSurfaceView.setVisibility(View.VISIBLE);
  }

  @Override
  public void resume() {
    Slog.d(TAG, "resume  []:");
    if (tagSurfaceHolderCreated && playingStatus) {
      play(currentPosition);
    }
  }

  @Override
  public void pause() {
    Slog.d(TAG, "pause  []:");
    stopPlay();
  }

  @Override
  public void stop() {
    Slog.d(TAG, "stop  []:");
    mSurfaceView.setVisibility(View.INVISIBLE);
  }

  @Override
  public void destoryView() {
    Slog.d(TAG, "destoryView  []:");
    if (mediaPlayer != null) {
      mediaPlayer.release();
    }
  }

  @Override
  public void setBroadcastProgress(BroadcastProgress progress) {
    mBroadcastProgress = progress;
  }

  @Override
  public void switchPlayingStatus(boolean playing) {
    if (playingStatus == playing) {
      return;
    }
    if (!playingStatus) {
      pause();
    }
  }

  private void play(final int msec) {
    currentPosition = 0;
    String path = mcBroadcastData.image_url_path;
    if (mediaPlayer == null) {
      mediaPlayer = new MediaPlayer();
    }
    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    mediaPlayer.setLooping(true);
    try {
      mediaPlayer.reset();
      mediaPlayer.setDataSource(path);
    } catch (IOException e) {
      e.printStackTrace();
    }
    mediaPlayer.setDisplay(surfaceHolder);
    mediaPlayer.prepareAsync();
    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mp) {
        mediaPlayer.start();
        mediaPlayer.seekTo(msec);
      }
    });
    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        if (mBroadcastProgress != null) {
          mBroadcastProgress.progress(100);
        }
      }
    });

    mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
      @Override
      public boolean onError(MediaPlayer mp, int what, int extra) {
        play(0);
        return false;
      }
    });
  }

  private void stopPlay() {
    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
      currentPosition = mediaPlayer.getCurrentPosition();
      mediaPlayer.stop();
    }

  }
}
