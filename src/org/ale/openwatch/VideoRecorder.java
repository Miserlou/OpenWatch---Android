package org.ale.openwatch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

public class VideoRecorder extends SurfaceView implements SurfaceHolder.Callback{

  final MediaRecorder recorder = new MediaRecorder();
  SurfaceHolder holder;
  String path;
  Context c;

  /**
   * Creates a new audio recording at the given path (relative to root of SD card).
   */
  public VideoRecorder(Context con, AttributeSet attrs) {
      super(con, attrs);
      c = con;
      
      holder = getHolder();
      holder.addCallback(this);
      holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

  }
  
  public void setPath(String patha) {
      try {
          path = sanitizePath(patha);
          File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/rpath.txt");
          f.delete();
          f.createNewFile();
          FileOutputStream fOut = new FileOutputStream(f);
          OutputStreamWriter osw = new OutputStreamWriter(fOut); 
          osw.write(path);
          osw.flush();
          osw.close();
      }catch(IOException e) {
          e.printStackTrace();
      }
  }

  private String sanitizePath(String path) {
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    if (!path.contains(".")) {
      path += ".mp4";
    }
    return Environment.getExternalStorageDirectory().getAbsolutePath() + path;
  }

  /**
   * Starts a new recording.
   */
  public void start(Context c) throws IOException {
    String state = android.os.Environment.getExternalStorageState();
    if(!state.equals(android.os.Environment.MEDIA_MOUNTED))  {
        throw new IOException("SD Card is not mounted.  It is " + state + ".");
    }

    // make sure the directory we plan to store the recording in exists
    File directory = new File(path).getParentFile();
    if (!directory.exists() && !directory.mkdirs()) {
      throw new IOException("Path to file could not be created.");
      //TODO WHY DOESN'T THIS WORK?
      //Toast toast=Toast.makeText(this, "There was an error saving your recording. Please contact the app developers if this persists.", Toast.LENGTH_LONG);
      //toast.show();
    }

    WindowManager mWinMgr = (WindowManager)c.getSystemService(Context.WINDOW_SERVICE);
    int displayWidth = mWinMgr.getDefaultDisplay().getWidth();

    if( (Integer.parseInt(Build.VERSION.SDK) >= 8) && (displayWidth >= 480)) {
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
    }
    else{
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        recorder.setVideoFrameRate(30);
        recorder.setVideoSize(320, 240);
    }
    recorder.setOutputFile(path);
    //TODO If we have overt recording mode, I believe this is where it would be implemented but not sure. - Ringo
    Surface s = holder.getSurface();
    recorder.setPreviewDisplay(s);
    recorder.prepare();
    recorder.start();
      }

  /**
   * Stops a recording that has been previously started.
   */
  public void stop() throws IOException {
    try {
        recorder.stop();
        recorder.release();
    }
    catch(Exception e) {
    }
  }
  
  public String getPath() {
      return path;
  }
  
public void surfaceChanged(SurfaceHolder sholder, int format, int width,
        int height) {
    // TODO Auto-generated method stub
}

public void surfaceCreated(SurfaceHolder holder) {
    // TODO Auto-generated method stub
    
}

public void surfaceDestroyed(SurfaceHolder holder) {
    // TODO Auto-generated method stub
    
}

}
