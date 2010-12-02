package org.ale.openwatch;

import org.ale.openwatch.R;
import org.ale.openwatch.rService;
import org.ale.openwatch.recordService;

import android.app.ActivityGroup;
import android.app.AlertDialog;
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
import android.os.RemoteException;
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
            System.out.println("onServiceConnected");
            }

        public void onServiceDisconnected(ComponentName name) {
            System.out.println("onServiceDisConnected");
            r_service = null;
            }
    };
    
    private ServiceConnection u_connection = new ServiceConnection(){

        public void onServiceConnected(ComponentName name, IBinder service) {
            u_service = uploadService.Stub.asInterface(service);
            System.out.println("onServiceConnected");
            }

        public void onServiceDisconnected(ComponentName name) {
            System.out.println("onServiceDisConnected");
            u_service = null;
            }
    };

    
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 9, 0, "Set Unlock Code");
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        
        if(raActivity.hidden) {
        
            System.out.println(codeLeft.substring(0, 1));
            
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
//                raActivity.reset();
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


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 9:
                AlertDialog.Builder alert2 = new AlertDialog.Builder(this);

                alert2.setTitle("Unlock Code");
                alert2.setMessage("What should your unlock code be? \n\n\t\tMenu:\t\t\t\t\t\t\tM\n\t\tBack:\t\t\t\t\t\t\tB\n\t\tVolume Up:\t\t\tU \n\t\tVolume Down:\t\tD");

                // Set an EditText view to get user input 
                final LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(R.layout.input, null);
                final EditText input = (EditText) ll.findViewById((R.id.input_et));
                input.setPadding(2, 2, 2, 2);
                input.setText(code);
                alert2.setView(ll);

                alert2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if(input.getText().toString() == "" || input.getText().toString() == null) {
                        return;
                    }
                    if(input.getText().toString().length() < 2) {
                        Toast.makeText(getBaseContext(), "Code is too short!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    code = input.getText().toString().toUpperCase();
                    final SharedPreferences.Editor editor;
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    editor = prefs.edit();
                    editor.putString("code", code);
                    editor.commit();
                    Toast.makeText(getBaseContext(), "Code Set!", Toast.LENGTH_SHORT).show();
                }});
                alert2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                  }
                });
                alert2.show();
                return true;
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
            .setMessage("Welcome to Stealth Video Recorder! Thanks for purchasing! :) \n\nThis app is very simple to use. First, hit Menu to enter your unlock code, then press the record button. This will blank the screen until you enter the unlock code, which will stop recording and let you watch your saved video. \n\nHappy recording!")
            .setPositiveButton("Okay!", null)
            .show();
            editor2 = prefs.edit();
            editor2.putString("first_time", "shitballs");
            editor2.commit();
        }
        
        code = prefs.getString("code", "MBUD");
        codeLeft = code;
        
        startService(new Intent(this, rService.class));
        startService(new Intent(this, uService.class));
        bindRecordService();
        bindUploadService();
       
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