package org.ale.openwatch;

import org.ale.openwatch.R;
import org.ale.openwatch.rService;
import org.openintents.filemanager.FileManagerActivity;

import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivityGroup extends ActivityGroup {
    /** Called when the activity is first created. */
    
    public boolean recording = false;
    final Handler mHandler = new Handler();
    private boolean r_servicedBind = false;
    private boolean u_servicedBind = false;
    private VideoRecorder vr;
    private String code = "BBB";
    private String codeLeft = "BBB";
    RecorderActivity raActivity;
    MainActivity maActivity;
    private int vol;
    recordService r_service;
    uploadService u_service;

    private ServiceConnection r_connection = new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder service) {
            r_service = recordService.Stub.asInterface(service);
            }

        public void onServiceDisconnected(ComponentName name) {
            r_service = null;
            }
    };
    
    private ServiceConnection u_connection = new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder service) {
            u_service = uploadService.Stub.asInterface(service);
            }

        public void onServiceDisconnected(ComponentName name) {
            u_service = null;
            }
    };

    

    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        if(raActivity.hidden) {
        
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if("B".equals(codeLeft.substring(0, 1))) {
                    codeLeft = codeLeft.substring(1, codeLeft.length());
                }
                else {
                    codeLeft = code;
                }
            }
            if (keyCode == KeyEvent.KEYCODE_MENU) {
                if("M".equals(codeLeft.substring(0, 1))) {
                    codeLeft = codeLeft.substring(1, codeLeft.length());
                }
                else {
                    codeLeft = code;
                }
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                if("U".equals(codeLeft.substring(0, 1))) {
                    codeLeft = codeLeft.substring(1, codeLeft.length());
                }
                else {
                    codeLeft = code;
                }
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                if("D".equals(codeLeft.substring(0, 1))) {
                    codeLeft = codeLeft.substring(1, codeLeft.length());
                }
                else {
                    codeLeft = code;
                }
            }
            
            if(codeLeft.length() == 0) {
                raActivity.stop();
                maActivity.activateButton();
                codeLeft = code;
                
                
                // UPLOAD STUFF
                // UPLOAD STUFF
                // UPLOAD STUFF
                // UPLOAD STUFF
                // UPLOAD STUFF
                
                AlertDialog.Builder alert2 = new AlertDialog.Builder(this);

                alert2.setTitle(getString(R.string.recording_saved));
                alert2.setMessage(getString(R.string.upload_recording_now));
                final Context c = this;
                alert2.setPositiveButton(getString(R.string.yes_upload), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                        mHandler.post(new Runnable() {

                            public void run() {
                                Intent mainIntent = new Intent(c, DescribeActivity.class); 
                                startActivity(mainIntent);
//                                    u_service.start();
                            }});

                        finish();
                }});
                alert2.setNegativeButton(getString(R.string.no_quit), new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                      finish();
                  }
                });
                alert2.show();
                
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            }
        return false;
    }
    
    private void bindRecordService(){
        r_servicedBind = bindService(new Intent(this, rService.class), 
                r_connection, Context.BIND_AUTO_CREATE);
    }
    
    private void bindUploadService(){
        u_servicedBind = bindService(new Intent(this, uService.class), 
                u_connection, Context.BIND_AUTO_CREATE);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        
        MenuItem mi = menu.add(0,0,0,R.string.open);
        mi.setIcon(android.R.drawable.ic_menu_add);
        MenuItem mi3 = menu.add(0,2,0,R.string.tutorial);
        mi3.setIcon(android.R.drawable.ic_menu_help);
        MenuItem mi2 = menu.add(0,1,0,R.string.about);
        mi2.setIcon(android.R.drawable.ic_menu_view);
//        MenuItem mi4 = menu.add(0,2,0,R.string.settings);
//        mi4.setIcon(android.R.drawable.ic_menu_preferences);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        

        switch (item.getItemId()) {
            case 0:
                startActivity(new Intent(this, FileManagerActivity.class));
                return(true);
            case 1:
                // About
                new AlertDialog.Builder(this)
                .setTitle("About OpenWatch")
                .setMessage(getString(R.string.about_text))
                .setPositiveButton("Okay!", null)
                .show();
                return(true);
            case 2:
                // Tutorial 
                new AlertDialog.Builder(this)
                .setTitle(getString(R.string.tutorial))
                .setMessage(getString(R.string.tutorial_text))
                .setPositiveButton("Okay!", null)
                .show();
                return(true);
            case 3:
                startActivity(new Intent(this, FileManagerActivity.class));
                return(true);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.group);
        
        Intent i = new Intent(this, RecorderActivity.class);
        // Ensure that only one ListenActivity can be launched. Otherwise, we may
        // get overlapping media players.
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Window w =
          getLocalActivityManager().startActivity(RecorderActivity.class.getName(),
              i);
        View v = w.getDecorView();
        ((ViewGroup) findViewById(R.id.Recorder)).addView(v,
            new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));

        // A little hacky, but otherwise we get an annoying black line where the
        // seam of the drawer's edge is.
        ((FrameLayout)((ViewGroup) v).getChildAt(0)).setForeground(null);
        
    
        Intent j = new Intent(this, MainActivity.class);
        // Ensure that only one ListenActivity can be launched. Otherwise, we may
        // get overlapping media players.
        Window w2 =
          getLocalActivityManager().startActivity(MainActivity.class.getName(),
              j);
        View v2 = w2.getDecorView();
        ((ViewGroup) findViewById(R.id.Main)).addView(v2,
            new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
        
        raActivity = (RecorderActivity) getLocalActivityManager().getActivity(RecorderActivity.class.getName());
        maActivity = (MainActivity) getLocalActivityManager().getActivity(MainActivity.class.getName());
        
        maActivity.setRecorderActivity(raActivity);
        raActivity.setMainActivity(maActivity);
        raActivity.setParentGroup(this);
        maActivity.setParentGroup(this);
        raActivity.setFL(maActivity.getFL());
        
        AudioManager mgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        vol = mgr.getStreamVolume(AudioManager.STREAM_SYSTEM);
        mgr.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        final SharedPreferences.Editor editor2;
        String first = prefs.getString("first_time", "fuck");
        if(first.contains("fuck")){
            new AlertDialog.Builder(this)
            .setMessage("Welcome to OpenWatch! \n\n This application allows opportunistic citizen journalists to invisibly record public and private officials and post the recordings to a central website, openwatch.net. A guide to using the application is availble in the Tutorial in the menu. More information about the OpenWatch can be found in the About section.")
            .setPositiveButton("Okay!", null)
            .show();
            editor2 = prefs.edit();
            editor2.putString("first_time", "shitballs");
            editor2.commit();
        }
        
        code = prefs.getString("code", "BBB");
        codeLeft = code;
        
        startService(new Intent(this, rService.class));
        bindRecordService();
       
    }

    
    public void onResume() {
        super.onResume();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        AudioManager mgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mgr.setStreamVolume(AudioManager.STREAM_SYSTEM, vol, 0);
    }
    
    public void stopMain() {
        MainActivity maActivity = (MainActivity) getLocalActivityManager().getActivity(MainActivity.class.getName());
        maActivity.finish();
    }
    
       
}