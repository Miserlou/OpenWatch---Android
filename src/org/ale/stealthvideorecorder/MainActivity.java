package org.ale.stealthvideorecorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    
    public boolean recording = false;
    final Handler mHandler = new Handler();
    private boolean m_servicedBind = false;
    private VideoRecorder vr;
    private RecorderActivity ra;
    private MainActivityGroup mag;
    private LinearLayout root;
    Context c;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        vr = (VideoRecorder) findViewById(R.id.camcorder_preview);
        c = this;
        
    }
    
    public void activateButton() {
        final ImageButton ib = (ImageButton) findViewById(R.id.ib);
        ib.setClickable(true);
        ib.setImageResource(R.drawable.grey);
        recording = false;
    }
    
    public void onResume() {
        super.onResume();
        final ImageButton ib = (ImageButton) findViewById(R.id.ib);
        root = (LinearLayout) findViewById(R.id.root);
        final Context c = this;

        final MainActivity ma = this;
        
        ib.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                
                if(event.getAction() != MotionEvent.ACTION_DOWN) {
                    return false;
                }
                
                if(ra.hidden) {
                    return false;
                }
                
                    if(recording){
                    return true;
                }
                   
                    else {
                        mHandler.postDelayed(new Runnable() {

                            public void run() {
                                recording = true;
//                                Toast.makeText(c, "Recording started!", Toast.LENGTH_SHORT).show();
                                ib.setClickable(false);
                                ra.start();
                            }}, 400);

                        ib.setImageResource(R.drawable.red);
                        return true;
                        }
            }
        });
        
       if(recording) {
           ib.setImageResource(R.drawable.red);
       }
    }
       
    
    public void setRecorderActivity(RecorderActivity raa) {
        ra = raa;
    }

    public void setParentGroup(MainActivityGroup magg) {
        mag = magg;
    }
    
    public FrameLayout getFL() {
        return (FrameLayout)findViewById(R.id.Recorder);
    }
    
}