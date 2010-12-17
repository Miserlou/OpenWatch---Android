package org.ale.openwatch;

import java.io.IOException;

import org.ale.openwatch.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class RecorderActivity extends Activity {
    /** Called when the activity is first created. */
    
    public boolean hidden = false;
    final Handler mHandler = new Handler();
    public VideoRecorder vr;
    private ImageView iv;
    private FrameLayout fl;
    private MainActivityGroup mag;
    private Activity mainer;
    Context co;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.playa);
        
        vr = (VideoRecorder) findViewById(R.id.camcorder_preview);
        iv = (ImageView) findViewById(R.id.hider);
        co = this;

    }
    
    public void reset() {
        vr.setPath("/recordings/" + System.currentTimeMillis() + ".mp4");
        vr.recorder.reset();
    }
    
    public String getPath() {
        return vr.getPath();
    }
    
    public void onResume() {
        super.onResume();
    }
       
    
    public void start() {
            vr.setVisibility(View.VISIBLE);
            iv.setVisibility(View.VISIBLE);
            hidden = true;
            
            final VideoRecorder vvv = vr;
            vr.setPath("/recordings/" + System.currentTimeMillis() + ".mp4");
            mHandler.post(new Runnable() {

                public void run() {
                    try {
                        
                        vvv.start(co);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }});
            
    }
    
    public void stop() {
            
            
            final VideoRecorder vvv = vr;
            mHandler.post(new Runnable() {

                public void run() {
                    try {
                        vvv.stop();
                        vr.setVisibility(View.GONE);
                        iv.setVisibility(View.GONE);
                        hidden = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }});
           
    }
    
    public void setParentGroup(MainActivityGroup magg) {
        mag = magg;
    }
    
    public void setMainActivity(Activity magg) {
        mainer = magg;
        fl = (FrameLayout) mainer.findViewById(R.id.Recorder);
    }
    
    public void setFL(FrameLayout magg) {
        fl = magg;
    }
}