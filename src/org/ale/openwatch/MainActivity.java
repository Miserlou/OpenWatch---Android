package org.ale.openwatch;

import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
	/** This is called when the program first starts and is where variables
	 * are first initialized */
    public boolean recording = false;
    final Handler mHandler = new Handler();
    private boolean m_servicedBind = false;
    private VideoRecorder vr;
    private RecorderActivity ra;
    private MainActivityGroup mag;
    private LinearLayout root;
    
    Context c;
    
    @Override
    /* This is called when the activity is created (when the app is started).
    It initialized the layout */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        vr = (VideoRecorder) findViewById(R.id.camcorder_preview);
        c = this;
        
    }
    // TODO: Figure out what this does
    public void activateButton() {
        final Button ib = (Button) findViewById(R.id.ib);
        ib.setClickable(true);
        ib.setBackgroundResource(R.drawable.button);
        recording = false;
    }
    // Called whenever the activity regains focus
    public void onResume() {
        super.onResume();
        final Button ib = (Button) findViewById(R.id.ib);
        root = (LinearLayout) findViewById(R.id.root);
        final Context c = this;

        final MainActivity ma = this;
        // Loads preferences
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final SharedPreferences.Editor editor;
        editor = prefs.edit();
        /** Checks to see if the user clicked record
         *  and if they did, starts main activity in
         *  RecorderActivity.java
         */ 
   
        final OnTouchListener realOTL = new OnTouchListener() {

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
                                ib.setClickable(false);
                                ra.start();
                            }}, 400);

                        ib.setBackgroundResource(R.drawable.buttonpressed);
                        return true;
                        }
            }
        };
        //Warns user about how video recording works, calls realOTL 
        final OnTouchListener fakeOTL = new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                
                if(event.getAction() != MotionEvent.ACTION_DOWN) {
                    return false;
                }

                    new AlertDialog.Builder(c)
                    .setMessage("When you hit Record Video, the screen will go BLACK. It IS recording when the screen is black! To finish recording and upload, press the BACK button 3 times. Pressing HOME will also stop the recording. ")
                    .setPositiveButton("Okay!", new OnClickListener() {
        
                        public void onClick(DialogInterface dialog, int which) {
                            ib.setOnTouchListener(realOTL);
                            
                        }})
                    .setTitle("IMPORTANT!!!")
                    .show();
                    
                    editor.putString("warned", "shitballs");
                    editor.commit();

                return true;
            }
        };
        
        String first = prefs.getString("warned", "fuck");
        if(first.contains("fuck")){
            ib.setOnTouchListener(fakeOTL);
        }
        else{
            ib.setOnTouchListener(realOTL);
        }
        
       if(recording) {
           ib.setBackgroundResource(R.drawable.buttonpressed);
       }
       
       final Button b = (Button) findViewById(R.id.aib);
       boolean running = prefs.getBoolean("running", false);
       /** If the recorder is running, kill the recorder. This runs every time we
        * give the main activity focus again (such as if after recording we start
        * openwatch again)
        */
       if(running){
           
           final Runnable stopper = new Runnable() {
               public void run(){
                   if(mag.r_service==null){
                       System.out.println("Null RSERVICE");
                       mHandler.postDelayed(this, 100);
                   }
                   else{
                       try {
                           if(mag.r_service.running()){
                               mag.r_service.stop();
                               editor.putBoolean("running", false);
                               editor.commit();
                               Toast.makeText(c, "Recording stopped!", Toast.LENGTH_SHORT).show();
                               stopService(new Intent(c, rService.class));
                               mag.r_service = null;
                               b.setVisibility(4);
                               AlertDialog.Builder alert2 = new AlertDialog.Builder(c);

                               alert2.setTitle(getString(R.string.recording_saved));
                               alert2.setMessage(getString(R.string.upload_recording_now));
                               alert2.setPositiveButton(getString(R.string.yes_upload), new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int whichButton) {
                                       mHandler.post(new Runnable() {

                                           public void run() {
                                               Intent mainIntent = new Intent(c, DescribeActivity.class); 
                                               startActivity(mainIntent);
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
                       } catch (RemoteException e) {
                           e.printStackTrace();
                       }
                   }
               }
           };
           
           mHandler.postDelayed(stopper,100);
           
       }
       // TODO: This starts Recording Service/Activity? , but not sure how or why
       b.setOnTouchListener(new OnTouchListener() {

           public boolean onTouch(View v, MotionEvent event) {
               
               if(event.getAction() != MotionEvent.ACTION_DOWN) {
                   return false;
               }
               
                   mHandler.postDelayed(new Runnable() {

                       public void run() {
                           try {
                            mag.r_service.start();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                       }
                   }, 400);
               b.setPressed(false);
               b.setClickable(false);
                   
               editor.putBoolean("running", true);
               editor.commit();
               Toast.makeText(c, "Recording started!", Toast.LENGTH_SHORT).show();
               b.setBackgroundResource(R.drawable.buttonpressed);
               finish();
               return true;
                       }
           
       });
       /** Picks a random number and displays a random quote defined from res/drawable-hdpi
        * For some reason these tags are .png files. Unless there is any particular reason
        * for this, they should just be changed to text strings! TODO: Look into this
        */
      
       ImageView tag = (ImageView)findViewById(R.id.tag);
       int rand = new Random().nextInt(10);
       switch(rand){
       case 0:
           tag.setImageResource(R.drawable.tag1);
           return;
       case 1:
           tag.setImageResource(R.drawable.tag2);
           return;
       case 2:
           tag.setImageResource(R.drawable.tag3);
           return;
       case 3:
           tag.setImageResource(R.drawable.tag4);
           return;
       case 4:
           tag.setImageResource(R.drawable.tag5);
           return;
       case 5:
           tag.setImageResource(R.drawable.tag6);
           return;
       case 6:
           tag.setImageResource(R.drawable.tag7);
           return;
       case 7:
           tag.setImageResource(R.drawable.tag8);
           return;
       case 8:
           tag.setImageResource(R.drawable.tag9);
           return;
       case 9:
           tag.setImageResource(R.drawable.tag10);
           return;
       
       }
   }
   
// TODO: What is this?
    public void setRecorderActivity(RecorderActivity raa) {
        ra = raa;
    }
// TODO: What is this?
    public void setParentGroup(MainActivityGroup magg) {
        mag = magg;
    }
// TODO: What is this?
    public FrameLayout getFL() {
        return (FrameLayout)findViewById(R.id.Recorder);
    }
    
}