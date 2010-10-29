package org.ale.stealthvideorecorder;

import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
    private boolean m_servicedBind = false;
    private VideoRecorder vr;
    private String code = "MBUD";
    private String codeLeft = "MBUD";
    RecorderActivity raActivity;
    MainActivity maActivity;
    private int vol;
    
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
                
                AlertDialog.Builder alert2 = new AlertDialog.Builder(this);

                alert2.setTitle("Recording Saved!");
                alert2.setMessage("Would you like to view your video now? If not, you can find the video in the Gallery once it refreshes.");

                alert2.setPositiveButton("Play Video", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Intent tostart = new Intent(Intent.ACTION_VIEW);
                    tostart.setDataAndType(Uri.parse(raActivity.vr.path), "video/*");
                    startActivity(tostart);
                    finish();
                }});
                alert2.setNegativeButton("No thank you, just quit!", new DialogInterface.OnClickListener() {
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
        raActivity.setFL(maActivity.getFL());
        
        AudioManager mgr = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        vol = mgr.getStreamVolume(AudioManager.STREAM_SYSTEM);
        mgr.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);

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